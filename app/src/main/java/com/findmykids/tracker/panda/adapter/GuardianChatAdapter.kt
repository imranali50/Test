package com.findmykids.tracker.panda.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.activity.ChatActivity
import com.findmykids.tracker.panda.databinding.ItemChatFragmentBinding
import com.findmykids.tracker.panda.model.response.AllChatResponse
import com.findmykids.tracker.panda.util.UTCToLocal
import com.bumptech.glide.Glide
import com.google.gson.Gson

class GuardianChatAdapter(
    private var mContext: Context,
    private var listChatDetails: List<AllChatResponse.Data>,
    private var function: (Intent) -> Unit
) : RecyclerView.Adapter<GuardianChatAdapter.GuardianChatHolder>() {
    class GuardianChatHolder(var binding: ItemChatFragmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianChatHolder {
        val binding: ItemChatFragmentBinding = ItemChatFragmentBinding.inflate(
            LayoutInflater.from(mContext), parent, false
        )
        return GuardianChatHolder(binding)
    }

    override fun getItemCount(): Int {
        return listChatDetails.size
    }

    override fun onBindViewHolder(holder: GuardianChatHolder, position: Int) {
        Glide.with(mContext).load(listChatDetails[position].profileImage).centerCrop()
            .placeholder(R.drawable.ic_placeholder).into(holder.binding.ivGuardianImage)
        holder.binding.tvChatGroupName.text = listChatDetails[position].name
        if (!listChatDetails[position].lastMessage.isJsonNull) {
            val todayActivityData = Gson().fromJson(
                listChatDetails[position].lastMessage,
                AllChatResponse.Data.LastMessage::class.java
            )
            if (todayActivityData.text != null && todayActivityData.text.isNotEmpty()) {
                holder.binding.tvChatLastMessage.text = todayActivityData.text
            } else if (todayActivityData.image != null && todayActivityData.image.isNotEmpty()) {
                holder.binding.tvChatLastMessage.text = "Image"
            }
            if (todayActivityData.createdAt != null && todayActivityData.createdAt.isNotEmpty())
                holder.binding.tvLastMessageTime.text =
                    UTCToLocal(todayActivityData.createdAt)
        }
        if (listChatDetails[position].unreadCount.toInt() == 0) {
            holder.binding.tvUnReadMessageCount.visibility = View.INVISIBLE
        } else {
            holder.binding.tvUnReadMessageCount.visibility = View.VISIBLE
            holder.binding.tvUnReadMessageCount.text = listChatDetails[position].unreadCount
        }

        holder.itemView.setOnClickListener {
            var intent = Intent(mContext, ChatActivity::class.java).putExtra(
                ChatActivity.ConnectionId, listChatDetails[position]._id
            ).putExtra(
                ChatActivity.ChatGroupIcon, listChatDetails[position].profileImage
            ).putExtra(
                ChatActivity.ChatGroupName, listChatDetails[position].name
            )
            function.invoke(intent)
        }
    }

    fun addList(allChatDetails: List<AllChatResponse.Data>) {
        listChatDetails = allChatDetails
        notifyDataSetChanged()
    }
}