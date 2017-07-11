package com.raffaelcavaliere.setlists.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by raffaelcavaliere on 2017-06-10.
 */

public class Storage {

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public static void replace(File src, String data) throws IOException {
        OutputStream out = new FileOutputStream(src, false);
        try {
            out.write(data.getBytes(Charset.defaultCharset()));
        } finally {
            out.close();
        }
    }
}
