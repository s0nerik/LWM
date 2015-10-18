//package app.model
//import android.database.Cursor
//import app.helper.db.ArtistsCursorGetter
//import groovy.transform.CompileStatic
//
//@CompileStatic
//public class ArtistWrapperList {
//
//    List<ArtistWrapper> artistWrappers = new ArrayList<>();
//
//    public ArtistWrapperList(ArtistsCursorGetter cursorGetter) {
//        if(cursor.moveToFirst()) {
//            artistWrappers << ArtistWrapper.initialize(cursor)
//            while (cursor.moveToNext()) {
//                artistWrappers << ArtistWrapper.initialize(cursor)
//            }
//        }
//        cursor.close();
//    }
//
//    static fromCursorGetter() {
//
//    }
//
//}