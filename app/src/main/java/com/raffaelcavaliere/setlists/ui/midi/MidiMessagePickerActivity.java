package com.raffaelcavaliere.setlists.ui.midi;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.data.SetlistsDbDocumentMidiMessageLoader;
import com.raffaelcavaliere.setlists.data.SetlistsDbMidiMessageLoader;
import com.raffaelcavaliere.setlists.ui.MainActivity;
import com.raffaelcavaliere.setlists.utils.MidiHelper;

import java.util.ArrayList;

public class MidiMessagePickerActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;

    private long document = 0;
    private ArrayList<Long> selectedMessages = new ArrayList<Long>();
    private ArrayList<Long> excludedMessages = new ArrayList<Long>();
    private Button addButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midi_message_picker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.midi_message_picker_list);

        document = getIntent().getExtras().getLong("document", 0);

        addButton = (Button) findViewById(R.id.message_picker_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                if (document > 0) {
                    int result = addDocumentMidiMessages();
                    if (result > 0)
                        setResult(RESULT_OK, data);
                }
                finish();
            }
        });

        cancelButton = (Button) findViewById(R.id.message_picker_cancel);
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

    @Override
    public void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(MainActivity.DOCUMENT_MIDI_MESSAGES_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (i == MainActivity.MIDI_MESSAGES_LOADER)
            return SetlistsDbMidiMessageLoader.newAllMidiMessagesInstance(this);
        else
            return SetlistsDbDocumentMidiMessageLoader.newInstanceForDocumentId(this, document);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == MainActivity.MIDI_MESSAGES_LOADER) {
            MidiMessagePickerActivity.Adapter adapter = new MidiMessagePickerActivity.Adapter(cursor);
            adapter.setHasStableIds(true);
            mRecyclerView.setAdapter(adapter);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
        }
        else if (cursorLoader.getId() == MainActivity.DOCUMENT_MIDI_MESSAGES_LOADER) {
            if (cursor.moveToFirst()) {
                do {
                    excludedMessages.add(cursor.getLong(2));
                } while (cursor.moveToNext());
            }
            getSupportLoaderManager().restartLoader(MainActivity.MIDI_MESSAGES_LOADER, null, this);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == MainActivity.MIDI_MESSAGES_LOADER)
            mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<MidiMessagePickerActivity.ViewHolder> {
        private Cursor mCursor;

        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(0);
        }

        @Override
        public MidiMessagePickerActivity.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_checkable, parent, false);
            final MidiMessagePickerActivity.ViewHolder vh = new MidiMessagePickerActivity.ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(MidiMessagePickerActivity.ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.bindData(mCursor.getLong(0), mCursor.getString(1), mCursor.getInt(2), mCursor.getInt(3), mCursor.getInt(4), mCursor.getInt(5), excludedMessages.contains(mCursor.getLong(0)));
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textName;
        private TextView textDetails;
        private CheckBox checkSelect;

        private long id;
        private String name;
        private int channel;
        private int status;
        private int data1;
        private int data2;
        private boolean disabled = false;

        public void bindData(long id, String name, int channel, int status, int data1, int data2, boolean disabled) {
            this.id = id;
            this.name = name;
            this.channel = channel;
            this.status = status;
            this.data1 = data1;
            this.data2 = data2;
            this.disabled = disabled;

            textName.setText(name);
            String details =  "(" + String.valueOf(channel) + ", " + String.valueOf(data1) + ", " + String.valueOf(data2) + ")";
            switch ((byte)status) {
                case MidiHelper.NOTE_OFF:
                    textDetails.setText(MidiHelper.STATUS_NOTE_OFF.getLabel() + " " + details);
                    break;
                case MidiHelper.NOTE_ON:
                    textDetails.setText(MidiHelper.STATUS_NOTE_ON.getLabel() + " " + details);
                    break;
                case MidiHelper.POLYPHONIC_AFTERTOUCH:
                    textDetails.setText(MidiHelper.STATUS_POLYPHONIC_AFTERTOUCH.getLabel() + " " + details);
                    break;
                case MidiHelper.CONTROL_CHANGE:
                    textDetails.setText(MidiHelper.STATUS_CONTROL_CHANGE.getLabel() + " " + details);
                    break;
                case MidiHelper.PROGRAM_CHANGE:
                    textDetails.setText(MidiHelper.STATUS_PROGRAM_CHANGE.getLabel() + " " + details);
                    break;
                case MidiHelper.CHANNEL_PRESSURE:
                    textDetails.setText(MidiHelper.STATUS_CHANNEL_PRESSURE.getLabel() + " " + details);
                    break;
                case MidiHelper.PITCH_BEND:
                    textDetails.setText(MidiHelper.STATUS_PITCH_BEND.getLabel() + " " + details);
                    break;
            }
            checkSelect.setChecked(selectedMessages.contains(id));
            checkSelect.setEnabled(!this.disabled);
        }

        public ViewHolder(View view) {
            super(view);
            textName = (TextView) view.findViewById(R.id.item_checkable_title);
            textDetails = (TextView) view.findViewById(R.id.item_checkable_subtitle);
            checkSelect = (CheckBox) view.findViewById(R.id.item_checkable_checkbox);

            checkSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        selectedMessages.add(id);
                    else
                        selectedMessages.remove(id);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!disabled)
                        checkSelect.toggle();
                }
            });
        }
    }

    private int addDocumentMidiMessages() {
        for (int i = 0; i < selectedMessages.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DOCUMENT, document);
            values.put(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_AUTOSEND, 0);
            values.put(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_MESSAGE, selectedMessages.get(i));

            Uri uri = getContentResolver().insert(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.CONTENT_URI, values);
        }
        return selectedMessages.size();
    }
}
