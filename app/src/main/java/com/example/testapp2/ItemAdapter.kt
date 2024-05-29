package com.example.testapp2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(private val dataList: List<String>) : RecyclerView.Adapter<ItemViewHolder>()  {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
            return ItemViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val data = dataList[position]
            holder.item_name.text = data
        }

        override fun getItemCount(): Int {
            return dataList.size
        }
}

class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    var item_name: TextView = itemView.findViewById(R.id.item_name)
}