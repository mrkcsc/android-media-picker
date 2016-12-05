package com.miguelgaeta.media_picker;

import android.content.Intent;
import android.os.Build;

@SuppressWarnings({"WeakerAccess", "SpellCheckingInspection"})
public class Filter {

    public enum Type {
        AUDIO, IMAGES, VIDEOS, TEXT, FILES;

        private String asMimetype() {
            switch (this) {
                case AUDIO:
                    return "audio/*";
                case IMAGES:
                    return "images/*";
                case VIDEOS:
                    return "videos/*";
                case TEXT:
                    return "text/*";
                case FILES:
                    return "application/*";
            }

            return "";
        }
    }

    private static final Filter EMPTY_FILTER = new Filter(null, null);

    private final Type[] types;
    private final String[] mimetypes;

    private Filter(final Type[] types, final String[] mimetypes) {
        this.types = types;
        this.mimetypes = mimetypes;
    }

    public static Filter from(final Type[] types, final String[] mimetypes) {
        return new Filter(types, mimetypes);
    }

    public static Filter fromType(final Type... types) {
        return from(types, null);
    }

    public static Filter fromMimetypes(final String... mimetypes) {
        return from(null, mimetypes);
    }

    public static Filter empty() {
        return EMPTY_FILTER;
    }

    Intent getIntent(final String action) {
        final Intent intent = new Intent(action);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType("*/*");

            final String[] mimetypes = getMimetypeArray();

            if (mimetypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, getMimetypeArray());
            }
        } else {
            intent.setType(getMimetypeString());
        }

        return intent;
    }

    private String getMimetypeString() {
        final StringBuilder builder = new StringBuilder();

        if (types != null) {
            for (final Type type : types) {
                builder.append(type.asMimetype());
                builder.append(",");
            }
        }

        if (mimetypes != null) {
            for (final String mimetype : mimetypes) {
                builder.append(mimetype);
                builder.append(",");
            }
        }

        if (builder.length() == 0) {
            builder.append("*/*");
        }

        return builder.toString();
    }

    private String[] getMimetypeArray() {
        final boolean hasTypes = types != null;
        final boolean hasMimetypes = mimetypes != null;

        final int size =
            (hasTypes ? types.length : 0) +
                (hasMimetypes ? mimetypes.length :0);

        final String[] mimetypes = new String[size];

        int index = 0;

        if (hasTypes) {
            for (int i = 0; i < this.types.length; i++) {
                mimetypes[i] = types[i].asMimetype();
            }
            index = types.length - 1;
        }

        if (hasMimetypes) {
            //noinspection ManualArrayCopy
            for (int i = 0; i < this.mimetypes.length; i++) {
                mimetypes[i + index] = this.mimetypes[i];
            }
        }

        return mimetypes;
    }
}
