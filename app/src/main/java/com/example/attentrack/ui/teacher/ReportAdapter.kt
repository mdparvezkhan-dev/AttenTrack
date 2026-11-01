package com.example.attentrack.ui.teacher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.attentrack.data.ReportStudentDto
import com.example.attentrack.databinding.ItemReportBinding
import com.example.attentrack.ui.color
import com.example.attentrack.ui.pctColorRes
import com.example.attentrack.ui.pctText
import kotlin.math.roundToInt

class ReportAdapter : RecyclerView.Adapter<ReportAdapter.VH>() {

    class VH(val b: ItemReportBinding) : RecyclerView.ViewHolder(b.root)

    private var items: List<ReportStudentDto> = emptyList()

    fun submit(list: List<ReportStudentDto>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        val color = holder.itemView.context.color(pctColorRes(s.percentage, s.lowAttendance))
        holder.b.tvName.text = s.name
        holder.b.tvId.text = "ID ${s.studentId}"
        holder.b.tvPercent.text = pctText(s.percentage)
        holder.b.tvPercent.setTextColor(color)
        holder.b.progress.setIndicatorColor(color)
        holder.b.progress.setProgressCompat((s.percentage ?: 0.0).roundToInt(), false)
        holder.b.tvStats.text =
            "Present ${s.present} \u00B7 Late ${s.late} \u00B7 Absent ${s.absent}  (of ${s.totalClasses} classes)"
        holder.b.tvLow.visibility = if (s.lowAttendance) View.VISIBLE else View.GONE
    }
}
