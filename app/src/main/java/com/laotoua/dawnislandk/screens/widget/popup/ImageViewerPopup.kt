package com.laotoua.dawnislandk.screens.widget.popup

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.laotoua.dawnislandk.R
import com.laotoua.dawnislandk.io.ImageUtil
import com.laotoua.dawnislandk.util.lazyOnMainOnly
import com.lxj.xpopup.core.ImageViewerPopupView
import com.lxj.xpopup.photoview.PhotoView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException


@SuppressLint("ViewConstructor")
class ImageViewerPopup(
    private val caller: Fragment,
    context: Context,
    private val imgUrl: String
) :
    ImageViewerPopupView(context) {

    private val _status = MutableLiveData<Boolean>()
    private val status: LiveData<Boolean> get() = _status
    private var saveShown = true
    private val saveButton by lazyOnMainOnly { findViewById<FloatingActionButton>(R.id.save) }

    override fun getImplLayoutId(): Int {
        return R.layout.popup_image_viewer
    }

    override fun initPopupContent() {
        super.initPopupContent()
        pager.adapter = PopupPVA()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.isShowSaveBtn = false
        saveButton.setOnClickListener {
            addPicToGallery(context, imgUrl)
        }

        status.observe(caller, Observer {
            when (it) {
                true -> Toast.makeText(context, R.string.image_saved, Toast.LENGTH_SHORT)
                    .show()
                else -> Toast.makeText(context, R.string.error_in_saving_image, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun addPicToGallery(context: Context, imgUrl: String) {
        caller.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                Timber.i("Saving image to Gallery... ")
                val relativeLocation =
                    Environment.DIRECTORY_PICTURES + File.separator + "Dawn"
                val name =
                    imgUrl.substring(imgUrl.lastIndexOf("/") + 1, imgUrl.lastIndexOf("."))
                val ext = imgUrl.substring(imgUrl.lastIndexOf(".") + 1)
                try {
                    ImageUtil.addPlaceholderImageUriToGallery(
                        caller.requireActivity(),
                        name,
                        ext,
                        relativeLocation
                    )
                        ?.run {
                            val stream =
                                caller.requireActivity().contentResolver.openOutputStream(this)
                                    ?: throw IOException("Failed to get output stream.")
                            val file = imageLoader.getImageFile(context, imgUrl)
                            stream.write(file.readBytes())
                            stream.close()
                            _status.postValue(true)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "failed to save img from $imgUrl")
                    _status.postValue(false)
                }
            }
        }
    }

    inner class PopupPVA : PhotoViewAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val photoView =
                PhotoView(container.context)
            if (imageLoader != null) imageLoader.loadImage(
                position,
                urls[if (isInfinite) position % urls.size else position],
                photoView
            )
            container.addView(photoView)
            photoView.setOnClickListener {
                saveShown = if (saveShown) {
                    saveButton.hide()
                    saveButton.isClickable = false
                    false
                } else {
                    saveButton.isClickable = true
                    saveButton.show()
                    true
                }

            }
            return photoView
        }
    }
}