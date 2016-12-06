package com.miguelgaeta.media_picker;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by Miguel Gaeta on 2/10/16.
 */
@SuppressWarnings({"UnusedDeclaration", "WeakerAccess"})
public class Encoder {

    /**
     * Fetch target {@link InputStream} as a data url representation with
     * associated {@link Byte} stream encoded as a {@link Base64} string.
     *
     * @param inputStream Target {@link InputStream}.
     * @param mimeType Target mime type.
     *
     * @return Associated data url.
     *
     * @throws IOException Failure to encode result.
     */
    public static String getDataUrl(final @NonNull String mimeType,
                                    final @NonNull InputStream inputStream) throws IOException {
        final String template = "data:%s;base64,%s";

        return String.format(template, mimeType, getBase64EncodedString(inputStream));
    }


    /**
     * @see #getDataUrl(String, InputStream)
     */
    public static String getDataUrl(final @NonNull String mimeType,
                                    final @NonNull Bitmap bitmap) throws IOException {
        final int byteSize = bitmap.getRowBytes() * bitmap.getHeight();

        final ByteBuffer byteBuffer = ByteBuffer.allocate(byteSize);

        bitmap.copyPixelsToBuffer(byteBuffer);

        final byte[] byteArray = byteBuffer.array();

        final InputStream inputStream = new ByteArrayInputStream(byteArray);

        return getDataUrl(mimeType, inputStream);
    }

    /**
     * @see #getDataUrl(String, InputStream)
     */
    public static String getDataUrl(final @NonNull String mimeType,
                                    final @NonNull File file) throws IOException  {
        return getDataUrl(mimeType, new FileInputStream(file));
    }

    /**
     * @see #getDataUrl(String, File)
     */
    public static String getDataUrl(final @NonNull String mimeType,
                                    final @NonNull String filePath) throws IOException  {
        return getDataUrl(mimeType, new File(filePath));
    }

    /**
     * Fetch target {@link InputStream} as a {@link Base64} encoded string.
     *
     * @param inputStream Target {@link InputStream}.
     * @param flags Target {@link Base64} encoding flags.
     *
     * @return Associated encoded string.
     *
     * @throws IOException Failure to encode result.
     */
    public static String getBase64EncodedString(final InputStream inputStream, final int flags) throws IOException {
        final byte[] buffer = new byte[8192];

        int bytesRead;

        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }

        final byte[] bytes = output.toByteArray();

        return Base64.encodeToString(bytes, flags);
    }

    /**
     * @see #getBase64EncodedString(InputStream, int)
     */
    public static String getBase64EncodedString(final InputStream inputStream) throws IOException {
        return getBase64EncodedString(inputStream, Base64.NO_WRAP);
    }
}
