package com.miguelgaeta.media_picker;

/**
 * Created by Miguel Gaeta on 2/11/16.
 */
public enum RequestType {

    CAMERA,
    GALLERY,
    DOCUMENTS,
    CROP,
    CHOOSER;

    /**
     * Internally get the associated request code to used in
     * the activity intent system.
     *
     * @return Unique request code for each operation.
     */
    int getCode() {
        switch (this) {
            case CAMERA:
                return 777;
            case GALLERY:
                return 778;
            case DOCUMENTS:
                return 779;
            case CROP:
                return 800;
            case CHOOSER:
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
    static RequestType create(int code) {
        switch (code) {
            case 777:
                return CAMERA;
            case 778:
                return GALLERY;
            case 779:
                return DOCUMENTS;
            case 800:
                return CROP;
            case 801:
                return CHOOSER;
        }

        return null;
    }
}
