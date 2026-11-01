package com.example.attentrack.ui.teacher

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.attentrack.R
import com.example.attentrack.databinding.ItemStudentAttendanceBinding
import com.example.attentrack.ui.color

class AttendanceAdapter(
    private val onChanged: () -> Unit,
) : RecyclerView.Adapter<AttendanceAdapter.VH>() {

    class VH(val b: ItemStudentAttendanceBinding) : RecyclerView.ViewHolder(b.root)

    private var all: List<AttendanceRow> = emptyList()
    private var shown: List<AttendanceRow> = emptyList()
    private var query: String = ""

    var readOnly: Boolean = false

    fun submit(rows: List<AttendanceRow>) {
        all = rows
        applyFilter()
    }

    fun filter(text: String) {
        query = text.trim().lowercase()
        applyFilter()
    }

    fun refreshAll() {
        notifyDataSetChanged()
    }

    private fun applyFilter() {
        shown = if (query.isEmpty()) {
            all
        } else {
            all.filter { it.name.lowercase().contains(query) || it.id.toString().contains(query) }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemStudentAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = shown.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        bindRow(holder.b, shown[position])
    }

    private fun bindRow(b: ItemStudentAttendanceBinding, row: AttendanceRow) {
        val ctx = b.root.context
        b.tvName.text = row.name
        b.tvId.text = "ID ${row.id}" + (if (row.dept != null) " \u00B7 ${row.dept}" else "")

        // remove listeners first, otherwise recycled rows trigger wrong callbacks
        b.cbPresent.setOnCheckedChangeListener(null)
        b.cbLate.setOnCheckedChangeListener(null)

        b.cbPresent.isChecked = row.present
        b.cbLate.isChecked = row.late
        b.cbPresent.isEnabled = !readOnly
        b.cbLate.isEnabled = !readOnly && row.present
        b.cbLate.alpha = if (row.present) 1f else 0.4f
        b.row.setBackgroundColor(if (row.present) Color.TRANSPARENT else ctx.color(R.color.danger_container))

        b.cbPresent.setOnCheckedChangeListener { _, checked ->
            row.present = checked
            if (!checked) row.late = false
            bindRow(b, row)
            onChanged()
        }
        b.cbLate.setOnCheckedChangeListener { _, checked ->
            row.late = checked
            onChanged()
        }
    }
}
