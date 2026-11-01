package com.example.attentrack.ui.student

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.attentrack.data.StudentCourseDto
import com.example.attentrack.databinding.ItemStudentCourseBinding
import com.example.attentrack.ui.color
import com.example.attentrack.ui.pctColorRes
import com.example.attentrack.ui.pctText
import kotlin.math.roundToInt

class StudentCourseAdapter(
    private val onClick: (StudentCourseDto) -> Unit,
) : RecyclerView.Adapter<StudentCourseAdapter.VH>() {

    class VH(val b: ItemStudentCourseBinding) : RecyclerView.ViewHolder(b.root)

    private var items: List<StudentCourseDto> = emptyList()

    fun submit(list: List<StudentCourseDto>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemStudentCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        val color = holder.itemView.context.color(pctColorRes(c.percentage, c.lowAttendance))
        holder.b.tvCode.text = c.code
        holder.b.tvTitle.text = c.title
        holder.b.tvPercent.text = pctText(c.percentage)
        holder.b.tvPercent.setTextColor(color)
        holder.b.progress.setIndicatorColor(color)
        holder.b.progress.setProgressCompat((c.percentage ?: 0.0).roundToInt(), false)
        holder.b.tvStats.text = if (c.totalClasses == 0) {
            "No classes yet"
        } else {
            "Present ${c.present} \u00B7 Late ${c.late} \u00B7 Absent ${c.absent}  (of ${c.totalClasses} classes)"
        }
        holder.b.tvTeacher.text = if (c.teachers.isNullOrBlank()) "" else "Teacher: ${c.teachers}"
        holder.b.tvTeacher.visibility = if (c.teachers.isNullOrBlank()) View.GONE else View.VISIBLE
        holder.b.tvLow.visibility = if (c.lowAttendance) View.VISIBLE else View.GONE
        holder.b.card.setOnClickListener { onClick(c) }
    }
}
