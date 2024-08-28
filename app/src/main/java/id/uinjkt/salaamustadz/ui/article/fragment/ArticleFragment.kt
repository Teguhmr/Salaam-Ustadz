package id.uinjkt.salaamustadz.ui.article.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.transition.MaterialSharedAxis
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.article.Article
import id.uinjkt.salaamustadz.databinding.FragmentArticleBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.adapter.article.ArticleAdapter
import id.uinjkt.salaamustadz.ui.adapter.article.ArticleCategoryAdapter
import id.uinjkt.salaamustadz.ui.adapter.article.ArticleTopAdapter
import id.uinjkt.salaamustadz.ui.article.ArticleViewModel
import id.uinjkt.salaamustadz.ui.article.activity.FavoriteArticlesActivity
import id.uinjkt.salaamustadz.ui.article.activity.SearchArticleActivity
import id.uinjkt.salaamustadz.ui.article.detail.DetailArticleActivity
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.dialog.CategoryArticleDialog
import id.uinjkt.salaamustadz.utils.gone
import id.uinjkt.salaamustadz.utils.recyclerview.AutoScrollManager
import id.uinjkt.salaamustadz.utils.recyclerview.SetUpIndicator
import id.uinjkt.salaamustadz.utils.show
import id.uinjkt.salaamustadz.utils.toast

class ArticleFragment : Fragment() {

    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding as FragmentArticleBinding
    private lateinit var articleViewModel: ArticleViewModel
    private lateinit var articleAdapter: ArticleAdapter
    private var categoryAdapter: ArticleCategoryAdapter? = null
    private lateinit var articleTopAdapter: ArticleTopAdapter
    private var autoScrollManager: AutoScrollManager? = null
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentArticleBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        articleViewModel = ViewModelProvider(this)[ArticleViewModel::class.java]


        // Create and start the AutoScrollManager
        preferenceManager = PreferenceManager(requireContext())

