package com.findmykids.tracker.panda.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.findmykids.tracker.panda.databinding.ItemApplicationInfoBinding
import com.findmykids.tracker.panda.roomDatabase.AppInfoFilter

class AppUsageAdapter(private var mContext: Context) :
    RecyclerView.Adapter<AppUsageAdapter.AppUsageHolder>() {

    private var appInfoList: MutableList<AppInfoFilter> = ArrayList()

    fun setAppInfoNewList(dataResponse: MutableList<AppInfoFilter>) {
        appInfoList.clear()
        appInfoList.addAll(dataResponse)
        notifyDataSetChanged()
//        dataResponse?.let {
//            val list = it as MutableList<AppInfoFilter>
//            val diffResult = DiffUtil.calculateDiff(AppInfoDiffCallback(appInfoList, list))
//            appInfoList = list
//            diffResult.dispatchUpdatesTo(this)
//        }
    }

    private class AppInfoDiffCallback(
        private val oldList: MutableList<AppInfoFilter>,
        private val newList: MutableList<AppInfoFilter>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//            if (oldList[oldItemPosition].appPkgName == newList[newItemPosition].appPkgName) {
//                return false
//            } else if (oldList[oldItemPosition].time == newList[newItemPosition].time) {
//                return false
//            }
            return true

        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//            if (oldList[oldItemPosition].appPkgName == newList[newItemPosition].appPkgName) {
//                return false
//            } else if (oldList[oldItemPosition].time == newList[newItemPosition].time) {
//                return false
//            }
            return true
        }
    }

    class AppUsageHolder(var binding: ItemApplicationInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppUsageHolder {
        val binding: ItemApplicationInfoBinding = ItemApplicationInfoBinding.inflate(
            LayoutInflater.from(mContext),
            parent,
            false
        )
        return AppUsageHolder(binding)
    }

    override fun getItemCount(): Int {
        return appInfoList.size
    }

    override fun onBindViewHolder(holder: AppUsageHolder, position: Int) {
        holder.binding.ivGuardianImage.setImageDrawable(appInfoList[position].appIcon)
        holder.binding.tvAppName.text = appInfoList[position].appName
        holder.binding.tvAppTime.text = getTime(appInfoList[position].today)
    }

    private fun getTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return if ((minutes % 60).toInt() == 0 && (hours % 24).toInt() == 0) {
            "No Use"
        } else if ((hours % 24).toInt() == 0) {
            String.format("%02d", minutes % 60) + " min"
        } else {
            String.format("%02d", hours % 24) + ":" + String.format(
                "%02d",
                minutes % 60
            ) + " hr"
        }
    }
}