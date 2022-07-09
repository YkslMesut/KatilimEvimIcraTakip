package com.mesutyukselusta.katlmevimicratakip.view

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.mesutyukselusta.katlmevimicratakip.databinding.FragmentCostsDetailBinding
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import com.mesutyukselusta.katlmevimicratakip.util.MoneyTextWatcher
import com.mesutyukselusta.katlmevimicratakip.viewmodel.CostDetailsViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

class CostsDetailsFragment : Fragment(), DatePickerDialog.OnDateSetListener{
    private val TAG = "CostsDetailFragment"
    private var _binding : FragmentCostsDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectedCostUuid : String

    private lateinit var selectedCost : Costs
    private lateinit var viewModel : CostDetailsViewModel

    var dateDay = -1
    var dateMonth = -1
    var dateYear = -1
    private lateinit var costAmountTextWatcher: MoneyTextWatcher


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCostsDetailBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(CostDetailsViewModel::class.java)

        arguments?.let {
            selectedCostUuid = CostsDetailsFragmentArgs.fromBundle(it).firestoreCostDocumentNo
            viewModel.getCost(selectedCostUuid)

        }

        observeLiveData()


        costAmountTextWatcher = MoneyTextWatcher(binding.etAmountOfExpense)
        binding.etAmountOfExpense.addTextChangedListener(costAmountTextWatcher)

        binding.btnUpdateCost.setOnClickListener {

            if (selectedCost != null) {
                val costAmount = binding.etAmountOfExpense.text.toString()
                val costName = binding.etCostName.text.toString()

                viewModel.updateCost(selectedCost,costName,costAmount,dateDay,
                    dateMonth,dateYear)
                observeLiveData()
            }
        }

        binding.btnPickDate.setOnClickListener {
            showDatePicker()
        }

    }

    private fun observeLiveData() {

        viewModel.costLiveData.observe(viewLifecycleOwner) {
            selectedCost = it
            dateDay = selectedCost.date_day!!
            dateMonth = selectedCost.date_month!!
            dateYear = selectedCost.date_year!!
            updateView(it)
        }
        viewModel.updateControl.observe(viewLifecycleOwner) {
            if (it) {
                NavHostFragment.findNavController(this@CostsDetailsFragment).navigateUp()
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateView(costs : Costs){

        val costName = costs.cost_name
        val costAmount = costs.amount_of_expense
        val  costDateDay = costs.date_day.toString()
        val costDateMonth = costs.date_month.toString()
        val costDateYear = costs.date_year.toString()

        binding.txtDate.setText("$costDateDay/$costDateMonth/$costDateYear")
        binding.etCostName.setText(costName)
        binding.etAmountOfExpense.setText(costAmountCastingFor(costAmount.toString()))

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
        Log.d(TAG, "onDateSet: $year+$month+$dayOfMonth")
        dateDay = dayOfMonth
        dateMonth = month+1
        dateYear = year
        binding.txtDate.text = "$dateDay/$dateMonth/$dateYear"
    }


    private fun costAmountCastingFor(costAmount : String) : String{
        val cleanString = costAmount.replace("[$,.]".toRegex(), "")
        val parsed: BigDecimal = BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR)
            .divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)
        val formatted: String = NumberFormat.getCurrencyInstance().format(parsed)
        return formatted
    }
}