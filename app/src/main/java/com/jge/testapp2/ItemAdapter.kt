package com.jge.testapp2

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(private val dataList: List<Item>) : RecyclerView.Adapter<ItemViewHolder>()  {
    var parent_: ViewGroup? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            parent_ = parent
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)

            return ItemViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val data = dataList[position]
            holder.item_name.text = data.name
            holder.item_name.background = if (data.rarity == "6") { ContextCompat.getDrawable(parent_!!.context, R.drawable.six_op_background) }
            else if (data.rarity == "5") { ContextCompat.getDrawable(parent_!!.context, R.drawable.five_op_background) }
            else { ContextCompat.getDrawable(parent_!!.context, R.drawable.op_background) }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }
}

class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    var item_name: TextView = itemView.findViewById(R.id.item_name)

}