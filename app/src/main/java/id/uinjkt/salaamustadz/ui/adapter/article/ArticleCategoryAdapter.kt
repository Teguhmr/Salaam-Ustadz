package id.uinjkt.salaamustadz.ui.adapter.article

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.databinding.ItemCategoryBinding
import id.uinjkt.salaamustadz.utils.PreferenceManager

class ArticleCategoryAdapter(private val context: Context, private val articlesList: List<String>) :
    RecyclerView.Adapter<ArticleCategoryAdapter.ArticleViewHolder>() {
    var selectedItemPos = -1
    var lastItemSelectedPos = -1
    interface OnItemClickListener {
        fun onItemClick(name: String)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    inner class ArticleViewHolder(binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        val textTitle = binding.txtTitle
        val materialCardView = binding.materialCardView

        init {
            binding.materialCardView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(articlesList[position])
                }
                setSelectedPositionAndSave(position)
                selectedItemPos = bindingAdapterPosition
                lastItemSelectedPos = if(lastItemSelectedPos == -1)
                    selectedItemPos
                else {
                    notifyItemChanged(lastItemSelectedPos)
                    selectedItemPos
                }
                notifyItemChanged(selectedItemPos)
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val currentArticle = articlesList[position]
        holder.textTitle.text = currentArticle
        if(position == selectedItemPos) {
            holder.materialCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.dark_blue_primary))
            holder.textTitle.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
        else {
            holder.materialCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            holder.textTitle.setTextColor(ContextCompat.getColor(context, R.color.dark_blue_primary))
        }


    }

    override fun getItemCount() = articlesList.size

    fun setSelectedPositionAndSave(position: Int) {
        selectedItemPos = position

        // Save selected position to PreferenceManager
        val sharedPreferences = PreferenceManager(context)
        sharedPreferences.putInt("selected_position", selectedItemPos)
    }

    // Method to load the selected position from PreferenceManager
    fun loadSelectedPosition() {
        val sharedPreferences = PreferenceManager(context)
        selectedItemPos = sharedPreferences.getInt("selected_position")
    }

    fun clearSelectedPosition() {
        val sharedPreferences = PreferenceManager(context)
        sharedPreferences.removeSingleValueString("selected_position")
    }

    fun getSelectedPosition(): Int {
        return selectedItemPos
    }
}