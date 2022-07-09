package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mesutyukselusta.katlmevimicratakip.db.PayerDatabase
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import kotlinx.coroutines.launch

class PayerFragmentViewModel(application: Application) : BaseViewModel(application) {
    private  val TAG = "PayerFragmentViewModel"

    val payerListLiveData = MutableLiveData<List<PayerInfo>>()
    val signOutLiveData = MutableLiveData<Boolean>()
    val payersError = MutableLiveData<Boolean>()
    val payersLoading = MutableLiveData<Boolean>()
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    val emptyPayerList = ArrayList<PayerInfo>()


    // Get Data Fun
    fun getAllPayersFromLocalDb() {
        launch {
            val payers = PayerDatabase(getApplication()).payerDao().getAllPayers()
            showPayers(payers)
        }
    }

    fun getAllPayersFromFireStore(context : Context){
        db.collection("PayerInfo").get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty){
                    val documents = result.documents
                    showPayers(castData(documents))
                } else {
                    Toast.makeText(context,"Hiç Borçlu Dosyası Bulunmamaktadır",Toast.LENGTH_LONG).show()
                    showPayers(emptyPayerList)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context,it.localizedMessage,Toast.LENGTH_LONG).show()
            }
    }

    private fun showPayers(payerList: List<PayerInfo>) {
        payerListLiveData.value = payerList
        payersError.value = false
        payersLoading.value = false
    }

    // Delete Data Fun

    fun deletePayerFromLocalDB(fireStoreDocumentNo : String){
        launch {
            PayerDatabase(getApplication()).payerDao().deletePayer(fireStoreDocumentNo)
            getAllPayersFromLocalDb()
        }
    }

    fun deletePayerFromFireStore(context: Context,fireStoreDocumentNo: String){
        db.collection("PayerInfo").document(fireStoreDocumentNo).delete().addOnSuccessListener {
            Toast.makeText(context,"Başarıyla Masraf Silindi",Toast.LENGTH_LONG).show()
            getAllPayersFromFireStore(context)

        } . addOnFailureListener {
            Toast.makeText(context,it.localizedMessage,Toast.LENGTH_LONG).show()
            getAllPayersFromFireStore(context)

        }
    }


    // Update Data Fun

    fun updateDocumentStatusFromLocalDB(payerInfo : PayerInfo,documentStatus : String) {
        launch {
            payerInfo.document_status = documentStatus
            PayerDatabase(getApplication()).payerDao().updatePayer(payerInfo)
            getAllPayersFromLocalDb()
        }
    }

    fun updateDocumentStatusFromFireStore(context: Context,fireStoreDocumentNo : String,documentStatus: String){
        db.collection("PayerInfo").document(fireStoreDocumentNo).update("document_status",documentStatus).addOnSuccessListener {
            getAllPayersFromFireStore(context)
        } .addOnFailureListener {
            Toast.makeText(context,it.localizedMessage,Toast.LENGTH_LONG).show()
            getAllPayersFromFireStore(context)
        }
    }

    // SignOut

    fun signOut(){
        auth.signOut()
        signOutLiveData.value = false
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