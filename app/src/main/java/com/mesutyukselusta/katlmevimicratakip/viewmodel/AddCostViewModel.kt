package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mesutyukselusta.katlmevimicratakip.db.PayerDatabase
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import kotlinx.coroutines.launch

class AddCostViewModel(application: Application) : BaseViewModel(application) {
    private  val TAG = "AddCostViewModel"
    val addCostInputControl = MutableLiveData<Boolean>()
    val addCostInsertControl = MutableLiveData<Boolean>()
    val addCostStatusMessage = MutableLiveData<String>()

    val db = Firebase.firestore
    private val dao = PayerDatabase(getApplication()).payerDao()

    fun validateControl(context: Context,costName : String,costAmount : String,fireStoreDocumentNo: String,
                        dateDay : Int,dateMonth : Int,dateYear : Int,isAdvanceFee : Boolean,isProtestCost : Boolean)  {
        addCostInputControl.value = costName.isNotEmpty() && costAmount.isNotEmpty() && fireStoreDocumentNo.isNotEmpty() &&
                dateDay != -1 &&
                dateMonth != -1 &&
                dateYear != -1
        if (addCostInputControl.value == true) {
            var cleanCost  = costAmount
            cleanCost = cleanCost.substring(1)
            cleanCost = cleanCost.replace(",","")
            cleanCost = cleanCost.replace(".","")
            insertCost(context,costName,cleanCost,fireStoreDocumentNo,dateDay,dateMonth,dateYear,isAdvanceFee,isProtestCost)
        }

    }

    private fun insertCost(context: Context,costName : String, costAmount: String, fireStoreDocumentNo : String, dateDay : Int, dateMonth : Int, dateYear : Int,isAdvanceFee : Boolean,isProtestCost: Boolean){
        val mCostAmount = Integer.parseInt(costAmount)
        val costs = Costs(mCostAmount,costName,dateDay,dateMonth,dateYear,fireStoreDocumentNo,isAdvanceFee,isProtestCost,"")
        insertCostToFireStore(context,costs)

    }
    private fun insertCostToLocalDB(cost: Costs) {
        launch {
            dao.insertCosts(cost)
        }
    }

    private fun insertCostToFireStore(context: Context,cost: Costs){

        //Create DataMap
        val dataMap = hashMapOf<String,Any>()
        dataMap["amount_of_expense"] =  cost.amount_of_expense!!
        dataMap["cost_name"] = cost.cost_name!!
        dataMap["date_day"] =  cost.date_day!!
        dataMap["date_month"] = cost.date_month!!
        dataMap["date_year"] =  cost.date_year!!
        dataMap["firestore_document_no"] = cost.firestore_document_no
        dataMap["advance_fee"] =  cost.advance_fee!!
        dataMap["protest_cost"] =  cost.protest_cost!!


        db.collection("Costs").add(dataMap).addOnSuccessListener {
            // Set fireStoreDocumentNo
            val fireStoreCostDocumentNo =  it.path.substring(6)
            cost.firestore_cost_document_no = fireStoreCostDocumentNo
            // Get Costs From Fire Store And Add Total Cost To Payer Info
            getCostsFromFireStore(cost.firestore_document_no)
            // Added To Local DB
            insertCostToLocalDB(cost)

        }.addOnFailureListener {
            val errorMessage = it.localizedMessage
            addCostStatusMessage.value = errorMessage
            addCostInsertControl.value = false
             }
    }

    private fun updatePayersCostValue(fireStoreDocumentNo: String,totalCost : Number){

        db.collection("PayerInfo").document(fireStoreDocumentNo).update("costs",totalCost).addOnSuccessListener {
            addCostInsertControl.value = true
        }.addOnFailureListener {
            addCostStatusMessage.value = it.localizedMessage
        }

    }
    private fun getCostsFromFireStore(fireStoreDocumentNo: String){

        db.collection("Costs").whereEqualTo("firestore_document_no",fireStoreDocumentNo).get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty){

                    var totalCost = 0
                    val documents = result.documents
                    val costList = castCostData(documents)

                    for (cost in costList){
                        totalCost += cost.amount_of_expense!!
                    }
                    updatePayersCostValue(fireStoreDocumentNo,totalCost)
                }

            }
            .addOnFailureListener {

            }
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

}