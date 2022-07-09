package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mesutyukselusta.katlmevimicratakip.db.PayerDatabase
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import kotlinx.coroutines.launch

class CostsViewModel(application: Application) : BaseViewModel(application) {
    private  val TAG = "CostViewModel"
    val costLiveData = MutableLiveData<List<Costs>>()
    val emptyCostList = ArrayList<Costs>()


    private val db = Firebase.firestore

    fun getCosts(fireStoreDocumentNo : String) {
        launch {
            val costs = PayerDatabase(getApplication()).payerDao().getCostListInfo(fireStoreDocumentNo)
            showCosts(costs)
        }
    }

    fun getCostsFromFireStore(context: Context, fireStoreDocumentNo: String){
        db.collection("Costs").whereEqualTo("firestore_document_no",fireStoreDocumentNo).get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty){
                    val documents = result.documents
                    showCosts(castCostData(documents))
                } else {
                    Toast.makeText(context,"Hiç Masraf Bulunmamaktadır", Toast.LENGTH_LONG).show()
                    showCosts(emptyCostList)

                }
            }
            .addOnFailureListener {
                Toast.makeText(context,it.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }


    private fun showCosts(costs : List<Costs>) {
        costLiveData.value = costs
    }

    /*fun deleteCostFromRoom(cost : Costs){
        launch {
            PayerDatabase(getApplication()).payerDao().deleteCost(cost.firestore_cost_document_no)
            getCosts(cost.firestore_document_no!!)
        }
    }*/

    fun deleteCostFromFireStore(context: Context,cost: Costs){
        db.collection("Costs").document(cost.firestore_cost_document_no).delete()
            .addOnSuccessListener {
                Toast.makeText(context,"Başarıyla Masraf Silindi",Toast.LENGTH_LONG).show()
                getCostsFromFireStore(context,cost.firestore_document_no)
        }.addOnFailureListener {
            Toast.makeText(context,it.localizedMessage,Toast.LENGTH_LONG).show()
                getCostsFromFireStore(context,cost.firestore_document_no)
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