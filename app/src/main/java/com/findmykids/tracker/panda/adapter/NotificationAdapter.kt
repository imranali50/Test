package com.findmykids.tracker.panda.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.activity.ChatActivity
import com.findmykids.tracker.panda.databinding.ItemNotificationInfoBinding
import com.findmykids.tracker.panda.model.response.NotificationResponse
class NotificationAdapter(var mContext: Context, var list: ArrayList<NotificationResponse.Data.Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationHolder>() {
    class NotificationHolder(var binding: ItemNotificationInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        val binding: ItemNotificationInfoBinding = ItemNotificationInfoBinding.inflate(
            LayoutInflater.from(mContext),
            parent,
            false
        )

        return NotificationHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        val builder = SpannableStringBuilder()
        for (i in list[position].content) {
            val str1 = SpannableString(i.text)
            val typeface: Typeface
            if (i.weight == "regular") {
                typeface =    ResourcesCompat.getFont(mContext, R.font.roboto_regular)!!
            } else if(i.weight == "bold"){
                typeface =  ResourcesCompat.getFont(mContext, R.font.roboto_bold)!!
            } else {
                typeface =  ResourcesCompat.getFont(mContext, R.font.roboto_medium)!!
            }

            str1.setSpan(
                StyleSpan(typeface.style),
                0,
                str1.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            str1.setSpan(ForegroundColorSpan(Color.parseColor(i.color)), 0, str1.length, 0)
            builder.append(str1)
            builder.append(" ")
        }

        holder.binding.tvTitle.text = builder
        holder.binding.tvTimeAgo.text = list[position].timeSpan
        holder.itemView.setOnClickListener {
            if (list[position].type == "MESSAGES") {

                mContext.startActivity(
                    Intent(mContext, ChatActivity::class.java).putExtra(
                        ChatActivity.ConnectionId, list[position].connectionId
                    )
                        .putExtra(ChatActivity.ChatGroupIcon, list[position].senderProfile)
                        .putExtra(
                            ChatActivity.ChatGroupName, list[position].senderName

                        )
                )
            }
        }
    }
}