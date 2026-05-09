package com.example.homify

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class propertyCard @JvmOverloads constructor(
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

    // تغيير Int إلى String في الباراميتر الرابع
    fun setPropertyData(name: String, price: String, location: String, imageUrl: String, type: String) {
        propertyName?.text = name
        propertyPrice?.text = "$price EGP" // غيرنا العلامة لـ EGP لتناسب بياناتك
        propertyLocation?.text = location
        unitTypeChip?.text = type

        propertyImage?.let { view ->
            if (imageUrl.isNotEmpty()) {
                Glide.with(context)
                    .load(imageUrl) // Glide سيفهم تلقائياً إذا كان رابط URL أو مسار ملف
                    .placeholder(R.drawable.home3) // صورة مؤقتة أثناء التحميل
                    .error(R.drawable.home3)       // صورة في حال حدوث خطأ
                    .into(view)
            } else {
                view.setImageResource(R.drawable.home3)
            }
        }
    }
}