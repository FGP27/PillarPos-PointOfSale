package com.pilarkreasi.pillarpos.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pilarkreasi.pillarpos.data.model.UserEntity
import com.pilarkreasi.pillarpos.databinding.ItemUserBinding

class UserAdapter(
    private val users: List<UserEntity>,
    private val onDeleteClick: (UserEntity) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserEntity) {
            binding.tvUsername.text = item.username
            binding.tvRole.text = item.peran.name
            
            
            binding.btnDelete.isEnabled = item.idUser != 1
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }
}
