package com.mesutyukselusta.katlmevimicratakip.view

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
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

    private lateinit var documentTypeSpinner: Spinner

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

        spinnerView()

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
        viewModel.createPayerInsertControl.observe(viewLifecycleOwner) { isInsertDatabase ->
            if (isInsertDatabase) {
                NavHostFragment.findNavController(this@CreatePayerFragment).navigateUp()
            }
        }
        viewModel.createPayerInputControl.observe(viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(context, "Lütfen Gerekli Alanları Doldurunuz", Toast.LENGTH_LONG)
                    .show()
            }
        }
        viewModel.createPayerInsertFirebaseErrorMessage.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
            }
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
    }

    private fun takeInfoFromView() {

        name = binding.etName.text.toString()
        surName = binding.etSurname.text.toString()
        if (binding.etDocumentType.isVisible){
            documentType = binding.etDocumentType.text.toString() }
        documentNo = binding.etDocumentNo.text.toString()
        documentYear = binding.etDocumentYear.text.toString()
        documentYear = binding.etDocumentYear.text.toString()
        createdMainDebt = binding.etCreatedMainDebt.text.toString()
        trackingAmount = binding.etTrackingAmount.text.toString()

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

    private fun spinnerView(){
        documentTypeSpinner = binding.documentTypeList

        val typeList: MutableList<String> = mutableListOf("Rehin","İlamsız","Senet","İpotek","Diğer")

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item,typeList)

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        documentTypeSpinner.adapter= adapter

        documentTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Toast.makeText(requireContext(),p0?.getItemAtPosition(p2).toString(), Toast.LENGTH_LONG).show()
                // Document Type Select And Bill Control
                when (p2) {
                    0 -> {
                        binding.etDocumentType.visibility = View.INVISIBLE
                        binding.etDocumentType.setText("")
                        documentType = p0?.getItemAtPosition(p2).toString()
                        documentTypeIsBill = false
                    }
                    1 -> {
                        binding.etDocumentType.visibility = View.INVISIBLE
                        binding.etDocumentType.setText("")
                        documentType = p0?.getItemAtPosition(p2).toString()
                        documentTypeIsBill = false
                    }
                    2 -> {
                        binding.etDocumentType.visibility = View.INVISIBLE
                        binding.etDocumentType.setText("")
                        documentType = p0?.getItemAtPosition(p2).toString()
                        documentTypeIsBill = true
                    }
                    3 -> {
                        binding.etDocumentType.visibility = View.INVISIBLE
                        binding.etDocumentType.setText("")
                        documentType = p0?.getItemAtPosition(p2).toString()
                        documentTypeIsBill = false
                    }
                    4 -> {
                        binding.etDocumentType.visibility = View.VISIBLE
                        binding.etDocumentType.setText("")
                        documentTypeIsBill = false
                        documentType = ""
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO()
            }
        }
    }

}