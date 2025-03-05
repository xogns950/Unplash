package com.example.paging3demo.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.paging3demo.data.local.UnsplashDatabase
import com.example.paging3demo.data.remote.UnsplashApi
import com.example.paging3demo.model.UnsplashImage
import com.example.paging3demo.model.UnsplashRemoteKeys
import com.example.paging3demo.util.Constants.ITEMS_PER_PAGE
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class UnsplashRemoteMediator @Inject constructor(
    private val unsplashApi: UnsplashApi,
    private val unsplashDatabase: UnsplashDatabase
): RemoteMediator<Int, UnsplashImage>() {

    private val unsplashImageDao =  unsplashDatabase.unsplashImageDao()
    private val unsplashRemoteKeysDao =  unsplashDatabase.unsplashRemoteKeysDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, UnsplashImage>
    ): MediatorResult {
        return try {
            val currentPage = when (loadType) {
                LoadType.REFRESH -> {//  초기 로드 또는 새로고침.
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextPage?.minus(1) ?: 1
                }

                LoadType.PREPEND -> { // 데이터 페이지를 로드하고 목록의 시작 부분에 추가.
                   val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prevPage = remoteKeys?.prevPage
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = remoteKeys != null
                        )
                    prevPage
                }
                LoadType.APPEND -> { // 데이터 페이지를 로드하고 목록의 끝에 추가.
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextPage = remoteKeys?.nextPage
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = remoteKeys != null
                        )
                    nextPage
                }
            }

            val response = unsplashApi.getAllImages(page = currentPage, perPage = ITEMS_PER_PAGE)
            val endOfPaginationReached = response.isEmpty()

            val prevPage = if (currentPage == 1) null else currentPage - 1
            val nextPage = if (endOfPaginationReached) null else currentPage + 1

            unsplashDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    unsplashImageDao.deleteAllImages()
                    unsplashRemoteKeysDao.deleteAllRemoteKeys()
                }
                val keys = response.map { unsplashImage ->
                    UnsplashRemoteKeys(
                        id = unsplashImage.id,
                        prevPage = prevPage,
                        nextPage = nextPage
                    )
                }
                unsplashRemoteKeysDao.addAllRemoteKeys(remoteKeys = keys)
                unsplashImageDao.addImages(images = response)
            }
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
        private suspend fun getRemoteKeyClosestToCurrentPosition(
            state: PagingState<Int, UnsplashImage>
        ): UnsplashRemoteKeys? {
            return state.anchorPosition?.let { position ->
                state.closestItemToPosition(position)?.id?.let { id ->
                    unsplashRemoteKeysDao.getRemoteKeys(id = id)
                }
            }
        }

        private suspend fun getRemoteKeyForFirstItem(
            state: PagingState<Int, UnsplashImage>
        ): UnsplashRemoteKeys? {
            return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
                ?.let { unsplashImage ->
                    unsplashRemoteKeysDao.getRemoteKeys(id = unsplashImage.id)
                }
        }

        private suspend fun getRemoteKeyForLastItem(
            state: PagingState<Int, UnsplashImage>
        ): UnsplashRemoteKeys? {
            return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
                ?.let { unsplashImage ->
                    unsplashRemoteKeysDao.getRemoteKeys(id = unsplashImage.id)
                }
        }
}