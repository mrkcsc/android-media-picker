package com.miguelgaeta.media_picker;

import android.content.Intent;
import android.os.Build;

@SuppressWarnings({"WeakerAccess", "SpellCheckingInspection"})
public class Filter {

    private final String[] mimetypes;

    private Filter(final String... mimetypes) {
        this.mimetypes = mimetypes;
    }

    public static Filter fromMimeTypes(final String... mimetypes) {
        return new Filter(mimetypes);
    }

    public static Filter empty() {
        return new Filter();
    }

    Intent getIntent(final String action) {
        final Intent intent = new Intent(action);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType("*/*");

            if (mimetypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            }
        } else {
            intent.setType(getMimetypeString());
        }

        return intent;
    }

    private String getMimetypeString() {
        final StringBuilder builder = new StringBuilder();

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
}
