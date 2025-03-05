package com.example.paging3demo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Urls(
    @SerialName("regular")
    val regularImage :String
)
