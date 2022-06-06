package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mesutyukselusta.katlmevimicratakip.db.PayerDatabase
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import kotlinx.coroutines.launch
import kotlin.math.log

class CreatePayerFragmentViewModel(application: Application) : BaseViewModel(application) {
     val createPayerInputControl = MutableLiveData<Boolean>()
     val createPayerInsertControl = MutableLiveData<Boolean>()
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
        Log.d(TAG, "insertPayer: InsertPayerFunc")
        val createDay = "$dateDay/$dateMonth/$dateYear"
        val payerInfo = PayerInfo(name,surname,documentNo,documentYear,documentType,null,null,null,
            null,null,createDay,documentTypeIsBill,createdMainDebt,
            false,trackingAmount,"new_document")
        payerInfo.tuition_fee = payerInfo.calculateTuitionFee(trackingAmount,false,0)
        val dao = PayerDatabase(getApplication()).payerDao()
        launch {
            Log.d(TAG, "insertPayerLaunch: ")
            dao.insertPayerInfo(payerInfo)
            createPayerInsertControl.value = true
        }
    }

    private fun cleanCastingAmountText(castingAmount : String) : String{
        var cleanText = castingAmount.substring(1)
        cleanText = cleanText.replace(",","")
        cleanText = cleanText.replace(".","")
        return cleanText
    }


}