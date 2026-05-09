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
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import java.io.File

class propertyAdapter(private var propertyList: List<property>) :
    RecyclerView.Adapter<propertyAdapter.PropertyViewHolder>() {

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

        // 1. تعبئة النصوص الأساسية
        holder.title.text = property.title
        holder.price.text = "${property.price} EGP"
        holder.location.text = "${property.governorate}, ${property.address}"
        holder.unitType.text = property.type

        // 2. معالجة وتحميل الصورة باستخدام Glide
        val firstImage = property.imageUrl.split(",").firstOrNull()?.trim() ?: ""

        if (firstImage.isNotEmpty()) {
            val file = File(firstImage)
            if (file.exists()) {
                Glide.with(holder.itemView.context)
                    .load(file)
                    .placeholder(R.drawable.home)
                    .error(R.drawable.home)
                    .into(holder.propertyImage)
            } else {
                holder.propertyImage.setImageResource(R.drawable.home)
            }
        } else {
            holder.propertyImage.setImageResource(R.drawable.home)
        }

        // 3. رسم الـ Amenities ديناميكياً
        holder.amenitiesContainer.removeAllViews()
        // هنجيب الـ context من العنصر اللي ماسكه الـ holder
        val context = holder.itemView.context
        property.amenities.forEach { amenity ->
            val iconRes = when (amenity) {
                context.getString(R.string.wifi) -> R.drawable.internet
                context.getString(R.string.parking) -> R.drawable.parking
                context.getString(R.string.gym) -> R.drawable.gym
                context.getString(R.string.laundry) -> R.drawable.laundry
                context.getString(R.string.pool) -> R.drawable.pool
                context.getString(R.string.garden) -> R.drawable.garden
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

        // 4. تمرير البيانات لشاشة التفاصيل عند الضغط
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, propertyDetailsActivity::class.java).apply {
                putExtra(context.getString(R.string.Unit_Id), property.id) // تم الدمج بنجاح
                putExtra(context.getString(R.string.TITLE), property.title)
                putExtra(context.getString(R.string.PRICE), property.price)
                putExtra(context.getString(R.string.GOVERNORATE), property.governorate)
                putExtra(context.getString(R.string.Address), property.address)
                putExtra(context.getString(R.string.desc), property.description)
                putExtra(context.getString(R.string.IMAGE), property.imageUrl)
                putExtra(context.getString(R.string.Bedrooms), property.bedrooms)
                putExtra(context.getString(R.string.Bathrooms), property.bathrooms)
                putExtra(context.getString(R.string.SIZE), property.size)
                putExtra(context.getString(R.string.UNIT_TYPE), property.type)
                putExtra(context.getString(R.string.map_link), property.locationLink)
                putStringArrayListExtra(context.getString(R.string.AMENITIES), ArrayList(property.amenities))
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = propertyList.size

    fun updateData(newList: List<property>) {
        this.propertyList = newList
        notifyDataSetChanged()
    }
}