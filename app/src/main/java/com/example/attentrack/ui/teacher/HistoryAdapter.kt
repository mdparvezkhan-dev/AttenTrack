package com.example.attentrack.ui.teacher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.attentrack.R
import com.example.attentrack.data.HistorySessionDto
import com.example.attentrack.databinding.ItemHistoryBinding
import com.example.attentrack.ui.formatDate
import com.example.attentrack.ui.setBadge

class HistoryAdapter(
    private val onClick: (HistorySessionDto) -> Unit,
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    class VH(val b: ItemHistoryBinding) : RecyclerView.ViewHolder(b.root)

    private var items: List<HistorySessionDto> = emptyList()

    fun submit(list: List<HistorySessionDto>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        holder.b.tvDate.text = formatDate(s.date)
        holder.b.tvCounts.text = "Present ${s.present} \u00B7 Late ${s.late} \u00B7 Absent ${s.absent}  (of ${s.total})"
        if (s.canEdit) {
            holder.b.tvEditable.setBadge("Editable", R.color.brand_on_primary_container, R.color.brand_primary_container)
        } else {
            holder.b.tvEditable.setBadge("Locked", R.color.text_secondary, R.color.divider)
        }
        holder.b.card.setOnClickListener { onClick(s) }
    }
}
