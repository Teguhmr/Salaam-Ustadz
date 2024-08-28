package id.uinjkt.salaamustadz.ui.adapter.article

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.data.models.article.Article
import id.uinjkt.salaamustadz.databinding.ItemArticleTopBinding
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil

class ArticleTopAdapter(private val context: Context, private val articlesList: List<Article>) :
    RecyclerView.Adapter<ArticleTopAdapter.ArticleViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(article: Article)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    inner class ArticleViewHolder(binding: ItemArticleTopBinding) : RecyclerView.ViewHolder(binding.root) {
        val textTitle = binding.txtTitleArticle
        val textCategory = binding.txtCategory
        val imgArticle = binding.imgArticle
        val txtUserName = binding.txtUsername
        // Add other views here for image, keywords, textArticle, etc.
        init {
            binding.materialCardView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(articlesList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(ItemArticleTopBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val currentArticle = articlesList[position]
        holder.textTitle.text = currentArticle.title
        holder.textCategory.text = currentArticle.category
        Glide.with(context)
            .load(StorageUtil.pathToReference(currentArticle.imageUrl))
            .into(holder.imgArticle)
        if (currentArticle.userRole.equals("ustadz") or (currentArticle.userRole.equals("ustadzah"))) {
            holder.txtUserName.text = "oleh Ust. ${currentArticle.userName}"
        }else if (currentArticle.userRole.equals("admin")){
            holder.txtUserName.text = "oleh Redaksi SalaamUstadz"
        }else {
            holder.txtUserName.text = "oleh Kontributor ${currentArticle.userName}"
        }
        // Bind other article data to views here as needed.
    }
    override fun getItemCount(): Int {
        val listCount: Int = if (articlesList.size < 6) {
            articlesList.size
        } else {
            6
        }
        return listCount
    }
}