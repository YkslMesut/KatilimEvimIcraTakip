package com.mesutyukselusta.katlmevimicratakip.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mesutyukselusta.katlmevimicratakip.R
import com.mesutyukselusta.katlmevimicratakip.adapter.PayerAdapter
import com.mesutyukselusta.katlmevimicratakip.databinding.FragmentPayerBinding
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import com.mesutyukselusta.katlmevimicratakip.util.SwipeGesture
import com.mesutyukselusta.katlmevimicratakip.viewmodel.PayerFragmentViewModel
import kotlinx.android.synthetic.main.fragment_payer.*

class PayerFragment : Fragment() {
    private lateinit var viewModel : PayerFragmentViewModel
    private val payerAdapter = PayerAdapter(arrayListOf())

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
        viewModel.getAllPayersFromRoom()

        adapterView()

        observeLiveData()

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
                        viewModel.deletePayerFromRoom(item.uuid)
                    }
                }
            }
        }

        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(binding.recyclerView)

        payerAdapter.setOnItemClickListener(object : PayerAdapter.onItemClickListener{
            override fun onItemClick(uuid: Int) {
                val action = PayerFragmentDirections.actionPayerFragmentToPayerInformationFragment(uuid)
                Navigation.findNavController(requireView()).navigate(action)
            }
        })

    }

    private fun observeLiveData() {

        viewModel.payerListLiveData.observe(viewLifecycleOwner) { payers ->

            payers?.let {
                recyclerView.visibility = View.VISIBLE
                payerAdapter.updatePayerAdapter(it)
            }
        }

        viewModel.payersError.observe(viewLifecycleOwner, Observer { error->
            error?.let {
                if(it) {
                    payersError.visibility = View.VISIBLE
                } else {
                    payersError.visibility = View.GONE
                }
            }
        })

        viewModel.payersLoading.observe(viewLifecycleOwner, Observer { loading->
            loading?.let {
                if (it) {
                    payersLoading.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    payersError.visibility = View.GONE
                } else {
                    payersLoading.visibility = View.GONE
                }
            }
        })
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
        }

        return super.onOptionsItemSelected(item)
    }

    private fun documentStateDialog(selectedPayer : PayerInfo) : AlertDialog {
        val alert = AlertDialog.Builder(requireContext())
            .setMessage("Dosya Durumunu Seçiniz")
            .setPositiveButton("Avans İadesi Bekleniyor") { dialog, which ->
                viewModel.updateDocumentStatus(selectedPayer, "avans_iade")
                dialog.dismiss()
            }
            .setNegativeButton("Dosya Kapandı") { dialog, which ->
                viewModel.updateDocumentStatus(selectedPayer, "dosya_kapandı")

                dialog.dismiss()
            }
            .setOnCancelListener {
                viewModel.getAllPayersFromRoom()
            }
            .create()

        return alert
    }


}