package com.example.ezplace.activities

import android.app.Dialog
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.ezplace.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.dialog_progress.*

open class BaseActivity : AppCompatActivity() {

    /**
     * This is a progress dialog instance which we will initialize later on.
     */
    private lateinit var mProgressDialog: Dialog

    fun fullScreenMode(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }
        else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    fun customFont(textView : TextView){
        val typeface: Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
        textView.typeface = typeface
    }

    fun setupActionBar(toolbar: androidx.appcompat.widget.Toolbar) {
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this@BaseActivity, R.color.snackbar_error_color))
        snackBar.show()
    }

    fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this)

        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        mProgressDialog.setContentView(R.layout.dialog_progress)
        mProgressDialog.setCancelable(false)
        mProgressDialog.tv_progress_text.text = text

        //Start the dialog and display it on screen.
        mProgressDialog.show()
    }

    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }
}