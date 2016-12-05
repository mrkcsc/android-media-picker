package com.miguelgaeta.media_picker;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@SuppressWarnings({"UnusedDeclaration", "WeakerAccess"})
public class MediaPickerFile {

    public static Rect getImageDimensions(final File file) {

        return getImageDimensions(Uri.fromFile(file));
    }

    public static Rect getImageDimensions(final Uri uri) {

        final BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(uri.getPath(), options);

        return new Rect(0, 0, options.outWidth, options.outHeight);
    }

    /**
     * Create a file in the devices external storage.
     *
     * @param root Root directory, typically invoked from {@link Context#getFilesDir()}.
     * @param directory Target directory within root.
     * @param name Target file name.
     * @param suffix Target file suffix.
     *
     * @return Created file.
     *
     * @throws IOException Throws when unable to create a file.
     */
    public static File create(final File root, final String directory, final String suffix, final String name) throws IOException {

        final File path = new File(root, directory);
        final File file = new File(path + File.separator + name + suffix);

        if (!path.exists()) {

            boolean result = path.mkdirs();

            if (!result) {

                throw new IOException("Unable to create directory.");
            }
        }

        if (!file.exists()) {
            final boolean created = file.createNewFile();

            if (!created) {
                throw new IOException("Unable to create temporary file, does not exist.");
            }
        }

        return file;
    }

    /**
     * @see #create(File, String, String, String)
     */
    public static File create(final File root, final String directory, final String suffix) throws IOException {

        return create(root, directory, suffix, UUID.randomUUID().toString());
    }

    /**
     * @see #create(File, String, String)
     */
    public static File create(final File root, final String directory) throws IOException {

        return create(root, directory, ".tmp");
    }

    /**
     * @see #create(File, String)
     */
    public static File create(final File root) throws IOException {

        return create(root, "temp");
    }
}
