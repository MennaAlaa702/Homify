package com.example.homify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

//import com.homify.app.models.UnitStatus

/**
 * RecyclerView Adapter for displaying property units in ManageUnitsActivity.
 * Supports delete action per item.
 */
class UnitAdapter(
    private val units: MutableList<Units>,
    private val onDeleteClick: (Units, Int) -> Any // غيري كلمة Unit الأخيرة لـ Any
) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

    inner class UnitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.iv_unit_image)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_unit_price)
        //val tvStatus: TextView = itemView.findViewById(R.id.tv_unit_status)
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

        holder.ivImage.setImageResource(unit.imageResId)
        holder.tvPrice.text = unit.price
        holder.tvName.text = unit.name
        holder.tvLandlord.text = ctx.getString(R.string.manage_units_subtitle).let { "Landlord: ${unit.landlord}" }
        holder.tvLandlord.text = "Landlord: ${unit.landlord}"
        holder.tvDetails.text = unit.details

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

        // Delete triggers confirmation dialog in activity
        holder.btnDelete.setOnClickListener {
            onDeleteClick(unit, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = units.size

    /**
     * Remove a unit from the list and notify the adapter.
     */
    fun removeUnit(position: Int) {
        if (position >= 0 && position < units.size) {
            units.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, units.size)
        }
    }
}
