package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PayerDetailsViewModel(application: Application) : BaseViewModel(application) {
    private val TAG = "PayerDetailsViewModel"

    val payerLiveData = MutableLiveData<PayerInfo>()
    val updateControl = MutableLiveData<Boolean>()
    val payerLiveDataStatusMessage = MutableLiveData<String>()

    private val db = Firebase.firestore



    fun getPayerFromFireStore(fireStoreDocumentNo: String){
        db.collection("PayerInfo").document(fireStoreDocumentNo).get().addOnSuccessListener { result ->
            if (result != null){

                val payer = castPayerData(result)
                val calculatedInterest = calculateInterest(payer.created_main_debt!!,
                    payer.document_type_is_bill!!,payer.document_creation_date!!)

                if (calculatedInterest != payer.interest){
                    payer.interest = calculatedInterest
                    updateInterestFromFireStore(payer,calculatedInterest)
                } else {
                    showPayers(payer)
                }
            }
        }.addOnFailureListener {
            payerLiveDataStatusMessage.value = it.localizedMessage
        }
    }


    private fun getCostsFromFireStore(fireStoreDocumentNo: String) : List<Costs>{
        var costList = ArrayList<Costs>()

        db.collection("Costs").whereEqualTo("firestore_document_no",fireStoreDocumentNo).get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty){
                    val documents = result.documents
                    costList = castCostData(documents)

                } else {
                    val emptyResult = "Hiç Masraf Bulunmamaktadır"
                    payerLiveDataStatusMessage.value = emptyResult
                }
            }
            .addOnFailureListener {
                payerLiveDataStatusMessage.value = it.localizedMessage
            }
        return costList
    }


    private fun showPayers(payer : PayerInfo) {
        calculateCostAndShowPayer(payer)

    }

    fun updatePayer(payerInfo: PayerInfo ,mainDebt : String,
                    proxy : String,isForeClosure : Boolean ,costs : String){

        if (payerInfo != null && mainDebt.isNotEmpty()  && proxy.isNotEmpty()){

            val  mainDebtClean = cleanCastingAmountText(mainDebt)

            if (payerInfo.created_main_debt!! >= Integer.parseInt(mainDebtClean)){

                val costsClean = cleanCastingAmountText(costs)
                var mCost = costsClean
                if (mCost.isEmpty()){
                    mCost = "0"
                }

                val selectedPayerInfo = payerInfo
                val proxyClean = cleanCastingAmountText(proxy)
                val advanceFee = calculateAdvanceFee(getCostsFromFireStore(payerInfo.firestore_document_no))
                val tuitionFeeClean = payerInfo.calculateTuitionFee( payerInfo.tracking_amount,
                    isForeClosure,advanceFee)
                val newPayer = PayerInfo(selectedPayerInfo.name,selectedPayerInfo.surname,
                    selectedPayerInfo.document_no,selectedPayerInfo.document_year,
                    selectedPayerInfo.document_type,Integer.parseInt(mainDebtClean),
                    calculateInterest(selectedPayerInfo.created_main_debt!!,selectedPayerInfo.document_type_is_bill!!,
                        selectedPayerInfo.document_creation_date!!),
                    Integer.parseInt(proxyClean),
                    Integer.parseInt(mCost),
                    tuitionFeeClean,selectedPayerInfo.document_creation_date,selectedPayerInfo.document_type_is_bill,
                    selectedPayerInfo.created_main_debt,isForeClosure,selectedPayerInfo.tracking_amount,selectedPayerInfo.document_status,selectedPayerInfo.firestore_document_no)
                newPayer.firestore_document_no = selectedPayerInfo.firestore_document_no
                updatePayerFromFireStore(newPayer)
            } else {
                updateControl.value = false
                val debtControl = "Girilen Ana Borç Oluştulan Borçtan Fazla olamaz"
                payerLiveDataStatusMessage.value = debtControl
            }
        } else {
            updateControl.value = false
            val fillTextView = "Lütfen Gerekli Alanları Doldurunuz"
            payerLiveDataStatusMessage.value = fillTextView
        }

    }

    private fun updatePayerFromFireStore(payerInfo: PayerInfo){
        val dataMap = createDataMap(payerInfo)
        db.collection("PayerInfo").document(payerInfo.firestore_document_no).set(dataMap).addOnSuccessListener {
            updateControl.value = true
            val success = "Başarıyla Güncellendi"
            payerLiveDataStatusMessage.value = success
        } . addOnFailureListener {
            payerLiveDataStatusMessage.value = it.localizedMessage
        }
    }

    private fun calculateInterest (createdMainDebt : Int,documentTypeIsBill : Boolean,documentCreatedDay : String) : Int{

        val now = currentDate()
        val sdf = SimpleDateFormat("dd/MM/yyyy")

        val createdDate: Date = sdf.parse(now)
        val currentDate: Date = sdf.parse(documentCreatedDay)

        val dayDiffWithTime = currentDate.time - createdDate.time
        val resultDayDiff = TimeUnit.DAYS.convert(dayDiffWithTime, TimeUnit.MILLISECONDS)
        val dayOfYear = 365

        var interestPercentage : Double = if (documentTypeIsBill) {
            15.75
        } else {
            9.0
        }

        val result = ((createdMainDebt * interestPercentage * resultDayDiff) / (dayOfYear * 100)).toInt()
        return result



    }

    private fun calculateAdvanceFee(costs : List<Costs>) : Int {
        var advanceFee = 0
        for (cost in costs) {
            if (cost.advance_fee == true) {
                advanceFee += cost.amount_of_expense!!
            }
        }
        return advanceFee
    }

    private fun currentDate(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    private fun cleanCastingAmountText(castingAmount : String) : String{
        var cleanText = castingAmount.substring(1)
        cleanText = cleanText.replace(",","")
        cleanText = cleanText.replace(".","")
        return cleanText
    }

    /*private fun updateInterestFromLocalDB(payerWithCosts: PayerInfoWithCosts, updatedInterest : Int){

        launch {
            payerWithCosts.payerInfo.interest = updatedInterest
            PayerDatabase(getApplication()).payerDao().updatePayer(payerWithCosts.payerInfo)
            showPayers(payerWithCosts)
        }

    }*/

    private fun updateInterestFromFireStore(payer: PayerInfo,updatedInterest: Number){
        db.collection("PayerInfo").document(payer.firestore_document_no).update("interest",updatedInterest).addOnSuccessListener {
            showPayers(payer)
        }.addOnFailureListener {
            payerLiveDataStatusMessage.value = it.localizedMessage
        }
    }

    private fun castPayerData(document : DocumentSnapshot) : PayerInfo{

        val fireStoreDocumentNo = document.reference.path.substring(10)
        val costs  = (document.get("costs") as Number).toInt()
        val createdMainDebt  = (document.get("created_main_debt") as Number).toInt()
        val documentCreationDate  = document.get("document_creation_date") as String
        val documentNo  = (document.get("document_no") as Number).toInt()
        val documentStatus  = document.get("document_status") as String
        val documentType  = document.get("document_type") as String
        val documentTypeIsBill  = document.get("document_type_is_bill") as Boolean
        val documentYear  = (document.get("document_year") as Number).toInt()
        val interest  = (document.get("interest") as Number).toInt()
        val isForeclosure  = document.get("is_foreclosure") as Boolean
        val mainDebt  = (document.get("main_debt") as Number).toInt()
        val name  = document.get("name") as String
        val proxyCost = (document.get("proxy_cost") as Number).toInt()
        val surname  = document.get("surname") as String
        val trackingAmount = (document.get("tracking_amount") as Number).toInt()
        val tuitionFee = (document.get("tuition_fee") as Number).toInt()

        val payer = PayerInfo(name,surname,documentNo,documentYear,documentType,mainDebt,interest,proxyCost,
            costs,tuitionFee,documentCreationDate,documentTypeIsBill,createdMainDebt,isForeclosure,trackingAmount,documentStatus,fireStoreDocumentNo)


        return payer
    }

    private fun castCostData(documents : List<DocumentSnapshot>) : ArrayList<Costs>{
        val costsList = ArrayList<Costs>()

        for (document in documents) {
            val fireStoreCostDocumentNo = document.reference.path.substring(6)
            val amountOfExpense = (document.get("amount_of_expense") as Number).toInt()
            val dateDay = (document.get("date_day") as Number).toInt()
            val dateMonth = (document.get("date_month") as Number).toInt()
            val dateYear = (document.get("date_year") as Number).toInt()
            val costName = document.get("cost_name") as String
            val fireStoreDocumentNo = document.get("firestore_document_no") as String
            val advanceFee = document.get("advance_fee") as Boolean
            val protestCost = document.get("protest_cost") as Boolean


            val cost = Costs(amountOfExpense,
                costName,
                dateDay,
                dateMonth,
                dateYear,
                fireStoreDocumentNo,
                advanceFee,
                protestCost,
                fireStoreCostDocumentNo)

            costsList.add(cost)
        }
        return costsList
    }

    private fun calculateTotalCost(documents : List<DocumentSnapshot>) : Int {
        var totalCost = 0

        for (document in documents) {
            val amountOfExpense = (document.get("amount_of_expense") as Number).toInt()
            totalCost += amountOfExpense
        }

        return totalCost
    }

    private fun createDataMap(payerInfo: PayerInfo) : HashMap<String,Any>{
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
        return dataMap
    }

    private fun calculateCostAndShowPayer(payerInfo: PayerInfo){
        var totalCost = 0
        db.collection("Costs").whereEqualTo("firestore_document_no",payerInfo.firestore_document_no).get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty){
                    val documents = result.documents
                    totalCost = calculateTotalCost(documents)
                    payerInfo.costs = totalCost
                }
            }
            .addOnFailureListener {
                payerLiveDataStatusMessage.value = it.localizedMessage
            }
        payerLiveData.value = payerInfo
    }



}