package app.helpers.providers

import app.R
import app.adapters.songs.SongItem
import groovy.transform.CompileStatic

@CompileStatic
class BubbleTextProviders {
    static final Map<Integer, Closure<String>> SONGS = [
            (R.id.songs_sort_title) : { SongItem it -> it.song.title[0] },
            (R.id.songs_sort_artist): { SongItem it -> it.song.artistName[0] },
            (R.id.songs_sort_album) : { SongItem it -> it.song.albumName[0] },
            (R.id.songs_sort_year)  : { SongItem it -> (it.song?.album?.year as String)[0..3] },
    ]
}