        fetchArticle()
        fetchCategory()
        menuItemClickListener()
    }

    private fun fetchCategory() {
        val listCategories:ArrayList<String> = ArrayList()
        articleViewModel.categoryLiveData.observe(viewLifecycleOwner) { categories ->
            when (categories) {
                is Result.Error -> {
                    stopLoadingCategory()
                    requireContext().toast(categories.message.toString())
                }
                is Result.Loading -> startLoadingCategory()
                is Result.Success -> {
                    stopLoadingCategory()
                    listCategories.add("Semua")
                    categories.data?.take(4)?.forEach { dataCategory ->
                        listCategories.add(dataCategory)

                    }
                    listCategories.add("Lainnya")
                    setChipsFromArrayList(listCategories)
                }
            }

        }
        articleViewModel.categoryLiveData.observe(viewLifecycleOwner) {categoryArticle ->
            when (categoryArticle){
                is Result.Error -> {
                    stopLoadingArticle()
                    requireContext().toast(categoryArticle.message.toString())
                }
                is Result.Loading -> {
                    startLoadingArticle()
                }
                is Result.Success -> {
                    stopLoadingArticle()
                    categoryAdapter = ArticleCategoryAdapter(requireContext(),
                        categoryArticle.data?.toList()!!
                    )
                }

            }
        }
    }
    private fun setChipsFromArrayList(stringList:List<String>) {
        var index = 0
        val shapeAppearanceModel = ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, 20f)
                .build()
        val chipGroup: ChipGroup = binding.chipGroup
        if (chipGroup.childCount > 0) {
            return
        }
        for (text in stringList) {
            val chip = Chip(context)
            val drawable = context?.let { it1 ->
                ChipDrawable.createFromAttributes(
                    it1, null, 0,
                    R.style.CustomChipChoice
                )
            }
            chip.setChipDrawable(drawable!!)
            chip.setPadding(10, 10, 10, 10)
            chip.text = text
            index++

            chip.isChecked = text == "Semua"

            val colorStateList =
                ContextCompat.getColorStateList(requireContext(), R.color.bg_text_chip_state_list)
            chip.setTextColor(colorStateList)
            chip.shapeAppearanceModel = shapeAppearanceModel
            chip.setChipStrokeColorResource(R.color.bg_text_chip_state_list)
            chip.chipStrokeWidth = 2f

            chip.id = index
            if (chip.isChecked) {
                getArticle(text)
                preferenceManager.putString("category", text)
            }

            chip.setOnClickListener {
                if (text != "Lainnya") {
                    getArticle(text)
                    preferenceManager.putString("category", text)
                } else {
                    chip.isClickable = true
                    CategoryArticleDialog(requireActivity(), categoryAdapter!!){ categoryOther ->
                        getArticle(categoryOther)
                    }.show(childFragmentManager, "categoryArticleDialog")
                }
                preferenceManager.putString("category", text)

            }
            chipGroup.setOnCheckedStateChangeListener { group,  _ ->
                for (j in 0 until group.childCount) {
                    val chips = group.getChildAt(j)
                    chips.isClickable = chips.id != group.checkedChipId
                }
            }

            chipGroup.addView(chip)
        }
    }

    private fun getArticle(category: String) {
        val rvArticle = binding.rvArticle
        articleViewModel.fetchCategoryArticle(category)
        articleViewModel.articlesCategoryLiveData.observe(viewLifecycleOwner) { screenState ->
            when (screenState) {
                is Result.Loading -> {
//                    startLoadingArticle()
                }
                is Result.Success -> {
//                    stopLoadingArticle()
                    if (screenState.data?.isNotEmpty() == true) {
                        rvArticle.show()
                        notFoundLottieVisibility(false)
                        articleAdapter = ArticleAdapter(requireContext(), screenState.data)

                        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
                        rvArticle.layoutManager = linearLayoutManager
                        rvArticle.setHasFixedSize(true)
                        rvArticle.adapter = articleAdapter

                        articleAdapter.setOnItemClickListener(object : ArticleAdapter.OnItemClickListener{
                            override fun onItemClick(article: Article) {
                                val intent = Intent(requireContext(), DetailArticleActivity::class.java)
                                intent.putExtra(Constants.KEY_ARTICLE, article) // Pass any relevant data to the detail activity
                                intent.putExtra(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID)) // Pass any relevant data to the detail activity
                                startActivity(intent)
                            }

                        })

                    } else {
                        rvArticle.gone()
                        notFoundLottieVisibility(true)
                    }
                }
                is Result.Error -> {
                    // Handle error state
                    requireContext().toast(screenState.message.toString())
//                    stopLoadingArticle()
                }
            }
        }
    }

    private fun notFoundLottieVisibility(visibility: Boolean){
        if (visibility){
            binding.lottieAnimationNotFound.show()
            binding.bodyAnnounce.show()
        } else {

            binding.lottieAnimationNotFound.gone()
            binding.bodyAnnounce.gone()
        }
    }

    private fun fetchArticle() {
        val rvTopArticle= binding.rvTopArticle
        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
        rvTopArticle.layoutManager = linearLayoutManager
        rvTopArticle.setHasFixedSize(true)
        autoScrollManager = AutoScrollManager(rvTopArticle)
        autoScrollManager?.startAutoScroll()
        articleViewModel.articleLiveData.observe(viewLifecycleOwner){ state ->
            when(state) {
                is Result.Loading -> {
                    startLoading()
                }
                is Result.Success -> {
                    stopLoading()
                    if (state.data?.isNotEmpty() == true){
                        articleTopAdapter = ArticleTopAdapter(requireContext(), state.data.filter { it.numberOfViews!! > 0 }.sortedByDescending { it.numberOfViews })
                        rvTopArticle.adapter = articleTopAdapter
                        rvTopArticle.onFlingListener = null

                        val snapHelper = PagerSnapHelper()
                        snapHelper.attachToRecyclerView(rvTopArticle)
                        SetUpIndicator(requireContext(), rvTopArticle, snapHelper, linearLayoutManager, binding.dotLayout, state.data)

                        articleTopAdapter.setOnItemClickListener(object : ArticleTopAdapter.OnItemClickListener {
                            override fun onItemClick(article: Article) {
                                // Handle the item click here, e.g., start the new activity with an Intent
                                val intent = Intent(requireContext(), DetailArticleActivity::class.java)
                                intent.putExtra(Constants.KEY_ARTICLE, article) // Pass any relevant data to the detail activity
                                intent.putExtra(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                                startActivity(intent)
                            }
                        })
                    }

                }
                is Result.Error -> {
                    requireContext().toast(state.message.toString())
                    stopLoading()
                }
            }
        }
    }
    private fun menuItemClickListener() {
        binding.toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.search -> {
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z,  true)
                    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z,  false)
                    val intent = Intent(requireContext(), SearchArticleActivity::class.java)
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity())

                    intent.putExtra(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                    startActivity(intent, options.toBundle())
                    true
                }
                R.id.bookmark_article -> {
                    val intent = Intent(requireContext(), FavoriteArticlesActivity::class.java)

                    intent.putExtra(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
    private fun startLoading(){
        binding.shimmerFrameLayout.apply {
            show()
            startShimmer()
        }
        startLoadingArticle()
    }
    private fun stopLoading(){
        binding.shimmerFrameLayout.apply {
            gone()
            stopShimmer()
        }
        stopLoadingArticle()
    }
    private fun startLoadingArticle(){
        binding.shimmerFrameLayoutArticle.apply {
            show()
            startShimmer()
        }
    }
    private fun stopLoadingArticle(){
        binding.shimmerFrameLayoutArticle.apply {
            gone()
            stopShimmer()
        }
    }
    private fun startLoadingCategory(){
        binding.shimmerFrameLayoutCategory.apply {
            show()
            startShimmer()
        }
    }
    private fun stopLoadingCategory(){
        binding.shimmerFrameLayoutCategory.apply {
            gone()
            stopShimmer()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        autoScrollManager?.stopAutoScroll()
        preferenceManager.removeSingleValueString("category")
        categoryAdapter?.clearSelectedPosition()
        categoryAdapter = null
    }

}