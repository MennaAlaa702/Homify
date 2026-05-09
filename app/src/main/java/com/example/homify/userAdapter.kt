package com.example.homify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


/**
 * RecyclerView Adapter for displaying users in ManageUsersActivity.
 * Supports delete action per item.
 */
class userAdapter(
    private val users: MutableList<users>,
    private val onDeleteClick: (users, Int) -> Unit
) : RecyclerView.Adapter<userAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        val tvName: TextView = itemView.findViewById(R.id.tv_user_name)
        val tvEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_user)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        holder.ivAvatar.setImageResource(R.drawable.ic_person)
        holder.tvName.text = user.name
        holder.tvEmail.text = user.email

        // Delete button triggers callback to activity for dialog confirmation
        holder.btnDelete.setOnClickListener {
            onDeleteClick(user, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = users.size

    /**
     * Remove a user from the list and notify the adapter.
     */
    fun removeUser(position: Int) {
        if (position >= 0 && position < users.size) {
            users.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, users.size)
        }
    }

    /**
     * Filter users by name and refresh the list.
     */
    fun filter(query: String, allUsers: List<users>) {
        users.clear()
        if (query.isEmpty()) {
            users.addAll(allUsers)
        } else {
            users.addAll(allUsers.filter {
                it.name.contains(query, ignoreCase = true)
            })
        }
        notifyDataSetChanged()
    }
}
