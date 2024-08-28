package id.uinjkt.salaamustadz.ui.adapter.zakat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.databinding.ItemImagesListBinding
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil

typealias Images = String

class DokumentasiSTFAdapter(private val context: Context, private val imagesList: List<Images>) :
    RecyclerView.Adapter<DokumentasiSTFAdapter.ArticleViewHolder>() {


    inner class ArticleViewHolder(binding: ItemImagesListBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageStf = binding.imgStf
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(ItemImagesListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val data = imagesList[position]
        Glide.with(context)
            .load(StorageUtil.pathToReference(data))
            .placeholder(R.color.soft_yellow_green)
            .into(holder.imageStf)

    }
    override fun getItemCount(): Int {
        return imagesList.size
    }
}