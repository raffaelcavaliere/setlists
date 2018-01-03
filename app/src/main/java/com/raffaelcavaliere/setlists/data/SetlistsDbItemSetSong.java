package com.raffaelcavaliere.setlists.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class SetlistsDbItemSetSong implements Parcelable {
    private String id;
    private String set;
    private String song;
    private String document;
    private String artist;
    private String title;
    private String description;
    private String key;
    private String notes;
    private int sequence;
    private int tempo;
    private int duration;
    private int type;
    private int transpose;
    private int transposeMode;

    public SetlistsDbItemSetSong(String id, String set, String song, int sequence, String document, int type, String artist,
                                 String title, String description, String key, int tempo, int duration, int transpose,
                                 int tranposeMode, String notes) {
        this.id = id;
        this.set = set;
        this.song = song;
        this.sequence = sequence;
        this.document = document;
        this.type = type;
        this.artist = artist;
        this.title = title;
        this.description = description;
        this.key = key;
        this.tempo = tempo;
        this.duration = duration;
        this.transpose = transpose;
        this.transposeMode = tranposeMode;
        this.notes = notes;
    }

    public String getId() { return this.id; }
    public void setId(String id) { this.id = id; }

    public String getSet() { return this.set; }
    public void setSet(String set) { this.set = set; }

    public String getSong() { return this.song; }
    public void setSong(String song) { this.song = song; }

    public int getSequence() { return this.sequence; }
    public void setSequence(int sequence) { this.sequence = sequence; }

    public String getDocument() { return this.document; }
    public void setDocument(String document) { this.document = document; }

    public int getType() { return this.type; }
    public void setType(int type) { this.type = type; }

    public String getArtist() { return this.artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getTitle() { return this.title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public String getKey() { return this.key; }
    public void setKey(String key) { this.key = key; }

    public int getTempo() { return this.tempo; }
    public void setTempo(int tempo) { this.tempo = tempo; }

    public int getDuration() { return this.duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getTranspose() { return this.transpose; }
    public void setTranspose(int transpose) { this.transpose = transpose; }

    public int getTranposeMode() { return this.transposeMode; }
    public void setTransposeMode(int transposeMode) { this.transposeMode = transposeMode; }

    public String getNotes() { return this.notes; }
    public void setNotes(String notes) { this.notes = notes; }

    private SetlistsDbItemSetSong(Parcel in) {
        Bundle bundle = in.readBundle();
        this.id = bundle.getString("id");
        this.set = bundle.getString("set");
        this.song = bundle.getString("song");
        this.sequence = bundle.getInt("sequence");
        this.document = bundle.getString("document");
        this.type = bundle.getInt("type");
        this.artist = bundle.getString("artist");
        this.title = bundle.getString("title");
        this.description = bundle.getString("description");
        this.key = bundle.getString("key");
        this.tempo = bundle.getInt("tempo");
        this.duration = bundle.getInt("duration");
        this.transpose = bundle.getInt("transpose");
        this.transposeMode = bundle.getInt("transposeMode");
        this.notes = bundle.getString("notes");
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("set", set);
        bundle.putString("song", song);
        bundle.putInt("sequence", sequence);
        bundle.putString("document", document);
        bundle.putInt("type", type);
        bundle.putString("artist", artist);
        bundle.putString("title", title);
        bundle.putString("description", description);
        bundle.putString("key", key);
        bundle.putInt("tempo", tempo);
        bundle.putInt("duration", duration);
        bundle.putInt("transpose", transpose);
        bundle.putInt("transposeMode", transposeMode);
        bundle.putString("notes", notes);
        out.writeBundle(bundle);
    }

    public static final Parcelable.Creator<SetlistsDbItemSetSong> CREATOR = new Parcelable.Creator<SetlistsDbItemSetSong>() {
        public SetlistsDbItemSetSong createFromParcel(Parcel in) {
            return new SetlistsDbItemSetSong(in);
        }

        public SetlistsDbItemSetSong[] newArray(int size) {
            return new SetlistsDbItemSetSong[size];
        }
    };
}

