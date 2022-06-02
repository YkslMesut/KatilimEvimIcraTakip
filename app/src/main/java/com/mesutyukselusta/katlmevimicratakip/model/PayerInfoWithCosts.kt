package com.mesutyukselusta.katlmevimicratakip.model

import androidx.room.Embedded
import androidx.room.PrimaryKey
import androidx.room.Relation

class PayerInfoWithCosts(
    @Embedded val payerInfo : PayerInfo,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "uuid"
    )
    val costs : List<Costs>
    ){

}