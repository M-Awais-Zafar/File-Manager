package com.android.filemanager.fragments

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.VIEW_TYPE_LIST
import org.fossify.commons.models.FileDirItem
import org.fossify.commons.views.MyFloatingActionButton
import com.android.filemanager.R
import com.android.filemanager.activities.MainActivity
import com.android.filemanager.activities.SimpleActivity
import com.android.filemanager.databinding.ItemsFragmentBinding
import com.android.filemanager.databinding.RecentsFragmentBinding
import com.android.filemanager.databinding.StorageFragmentBinding
import com.android.filemanager.extensions.isPathOnRoot
import com.android.filemanager.extensions.tryOpenPathIntent
import com.android.filemanager.helpers.RootHelpers

abstract class MyViewPagerFragment<BINDING : MyViewPagerFragment.InnerBinding>(context: Context, attributeSet: AttributeSet) :
    RelativeLayout(context, attributeSet) {
    protected var activity: SimpleActivity? = null
    protected var currentViewType = VIEW_TYPE_LIST

    var currentPath = ""
    var isGetContentIntent = false
    var isGetRingtonePicker = false
    var isPickMultipleIntent = false
    var wantedMimeTypes = listOf("")
    protected var isCreateDocumentIntent = false
    protected lateinit var innerBinding: BINDING

    protected fun clickedPath(path: String) {
        if (isGetContentIntent || isCreateDocumentIntent) {
            (activity as MainActivity).pickedPath(path)
        } else if (isGetRingtonePicker) {
            if (path.isAudioFast()) {
                (activity as MainActivity).pickedRingtone(path)
            } else {
                activity?.toast(R.string.select_audio_file)
            }
        } else {
            activity?.tryOpenPathIntent(path, false)
        }
    }

    fun updateIsCreateDocumentIntent(isCreateDocumentIntent: Boolean) {
        val iconId = if (isCreateDocumentIntent) {
            R.drawable.ic_check_vector
        } else {
            R.drawable.ic_plus_vector
        }

        this.isCreateDocumentIntent = isCreateDocumentIntent
        val fabIcon = context.resources.getColoredDrawableWithColor(iconId, context.getProperPrimaryColor().getContrastColor())
        innerBinding.itemsFab?.setImageDrawable(fabIcon)
    }

    fun handleFileDeleting(files: ArrayList<FileDirItem>, hasFolder: Boolean) {
        val firstPath = files.firstOrNull()?.path
        if (firstPath == null || firstPath.isEmpty() || context == null) {
            return
        }

        if (context!!.isPathOnRoot(firstPath)) {
            RootHelpers(activity!!).deleteFiles(files)
        } else {
            (activity as SimpleActivity).deleteFiles(files, hasFolder) {
                if (!it) {
                    activity!!.runOnUiThread {
                        activity!!.toast(R.string.unknown_error_occurred)
                    }
                }
            }
        }
    }

    protected fun isProperMimeType(wantedMimeType: String, path: String, isDirectory: Boolean): Boolean {
        return if (wantedMimeType.isEmpty() || wantedMimeType == "*/*" || isDirectory) {
            true
        } else {
            val fileMimeType = path.getMimeType()
            if (wantedMimeType.endsWith("/*")) {
                fileMimeType.substringBefore("/").equals(wantedMimeType.substringBefore("/"), true)
            } else {
                fileMimeType.equals(wantedMimeType, true)
            }
        }
    }

    abstract fun setupFragment(activity: SimpleActivity)

    abstract fun onResume(textColor: Int)

    abstract fun refreshFragment()

    abstract fun searchQueryChanged(text: String)

    interface InnerBinding {
        val itemsFab: MyFloatingActionButton?
    }

    class ItemsInnerBinding(val binding: ItemsFragmentBinding) : InnerBinding {
        override val itemsFab: MyFloatingActionButton = binding.itemsFab
    }

    class RecentsInnerBinding(val binding: RecentsFragmentBinding) : InnerBinding {
        override val itemsFab: MyFloatingActionButton? = null
    }

    class StorageInnerBinding(val binding: StorageFragmentBinding) : InnerBinding {
        override val itemsFab: MyFloatingActionButton? = null
    }
}
