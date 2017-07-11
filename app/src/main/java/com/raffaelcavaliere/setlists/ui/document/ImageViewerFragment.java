package com.raffaelcavaliere.setlists.ui.document;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.raffaelcavaliere.setlists.R;


public class ImageViewerFragment extends ViewerFragment {

    private ImageView imageView;

    public ImageViewerFragment() {
    }

    public static ImageViewerFragment newInstance(String path) {
        ImageViewerFragment fragment = new ImageViewerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DOCUMENT_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_viewer_image, container, false);
        imageView = (ImageView) v.findViewById(R.id.documentImageView);
        checkPermissionReadStorage(getContext(), getActivity());
        return v;
    }

    @Override
    public void loadDocument(Uri uri) {
        imageView.setImageURI(uri);
    }
}
