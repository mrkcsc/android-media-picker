package com.miguelgaeta.media_picker;

import android.content.ContentUris;
import android.content.Context;
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

            throw new IOException("Context cannot be null.");
        }

        if (uri == null) {

            throw new IOException("Uri cannot be null.");
        }

        final String path = getPath(context, uri);

        if (path == null) {

            throw new IOException("Path was not found.");
        }

        if (isLocal(path)) {

            throw new IOException("Path must be a local Uri.");
        }

        return new File(path);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {

                if (uri.getAuthority() == null) {

                    throw new IOException("Uri authority cannot be null.");
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

                        return getDataColumn(context, contentUriAppended, null, null);
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

                        return getDataColumn(context, contentUri, selection, selectionArgs);
                    }
                    default:

                        throw new IOException("Unknown URI document authority encountered: " + uri.getAuthority());
                }
            }
        }

        if (uri.getScheme() == null) {

            throw new IOException();
        }

        switch (uri.getScheme()) {

            case "content":

                if (AUTHORITY_GOOGLE_PHOTOS.equals(uri.getAuthority())) {

                    return uri.getLastPathSegment();
                }

                return getDataColumn(context, uri, null, null);

            case "file":

                return uri.getPath();

            default:
                throw new IOException("Unknown URI scheme encountered: " + uri.getScheme());
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
    private static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;

        final String column = "_data";
        final String[] projection = {
            column
        };

        try {

            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                null);
            if (cursor != null && cursor.moveToFirst()) {

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }

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
