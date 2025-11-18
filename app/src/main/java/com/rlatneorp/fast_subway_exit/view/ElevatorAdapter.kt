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
private const val BADGE_HEIGHT_DP = 48f
private const val BADGE_MIN_WIDTH_DP = 48f
private const val BADGE_MARGIN_END_DP = 6f
private const val BADGE_TEXT_SIZE_SP = 24f
private const val BADGE_PADDING_DP = 8f

class ElevatorAdapter : ListAdapter<ElevatorRow, ElevatorAdapter.ElevatorViewHolder>(ElevatorDiffCallback()) {

    class ElevatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val location: TextView = itemView.findViewById(R.id.tvLocation)
        private val runStatus: TextView = itemView.findViewById(R.id.tvRunStatus)
        private val badgeContainer: LinearLayout = itemView.findViewById(R.id.llBadges)

        fun bind(item: ElevatorRow) {
            runStatus.text = getFormattedRunStatus(item.runStatus)

            badgeContainer.removeAllViews()

            val numberRegex = Regex(REGEX_NUMBER_PATTERN)
            val matches = numberRegex.findAll(item.location)
            val numbers = matches.map { it.value }.toList()

            if (numbers.isEmpty()) {
                badgeContainer.visibility = View.GONE
                location.text = item.location
                return
            }

            createBadges(numbers)
            badgeContainer.visibility = View.VISIBLE

            location.text = getCleanedLocationText(item.location, numbers)
        }

        private fun getFormattedRunStatus(status: String): String {
            if (status.contains(KEYWORD_REPAIR) || status.contains(KEYWORD_CHECK) || status.contains(KEYWORD_FIX)) {
                return "$TEXT_PREFIX_ELEVATOR$status"
            }
            return status
        }

        private fun getCleanedLocationText(originalLocation: String, numbers: List<String>): String {
            var cleanLocation = originalLocation

            numbers.forEach { num ->
                cleanLocation = cleanLocation.replace(num, "")
            }

            cleanLocation = cleanLocation.replace(",", "")
                .trim()

            if (cleanLocation.isBlank()) {
                return TEXT_DEFAULT_LOCATION
            }
            return cleanLocation
        }

        private fun createBadges(numbers: List<String>) {
            val context = itemView.context
            for (num in numbers) {
                val badge = createSingleBadge(context, num)
                badgeContainer.addView(badge)
            }
        }

        private fun createSingleBadge(context: Context, text: String): TextView {
            val badge = TextView(context)

            val heightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BADGE_HEIGHT_DP, context.resources.displayMetrics).toInt()
            val minWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BADGE_MIN_WIDTH_DP, context.resources.displayMetrics).toInt()

            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, heightPx)
            layoutParams.marginEnd = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BADGE_MARGIN_END_DP, context.resources.displayMetrics).toInt()

            badge.layoutParams = layoutParams
            badge.minimumWidth = minWidthPx

            val paddingPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BADGE_PADDING_DP, context.resources.displayMetrics).toInt()
            badge.setPadding(paddingPx, 0, paddingPx, 0)

            badge.setBackgroundResource(R.drawable.station_button)
            badge.backgroundTintList = ColorStateList.valueOf(Color.parseColor(COLOR_BADGE_HEX))

            badge.text = text
            badge.gravity = Gravity.CENTER
            badge.includeFontPadding = false
            badge.setTextColor(Color.BLACK)
            badge.setTypeface(null, Typeface.BOLD)
            badge.textSize = BADGE_TEXT_SIZE_SP

            return badge
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElevatorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_elevator, parent, false)
        return ElevatorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ElevatorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ElevatorDiffCallback : DiffUtil.ItemCallback<ElevatorRow>() {
    override fun areItemsTheSame(oldItem: ElevatorRow, newItem: ElevatorRow): Boolean {
        return oldItem.facilityName == newItem.facilityName && oldItem.location == newItem.location
    }

    override fun areContentsTheSame(oldItem: ElevatorRow, newItem: ElevatorRow): Boolean {
        return oldItem == newItem
    }
}