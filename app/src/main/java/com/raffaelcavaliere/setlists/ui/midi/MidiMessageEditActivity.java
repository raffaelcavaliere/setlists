package com.raffaelcavaliere.setlists.ui.midi;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.utils.MidiHelper;

import java.util.Date;
import java.util.UUID;

public class MidiMessageEditActivity extends AppCompatActivity {

    private TextView textName;
    private Spinner spinnerChannel;
    private Spinner spinnerStatus;
    private TextView textData1;
    private TextView textData2;
    private Button saveButton;
    private Button cancelButton;
    private String id = null;

    private int mode;
    public final static String EXTRA_MODE = "mode";
    public final static int EXTRA_MODE_NEW = 0;
    public final static int EXTRA_MODE_EDIT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midi_message_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        mode = extras.getInt(EXTRA_MODE, 0);

        id = extras.getString("id", null);

        String name = extras.getString("name", "");
        textName = (TextView) findViewById(R.id.editMidiMessageName);
        textName.setText(name);

        int channel = extras.getInt("channel", 0);
        spinnerChannel = (Spinner) findViewById(R.id.editMidiMessageChannel);
        final Integer[] channels = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
        final ArrayAdapter<Integer> channelsAdapter = new ArrayAdapter<Integer>(this, R.layout.spinner_element, channels);
        spinnerChannel.setAdapter(channelsAdapter);
        spinnerChannel.setSelection(channelsAdapter.getPosition(channel));

        int status = extras.getInt("status", 0);
        spinnerStatus = (Spinner) findViewById(R.id.editMidiMessageStatus);
        final MidiHelper.MidiStatus[] statuses = MidiHelper.getMidiStatuses();
        final ArrayAdapter<MidiHelper.MidiStatus> statusesAdapter = new ArrayAdapter<MidiHelper.MidiStatus>(this, R.layout.spinner_element, statuses);
        spinnerStatus.setAdapter(statusesAdapter);
        for (MidiHelper.MidiStatus midiStatus : statuses) {
            if (midiStatus.getStatus() == (byte) status) {
                spinnerStatus.setSelection(statusesAdapter.getPosition(midiStatus));
                break;
            }
        }

        int data1 = extras.getInt("data1", 0);
        textData1 = (TextView) findViewById(R.id.editMidiMessageData1);
        textData1.setText(String.valueOf(data1));

        int data2 = extras.getInt("data2", 0);
        textData2 = (TextView) findViewById(R.id.editMidiMessageData2);
        textData2.setText(String.valueOf(data2));

        saveButton = (Button) findViewById(R.id.editMidiMessageSave);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textName.getText().length() > 0) {
                    Intent data = new Intent();
                    String name = textName.getText().toString();
                    int channel = (Integer)spinnerChannel.getSelectedItem();
                    int status = ((MidiHelper.MidiStatus)spinnerStatus.getSelectedItem()).getStatus();
                    String data1 = textData1.getText().toString();
                    String data2 = textData2.getText().toString();

                    if (mode == EXTRA_MODE_EDIT) {
                        int result = updateMidiMessage(
                                name,
                                channel,
                                status,
                                data1.isEmpty() ? 0 : Integer.valueOf(data1),
                                data2.isEmpty() ? 0 : Integer.valueOf(data2)
                        );
                        setResult(RESULT_OK, data);

                    } else {
                        Uri result = addMidiMessage(name,
                                channel,
                                status,
                                data1.isEmpty() ? 0 : Integer.valueOf(data1),
                                data2.isEmpty() ? 0 : Integer.valueOf(data2)
                        );
                        data.setData(result);
                        id = result.getLastPathSegment();
                        setResult(RESULT_OK, data);
                    }
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide a name", Toast.LENGTH_SHORT).show();
                }

            }
        });

        cancelButton = (Button) findViewById(R.id.editMidiMessageCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Uri addMidiMessage(String name, int channel, int status, int data1, int data2) {
        ContentValues values = new ContentValues();
        values.put(SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_ID, UUID.randomUUID().toString());
        values.put(SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_NAME, name);
        values.put(SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_CHANNEL, channel);
        values.put(SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_STATUS, status);
        values.put(SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATA1, data1);
        values.put(SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATA2, data2);
        values.put(SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATE_ADDED, new Date().getTime() / 1000);
        values.put(SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

        return getContentResolver().insert(SetlistsDbContract.SetlistsDbMidiMessageEntry.CONTENT_URI, values);
    }

    private int updateMidiMessage(String name, int channel, int status, int data1, int data2) {
        ContentValues values = new ContentValues();
        values.put(SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_NAME, name);
        values.put(SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_CHANNEL, channel);
        values.put(SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_STATUS, status);
        values.put(SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATA1, data1);
        values.put(SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATA2, data2);
        values.put(SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

        return getContentResolver().update(SetlistsDbContract.SetlistsDbMidiMessageEntry.buildSetlistsDbMidiMessageUri(id), values,
                SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
    }


}
