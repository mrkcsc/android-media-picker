package com.miguelgaeta.media_picker;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

/**
 * Created by Miguel Gaeta on 7/20/15.
 *
 * File chooser library: https://github.com/iPaulPro/aFileChooser
 */
@SuppressWarnings("UnusedDeclaration")
public class MediaPickerUri {

    private static final String AUTHORITY_GOOGLE_PHOTOS = "com.google.android.apps.photos.content";
    private static final String AUTHORITY_EXTERNAL_STORAGE = "com.android.externalstorage.documents";
    private static final String AUTHORITY_DOWNLOADS_DOCUMENT = "com.android.providers.downloads.documents";
    private static final String AUTHORITY_MEDIA_DOCUMENT = "com.android.providers.media.documents";

    /**
     * Convert a Uri into a file if possible.
     *
     * @see #getPath(Context, Uri)
     *
     * @param context Android application or activity context.
     * @param uri Source Uri.
     *
     * @return Associated file on device.
     *
     * @throws IOException
     */
    public static File resolveToFile(Context context, Uri uri) throws IOException {

        if (context == null) {

            throw new IOException("A valid android application context is required.");
        }

        if (uri == null) {

            throw new IOException("File URI cannot be null.");
        }

        final String path = getPath(context, uri);

        if (path == null) {

            throw new IOException("File path was not found.");
        }

        if (!isLocal(path)) {

            throw new IOException("File path was found, but path must be a local URI.");
        }

        final File file = new File(path);

        if (!file.exists()) {

            throw new IOException("File path was found, but file does not exist.");
        }

        return new File(path);
    }

    /**
     * @see #resolveToFile(Context, Uri)
     */
    public static File resolveToFile(final Context context, final Intent intentUri, final String name) throws IOException {

        final Uri uri = intentUri.getParcelableExtra(name);

        return resolveToFile(context, uri);
    }

    /**
     * @see #resolveToFile(Context, Uri)
     */
    public static File resolveToFile(final Context context, final Intent intentUri) throws IOException {

        return resolveToFile(context, intentUri, Intent.EXTRA_STREAM);
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     */
    private static String getPath(final Context context, final Uri uri) throws IOException {

        if (isDocumentsProviderUri(context, uri)) {

            return getPathFromDocumentsProvider(context, uri);
        }

        if (uri.getScheme() == null) {

            throw new IOException("Unknown URI scheme encountered.");
        }

        switch (uri.getScheme()) {

            case "content":

                if (AUTHORITY_GOOGLE_PHOTOS.equals(uri.getAuthority())) {

                    return uri.getLastPathSegment();
                }

                return getDataColumn(context, uri, null, null);

            case "file":

                final String path = uri.getPath();

                if (path == null) {

                    throw new IOException("URI file path cannot be null.");
                }

                return path;

            default:
                throw new IOException("Unknown URI scheme encountered: " + uri.getScheme());
        }
    }

    /**
     * Identity if the provided {@link Uri} is from the document provider.
     *
     * @param context Android {@link Context} object.
     * @param uri Android {@link Uri} object.
     *
     * @return True if the provided {@link Uri} belongs to the documents provided.
     */
    private static boolean isDocumentsProviderUri(final Context context, final Uri uri) {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri);
    }

    /**
     * Extract a file path from the documents provided.  If a path cannot be
     * found throws an IOException.
     *
     * @param context Android {@link Context} object.
     * @param uri Android {@link Uri} object.
     *
     * @return Discovered file path.
     *
     * @throws IOException Throw from any null file path or unexpected authority.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getPathFromDocumentsProvider(final Context context, final Uri uri) throws IOException {

        if (uri.getAuthority() == null) {

            throw new IOException("URI authority cannot be null.");
        }

        final String documentId = DocumentsContract.getDocumentId(uri);

        switch (uri.getAuthority()) {

            case AUTHORITY_EXTERNAL_STORAGE: {

                final String[] split = documentId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {

                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                throw new IOException("Unable to handle non-primary external storage volumes.");
            }
            case AUTHORITY_DOWNLOADS_DOCUMENT: {

                final Uri contentUri = Uri.parse("content://downloads/public_downloads");
                final Uri contentUriAppended = ContentUris.withAppendedId(contentUri, Long.valueOf(documentId));

                final String path = getDataColumn(context, contentUriAppended, null, null);

                if (path == null) {
                    throw new IOException("Unable to find downloaded document path.");
                }

                return path;
            }
            case AUTHORITY_MEDIA_DOCUMENT: {

                final String[] split = documentId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                    split[1]
                };

                final String path = getDataColumn(context, contentUri, selection, selectionArgs);

                if (path == null) {
                    throw new IOException("Unable to find media document path.");
                }

                return path;
            }
            default:

                throw new IOException("Unknown URI document authority encountered: " + uri.getAuthority());
        }
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     *
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) throws IOException {

        Cursor cursor = null;

        final String column = "_data";
        final String[] projection = {
            column
        };

        try {

            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {

                return cursor.getString(cursor.getColumnIndexOrThrow(column));
            }

        } catch (IllegalArgumentException e) {

            throw new IOException("Unable to read data column for intent.", e);

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    /**
     * Checks if a target URL is local.
     *
     * @param url Source URL.
     *
     * @return Whether the URL is a local one.
     */
    private static boolean isLocal(String url) {

        return url != null && !url.startsWith("http://") && !url.startsWith("https://");
    }
}
