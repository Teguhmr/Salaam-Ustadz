package id.uinjkt.salaamustadz.ui.adapter.review

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.uinjkt.salaamustadz.data.models.chat.ConsultReview
import id.uinjkt.salaamustadz.databinding.ItemListReviewBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewAdapter(
    private var dataList: List<ConsultReview>
): RecyclerView.Adapter<ReviewAdapter.MenuHolder>(), Filterable {
    private var filteredDataList: List<ConsultReview> = dataList.toMutableList()
    private var showAllItems = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuHolder {
        return MenuHolder(
            ItemListReviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setShowAllItems(showAll: Boolean) {
        showAllItems = showAll
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (showAllItems) {
            filteredDataList.size
        } else {
            minOf(5, filteredDataList.size)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MenuHolder, position: Int) {
        val data = filteredDataList[position]
        holder.apply {
            data.apply {
                tvTitle.text = titleQuestion
                tvSenderName.text = senderName
                val t1 = createdAt?.toDate()
                val t3 = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(t1 as Date)
                tvDate.text = "  •  ".plus(t3)
                tvReview.text = textReview
                appCompatRatingBar.rating = rating

            }
        }

    }


    inner class MenuHolder(binding: ItemListReviewBinding): RecyclerView.ViewHolder(binding.root) {
        val tvTitle: TextView = binding.tvTitle
        val tvSenderName: TextView = binding.tvSenderName
        val tvDate: TextView = binding.tvDate
        val appCompatRatingBar: RatingBar = binding.appCompatRatingBar
        val tvReview: TextView = binding.tvReview
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint.isNullOrBlank()) {
                    results.values = dataList
                } else {
                    val filteredList = dataList.filter { it.titleQuestion.toString().contains(constraint, ignoreCase = true) }
                    results.values = filteredList
                }
                return results
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null) {
                    filteredDataList = results.values as List<ConsultReview>
                    notifyDataSetChanged()
                }
            }
        }
    }


}