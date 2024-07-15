package com.findmykids.tracker.panda.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ItemGuardianBinding
import com.findmykids.tracker.panda.model.response.ConnectedGuardianResponse
import com.bumptech.glide.Glide

class GuardianAdapter(private var mContext: Context, private var onGuardianCLick: (ConnectedGuardianResponse.Data) -> Unit) :
    RecyclerView.Adapter<GuardianAdapter.GuardianHolder>() {

    private var guardianList: List<ConnectedGuardianResponse.Data> = ArrayList()

    class GuardianHolder(var binding: ItemGuardianBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianHolder {
        val binding: ItemGuardianBinding = ItemGuardianBinding.inflate(
            LayoutInflater.from(mContext),
            parent,
            false
        )
        return GuardianHolder(binding)
    }

    override fun getItemCount(): Int {
        return guardianList.size
    }

    override fun onBindViewHolder(holder: GuardianHolder, position: Int) {
        Glide.with(mContext)
            .load(guardianList[position].profileImage)
            .centerCrop()
            .placeholder(R.drawable.ic_placeholder).into(holder.binding.sivGuardianImage)

        holder.itemView.setOnClickListener {
            onGuardianCLick.invoke(guardianList[position])
        }
    }

    fun addList(list: List<ConnectedGuardianResponse.Data>) {
        guardianList = list
        notifyDataSetChanged()
    }
}