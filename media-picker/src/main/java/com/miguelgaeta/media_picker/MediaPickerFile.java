package com.miguelgaeta.media_picker;

import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@SuppressWarnings("UnusedDeclaration")
public class MediaPickerFile {

    public static Rect getImageDimensions(final @NonNull File file) {

        return getImageDimensions(Uri.fromFile(file));
    }

    public static Rect getImageDimensions(final @NonNull Uri uri) {

        final BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(uri.getPath(), options);

        return new Rect(0, 0, options.outWidth, options.outHeight);
    }

    /**
     * Create a file in the devices external storage.
     *
     * @param directory Target directory.
     * @param name Target file name.
     * @param suffix Target file suffix.
     *
     * @return Created file.
     *
     * @throws IOException
     */
    public static File create(final String directory, final String name, final String suffix) throws IOException {

        final File dir = new File(Environment.getExternalStorageDirectory() + File.separator + directory);

        if (!dir.exists()) {

            boolean result = dir.mkdirs();

            if (!result) {

                throw new IOException("Unable to create directory.");
            }
        }

        final File file = File.createTempFile(name, suffix, dir);

        if (!file.exists()) {

            throw new IOException("Unable to create temporary file, does not exist.");
        }

        return file;
    }

    /**
     * @see #create(String, String, String)
     */
    public static File create(final String directory, final String name) throws IOException {

        return create(directory, name, null);
    }

    /**
     * @see #create(String, String)
     */
    public static File create(final String directory) throws IOException {

        return create(directory, UUID.randomUUID().toString());
    }

    /**
     * @see #create(String)
     */
    public static File create() throws IOException {

        return create("media_picker");
    }

    /**
     * @see #create(String, String, String)
     */
    public static File createWithSuffix(final String suffix) throws IOException {

        return create("media_picker", UUID.randomUUID().toString(), suffix);
    }
}
