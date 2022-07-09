package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import com.mesutyukselusta.katlmevimicratakip.model.MoneyToBeSent
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class CostCalculateViewModel(application: Application) : BaseViewModel(application) {
    private  val TAG = "CostCalculateViewModel"
    val moneyToBeSentData = MutableLiveData<MoneyToBeSent>()
    lateinit var selectedPayer : PayerInfo
    val payerLiveDataStatusMessage = MutableLiveData<String>()

    private val db = Firebase.firestore

    fun getPayerFromFireStore(fireStoreDocumentNo: String){
        db.collection("PayerInfo").document(fireStoreDocumentNo).get().addOnSuccessListener { result ->
            if (result != null){
                val payer = castPayerData(result)
                selectedPayer = payer
            }
        }.addOnFailureListener {
            payerLiveDataStatusMessage.value = it.localizedMessage
        }
    }

    fun calculateAllDebt(closingDay: Int, closingMonth : Int, closingYear : Int) {

        val mainDebt : Int = selectedPayer.main_debt!!
        val interest : Int = calculateInterest(selectedPayer.created_main_debt!!,selectedPayer.document_type_is_bill!!,selectedPayer.document_creation_date!!,closingDay,closingMonth,closingYear)

        getCostsFromFireStore(selectedPayer,closingMonth,closingYear,mainDebt,interest)


    }

    private fun calculateAllData (mainDebt : Int , interest : Int , costs : Int, totalDebt : Int){
        val calculatedData = MoneyToBeSent(mainDebt,interest,costs,totalDebt)
        moneyToBeSentData.value = calculatedData
    }

    private fun calculateInterest (createdMainDebt : Int,documentTypeIsBill : Boolean,documentCreatedDay : String,closingDay: Int,
                                   closingMonth: Int,closingYear: Int) : Int{

        val closingDateInt = "$closingDay/$closingMonth/$closingYear"
        val sdf = SimpleDateFormat("dd/MM/yyyy")

        val closingDate: Date = sdf.parse(closingDateInt)
        val currentDate: Date = sdf.parse(documentCreatedDay)

        val dayDiffWithTime = closingDate.time - currentDate.time
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

    private fun calculateCosts(
        costList: ArrayList<Costs?>,
        closingMonth: Int,
        closingYear: Int,
        mainDebt: Int,
        interest: Int
    )  {
        if (costList != null) {
            var totalCost = 0
            for (cost in costList) {
                if (cost!!.protest_cost == true){
                    totalCost += cost.amount_of_expense!!

                } else {
                    if (cost.date_year!! < closingYear) {
                        totalCost += cost.amount_of_expense!!
                    } else {
                        if (cost.date_month!! < closingMonth) {
                            totalCost += cost.amount_of_expense!!
                        }
                    }
                }

            }
            val totalDebt : Int = mainDebt + interest + totalCost + selectedPayer.tuition_fee!! + selectedPayer.proxy!!
            calculateAllData(mainDebt,interest,totalCost,totalDebt)
        } else {
            val totalDebt : Int = mainDebt + interest + 0 + selectedPayer.tuition_fee!! + selectedPayer.proxy!!
            calculateAllData(mainDebt,interest,0,totalDebt)

        }

    }

    private fun castPayerData(document : DocumentSnapshot) : PayerInfo {

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

    private fun getCostsFromFireStore(
        payerInfo: PayerInfo,
        closingMonth: Int,
        closingYear: Int,
        mainDebt: Int,
        interest: Int
    ) : ArrayList<Costs?> {
        var costList: ArrayList<Costs?> = ArrayList()
        db.collection("Costs").whereEqualTo("firestore_document_no",payerInfo.firestore_document_no).get()
            .addOnSuccessListener { result ->
                val documents = result.documents
                costList.addAll(castCostData(documents))
                calculateCosts(costList, closingMonth, closingYear, mainDebt, interest)

            }
            .addOnFailureListener {
                Log.d(TAG, "getCostsFromFireStore: " + it.localizedMessage)
            }
       return costList
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
            val advanceFee = document.get("advance_fee") as Boolean
            val protestCost = document.get("protest_cost") as Boolean
            val fireStoreDocumentNo = document.get("firestore_document_no") as String


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
}