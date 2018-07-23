package com.raffaelcavaliere.setlists.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class SetlistsDbItemSong implements Parcelable {
    private String id;
    private String document;
    private String artist;
    private String artistName;
    private String title;
    private String key;
    private String notes;
    private String author;
    private int tempo;
    private int duration;
    private Date modified;

    public SetlistsDbItemSong(String id, String document, String artist, String artistName,
                              String title, String key, int tempo, int duration, String notes,
                              String author, Date modified) {
        this.id = id;
        this.document = document;
        this.artist = artist;
        this.artistName = artistName;
        this.title = title;
        this.key = key;
        this.tempo = tempo;
        this.duration = duration;
        this.notes = notes;
        this.author = author;
        this.modified = modified;
    }

    public String getId() { return this.id; }
    public void setId(String id) { this.id = id; }

    public String getDocument() { return this.document; }
    public void setDocument(String document) { this.document = document; }

    public String getArtist() { return this.artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getArtistName() { return this.artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public String getTitle() { return this.title; }
    public void setTitle(String title) { this.title = title; }

    public String getKey() { return this.key; }
    public void setKey(String key) { this.key = key; }

    public int getTempo() { return this.tempo; }
    public void setTempo(int tempo) { this.tempo = tempo; }

    public int getDuration() { return this.duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getNotes() { return this.notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getAuthor() { return this.author; }
    public void setAuthor(String author) { this.author = author; }

    public Date getModified() { return this.modified; }
    public void setModified(Date modified) { this.modified = modified; }

    private SetlistsDbItemSong(Parcel in) {
        Bundle bundle = in.readBundle();
        this.id = bundle.getString("id");
        this.document = bundle.getString("document");
        this.artist = bundle.getString("artist");
        this.artistName = bundle.getString("artistName");
        this.title = bundle.getString("title");
        this.key = bundle.getString("key");
        this.tempo = bundle.getInt("tempo");
        this.duration = bundle.getInt("duration");
        this.notes = bundle.getString("notes");
        this.author = bundle.getString("author");
        this.modified = new Date(bundle.getLong("modified") * 1000);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("document", document);
        bundle.putString("artist", artist);
        bundle.putString("artistName", artistName);
        bundle.putString("title", title);
        bundle.putString("key", key);
        bundle.putInt("tempo", tempo);
        bundle.putInt("duration", duration);
        bundle.putString("notes", notes);
        bundle.putString("author", author);
        bundle.putLong("modified", modified.getTime() / 1000);
        out.writeBundle(bundle);
    }

    public static final Creator<SetlistsDbItemSong> CREATOR = new Creator<SetlistsDbItemSong>() {
        public SetlistsDbItemSong createFromParcel(Parcel in) {
            return new SetlistsDbItemSong(in);
        }

        public SetlistsDbItemSong[] newArray(int size) {
            return new SetlistsDbItemSong[size];
        }
    };
}

