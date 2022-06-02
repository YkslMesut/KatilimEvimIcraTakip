package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mesutyukselusta.katlmevimicratakip.db.PayerDatabase
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import kotlinx.coroutines.launch

class AddCostViewModel(application: Application) : BaseViewModel(application) {
    private  val TAG = "AddCostViewModel"
    val addCostInputControl = MutableLiveData<Boolean>()
    val addCostInsertControl = MutableLiveData<Boolean>()

    fun validateControl(costName : String,costAmount : String,uuid: Int,
                        dateDay : Int,dateMonth : Int,dateYear : Int,isAdvanceFee : Boolean,isProtestCost : Boolean)  {
        addCostInputControl.value = costName.isNotEmpty() && costAmount.isNotEmpty() &&
                uuid != -1 &&
                dateDay != -1 &&
                dateMonth != -1 &&
                dateYear != -1
        if (addCostInputControl.value == true) {
            var cleanCost  = costAmount
            cleanCost = cleanCost.substring(1)
            cleanCost = cleanCost.replace(",","")
            cleanCost = cleanCost.replace(".","")
            insertCost(costName,cleanCost,uuid,dateDay,dateMonth,dateYear,isAdvanceFee,isProtestCost)
        }

    }

    private fun insertCost(costName : String, costAmount: String, uuid : Int, dateDay : Int, dateMonth : Int, dateYear : Int,isAdvanceFee : Boolean,isProtestCost: Boolean){
        val mCostAmount = Integer.parseInt(costAmount)
        val costs = Costs(mCostAmount,costName,dateDay,dateMonth,dateYear,uuid,isAdvanceFee,isProtestCost)
        val dao = PayerDatabase(getApplication()).payerDao()
        launch {
            dao.insertCosts(costs)
            addCostInsertControl.value = true
        }
    }


}