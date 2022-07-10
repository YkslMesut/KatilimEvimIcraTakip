package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mesutyukselusta.katlmevimicratakip.db.PayerDatabase
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import kotlinx.coroutines.launch

class CostDetailsViewModel(application: Application) : BaseViewModel(application) {
    private  val TAG = "CostDetailsViewModel"
    val costLiveData = MutableLiveData<Costs>()
    val updateControl = MutableLiveData<Boolean>()
    val costLiveDataStatusMessage = MutableLiveData<String>()

    private val db = Firebase.firestore

    /*fun getCosts(costUuid : String) {
        launch {
            val cost = PayerDatabase(getApplication()).payerDao().getCostInfo("costUuid")
            showCost(cost)
        }
    }*/

    fun getCost(costUuid: String){
        launch {
            db.collection("Costs").document(costUuid).get().addOnSuccessListener { result ->
                if (result != null){
                    showCost(castCostData(result))
                }
            }.addOnFailureListener {
                costLiveDataStatusMessage.postValue(it.localizedMessage)
            }
        }
    }

    private fun showCost(cost : Costs) {
        launch {
            costLiveData.postValue(cost)
        }
    }

    fun updateCost(cost : Costs, newCostName : String, newCostAmount : String,
                   newCostDateDay : Int, newCostDateMonth : Int, newCostDateYear : Int){
            if (newCostName.isNotEmpty()  && newCostAmount.isNotEmpty() &&
                newCostDateDay != -1 &&
                newCostDateMonth != -1 &&
                newCostDateYear != -1 ) {

                if (cost.cost_name != newCostName ||
                    cost.amount_of_expense != Integer.parseInt(cleanCastingAmountText(newCostAmount)) ||
                    cost.date_day != newCostDateDay ||
                    cost.date_month != newCostDateMonth ||
                    cost.date_year != newCostDateYear){
                    cost.cost_name = newCostName
                    cost.amount_of_expense = Integer.parseInt(cleanCastingAmountText(newCostAmount))
                    cost.date_day = newCostDateDay
                    cost.date_month = newCostDateMonth
                    cost.date_year = newCostDateYear
                    launch {
                        updateCostFromFireStore(cost)
                    }

                }
            }


    }

    private fun cleanCastingAmountText(castingAmount : String) : String{
        var cleanText = castingAmount.substring(1)
        cleanText = cleanText.replace(",","")
        cleanText = cleanText.replace(".","")
        return cleanText
    }

    private fun castCostData(responseCost : DocumentSnapshot) : Costs{

        val fireStoreCostDocumentNo = responseCost.reference.path.substring(6)
        val amountOfExpense = (responseCost.get("amount_of_expense") as Number).toInt()
        val dateDay = (responseCost.get("date_day") as Number).toInt()
        val dateMonth = (responseCost.get("date_month") as Number).toInt()
        val dateYear = (responseCost.get("date_year") as Number).toInt()
        val costName = responseCost.get("cost_name") as String
        val fireStoreDocumentNo = responseCost.get("firestore_document_no") as String
        val advanceFee = responseCost.get("advance_fee") as Boolean
        val protestCost = responseCost.get("protest_cost") as Boolean


        val cost = Costs(amountOfExpense,
            costName,
            dateDay,
            dateMonth,
            dateYear,
            fireStoreDocumentNo,
            advanceFee,
            protestCost,
            fireStoreCostDocumentNo)


        return cost
    }


    private fun updateCostFromFireStore(cost: Costs){
        val dataMap = createDataMap(cost)
        launch {
            db.collection("Costs").document(cost.firestore_cost_document_no).set(dataMap).addOnSuccessListener {
                updateControl.postValue(true)
                val success = "Başarıyla Güncellendi"
                costLiveDataStatusMessage.postValue(success)
            } . addOnFailureListener {
                costLiveDataStatusMessage.postValue(it.localizedMessage)
            }
        }
    }

    private fun createDataMap(cost: Costs) : HashMap<String,Any>{
        val dataMap = hashMapOf<String,Any>()
        dataMap["amount_of_expense"] = cost.amount_of_expense!!
        dataMap["cost_name"] = cost.cost_name!!
        dataMap["date_day"] = cost.date_day!!
        dataMap["date_month"] = cost.date_month!!
        dataMap["date_year"] = cost.date_year!!
        dataMap["firestore_document_no"] = cost.firestore_document_no
        dataMap["advance_fee"] = cost.advance_fee!!
        dataMap["protest_cost"] = cost.protest_cost!!

        return dataMap
    }

}