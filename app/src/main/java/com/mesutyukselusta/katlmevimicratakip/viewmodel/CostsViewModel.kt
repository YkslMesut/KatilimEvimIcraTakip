package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mesutyukselusta.katlmevimicratakip.db.PayerDatabase
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import kotlinx.coroutines.launch

class CostsViewModel(application: Application) : BaseViewModel(application) {
    private  val TAG = "CostViewModel"
    val costLiveData = MutableLiveData<List<Costs>>()

    fun getCosts(fireStoreDocumentNo : String) {
        launch {
            val costs = PayerDatabase(getApplication()).payerDao().getCostListInfo(fireStoreDocumentNo)
            showCosts(costs)
        }
    }

    private fun showCosts(costs : List<Costs>) {
        costLiveData.value = costs
    }

    fun deleteCostFromRoom(cost : Costs){
        launch {
            PayerDatabase(getApplication()).payerDao().deleteCost(cost.cost_uuid)
            getCosts(cost.uuid!!)
        }
    }
}