package com.example.attentrack.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.attentrack.data.RecordDto
import com.example.attentrack.databinding.ItemRecordBinding
import com.example.attentrack.ui.formatDate
import com.example.attentrack.ui.setStatusBadge

class RecordAdapter : RecyclerView.Adapter<RecordAdapter.VH>() {

    class VH(val b: ItemRecordBinding) : RecyclerView.ViewHolder(b.root)

    private var items: List<RecordDto> = emptyList()

    fun submit(list: List<RecordDto>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.b.tvDate.text = formatDate(r.date)
        holder.b.tvStatus.setStatusBadge(r.status)
    }
}
