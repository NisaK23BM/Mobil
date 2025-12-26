package com.example.haritap.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.haritap.R
import com.example.haritap.data.entity.ReportEntity

class ReportAdapter(
    private var items: List<ReportEntity>,
    private val onClick: (ReportEntity) -> Unit
) : RecyclerView.Adapter<ReportAdapter.VH>() {

    fun submit(newItems: List<ReportEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.bind(item, onClick)
    }

    override fun getItemCount() = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)
        private val tvMeta: TextView = itemView.findViewById(R.id.tvMeta)

        fun bind(item: ReportEntity, onClick: (ReportEntity) -> Unit) {
            tvTitle.text = item.title
            tvDesc.text = item.description
            tvMeta.text = "${item.type} • ${item.status}"
            itemView.setOnClickListener { onClick(item) }
        }
    }
}
