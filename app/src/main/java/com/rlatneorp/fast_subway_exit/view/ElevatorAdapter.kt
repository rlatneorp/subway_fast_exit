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
import com.rlatneorp.fast_subway_exit.model.ElevatorRow
import androidx.core.graphics.toColorInt

private const val COLOR_BADGE_BG = "#FF80AB"
private const val COLOR_STATUS_REPAIR = "#E91E63"
private const val COLOR_STATUS_OK = "#4CAF50"
private const val COLOR_STATUS_DEFAULT = "#000000"
private const val REGEX_NUMBER_PATTERN = "\\d+(?:-\\d+)?"
private const val REGEX_DIRECTION_PATTERN = "\\S+\\s*방면"
private const val TEXT_DEFAULT_LOCATION = "출입구"
private const val TEXT_PREFIX_ELEVATOR = "승강기 "
private const val KEY_REPAIR = "보수"
private const val KEY_CHECK = "점검"
private const val KEY_FIX = "수리"
private const val KEY_OK = "가능"
private const val BADGE_H_DP = 40f
private const val BADGE_MIN_W_DP = 40f
private const val BADGE_MARGIN_DP = 6f
private const val BADGE_TEXT_SP = 28f
private const val BADGE_PAD_DP = 8f

class ElevatorAdapter : ListAdapter<ElevatorRow, ElevatorViewHolder>(ElevatorDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElevatorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_elevator, parent, false)
        return ElevatorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ElevatorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ElevatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val location: TextView = itemView.findViewById(R.id.tvLocation)
    val runStatus: TextView = itemView.findViewById(R.id.tvRunStatus)
    val facilityName: TextView = itemView.findViewById(R.id.tvFacilityName)
    val badgeContainer: LinearLayout = itemView.findViewById(R.id.llBadges)

    fun bind(item: ElevatorRow) {
        bindRunStatus(item.runStatus)
        bindFacility(item.facilityName)
        bindLocationInfo(item.location)
    }
}

class ElevatorDiffCallback : DiffUtil.ItemCallback<ElevatorRow>() {
    override fun areItemsTheSame(old: ElevatorRow, new: ElevatorRow) =
        old.facilityName == new.facilityName && old.location == new.location

    override fun areContentsTheSame(old: ElevatorRow, new: ElevatorRow) = old == new
}

private fun ElevatorViewHolder.bindRunStatus(status: String) {
    runStatus.text = formatRunStatus(status)
    runStatus.setTextColor(getStatusColor(status))
}

private fun formatRunStatus(status: String): String {
    if (isRepairing(status)) return status
    return status
}

private fun getStatusColor(status: String): Int {
    if (isRepairing(status)) return Color.parseColor(COLOR_STATUS_REPAIR)
    if (status.contains(KEY_OK)) return COLOR_STATUS_OK.toColorInt()
    return COLOR_STATUS_DEFAULT.toColorInt()
}

private fun isRepairing(status: String): Boolean {
    return status.contains(KEY_REPAIR) ||
            status.contains(KEY_CHECK) ||
            status.contains(KEY_FIX)
}

private fun ElevatorViewHolder.bindFacility(rawName: String) {
    facilityName.text = simplifyFacilityName(rawName)
}

private fun simplifyFacilityName(rawName: String): String {
    if (rawName.contains("에스컬레이터")) return "에스컬레이터"
    if (rawName.contains("엘리베이터")) return "엘리베이터"
    if (rawName.contains("휠체어")) return "휠체어 리프트"
    if (rawName.contains("무빙")) return "무빙워크"
    return "승강기"
}

private fun ElevatorViewHolder.bindLocationInfo(loc: String) {
    badgeContainer.removeAllViews()
    val numbers = extractNumbers(loc)

    if (numbers.isEmpty()) {
        showNoBadgeState(loc)
        return
    }
    showBadgeState(numbers, loc)
}

private fun extractNumbers(location: String): List<String> {
    val regex = Regex(REGEX_NUMBER_PATTERN)
    return regex.findAll(location).map { it.value }.toList()
}

private fun ElevatorViewHolder.showNoBadgeState(loc: String) {
    badgeContainer.visibility = View.GONE
    location.text = loc
}

private fun ElevatorViewHolder.showBadgeState(numbers: List<String>, loc: String) {
    addBadges(itemView.context, badgeContainer, numbers)
    badgeContainer.visibility = View.VISIBLE
    location.text = cleanLocationText(loc, numbers)
}

private fun cleanLocationText(original: String, numbers: List<String>): String {
    var text = original

    numbers.forEach { text = text.replace(it, "") }
    text = text.replace(Regex(REGEX_DIRECTION_PATTERN), "")
    text = text.replace("번", "")
        .replace("출입구", "")
        .replace("출구", "")
        .replace(",", "")
        .trim()

    if (text.isBlank()) return TEXT_DEFAULT_LOCATION
    return text
}

private fun addBadges(ctx: Context, container: LinearLayout, numbers: List<String>) {
    for (num in numbers) {
        container.addView(createBadge(ctx, num))
    }
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
        COLOR_BADGE_BG.toColorInt()
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