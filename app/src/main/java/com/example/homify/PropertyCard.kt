package com.example.homify

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class PropertyCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private var propertyImage: ImageView? = null
    private var propertyName: TextView? = null
    private var propertyPrice: Chip? = null
    private var propertyLocation: TextView? = null
    private var unitTypeChip: Chip? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        propertyImage = findViewById(R.id.ivProperty)
        propertyName = findViewById(R.id.tvPropertyName)
        propertyPrice = findViewById(R.id.tvPropertyPrice)
        propertyLocation = findViewById(R.id.tvLocation)
        unitTypeChip = findViewById(R.id.unitType)
    }

    fun setPropertyData(name: String, price: String, location: String, imageUrl: Int, type: String) {
        propertyName?.text = name
        propertyPrice?.text = "$$price/mo"
        propertyLocation?.text = location
        unitTypeChip?.text = type

        propertyImage?.let { view ->
            Glide.with(context)
                .load(imageUrl)
                .placeholder(android.R.drawable.progress_horizontal)
                .error(android.R.drawable.stat_notify_error)
                .into(view)
        }
    }
}