package com.mesutyukselusta.katlmevimicratakip.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mesutyukselusta.katlmevimicratakip.R
import com.mesutyukselusta.katlmevimicratakip.adapter.PayerAdapter
import com.mesutyukselusta.katlmevimicratakip.databinding.FragmentPayerBinding
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import com.mesutyukselusta.katlmevimicratakip.util.SwipeGesture
import com.mesutyukselusta.katlmevimicratakip.viewmodel.PayerFragmentViewModel

class PayerFragment : Fragment() {
    private lateinit var viewModel : PayerFragmentViewModel
    private val payerAdapter = PayerAdapter(arrayListOf())

    private  val TAG = "PayerFragment"

    private var _binding: FragmentPayerBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
            super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentPayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PayerFragmentViewModel::class.java)
        observeLiveData()

        viewModel.getAllPayersFromFireStore()

        adapterView()


        binding.payerSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                payerAdapter.filter.filter(newText)
                return false
            }

        })

        binding.refreshLayout.setOnRefreshListener {
            viewModel.getAllPayersFromFireStore()
            binding.refreshLayout.isRefreshing = false
        }
    }

    private fun adapterView(){

        _binding!!.recyclerView.layoutManager = LinearLayoutManager(context)
        _binding!!.recyclerView.adapter = payerAdapter

        val swipeGesture = object : SwipeGesture(requireContext()){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when(direction){
                    ItemTouchHelper.LEFT ->{
                        val item = payerAdapter.getPayerFromPosition(viewHolder.adapterPosition)
                        val documentStateAlertDialog = documentStateDialog(item)
                        documentStateAlertDialog.show()
                    }
                    ItemTouchHelper.RIGHT ->{
                        val item = payerAdapter.getPayerFromPosition(viewHolder.adapterPosition)
                        viewModel.deletePayerFromFireStore(item.firestore_document_no)
                    }
                }
            }
        }

        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(binding.recyclerView)

        payerAdapter.setOnItemClickListener(object : PayerAdapter.onItemClickListener{
            override fun onItemClick(fireStoreDocumentNo: String) {
                val action = PayerFragmentDirections.actionPayerFragmentToPayerInformationFragment(fireStoreDocumentNo)
                Navigation.findNavController(requireView()).navigate(action)
            }
        })

    }

    private fun observeLiveData() {

        viewModel.signOutLiveData.observe(viewLifecycleOwner){ signOut->
            signOut?.let {
                if (signOut){
                    val intent = Intent(requireContext(),LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        }

        viewModel.payerListLiveData.observe(viewLifecycleOwner) { payers ->

            payers?.let {
                if (payers.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.payersError.visibility = View.GONE
                    payerAdapter.updatePayerAdapter(it)
                    Log.d(TAG, "observeLiveData: " + payers.size)
                }

            }
        }

        viewModel.payersStatusMessage.observe(viewLifecycleOwner) { statusResponseMessage ->
            Toast.makeText(context,statusResponseMessage,Toast.LENGTH_SHORT).show()
        }

        viewModel.payerEmptyResultControl.observe(viewLifecycleOwner) { isEmptyResult ->
            if (isEmptyResult) {
                showError()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.payer_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.add_payer){
            val action = PayerFragmentDirections.actionPayerFragmentToCreatePayerFragment()
            Navigation.findNavController(requireView()).navigate(action)
        } else if (id == R.id.logout){
            viewModel.signOut()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun documentStateDialog(selectedPayer : PayerInfo) : AlertDialog {
        val alert = AlertDialog.Builder(requireContext())
            .setMessage("Dosya Durumunu Seçiniz")
            .setPositiveButton("Avans İadesi Bekleniyor") { dialog, which ->
                viewModel.updateDocumentStatusFromFireStore(selectedPayer.firestore_document_no, "avans_iade")
                dialog.dismiss()
            }
            .setNegativeButton("Dosya Kapandı") { dialog, which ->
                viewModel.updateDocumentStatusFromFireStore(selectedPayer.firestore_document_no, "dosya_kapandı")

                dialog.dismiss()
            }
            .setOnCancelListener {
                viewModel.getAllPayersFromFireStore()
            }
            .create()

        return alert
    }

    private fun showError() {
        val emptyPayer = "Hiç Borçlu Bulunmamaktadır"
        binding.recyclerView.visibility = View.GONE
        binding.payersError.visibility = View.VISIBLE
        binding.payersError.text = emptyPayer
    }


}