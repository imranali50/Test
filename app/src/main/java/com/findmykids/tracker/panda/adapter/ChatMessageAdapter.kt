package com.findmykids.tracker.panda.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.findmykids.tracker.panda.R
import com.findmykids.tracker.panda.databinding.ChatItemBinding
import com.findmykids.tracker.panda.model.response.ChatResponse
import com.findmykids.tracker.panda.util.Const
import com.findmykids.tracker.panda.util.PreferenceManager
import com.findmykids.tracker.panda.util.UTCToLocal
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class ChatMessageAdapter(
    private var mContext: Context,
    private var chatList: MutableList<ChatResponse.Messages>
) :
    RecyclerView.Adapter<ChatMessageAdapter.ChatMessageHolder>() {

    lateinit var pref: PreferenceManager

    class ChatMessageHolder(var binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageHolder {
        val binding: ChatItemBinding =
            ChatItemBinding.inflate(LayoutInflater.from(mContext), parent, false)
        pref = PreferenceManager(mContext)
        return ChatMessageHolder(binding)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ChatMessageHolder, position: Int) {
        if (chatList[position].senderId == pref.getString(Const.id)) {
            holder.binding.rlUser.visibility = View.GONE
            holder.binding.rlMe.visibility = View.VISIBLE

            if (chatList[position].text.isNotEmpty()) {
                holder.binding.tvMyMessage.visibility = View.VISIBLE
                holder.binding.sivMyImage.visibility = View.GONE
                holder.binding.tvMyMessage.text = chatList[position].text
            } else if (chatList[position].image.isNotEmpty()) {
                holder.binding.tvMyMessage.visibility = View.GONE
                holder.binding.sivMyImage.visibility = View.VISIBLE
                Glide.with(mContext).load(chatList[position].image).skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().placeholder(R.drawable.ic_placeholder)
                    .into(holder.binding.sivMyImage)
            }
            holder.binding.tvMyMessageTime.text = UTCToLocal(chatList[position].createdAt)
        } else {
            holder.binding.rlUser.visibility = View.VISIBLE
            holder.binding.rlMe.visibility = View.GONE

            if (chatList[position].text.isNotEmpty()) {
                holder.binding.tvUserMessage.visibility = View.VISIBLE
                holder.binding.sivUserImage.visibility = View.GONE
                holder.binding.tvUserMessage.text = chatList[position].text
            } else if (chatList[position].image.isNotEmpty()) {
                holder.binding.tvUserMessage.visibility = View.GONE
                holder.binding.sivUserImage.visibility = View.VISIBLE
                Glide.with(mContext).load(chatList[position].image).skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().placeholder(R.drawable.ic_placeholder)
                    .into(holder.binding.sivUserImage)
            }
            holder.binding.tvUserTime.text = UTCToLocal(chatList[position].createdAt)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyWithList(list: MutableList<ChatResponse.Messages>) {
        chatList.addAll(0, list)
        notifyDataSetChanged()

    }
    @SuppressLint("NotifyDataSetChanged")
    fun notifyWithListONLY(list: MutableList<ChatResponse.Messages>) {
        chatList.addAll(0, list)
//        notifyDataSetChanged()

    }

    fun notifyItem(data: ChatResponse.Messages) {
        val last = chatList.size
        chatList.add(data)
        notifyItemInserted(last)
        notifyItemChanged(last - 1)
    }
}