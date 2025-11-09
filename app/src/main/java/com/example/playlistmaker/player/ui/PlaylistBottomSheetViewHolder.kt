package com.example.playlistmaker.player.ui

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.playlist.domain.Playlist
import java.io.File

class PlaylistBottomSheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val coverImageView: ImageView = itemView.findViewById(R.id.playlistCoverImageView)
    private val nameTextView: TextView = itemView.findViewById(R.id.playlistNameTextView)
    private val trackCountTextView: TextView = itemView.findViewById(R.id.playlistTrackCountTextView)

    fun bind(playlist: Playlist) {
        nameTextView.text = playlist.name
        trackCountTextView.text = formatTrackCount(playlist.trackCount)

        val placeholderRes = if (isInNightMode(itemView.context)) {
            R.drawable.ic_cover_placeholder_night
        } else {
            R.drawable.ic_cover_placeholder
        }

        if (playlist.coverPath != null) {
            val coverFile = File(playlist.coverPath)
            if (coverFile.exists()) {
                Glide.with(itemView)
                    .load(coverFile)
                    .placeholder(placeholderRes)
                    .error(placeholderRes)
                    .centerCrop()
                    .transform(RoundedCorners(dpToPx(itemView.context, 8)))
                    .into(coverImageView)
            } else {
                Glide.with(itemView)
                    .load(placeholderRes)
                    .centerCrop()
                    .transform(RoundedCorners(dpToPx(itemView.context, 8)))
                    .into(coverImageView)
            }
        } else {
            Glide.with(itemView)
                .load(placeholderRes)
                .centerCrop()
                .transform(RoundedCorners(dpToPx(itemView.context, 8)))
                .into(coverImageView)
        }
    }

    private fun formatTrackCount(count: Int): String {
        return when {
            count == 0 -> itemView.context.getString(R.string.playlist_track_count_zero)
            count == 1 -> itemView.context.getString(R.string.playlist_track_count_one)
            count in 2..4 -> itemView.context.getString(R.string.playlist_track_count_few, count)
            else -> itemView.context.getString(R.string.playlist_track_count_many, count)
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    private fun isInNightMode(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode
        return (uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
    }
}

