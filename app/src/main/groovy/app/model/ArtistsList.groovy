//package app.model
//import android.database.Cursor
//import groovy.transform.CompileStatic
//
//@CompileStatic
//class ArtistsList {
//
//    List<Artist> artists = new ArrayList<>()
//
//    ArtistsList(Cursor cursor) {
//        if(cursor.moveToFirst()) {
//            artists << Artist.initialize(cursor)
//            while (cursor.moveToNext()) {
//                artists << buildArtistFromCursor(cursor)
//            }
//        }
//        cursor.close();
//    }
//}