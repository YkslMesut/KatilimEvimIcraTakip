package com.mesutyukselusta.katlmevimicratakip.model

import androidx.room.Embedded
import androidx.room.PrimaryKey
import androidx.room.Relation

class PayerInfoWithCosts(
    @Embedded val payerInfo : PayerInfo,
    @Relation(
        parentColumn = "firestore_document_no",
        entityColumn = "firestore_document_no"
    )
    val costs : List<Costs>
    ){

}