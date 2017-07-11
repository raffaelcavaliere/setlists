package com.raffaelcavaliere.setlists.utils;

import android.support.annotation.NonNull;

import com.nononsenseapps.filepicker.FilePickerFragment;

import java.io.File;

/**
 * Created by raffaelcavaliere on 2017-06-13.
 */


public class FilteredFilePickerFragment extends FilePickerFragment {

    // File extension to filter on
    public static final String EXTENSION_TXT = ".txt";
    public static final String EXTENSION_BMP = ".bmp";
    public static final String EXTENSION_JPG = ".jpg";
    public static final String EXTENSION_GIF = ".gif";
    public static final String EXTENSION_PRO = ".pro";
    public static final String EXTENSION_PNG = ".png";
    public static final String EXTENSION_PDF = ".pdf";

    /**
     *
     * @param file
     * @return The file extension. If file has no extension, it returns null.
     */
    private String getExtension(@NonNull File file) {
        String path = file.getPath();
        int i = path.lastIndexOf(".");
        if (i < 0) {
            return null;
        } else {
            return path.substring(i);
        }
    }

    @Override
    protected boolean isItemVisible(final File file) {
        boolean ret = super.isItemVisible(file);
        if (ret && !isDir(file) && (mode == MODE_FILE || mode == MODE_FILE_AND_DIR)) {
            String ext = getExtension(file);
            return ext != null &&
                (EXTENSION_TXT.equalsIgnoreCase(ext) | EXTENSION_BMP.equalsIgnoreCase(ext) |
                 EXTENSION_GIF.equalsIgnoreCase(ext) | EXTENSION_JPG.equalsIgnoreCase(ext) |
                 EXTENSION_PNG.equalsIgnoreCase(ext) | EXTENSION_PRO.equalsIgnoreCase(ext) |
                 EXTENSION_PDF.equalsIgnoreCase(ext));
        }
        return ret;
    }
}