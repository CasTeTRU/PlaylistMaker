package com.example.playlistmaker

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val artwork: ImageView = itemView.findViewById(R.id.artworkImageView)
    private val trackName: TextView = itemView.findViewById(R.id.trackNameTextView)
    private val artistName: TextView = itemView.findViewById(R.id.artistNameTextView)


    fun bind(track: Track) {
        trackName.text = track.trackName
        artistName.text = itemView.context.getString(R.string.artist_name_track_time, track.artistName, track.formattedTrackTime)


        val radius = 12.dpToPx(itemView.context)
        Glide.with(itemView)
            .load(track.artworkUrl100)
            .centerCrop()
            .transform(RoundedCorners(radius))
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(artwork)
    }

    fun Int.dpToPx(context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }
}