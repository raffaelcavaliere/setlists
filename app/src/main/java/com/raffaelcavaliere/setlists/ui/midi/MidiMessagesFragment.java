package com.raffaelcavaliere.setlists.ui.midi;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.data.SetlistsDbMidiMessageLoader;
import com.raffaelcavaliere.setlists.ui.MainActivity;
import com.raffaelcavaliere.setlists.utils.MidiHelper;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class MidiMessagesFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_ADD_MIDI_MESSAGE = 1000;
    private static final int REQUEST_EDIT_MIDI_MESSAGE = 1001;

    private OnFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private TextView textNothingToShow;
    private int selectedMessage = 0;
    MidiManager midiManager;
    private ArrayList<MidiInputPort> midiPorts = new ArrayList<>();

    public MidiMessagesFragment() {
        // Required empty public constructor
    }

    public static MidiMessagesFragment newInstance() {
        MidiMessagesFragment fragment = new MidiMessagesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ;
        }
        midiPorts.clear();
        midiManager = (MidiManager) getActivity().getSystemService(Activity.MIDI_SERVICE);
        final MidiDeviceInfo[] infos = midiManager.getDevices();
        Handler handler = new Handler();
        for (final MidiDeviceInfo deviceInfo : infos) {
            midiManager.openDevice(deviceInfo, new MidiManager.OnDeviceOpenedListener() {
                @Override
                public void onDeviceOpened(MidiDevice device) {
                    if (device == null) {
                        Toast.makeText(getContext(), "MIDI error: could not open device " + String.valueOf(deviceInfo.getId()), Toast.LENGTH_LONG).show();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_midi_messages, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.midi_message_list);

        FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.fab_add_midi_message);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newMidiMessageIntent = new Intent(getActivity(), MidiMessageEditActivity.class);
                newMidiMessageIntent.putExtra(MidiMessageEditActivity.EXTRA_MODE, MidiMessageEditActivity.EXTRA_MODE_NEW);
                startActivityForResult(newMidiMessageIntent, REQUEST_ADD_MIDI_MESSAGE);
            }
        });
        textNothingToShow = (TextView) view.findViewById(R.id.text_no_midi_message);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSupportLoaderManager().restartLoader(MainActivity.MIDI_MESSAGES_LOADER, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_MIDI_MESSAGE:
                if (resultCode == RESULT_OK) {
                    String returnedResult = data.getData().toString();
                    Log.d("RETURNED RESULT", returnedResult);
                }
                break;
            case REQUEST_EDIT_MIDI_MESSAGE:
                if (resultCode == RESULT_OK) {
                    Log.d("RETURNED RESULT", "OK UPDATE MIDI MESSAGE");
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MidiMessagesFragment.OnFragmentInteractionListener) {
            mListener = (MidiMessagesFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDestroy() {
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return SetlistsDbMidiMessageLoader.newAllMidiMessagesInstance(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        MidiMessagesFragment.Adapter adapter = new MidiMessagesFragment.Adapter(cursor);
        if (cursor.getCount() <= 0)
            textNothingToShow.setVisibility(View.VISIBLE);
        else
            textNothingToShow.setVisibility(View.GONE);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.scrollToPosition(selectedMessage);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<MidiMessagesFragment.ViewHolder> {
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
        public MidiMessagesFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_details, parent, false);
            final MidiMessagesFragment.ViewHolder vh = new MidiMessagesFragment.ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(MidiMessagesFragment.ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.bindData(mCursor.getLong(0), mCursor.getString(1), mCursor.getInt(2), mCursor.getInt(3), mCursor.getInt(4), mCursor.getInt(5));
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textName;
        private TextView textStatus;
        private TextView textData1;
        private TextView textData2;
        private TextView textChannel;
        private ImageButton btnMenu;

        private long id;
        private String name;
        private int channel;
        private int status;
        private int data1;
        private int data2;

        public void bindData(long id, String name, int channel, int status, int data1, int data2) {
            this.id = id;
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
        }

        public ViewHolder(View view) {
            super(view);
            textName = (TextView) view.findViewById(R.id.item_details_title);
            textStatus = (TextView) view.findViewById(R.id.item_details_subtitle);
            textChannel = (TextView) view.findViewById(R.id.item_details_comment_left);
            textData1 = (TextView) view.findViewById(R.id.item_details_comment_center_left);
            textData1.setVisibility(View.VISIBLE);
            textData2 = (TextView) view.findViewById(R.id.item_details_comment_center_right);
            textData2.setVisibility(View.VISIBLE);
            btnMenu = (ImageButton) view.findViewById(R.id.item_details_menu);
            ((TextView) view.findViewById(R.id.item_details_comment_right)).setVisibility(View.GONE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
                        for (MidiInputPort port : midiPorts) {
                            byte[] buffer = new byte[3];
                            int numBytes = 0;
                            buffer[numBytes++] = (byte) (status + (channel > 0 ? channel - 1 : 0));
                            buffer[numBytes++] = (byte) (data1 > 0 ? data1 - 1 : 0);
                            if (data2 > 0)
                                buffer[numBytes++] = (byte) (data2 > 0 ? data2 - 1 : 0);
                            int offset = 0;
                            long now = System.nanoTime();
                            try {
                                Toast.makeText(getActivity(), "Sending MIDI: " + MidiHelper.bytesToHex(buffer), Toast.LENGTH_SHORT).show();
                                port.send(buffer, offset, numBytes, now);
                            } catch (Exception ex) {
                                Toast.makeText(getActivity(), "MIDI error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });

            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    popupMenu.getMenuInflater().inflate(R.menu.midi_message_context_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.midi_message_context_menu_edit:
                                    selectedMessage = getAdapterPosition();
                                    Intent editMidiMessageIntent = new Intent(v.getContext(), MidiMessageEditActivity.class);
                                    editMidiMessageIntent.putExtra("id", id);
                                    editMidiMessageIntent.putExtra("name", name);
                                    editMidiMessageIntent.putExtra("channel", channel);
                                    editMidiMessageIntent.putExtra("status", status);
                                    editMidiMessageIntent.putExtra("data1", data1);
                                    editMidiMessageIntent.putExtra("data2", data2);
                                    editMidiMessageIntent.putExtra(MidiMessageEditActivity.EXTRA_MODE, MidiMessageEditActivity.EXTRA_MODE_EDIT);
                                    startActivityForResult(editMidiMessageIntent, REQUEST_EDIT_MIDI_MESSAGE);
                                    return true;
                                case R.id.midi_message_context_menu_remove:
                                    selectedMessage = getAdapterPosition();
                                    new AlertDialog.Builder(v.getContext())
                                            .setTitle(getResources().getString(R.string.menu_remove_midi_message))
                                            .setMessage(getResources().getString(R.string.dialog_remove_midi_message))
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    if (getActivity().getContentResolver().delete(SetlistsDbContract.SetlistsDbMidiMessageEntry.buildSetlistsDbMidiMessageUri(id),
                                                            SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)}) > 0) {
                                                        getActivity().getSupportLoaderManager().restartLoader(MainActivity.MIDI_MESSAGES_LOADER, null, MidiMessagesFragment.this);
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
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
