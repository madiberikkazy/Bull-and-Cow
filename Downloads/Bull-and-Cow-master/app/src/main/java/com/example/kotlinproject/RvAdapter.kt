package com.example.kotlinproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinproject.models.UserNumber

class RvAdapter(private val items: List<UserNumber>) : RecyclerView.Adapter<RvAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numberTextView: TextView = itemView.findViewById(R.id.text_main)
        val buqaSanyTextView: TextView = itemView.findViewById(R.id.txt_buqa_sany)
        val siyrSanyTextView: TextView = itemView.findViewById(R.id.txt_siyr_sany)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rv, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userNumber = items[position]
        holder.numberTextView.text = userNumber.number
        holder.buqaSanyTextView.text = userNumber.buqaSany
        holder.siyrSanyTextView.text = userNumber.siyrSany
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
