package com.example.playlistmaker

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val artwork: ImageView = itemView.findViewById(R.id.artworkImageView)
    private val trackName: TextView = itemView.findViewById(R.id.trackNameTextView)
    private val artistName: TextView = itemView.findViewById(R.id.artistNameTextView)
    private val trackTime: TextView = itemView.findViewById(R.id.trackTimeTextView)

    fun bind(track: Track) {
        trackName.text = track.trackName
        artistName.text = track.artistName
        trackTime.text = track.formattedTrackTime

        val placeholderRes = if (isInNightMode(itemView.context)) {
            R.drawable.ic_cover_placeholder_night
        } else {
            R.drawable.ic_cover_placeholder
        }

        val coverUrl = track.getCoverArtwork()

        Glide.with(itemView)
            .load(coverUrl)
            .placeholder(placeholderRes)
            .error(placeholderRes)
            .centerCrop()
            .transform(RoundedCorners(dpToPx(itemView.context, 12)))
            .into(artwork)
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    private fun isInNightMode(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode
        return (uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}