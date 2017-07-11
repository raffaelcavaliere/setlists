package com.raffaelcavaliere.setlists.ui.document;

import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.raffaelcavaliere.setlists.R;

import com.raffaelcavaliere.setlists.chordpro.BlockElement;
import com.raffaelcavaliere.setlists.chordpro.ChordproRoot;
import com.raffaelcavaliere.setlists.chordpro.DirectiveElement;
import com.raffaelcavaliere.setlists.chordpro.Element;
import com.raffaelcavaliere.setlists.chordpro.InlineElement;
import com.raffaelcavaliere.setlists.chordpro.ChordproParser;
import com.raffaelcavaliere.setlists.chordpro.ChordproTokenizer;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChordproViewerFragment extends ViewerFragment {

    private LinearLayout layout;
    private TextView title, subtitle, key, tempo, composer, arranger, lyricist, album, copyright, year, time, duration;
    private int transpose = 0;
    private int transposeMode = SetlistsDbContract.SetlistsDbDocumentEntry.TRANSPOSE_USE_SHARPS;

    public ChordproViewerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transpose = getArguments().getInt(ARG_TRANSPOSE);
            transposeMode = getArguments().getInt(ARG_TRANSPOSE_MODE);
        }
    }

    public static ChordproViewerFragment newInstance(String path, int transpose, int transposeMode) {
        ChordproViewerFragment fragment = new ChordproViewerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DOCUMENT_PATH, path);
        args.putInt(ARG_TRANSPOSE, transpose);
        args.putInt(ARG_TRANSPOSE_MODE, transposeMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_viewer_chordpro, container, false);
        layout = (LinearLayout) v.findViewById(R.id.documentChordproView);
        title = (TextView) v.findViewById(R.id.chordpro_title);
        subtitle = (TextView) v.findViewById(R.id.chordpro_subtitle);
        key = (TextView) v.findViewById(R.id.chordpro_key);
        tempo = (TextView) v.findViewById(R.id.chordpro_tempo);
        composer = (TextView) v.findViewById(R.id.chordpro_composer);
        arranger = (TextView) v.findViewById(R.id.chordpro_arranger);
        lyricist = (TextView) v.findViewById(R.id.chordpro_lyricist);
        time = (TextView) v.findViewById(R.id.chordpro_time);
        duration = (TextView) v.findViewById(R.id.chordpro_duration);

        checkPermissionReadStorage(getContext(), getActivity());

        return v;
    }

    @Override
    public void loadDocument(Uri uri) {
        File file = new File(uri.getPath());
        try {
            String content = "";
            Scanner scanner = new Scanner(file);
            content = scanner.nextLine();
            while (scanner.hasNextLine()) {
                content = content + "\n" + scanner.nextLine();
            }
            char[] charArray = content.toCharArray();
            ChordproParser parser = new ChordproParser(new ChordproTokenizer(charArray));
            ChordproRoot root = parser.parseTokenizer();
            createDocumentView(root);

        } catch (Exception ex) {
            Log.d("EXCEPTION", ex.toString());
        }
    }

    private void createDocumentView(ChordproRoot root) {

        if (root.getTitle() != null) {
            title.setText(root.getTitle());
            title.setVisibility(View.VISIBLE);
        }

        if (root.getSubtitle() != null) {
            subtitle.setText(root.getSubtitle());
            subtitle.setVisibility(View.VISIBLE);
        }

        if (root.getComposer() != null) {
            composer.setText(root.getComposer());
            composer.setVisibility(View.VISIBLE);
        }

        if (root.getArranger() != null) {
            arranger.setText(root.getArranger());
            arranger.setVisibility(View.VISIBLE);
        }

        if (root.getLyricist() != null) {
            lyricist.setText(root.getLyricist());
            lyricist.setVisibility(View.VISIBLE);
        }

        if (root.getKey() != null) {
            key.setText(getResources().getString(R.string.key_of) + " " + transposeChord(root.getKey()));
            key.setVisibility(View.VISIBLE);
        }

        if (root.getTempo() != null) {
            tempo.setText(getResources().getString(R.string.bpm_note) + " " + root.getTempo());
            tempo.setVisibility(View.VISIBLE);
        }

        if (root.getTime() != null) {
            time.setText(root.getTime());
            time.setVisibility(View.VISIBLE);
        }

        if (root.getDuration() != null) {
            duration.setText(root.getDuration());
            duration.setVisibility(View.VISIBLE);
        }

        Iterator<Element> iterator = root.getElements().iterator();
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;
        ChordproLyricLine line = new ChordproLyricLine();
        do {
            Element element = iterator.next();
            if (element instanceof InlineElement) {
                InlineElement inlineElement = (InlineElement)element;
                switch (inlineElement.getType()) {
                    case CHORDPRO_BREAK:
                        if (line.getLayout().getParent() == null) {
                            layout.addView(line.generateLayout(width));
                        } else {
                            LinearLayout space = new LinearLayout(getContext());
                            space.setMinimumHeight(80);
                            layout.addView(space);
                        }
                        break;
                    case CHORDPRO_LYRIC:
                        if (line.getLayout().getParent() != null) {
                            line = new ChordproLyricLine();
                        }
                        line.addLyric(inlineElement.getContent());
                        break;
                    case CHORDPRO_CHORD:
                        if (line.getLayout().getParent() != null) {
                            line = new ChordproLyricLine();
                        }
                        line.addChord(inlineElement.getContent());
                        break;
                }
            }
            else if (element instanceof BlockElement) {
                BlockElement blockElement = (BlockElement) element;
                switch (blockElement.getType()) {
                    case CHORDPRO_TAB:
                        for (int i = 0; i < blockElement.getChildren().size(); i++) {
                            InlineElement child = blockElement.getChildren().get(i);
                            if (child.getContent() != null) {
                                FrameLayout tabLine = new FrameLayout(getContext());
                                TextView vTab = new TextView(getContext());
                                vTab.setTextAppearance(R.style.DocumentTablature);
                                vTab.setText(child.getContent());
                                tabLine.addView(vTab);
                                layout.addView(tabLine);
                            }
                        }
                        break;
                    case CHORDPRO_HIGHLIGHT:
                        for (int i = 0; i < blockElement.getChildren().size(); i++) {
                            InlineElement child = blockElement.getChildren().get(i);
                            switch (child.getType()) {
                                case CHORDPRO_BREAK:
                                    if (line.getLayout().getParent() == null) {
                                        layout.addView(line.generateLayout(width));
                                    } else {
                                        LinearLayout space = new LinearLayout(getContext());
                                        space.setMinimumHeight(80);
                                        layout.addView(space);
                                    }
                                    break;
                                case CHORDPRO_LYRIC:
                                    if (line.getLayout().getParent() != null) {
                                        line = new ChordproLyricLine();
                                        line.getLayout().setBackgroundColor(getResources().getColor(R.color.nnf_light_separator_color, getActivity().getTheme()));
                                        line.getLayout().setPadding(16, 5, 16, 5);
                                    }
                                    line.addLyric(child.getContent());
                                    break;
                                case CHORDPRO_CHORD:
                                    if (line.getLayout().getParent() != null) {
                                        line = new ChordproLyricLine();
                                        line.getLayout().setBackgroundColor(getResources().getColor(R.color.nnf_light_separator_color, getActivity().getTheme()));
                                        line.getLayout().setPadding(16, 5, 16, 5);
                                    }
                                    line.addChord(child.getContent());
                                    break;
                            }
                        }
                        break;
                }
            }
            else if (element instanceof DirectiveElement) {
                DirectiveElement directiveElement = (DirectiveElement) element;
                switch (directiveElement.getType()) {
                    case CHORDPRO_COMMENT:
                        ChordproCommentLine comment = new ChordproCommentLine();
                        layout.addView(comment.generateLayout(directiveElement.getContent(), false));
                        break;
                    case CHORDPRO_COMMENT_ITALIC:
                        ChordproCommentLine commentItalic = new ChordproCommentLine();
                        layout.addView(commentItalic.generateLayout(directiveElement.getContent(), true));
                        break;
                    case CHORDPRO_COMMENT_BOX:
                        ChordproCommentLine commentBox = new ChordproCommentLine();
                        layout.addView(commentBox.generateBoxLayout(directiveElement.getContent()));
                        break;
                }
            }

        } while (iterator.hasNext());

        if (line.getLayout().getParent() == null)
            layout.addView(line.generateLayout(width));
    }

    private String transposeChord(String chord) {
        if (transpose == 0)
            return chord;
        Pattern p = Pattern.compile("([A-G][#b]?)+");
        Matcher m = p.matcher(chord);
        StringBuffer s = new StringBuffer();
        while (m.find())
            m.appendReplacement(s, transpose(m.group(1), transpose, transposeMode == SetlistsDbContract.SetlistsDbDocumentEntry.TRANSPOSE_USE_FLATS));
        m.appendTail(s);
        return s.toString();
    }

    private String transpose(String note, int interval, boolean useFlat) {
        return interval > 0 ? transposeUp(note, interval, useFlat) : transposeDown(note, interval, useFlat);
    }

    private String transposeUp(String note, int interval, boolean useFlat) {
        if (interval == 0)
            return note;
        switch (note) {
            case "C":
                return transposeUp(useFlat ? "Db":"C#", interval-1, useFlat);
            case "C#":
            case "Db":
                return transposeUp("D", interval-1, useFlat);
            case "D":
                return transposeUp(useFlat ? "Eb":"D#", interval-1, useFlat);
            case "Eb":
            case "D#":
                return transposeUp("E", interval-1, useFlat);
            case "E":
                return transposeUp("F", interval-1, useFlat);
            case "F":
                return transposeUp(useFlat ? "Gb":"F#", interval-1, useFlat);
            case "F#":
            case "Gb":
                return transposeUp("G", interval-1 , useFlat);
            case "G":
                return transposeUp(useFlat ? "Ab":"G#", interval-1, useFlat);
            case "G#":
            case "Ab":
                return transposeUp("A", interval-1, useFlat);
            case "A":
                return transposeUp(useFlat ? "Bb":"A#", interval-1, useFlat);
            case "A#":
            case "Bb":
                return transposeUp("B", interval-1, useFlat);
            case "B":
                return transposeUp("C", interval-1, useFlat);
            default:
                return note;
        }
    }

    private String transposeDown(String note, int interval, boolean useFlat) {
        if (interval == 0)
            return note;
        switch (note) {
            case "C":
                return transposeDown("B", interval+1, useFlat);
            case "C#":
            case "Db":
                return transposeDown("C", interval+1, useFlat);
            case "D":
                return transposeDown(useFlat ? "Db":"C#", interval+1, useFlat);
            case "D#":
            case "Eb":
                return transposeDown("D", interval+1, useFlat);
            case "E":
                return transposeDown(useFlat ? "Eb":"D#", interval+1, useFlat);
            case "F":
                return transposeDown("E", interval+1, useFlat);
            case "F#":
            case "Gb":
                return transposeDown("F", interval+1, useFlat);
            case "G":
                return transposeDown(useFlat ? "Gb":"F#", interval+1, useFlat);
            case "G#":
            case "Ab":
                return transposeDown("G", interval+1, useFlat);
            case "A":
                return transposeDown(useFlat ? "Ab":"G#", interval+1, useFlat);
            case "A#":
            case "Bb":
                return transposeDown("A", interval+1, useFlat);
            case "B":
                return transposeDown(useFlat ? "Bb":"A#", interval+1, useFlat);
            default:
                return note;
        }
    }

    private class ChordproLyricLine {

        private ArrayList<ChordproLyricColumn> columns = new ArrayList<>();
        LinearLayout line = new LinearLayout(getContext());

        ChordproLyricLine() {
            line.setOrientation(LinearLayout.HORIZONTAL);
        }

        public LinearLayout getLayout() {
            return line;
        }

        LinearLayout generateLayout(int screenWidth) {
            line.removeAllViews();
            boolean lyricOnly = true;
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).getChord() != null) {
                    lyricOnly = false;
                    break;
                }
            }

            for (int i = 0; i < columns.size(); i++) {
                LinearLayout column = new LinearLayout(getContext());
                column.setOrientation(LinearLayout.VERTICAL);
                if (!lyricOnly) {
                    TextView vChord = new TextView(getContext());
                    vChord.setTextAppearance(R.style.DocumentChord);
                    if (columns.get(i).getChord() != null)
                        vChord.setText(transposeChord(columns.get(i).getChord()).concat(" "));
                    column.addView(vChord);
                }
                TextView vLyric = new TextView(getContext());
                vLyric.setTextAppearance(R.style.DocumentLyric);
                vLyric.setMaxWidth(screenWidth / columns.size());
                if (columns.get(i).getLyric() != null) {
                    vLyric.setText(columns.get(i).getLyric());
                }
                column.addView(vLyric);

                line.addView(column);
            }
            return line;
        }

        void addChord(String text) {
            ChordproLyricColumn column = new ChordproLyricColumn();
            column.setChord(text);
            columns.add(column);
        }

        void addLyric(String text) {
            ChordproLyricColumn column;
            if (columns.size() > 0)
                column = columns.get(columns.size() - 1);
            else {
                column = new ChordproLyricColumn();
                columns.add(column);
            }
            column.setLyric(text);
        }
    }

    private class ChordproLyricColumn {
        private String chord = null;
        private String lyric = null;

        String getChord() {
            return chord;
        }

        void setChord(String chord) {
            this.chord = chord;
        }

        String getLyric() {
            return lyric;
        }

        void setLyric(String lyric) {
            this.lyric = lyric;
        }
    }

    private class ChordproCommentLine {

        private FrameLayout line = new FrameLayout(getContext());

        ChordproCommentLine() {
            line.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }

        public FrameLayout getLayout() {
            return line;
        }

        FrameLayout generateLayout(String comment, boolean italic) {
            line.removeAllViews();
            TextView vComment = new TextView(getContext());
            vComment.setTextAppearance(italic ? R.style.DocumentItalicComment : R.style.DocumentComment);
            vComment.setText(comment);
            line.addView(vComment);
            return line;
        }

        FrameLayout generateBoxLayout(String comment) {
            line.removeAllViews();
            TextView vComment = new TextView(getContext());
            vComment.setTextAppearance(R.style.DocumentBoxComment);
            vComment.setBackground(getResources().getDrawable(R.drawable.emphasis_box, getActivity().getTheme()));
            vComment.setText(comment);
            line.addView(vComment);
            return line;
        }
    }
}
