package com.miguelgaeta.media_picker;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

/**
 * Exposes methods for extracting mime type given a
 * target url or uri.  Uses a combination of
 * extraction methods to heuristically determine a
 * best guess for the associated type.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class MimeType {

    private static final MimeTypeMap MIME_TYPE_MAP = MimeTypeMap.getSingleton();

    /**
     * Convenience method to determine if a given
     * mime type is an image.
     *
     * @param mimeType Mime type string.
     *
     * @return True if image, false otherwise.
     */
    public boolean isImage(final String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * Get a best guess of the mime type associated with the target {@link Uri}.
     *
     * @param context Optional {@link Context} that checks for mime type from {@link ContentResolver}.
     * @param uri Target {@link Uri} to obtain mime type for.
     *
     * @return Discovered mime type or null.
     */
    public static String getMimeType(final Context context, final Uri uri) {
        if (uri == null) {
            return null;
        }

        if (context != null) {
            final String mimeType = getMimeTypeFromContentResolver(context.getContentResolver(), uri);

            if (mimeType != null) {
                return mimeType;
            }
        }

        return getMimeTypeFromExtension(uri.toString());
    }

    /**
     * @see #getMimeType(Context, Uri)
     */
    public static String getMimeType(final Context context, final String url) {
        if (url == null) {
            return null;
        }

        final Uri uri = Uri.parse(url);

        return getMimeType(context, uri);
    }

    /**
     * Uses {@link ContentResolver#getType(Uri)} to extract a mime type.
     *
     * @param contentResolver {@link ContentResolver}.
     * @param uri {@link Uri}.
     *
     * @return Discovered mime type or null.
     */
    private static String getMimeTypeFromContentResolver(final ContentResolver contentResolver,
                                                         final Uri uri) {
        if (uri == null || uri.getScheme() == null || contentResolver == null) {
            return null;
        }

        if (!uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            return null;
        }

        return contentResolver.getType(uri);
    }

    /**
     * Uses {@link MimeTypeMap#getMimeTypeFromExtension(String)} to extract a mime type.
     *
     * @param url {@link Uri}.
     *
     * @return Discovered mime type or null.
     */
    private static String getMimeTypeFromExtension(final String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);

        if (extension != null) {
            extension = extension.toLowerCase();

            return MIME_TYPE_MAP.getMimeTypeFromExtension(extension);
        }

        return null;
    }
}
