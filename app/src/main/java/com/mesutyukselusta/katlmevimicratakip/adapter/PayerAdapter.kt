package com.mesutyukselusta.katlmevimicratakip.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.mesutyukselusta.katlmevimicratakip.R
import com.mesutyukselusta.katlmevimicratakip.databinding.RowPayerListBinding
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import java.util.*
import kotlin.collections.ArrayList

class PayerAdapter(private val payerList : ArrayList<PayerInfo>): RecyclerView.Adapter<PayerAdapter.PayerViewHolder>(),
    Filterable {
    private  val TAG = "payerFilterList"

    private lateinit var mListener : onItemClickListener
    var payerFilterList = ArrayList<PayerInfo>()

    init {
        payerFilterList = payerList
    }

    interface onItemClickListener {
        fun onItemClick(fireStoreDocumentNo : String)
    }

    fun setOnItemClickListener(listener : onItemClickListener){
        mListener = listener
    }

    class PayerViewHolder(private val itemBinding: RowPayerListBinding,listener : onItemClickListener) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(payerInfo: PayerInfo) {
            itemBinding.txtName.text = payerInfo.name
            itemBinding.txtSurname.text = payerInfo.surname
            itemBinding.txtDocumentId.text = payerInfo.document_year.toString()+"-"+payerInfo.document_no.toString()
            itemBinding.txtDocumentType.text = payerInfo.document_type
            itemBinding.containerUuid.text = payerInfo.firestore_document_no

            if (payerInfo.document_status.equals("avans_iade")){
                itemBinding.container.setBackgroundColor(Color.parseColor("#EEEE9B"))
            } else if (payerInfo.document_status.equals("dosya_kapandı")){
                itemBinding.container.setBackgroundColor(Color.parseColor("#F47174"))
            } else {
                itemBinding.container.setBackgroundColor(Color.TRANSPARENT)
            }

        }
        init{
            itemView.setOnClickListener {
                listener.onItemClick(itemBinding.containerUuid.text.toString())
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PayerViewHolder {
        val itemBinding = RowPayerListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PayerViewHolder(itemBinding,mListener)
    }

    override fun onBindViewHolder(holder: PayerViewHolder, position: Int) {
        val payerInfo: PayerInfo = payerFilterList[position]
        holder.bind(payerInfo)
    }

    override fun getItemCount(): Int {
        return payerFilterList.size
    }

    fun updatePayerAdapter(newPayerList : List<PayerInfo>){
        payerList.clear()
        payerList.addAll(newPayerList)
        payerFilterList = payerList
        notifyDataSetChanged()
    }

    fun getPayerFromPosition(position : Int) : PayerInfo {
        return payerFilterList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    payerFilterList = payerList
                } else {
                    val resultList = ArrayList<PayerInfo>()
                    for (payer in payerList) {
                        if (payer.name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) ||
                            payer.surname!!.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) ||
                            payer.document_no.toString().lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) ||
                            payer.document_year.toString().lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                            resultList.add(payer)
                        }
                    }
                    payerFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = payerFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                payerFilterList = results?.values as ArrayList<PayerInfo>
                for (payer in payerFilterList){
                    Log.d(TAG, "publishResults: " + payer.name)
                }
                notifyDataSetChanged()
            }

        }
    }

}