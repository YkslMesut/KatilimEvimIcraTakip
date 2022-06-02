package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mesutyukselusta.katlmevimicratakip.db.PayerDatabase
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfoWithCosts
import kotlinx.coroutines.launch

class CostDetailsViewModel(application: Application) : BaseViewModel(application) {
    private  val TAG = "CostDetailsViewModel"
    val costLiveData = MutableLiveData<Costs>()
    val updateControl = MutableLiveData<Boolean>()


    fun getCosts(costUuid : Int) {
        launch {
            val cost = PayerDatabase(getApplication()).payerDao().getCostInfo(costUuid)
            showCost(cost)
        }
    }

    private fun showCost(cost : Costs) {
        costLiveData.value = cost
    }

    fun updateCost(oldCost : Costs,newCostName : String,newCostAmount : String,
    newCostDateDay : Int,newCostDateMonth : Int , newCostDateYear : Int){
            if (newCostName.isNotEmpty()  && newCostAmount.isNotEmpty() &&
                newCostDateDay != -1 &&
                newCostDateMonth != -1 &&
                newCostDateYear != -1 ) {

                if (oldCost.cost_name != newCostName ||
                    oldCost.amount_of_expense != Integer.parseInt(cleanCastingAmountText(newCostAmount)) ||
                    oldCost.date_day != newCostDateDay ||
                    oldCost.date_month != newCostDateMonth ||
                    oldCost.date_year != newCostDateYear){
                    oldCost.cost_name = newCostName
                    oldCost.amount_of_expense = Integer.parseInt(cleanCastingAmountText(newCostAmount))
                    oldCost.date_day = newCostDateDay
                    oldCost.date_month = newCostDateMonth
                    oldCost.date_year = newCostDateYear
                    launch {
                        PayerDatabase(getApplication()).payerDao().updateCosts(oldCost)
                        updateControl.value = true
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

}