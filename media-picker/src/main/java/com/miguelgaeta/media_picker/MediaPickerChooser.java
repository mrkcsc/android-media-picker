package com.miguelgaeta.media_picker;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Miguel Gaeta on 2/11/16.
 */
class MediaPickerChooser {

    /**
     * Gets an chooser intent that attempts to discover all activities on
     * the device that can be used to select media.
     *
     * @param packageManager Device {@link PackageManager}.
     * @param chooserTitle Title for the chooser.
     * @param captureFileURI Capture result URI for camera.
     *
     * @return Chooser {@link Intent}
     *
     * @throws IOException
     */
    static Intent getMediaChooserIntent(final PackageManager packageManager,
                                        final String chooserTitle,
                                        final Uri captureFileURI,
                                        final Filter filter) throws IOException {

        final Collection<Intent> intents = getMediaActivityIntents(packageManager, captureFileURI, filter);

        if (intents.isEmpty()) {

            throw new IOException("No media applications available on this device.");
        }

        final Iterator<Intent> iterator = intents.iterator();

        final Intent firstIntent = iterator.next();

        final List<Intent> remainingIntents = new ArrayList<>();

        while (iterator.hasNext()) {

            remainingIntents.add(iterator.next());
        }

        final Intent chooserIntent = Intent.createChooser(firstIntent, chooserTitle);

        if (!remainingIntents.isEmpty()) {

            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, remainingIntents.toArray(new Parcelable[remainingIntents.size()]));
        }

        return chooserIntent;
    }

    /**
     * Find a collection of all matching media intents on the device.
     *
     * @param packageManager Device {@link PackageManager}.
     * @param captureFileURI Capture result URI for camera.
     *
     * @return Collection of media activity intents.
     */
    private static Collection<Intent> getMediaActivityIntents(final PackageManager packageManager,
                                                              final Uri captureFileURI,
                                                              final Filter filter) {

        final Intent typeCamera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final Intent typeGallery = filter.getIntent(Intent.ACTION_PICK);
        final Intent typeDocuments = filter.getIntent(Intent.ACTION_GET_CONTENT);

        final Map<String, Intent> intents = new LinkedHashMap<>();

        getIntentActivities(intents, packageManager, typeDocuments, null);
        getIntentActivities(intents, packageManager, typeGallery, null);
        getIntentActivities(intents, packageManager, typeCamera, new IntentModifier() {

            @Override
            public void onFoundIntent(Intent intent) {

                intent.putExtra(MediaStore.EXTRA_OUTPUT, captureFileURI);
            }
        });

        return intents.values();
    }

    /**
     * Given a filter, create a list of all matching activities and
     * return them as a list of intents.
     *
     * @param packageManager Device {@link PackageManager}.
     * @param filterIntent Filter by {@link Intent}.
     * @param modifier Callback to modify each matched {@link Intent}.
     */
    private static void getIntentActivities(final Map<String, Intent> intents, final PackageManager packageManager, final Intent filterIntent, final IntentModifier modifier) {

        for (final ResolveInfo resolveInfo : packageManager.queryIntentActivities(filterIntent, 0)) {

            final ComponentName componentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
            final Intent intent = new Intent(filterIntent);

            intent.setComponent(componentName);
            intent.setPackage(resolveInfo.activityInfo.packageName);

            if (modifier != null) {
                modifier.onFoundIntent(intent);
            }

            intents.put(resolveInfo.activityInfo.packageName, intent);
        }
    }

    private interface IntentModifier {

        void onFoundIntent(final Intent intent);
    }
}
