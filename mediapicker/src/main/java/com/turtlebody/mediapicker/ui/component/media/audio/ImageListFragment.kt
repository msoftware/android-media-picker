package com.turtlebody.mediapicker.ui.component.media.audio

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.turtlebody.mediapicker.ui.ActivityLibMain
import com.turtlebody.mediapicker.ui.component.media.audio.adapter.ImageAdapter
import com.turtlebody.mediapicker.core.FileManager
import com.turtlebody.mediapicker.ui.common.MediaListFragment
import com.turtlebody.mediapicker.ui.component.models.Image
import com.turtlebody.mediapicker.ui.component.models.ImageVideoFolder
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.tb_media_picker_frame_progress.*
import kotlinx.android.synthetic.main.tb_media_picker_image_fragment.*
import org.jetbrains.anko.info
import java.io.File

/**
 * Created by niraj on 12-04-2019.
 */
class ImageListFragment : MediaListFragment(), ImageAdapter.OnImageClickListener {


    companion object {

        @JvmStatic
        fun newInstance(key: Int, b: Bundle?): Fragment {
            val bf: Bundle = b ?: Bundle()
            bf.putInt("fragment.key", key);
            val fragment = ImageListFragment()
            fragment.arguments = bf
            return fragment
        }

    }

    private var mFolderId: String = ""
    private var mUriList: MutableList<Uri> = arrayListOf()

    private var mImageAdapter: ImageAdapter = ImageAdapter()
    private var mImageList: MutableList<Image> = arrayListOf()
    private var mSelectedImageList: MutableList<Image> = arrayListOf()




    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initAdapter()
    }

    override fun onRestoreState(savedInstanceState: Bundle?, args: Bundle?) {
        arguments?.let {
            mFolderId= it.getString(ImageVideoFolder.FOLDER_ID,"")
            info { "fileId: $mFolderId" }
        }
    }


    override fun getAllUris() {
        if(mSelectedImageList.isNotEmpty()){
            for (i in mSelectedImageList){
                info { "audio path: ${i.filePath}" }
                mUriList.add(FileManager.getContentUri(context!!, File(i.filePath)))
            }
            (activity as ActivityLibMain).sendBackData(mUriList)
        }
    }


    override fun onImageCheck(pData: Image) {
        if(!mPickerConfig.mAllowMultiImages){
            if(mPickerConfig.mShowDialog){
                val simpleAlert = AlertDialog.Builder(context!!)
                simpleAlert.setMessage("Are you sure to select ${pData.name}")
                        .setCancelable(false)
                        .setPositiveButton("OK") { dialog, which ->
                            (activity as ActivityLibMain).sendBackData(arrayListOf(FileManager.getContentUri(context!!, File(pData.filePath))))
                        }
                        .setNegativeButton("Cancel") { dialog, which -> dialog.dismiss()  }
                simpleAlert.show()
            }
            else{
                (activity as ActivityLibMain).sendBackData(arrayListOf(FileManager.getContentUri(context!!, File(pData.filePath))))
            }
        }
        else{
            val selectedIndex = mImageList.indexOf(pData)

            if(selectedIndex >= 0){
                //toggle
                mImageList[selectedIndex].isSelected = !(mImageList[selectedIndex].isSelected)
                //update ui
                mImageAdapter.updateIsSelected(mImageList[selectedIndex])

                //update selectedList
                if(mImageList[selectedIndex].isSelected){
                    mSelectedImageList.add(mImageList[selectedIndex])
                }
                else{
                    mSelectedImageList.removeAt(mSelectedImageList.indexOf(pData))
                }
            }
            (activity as ActivityLibMain).updateCounter(mSelectedImageList.size)
            btn_add_file.isEnabled = mSelectedImageList.size>0
        }
    }


    private fun initAdapter() {
        mImageAdapter.setListener(this)
        mImageAdapter.mShowCheckBox = mPickerConfig.mAllowMultiImages
        recycler_view.layoutManager = GridLayoutManager(context,2)
        recycler_view.adapter = mImageAdapter
        fetchImageFiles()

    }

    private fun fetchImageFiles() {
        val fileItems = Single.fromCallable<Boolean> {
            mImageList.clear()
            val tempArray = FileManager.getImageFilesInFolder(context!!, mFolderId)

            //include only valid files
            for(i in tempArray){
                if(File(i.filePath).length()>0){
                    mImageList.add(i)
                }
            }
            true
        }

        fileItems.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Boolean> {
                    override fun onSubscribe(@NonNull d: Disposable) {
                        progress_view.visibility = View.VISIBLE
                    }

                    override fun onSuccess(t: Boolean) {
                        mImageAdapter.setData(mImageList)
                        progress_view.visibility = View.GONE
                    }

                    override fun onError(@NonNull e: Throwable) {
                        progress_view.visibility = View.GONE
                        info { "error: ${e.message}" }
                    }
                })
    }

}