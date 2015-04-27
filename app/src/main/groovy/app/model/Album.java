package app.model;

import hrisey.Parcelable;
import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
@Parcelable
public final class Album implements android.os.Parcelable {
    private int id;
    private String title;
    private String artist;
    private int year;
    private int songsCount;
    private String albumArtPath;
}
