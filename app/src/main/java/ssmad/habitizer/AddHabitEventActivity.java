/*
 *  Class Name: AddHabitEventActivity
 *  Version: 1.0
 *  Date: November 13th, 2017
 *  Copyright (c) TEAM SSMAD, CMPUT 301, University of Alberta - All Rights Reserved.
 *  You may use, distribute, or modify this code under terms and conditions of the
 *  Code of Students Behaviour at University of Alberta
 */
package ssmad.habitizer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;


/**
 * Activity for adding in a Habit Event
 * @author Sadman, Simon (a little)
 * @version 0.5
 * @see HabitEvent
 * @since 0.5
 */
public class AddHabitEventActivity extends AppCompatActivity {
    public static final int GET_PIC_WITH_CAMERA = 0;
    public static final int GET_PIC_FROM_GALLERY = 1;
    //public static final int MY_PERMISSIONS_REQUEST_LOCATION = 8;
    public static final int PIC_MAX_SIZE = 65536;
    public static final int COMMENT_MAX_SIZE = 30;
    public static final int EVENT_PERMISSION_CHECK = 8;
    public static boolean picIsVisible = Boolean.FALSE;
    /*public static boolean picButtonsAreVisible = Boolean.FALSE;
    public static boolean mapIsVisible = Boolean.FALSE;
    public static boolean picWasChanged = Boolean.FALSE;
    public static boolean locWasChanged = Boolean.FALSE;*/
    public static double[] location;
    public static String comment;
    //public static Bitmap pic;
    public static byte[] picBytes;
    //public static GoogleMap gmap;

