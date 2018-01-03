package com.raffaelcavaliere.setlists.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class SetlistsDbItemDocumentMidiMessage implements Parcelable {

    private String id;
    private String document;
    private String name;
    private int channel;
    private int status;
    private int data1;
    private int data2;
    private int autosend;


    public String getId() { return this.id; }

    public void setId(String id) { this.id = id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getData1() {
        return data1;
    }

    public void setData1(int data1) {
        this.data1 = data1;
    }

    public int getData2() {
        return data2;
    }

    public void setData2(int data2) {
        this.data2 = data2;
    }

    public int getAutosend() {
        return autosend;
    }

    public void setAutosend(int autosend) {
        this.autosend = autosend;
    }

    public SetlistsDbItemDocumentMidiMessage(String id, String document, String name, int channel, int status, int data1, int data2, int autosend) {
        this.id = id;
        this.document = document;
        this.name = name;
        this.channel = channel;
        this.status = status;
        this.data1 = data1;
        this.data2 = data2;
        this.autosend = autosend;
    }



    private SetlistsDbItemDocumentMidiMessage(Parcel in) {
        Bundle bundle = in.readBundle();
        this.id = bundle.getString("id");
        this.document = bundle.getString("document");
        this.name = bundle.getString("name");
        this.channel = bundle.getInt("channel");
        this.status = bundle.getInt("status");
        this.data1 = bundle.getInt("data1");
        this.data2 = bundle.getInt("data2");
        this.autosend = bundle.getInt("autosend");
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("document", document);
        bundle.putString("name", name);
        bundle.putInt("channel", channel);
        bundle.putInt("status", status);
        bundle.putInt("data1", data1);
        bundle.putInt("data2", data2);
        bundle.putInt("autosend", autosend);
        out.writeBundle(bundle);
    }

    public static final Creator<SetlistsDbItemDocumentMidiMessage> CREATOR = new Creator<SetlistsDbItemDocumentMidiMessage>() {
        public SetlistsDbItemDocumentMidiMessage createFromParcel(Parcel in) {
            return new SetlistsDbItemDocumentMidiMessage(in);
        }

        public SetlistsDbItemDocumentMidiMessage[] newArray(int size) {
            return new SetlistsDbItemDocumentMidiMessage[size];
        }
    };
}

