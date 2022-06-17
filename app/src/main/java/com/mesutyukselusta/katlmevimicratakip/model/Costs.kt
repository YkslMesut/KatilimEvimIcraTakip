package com.mesutyukselusta.katlmevimicratakip.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Costs(

    @ColumnInfo(name = "amount_of_expense")
    @SerializedName("amount_of_expense")
    var amount_of_expense : Int?,

    @ColumnInfo(name = "cost_name")
    @SerializedName("cost_name")
    var cost_name : String?,

    @ColumnInfo(name = "date_day")
    @SerializedName("date_day")
    var date_day : Int?,

    @ColumnInfo(name = "date_month")
    @SerializedName("date_month")
    var date_month : Int?,

    @ColumnInfo(name = "date_year")
    @SerializedName("date_year")
    var date_year : Int?,

    @ColumnInfo(name = "firestore_document_no")
    @SerializedName("firestore_document_no")
    val uuid: String,
    @ColumnInfo(name = "advance_fee")
    @SerializedName("advance_fee")
    val advance_fee: Boolean?,
    @ColumnInfo(name = "protest_cost")
    @SerializedName("protest_cost")
    val protest_cost: Boolean?
    ){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "cost_uuid")
    @SerializedName("cost_uuid")
    var cost_uuid: Int = 0


}