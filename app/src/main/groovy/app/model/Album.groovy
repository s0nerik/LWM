package app.model

import com.github.s0nerik.betterknife.annotations.Parcelable
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder;

@CompileStatic
@Builder
@Parcelable(exclude = {metaClass})
public final class Album {
    int id;
    int year;
    int songsCount;
    String title;
    String artist;
    String albumArtPath;
}