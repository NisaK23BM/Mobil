package com.example.haritap.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.haritap.Announcement
import com.example.haritap.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnnouncementAdapter(
    private var items: List<Announcement>,
    private val isAdmin: Boolean,
    private val onDelete: (Announcement) -> Unit
) : RecyclerView.Adapter<AnnouncementAdapter.VH>() {

    private val fmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR"))

    fun submit(newList: List<Announcement>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_announcement, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val a = items[position]
        holder.tvTitle.text = a.title
        holder.tvMsg.text = a.message
        holder.tvTime.text = fmt.format(Date(a.time))

        holder.btnDelete.visibility = if (isAdmin) View.VISIBLE else View.GONE
        holder.btnDelete.setOnClickListener { onDelete(a) }
    }

    override fun getItemCount(): Int = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        val tvMsg: TextView = v.findViewById(R.id.tvMessage)
        val tvTime: TextView = v.findViewById(R.id.tvTime)
        val btnDelete: Button = v.findViewById(R.id.btnDelete)
    }
}
