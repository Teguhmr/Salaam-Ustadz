package id.uinjkt.salaamustadz.ui.adapter.article

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.data.models.article.Article
import id.uinjkt.salaamustadz.databinding.ItemArticleBinding
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil

class ArticleAdapter(private val context: Context, private val arrayList: List<Article>, private val size: Int? = null) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>(), Filterable {
    private var filteredDataList: List<Article> = arrayList.toMutableList()

    interface OnItemClickListener {
        fun onItemClick(article: Article)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    inner class ArticleViewHolder(binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root) {
        val textTitle = binding.txtTitle
        val textCategory = binding.chip
        val imgArticle = binding.imgBgArticle
        val txtUserName = binding.txtUserName
        // Add other views here for image, keywords, textArticle, etc.
        init {
            binding.materialCardView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(filteredDataList[position])
                }
            }
        }
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint.toString()
                val results = FilterResults()

                if (query.isBlank()) {
                    results.values = arrayList
                    results.count = arrayList.size
                } else {

                    val filteredList = filteredDataList.filter { article ->
                        var keyword = ""
                        article.keyword.forEach {
                            keyword = it
                        }
                        article.title.contains(query, ignoreCase = true) ||
                               keyword.contains(query, ignoreCase = true)
                    }
                    results.values = filteredList
                    results.count = filteredList.size
                }

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null) {
                    filteredDataList = results.values as List<Article>
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return size ?: filteredDataList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val currentArticle = filteredDataList[position]
        holder.textTitle.text = currentArticle.title
        holder.textCategory.text = currentArticle.category
        holder.textCategory.isClickable = false
        Glide.with(context)
            .load(StorageUtil.pathToReference(currentArticle.imageUrl))
            .into(holder.imgArticle)
        if (currentArticle.userRole.equals("ustadz") or (currentArticle.userRole.equals("ustadzah"))) {
            holder.txtUserName.text = "oleh Ust. ${currentArticle.userName}"
        }else if (currentArticle.userRole.equals("admin")){
            holder.txtUserName.text = "oleh Redaksi SalaamUstadz"
        }else {
            holder.txtUserName.text = "oleh ${currentArticle.userName}"
        }
        // Bind other article data to views here as needed.
    }


}