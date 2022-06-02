package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mesutyukselusta.katlmevimicratakip.db.PayerDatabase
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import com.mesutyukselusta.katlmevimicratakip.model.MoneyToBeSent
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfoWithCosts
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CostCalculateViewModel(application: Application) : BaseViewModel(application) {
    private  val TAG = "CostCalculateViewModel"
    val moneyToBeSentData = MutableLiveData<MoneyToBeSent>()
    lateinit var selectedPayer : PayerInfoWithCosts


    fun getPayerInfo(uuid : Int) {
        launch {
            val payer = PayerDatabase(getApplication()).payerDao().getPayerInfoWithCosts(uuid)
            selectedPayer = payer
        }
    }

    fun calculateAllDebt(closingDay: Int, closingMonth : Int, closingYear : Int) {
        val mainDebt : Int
        val interest : Int
        val costs : Int
        val totalDebt : Int

        mainDebt = selectedPayer.payerInfo.main_debt!!
        interest = calculateInterest(selectedPayer.payerInfo.created_main_debt!!,selectedPayer.payerInfo.document_type_is_bill!!,selectedPayer.payerInfo.document_creation_date!!,closingDay,closingMonth,closingYear)
        costs = calculateCosts(selectedPayer.costs,closingMonth,closingYear)
        Log.d(TAG, "calculateAllDebt: " +"maindebt : $mainDebt " + "interest : $interest" + "costs : ${selectedPayer.payerInfo.costs!!}" +
                "tuitionfee : ${selectedPayer.payerInfo.tuition_fee}" + "proxy : ${selectedPayer.payerInfo.proxy}")
        totalDebt = mainDebt + interest + selectedPayer.payerInfo.costs!! + selectedPayer.payerInfo.tuition_fee!! + selectedPayer.payerInfo.proxy!!
        Log.d(TAG, "calculateAllDebt:  totaldebt : $totalDebt")
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


    private fun calculateCosts(costList : List<Costs>,closingMonth : Int,closingYear : Int) : Int {
        var totalCost = 0
        for (cost in costList) {
            if (cost.protest_cost == true){
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
        return totalCost
    }
}