package com.turtlebody.mediapicker.ui.component.media.audio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.turtlebody.mediapicker.R
import com.turtlebody.mediapicker.ui.component.models.Image
import kotlinx.android.synthetic.main.tb_media_picker_item_image.view.*
import java.io.File

/**
 * Created by WANGSUN on 26-Mar-19.
 */
class ImageAdapter: RecyclerView.Adapter<ImageAdapter.ImageVewHolder>() {
    private var mData: MutableList<Image> = arrayListOf()
    private var mOnImageClickListener: OnImageClickListener? = null
    var mShowCheckBox: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tb_media_picker_item_image, parent, false)
        return ImageVewHolder(view)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: ImageVewHolder, position: Int) {
        holder.bind(mData[position])
    }


    fun setListener(listener : OnImageClickListener){
        mOnImageClickListener = listener
    }

    fun setData(pData: MutableList<Image>){
        mData = pData
        notifyDataSetChanged()
    }

    fun updateIsSelected(pData: Image){
        val pos = mData.indexOf(pData)
        if(pos>=0){
            mData[pos] = pData
            notifyItemChanged(pos)
        }
    }

    inner class ImageVewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(pData: Image){

            Glide.with(itemView)
                    .load(File(pData.thumbnailPath))
                    .into(itemView.iv_image)

            itemView.cb_btn_selection.isChecked = pData.isSelected

            itemView.setOnClickListener {
                mOnImageClickListener?.onImageCheck(pData)
            }

            if(!mShowCheckBox){
                itemView.cb_btn_selection.visibility = View.GONE
            }
            else{
                itemView.cb_btn_selection.visibility = View.VISIBLE
            }
        }
    }


    interface OnImageClickListener {
        fun onImageCheck(pData: Image)
    }
}