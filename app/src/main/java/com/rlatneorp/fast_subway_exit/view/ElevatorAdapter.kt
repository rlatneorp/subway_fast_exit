package com.rlatneorp.fast_subway_exit.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rlatneorp.fast_subway_exit.R
import com.rlatneorp.fast_subway_exit.model.ElevatorUIModel

private const val COLOR_BADGE_BG = "#FF80AB"
private const val COLOR_STATUS_REPAIR = "#E91E63"
private const val COLOR_STATUS_OK = "#4CAF50"
private const val COLOR_STATUS_DEFAULT = "#000000"
private const val COLOR_TEXT_LOCATION = "#000000"
private const val REGEX_NUMBER_PATTERN = "\\d+(?:-\\d+)?"
private const val REGEX_DIRECTION_PATTERN = "\\S+\\s*방면"
private const val TEXT_DEFAULT_LOCATION = "출입구"
private const val TEXT_PREFIX_ELEVATOR = "승강기 "
private const val KEY_REPAIR = "보수"
private const val KEY_CHECK = "점검"
private const val KEY_FIX = "수리"
private const val KEY_OK = "가능"
private const val BADGE_H_DP = 44f
private const val BADGE_MIN_W_DP = 44f
private const val BADGE_MARGIN_DP = 6f
private const val BADGE_TEXT_SP = 20f
private const val BADGE_PAD_DP = 8f
private const val LOC_TEXT_SP = 26f
private const val LOC_MARGIN_START_DP = 10f
private const val ROW_MARGIN_BOTTOM_DP = 8f
private const val BADGES_PER_ROW = 4

class ElevatorAdapter : ListAdapter<ElevatorUIModel, ElevatorViewHolder>(ElevatorDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElevatorViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_elevator, parent, false)
        return ElevatorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ElevatorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ElevatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val runStatus: TextView = itemView.findViewById(R.id.tvRunStatus)
    private val facilityName: TextView = itemView.findViewById(R.id.tvFacilityName)
    private val container: LinearLayout = itemView.findViewById(R.id.llBadges)

    fun bind(item: ElevatorUIModel) {
        bindStatus(item.runStatus)
        bindFacility(item.facilityName)
        bindLocationInfo(item.location)
    }

    fun getContainer(): LinearLayout = container
    fun setFacilityText(text: String) { facilityName.text = text }
    fun setStatusText(text: String) { runStatus.text = text }
    fun setStatusColor(color: Int) { runStatus.setTextColor(color) }
}

class ElevatorDiffCallback : DiffUtil.ItemCallback<ElevatorUIModel>() {
    override fun areItemsTheSame(old: ElevatorUIModel, new: ElevatorUIModel): Boolean {
        return old.location == new.location && old.facilityName == new.facilityName
    }

    override fun areContentsTheSame(old: ElevatorUIModel, new: ElevatorUIModel): Boolean {
        return old == new
    }
}

private fun ElevatorViewHolder.bindLocationInfo(loc: String) {
    val container = getContainer()
    container.removeAllViews()
    val numbers = extractNumbers(loc)
    val text = cleanLocationText(loc, numbers)
    if (numbers.isEmpty()) {
        addSingleTextRow(container, text)
        return
    }
    addBadgeRows(container, numbers, text)
}

private fun addSingleTextRow(container: LinearLayout, text: String) {
    val row = createRowLayout(container.context)
    val textView = createLocationTextView(container.context, text)
    row.addView(textView)
    container.addView(row)
}

private fun addBadgeRows(container: LinearLayout, numbers: List<String>, text: String) {
    val chunks = numbers.chunked(BADGES_PER_ROW)
    chunks.forEachIndexed { index, chunk ->
        createAndAddRow(container, chunk, index == chunks.lastIndex, text)
    }
}

private fun createAndAddRow(container: LinearLayout, chunk: List<String>, isLastRow: Boolean, text: String) {
    val row = createRowLayout(container.context)
    chunk.forEach { num ->
        row.addView(createBadge(container.context, num))
    }
    if (isLastRow) {
        row.addView(createLocationTextView(container.context, text))
    }
    container.addView(row)
}

