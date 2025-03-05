package com.example.paging3demo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.paging3demo.screen.home.HomeScreen
import com.example.paging3demo.screen.search.SearchScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ){
        composable(route = Screen.Home.route){
            HomeScreen(navController =navController)
        }
        composable(route = Screen.Search.route){
            SearchScreen(navController =navController)
        }
    }
}