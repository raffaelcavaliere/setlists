package com.raffaelcavaliere.setlists.ui.library;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.data.SetlistsDbDocumentMidiMessageLoader;
import com.raffaelcavaliere.setlists.ui.MainActivity;
import com.raffaelcavaliere.setlists.ui.document.ViewerActivity;
import com.raffaelcavaliere.setlists.ui.document.TextEditorActivity;
import com.raffaelcavaliere.setlists.ui.midi.MidiMessageEditActivity;
import com.raffaelcavaliere.setlists.ui.midi.MidiMessagePickerActivity;
import com.raffaelcavaliere.setlists.utils.FilteredFilePickerFragment;
import com.raffaelcavaliere.setlists.utils.MidiHelper;
import com.raffaelcavaliere.setlists.utils.Storage;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class DocumentEditActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private TextView textDescription;
    private ImageButton viewButton;
    private ImageButton editButton;
    private ImageButton browseButton;
    private ImageView imageDocument;
    private TextView textNoDocument;
    private Button saveButton;
    private Button cancelButton;
    private ImageButton addMessageButton;
    private CheckBox checkboxIsPreferred;
    private int type;
    private String title;
    private File fileToImport;
    private String id;
    private String song;
    private boolean isPreferred;
    private int tempo;
    private String description;

    private final int REQUEST_SELECT_FILE = 1003;
    private final int REQUEST_EDIT_MIDI_MESSAGE = 1004;
    private final int REQUEST_ADD_MIDI_MESSAGE = 1005;

    private int mode;
    public final static String EXTRA_MODE = "mode";
    public final static int EXTRA_MODE_NEW = 0;
    public final static int EXTRA_MODE_EDIT = 1;
    public final static int EXTRA_MODE_DUPLICATE = 2;

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        mode = extras.getInt(EXTRA_MODE, 0);

        song = getIntent().getExtras().getString("song", null);
        isPreferred = getIntent().getExtras().getBoolean("isPreferred", true);
        id = getIntent().getExtras().getString("id", null);
        title = getIntent().getExtras().getString("title", "");
        tempo = getIntent().getExtras().getInt("tempo", 0);

        textDescription = (TextView) findViewById(R.id.editDocumentDescription);
        description = extras.getString("description", "");
        textDescription.setText(description);

        textNoDocument = (TextView) findViewById(R.id.editDocumentNoFileSelectedLabel);
        imageDocument = (ImageView) findViewById(R.id.editDocumentFileIcon);
        viewButton = (ImageButton) findViewById(R.id.editDocumentFileView);
        editButton = (ImageButton) findViewById(R.id.editDocumentFileEdit);
        browseButton = (ImageButton) findViewById(R.id.editDocumentFileBrowse);
        addMessageButton = (ImageButton) findViewById(R.id.editDocumentAddMidiMessage);
        checkboxIsPreferred = (CheckBox) findViewById(R.id.editDocumentIsPreferred);

        type = extras.getInt("type", 0);

        if ((mode == EXTRA_MODE_EDIT || mode == EXTRA_MODE_DUPLICATE) && type > 0) {
            String path = getFilesDir().getPath() + "/" + String.valueOf(id);
            if (new File(path).exists()) {
                setDocumentIconAndButtons();
                if (mode == EXTRA_MODE_DUPLICATE)
                    fileToImport = new File(path);
            }
        }

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent documentIntent = new Intent(v.getContext(), ViewerActivity.class);
                if (fileToImport != null)
                    documentIntent.putExtra("path", fileToImport.getPath());
                else if (id != null)
                    documentIntent.putExtra("path", getFilesDir().getPath() + "/" + String.valueOf(id));

                documentIntent.putExtra("type", type);
                documentIntent.putExtra("song", song);
                documentIntent.putExtra("title", title);
                documentIntent.putExtra("tempo", tempo);
                documentIntent.putExtra("description", textDescription.getText().toString());
                startActivity(documentIntent);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editDocumentIntent = new Intent(v.getContext(), TextEditorActivity.class);
                if (fileToImport != null)
                    editDocumentIntent.putExtra("path", fileToImport.getPath());
                else if (id != null)
                    editDocumentIntent.putExtra("path", getFilesDir().getPath() + "/" + id);
                startActivity(editDocumentIntent);
            }
        });

        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent filePickerIntent = new Intent(v.getContext(), FilePickerActivity.class);
                filePickerIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                filePickerIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                filePickerIntent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                filePickerIntent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());

                startActivityForResult(filePickerIntent, REQUEST_SELECT_FILE);
            }
        });

        checkboxIsPreferred.setChecked(isPreferred);

        addMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent midiMessagePickerIntent = new Intent(v.getContext(), MidiMessagePickerActivity.class);
                midiMessagePickerIntent.putExtra("document", id);
                startActivityForResult(midiMessagePickerIntent, REQUEST_ADD_MIDI_MESSAGE);
            }
        });

        saveButton = (Button) findViewById(R.id.editDocumentSave);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (textDescription.getText().length() > 0) {
                    Intent data = new Intent();

                    if (mode == EXTRA_MODE_EDIT) {
                        int result = updateDocument(textDescription.getText().toString(), type);
                        if (fileToImport != null) {
                            importFile();
                        }
                    } else {
                        Uri result = addDocument(textDescription.getText().toString(), type);
                        data.setData(result);
                        id = result.getLastPathSegment();
                        if (fileToImport != null) {
                            importFile();
                        }
                    }
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide a description", Toast.LENGTH_SHORT).show();
                }

            }
        });

        cancelButton = (Button) findViewById(R.id.editDocumentCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.documentMidiMessageList);

        if (mode == EXTRA_MODE_NEW || mode == EXTRA_MODE_DUPLICATE) {
            mRecyclerView.setVisibility(View.GONE);
            addMessageButton.setVisibility(View.GONE);
            ((TextView) findViewById(R.id.editDocumentMidiMessagesLabel)).setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(MainActivity.DOCUMENT_MIDI_MESSAGES_LOADER, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDocumentIconAndButtons () {
        textNoDocument.setVisibility(View.INVISIBLE);
        switch (type) {
            case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_PDF:
                imageDocument.setImageResource(R.drawable.pdf_icon);
                break;
            case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_IMAGE:
                imageDocument.setImageResource(R.drawable.image_icon);
                break;
            case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_CHORDPRO:
                imageDocument.setImageResource(R.drawable.chordpro_icon);
                editButton.setVisibility(View.VISIBLE);
                break;
            default:
                imageDocument.setImageResource(R.drawable.text_icon);
                editButton.setVisibility(View.VISIBLE);
        }
        imageDocument.setVisibility(View.VISIBLE);
        viewButton.setVisibility(View.VISIBLE);
    }

    private void importFile () {
        try {
            Storage.copy(fileToImport, new File(getFilesDir().getPath() + "/" + id));
            Log.d("IMPORT", fileToImport.getPath() + " > " + getFilesDir().getPath() + "/" + String.valueOf(id));
        } catch (IOException ex) {
            Log.d("EXCEPTION", ex.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_FILE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri.getPath().length() > 0) {
                int dot = uri.getLastPathSegment().lastIndexOf(".");
                if (dot < 0) {
                    Toast.makeText(this, "File type is not supported", Toast.LENGTH_SHORT).show();
                    return;
                }
                String extension = uri.getLastPathSegment().substring(dot);
                if (extension.equalsIgnoreCase(FilteredFilePickerFragment.EXTENSION_PDF))
                    type = SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_PDF;
                else if (extension.equalsIgnoreCase(FilteredFilePickerFragment.EXTENSION_JPG) ||
                         extension.equalsIgnoreCase(FilteredFilePickerFragment.EXTENSION_BMP) ||
                         extension.equalsIgnoreCase(FilteredFilePickerFragment.EXTENSION_PNG) ||
                         extension.equalsIgnoreCase(FilteredFilePickerFragment.EXTENSION_GIF))
                    type = SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_IMAGE;
                else if (extension.equalsIgnoreCase(FilteredFilePickerFragment.EXTENSION_PRO))
                    type = SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_CHORDPRO;
                else if (extension.equalsIgnoreCase(FilteredFilePickerFragment.EXTENSION_TXT))
                    type = SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_TEXT;
                else
                {
                    Toast.makeText(this, "File type is not supported", Toast.LENGTH_SHORT).show();
                    return;
                }
                fileToImport = new File(uri.getPath().replace("/root", ""));
                setDocumentIconAndButtons();
                textDescription.setText(uri.getLastPathSegment().substring(0, dot));
            }
        } else if (requestCode == REQUEST_ADD_MIDI_MESSAGE && resultCode == RESULT_OK) {
            Log.d("RETURNED RESULT", "OK ADD MIDI MESSAGE");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return SetlistsDbDocumentMidiMessageLoader.newInstanceForDocumentId(this, this.id);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == MainActivity.DOCUMENT_MIDI_MESSAGES_LOADER) {
            DocumentEditActivity.Adapter adapter = new DocumentEditActivity.Adapter(cursor);
            adapter.setHasStableIds(true);
            mRecyclerView.setAdapter(adapter);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == MainActivity.DOCUMENT_MIDI_MESSAGES_LOADER)
            mRecyclerView.setAdapter(null);
    }

    private Uri addDocument(String description, int type) {
        ContentValues values = new ContentValues();
        id = UUID.randomUUID().toString();
        values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID, id);
        values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DESCRIPTION, description);
        values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_SONG, song);
        values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_AUTHOR, "raffaelcavaliere");
        values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TYPE, type <= 0 ? null : type);
        values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_ADDED, new Date().getTime() / 1000);
        values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

        Uri result = getContentResolver().insert(SetlistsDbContract.SetlistsDbDocumentEntry.CONTENT_URI, values);

        if (isPreferred && checkboxIsPreferred.isChecked() == false)
            unsetDocumentAsPreferred();
        else if (checkboxIsPreferred.isChecked())
            setDocumentAsPreferred();

        return result;
    }

    private int updateDocument(String description, int type) {
        ContentValues values = new ContentValues();
        values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DESCRIPTION, description);
        values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TYPE, type <= 0 ? null : type);
        values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

        int result = getContentResolver().update(SetlistsDbContract.SetlistsDbDocumentEntry.buildSetlistsDbDocumentUri(id), values,
                SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});

        if (isPreferred && checkboxIsPreferred.isChecked() == false)
            unsetDocumentAsPreferred();
        else if (checkboxIsPreferred.isChecked())
            setDocumentAsPreferred();

        return result;
    }

    private void setDocumentAsPreferred() {
        ContentValues values = new ContentValues();
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT, id);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);
        if (getContentResolver().update(SetlistsDbContract.SetlistsDbSongEntry.buildSetlistsDbSongUri(song),
                values, SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + "=?", new String[] {String.valueOf(song)}) > 0) {
            isPreferred = true;
        }
    }

    private void unsetDocumentAsPreferred() {
        ContentValues values = new ContentValues();
        values.putNull(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);
        if (getContentResolver().update(SetlistsDbContract.SetlistsDbSongEntry.buildSetlistsDbSongUri(song),
                values, SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + "=?", new String[] {String.valueOf(song)}) > 0) {
            isPreferred = false;
        }
    }

    public class Adapter extends RecyclerView.Adapter<DocumentEditActivity.ViewHolder> {
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
        public DocumentEditActivity.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_details, parent, false);
            final DocumentEditActivity.ViewHolder vh = new DocumentEditActivity.ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(DocumentEditActivity.ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.bindData(mCursor.getString(0), mCursor.getString(2), mCursor.getInt(3), mCursor.getString(4),
                    mCursor.getInt(5), mCursor.getInt(6), mCursor.getInt(7), mCursor.getInt(8));
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textName;
        private TextView textAutosend;
        private TextView textStatus;
        private TextView textData1;
        private TextView textData2;
        private TextView textChannel;
        private ImageButton btnMenu;

        private String id;
        private String message;
        private int autosend;
        private String name;
        private int channel;
        private int status;
        private int data1;
        private int data2;

        public void bindData(String id, String message, int autosend, String name, int channel, int status, int data1, int data2) {
            this.id = id;
            this.message = message;
            this.autosend = autosend;
            this.name = name;
            this.channel = channel;
            this.status = status;
            this.data1 = data1;
            this.data2 = data2;
            textName.setText(name);
            textChannel.setText("Channel " + String.valueOf(channel));
            switch ((byte)status) {
                case MidiHelper.NOTE_OFF:
                    textStatus.setText(MidiHelper.STATUS_NOTE_OFF.getLabel());
                    break;
                case MidiHelper.NOTE_ON:
                    textStatus.setText(MidiHelper.STATUS_NOTE_ON.getLabel());
                    break;
                case MidiHelper.POLYPHONIC_AFTERTOUCH:
                    textStatus.setText(MidiHelper.STATUS_POLYPHONIC_AFTERTOUCH.getLabel());
                    break;
                case MidiHelper.CONTROL_CHANGE:
                    textStatus.setText(MidiHelper.STATUS_CONTROL_CHANGE.getLabel());
                    break;
                case MidiHelper.PROGRAM_CHANGE:
                    textStatus.setText(MidiHelper.STATUS_PROGRAM_CHANGE.getLabel());
                    break;
                case MidiHelper.CHANNEL_PRESSURE:
                    textStatus.setText(MidiHelper.STATUS_CHANNEL_PRESSURE.getLabel());
                    break;
                case MidiHelper.PITCH_BEND:
                    textStatus.setText(MidiHelper.STATUS_PITCH_BEND.getLabel());
                    break;
            }
            textData1.setText(String.valueOf(data1));
            if (data2 > 0)
                textData2.setText(String.valueOf(data2));
            else
                textData2.setVisibility(View.INVISIBLE);

            if (autosend > 0)
                textAutosend.setVisibility(View.VISIBLE);
        }

        public ViewHolder(View view) {
            super(view);
            textName = (TextView) view.findViewById(R.id.item_details_title);
            textAutosend = (TextView) view.findViewById(R.id.item_details_title_super);
            textStatus = (TextView) view.findViewById(R.id.item_details_subtitle);
            textChannel = (TextView) view.findViewById(R.id.item_details_comment_left);
            textData1 = (TextView) view.findViewById(R.id.item_details_comment_center_left);
            textData1.setVisibility(View.VISIBLE);
            textData2 = (TextView) view.findViewById(R.id.item_details_comment_center_right);
            textData2.setVisibility(View.VISIBLE);
            btnMenu = (ImageButton) view.findViewById(R.id.item_details_menu);
            ((TextView) view.findViewById(R.id.item_details_comment_right)).setVisibility(View.GONE);

            textAutosend.setText(getString(R.string.document_midi_message_autosend));

            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    popupMenu.getMenuInflater().inflate(R.menu.document_midi_message_context_menu, popupMenu.getMenu());
                    if (autosend > 0)
                        popupMenu.getMenu().removeItem(R.id.document_midi_message_context_menu_set_autosend);
                    else
                        popupMenu.getMenu().removeItem(R.id.document_midi_message_context_menu_remove_autosend);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            ContentValues values;
                            switch (item.getItemId()) {
                                case R.id.document_midi_message_context_menu_set_autosend:
                                    values = new ContentValues();
                                    values.put(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_AUTOSEND, 1);
                                    if (getContentResolver().update(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.buildSetlistsDbDocumentMidiMessageUri(id),
                                            values, SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)}) > 0) {
                                        autosend = 1;
                                        getSupportLoaderManager().restartLoader(MainActivity.DOCUMENT_MIDI_MESSAGES_LOADER, null, DocumentEditActivity.this);
                                    }
                                    return true;
                                case R.id.document_midi_message_context_menu_remove_autosend:
                                    values = new ContentValues();
                                    values.put(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_AUTOSEND, 0);
                                    if (getContentResolver().update(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.buildSetlistsDbDocumentMidiMessageUri(id),
                                            values, SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)}) > 0) {
                                        autosend = 0;
                                        getSupportLoaderManager().restartLoader(MainActivity.DOCUMENT_MIDI_MESSAGES_LOADER, null, DocumentEditActivity.this);
                                    }
                                    return true;
                                case R.id.document_midi_message_context_menu_edit:
                                    Intent editMidiMessageIntent = new Intent(v.getContext(), MidiMessageEditActivity.class);
                                    editMidiMessageIntent.putExtra("id", message);
                                    editMidiMessageIntent.putExtra("name", name);
                                    editMidiMessageIntent.putExtra("channel", channel);
                                    editMidiMessageIntent.putExtra("status", status);
                                    editMidiMessageIntent.putExtra("data1", data1);
                                    editMidiMessageIntent.putExtra("data2", data2);
                                    editMidiMessageIntent.putExtra(MidiMessageEditActivity.EXTRA_MODE, MidiMessageEditActivity.EXTRA_MODE_EDIT);
                                    startActivityForResult(editMidiMessageIntent, REQUEST_EDIT_MIDI_MESSAGE);
                                    return true;
                                case R.id.document_midi_message_context_menu_remove:
                                    new AlertDialog.Builder(v.getContext())
                                            .setTitle(getResources().getString(R.string.menu_remove_document_midi_message))
                                            .setMessage(getResources().getString(R.string.dialog_remove_document_midi_message))
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    if (getContentResolver().delete(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.buildSetlistsDbDocumentMidiMessageUri(id),
                                                            SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)}) > 0) {
                                                        getSupportLoaderManager().restartLoader(MainActivity.DOCUMENT_MIDI_MESSAGES_LOADER, null, DocumentEditActivity.this);
                                                    }
                                                }})
                                            .setNegativeButton(android.R.string.no, null)
                                            .show();
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.show();
                }
            });
        }
    }
}
