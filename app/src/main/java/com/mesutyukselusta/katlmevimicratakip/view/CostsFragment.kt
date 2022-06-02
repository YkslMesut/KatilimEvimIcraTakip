package com.mesutyukselusta.katlmevimicratakip.view

import android.os.Bundle
import android.util.Log
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
import com.mesutyukselusta.katlmevimicratakip.adapter.CostAdapter
import com.mesutyukselusta.katlmevimicratakip.adapter.PayerAdapter
import com.mesutyukselusta.katlmevimicratakip.databinding.FragmentOpeningCostsBinding
import com.mesutyukselusta.katlmevimicratakip.util.SwipeGesture
import com.mesutyukselusta.katlmevimicratakip.viewmodel.CostsViewModel
import java.math.BigDecimal
import java.text.NumberFormat


class CostsFragment : Fragment() {
    private  val TAG = "CostsFragment"
    private var _binding: FragmentOpeningCostsBinding? = null
    private val binding get() = _binding!!
    private var selectedUuid = 0
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
            selectedUuid = CostsFragmentArgs.fromBundle(it).uuid
            Log.d(TAG, "arguments: "+selectedUuid)

        }
        viewModel = ViewModelProvider(this).get(CostsViewModel::class.java)
        viewModel.getCosts(selectedUuid)

        adapterView()

        observeLiveData()

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
            val action = CostsFragmentDirections.actionOpeningCostsFragmentToAddCostFragment(selectedUuid)
            Navigation.findNavController(requireView()).navigate(action)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun observeLiveData() {
        viewModel.costLiveData.observe(viewLifecycleOwner, Observer {costs->
            costAdapter.updateCostAdapter(costs)
        })
    }

    private fun adapterView(){

        _binding!!.recyclerView.layoutManager = LinearLayoutManager(context)
        _binding!!.recyclerView.adapter = costAdapter



        val swipeGesture = object : SwipeGesture(requireContext()){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when(direction){
                    ItemTouchHelper.LEFT ->{
                        val cost = costAdapter.getCostFromPosition(viewHolder.adapterPosition)
                        viewModel.deleteCostFromRoom(cost)
                    }
                    ItemTouchHelper.RIGHT ->{
                        val cost = costAdapter.getCostFromPosition(viewHolder.adapterPosition)
                        viewModel.deleteCostFromRoom(cost)
                    }
                }

            }
        }

        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(binding.recyclerView)

        costAdapter.setOnItemClickListener(object : CostAdapter.onItemClickListener{
            override fun onItemClick(costUuid: Int) {
                val action = CostsFragmentDirections.actionOpeningCostsFragmentToCostsDetailFragment(costUuid)
                Navigation.findNavController(requireView()).navigate(action)
            }


        })


    }




}