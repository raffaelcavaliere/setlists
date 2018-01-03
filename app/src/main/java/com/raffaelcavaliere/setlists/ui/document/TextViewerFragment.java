package com.raffaelcavaliere.setlists.ui.document;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.raffaelcavaliere.setlists.R;

import java.io.File;
import java.util.Scanner;


public class TextViewerFragment extends ViewerFragment {

    private TextView textView;
    private TextView textNothingToShow;

    public TextViewerFragment() {
    }

    public static TextViewerFragment newInstance(String path) {
        TextViewerFragment fragment = new TextViewerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DOCUMENT_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_viewer_text, container, false);
        textView = (TextView) v.findViewById(R.id.documentTextView);
        textNothingToShow = (TextView) v.findViewById(R.id.text_no_file);
        checkPermissionReadStorage(getContext(), getActivity());
        return v;
    }

    @Override
    public void loadDocument() {
        File file = new File(getUri().getPath());
        if (!file.exists())
            textNothingToShow.setVisibility(View.VISIBLE);
        else {
            try {
                String content = "";
                Scanner scanner = new Scanner(file);
                content = scanner.nextLine();
                while (scanner.hasNextLine()) {
                    content = content + "\n" + scanner.nextLine();
                }
                textView.setText(content);

            } catch (Exception ex) {
                Log.d("EXCEPTION", ex.toString());
            }
        }
    }
}
