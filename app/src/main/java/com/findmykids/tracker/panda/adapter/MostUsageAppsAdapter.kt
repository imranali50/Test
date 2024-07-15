package com.findmykids.tracker.panda.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.findmykids.tracker.panda.databinding.MostUseItemBinding
import com.findmykids.tracker.panda.roomDatabase.AppInfoFilter

class MostUsageAppsAdapter(var mContext: Context, var listOfApps: ArrayList<AppInfoFilter>) :
    RecyclerView.Adapter<MostUsageAppsAdapter.MostUsageAppHolder>() {
    class MostUsageAppHolder(var binding: MostUseItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MostUsageAppHolder {
        val binding: MostUseItemBinding = MostUseItemBinding.inflate(
            LayoutInflater.from(mContext),
            parent,
            false
        )
        return MostUsageAppHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (listOfApps.size >= 4) 4 else listOfApps.size
    }

    override fun onBindViewHolder(holder: MostUsageAppHolder, position: Int) {
        holder.binding.ivUseAppIcon.setImageDrawable(listOfApps[position].appIcon)
        holder.binding.appName.text = listOfApps[position].appName
    }

    fun setAppInfoNewList(appList: Any?) {
        listOfApps = appList as ArrayList<AppInfoFilter>
        notifyDataSetChanged()
    }


}