package com.raffaelcavaliere.setlists.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class SetlistsDbItemSetSong implements Parcelable {
    private long id;
    private long set;
    private long song;
    private long document;
    private long artist;
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

    public SetlistsDbItemSetSong(long id, long set, long song, int sequence, long document, int type, long artist,
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

    public long getId() { return this.id; }
    public void setId(long id) { this.id = id; }

    public long getSet() { return this.set; }
    public void setSet(long set) { this.set = set; }

    public long getSong() { return this.song; }
    public void setSong(long song) { this.song = song; }

    public int getSequence() { return this.sequence; }
    public void setSequence(int sequence) { this.sequence = sequence; }

    public long getDocument() { return this.document; }
    public void setDocument(long document) { this.document = document; }

    public int getType() { return this.type; }
    public void setType(int type) { this.type = type; }

    public long getArtist() { return this.artist; }
    public void setArtist(long artist) { this.artist = artist; }

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
        this.id = bundle.getLong("id");
        this.set = bundle.getLong("set");
        this.song = bundle.getLong("song");
        this.sequence = bundle.getInt("sequence");
        this.document = bundle.getLong("document");
        this.type = bundle.getInt("type");
        this.artist = bundle.getLong("artist");
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
        bundle.putLong("id", id);
        bundle.putLong("set", set);
        bundle.putLong("song", song);
        bundle.putInt("sequence", sequence);
        bundle.putLong("document", document);
        bundle.putInt("type", type);
        bundle.putLong("artist", artist);
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

