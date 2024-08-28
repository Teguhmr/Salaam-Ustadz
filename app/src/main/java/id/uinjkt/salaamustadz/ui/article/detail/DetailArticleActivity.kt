package id.uinjkt.salaamustadz.ui.article.detail

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.article.Article
import id.uinjkt.salaamustadz.databinding.ActivityDetailArticleBinding
import id.uinjkt.salaamustadz.ui.article.ArticleViewModel
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.dialog.ProgressDialog
import id.uinjkt.salaamustadz.utils.firebase.DatabaseUtil
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import id.uinjkt.salaamustadz.utils.image.WebInterface
import id.uinjkt.salaamustadz.utils.parcelable
import id.uinjkt.salaamustadz.utils.toast
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailArticleActivity : AppCompatActivity() {
    private var _binding: ActivityDetailArticleBinding? = null
    private val binding get() = _binding as ActivityDetailArticleBinding
    private var article: Article? = null
    private var drawable: Drawable? = null
    private lateinit var articleViewModel: ArticleViewModel
    private lateinit var progressDialog: ProgressDialog
    private var userId: String? = null
    private var favoriteMenuItem: MenuItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDetailArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra(Constants.KEY_USER_ID)
        progressDialog = ProgressDialog(this)
        articleViewModel = ViewModelProvider(this)[ArticleViewModel::class.java]
        article = intent.parcelable(Constants.KEY_ARTICLE)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""

        setUpUI(article!!)

    }
    @SuppressLint("SetTextI18n", "SetJavaScriptEnabled")
    private fun setUpUI(article: Article) {
        DatabaseUtil.incrementNumberOfViews(article.articleId!!)
        Glide.with(this)
            .load(StorageUtil.pathToReference(article.imageUrl))
            .into(binding.imgBackgroundArticle)

        val originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateUpdated: Date = originalFormat.parse(article.dateUpdated)!!

        val defaultFormattedFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

        val defaultDateUpdate = defaultFormattedFormat.format(dateUpdated)

        binding.txtTitle.text = article.title
        if (article.userRole.equals("ustadz") or (article.userRole.equals("ustadzah"))) {
            binding.txtUserName.text = "oleh Ust. ${article.userName}"
        }else if (article.userRole.equals("admin")){
            binding.txtUserName.text = "oleh Redaksi SalaamUstadz"
        }else {
            binding.txtUserName.text = "oleh ${article.userName}"
        }
        binding.txtDateUpdated.text = defaultDateUpdate

        setChipsFromArrayList(article.keyword)

        val head1 = "<head><style>@font-face {font-family: 'arial';src: url('file:///android_asset/fonts/alif_quran.ttf');}body {font-family: 'verdana';font-size: 16px}</style></head>" +
                "<script type='text/javascript' src='file:///android_asset/script.js'></script>" +
                "</head>";
        val text="<html>"+head1 + "<body style='font-family:arial'>" + article.textArticle + "</body></html>"
        binding.txtArticle.settings.javaScriptEnabled = true
        binding.txtArticle.addJavascriptInterface(WebInterface(this), "Android")
        binding.txtArticle.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null)

        val upArrow = ResourcesCompat.getDrawable(resources, R.drawable.arrow_back, null)
        upArrow?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP)
        supportActionBar!!.setHomeAsUpIndicator(upArrow)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        var isShow: Boolean? = false
        var scrollRange = -1
        // Set the tint color for the icon
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, offset ->
            if (scrollRange == -1) {
                scrollRange = appBarLayout.totalScrollRange
            }
            if (scrollRange + offset == 0) {
                //collapse map
                isShow = true
                drawable?.setTint(ContextCompat.getColor(this, R.color.dark_blue_primary))
                upArrow?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.dark_blue_primary), PorterDuff.Mode.SRC_ATOP)
                if (article.favorite) {
                    favoriteMenuItem?.icon?.setTint(ContextCompat.getColor(this, R.color.schatscreen_color_offline))
                } else {
                    favoriteMenuItem?.icon?.setTint(ContextCompat.getColor(this, R.color.dark_blue_primary))
                }

            } else if (isShow == true) {
                //expanded map
                isShow = false
                drawable?.setTint(ContextCompat.getColor(this, R.color.white))
                upArrow?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP)
                if (article.favorite) {
                    favoriteMenuItem?.icon?.setTint(ContextCompat.getColor(this, R.color.schatscreen_color_offline))
                } else {
                    favoriteMenuItem?.icon?.setTint(ContextCompat.getColor(this, R.color.white))
                }
            }

        }
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        invalidateOptionsMenu()
        return super.onMenuOpened(featureId, menu)
    }

    private fun setChipsFromArrayList(stringList:List<String>) {
        for (text in stringList) {
            val shapeAppearanceModel = ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, 50f)
                .build()
            val chip = Chip(this)
            chip.text = text
            chip.setTextColor(ContextCompat.getColor(this, R.color.gray))
            chip.textSize = 13f
            chip.shapeAppearanceModel = shapeAppearanceModel
            chip.setChipBackgroundColorResource(R.color.gray_chip)
            chip.chipStrokeWidth = 0f
            chip.isCloseIconVisible = false // Set to true if you want to show close icon
            chip.isClickable = false
            // Set any other properties for the chip as needed (e.g., chip background, text color, etc.)

            // Add the chip to the ChipGroup
            binding.chipGroup.addView(chip)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_article_menu, menu)
        drawable = menu?.findItem(R.id.share_article)?.icon

        favoriteMenuItem = menu?.findItem(R.id.bookmark_article)
        updateToolbarFavorites()

        if(drawable != null) {
            drawable!!.mutate()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item clicks
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.bookmark_article -> {
                Timber.tag("userid").d(article.toString())
                DatabaseUtil.updateFavoriteStatus(userId = userId.toString(), article = article!!){ newFavorite ->
                    updateToolbarFavoriteIcon(newFavorite)
                }
                return true
            }
            R.id.share_article -> {
                toast("Fitur sedang dikembangkan")
                return true
            }

            // Add more menu items handling here if needed
        }
        return super.onOptionsItemSelected(item)
    }
    private fun updateToolbarFavorites() {
        DatabaseUtil.getFavoriteStatus(userId.toString()){ userFavorites->
            val isFavorite = userFavorites[article?.articleId] ?: false
            article?.favorite = isFavorite
            updateToolbarFavoriteIcon(isFavorite)
        }
    }

    private fun updateToolbarFavoriteIcon(isFavorite: Boolean) {
            if (isFavorite) {
                favoriteMenuItem?.icon = ContextCompat.getDrawable(this, R.drawable.bookmark_filled)
                favoriteMenuItem?.icon?.setTint(ContextCompat.getColor(this, R.color.schatscreen_color_offline))
            }
            else {
                favoriteMenuItem?.icon = ContextCompat.getDrawable(this, R.drawable.bookmark_outline)
            }

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}