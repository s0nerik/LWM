package app.model

import com.arasthel.swissknife.annotations.Parcelable
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder;

@CompileStatic
@Builder
@Parcelable
public final class Album {
    int id;
    int year;
    int songsCount;
    String title;
    String artist;
    String albumArtPath;
}