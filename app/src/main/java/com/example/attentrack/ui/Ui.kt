package com.example.attentrack.ui

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.attentrack.R
import com.example.attentrack.data.SemesterDto
import com.example.attentrack.databinding.ItemInfoRowBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Edge-to-edge e status bar / navigation bar / keyboard er jaygay padding dey. */
fun View.applyInsets(top: Boolean = false, bottom: Boolean = false, ime: Boolean = false) {
    val l = paddingLeft
    val t = paddingTop
    val r = paddingRight
    val b = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
        )
        val imeBottom = if (ime) insets.getInsets(WindowInsetsCompat.Type.ime()).bottom else 0
        v.setPadding(
            l,
            t + (if (top) bars.top else 0),
            r,
            b + (if (bottom) maxOf(bars.bottom, imeBottom) else 0),
        )
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

fun Context.color(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)

fun todayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

fun parseIso(iso: String?): Date? = try {
    if (iso == null) null else SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso)
} catch (e: Exception) {
    null
}

/** "2026-10-14" -> "14 Oct 2026, Wed" */
fun formatDate(iso: String?): String {
    val d = parseIso(iso) ?: return iso ?: "-"
    return SimpleDateFormat("dd MMM yyyy, EEE", Locale.ENGLISH).format(d)
}

fun pctText(p: Double?): String = when {
    p == null -> "--"
    p % 1.0 == 0.0 -> "${p.toInt()}%"
    else -> "$p%"
}

@ColorRes
fun pctColorRes(p: Double?, low: Boolean): Int = when {
    p == null -> R.color.text_secondary
    low -> R.color.danger
    p < 85.0 -> R.color.warning
    else -> R.color.success
}

/** Chhoto rongin label (Present / Late / Absent / Editable). View e android:background="@drawable/bg_badge" thakte hobe. */
fun TextView.setBadge(label: String, @ColorRes textColor: Int, @ColorRes bgColor: Int) {
    text = label
    setTextColor(context.color(textColor))
    backgroundTintList = ColorStateList.valueOf(context.color(bgColor))
}

fun TextView.setStatusBadge(status: String) {
    when (status) {
        "present" -> setBadge("Present", R.color.success, R.color.success_container)
        "late" -> setBadge("Late", R.color.warning, R.color.warning_container)
        else -> setBadge("Absent", R.color.danger, R.color.danger_container)
    }
}

/** "Good morning" / "Good afternoon" / "Good evening". */
fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

/** "Rahim Uddin" -> "RU". */
fun initials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}

/** Header er semester chip e click korle semester list popup dekhay. */
fun showSemesterMenu(anchor: View, semesters: List<SemesterDto>, onSelect: (SemesterDto) -> Unit) {
    val popup = PopupMenu(anchor.context, anchor)
    semesters.forEachIndexed { index, semester -> popup.menu.add(0, index, index, semester.name) }
    popup.setOnMenuItemClickListener { item ->
        onSelect(semesters[item.itemId])
        true
    }
    popup.show()
}

/** "2026-10-14" -> "14 Oct" (chart label). */
fun formatShortDate(iso: String?): String {
    val d = parseIso(iso) ?: return iso ?: ""
    return SimpleDateFormat("dd MMM", Locale.ENGLISH).format(d)
}

/** Row gulor majhe patla divider line. */
fun LinearLayout.addDivider() {
    val line = View(context)
    line.setBackgroundColor(context.color(R.color.divider))
    addView(line, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1))
}

/** Label + value row gulo (Profile er Details card). */
fun LinearLayout.setInfoRows(inflater: LayoutInflater, rows: List<Pair<String, String>>) {
    removeAllViews()
    rows.forEachIndexed { index, (label, value) ->
        val row = ItemInfoRowBinding.inflate(inflater, this, false)
        row.tvLabel.text = label
        row.tvValue.text = value
        addView(row.root)
        if (index < rows.size - 1) addDivider()
    }
}
