package com.raffaelcavaliere.setlists.ui.document;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.ui.document.ViewerActivity;

/**
 * Created by raffaelcavaliere on 2017-04-06.
 */

public class TransposeDialogFragment extends DialogFragment {

    private int transpose = 0;
    private int mode = SetlistsDbContract.SetlistsDbDocumentEntry.TRANSPOSE_USE_SHARPS;
    private TextView textTranspose;
    private Button btnUp;
    private Button btnDown;
    private RadioButton radioSharp;
    private RadioButton radioFlat;
    private Button btnOk;
    private Button btnCancel;

    @Override
    public void setArguments(Bundle args) {
        this.transpose = args.getInt("transpose", 0);
        this.mode = args.getInt("mode", SetlistsDbContract.SetlistsDbDocumentEntry.TRANSPOSE_USE_SHARPS);
        super.setArguments(args);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("transpose", transpose);
        outState.putInt("mode", mode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            transpose = savedInstanceState.getInt("transpose");
            mode = savedInstanceState.getInt("mode");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transpose, container, false);
        textTranspose = (TextView) v.findViewById(R.id.transpose_semitones);
        textTranspose.setText(String.valueOf(transpose));

        btnUp = (Button) v.findViewById(R.id.transpose_up);
        btnDown = (Button) v.findViewById(R.id.transpose_down);

        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transpose < 11)
                    transpose += 1;
                textTranspose.setText(String.valueOf(transpose));
            }
        });

        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transpose > -11)
                    transpose -= 1;
                textTranspose.setText(String.valueOf(transpose));
            }
        });

        radioFlat = (RadioButton) v.findViewById(R.id.transpose_prefer_flats);
        radioSharp = (RadioButton) v.findViewById(R.id.transpose_prefer_sharps);

        if (mode == SetlistsDbContract.SetlistsDbDocumentEntry.TRANSPOSE_USE_FLATS)
            radioFlat.setChecked(true);

        btnOk = (Button) v.findViewById(R.id.nnf_button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransposeDialogListener activity = (TransposeDialogListener)getActivity();
                if (activity != null) {
                    activity.onTransposeDialogResult(transpose,
                            radioFlat.isChecked() ? SetlistsDbContract.SetlistsDbDocumentEntry.TRANSPOSE_USE_FLATS : SetlistsDbContract.SetlistsDbDocumentEntry.TRANSPOSE_USE_SHARPS);
                }
                dismiss();
            }
        });

        btnCancel = (Button) v.findViewById(R.id.nnf_button_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }

    public interface TransposeDialogListener {
        void onTransposeDialogResult(int tranpose, int mode);
    }
}

