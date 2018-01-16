package com.raffaelcavaliere.setlists.ui.document;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.data.SetlistsDbDocumentMidiMessageLoader;
import com.raffaelcavaliere.setlists.data.SetlistsDbItemDocumentMidiMessage;
import com.raffaelcavaliere.setlists.data.SetlistsDbItemSetSong;
import com.raffaelcavaliere.setlists.data.SetlistsDbSetSongLoader;
import com.raffaelcavaliere.setlists.ui.MainActivity;
import com.raffaelcavaliere.setlists.ui.library.DocumentEditActivity;
import com.raffaelcavaliere.setlists.utils.FilteredFilePickerFragment;
import com.raffaelcavaliere.setlists.utils.MidiHelper;
import com.raffaelcavaliere.setlists.utils.Storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;


public class SwipeViewerActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ViewerFragment.OnFragmentInteractionListener,
        TransposeDialogFragment.TransposeDialogListener {

    private ViewPager viewPager;
    private PagerTitleStrip titleStrip;
    private Toolbar toolbar;
    private String set;
    private int startPosition;
    private Adapter fragmentAdapter;
    private ToggleButton toggleMetronome;
    private ToggleButton toggleMidi;
    private ImageView imageMetronome;
    private TextView textTempo;
    private int tempo = 0;
    private boolean isMetronomeRunning = false;

    private final int TEMPO_MAX_DELAY = 1500;
    private final int TEMPO_MIN_DELAY = 250;
    private final int REQUEST_EDIT_DOCUMENT = 1002;
    private final int REQUEST_EDIT_FILE = 1003;

    private ArrayList<SetlistsDbItemSetSong> songs = new ArrayList<>();
    private ArrayList<SetlistsDbItemDocumentMidiMessage> midiMessages = new ArrayList<>();
    FragmentManager mFragmentManager;
    MidiManager midiManager;
    private ArrayList<MidiInputPort> midiPorts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_viewer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        setTitle(extras.getString("title"));
        toolbar.setSubtitle(extras.getString("description"));

        set = extras.getString("set");
        startPosition = extras.getInt("position", 0);

        if (savedInstanceState != null) {
            startPosition = savedInstanceState.getInt("position", 0);
        }

        viewPager = (ViewPager) findViewById(R.id.swipe_viewer_pager);
        fragmentAdapter = new Adapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentAdapter);

        this.viewPager.clearOnPageChangeListeners();
        this.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                startPosition = position;
                SetlistsDbItemSetSong selected = songs.get(position);
                setSongLayout(selected);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        titleStrip = (PagerTitleStrip) findViewById(R.id.swipe_viewer_pager_title_strip);
        titleStrip.setTextSpacing(150);

        toggleMetronome = (ToggleButton) findViewById(R.id.swipe_viewer_metronome_toggle);
        imageMetronome = (ImageView) findViewById(R.id.swipe_viewer_metronome);
        textTempo = (TextView) findViewById(R.id.swipe_viewer_footer_tempo);

        textTempo.setText(getResources().getString(R.string.bpm_note) + " " + String.valueOf(tempo));

        toggleMetronome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.swipe_viewer_footer).setVisibility(View.VISIBLE);
                    imageMetronome.setImageResource(R.drawable.led_on_96);
                    startMetronome(new Handler());
                }
                else {
                    findViewById(R.id.swipe_viewer_footer).setVisibility(View.GONE);
                    isMetronomeRunning = false;
                    imageMetronome.setImageResource(R.drawable.led_off_96);
                }
            }
        });

        toggleMidi = (ToggleButton) findViewById(R.id.swipe_viewer_midi_toggle);
        toggleMidi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.swipe_viewer_header).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.swipe_viewer_header).setVisibility(View.GONE);
                }
            }
        });

        imageMetronome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMetronome.setChecked(false);
            }
        });

        midiPorts.clear();
        midiManager = (MidiManager) getSystemService(Activity.MIDI_SERVICE);
        final MidiDeviceInfo[] infos = midiManager.getDevices();
        Handler handler = new Handler();
        for (final MidiDeviceInfo deviceInfo : infos) {
            midiManager.openDevice(deviceInfo, new MidiManager.OnDeviceOpenedListener() {
                @Override
                public void onDeviceOpened(MidiDevice device) {
                    if (device == null) {
                        Toast.makeText(getApplicationContext(), getResources().getText(R.string.toast_midi_device_error) + " " + String.valueOf(deviceInfo.getId()), Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            for (int i = 0; i < device.getInfo().getInputPortCount(); i++) {
                                MidiInputPort inputPort = device.openInputPort(i);
                                if (inputPort != null) {
                                    midiPorts.add(inputPort);
                                    break;
                                }
                            }
                        }
                        catch(Exception ex) {
                            Log.d("MIDI ERROR", ex.getMessage());
                        }
                    }
                }
            }, handler);
        }

        getSupportLoaderManager().restartLoader(MainActivity.SET_SONGS_LOADER, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", startPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            startPosition = savedInstanceState.getInt("position", 0);
        super.onRestoreInstanceState(savedInstanceState);
    }

    void startMetronome(final Handler handler) {
        isMetronomeRunning = true;
        new Runnable() {
            public void run() {
                if (isMetronomeRunning) {
                    imageMetronome.setImageResource(R.drawable.led_on_96);
                    blinkMetronome(handler, this);
                }
            }
        }.run();
    }

    void blinkMetronome(Handler handler, Runnable runnable) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                imageMetronome.setImageResource(R.drawable.led_off_96);
            }
        }, 100);
        if (isMetronomeRunning)
            handler.postDelayed(runnable, getMetronomeDelay());
    }

    int getMetronomeDelay() {
        int delay = 60000 / (tempo > 0 ? tempo : 1);
        if (delay > TEMPO_MAX_DELAY)
            return TEMPO_MAX_DELAY;
        if (delay < TEMPO_MIN_DELAY)
            return TEMPO_MIN_DELAY;
        return delay;
    }

    @Override
    public void onResume() {
        super.onResume();
     //   getSupportLoaderManager().restartLoader(MainActivity.SET_SONGS_LOADER, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (MidiInputPort port : midiPorts) {
            try {
                port.close();
            } catch (Exception ex) {
                Log.d("MIDI ERROR", ex.getMessage());
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (i == MainActivity.SET_SONGS_LOADER) {
            return SetlistsDbSetSongLoader.newInstanceForSetId(this, set);
        } else {
            return SetlistsDbDocumentMidiMessageLoader.newInstanceForSetId(this, set);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == MainActivity.SET_SONGS_LOADER) {
            toolbar.setSubtitle(cursor.getCount() <= 0 ? "No song" : String.valueOf(cursor.getCount()) + " song" + (cursor.getCount() > 1 ? "s" : ""));

            songs.clear();
            int sequence = 1;
            if (cursor.moveToFirst()) {
                do {
                    songs.add(new SetlistsDbItemSetSong(cursor.getString(0), cursor.getString(1), cursor.getString(2), sequence,
                            cursor.getString(4), cursor.getInt(5), cursor.getString(6), cursor.getString(7),
                            cursor.getString(8), cursor.getString(9), cursor.getInt(10), cursor.getInt(11), cursor.getInt(12),
                            cursor.getInt(13), cursor.getString(14)));
                    sequence++;
                }
                while (cursor.moveToNext());
            }

            this.fragmentAdapter.setData(songs);
            this.fragmentAdapter.notifyDataSetChanged();

            if (startPosition > 0) {
                this.viewPager.setCurrentItem(startPosition, false);
            }

            getSupportLoaderManager().restartLoader(MainActivity.DOCUMENT_MIDI_MESSAGES_LOADER, null, this);

        } else if (cursorLoader.getId() == MainActivity.DOCUMENT_MIDI_MESSAGES_LOADER) {
            midiMessages.clear();
            if (cursor.moveToFirst()) {
                do {
                    midiMessages.add(new SetlistsDbItemDocumentMidiMessage(
                            cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getInt(8),
                            cursor.getInt(3))
                    );
                } while (cursor.moveToNext());
            }
            setSongLayout(songs.get(startPosition));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == MainActivity.SET_SONGS_LOADER)
            ;
    }

    public void setSongLayout(SetlistsDbItemSetSong selected) {
        tempo = selected.getTempo();
        textTempo.setText(getResources().getString(R.string.bpm_note) + " " + String.valueOf(tempo));
        LinearLayout layout = (LinearLayout)findViewById(R.id.swipe_viewer_midi_message_container);
        layout.removeAllViews();
        for (final SetlistsDbItemDocumentMidiMessage message : midiMessages) {
            if (message.getDocument() == selected.getDocument()) {
                Button messageButton = (Button) getLayoutInflater().inflate(R.layout.button_midi_message, layout, false);
                messageButton.setText(message.getName());
                messageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
                            for (MidiInputPort port : midiPorts) {
                                byte[] buffer = new byte[3];
                                int numBytes = 0;
                                buffer[numBytes++] = (byte) (message.getStatus() + (message.getChannel() > 0 ? message.getChannel() - 1 : 0));
                                buffer[numBytes++] = (byte) (message.getData1() > 0 ? message.getData1() - 1 : 0);
                                if (message.getData2() > 0)
                                    buffer[numBytes++] = (byte) (message.getData2() > 0 ? message.getData2() - 1 : 0);
                                int offset = 0;
                                long now = System.nanoTime();
                                try {
                                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.toast_sending_midi) + " " + MidiHelper.bytesToHex(buffer), Toast.LENGTH_SHORT).show();
                                    port.send(buffer, offset, numBytes, now);
                                } catch (Exception ex) {
                                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.toast_midi_error) + " " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
                layout.addView(messageButton);
                if (message.getAutosend() > 0)
                    messageButton.callOnClick();
            }
        }
    }

    class Adapter extends FragmentStatePagerAdapter {

        private ArrayList<SetlistsDbItemSetSong> items = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void setData(ArrayList<SetlistsDbItemSetSong> items) {
            this.items.clear();
            this.items.addAll(items);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position)
        {
            SetlistsDbItemSetSong item = this.items.get(position);
            switch (item.getType()) {
                case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_PDF:
                    return PdfViewerFragment.newInstance(getFilesDir().getPath() + "/" + String.valueOf(item.getDocument()));
                case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_IMAGE:
                    return ImageViewerFragment.newInstance(getFilesDir().getPath() + "/" + String.valueOf(item.getDocument()));
                case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_CHORDPRO:
                    return ChordproViewerFragment.newInstance(
                            getFilesDir().getPath() + "/" + String.valueOf(item.getDocument()),
                            item.getTranspose(),
                            item.getTranposeMode());
                case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_TEXT:
                    return TextViewerFragment.newInstance(getFilesDir().getPath() + "/" + String.valueOf(item.getDocument()));
                default:
                    return TextViewerFragment.newInstance(getFilesDir().getPath() + "/" + String.valueOf(item.getDocument()));
            }
        }

        @Override
        public int getCount() {
            return this.items.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            SetlistsDbItemSetSong item = this.items.get(position);
            return item.getTitle();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.swipe_viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        SetlistsDbItemSetSong selected = fragmentAdapter.items.get(viewPager.getCurrentItem());
        int transpose = selected.getTranspose();
        int transposeMode = selected.getTranposeMode();
        int type = selected.getType();

        if (id == R.id.file_menu_transpose && type == SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_CHORDPRO) {
            DialogFragment dialog = new TransposeDialogFragment();
            Bundle args = new Bundle();
            args.putInt("transpose", transpose);
            args.putInt("mode", transposeMode);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "dialog");
        } else if (id == R.id.file_menu_transpose) {
            Toast.makeText(this, getResources().getText(R.string.toast_document_cannot_be_transposed), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.document_context_menu_edit && type > 0) {
            Intent editDocumentIntent = new Intent(this, DocumentEditActivity.class);
            editDocumentIntent.putExtra(DocumentEditActivity.EXTRA_MODE, DocumentEditActivity.EXTRA_MODE_EDIT);
            editDocumentIntent.putExtra("id", selected.getDocument());
            editDocumentIntent.putExtra("song", selected.getSong());
            editDocumentIntent.putExtra("title", selected.getTitle());
            editDocumentIntent.putExtra("description", selected.getDescription());
            editDocumentIntent.putExtra("type", selected.getType());
            editDocumentIntent.putExtra("tempo", selected.getTempo());
            startActivityForResult(editDocumentIntent, REQUEST_EDIT_DOCUMENT);
            return true;
        } else if (id == R.id.document_context_menu_edit) {
            Toast.makeText(this, getResources().getText(R.string.toast_document_cannot_be_edited), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.file_menu_edit &&
                (selected.getType() == SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_TEXT ||
                 selected.getType() == SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_CHORDPRO)) {
            Intent editDocumentIntent = new Intent(this, TextEditorActivity.class);
            editDocumentIntent.putExtra("path", getFilesDir().getPath() + "/" + String.valueOf(selected.getDocument()));
            startActivityForResult(editDocumentIntent, REQUEST_EDIT_FILE);
            return true;
        } else if (id == R.id.file_menu_edit) {
            Toast.makeText(this, getResources().getText(R.string.toast_file_cannot_be_edited), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onTransposeDialogResult(int transpose, int mode) {
        try {
            SetlistsDbItemSetSong selected = fragmentAdapter.items.get(viewPager.getCurrentItem());
            ContentValues values = new ContentValues();
            values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TRANSPOSE, transpose);
            values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TRANSPOSE_MODE, mode);
            values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

            getContentResolver().update(SetlistsDbContract.SetlistsDbDocumentEntry.buildSetlistsDbDocumentUri(selected.getDocument()), values,
                    SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + "=?", new String[]{String.valueOf(selected.getDocument())});

            getSupportLoaderManager().restartLoader(MainActivity.SET_SONGS_LOADER, null, this);
        } catch (Exception ex) {
            Log.d("ERROR SAVING VERSION", ex.toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_EDIT_DOCUMENT:
                if (resultCode == RESULT_OK) {
                    Log.d("RETURNED RESULT", "EDIT DOCUMENT OK");
                    getSupportLoaderManager().restartLoader(MainActivity.SET_SONGS_LOADER, null, this);
                }
                break;
            case REQUEST_EDIT_FILE:
                if (resultCode == RESULT_OK) {
                    String returnedResult = data.getData().toString();
                    Log.d("RETURNED RESULT", returnedResult);
                    getSupportLoaderManager().restartLoader(MainActivity.SET_SONGS_LOADER, null, this);
                }
                break;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("FRAGMENT INTERACTION", uri.toString());
    }

}
