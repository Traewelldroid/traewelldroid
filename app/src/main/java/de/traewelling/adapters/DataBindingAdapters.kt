package de.traewelling.adapters

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.time.LocalDateTime
import java.util.*

@BindingAdapter("imageResource")
fun setImageResource(imageView: ImageView, resourceId: Int) {
    imageView.setImageResource(resourceId)
}

@BindingAdapter("displayTime")
fun setTime(textView: TextView, date: LocalDateTime) {
}