package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mesutyukselusta.katlmevimicratakip.db.PayerDatabase
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import kotlinx.coroutines.launch

class PayerFragmentViewModel(application: Application) : BaseViewModel(application) {

    val payerListLiveData = MutableLiveData<List<PayerInfo>>()
    val payersError = MutableLiveData<Boolean>()
    val payersLoading = MutableLiveData<Boolean>()

    fun getAllPayersFromRoom() {
        launch {
            val payers = PayerDatabase(getApplication()).payerDao().getAllPayers()
            showPayers(payers)
        }
    }

    private fun showPayers(payerList: List<PayerInfo>) {
        payerListLiveData.value = payerList
        payersError.value = false
        payersLoading.value = false
    }

    fun deletePayerFromRoom(uuid : Int){
        launch {
            PayerDatabase(getApplication()).payerDao().deletePayer(uuid)
            getAllPayersFromRoom()
        }
    }

    fun updateDocumentStatus(payerInfo : PayerInfo,documentStatus : String) {
        launch {
            payerInfo.document_status = documentStatus
            PayerDatabase(getApplication()).payerDao().updatePayer(payerInfo)
            getAllPayersFromRoom()
        }
    }
}