package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import kotlinx.coroutines.launch

class PayerFragmentViewModel(application: Application) : BaseViewModel(application) {
    private  val TAG = "PayerFragmentViewModel"

    val payerListLiveData = MutableLiveData<List<PayerInfo>>()
    val signOutLiveData = MutableLiveData<Boolean>()
    val payersStatusMessage = MutableLiveData<String>()
    val payerEmptyResultControl = MutableLiveData<Boolean>()
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val emptyPayerList = ArrayList<PayerInfo>()


    // Get Data Fun With Db
    /*private fun getAllPayersFromLocalDb() {
        launch {
            val payers = PayerDatabase(getApplication()).payerDao().getAllPayers()
            showPayers(payers)
        }
    }*/

    fun getAllPayersFromFireStore(){
        launch {
            db.collection("PayerInfo").get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty){
                        payerEmptyResultControl.postValue(false)
                        val documents = result.documents
                        showPayers(castData(documents))
                    } else {
                        payerEmptyResultControl.postValue(true)
                        showPayers(emptyPayerList)
                    }
                }
                .addOnFailureListener {
                    payersStatusMessage.postValue(it.localizedMessage)
                }
        }

    }

    private fun showPayers(payerList: List<PayerInfo>) {
        launch {
            payerListLiveData.postValue(payerList)
        }
    }

    // Delete Data Fun
    /*fun deletePayerFromLocalDB(fireStoreDocumentNo : String){
        launch {
            PayerDatabase(getApplication()).payerDao().deletePayer(fireStoreDocumentNo)
            getAllPayersFromLocalDb()
        }
    }*/

    fun deletePayerFromFireStore(fireStoreDocumentNo: String){
        launch {
            db.collection("PayerInfo").document(fireStoreDocumentNo).delete().addOnSuccessListener {
                getPayerCosts(fireStoreDocumentNo)
            } . addOnFailureListener {
                payersStatusMessage.postValue(it.localizedMessage)
                getAllPayersFromFireStore()

            }
        }

    }
    private fun getPayerCosts(fireStoreDocumentNo: String){

            launch {
                db.collection("Costs").whereEqualTo("firestore_document_no",fireStoreDocumentNo).get()
                    .addOnSuccessListener { result ->
                        if (!result.isEmpty){
                            val costList = castCostData(result.documents)
                            deletePayersCosts(costList)
                        } else {
                            payersStatusMessage.postValue("Borçlu Başarıyla Silindi")
                            getAllPayersFromFireStore()
                        }
                    }
                    .addOnFailureListener {
                        payersStatusMessage.postValue(it.localizedMessage)
                        getAllPayersFromFireStore()
                    }
            }
    }

    private fun deletePayersCosts(costList : ArrayList<Costs>){
       launch {
           val deleteBatch = db.batch()
           for (cost in costList) {
               deleteBatch.delete(db.collection("Costs").document(cost.firestore_cost_document_no))
           }
           deleteBatch.commit()
               .addOnSuccessListener {
                   payersStatusMessage.postValue("Borçlu ve Masrafları Başarıyla Silindi")
                   getAllPayersFromFireStore()
               }.addOnFailureListener {
                   payersStatusMessage.postValue("Borçlu Başarıyla Silindi Fakat Dosyanın Masrafları Sunucudan Silinemedi")
                   getAllPayersFromFireStore()
               }
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


    // Update Data Fun
    /*fun updateDocumentStatusFromLocalDB(payerInfo : PayerInfo,documentStatus : String) {
        launch {
            payerInfo.document_status = documentStatus
            PayerDatabase(getApplication()).payerDao().updatePayer(payerInfo)
            getAllPayersFromLocalDb()
        }
    }*/

    fun updateDocumentStatusFromFireStore(fireStoreDocumentNo : String,documentStatus: String){
        db.collection("PayerInfo").document(fireStoreDocumentNo).update("document_status",documentStatus).addOnSuccessListener {
            payersStatusMessage.postValue("Dosya Durumu Değiştirildi")
            getAllPayersFromFireStore()
        } .addOnFailureListener {
            payersStatusMessage.postValue(it.localizedMessage)
            getAllPayersFromFireStore()
        }
    }

    // SignOut

    fun signOut(){
        launch {
            auth.signOut()
            signOutLiveData.postValue(true)
        }

    }

    private fun castData(documents : List<DocumentSnapshot>) : ArrayList<PayerInfo>{
        val payerList = ArrayList<PayerInfo>()

        for (document in documents){

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

            payerList.add(payer)
        }
        return payerList
    }
}