package com.miguelgaeta.media_picker;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@SuppressWarnings("UnusedDeclaration")
public class MediaPickerFile {

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
    private static File create(final String directory, final String name, final String suffix) throws IOException {

        final File dir = new File(Environment.getExternalStorageDirectory() + File.separator + directory);

        if (!dir.exists()) {

            boolean result = dir.mkdirs();

            if (!result) {

                throw new IOException("Unable to create directory.");
            }
        }

        return File.createTempFile(name, suffix, dir);
    }

    /**
     * @see #create(String, String, String)
     */
    private static File create(final String directory, final String name) throws IOException {

        return create(directory, name, null);
    }

    /**
     * @see #create(String, String)
     */
    private static File create(final String directory) throws IOException {

        return create(directory, UUID.randomUUID().toString());
    }

    /**
     * @see #create(String)
     */
    private static File create() throws IOException {

        return create("media_picker");
    }
}
