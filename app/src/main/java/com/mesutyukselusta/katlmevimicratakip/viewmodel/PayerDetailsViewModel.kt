package com.mesutyukselusta.katlmevimicratakip.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mesutyukselusta.katlmevimicratakip.db.PayerDatabase
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfoWithCosts
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PayerDetailsViewModel(application: Application) : BaseViewModel(application) {
    private val TAG = "PayerDetailsViewModel"

    val payerLiveData = MutableLiveData<PayerInfoWithCosts>()
    val updateControl = MutableLiveData<Boolean>()

    fun getPayer(uuid : Int) {
        launch {
            val payer = PayerDatabase(getApplication()).payerDao().getPayerInfoWithCosts(uuid)
            // Control Interest
            val calculatedInterest = calculateInterest(payer.payerInfo.created_main_debt!!,
                payer.payerInfo.document_type_is_bill!!,payer.payerInfo.document_creation_date!!)

            if (calculatedInterest != payer.payerInfo.interest){
                updateInterest(payer,calculatedInterest)
            } else {
                PayerDatabase(getApplication()).payerDao().updatePayer(payer.payerInfo)
                showPayers(payer)
            }
        }
    }

    private fun showPayers(payer : PayerInfoWithCosts) {
        payerLiveData.value = payer
    }

    fun updatePayer(payerWithCosts : PayerInfoWithCosts,mainDebt : String,
                    proxy : String,isForeClosure : Boolean ,costs : String){

        if (payerWithCosts != null && mainDebt.isNotEmpty()  && proxy.isNotEmpty()){

            val  mainDebtClean = cleanCastingAmountText(mainDebt)

            if (payerWithCosts.payerInfo.created_main_debt!! >= Integer.parseInt(mainDebtClean)){

                val costsClean = cleanCastingAmountText(costs)
                var mCost = costsClean
                if (mCost.isEmpty()){
                    mCost = "0"
                }

                val selectedPayerInfo = payerWithCosts.payerInfo
                val proxyClean = cleanCastingAmountText(proxy)
                val advanceFee = calculateAdvanceFee(payerWithCosts.costs)
                val tuitionFeeClean = payerWithCosts.payerInfo.calculateTuitionFee(payerWithCosts.payerInfo.tracking_amount,
                    isForeClosure,advanceFee)
                val newPayer = PayerInfo(selectedPayerInfo.name,selectedPayerInfo.surname,
                    selectedPayerInfo.document_no,selectedPayerInfo.document_year,
                    selectedPayerInfo.document_type,Integer.parseInt(mainDebtClean),
                    calculateInterest(selectedPayerInfo.created_main_debt!!,selectedPayerInfo.document_type_is_bill!!,
                        selectedPayerInfo.document_creation_date!!),
                    Integer.parseInt(proxyClean),
                    Integer.parseInt(mCost),
                    tuitionFeeClean,selectedPayerInfo.document_creation_date,selectedPayerInfo.document_type_is_bill,
                    selectedPayerInfo.created_main_debt,isForeClosure,selectedPayerInfo.tracking_amount,selectedPayerInfo.document_status)
                newPayer.uuid = selectedPayerInfo.uuid
                launch {
                    PayerDatabase(getApplication()).payerDao().updatePayer(newPayer)
                    updateControl.value = true
                }
            } else {
                updateControl.value = false
            }
        } else {
            updateControl.value = false
        }

    }

    private fun calculateInterest (createdMainDebt : Int,documentTypeIsBill : Boolean,documentCreatedDay : String) : Int{

        val now = currentDate()
        val sdf = SimpleDateFormat("dd/MM/yyyy")

        val createdDate: Date = sdf.parse(now)
        val currentDate: Date = sdf.parse(documentCreatedDay)

        val dayDiffWithTime = createdDate.time - currentDate.time
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

    private fun calculateAdvanceFee(costs : List<Costs>) : Int {
        var advanceFee = 0
        for (cost in costs) {
            if (cost.advance_fee == true) {
                advanceFee += cost.amount_of_expense!!
            }
        }
        return advanceFee
    }

    private fun currentDate(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    private fun cleanCastingAmountText(castingAmount : String) : String{
        var cleanText = castingAmount.substring(1)
        cleanText = cleanText.replace(",","")
        cleanText = cleanText.replace(".","")
        return cleanText
    }

    private fun updateInterest(payerWithCosts: PayerInfoWithCosts, updatedInterest : Int){

        launch {
            payerWithCosts.payerInfo.interest = updatedInterest
            PayerDatabase(getApplication()).payerDao().updatePayer(payerWithCosts.payerInfo)
            showPayers(payerWithCosts)
        }

    }

}