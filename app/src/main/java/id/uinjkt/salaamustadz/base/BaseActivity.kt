package id.uinjkt.salaamustadz.base

import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewbinding.ViewBinding
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.utils.PreferenceManager
import id.uinjkt.salaamustadz.utils.dialog.ProgressDialog

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    private var mBinding: T? = null
    protected val binding
        get() = mBinding!!
    private var loadingDialog: AlertDialog? = null
    private var errorDialog: AlertDialog? = null
    private var dialog: ProgressDialog? = null
    protected lateinit var sharedPref: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = getViewBinding()
        dialog = ProgressDialog(this)
        sharedPref = PreferenceManager(this)

        setContentView(mBinding?.root)
        setupIntent()
        setupProcess()
        setupUI()
        setupAction()
        setupObserver()
    }

    abstract fun getViewBinding(): T

    abstract fun setupIntent()

    abstract fun setupUI()

    abstract fun setupAction()

    abstract fun setupProcess()

    abstract fun setupObserver()

    protected fun setToolbar(toolbar: Toolbar){
        setSupportActionBar(toolbar)
    }

    protected fun showLoading(setCancelable: Boolean = false) {
        if (dialog == null) {
            dialog = ProgressDialog(this)
        }
        dialog?.startProgressDialog(setCancelable)
    }

    protected fun dismissLoading() {
        dialog?.dismissProgressDialog()
    }

    protected fun showErrorDialog(message: String, onRetry: () -> Unit) {
        errorDialog?.apply {
            this.setTitle(getString(R.string.label_error))
            setMessage(message)
            setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.action_retry)) { dialog, _ ->
                onRetry.invoke()
                dialog.dismiss()
            }
            show()
        }
    }

    protected fun setLoadingDialog(alertDialog: AlertDialog) {
        loadingDialog = alertDialog
    }

    protected fun setErrorDialog(alertDialog: AlertDialog) {
        errorDialog = alertDialog
    }

    protected fun navigateBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        dismissLoading()
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item clicks
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}