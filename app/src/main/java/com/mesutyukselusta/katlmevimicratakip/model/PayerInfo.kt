package com.mesutyukselusta.katlmevimicratakip.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class PayerInfo(

    @ColumnInfo(name = "name")
    @SerializedName("name")
    val name : String,

    @ColumnInfo(name = "surname")
    @SerializedName("surname")
    val surname: String?,

    @ColumnInfo(name = "document_no")
    @SerializedName("document_no")
    val document_no: Int?,

    @ColumnInfo(name = "document_year")
    @SerializedName("document_year")
    val document_year: Int?,

    @ColumnInfo(name = "document_type")
    @SerializedName("document_type")
    val document_type : String?,

    @ColumnInfo(name = "main_debt")
    @SerializedName("main_debt")
    val main_debt : Int?,

    @ColumnInfo(name = "interest")
    @SerializedName("interest")
    var interest: Int?,

    @ColumnInfo(name = "proxy_cost")
    @SerializedName("proxy_cost")
    val proxy : Int?,

    @ColumnInfo(name = "costs")
    @SerializedName("costs")
    val costs : Int?,

    @ColumnInfo(name = "tuition_fee")
    @SerializedName("tuition_fee")
    var tuition_fee : Int?,

    @ColumnInfo(name = "document_creation_date")
    @SerializedName("document_creation_date")
    val document_creation_date : String?,

    @ColumnInfo(name = "document_type_is_bill")
    @SerializedName("document_type_is_bill")
    val document_type_is_bill : Boolean?,

    @ColumnInfo(name = "created_main_debt")
    @SerializedName("created_main_debt")
    val created_main_debt : Int?,

    @ColumnInfo(name = "is_foreclosure")
    @SerializedName("is_foreclosure")
    val is_foreclosure : Boolean?,

    @ColumnInfo(name = "tracking_amount")
    @SerializedName("tracking_amount")
    val tracking_amount : Int?,

    @ColumnInfo(name = "document_status")
    @SerializedName("document_status")
    var document_status : String?,

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "firestore_document_no")
    @SerializedName("firestore_document_no")
    var firestore_document_no : String,
){

     fun calculateTuitionFee(tracking_amount: Int?,isForeclosure: Boolean?,advanceFee : Int) : Int{
         val trackingAmount : Int = tracking_amount!!
         var  trackingPercentage = if (isForeclosure == true){
            4.55
        } else {
            2.25
        }
         val  result =  (((trackingAmount * trackingPercentage)/100).toInt())- advanceFee
         return result
    }


}