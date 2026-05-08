package com.example.homify

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class PropertyAdapter(private var propertyList: List<Property>) :
    RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>() {

    class PropertyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvPropertyName)
        val price: TextView = view.findViewById(R.id.tvPropertyPrice)
        val location: TextView = view.findViewById(R.id.tvLocation)
        val propertyImage: ImageView = view.findViewById(R.id.ivProperty)
        val unitType: Chip = view.findViewById(R.id.unitType)
        val amenitiesContainer: LinearLayout = view.findViewById(R.id.amenitiescontainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_property, parent, false)
        return PropertyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = propertyList[position]

        holder.title.text = property.title
        holder.price.text = "${property.price} EGP"
        holder.location.text = "${property.governorate}, ${property.address}"
        holder.propertyImage.setImageResource(property.imageUrl)
        holder.unitType.text = property.type

        holder.amenitiesContainer.removeAllViews()
        property.amenities.forEach { amenity ->
            val iconRes = when (amenity) {
                "WiFi" -> R.drawable.internet
                "Parking" -> R.drawable.parking
                "Gym" -> R.drawable.gym
                "Laundry" -> R.drawable.laundry
                "Pool" -> R.drawable.pool
                "Garden" -> R.drawable.garden
                else -> null
            }

            if (iconRes != null) {

                val imageView = ImageView(holder.itemView.context).apply {
                    layoutParams = LinearLayout.LayoutParams(35, 35).apply {
                        setMargins(0, 0, 8, 0)
                    }
                    setImageResource(iconRes)
                    setColorFilter(Color.parseColor("#666666"))
                }

                val textView = TextView(holder.itemView.context).apply {
                    text = amenity
                    textSize = 11f
                    setTextColor(Color.parseColor("#666666"))
                    setPadding(0, 0, 20, 0)
                }

                holder.amenitiesContainer.addView(imageView)
                holder.amenitiesContainer.addView(textView)
            }
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PropertyDetailsActivity::class.java).apply {
                putExtra("TITLE", property.title)
                putExtra("PRICE", property.price)
                putExtra("GOVERNORATE", property.governorate)
                putExtra("ADDRESS", property.address)
                putExtra("DESC", property.description)
                putExtra("IMAGE", property.imageUrl)
                putExtra("BEDROOMS", property.bedrooms)
                putExtra("BATHROOMS", property.bathrooms)
                putExtra("SIZE", property.size)
                putExtra("UNIT_TYPE", property.type)
                putExtra("MAP_LINK", property.locationLink)
                putExtra("UNIT_ID", property.id)
                putStringArrayListExtra("AMENITIES", ArrayList(property.amenities))
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = propertyList.size

    fun updateData(newList: List<Property>) {
        this.propertyList = newList
        notifyDataSetChanged()
    }
}