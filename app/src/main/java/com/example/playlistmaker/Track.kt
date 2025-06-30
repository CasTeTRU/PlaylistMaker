package com.example.playlistmaker

import android.os.Parcel
import android.os.Parcelable

data class Track(
    val trackId: String,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String,
    val collectionName: String? = null,
    val releaseDate: String? = null,
    val primaryGenreName: String? = null,
    val country: String? = null,
    val previewUrl: String? = null
) : Parcelable {

    val formattedTrackTime: String
        get() = Track.formatMillis(trackTimeMillis)

    fun getCoverArtwork(): String? {
        return artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
    }

    fun getReleaseYear(): String? {
        return releaseDate?.take(4)
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(trackId)
        dest.writeString(trackName)
        dest.writeString(artistName)
        dest.writeLong(trackTimeMillis)
        dest.writeString(artworkUrl100)
        dest.writeString(collectionName)
        dest.writeString(releaseDate)
        dest.writeString(primaryGenreName)
        dest.writeString(country)
        dest.writeString(previewUrl)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Track> {
            override fun createFromParcel(parcel: Parcel): Track {
                return Track(
                    trackId = parcel.readString()!!,
                    trackName = parcel.readString()!!,
                    artistName = parcel.readString()!!,
                    trackTimeMillis = parcel.readLong(),
                    artworkUrl100 = parcel.readString()!!,
                    collectionName = parcel.readString(),
                    releaseDate = parcel.readString(),
                    primaryGenreName = parcel.readString(),
                    country = parcel.readString(),
                    previewUrl = parcel.readString()
                )
            }

            override fun newArray(size: Int): Array<Track?> {
                return arrayOfNulls(size)
            }
        }

        fun formatMillis(millis: Long): String {
            val totalSeconds = millis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }
}