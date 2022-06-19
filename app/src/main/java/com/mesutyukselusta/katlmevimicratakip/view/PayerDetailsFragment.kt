package com.mesutyukselusta.katlmevimicratakip.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.mesutyukselusta.katlmevimicratakip.databinding.FragmentPayerDetailsBinding
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfoWithCosts
import com.mesutyukselusta.katlmevimicratakip.util.MoneyTextWatcher
import com.mesutyukselusta.katlmevimicratakip.viewmodel.PayerDetailsViewModel
import java.math.BigDecimal
import java.text.NumberFormat

class PayerDetailsFragment : Fragment() {
    private val TAG = "PayerDetailsFragment"
    private var _binding: FragmentPayerDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel : PayerDetailsViewModel

    private lateinit var selectedPayer : PayerInfo

    private lateinit var mainDebtTextWatcher: MoneyTextWatcher
    private lateinit var proxyTextWatcher: MoneyTextWatcher

    private lateinit var fireStoreDocumentNo : String



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentPayerDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(PayerDetailsViewModel::class.java)

        arguments?.let {
            fireStoreDocumentNo = PayerDetailsFragmentArgs.fromBundle(it).fireStoreDocumentNo
            viewModel.getPayerFromFireStore(requireContext(),fireStoreDocumentNo)

        }

        observeLiveData()


        mainDebtTextWatcher = MoneyTextWatcher(binding.etMainDebt)
        binding.etMainDebt.addTextChangedListener(mainDebtTextWatcher)

        proxyTextWatcher = MoneyTextWatcher(binding.etProxy)
        binding.etProxy.addTextChangedListener(proxyTextWatcher)



        binding.btnUpdate.setOnClickListener {
            if (selectedPayer != null) {
                val  mainDebt = (binding.etMainDebt.text.toString())
                val proxy = (binding.etProxy.text.toString())
                val costs = (binding.txtCosts.text.toString())
                val isForeClosure = (binding.checkBoxIsForeclosure.isChecked)
                viewModel.updatePayer(requireContext(),selectedPayer,mainDebt,proxy,isForeClosure,costs)
                observeLiveData()
            }
        }

        binding.viewCosts.setOnClickListener {
            Log.d(TAG, "onViewCreated: "+fireStoreDocumentNo)
            val action = PayerDetailsFragmentDirections.actionPayerInformationFragmentToOpeningCostsFragment(fireStoreDocumentNo)
            Navigation.findNavController(it).navigate(action)
        }

        binding.showCalculates.setOnClickListener {
            if (selectedPayer.main_debt != null && selectedPayer.proxy != null) {
                val action = PayerDetailsFragmentDirections.actionPayerInformationFragmentToCostCalculateFragment(fireStoreDocumentNo)
                Navigation.findNavController(it).navigate(action)
            }

        }
    }

    private fun observeLiveData() {
        viewModel.payerLiveData.observe(viewLifecycleOwner, Observer {payer->
            selectedPayer = payer
            updateView(payer)
        })
        viewModel.updateControl.observe(viewLifecycleOwner, Observer {
            if (it){
                NavHostFragment.findNavController(this@PayerDetailsFragment).navigateUp()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateView(payer : PayerInfo){

        val name = payer.name
        val surName = payer.surname
        val  mainDebt = payer.main_debt.toString()
        val interest = payer.interest.toString()
        val proxy = payer.proxy.toString()
        val tuitionFee = payer.tuition_fee.toString()
        val documentNo = payer.document_year.toString() + "/" +
                payer.document_no.toString()
        val documentCreatedDate = payer.document_creation_date

        var costs =payer.costs

        binding.payerNameSurname.text = "$name/$surName"
        binding.txtDocumentCreatedDay.text = documentCreatedDate
        binding.txtDocumentNo.text = "$documentNo"
        binding.checkBoxIsForeclosure.isChecked = payer.is_foreclosure!!

        if (mainDebt.isNotEmpty()){
            with(binding) { etMainDebt.setText(costAmountCastingFor(mainDebt)) }
        }
        if (interest.isNotEmpty()){
            binding.txtInterest.text = costAmountCastingFor(interest)
        }
        if (proxy.isNotEmpty()){
            binding.etProxy.setText(costAmountCastingFor(proxy))
        }
        if (costs.toString().isNotEmpty()){
            binding.txtCosts.text = costAmountCastingFor(costs.toString())
        }
        if (tuitionFee.isNotEmpty()){
            binding.txtTuitionFee.text = costAmountCastingFor(tuitionFee)
        }
    }

    private fun costAmountCastingFor(costAmount : String) : String{
        val cleanString = costAmount.replace("[$,.]".toRegex(), "")
        val parsed: BigDecimal = BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR)
            .divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)
        val formatted: String = NumberFormat.getCurrencyInstance().format(parsed)
        return formatted
    }


}