private fun createRowLayout(context: Context): LinearLayout {
    val row = LinearLayout(context)
    row.orientation = LinearLayout.HORIZONTAL
    val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    params.bottomMargin = getPx(context, ROW_MARGIN_BOTTOM_DP)
    row.layoutParams = params
    row.gravity = Gravity.CENTER_VERTICAL or Gravity.START
    return row
}

private fun createLocationTextView(context: Context, text: String): TextView {
    val textView = TextView(context)
    textView.text = text
    textView.textSize = LOC_TEXT_SP
    textView.setTextColor(Color.parseColor(COLOR_TEXT_LOCATION))
    textView.setTypeface(null, Typeface.BOLD)
    val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    params.marginStart = getPx(context, LOC_MARGIN_START_DP)
    textView.layoutParams = params
    return textView
}

private fun extractNumbers(location: String): List<String> {
    val regex = Regex(REGEX_NUMBER_PATTERN)
    return regex.findAll(location).map { it.value }.toList()
}

private fun cleanLocationText(original: String, numbers: List<String>): String {
    var text = original
    numbers.forEach { text = text.replace(it, "") }
    text = text.replace(Regex(REGEX_DIRECTION_PATTERN), "")
    text = text.replace("번", "")
        .replace("출입구", "")
        .replace("출구", "")
        .replace("환승통로", "")
        .replace(",", "")
        .replace("(", "").replace(")", "")
        .replace("-", "").replace("~", "")
        .replace("/", "").replace(".", "")
        .trim()
    if (text.isBlank()) return TEXT_DEFAULT_LOCATION
    return text
}

private fun createBadge(ctx: Context, text: String): TextView {
    val badge = TextView(ctx)
    applyBadgeLayout(ctx, badge)
    applyBadgeStyle(badge)
    badge.text = text
    return badge
}

private fun applyBadgeLayout(ctx: Context, badge: TextView) {
    val height = getPx(ctx, BADGE_H_DP)
    val minWidth = getPx(ctx, BADGE_MIN_W_DP)
    val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, height
    )
    params.marginEnd = getPx(ctx, BADGE_MARGIN_DP)
    badge.layoutParams = params
    badge.minimumWidth = minWidth
    val pad = getPx(ctx, BADGE_PAD_DP)
    badge.setPadding(pad, 0, pad, 0)
}

private fun applyBadgeStyle(badge: TextView) {
    badge.setBackgroundResource(R.drawable.station_button)
    badge.backgroundTintList = ColorStateList.valueOf(
        Color.parseColor(COLOR_BADGE_BG)
    )
    badge.gravity = Gravity.CENTER
    badge.includeFontPadding = false
    badge.setTextColor(Color.BLACK)
    badge.setTypeface(null, Typeface.BOLD)
    badge.textSize = BADGE_TEXT_SP
}

private fun getPx(ctx: Context, dp: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, ctx.resources.displayMetrics
    ).toInt()
}

private fun ElevatorViewHolder.bindFacility(name: String) {
    setFacilityText(name)
}

private fun ElevatorViewHolder.bindStatus(status: String) {
    setStatusText(formatStatusText(status))
    setStatusColor(getStatusColor(status))
}

private fun formatStatusText(status: String): String {
    if (isRepairing(status)) return "$TEXT_PREFIX_ELEVATOR$status"
    return status
}

private fun getStatusColor(status: String): Int {
    if (isRepairing(status)) return Color.parseColor(COLOR_STATUS_REPAIR)
    if (status.contains(KEY_OK)) return Color.parseColor(COLOR_STATUS_OK)
    return Color.parseColor(COLOR_STATUS_DEFAULT)
}

private fun isRepairing(status: String): Boolean {
    return status.contains(KEY_REPAIR) ||
            status.contains(KEY_CHECK) ||
            status.contains(KEY_FIX)
}