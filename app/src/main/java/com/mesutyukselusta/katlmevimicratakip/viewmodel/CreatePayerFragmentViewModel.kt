package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mesutyukselusta.katlmevimicratakip.db.PayerDatabase
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import kotlinx.coroutines.launch
import kotlin.math.log

class CreatePayerFragmentViewModel(application: Application) : BaseViewModel(application) {
     val createPayerInputControl = MutableLiveData<Boolean>()
     val createPayerInsertControl = MutableLiveData<Boolean>()
     val createPayerInsertFirebaseControl = MutableLiveData<Boolean>()
     val createPayerInsertFirebaseErrorMessage = MutableLiveData<String>()

    private  val TAG = "CreatePayerFragmentView"

    fun validateControl(name : String,surname : String,documentType : String,documentNo : String,documentYear : String,
                        dateDay : Int, dateMonth : Int, dateYear : Int,documentTypeIsBill : Boolean,createdMainDebt : String,trackingAmount : String)  {
        createPayerInputControl.value = name.isNotEmpty() && surname.isNotEmpty() &&
                documentType.isNotEmpty() && documentNo.isNotEmpty() && documentYear.isNotEmpty() && createdMainDebt.isNotEmpty() && trackingAmount.isNotEmpty()
                && documentNo != "-1"
                && documentYear != "-1"
                && createdMainDebt != "-1"
                && trackingAmount != "-1"
                && dateDay != -1
                && dateMonth != -1
                && dateYear != -1

        if (createPayerInputControl.value == true) {
            var cleanMainDebt  = cleanCastingAmountText(createdMainDebt)
            var cleanTrackingAmount  = cleanCastingAmountText(trackingAmount)
            insertPayer(name,surname,documentType,Integer.parseInt(documentNo),
                Integer.parseInt(documentYear),dateDay,dateMonth,dateYear,documentTypeIsBill,Integer.parseInt(cleanMainDebt),Integer.parseInt(cleanTrackingAmount))
        }

    }

    private fun insertPayer(name : String, surname : String, documentType : String, documentNo : Int, documentYear: Int, dateDay : Int,
                            dateMonth : Int, dateYear : Int,documentTypeIsBill: Boolean,createdMainDebt: Int,trackingAmount : Int){
        val createDay = "$dateDay/$dateMonth/$dateYear"
        val payerInfo = PayerInfo(name,surname,documentNo,documentYear,documentType,0,0,0,
            0,0,createDay,documentTypeIsBill,createdMainDebt,
            false,trackingAmount,"new_document","")
        payerInfo.tuition_fee = payerInfo.calculateTuitionFee(trackingAmount,false,0)
        insertPayerToDB(payerInfo)
    }

    private fun cleanCastingAmountText(castingAmount : String) : String{
        var cleanText = castingAmount.substring(1)
        cleanText = cleanText.replace(",","")
        cleanText = cleanText.replace(".","")
        return cleanText
    }

    private fun insertPayerToFirestore(payerInfo: PayerInfo){
        val fireStore = Firebase.firestore

        //Create DataMap
        val dataMap = hashMapOf<String,Any>()
        dataMap["name"] = payerInfo.name
        dataMap["surname"] = payerInfo.surname!!
        dataMap["document_no"] = payerInfo.document_no!!
        dataMap["document_year"] = payerInfo.document_year!!
        dataMap["document_type"] = payerInfo.document_type!!
        dataMap["main_debt"] = payerInfo.main_debt!!
        dataMap["interest"] = payerInfo.interest!!
        dataMap["proxy_cost"] = payerInfo.proxy!!
        dataMap["costs"] = payerInfo.costs!!
        dataMap["tuition_fee"] = payerInfo.tuition_fee!!
        dataMap["document_creation_date"] = payerInfo.document_creation_date!!
        dataMap["document_type_is_bill"] = payerInfo.document_type_is_bill!!
        dataMap["created_main_debt"] = payerInfo.created_main_debt!!
        dataMap["is_foreclosure"] = payerInfo.is_foreclosure!!
        dataMap["tracking_amount"] = payerInfo.tracking_amount!!
        dataMap["document_status"] = payerInfo.document_status!!

        fireStore.collection("PayerInfo").add(dataMap).addOnSuccessListener {
            createPayerInsertFirebaseControl.value = true
            // Set fireStoreDocumentNo

            val fireStoreDocumentNo =  it.path.substring(10)
            payerInfo.firestore_document_no = fireStoreDocumentNo

            insertPayerToDB(payerInfo)

        }.addOnFailureListener {
            createPayerInsertFirebaseErrorMessage.value = it.localizedMessage
        }
    }

    private fun insertPayerToDB(payerInfo: PayerInfo){
        val dao = PayerDatabase(getApplication()).payerDao()

        launch {
            dao.insertPayerInfo(payerInfo)
            createPayerInsertControl.value = true
        }
    }
}