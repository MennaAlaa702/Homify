package com.example.homify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import java.io.File

class unitAdapter(
    private val units: MutableList<units>,
    private val onDeleteClick: (units, Int) -> Any
) : RecyclerView.Adapter<unitAdapter.UnitViewHolder>() {

    inner class UnitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.iv_unit_image)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_unit_price)
        val tvName: TextView = itemView.findViewById(R.id.tv_unit_name)
        val tvLandlord: TextView = itemView.findViewById(R.id.tv_unit_landlord)
        val tvDetails: TextView = itemView.findViewById(R.id.tv_unit_details)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btn_delete_unit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_unit, parent, false)
        return UnitViewHolder(view)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        val unit = units[position]
        val ctx = holder.itemView.context

        holder.tvPrice.text = unit.price
        holder.tvName.text = unit.name
        holder.tvLandlord.text = "Landlord: ${unit.landlord}"
        holder.tvDetails.text = unit.details

        val imagePath = unit.imagePath
        if (!imagePath.isNullOrEmpty()) {
            val file = File(imagePath)
            if (file.exists()) {
                Glide.with(ctx)
                    .load(file)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(holder.ivImage)
            } else {
                holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Apply status styling
        /*when (unit.status) {
            UnitStatus.PENDING_APPROVAL -> {
                holder.tvStatus.text = ctx.getString(R.string.status_pending)
                holder.tvStatus.setTextColor(Color.parseColor("#F39C12"))
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FFF8E1"))
            }
            UnitStatus.ACTIVE -> {
                holder.tvStatus.text = ctx.getString(R.string.status_active)
                holder.tvStatus.setTextColor(Color.parseColor("#27AE60"))
                holder.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
            }
            UnitStatus.DRAFT -> {
                holder.tvStatus.text = ctx.getString(R.string.status_draft)
                holder.tvStatus.setTextColor(Color.parseColor("#1565C0"))
                holder.tvStatus.setBackgroundColor(Color.parseColor("#E3F2FD"))
            }
        }*/


        holder.btnDelete.setOnClickListener {
            onDeleteClick(unit, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = units.size

    fun removeUnit(position: Int) {
        if (position >= 0 && position < units.size) {
            units.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, units.size)
        }
    }
}