    /**
     * Called when activity starts
     * Takes input for habit event
     * Connects buttons to actions
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_habit_event);
        SyncController.sync(this);
        // Get stuff
        final int position = getIntent().getExtras().getInt(HabitTabActivity.GENERIC_REQUEST_CODE);
        final Habit habit = DummyMainActivity.myHabits.get(position);
        final TextView habitTitle = (TextView) findViewById(R.id.what_habit);
        (findViewById(R.id.add_title)).setVisibility(View.VISIBLE);
        final Button add = (Button) findViewById(R.id.add);
        Button cancel = (Button) findViewById(R.id.cancel);

        habitTitle.setText(habit.getTitle());
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEvent();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelEvent();
            }
        });
        setUpCheckBoxes(this);
        setUpPicButtons(this);

    }

    /**
     * Tries to get picture and makes add button available
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // OK
        super.onActivityResult(requestCode, resultCode, data);
        tryGetPic(this, requestCode, resultCode, data);
    }


    /**
     * Sets up checkboxes
     * @param ctx
     * @param btn
     */
    public static void setUpCheckBoxes(Activity ctx) {
        CheckBox locationCheck = (CheckBox) ctx.findViewById(R.id.location_check);
        CheckBox picCheck = (CheckBox) ctx.findViewById(R.id.pic_check);
        final Activity fctx = ctx;
        picBytes = null;
        picCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox thisBox = (CheckBox) v;
                picIsVisible = false;
                LinearLayout picToggle = (LinearLayout) fctx.findViewById(R.id.pic_toggle);
                if (thisBox.isChecked()) {
                    picToggle.setVisibility(View.VISIBLE);
                } else {
                    picToggle.setVisibility(View.GONE);
                }
            }


        });
        location = null;

        locationCheck.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                CheckBox thisBox = (CheckBox) v;
                LinearLayout mapToggle = (LinearLayout) fctx.findViewById(R.id.map_toggle);
                if (thisBox.isChecked()) {
                    mapToggle.setVisibility(View.VISIBLE);

                    MapController.initMap2(fctx, null, EVENT_PERMISSION_CHECK);

                } else {
                    mapToggle.setVisibility(View.GONE);
                    location = null;
                }
            }
        });
    }

    /**
     * Sets up buttons related to pictures
     * @param ctx
     * @param btn
     */
    public static void setUpPicButtons(Activity ctx) { // OK
        Button fromCamera = (Button) ctx.findViewById(R.id.pic_camera);
        Button fromGallery = (Button) ctx.findViewById(R.id.pic_gallery);
        final Activity fctx = ctx;
        fromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fctx.startActivityForResult(takePicture, GET_PIC_WITH_CAMERA);

            }
        });
        fromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                fctx.startActivityForResult(pickPhoto, GET_PIC_FROM_GALLERY);
            }
        });
    }

    /**
     * Sets location
     * @param loc
     */
    public static void setLocation(double[] loc){
        location = loc;
    }


    /**
     * Gets picture from bytes
     * @param bytes
     * @return
     */
    public static Bitmap getPicFromBytes(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Sends request to server for picture
     * @param ctx
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public static void tryGetPic(Activity ctx, int requestCode, int resultCode, Intent data) {

        switch (requestCode) {


            case GET_PIC_WITH_CAMERA:
                if (resultCode == RESULT_OK) {
                    Bitmap pic = (Bitmap) data.getExtras().get("data");
                    setPic(ctx, pic);
                    DummyMainActivity.toastMe("get pic from camera", ctx);
                } else {
                    getPictureFail(ctx);
                }
                break;
            case GET_PIC_FROM_GALLERY:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    try {
                        Bitmap pic = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(),
                                imageUri);
                        setPic(ctx, pic);
                    } catch (IOException e) {
                        getPictureFail(ctx);
                    }
                    DummyMainActivity.toastMe("get pic from gallery", ctx);
                } else {
                    getPictureFail(ctx);
                }
                break;
        }
    }

    /**
     * Called upon failure to get picture
     * @param ctx
     */
    public static void getPictureFail(Activity ctx) {
        DummyMainActivity.toastMe("Failed to get pic", ctx);
        ImageView picPreview = (ImageView) ctx.findViewById(R.id.pic_preview);
        picPreview.setImageBitmap(null);
    }

    /**
     * Sets picture
     * @param ctx
     * @param pic
     */
    public static void setPic(Activity ctx, Bitmap pic) { // OK
        picBytes = getCompressedByteFromBitmap(pic, PIC_MAX_SIZE);
        ImageView picPreview = (ImageView) ctx.findViewById(R.id.pic_preview);
        picPreview.setImageBitmap(pic);
        picPreview.setVisibility(View.VISIBLE);
        picIsVisible = true;

    }


    /**
     * Gets compressed byte from bitmap
     * @param pic
     * @param maxSize
     * @return
     */
    public static byte[] getCompressedByteFromBitmap(Bitmap pic, int maxSize) {
        double div = 100.0;
        ByteArrayOutputStream stream;
        int size;
        byte[] image;
        int quality;
        do {
            quality = (int) div;
            stream = new ByteArrayOutputStream();
            pic.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            image = stream.toByteArray();
            size = image.length;
            div = div * 0.9;
        } while (size >= maxSize);


        return image;
    }


    /**
     * For adding the event
     */
    public void addEvent() {
        int position = getIntent()
                .getExtras()
                .getInt(HabitTabActivity.GENERIC_REQUEST_CODE);
        Habit habit = DummyMainActivity.myHabits.get(position);
        String comment = ((EditText) findViewById(R.id.comment_input)).getText().toString();
        if (comment.length() > COMMENT_MAX_SIZE) {
            DummyMainActivity.toastMe("Comment must be less than " + COMMENT_MAX_SIZE + " chars",
                    this);
        } else {

            ElasticsearchController.AddItemsTask postHabitEvent =
                    new ElasticsearchController.AddItemsTask();

            String title = habit.getTitle();
            Date completeDate = new Date();
            HabitEvent habitEvent = new HabitEvent(title, completeDate, picBytes, location, comment);
            habitEvent.setHabit_id(habit.getId());
            habitEvent.setUsername(habit.getUsername());
            postHabitEvent.execute(DummyMainActivity.Event_Index, habitEvent.getJsonString());

            try {
                String id = postHabitEvent.get();
                if (id == null) {
                    throw new Exception("lol");
                }
                habitEvent.setId(id);
            } catch (Exception e) {
                Log.d("ESC", "Could not update habit event on first try.");
                String[] s = {
                        DummyMainActivity.Event_Index,
                        String.valueOf(SyncController.TASK_ADD),
                        habitEvent.getJsonString()
                };
                SyncController.addToSync(s, habitEvent);
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(completeDate);
            int[] daysCompleteList = habit.getDaysOfWeekComplete();
            int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if (day == 0) {
                day = 7;
            }
            daysCompleteList[day - 1] = 1;
            habit.setDaysOfWeekComplete(daysCompleteList);

            ElasticsearchController.UpdateItemsTask updateHabitTask = new ElasticsearchController.UpdateItemsTask();
            updateHabitTask.execute(DummyMainActivity.Habit_Index, habit.getId(), habit.getJsonString());
            finish();
        }

    }

    /**
     * Cancel add event
     */
    public void cancelEvent() {
        //setResult(0 ,new Intent());
        finish();
    }

    /**
     * Called on getting permission for location
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case EVENT_PERMISSION_CHECK: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {


                        DummyMainActivity.toastMe("Can now set location", this);
                        MapController.initMap2(this, null, EVENT_PERMISSION_CHECK);

                    }

                }else {
                    LinearLayout mapToggle = (LinearLayout) this.findViewById(R.id.map_toggle);
                    mapToggle.setVisibility(View.GONE);
                }
            }

        }
    }


}
