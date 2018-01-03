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
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
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

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.data.SetlistsDbDocumentMidiMessageLoader;
import com.raffaelcavaliere.setlists.data.SetlistsDbItemDocumentMidiMessage;
import com.raffaelcavaliere.setlists.ui.MainActivity;
import com.raffaelcavaliere.setlists.utils.FilteredFilePickerFragment;
import com.raffaelcavaliere.setlists.utils.MidiHelper;
import com.raffaelcavaliere.setlists.utils.Storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;


public class ViewerActivity extends AppCompatActivity
        implements ViewerFragment.OnFragmentInteractionListener,
        TransposeDialogFragment.TransposeDialogListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_EDIT_DOCUMENT = 1000;
    private static final int REQUEST_EXPORT_DOCUMENT = 1001;

    private String id;
    private String path;
    private ToggleButton toggleMetronome;
    private ToggleButton toggleMidi;
    private ImageView imageMetronome;
    private TextView textTempo;
    private int tempo;
    private int transpose;
    private int transposeMode = SetlistsDbContract.SetlistsDbDocumentEntry.TRANSPOSE_USE_SHARPS;
    private boolean isMetronomeRunning = false;
    private int type;
    private TextView textNothingToShow;
    private ArrayList<SetlistsDbItemDocumentMidiMessage> midiMessages = new ArrayList<>();

    private final int TEMPO_MAX_DELAY = 1500;
    private final int TEMPO_MIN_DELAY = 250;

    FragmentManager mFragmentManager;
    MidiManager midiManager;
    private ArrayList<MidiInputPort> midiPorts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        setTitle(extras.getString("title"));

        toolbar.setSubtitle(extras.getString("description"));

        id = extras.getString("id");
        path = extras.getString("path", "");
        type = extras.getInt("type", 0);

        tempo = extras.getInt("tempo", 0);
        transpose = extras.getInt("transpose", 0);
        transposeMode = extras.getInt("transposeMode", SetlistsDbContract.SetlistsDbDocumentEntry.TRANSPOSE_USE_SHARPS);

        toggleMetronome = (ToggleButton) findViewById(R.id.viewer_metronome_toggle);
        imageMetronome = (ImageView) findViewById(R.id.viewer_metronome);
        textTempo = (TextView) findViewById(R.id.viewer_footer_tempo);

        textTempo.setText(getResources().getString(R.string.bpm_note) + " " + String.valueOf(tempo));

        toggleMetronome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.viewer_footer).setVisibility(View.VISIBLE);
                    imageMetronome.setImageResource(R.drawable.led_on_96);
                    startMetronome(new Handler());
                } else {
                    findViewById(R.id.viewer_footer).setVisibility(View.GONE);
                    isMetronomeRunning = false;
                    imageMetronome.setImageResource(R.drawable.led_off_96);
                }
            }
        });

        imageMetronome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMetronome.setChecked(false);
            }
        });

        toggleMidi = (ToggleButton) findViewById(R.id.viewer_midi_toggle);
        toggleMidi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.viewer_header).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.viewer_header).setVisibility(View.GONE);
                }
            }
        });

        textNothingToShow = (TextView) findViewById(R.id.text_no_document);

        midiPorts.clear();
        midiManager = (MidiManager) getSystemService(Activity.MIDI_SERVICE);
        final MidiDeviceInfo[] infos = midiManager.getDevices();
        Handler handler = new Handler();
        for (final MidiDeviceInfo deviceInfo : infos) {
            midiManager.openDevice(deviceInfo, new MidiManager.OnDeviceOpenedListener() {
                @Override
                public void onDeviceOpened(MidiDevice device) {
                    if (device == null) {
                        Toast.makeText(getApplicationContext(), "MIDI error: could not open device " + String.valueOf(deviceInfo.getId()), Toast.LENGTH_LONG).show();
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

    private void loadFragment() {
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();

        switch (type) {
            case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_PDF:
                xfragmentTransaction.replace(R.id.document_container, PdfViewerFragment.newInstance(path)).commit();
                break;
            case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_IMAGE:
                xfragmentTransaction.replace(R.id.document_container, ImageViewerFragment.newInstance(path)).commit();
                break;
            case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_CHORDPRO:
                xfragmentTransaction.replace(R.id.document_container, ChordproViewerFragment.newInstance(path, transpose, transposeMode)).commit();
                break;
            case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_TEXT:
                xfragmentTransaction.replace(R.id.document_container, TextViewerFragment.newInstance(path)).commit();
                break;
            default:
                textNothingToShow.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(MainActivity.DOCUMENT_MIDI_MESSAGES_LOADER, null, this);
        loadFragment();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return SetlistsDbDocumentMidiMessageLoader.newInstanceForDocumentId(this, this.id);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == MainActivity.DOCUMENT_MIDI_MESSAGES_LOADER) {
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
            LinearLayout layout = (LinearLayout)findViewById(R.id.viewer_midi_message_container);
            layout.removeAllViews();
            for (final SetlistsDbItemDocumentMidiMessage message : midiMessages) {
                Button messageButton = (Button)getLayoutInflater().inflate(R.layout.button_midi_message, layout, false);
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
                                    Toast.makeText(getApplicationContext(), "Sending MIDI: " + MidiHelper.bytesToHex(buffer), Toast.LENGTH_SHORT).show();
                                    port.send(buffer, offset, numBytes, now);
                                } catch (Exception ex) {
                                    Toast.makeText(getApplicationContext(), "MIDI error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
                layout.addView(messageButton);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (type > 0) {
            int layout = R.menu.document_menu;
            if (type == SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_TEXT)
                layout = R.menu.text_document_menu;
            else if (type == SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_CHORDPRO)
                layout = R.menu.chordpro_document_menu;
            getMenuInflater().inflate(layout, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.file_menu_edit) {
            Intent editDocumentIntent = new Intent(this, TextEditorActivity.class);
            editDocumentIntent.putExtra("path", path);
            startActivityForResult(editDocumentIntent, REQUEST_EDIT_DOCUMENT);
            return true;
        }
        else if (id == R.id.file_menu_export) {
            Intent filePickerIntent = new Intent(this, FilePickerActivity.class);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_NEW_FILE);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().toString());

            startActivityForResult(filePickerIntent, REQUEST_EXPORT_DOCUMENT);
        }
        else if (id == R.id.file_menu_transpose) {
            DialogFragment dialog = new TransposeDialogFragment();
            Bundle args = new Bundle();
            args.putInt("transpose", transpose);
            args.putInt("mode", transposeMode);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "dialog");
        }
        else if (id == R.id.file_menu_share) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            String temp_path = path;
            switch (type) {
                case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_IMAGE:
                    try {
                        temp_path = getFilesDir().getPath() + "/temp/" + getTitle() + FilteredFilePickerFragment.EXTENSION_PNG;
                        Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(path));
                        FileOutputStream dest = new FileOutputStream(temp_path);
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, dest);
                    } catch (Exception ex) {
                        Log.d("ERROR DECODING STREAM", ex.toString());
                    }

                    shareIntent.setType("image/png");
                    break;
                case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_PDF:
                    try {
                        temp_path = getFilesDir().getPath() + "/temp/" + getTitle() + FilteredFilePickerFragment.EXTENSION_PDF;
                        Storage.copy(new File(path), new File(temp_path));
                    } catch (Exception ex) {
                        Log.d("ERROR COPYING FILE", ex.toString());
                    }
                    shareIntent.setType("application/pdf");
                    break;
                case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_CHORDPRO:
                    try {
                        temp_path = getFilesDir().getPath() + "/temp/" + getTitle() + FilteredFilePickerFragment.EXTENSION_PRO;
                        Storage.copy(new File(path), new File(temp_path));
                    } catch (Exception ex) {
                        Log.d("ERROR COPYING FILE", ex.toString());
                    }
                    shareIntent.setType("text/*");
                    break;
                case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_TEXT:
                    try {
                        temp_path = getFilesDir().getPath() + "/temp/" + getTitle() + FilteredFilePickerFragment.EXTENSION_TXT;
                        Storage.copy(new File(path), new File(temp_path));
                    } catch (Exception ex) {
                        Log.d("ERROR COPYING FILE", ex.toString());
                    }
                    shareIntent.setType("text/*");
                    break;
            }
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, "com.raffaelcavaliere.setlists.provider", new File(temp_path)));
            startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.menu_share_file)));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTransposeDialogResult(int transpose, int mode) {
        this.transpose = transpose;
        this.transposeMode = mode;

        try {
            ContentValues values = new ContentValues();
            values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TRANSPOSE, this.transpose);
            values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TRANSPOSE_MODE, this.transposeMode);
            values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

            getContentResolver().update(SetlistsDbContract.SetlistsDbDocumentEntry.buildSetlistsDbDocumentUri(id), values,
                    SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + "=?", new String[]{String.valueOf(id)});

            loadFragment();
        } catch (Exception ex) {
            Log.d("ERROR SAVING VERSION", ex.toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_EDIT_DOCUMENT:
                if (resultCode == RESULT_OK) {
                    String returnedResult = data.getData().toString();
                    Log.d("RETURNED RESULT", returnedResult);
                }
                break;
            case REQUEST_EXPORT_DOCUMENT:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Log.d("RETURNED RESULT", uri.toString());
                    if (uri.getPath().length() > 0) {
                        String extension = "";
                        switch (type) {
                            case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_PDF:
                                extension = FilteredFilePickerFragment.EXTENSION_PDF;
                                break;
                            case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_IMAGE:
                                extension = FilteredFilePickerFragment.EXTENSION_PNG;
                                break;
                            case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_CHORDPRO:
                                extension = FilteredFilePickerFragment.EXTENSION_PRO;
                                break;
                            case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_TEXT:
                                extension = FilteredFilePickerFragment.EXTENSION_TXT;
                                break;
                        }
                        String dest_path = uri.getPath().replace("/root", "");
                        int i = dest_path.lastIndexOf(".");
                        if (i >= 0) {
                            String user_extension = dest_path.substring(i);
                            if (!user_extension.equalsIgnoreCase(extension))
                                dest_path = dest_path.replace(user_extension, extension);
                        }
                        else {
                            dest_path = dest_path.concat(extension);
                        }

                        File src = new File(path);

                        if (type == SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_IMAGE) {
                            try {
                                Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(src));
                                FileOutputStream dest = new FileOutputStream(dest_path);
                                bmp.compress(Bitmap.CompressFormat.PNG, 100, dest);
                            } catch (Exception ex) {
                                Log.d("ERROR DECODING STREAM", ex.toString());
                            }
                        }
                        else {
                            try {
                                Storage.copy(src, new File(dest_path));
                            } catch (Exception ex) {
                                Log.d("ERROR COPY FILE", ex.toString());
                            }
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("FRAGMENT INTERACTION", uri.toString());
    }

}
