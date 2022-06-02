package com.mesutyukselusta.katlmevimicratakip.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.mesutyukselusta.katlmevimicratakip.databinding.FragmentCreatePayerBinding
import com.mesutyukselusta.katlmevimicratakip.util.MoneyTextWatcher
import com.mesutyukselusta.katlmevimicratakip.viewmodel.CreatePayerFragmentViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*


class CreatePayerFragment : Fragment(), DatePickerDialog.OnDateSetListener{
    private val TAG = "CreatePayerFragment"
    private var _binding: FragmentCreatePayerBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel : CreatePayerFragmentViewModel

    private lateinit var createdMainDebtTextWatcher: MoneyTextWatcher
    private lateinit var trackingAmountTextWatcher: MoneyTextWatcher

    private lateinit var name : String
    private lateinit var surName : String
    private var documentNo =  "-1"
    private var documentYear = "-1"
    private var createdMainDebt = "-1"
    private var trackingAmount = "-1"
    var dateDay = -1
    var dateMonth = -1
    var dateYear = -1
    var documentTypeIsBill = false
    private lateinit var documentType : String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentCreatePayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         viewModel = ViewModelProvider(this).get(CreatePayerFragmentViewModel::class.java)

        createdMainDebtTextWatcher = MoneyTextWatcher(binding.etCreatedMainDebt)
        binding.etCreatedMainDebt.addTextChangedListener(createdMainDebtTextWatcher)

        trackingAmountTextWatcher = MoneyTextWatcher(binding.etTrackingAmount)
        binding.etTrackingAmount.addTextChangedListener(trackingAmountTextWatcher)

        binding.btnSave.setOnClickListener {
            takeInfoFromView()
            viewModel.validateControl(name,surName,documentType,documentNo,documentYear,dateDay,dateMonth,dateYear,documentTypeIsBill,createdMainDebt,trackingAmount)
            Log.d(TAG, "btnSave: ")
            observeLiveData()
        }
        binding.btnPickDate.setOnClickListener {
            showDatePicker()
        }


    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeLiveData(){
        viewModel.createPayerInsertControl.observe(viewLifecycleOwner, Observer {isInsertDatabase->
            if (isInsertDatabase){
                NavHostFragment.findNavController(this@CreatePayerFragment).navigateUp()
            }
        })
    }

    private fun takeInfoFromView() {

        name = binding.etName.text.toString()
        surName = binding.etSurname.text.toString()
        documentType = binding.etDocumentType.text.toString()
        documentNo = binding.etDocumentNo.text.toString()
        documentYear = binding.etDocumentYear.text.toString()
        documentYear = binding.etDocumentYear.text.toString()
        createdMainDebt = binding.etCreatedMainDebt.text.toString()
        trackingAmount = binding.etTrackingAmount.text.toString()
        documentTypeIsBill = binding.checkBoxDocumentTypeIsBill.isChecked

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