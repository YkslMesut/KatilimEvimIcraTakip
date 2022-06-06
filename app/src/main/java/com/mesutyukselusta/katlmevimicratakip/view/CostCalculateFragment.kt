package com.mesutyukselusta.katlmevimicratakip.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mesutyukselusta.katlmevimicratakip.databinding.FragmentCostCalculateBinding
import com.mesutyukselusta.katlmevimicratakip.model.MoneyToBeSent
import com.mesutyukselusta.katlmevimicratakip.viewmodel.CostCalculateViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

class CostCalculateFragment : Fragment(),DatePickerDialog.OnDateSetListener {
    private val TAG = "PayerDetailsFragment"
    private var _binding: FragmentCostCalculateBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel : CostCalculateViewModel
    var dateDay = -1
    var dateMonth = -1
    var dateYear = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCostCalculateBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(CostCalculateViewModel::class.java)

        arguments?.let {
            val selectedUuid = CostCalculateFragmentArgs.fromBundle(it).uuid
            viewModel.getPayerInfo(selectedUuid)
        }
        val interest = 0.57
        if (interest < 1) {
            Log.d(TAG, "onViewCreated: "+ 999+(57/100))
        } else {
            Log.d(TAG, "onViewCreated: "+"büyük")

        }
        binding.btnPickDate.setOnClickListener {
            showDatePicker()

        }

    }

    private fun observeLiveData() {
        viewModel.moneyToBeSentData.observe(viewLifecycleOwner) { moneyToBeSent ->
            if (moneyToBeSent != null) {
                updateView(moneyToBeSent)

            }
        }

    }

    private fun updateView(moneyToBeSent: MoneyToBeSent?) {
        binding.viewCalculated.visibility = View.VISIBLE
        binding.txtKatilimMainDebt.text = costAmountCastingFor(moneyToBeSent!!.main_debt.toString())
        binding.txtKatilimTotalDebt.text = costAmountCastingFor((moneyToBeSent.main_debt +moneyToBeSent.costs + moneyToBeSent.interest).toString())
        binding.txtKatilimCost.text = costAmountCastingFor(moneyToBeSent!!.costs.toString())
        binding.txtKatilimInterest.text = costAmountCastingFor(moneyToBeSent!!.interest.toString())
        binding.txtTotalDept.text = costAmountCastingFor(moneyToBeSent!!.total_debt.toString())
        binding.txtKatilimCostAndInterest.text = costAmountCastingFor((moneyToBeSent.costs + moneyToBeSent.interest).toString())

    }
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(requireContext(),
            this,
            Calendar.getInstance()[Calendar.YEAR],
            Calendar.getInstance()[Calendar.MONTH],
            Calendar.getInstance()[Calendar.DAY_OF_MONTH])
        datePickerDialog.show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        dateDay = dayOfMonth
        dateMonth = month+1
        dateYear = year
        binding.txtDate.text = "$dateDay/$dateMonth/$dateYear"
        viewModel.calculateAllDebt(dateDay,dateMonth,dateYear)
        observeLiveData()
    }
    private fun costAmountCastingFor(costAmount : String) : String{
        val cleanString = costAmount.replace("[$,.]".toRegex(), "")
        val parsed: BigDecimal = BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR)
            .divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)
        val formatted: String = NumberFormat.getCurrencyInstance().format(parsed)
        return formatted
    }

    private fun cleanCastingAmountText(castingAmount : String) : String{
        var cleanText = castingAmount.substring(1)
        cleanText = cleanText.replace(",","")
        cleanText = cleanText.replace(".","")
        return cleanText
    }

}