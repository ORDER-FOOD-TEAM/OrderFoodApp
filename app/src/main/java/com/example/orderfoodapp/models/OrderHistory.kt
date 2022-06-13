package com.example.orderfoodapp.models

data class OrderHistory(
    var id: String,
    var total: Double,
    var num: Int,
    var time: String,
    var status: String,
    var address: String
)
