package com.example.flowmahuang.chocotvques.module.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.flowmahuang.chocotvques.R
import com.example.flowmahuang.chocotvques.module.UtcTimeFormater
import com.example.flowmahuang.chocotvques.module.network.apidrama.Datum
import com.example.flowmahuang.chocotvques.module.network.apidrama.Drama


/**
 * Created by flowmahuang on 2018/3/27.
 */
class DramaListRecyclerViewAdapter(val mContext: Context, val mCallback: DramaListRecyclerCallback)
    : RecyclerView.Adapter<DramaListRecyclerViewAdapter.DramaItemViewHolder>() {
    interface DramaListRecyclerCallback {
        fun onClick(data: Datum)

        fun onBindViewImageLoad(view: ImageView, cacheKey: String)
    }

    private var dramaInformation: Drama? = null
    private val inflater: LayoutInflater = LayoutInflater.from(mContext)


    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int {
        return if (dramaInformation != null) dramaInformation!!.data.size else 0
    }

    override fun getItemId(position: Int): Long {
        return dramaInformation!!.data[position].drama_id.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DramaItemViewHolder {
        return DramaItemViewHolder(inflater.inflate(
                R.layout.item_drama_information,
                parent,
                false))
    }

    override fun onBindViewHolder(holder: DramaItemViewHolder, position: Int) {
        val thisDrama = dramaInformation!!.data[position]
        val subUrl = thisDrama.thumb.replace("[^\\w]".toRegex(), "")
        val date = UtcTimeFormater.timeFormat(thisDrama.created_at)

        holder.mDramaNameTextView.text = thisDrama.name
        holder.mDramaRatingTextView.text = mContext.getString(R.string.item_drama_rating_title, thisDrama.rating.toString())
        holder.mDramaCreateAtTextView.text = mContext.getString(R.string.item_drama_create_at_title, date)

        holder.mDramaThumbImageView.tag = subUrl
        mCallback.onBindViewImageLoad(holder.mDramaThumbImageView, subUrl)
        holder.itemView.setOnClickListener({
            mCallback.onClick(thisDrama)
        })
    }

    fun setDramaInformation(information: Drama) {
        this.dramaInformation = information
        notifyDataSetChanged()
    }

    fun clearDramaInformation() {
        notifyItemRangeRemoved(0, dramaInformation!!.data.size)
        this.dramaInformation = null
    }

    class DramaItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var mDramaThumbImageView: ImageView = v.findViewById(R.id.item_drama_thumb)
        var mDramaNameTextView: TextView = v.findViewById(R.id.item_drama_name)
        var mDramaRatingTextView: TextView = v.findViewById(R.id.item_drama_rating)
        var mDramaCreateAtTextView: TextView = v.findViewById(R.id.item_drama_created_at)
    }
}