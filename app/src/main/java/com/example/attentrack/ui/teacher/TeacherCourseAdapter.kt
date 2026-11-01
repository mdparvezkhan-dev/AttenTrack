package com.example.attentrack.ui.teacher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.attentrack.data.TeacherCourseDto
import com.example.attentrack.databinding.ItemTeacherCourseBinding
import com.example.attentrack.ui.formatDate

class TeacherCourseAdapter(
    private val onClick: (TeacherCourseDto) -> Unit,
) : RecyclerView.Adapter<TeacherCourseAdapter.VH>() {

    class VH(val b: ItemTeacherCourseBinding) : RecyclerView.ViewHolder(b.root)

    private var items: List<TeacherCourseDto> = emptyList()

    fun submit(list: List<TeacherCourseDto>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemTeacherCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        holder.b.tvCode.text = c.code
        holder.b.tvTitle.text = c.title
        holder.b.tvBatch.text = "${c.batch} \u00B7 ${c.section}"
        holder.b.tvStudents.text = "${c.studentCount} students"
        holder.b.tvClasses.text = "${c.classesHeld} classes"
        holder.b.tvLast.text = if (c.lastClassDate != null) "Last: ${formatDate(c.lastClassDate)}" else "No class yet"
        holder.b.card.setOnClickListener { onClick(c) }
    }
}
