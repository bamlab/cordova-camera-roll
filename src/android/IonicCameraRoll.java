package com.drifty.cordova.cameraroll;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.media.ExifInterface;
import android.net.Uri;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

public class IonicCameraRoll extends CordovaPlugin {

	public final String ACTION_GET_PHOTOS = "getPhotos";

	public final String ACTION_SAVE = "saveToCameraRoll";

    private CallbackContext callbackContext;
    private DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

		if (action.equals(ACTION_GET_PHOTOS)) {
		    getPhotos(args.getInt(0));
            return true;
        } else if (action.equals(ACTION_SAVE)) {
            // Not implemented yet
        }
        return false;
    }

    /**
     * Fetch both full sized images and thumbnails via a single query.
     * Returns all images not in the Camera Roll.
     */
    private void getPhotos(int maxPhotoCount) throws JSONException {
        int photoCount = 0;
        boolean hasLimit = maxPhotoCount > 0;

        final String[] projection = { MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID };

        Context context = this.cordova.getActivity();
        Cursor thumbnailsCursor = context.getContentResolver().query( MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                projection, // Which columns to return
                null,       // Return all rows
                null,
                null);

        // Extract the proper column thumbnails
        int thumbnailColumnIndex = thumbnailsCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);

        boolean hasImage = thumbnailsCursor.moveToLast();
        while (hasImage && (!hasLimit || photoCount < maxPhotoCount)) {
            // Get the tiny thumbnail and the full image path
            int thumbnailImageID = thumbnailsCursor.getInt(thumbnailColumnIndex);
            String thumbnailPath = thumbnailsCursor.getString(thumbnailImageID);
            String fullImagePath = uriToFullImage(thumbnailsCursor, context);

            // Create the result object
            JSONObject json = new JSONObject();
            json.put("path", fullImagePath);
            json.put("thumbnailPath", thumbnailPath);
            json.put("orientation", getOrientation(fullImagePath));
            json.put("date", dateFromImagePath(fullImagePath));

            PluginResult r = new PluginResult(PluginResult.Status.OK, json);
            r.setKeepCallback(true);
            this.callbackContext.sendPluginResult(r);

            photoCount++;
            hasImage = thumbnailsCursor.moveToPrevious();
        }
        thumbnailsCursor.close();

        // Send empty JSON to indicate the end of photostreaming
        PluginResult r = new PluginResult(PluginResult.Status.OK, new JSONObject());
        r.setKeepCallback(true);
        this.callbackContext.sendPluginResult(r);
    }

    private static int getOrientation(String fullImagePath) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(fullImagePath);
        } catch (IOException e) {
            return 0;
        }
        if (exif == null) return 0;

        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) return 90;
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) return 180;
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) return 270;
        return 0;
    }

    /**
     * Get the path to the full image for a given thumbnail.
     */
    private static String uriToFullImage(Cursor thumbnailsCursor, Context context){
        String imageId = thumbnailsCursor.getString(thumbnailsCursor.getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID));

        // Request image related to this thumbnail
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor imagesCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, filePathColumn, MediaStore.Images.Media._ID + "=?", new String[]{imageId}, null);

        if (imagesCursor != null && imagesCursor.moveToFirst()) {
            int columnIndex = imagesCursor.getColumnIndex(filePathColumn[0]);
            String filePath = imagesCursor.getString(columnIndex);
            imagesCursor.close();
            return filePath;
        } else {
            imagesCursor.close();
            return "";
        }
    }

    private long dateFromImagePath(String path) {
        ExifInterface intf = null;
        try {
            intf = new ExifInterface(path);
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        if (intf != null) {
            String date = intf.getAttribute(ExifInterface.TAG_DATETIME);
            if (date != null) {
                try {
                    return formatter.parse(date).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                File file = new File(path);
                if (file.exists()) {
                    return file.lastModified();
                }
            }
        }

        return 0;
    }
}
