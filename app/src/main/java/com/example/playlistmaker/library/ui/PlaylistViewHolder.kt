package com.example.playlistmaker.library.ui

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.playlist.domain.Playlist
import java.io.File

class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        val coverFile = playlist.coverPath?.let { File(it) }
        Glide.with(itemView)
            .load(coverFile)
            .placeholder(placeholderRes)
            .error(placeholderRes)
            .centerCrop()
            .into(coverImageView)
    }

    private fun formatTrackCount(count: Int): String {
        return itemView.context.resources.getQuantityString(R.plurals.playlist_track_count, count, count)
    }

    private fun isInNightMode(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode
        return (uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
    }
}

