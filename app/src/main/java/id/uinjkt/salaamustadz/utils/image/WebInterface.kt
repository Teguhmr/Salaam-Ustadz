package id.uinjkt.salaamustadz.utils.image

import android.content.Context
import android.content.Intent
import android.webkit.JavascriptInterface
import android.widget.Toast
import id.uinjkt.salaamustadz.ui.image.ImagePreviewActivity
import id.uinjkt.salaamustadz.utils.Constants

class WebInterface(private val mContext: Context) {

    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun openDetailImage(pathImgUrl: String) {
        val intent = Intent(mContext, ImagePreviewActivity::class.java)
        intent.putExtra(Constants.KEY_IMAGE_MESSAGE, pathImgUrl)
        intent.putExtra(Constants.KEY_NAME_IMAGE_OWNER, "")
        mContext.startActivity(intent)
    }
}