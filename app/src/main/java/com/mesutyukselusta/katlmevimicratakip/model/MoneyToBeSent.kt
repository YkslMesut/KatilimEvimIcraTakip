package com.mesutyukselusta.katlmevimicratakip.model

data class MoneyToBeSent(
    val main_debt : Int,
    val interest : Int,
    val costs : Int,
    val total_debt : Int,
) {
}