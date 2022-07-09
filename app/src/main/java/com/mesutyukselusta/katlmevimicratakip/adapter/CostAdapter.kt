package com.mesutyukselusta.katlmevimicratakip.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.mesutyukselusta.katlmevimicratakip.databinding.RowCostListBinding
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class CostAdapter(private val costsList : ArrayList<Costs>) : RecyclerView.Adapter<CostAdapter.CostViewHolder>(),Filterable {
    private  val TAG = "CostAdapter"

    private lateinit var mListener : onItemClickListener

    var costFilterList = ArrayList<Costs>()

    init {
        costFilterList = costsList
    }
    interface onItemClickListener {
        fun onItemClick(costUuid : String)
    }

    fun setOnItemClickListener(listener : CostAdapter.onItemClickListener){
        mListener = listener
    }

    class CostViewHolder(private val itemBinding: RowCostListBinding,listener : onItemClickListener) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(costs: Costs) {

            //Cast Cost Amount
            val cleanString = costs.amount_of_expense.toString().replace("[$,.]".toRegex(), "")
            val parsed: BigDecimal = BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR)
                .divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)
            val formattedCostAmount: String = NumberFormat.getCurrencyInstance().format(parsed)
            itemBinding.txtAmountOfExpense.text = formattedCostAmount

            itemBinding.txtCostName.text = costs.cost_name
            itemBinding.txtDate.text = costs.date_day.toString()+"/"+costs.date_month.toString()+"/"+
                    costs.date_year.toString()
            itemBinding.containerCostUuid.text = costs.firestore_cost_document_no
        }
        init{
            itemView.setOnClickListener {
                listener.onItemClick(itemBinding.containerCostUuid.text.toString())
            }
        }
    }



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CostAdapter.CostViewHolder {
        val itemBinding = RowCostListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CostViewHolder(itemBinding,mListener)
    }

    override fun onBindViewHolder(holder: CostAdapter.CostViewHolder, position: Int) {
        val costs: Costs = costFilterList[position]
        holder.bind(costs)
    }

    override fun getItemCount(): Int {
        return costFilterList.size
    }

    fun getCostFromPosition(position : Int) : Costs {
        return costFilterList[position]
    }

    fun updateCostAdapter(newCostList : List<Costs>){
        costsList.clear()
        costsList.addAll(newCostList)
        costFilterList = costsList
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    costFilterList = costsList
                } else {
                    val resultList = ArrayList<Costs>()
                    for (cost in costsList) {
                        if (cost.cost_name!!.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                            resultList.add(cost)
                        }
                    }
                    costFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = costFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                costFilterList = results?.values as ArrayList<Costs>
                notifyDataSetChanged()
            }

        }
    }

}