package com.example.clouddart

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val isTyping: Boolean = false,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val username: String = "justinedeluana"
) {
    fun getFormattedTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    sealed class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        class UserMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
            val messageText: TextView = itemView.findViewById(R.id.messageText)
            val timeText: TextView = itemView.findViewById(R.id.timeText)
            val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)
        }

        class BotMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
            val messageText: TextView = itemView.findViewById(R.id.messageText)
            val timeText: TextView = itemView.findViewById(R.id.timeText)
            val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)
            val typingIndicator: View? = itemView.findViewById(R.id.typingIndicator)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = inflater.inflate(R.layout.item_user_message, parent, false)
                MessageViewHolder.UserMessageViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_bot_message, parent, false)
                MessageViewHolder.BotMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is MessageViewHolder.UserMessageViewHolder -> {
                bindUserMessage(holder, message)
            }
            is MessageViewHolder.BotMessageViewHolder -> {
                bindBotMessage(holder, message)
            }
        }
    }

    private fun bindUserMessage(holder: MessageViewHolder.UserMessageViewHolder, message: ChatMessage) {
        holder.apply {
            messageText.text = message.content
            timeText.text = message.getFormattedTime()
            messageContainer.gravity = Gravity.END

            // Set background and text colors for user messages
            messageText.setBackgroundResource(R.drawable.user_message_background)
            messageText.setTextColor(android.graphics.Color.WHITE)
        }
    }

    private fun bindBotMessage(holder: MessageViewHolder.BotMessageViewHolder, message: ChatMessage) {
        holder.apply {
            messageContainer.gravity = Gravity.START

            when {
                message.isTyping -> {
                    messageText.visibility = View.GONE
                    timeText.visibility = View.GONE
                    typingIndicator?.visibility = View.VISIBLE
                }
                message.isError -> {
                    messageText.visibility = View.VISIBLE
                    timeText.visibility = View.VISIBLE
                    typingIndicator?.visibility = View.GONE

                    messageText.text = message.content
                    timeText.text = message.getFormattedTime()
                    messageText.setBackgroundResource(R.drawable.error_message_background)
                    messageText.setTextColor(android.graphics.Color.WHITE)
                }
                else -> {
                    messageText.visibility = View.VISIBLE
                    timeText.visibility = View.VISIBLE
                    typingIndicator?.visibility = View.GONE

                    messageText.text = message.content
                    timeText.text = message.getFormattedTime()
                    messageText.setBackgroundResource(R.drawable.bot_message_background)
                    messageText.setTextColor(android.graphics.Color.BLACK)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    override fun getItemCount() = messages.size

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 0
    }
}