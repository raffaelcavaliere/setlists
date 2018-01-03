package com.raffaelcavaliere.setlists.ui.library;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ImageButton;
import android.widget.TextView;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.data.SetlistsDbDocumentLoader;
import com.raffaelcavaliere.setlists.ui.MainActivity;
import com.raffaelcavaliere.setlists.ui.document.ViewerActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DocumentsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private TextView textNothingToShow;
    private String song;
    private String preferred;
    private int tempo;
    private int position = 0;

    private static final int REQUEST_ADD_DOCUMENT = 3000;
    private static final int REQUEST_EDIT_DOCUMENT = 3001;
    private static final int REQUEST_DUPLICATE_DOCUMENT = 3002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        setTitle(extras.getString("title"));

        song = extras.getString("song");
        preferred = extras.getString("preferred", null);
        tempo = extras.getInt("tempo", 0);

        mRecyclerView = (RecyclerView) findViewById(R.id.document_list);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab_add_document);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newVersionIntent = new Intent(view.getContext(), DocumentEditActivity.class);
                newVersionIntent.putExtra(DocumentEditActivity.EXTRA_MODE, DocumentEditActivity.EXTRA_MODE_NEW);
                newVersionIntent.putExtra("song", song);
                newVersionIntent.putExtra("isPreferred", false);
                startActivityForResult(newVersionIntent, REQUEST_ADD_DOCUMENT);
            }
        });
        textNothingToShow = (TextView) findViewById(R.id.text_no_document);
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
        getSupportLoaderManager().restartLoader(MainActivity.DOCUMENTS_LOADER, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_DOCUMENT:
                if (resultCode == RESULT_OK) {
                    String returnedResult = data.getData().toString();
                    refreshPreferredDocument();
                    Log.d("RETURNED RESULT", returnedResult);
                }
                break;
            case REQUEST_EDIT_DOCUMENT:
                if (resultCode == RESULT_OK) {
                    refreshPreferredDocument();
                    Log.d("RETURNED RESULT", "OK UPDATE DOCUMENT");
                }
                break;
            case REQUEST_DUPLICATE_DOCUMENT:
                if (resultCode == RESULT_OK) {
                    String returnedResult = data.getData().toString();
                    refreshPreferredDocument();
                    Log.d("RETURNED RESULT", returnedResult);
                }
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return SetlistsDbDocumentLoader.newInstanceForSongId(this, song);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == MainActivity.DOCUMENTS_LOADER) {
            if (cursor.getCount() <= 0)
                textNothingToShow.setVisibility(View.VISIBLE);
            else
                textNothingToShow.setVisibility(View.GONE);
            toolbar.setSubtitle(cursor.getCount() <= 0 ? "No document" : String.valueOf(cursor.getCount()) + " document" + (cursor.getCount() > 1 ? "s" : ""));
            DocumentsActivity.Adapter adapter = new DocumentsActivity.Adapter(cursor);
            adapter.setHasStableIds(true);
            mRecyclerView.setAdapter(adapter);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.scrollToPosition(position);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == MainActivity.DOCUMENTS_LOADER)
            mRecyclerView.setAdapter(null);
    }

    private void refreshPreferredDocument() {
        Cursor songCursor = getContentResolver().query(SetlistsDbContract.SetlistsDbSongEntry.buildSetlistsDbSongUri(song),
            null, null, null, null);

        if (songCursor.moveToFirst()) {
            if (songCursor.isNull(songCursor.getColumnIndex(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT))) {
                Cursor documentCursor = getContentResolver().query(SetlistsDbContract.SetlistsDbDocumentEntry.buildSetlistsDbSongDocumentsUri(song),
                        null, null, null, null);
                if (documentCursor.moveToFirst()) {
                    preferred = documentCursor.getString(documentCursor.getColumnIndex(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID));
                    ContentValues values = new ContentValues();
                    values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT, preferred);
                    values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);
                    if (getContentResolver().update(SetlistsDbContract.SetlistsDbSongEntry.buildSetlistsDbSongUri(song),
                            values, SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + "=?", new String[] {String.valueOf(song)}) > 0) {
                        getSupportLoaderManager().restartLoader(MainActivity.DOCUMENTS_LOADER, null, DocumentsActivity.this);
                    }
                } else {
                    preferred = null;
                }
                documentCursor.close();
            }
            else
                preferred = songCursor.getString(songCursor.getColumnIndex(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT));
        }

        songCursor.close();
    }

    private class Adapter extends RecyclerView.Adapter<DocumentsActivity.ViewHolder> {
        private Cursor mCursor;
        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public DocumentsActivity.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_details, parent, false);
            final DocumentsActivity.ViewHolder vh = new DocumentsActivity.ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(DocumentsActivity.ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.bindData(mCursor.getString(0), mCursor.getString(1), mCursor.getString(2), mCursor.getString(3), mCursor.getString(9),
                    mCursor.getInt(4), mCursor.getInt(5), mCursor.getInt(6), new Date(mCursor.getLong(8) * 1000));
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textDescription;
        private TextView textAuthor;
        private TextView textType;
        private TextView textPreferred;
        private TextView textDateModified;
        private ImageButton btnMenu;

        private String id;
        private String song;
        private String title;
        private String author;
        private String description;
        private int transpose;
        private int transposeMode;
        private int type;
        private Date dateModified;

        public void bindData(String id, String description, String author, String song, String title, int type, int transpose, int transposeMode, Date dateModified) {
            this.id = id;
            this.song = song;
            this.title = title == null ? "" : title;
            this.description = description == null ? "" : description;
            this.author = author == null ? "" : author;
            this.type = type;
            this.dateModified = dateModified;
            this.transpose = transpose;
            this.transposeMode = transposeMode;

            textDescription.setText(this.description);
            textAuthor.setText(getResources().getString(R.string.created_by) + " " + this.author);

            switch (type) {
                case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_PDF:
                    textType.setText("PDF file");
                    break;
                case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_CHORDPRO:
                    textType.setText("Chordpro file");
                    break;
                case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_IMAGE:
                    textType.setText("Image file");
                    break;
                case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_TEXT:
                    textType.setText("Text file");
                    break;
            }

            if (this.dateModified != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                String dateModifiedFormatted = sdf.format(this.dateModified);
                textDateModified.setText(getResources().getString(R.string.modified_on) + " " + dateModifiedFormatted);
            }

            if (id.equals(preferred)) {
                textPreferred.setText(getText(R.string.document_title_preferred));
                textPreferred.setVisibility(View.VISIBLE);
            }
        }

        public ViewHolder(View view) {
            super(view);
            textDescription = (TextView) view.findViewById(R.id.item_details_title);
            textAuthor = (TextView) view.findViewById(R.id.item_details_subtitle);
            textType = (TextView) view.findViewById(R.id.item_details_comment_left);
            textDateModified = (TextView) view.findViewById(R.id.item_details_comment_right);
            textPreferred = (TextView) view.findViewById(R.id.item_details_title_super);
            btnMenu = (ImageButton) view.findViewById(R.id.item_details_menu);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAdapterPosition();
                    Intent documentIntent = new Intent(v.getContext(), ViewerActivity.class);
                    documentIntent.putExtra("id", id);
                    documentIntent.putExtra("path", getFilesDir().getPath() + "/" + String.valueOf(id));
                    documentIntent.putExtra("type", type);
                    documentIntent.putExtra("song", song);
                    documentIntent.putExtra("title", title);
                    documentIntent.putExtra("tempo", tempo);
                    documentIntent.putExtra("description", description);
                    documentIntent.putExtra("transpose", transpose);
                    documentIntent.putExtra("transposeMode", transposeMode);
                    startActivity(documentIntent);
                }
            });

            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    popupMenu.getMenuInflater().inflate(R.menu.document_context_menu, popupMenu.getMenu());
                    if (id == preferred)
                        popupMenu.getMenu().removeItem(R.id.document_context_menu_preferred);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.document_context_menu_preferred:
                                    position = getAdapterPosition();
                                    ContentValues values = new ContentValues();
                                    values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT, id);
                                    values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);
                                    if (getContentResolver().update(SetlistsDbContract.SetlistsDbSongEntry.buildSetlistsDbSongUri(song),
                                            values, SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + "=?", new String[] {String.valueOf(song)}) > 0) {
                                        preferred = id;
                                        getSupportLoaderManager().restartLoader(MainActivity.DOCUMENTS_LOADER, null, DocumentsActivity.this);
                                    }
                                    return true;
                                case R.id.document_context_menu_edit:
                                    position = getAdapterPosition();
                                    Intent editDocumentIntent = new Intent(v.getContext(), DocumentEditActivity.class);
                                    editDocumentIntent.putExtra(DocumentEditActivity.EXTRA_MODE, DocumentEditActivity.EXTRA_MODE_EDIT);
                                    editDocumentIntent.putExtra("id", id);
                                    editDocumentIntent.putExtra("song", song);
                                    editDocumentIntent.putExtra("title", title);
                                    editDocumentIntent.putExtra("description", description);
                                    editDocumentIntent.putExtra("type", type);
                                    editDocumentIntent.putExtra("tempo", tempo);
                                    editDocumentIntent.putExtra("isPreferred", id == preferred);
                                    startActivityForResult(editDocumentIntent, REQUEST_EDIT_DOCUMENT);
                                    return true;
                                case R.id.document_context_menu_duplicate:
                                    position = getAdapterPosition();
                                    Intent duplicateDocumentIntent = new Intent(v.getContext(), DocumentEditActivity.class);
                                    duplicateDocumentIntent.putExtra(DocumentEditActivity.EXTRA_MODE, DocumentEditActivity.EXTRA_MODE_DUPLICATE);
                                    duplicateDocumentIntent.putExtra("id", id);
                                    duplicateDocumentIntent.putExtra("song", song);
                                    duplicateDocumentIntent.putExtra("description", description);
                                    duplicateDocumentIntent.putExtra("type", type);
                                    duplicateDocumentIntent.putExtra("tempo", tempo);
                                    duplicateDocumentIntent.putExtra("isPreferred", false);
                                    startActivityForResult(duplicateDocumentIntent, REQUEST_DUPLICATE_DOCUMENT);
                                    return true;
                                case R.id.document_context_menu_remove:
                                    position = getAdapterPosition();
                                    new AlertDialog.Builder(v.getContext())
                                            .setTitle(getResources().getString(R.string.menu_remove_document))
                                            .setMessage(getResources().getString(R.string.dialog_remove_document))
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    if (getContentResolver().delete(SetlistsDbContract.SetlistsDbDocumentEntry.buildSetlistsDbDocumentUri(id),
                                                            SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)}) > 0) {
                                                        refreshPreferredDocument();
                                                        getSupportLoaderManager().restartLoader(MainActivity.DOCUMENTS_LOADER, null, DocumentsActivity.this);
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
