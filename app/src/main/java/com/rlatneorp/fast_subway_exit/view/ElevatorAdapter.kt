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

private const val COLOR_BADGE_HEX = "#FF80AB"
private const val REGEX_NUMBER_PATTERN = "\\d+(?:-\\d+)?"
private const val TEXT_DEFAULT_LOCATION = "출입구"
private const val TEXT_PREFIX_ELEVATOR = "승강기 "
private const val KEYWORD_REPAIR = "보수"
private const val KEYWORD_CHECK = "점검"
private const val KEYWORD_FIX = "수리"
private const val KEYWORD_ESCALATOR = "에스컬레이터"
private const val KEYWORD_ELEVATOR = "엘리베이터"
private const val KEYWORD_WHEELCHAIR = "휠체어"
private const val KEYWORD_MOVING = "무빙"
private const val DEFAULT_FACILITY = "승강기"

private const val BADGE_HEIGHT_DP = 48f
private const val BADGE_MIN_WIDTH_DP = 48f
private const val BADGE_MARGIN_END_DP = 6f
private const val BADGE_TEXT_SIZE_SP = 24f
private const val BADGE_PADDING_DP = 8f

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
        bindLocationInfo(item.location)
        bindFacilityName(item.facilityName)
    }
}

class ElevatorDiffCallback : DiffUtil.ItemCallback<ElevatorRow>() {
    override fun areItemsTheSame(old: ElevatorRow, new: ElevatorRow): Boolean {
        return old.facilityName == new.facilityName && old.location == new.location
    }

    override fun areContentsTheSame(old: ElevatorRow, new: ElevatorRow): Boolean {
        return old == new
    }
}

private fun ElevatorViewHolder.bindRunStatus(status: String) {
    runStatus.text = formatRunStatus(status)

    if (isRepairing(status)) {
        runStatus.setTextColor(Color.parseColor("#CA1A86"))
    } else if (status.contains("가능")) {
        runStatus.setTextColor(Color.parseColor("#4CAF50"))
    } else {
        runStatus.setTextColor(Color.BLACK)
    }
}

private fun ElevatorViewHolder.bindFacilityName(rawName: String) {
    facilityName.text = simplifyFacilityName(rawName)
    facilityName.visibility = View.VISIBLE
}

private fun simplifyFacilityName(rawName: String): String {
    if (rawName.contains(KEYWORD_ESCALATOR)) return KEYWORD_ESCALATOR
    if (rawName.contains(KEYWORD_ELEVATOR)) return KEYWORD_ELEVATOR
    if (rawName.contains(KEYWORD_WHEELCHAIR)) return "$KEYWORD_WHEELCHAIR 리프트"
    if (rawName.contains(KEYWORD_MOVING)) return "$KEYWORD_MOVING 워크"
    return DEFAULT_FACILITY
}

private fun formatRunStatus(status: String): String {
    return status
}

private fun isRepairing(status: String): Boolean {
    return status.contains(KEYWORD_REPAIR) ||
            status.contains(KEYWORD_CHECK) ||
            status.contains(KEYWORD_FIX)
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
    val numberRegex = Regex(REGEX_NUMBER_PATTERN)
    val matches = numberRegex.findAll(location)
    return matches.map { it.value }.toList()
}

private fun ElevatorViewHolder.showNoBadgeState(loc: String) {
    badgeContainer.visibility = View.GONE
    location.text = loc
}

private fun ElevatorViewHolder.showBadgeState(numbers: List<String>, loc: String) {
    addBadgesToContainer(itemView.context, badgeContainer, numbers)
    badgeContainer.visibility = View.VISIBLE
    location.text = cleanLocationText(loc, numbers)
}

private fun cleanLocationText(original: String, numbers: List<String>): String {
    var text = original
    numbers.forEach { text = text.replace(it, "") }
    text = text.replace(",", "").replace("번", "").trim()

    if (text.isBlank()) {
        return TEXT_DEFAULT_LOCATION
    }
    return text
}

private fun addBadgesToContainer(ctx: Context, container: LinearLayout, numbers: List<String>) {
    for (num in numbers) {
        val badge = createBadgeView(ctx, num)
        container.addView(badge)
    }
}

private fun createBadgeView(context: Context, text: String): TextView {
    val badge = TextView(context)
    setupBadgeLayout(context, badge)
    setupBadgeStyle(badge)
    badge.text = text
    return badge
}

private fun setupBadgeLayout(context: Context, badge: TextView) {
    val height = getPx(context, BADGE_HEIGHT_DP)
    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, height)
    params.marginEnd = getPx(context, BADGE_MARGIN_END_DP)

    badge.layoutParams = params
    badge.minimumWidth = getPx(context, BADGE_MIN_WIDTH_DP)

    val padding = getPx(context, BADGE_PADDING_DP)
    badge.setPadding(padding, 0, padding, 0)
}

private fun setupBadgeStyle(badge: TextView) {
    badge.setBackgroundResource(R.drawable.station_button)
    badge.backgroundTintList = ColorStateList.valueOf(Color.parseColor(COLOR_BADGE_HEX))
    badge.gravity = Gravity.CENTER
    badge.includeFontPadding = false
    badge.setTextColor(Color.BLACK)
    badge.setTypeface(null, Typeface.BOLD)
    badge.textSize = BADGE_TEXT_SIZE_SP
}

private fun getPx(context: Context, dp: Float): Int {
    val metrics = context.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics).toInt()
}