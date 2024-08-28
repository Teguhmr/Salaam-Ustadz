package id.uinjkt.salaamustadz.ui.chat

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.utils.ChatAvailability

class ChipAdapter(
    private val context: Context,
    private val chipGroup: ChipGroup,
    private var stringList: List<String>,
    private var defaultChatAvailability: Int
) {
    init {
        notifyDataSetChanged()
    }

    fun setChips(stringList: List<String>, defaultChatAvailability: Int, statusOnClick:(Int)-> Unit) {
        this.stringList = stringList
        this.defaultChatAvailability = defaultChatAvailability
        notifyDataSetChanged{
            statusOnClick.invoke(it)
        }
    }

    private fun notifyDataSetChanged(onClick: ((Int) -> Unit?)? = null) {
        chipGroup.removeAllViews()
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, 20f)
            .build()

        for ((index, text) in stringList.withIndex()) {
            val chip = Chip(context)
            val drawable = ChipDrawable.createFromAttributes(
                context, null, 0,
                R.style.CustomChipChoice
            )
            chip.setChipDrawable(drawable)
            chip.setPadding(10, 10, 10, 10)
            chip.text = text
            chip.isChecked = ChatAvailability.getTitle(text).status == defaultChatAvailability

            val colorStateList =
                ContextCompat.getColorStateList(context, R.color.bg_text_chip_state_list)
            chip.setTextColor(colorStateList)
            chip.shapeAppearanceModel = shapeAppearanceModel
            chip.setChipStrokeColorResource(R.color.bg_text_chip_state_list)
            chip.chipStrokeWidth = 2f

            chip.id = index
            if (chip.isChecked) {
                chip.isChecked = true
            }
            chip.setOnClickListener {
                onClick?.invoke(ChatAvailability.getTitle(text).status)
            }

            chipGroup.addView(chip)
        }
    }
}
