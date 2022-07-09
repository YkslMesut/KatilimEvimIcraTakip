package com.mesutyukselusta.katlmevimicratakip.view

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextWatcher
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

    private lateinit var documentTypeSpinner: Spinner

    private lateinit var costName : String
    private lateinit var costAmount : String
    private var isAdvanceFee : Boolean = false
    private var isProtestCost : Boolean = false

    private lateinit var fireStoreDocumentNo : String
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
            fireStoreDocumentNo = AddCostFragmentArgs.fromBundle(it).fireStoreDocumentNo
        }

        spinnerView()

        binding.btnAddCosts.setOnClickListener {

            takeInfoFromEditTexts()

            viewModel.validateControl(requireContext(),costName,costAmount,fireStoreDocumentNo,dateDay,dateMonth,dateYear,isAdvanceFee,isProtestCost)
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
                Toast.makeText(context,"Başarıyla Masraf Eklendi",Toast.LENGTH_LONG).show()
                NavHostFragment.findNavController(this@AddCostFragment).navigateUp()
            }
        })
        viewModel.addCostInputControl.observe(viewLifecycleOwner, Observer {
            if (!it){
                Toast.makeText(requireContext(),"Lütfen Boş Alanları Doldurunuz",Toast.LENGTH_LONG).show()
            }
        })
        viewModel.addCostStatusMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(),it,Toast.LENGTH_LONG).show()
        })
    }

    private fun takeInfoFromEditTexts() {
        if (binding.etCostType.isVisible){
            costName = binding.etCostType.text.toString() }
        costAmount = binding.etAmountOfExpense.text.toString()
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
        documentTypeSpinner = binding.costTypeList

        val typeList: MutableList<String> = mutableListOf("Peşin Harcı","İhtarname Masrafı","Yakalama avansı","Masraf açılış","Diğer")

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item,typeList)

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        documentTypeSpinner.adapter= adapter

        documentTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Toast.makeText(requireContext(),p0?.getItemAtPosition(p2).toString(), Toast.LENGTH_LONG).show()
                // Document Type Select And Bill Control
                when (p2) {
                    0 -> {
                        binding.etCostType.visibility = View.INVISIBLE
                        binding.etCostType.setText("")
                        costName = p0?.getItemAtPosition(p2).toString()
                        isAdvanceFee = true
                        isProtestCost = false
                    }
                    1 -> {
                        binding.etCostType.visibility = View.INVISIBLE
                        binding.etCostType.setText("")
                        costName = p0?.getItemAtPosition(p2).toString()
                        isAdvanceFee = false
                        isProtestCost = true
                    }
                    2 -> {
                        binding.etCostType.visibility = View.INVISIBLE
                        binding.etCostType.setText("")
                        costName = p0?.getItemAtPosition(p2).toString()
                        isAdvanceFee = false
                        isProtestCost = false
                    }
                    3 -> {
                        binding.etCostType.visibility = View.INVISIBLE
                        binding.etCostType.setText("")
                        costName = p0?.getItemAtPosition(p2).toString()
                        isAdvanceFee = false
                        isProtestCost = false
                    }
                    4 -> {
                        binding.etCostType.visibility = View.VISIBLE
                        binding.etCostType.setText("")
                        isAdvanceFee = false
                        isProtestCost = false
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO()
            }
        }
    }


}