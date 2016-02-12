package com.miguelgaeta.media_picker;

/**
 * Created by Miguel Gaeta on 2/11/16.
 */
public enum MediaPickerRequest {

    REQUEST_CAPTURE,
    REQUEST_GALLERY,
    REQUEST_DOCUMENTS,
    REQUEST_CROP,
    REQUEST_CHOOSER;

    /**
     * Internally get the associated request code to used in
     * the activity intent system.
     *
     * @return Unique request code for each operation.
     */
    int getCode() {

        switch (this) {

            case REQUEST_CAPTURE:

                return 777;

            case REQUEST_GALLERY:

                return 778;

            case REQUEST_DOCUMENTS:

                return 779;

            case REQUEST_CROP:

                return 800;

            case REQUEST_CHOOSER:

                return 801;
        }

        return -1;
    }

    /**
     * Convert request code integer back into a user
     * friendly enum.
     *
     * @param code Request code integer.
     *
     * @return Request enum.
     */
    static MediaPickerRequest create(int code) {

        switch (code) {

            case 777:

                return REQUEST_CAPTURE;

            case 778:

                return REQUEST_GALLERY;

            case 779:

                return REQUEST_DOCUMENTS;

            case 800:

                return REQUEST_CROP;

            case 801:

                return REQUEST_CHOOSER;
        }

        return null;
    }
}
