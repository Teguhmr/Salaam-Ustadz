package id.uinjkt.salaamustadz.ui.image

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.databinding.ActivityImagePreviewBinding
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.downloader.AndroidDownloader
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import timber.log.Timber

class ImagePreviewActivity : AppCompatActivity() {
    private var _binding: ActivityImagePreviewBinding? = null
    private val binding get() = _binding as ActivityImagePreviewBinding
    private var imagePath: String? = null
    private lateinit var downloader: AndroidDownloader
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val nameOwnerImage = intent.getStringExtra(Constants.KEY_NAME_IMAGE_OWNER)
        binding.chatscreenTvName.text = nameOwnerImage
        imagePath = intent.getStringExtra(Constants.KEY_IMAGE_MESSAGE)
        downloader = AndroidDownloader(this)

        if (imagePath?.contains("https://") == true) {
            Glide.with(this)
                .load(imagePath)
                .placeholder(R.drawable.ic_image_black_24dp)
                .into(binding.imagePreviewChat)
        } else {
            Glide.with(this)
                .load(StorageUtil.pathToReference(imagePath.toString()))
                .placeholder(R.drawable.ic_image_black_24dp)
                .into(binding.imagePreviewChat)
        }

        binding.chatscreenIvBack.setOnClickListener {
            finish()
        }

        if (nameOwnerImage == "Anda"){
            binding.chatscreenIvVerticaldots.visibility = View.GONE
        } else {
            binding.chatscreenIvVerticaldots.visibility = View.VISIBLE
            binding.chatscreenIvVerticaldots.setOnClickListener {v ->
                showMenu(v, R.menu.download_menu_pop_up)
            }
        }
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            // Respond to menu item click.
            when (menuItem.itemId) {
                R.id.option_download -> {
                    StorageUtil.downloadMessageImage(imagePath.toString()){
                        downloader.downloadImage(it.toString())
                        Timber.tag("downloadImage").d(it.toString())
                    }
                }

                else -> {}
            }

            true
        }
        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.
        popup.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}