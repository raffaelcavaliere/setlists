package com.raffaelcavaliere.setlists.ui.document;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.utils.Storage;

import java.io.File;
import java.util.Date;
import java.util.Scanner;


public class TextEditorActivity extends AppCompatActivity {

    String id;
    String path;
    EditText editText;
    private Button saveButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        setTitle(getResources().getString(R.string.title_activity_document_edit));
        editText = (EditText) findViewById(R.id.document_edit_text);

        try {
            path = extras.getString("path", "");
            if (path.length() > 0) {
                File file = new File(path);
                String content = "";
                Scanner scanner = new Scanner(file);
                content = scanner.nextLine();
                while (scanner.hasNextLine()) {
                    content = content + "\n" + scanner.nextLine();
                }
                editText.setText(content);
            }

        } catch (Exception ex) {
            Log.d("EXCEPTION", ex.toString());
        }

        id = extras.getString("id", "");

        saveButton = (Button) findViewById(R.id.editTextEditorSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            if (path.length() > 0) {
                Intent data = new Intent();
                try {
                    File file = new File(path);
                    Storage.replace(file, editText.getText().toString());
                    if (!id.isEmpty()) {
                        ContentValues values = new ContentValues();
                        values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);
                        int result = getContentResolver().update(SetlistsDbContract.SetlistsDbDocumentEntry.buildSetlistsDbDocumentUri(id), values,
                                SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
                    }
                    data.setData(Uri.fromFile(file));
                    setResult(RESULT_OK, data);
                } catch (Exception ex) {
                    Log.d("EXCEPTION", ex.toString());
                }
                finish();
            } else {
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.toast_file_cannot_be_opened), Toast.LENGTH_SHORT).show();
            }
            }
        });

        cancelButton = (Button) findViewById(R.id.editTextEditorCancel);
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
}
