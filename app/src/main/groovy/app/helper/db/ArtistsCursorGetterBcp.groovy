//package app.helper.db
//import android.database.Cursor
//import android.net.Uri
//import android.provider.MediaStore
//import app.model.Artist
//import groovy.transform.CompileStatic
//
//@CompileStatic
//final class ArtistsCursorGetterBcp extends CursorGetter {
//
//    Uri contentUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
//    List<String> projection = [
//            MediaStore.Audio.Artists._ID,
//            MediaStore.Audio.Artists.ARTIST,
//            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
//            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
//    ]
//
//
////    public Cursor getArtistsCursor(){
////        return contentResolver.query(
////                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
////                projection,
////                null,
////                null,
////                MediaStore.Audio.Artists.DEFAULT_SORT_ORDER
////        );
////    }
////
////    public Artist getArtistById(long id){
////        String selection = MediaStore.Audio.Artists._ID + " = ?";
////        String[] selectionArgs = [ String.valueOf(id) ];
////        Cursor cursor = contentResolver.query(
////                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
////                projection,
////                selection,
////                selectionArgs,
////                MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
////
////        Artist artist = null;
////        if (cursor.moveToFirst()) {
////            artist = Artist.builder()
////                    .id(cursor.getInt(_ID))
////                    .name(cursor.getString(ARTIST))
////                    .numberOfAlbums(cursor.getInt(NUMBER_OF_ALBUMS))
////                    .numberOfSongs(cursor.getInt(NUMBER_OF_TRACKS))
////                    .build();
////        }
////        cursor.close();
////
////        return artist;
////    }
//
//    @Override
//    String getDefaultSelection() {
//        return "$MediaStore.Audio.Artists._ID = ?"
//    }
//
//    @Override
//    List<String> getDefaultSelectionArgs() {
//        return [ id as String ]
//    }
//
//    @Override
//    SortOrder getDefaultSortOrder() {
//        return new StringSortOrder(MediaStore.Audio.Artists.DEFAULT_SORT_ORDER)
//    }
//
//    @Override
//    Cursor getCursor() {
//        String selection = MediaStore.Audio.Artists._ID + " = ?";
//        String[] selectionArgs = [ String.valueOf(id) ];
//        Cursor cursor = contentResolver.query(
//                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
//                projection,
//                selection,
//                selectionArgs,
//                MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
//
//        Artist artist = null;
//        if (cursor.moveToFirst()) {
//            artist = Artist.builder()
//                    .id(cursor.getInt(_ID))
//                    .name(cursor.getString(ARTIST))
//                    .numberOfAlbums(cursor.getInt(NUMBER_OF_ALBUMS))
//                    .numberOfSongs(cursor.getInt(NUMBER_OF_TRACKS))
//                    .build();
//        }
//        cursor.close();
//
//        return artist;
//
//        return null
//    }
//}