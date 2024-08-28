package id.uinjkt.salaamustadz.utils.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.uinjkt.salaamustadz.databinding.DialogCategoryArticleBinding
import id.uinjkt.salaamustadz.ui.adapter.article.ArticleCategoryAdapter

class CategoryArticleDialog(private val context: Activity, private val articleCategoryAdapter: ArticleCategoryAdapter, private val onOkListener:(category: String) -> Unit): DialogFragment() {

    private lateinit var binding: DialogCategoryArticleBinding
    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogCategoryArticleBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)
        val rvCategoryArticle = binding.rvCategoryArticle

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        rvCategoryArticle.layoutManager = gridLayoutManager
        rvCategoryArticle.setHasFixedSize(true)
        articleCategoryAdapter.loadSelectedPosition()
        rvCategoryArticle.adapter = articleCategoryAdapter

        // Scroll to the selected position smoothly
        val selectedPosition = articleCategoryAdapter.getSelectedPosition()
        if (selectedPosition != -1) {
            gridLayoutManager.smoothScrollToPosition(rvCategoryArticle, RecyclerView.State(), selectedPosition)
        }

        articleCategoryAdapter.setOnItemClickListener(object : ArticleCategoryAdapter.OnItemClickListener{
            override fun onItemClick(name: String) {
                onOkListener.invoke(name)
                dismiss()
            }

        })

        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
}