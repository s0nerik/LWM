package com.lwm.app.player;

import android.util.Log;

import com.lwm.app.App;
import com.lwm.app.model.Song;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

public class Queue {

    // Needed only for proper shuffling
    private Stack<Song> played = new Stack<>();

    private List<Song> queue = new ArrayList<>();

    private int currentIndex;
    private Song currentSong;

    private boolean shuffled = false;

    public Queue() {
    }

    public Queue(List<Song> songs) {
        this.queue = songs;
    }

    public void addSong(Song song) {
        queue.add(song);
    }

    public void addSong(Song song, int pos) {
        queue.add(pos, song);
    }

    public void addSongs(Collection<Song> songs) {
        queue.addAll(songs);
    }

    public void addSongs(List<Song> songs, int pos) {
        queue.addAll(pos, songs);
    }

    public void removeSong(Song song) {
        queue.remove(song);
    }

    public void removeSongs(List<Song> songs) {
        queue.removeAll(songs);
    }

    /**
     * Removes played list from queue list, shuffles queue list and
     * then adds played list to the beginning of queue list.
     */
    public void shuffleExceptPlayed() {
        queue.removeAll(played);
        Collections.shuffle(queue);
        queue.addAll(0, played);

        currentIndex = played.size()-1;
        currentSong = queue.get(currentIndex);

        shuffled = true;
    }

    public void shuffle() {
        played.clear();
        Collections.shuffle(queue);
        currentIndex = 0;
        currentSong = queue.get(currentIndex);
        shuffled = true;
    }

    /**
     *
     * @return true if successfully moved to the next song, else returns false
     */
    public boolean moveToNext() {
        try {
            currentSong = queue.get(currentIndex + 1);
            played.push(currentSong);
            currentIndex++;
            return true;
        } catch (IndexOutOfBoundsException e) {
            currentSong = queue.get(currentIndex);
            return false;
        }
    }

    /**
     *
     * @return true if successfully moved to the next previous song, else returns false
     */
    public boolean moveToPrev() {
        try {
            currentSong = queue.get(currentIndex - 1);
            played.pop();
            currentIndex--;
            return true;
        } catch (IndexOutOfBoundsException | EmptyStackException e) {
            currentSong = queue.get(currentIndex);
            return false;
        }
    }

    /**
     *
     * @return true if successfully moved to the next previous song, else returns false
     */
    public boolean moveTo(int position) {
        try {
            currentSong = queue.get(position);
            played.push(currentSong);
            currentIndex = position;
            return true;
        } catch (IndexOutOfBoundsException e) {
            Log.e(App.TAG, "moveTo IndexOutOfBoundsException");
            currentSong = queue.get(currentIndex);
            return false;
        }
    }

    public Song getSong() {
        return currentSong;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public List<Song> getQueue() {
        return queue;
    }

    public int getSize() {
        return queue.size();
    }

    public boolean isShuffled() {
        return shuffled;
    }

    public boolean contains(Song song) {
        return queue.contains(song);
    }
}
