package com.mesutyukselusta.katlmevimicratakip.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.mesutyukselusta.katlmevimicratakip.databinding.FragmentAddCostBinding
import com.mesutyukselusta.katlmevimicratakip.util.MoneyTextWatcher
import com.mesutyukselusta.katlmevimicratakip.viewmodel.AddCostViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

class AddCostFragment : Fragment(),DatePickerDialog.OnDateSetListener {
    private  val TAG = "AddCostFragment"
    private var _binding: FragmentAddCostBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel : AddCostViewModel

    private lateinit var textWatcher: MoneyTextWatcher

    private lateinit var costName : String
    private lateinit var costAmount : String
    private var isAdvanceFee : Boolean = false
    private var isProtestCost : Boolean = false

    var selectedUuid = -1
    var dateDay = -1
    var dateMonth = -1
    var dateYear = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(AddCostViewModel::class.java)

        arguments?.let {
            selectedUuid = AddCostFragmentArgs.fromBundle(it).uuid
        }

        binding.btnAddCosts.setOnClickListener {

            takeInfoFromEditTexts()

            viewModel.validateControl(costName,costAmount,selectedUuid,dateDay,dateMonth,dateYear,isAdvanceFee,isProtestCost)
            observeLiveData()
        }
        binding.btnPickDate.setOnClickListener {
            showDatePicker()
        }

        textWatcher = MoneyTextWatcher(binding.etAmountOfExpense)
        binding.etAmountOfExpense.addTextChangedListener(textWatcher)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentAddCostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeLiveData(){

        viewModel.addCostInsertControl.observe(viewLifecycleOwner, Observer {isInsertDatabase->
            if (isInsertDatabase){
                NavHostFragment.findNavController(this@AddCostFragment).navigateUp()
            }
        })
    }

    private fun takeInfoFromEditTexts() {
        costName = binding.etCostName.text.toString()
        costAmount = binding.etAmountOfExpense.text.toString()
        isAdvanceFee = binding.checkBoxIsAdvanceFee.isChecked
        isProtestCost = binding.checkBoxIsProtestCost.isChecked
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

    }



}