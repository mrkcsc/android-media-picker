package com.miguelgaeta.media_picker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by Miguel Gaeta on 2/10/16.
 */
@SuppressWarnings("UnusedDeclaration")
public class MediaPickerEncoder {

    private static final Bitmap.CompressFormat defaultFormat = Bitmap.CompressFormat.JPEG;
    private static final int defaultFormatQuality = 100;

    /**
     * Converts a bitmap into a Base64 encoded data URL.
     *
     * @param bitmap Input bitmap.
     * @param mimeType Input mime type.
     * @param format Compression format.
     * @param quality Compression quality.
     *
     * @return Base64 encoded data URL.
     *
     * @throws IOException
     */
    public static String toDataUrl(final Bitmap bitmap, final String mimeType, final Bitmap.CompressFormat format, final int quality) throws IOException {

        if (bitmap == null) {

            throw new IOException("Bitmap cannot be null.");
        }

        if (mimeType == null) {

            throw new IOException("Mime type cannot be null.");
        }

        return "data:" + mimeType + ";base64," + asEncodedBase64String(bitmap, format, quality);
    }

    /**
     * @see MediaPickerEncoder#toDataUrl(Bitmap, String, Bitmap.CompressFormat, int)
     */
    public static String toDataUrl(final Bitmap bitmap, final String mimeType) throws IOException {

        return toDataUrl(bitmap, mimeType, defaultFormat, defaultFormatQuality);
    }

    /**
     * Converts a file into a Base64 encoded data URL.
     *
     * @param filePath Path to file.
     * @param mimeType Optional file mime type.
     * @param format Compression format.
     * @param quality Compression quality.
     *
     * @return Base64 encoded data URL.
     *
     * @throws IOException
     */
    public static String toDataUrl(final String filePath, final String mimeType, final Bitmap.CompressFormat format, final int quality) throws IOException  {

        if (filePath == null) {

            throw new IOException("File path cannot be null.");
        }

        final BitmapFactory.Options opt = new BitmapFactory.Options();
        final Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        return toDataUrl(bitmap, mimeType != null ? mimeType : opt.outMimeType, format, quality);
    }

    /**
     * @see MediaPickerEncoder#toDataUrl(String, String, Bitmap.CompressFormat, int)
     */
    public static String toDataUrl(final String filePath, final String mimeType) throws IOException {

        return toDataUrl(filePath, mimeType, defaultFormat, defaultFormatQuality);
    }

    /**
     * @see MediaPickerEncoder#toDataUrl(String, String)
     */
    public static String toDataUrl(String filePath) throws IOException  {

        return toDataUrl(filePath, null);
    }

    /**
     * Converts a file into a Base64 encoded data URL.
     *
     * @param file Input {@link File}
     * @param mimeType Optional file mime type.
     * @param format Compression format.
     * @param quality Compression quality.
     *
     * @return Base64 encoded data URL.
     *
     * @throws IOException
     */
    public static String toDataUrl(final File file, String mimeType, final Bitmap.CompressFormat format, final int quality) throws IOException  {

        if (file == null) {

            throw new IOException("File cannot be null.");
        }

        return toDataUrl(file.getAbsolutePath(), mimeType, format, quality);
    }

    /**
     * @see MediaPickerEncoder#toDataUrl(File, String, Bitmap.CompressFormat, int)
     */
    public static String toDataUrl(final File file, String mimeType) throws IOException  {

        return toDataUrl(file, mimeType, defaultFormat, defaultFormatQuality);
    }

    /**
     * @see MediaPickerEncoder#toDataUrl(File, String)
     */
    public static String toDataUrl(File file) throws IOException  {

        return toDataUrl(file, null);
    }

    /**
     * Given a bitmap input, return it as an encoded Base64 string.
     *
     * @param bitmap Source {@link Bitmap} to encode.
     * @param format Specified compression {@link android.graphics.Bitmap.CompressFormat}.
     * @param quality Desired compression quality.
     *
     * @return An encoded Base64 string representation of the input bitmap.
     *
     * @throws IOException Can throw on {@link ByteArrayOutputStream} close or invalid bitmap input.
     */
    private static String asEncodedBase64String(final Bitmap bitmap, final Bitmap.CompressFormat format, final int quality) throws IOException {

        if (bitmap == null || format == null) {

            throw new IOException("Cannot encode a null bitmap or compression format.");
        }

        final ByteArrayOutputStream bitmapData = new ByteArrayOutputStream();

        bitmap.compress(format, quality, bitmapData);

        final String encodedData = Base64.encodeToString(bitmapData.toByteArray(), Base64.NO_WRAP);

        bitmap.recycle();
        bitmapData.close();

        return encodedData;
    }
}
