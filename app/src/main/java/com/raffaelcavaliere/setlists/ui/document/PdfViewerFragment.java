package com.raffaelcavaliere.setlists.ui.document;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.barteksc.pdfviewer.PDFView;
import com.raffaelcavaliere.setlists.R;

public class PdfViewerFragment extends ViewerFragment {

    private PDFView pdfView;

    public PdfViewerFragment() {
    }

    public static PdfViewerFragment newInstance(String path) {
        PdfViewerFragment fragment = new PdfViewerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DOCUMENT_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_viewer_pdf, container, false);
        pdfView = (PDFView) v.findViewById(R.id.documentPdfView);
        checkPermissionReadStorage(getContext(), getActivity());
        return v;
    }

    @Override
    public void loadDocument(Uri uri) {
        pdfView.fromUri(uri).load();
    }
}
