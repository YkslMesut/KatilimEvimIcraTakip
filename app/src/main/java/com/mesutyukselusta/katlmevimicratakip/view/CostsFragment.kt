package com.mesutyukselusta.katlmevimicratakip.view

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mesutyukselusta.katlmevimicratakip.R
import com.mesutyukselusta.katlmevimicratakip.adapter.CostAdapter
import com.mesutyukselusta.katlmevimicratakip.databinding.FragmentOpeningCostsBinding
import com.mesutyukselusta.katlmevimicratakip.util.SwipeGesture
import com.mesutyukselusta.katlmevimicratakip.viewmodel.CostsViewModel


class CostsFragment : Fragment() {
    private  val TAG = "CostsFragment"
    private var _binding: FragmentOpeningCostsBinding? = null
    private val binding get() = _binding!!
    private lateinit var fireStoreDocumentNo : String
    private val costAdapter = CostAdapter(arrayListOf())
    private lateinit var viewModel : CostsViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentOpeningCostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        arguments?.let {
            fireStoreDocumentNo = CostsFragmentArgs.fromBundle(it).fireStoreDocumentNo

        }
        viewModel = ViewModelProvider(this).get(CostsViewModel::class.java)
        observeLiveData()

        viewModel.getCostsFromFireStore(fireStoreDocumentNo)

        adapterView()


        binding.payerSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "onQueryTextChange: "+newText)
                costAdapter.filter.filter(newText)
                return false
            }

        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getCostsFromFireStore(fireStoreDocumentNo)
            binding.swipeRefreshLayout.isRefreshing = false
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_costs_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.add_costs){
            val action = CostsFragmentDirections.actionOpeningCostsFragmentToAddCostFragment(fireStoreDocumentNo)
            Navigation.findNavController(requireView()).navigate(action)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun observeLiveData() {
        viewModel.costLiveData.observe(viewLifecycleOwner) { costs ->
            if (costs.isNotEmpty()) {
                binding.recyclerView.visibility = View.VISIBLE
                binding.payersError.visibility = View.GONE
                costAdapter.updateCostAdapter(costs)
            }
        }
        viewModel.costStatusMessage.observe(viewLifecycleOwner) { errMessage ->
            Toast.makeText(context, errMessage, Toast.LENGTH_SHORT).show()
        }

        viewModel.costEmptyResultControl.observe(viewLifecycleOwner) { isEmpty ->
            if (isEmpty) {
                showEmptyError()
            }
        }
    }

    private fun adapterView(){

        _binding!!.recyclerView.layoutManager = LinearLayoutManager(context)
        _binding!!.recyclerView.adapter = costAdapter



        val swipeGesture = object : SwipeGesture(requireContext()){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when(direction){
                    ItemTouchHelper.LEFT ->{
                        val cost = costAdapter.getCostFromPosition(viewHolder.adapterPosition)
                        viewModel.deleteCostFromFireStore(cost)
                    }
                    ItemTouchHelper.RIGHT ->{
                        val cost = costAdapter.getCostFromPosition(viewHolder.adapterPosition)
                        viewModel.deleteCostFromFireStore(cost)
                    }
                }

            }
        }

        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(binding.recyclerView)

        costAdapter.setOnItemClickListener(object : CostAdapter.onItemClickListener{
            override fun onItemClick(costUuid: String) {
                val action = CostsFragmentDirections.actionOpeningCostsFragmentToCostsDetailFragment(costUuid)
                Navigation.findNavController(requireView()).navigate(action)
            }


        })


    }

    private fun showEmptyError() {
        val emptyCost = "Hiç Masraf Bulunmamaktadır"
        binding.recyclerView.visibility = View.GONE
        binding.payersError.visibility = View.VISIBLE
        binding.payersError.text = emptyCost
    }


}