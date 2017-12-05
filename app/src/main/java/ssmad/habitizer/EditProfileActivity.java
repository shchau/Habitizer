/*
 *  Class Name: EditProfileActivity
 *  Version: 0.5
 *  Date: November 13th, 2017
 *  Copyright (c) TEAM SSMAD, CMPUT 301, University of Alberta - All Rights Reserved.
 *  You may use, distribute, or modify this code under terms and conditions of the
 *  Code of Students Behaviour at University of Alberta
 */

package ssmad.habitizer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
/**
 * Edits Profile of user
 * @author Andrew
 * @version 0.5
 * @see UserProfile
 * @since 0.5
 */
public class EditProfileActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    public static String USER_NAME = "Username of current user will store here";
    public static final String FILENAME= "userProfiles.sav";

    private Integer REQUEST_GALLERY = 0;
    private Integer REQUEST_CAMERA = 1;
    private int PIC_MAX_SIZE = 65536;
    private static Bitmap pic;
    public Boolean selected = false;

    private static ArrayList<UserProfile> profileList = new ArrayList<UserProfile>();
    private Account userInfo;

    private TextView nmText;
    private TextView btText;
    private TextView gdText;
    private ImageButton imageButton;
    private ImageView imageV;
    private EditText nameText;
    private TextView birthdayText;
    private Spinner genderSpn;
    private Button confirmButton;
    private ArrayAdapter<CharSequence> adapter;

    private TextView nameDisplay;
    private TextView birthDisplay;
    private TextView genderDisplay;
    private Button followerButton;
    private Button followingButton;
    private Button editButton;
    private Button logoutButton;
    private LinearLayout nameLayout;
    private LinearLayout birthLayout;
    private LinearLayout genderLayout;
    private LinearLayout followLayout;
    private static String currentUser;
    private static Boolean fromSignup;


    /**
     * Called when activity starts, used for displaying profile
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_tab);

        Intent intent = getIntent();
        fromSignup = intent.getBooleanExtra("fromSignup", false);

        if (!fromSignup){
            DummyMainActivity.initTabs(DummyMainActivity.VIEW_EDIT_PROFILE, EditProfileActivity.this, intent);}

        nmText = (TextView) findViewById(R.id.nmText);
        btText = (TextView) findViewById(R.id.btText);
        gdText = (TextView) findViewById(R.id.gdText);
        imageButton = (ImageButton) findViewById(R.id.image_btn);
        imageV = (ImageView) findViewById(R.id.imageView);
        nameText = (EditText) findViewById(R.id.usr_name);
        birthdayText = (TextView) findViewById(R.id.birth_date);
        genderSpn = (Spinner) findViewById(R.id.gender_spn);
        confirmButton = (Button) findViewById(R.id.confirm_btn);
        adapter = ArrayAdapter.createFromResource(this, R.array.gender_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        genderSpn.setAdapter(adapter);

        nameDisplay = (TextView) findViewById(R.id.name_dis);
        birthDisplay = (TextView) findViewById(R.id.birth_dis);
        genderDisplay = (TextView) findViewById(R.id.gender_dis);
        followerButton = (Button) findViewById(R.id.follower_btn);
        followingButton = (Button) findViewById(R.id.following_btn);
        editButton = (Button) findViewById(R.id.edit_btn);
        logoutButton = (Button) findViewById(R.id.logout_btn);
        nameLayout = (LinearLayout) findViewById(R.id.lay_name);
        birthLayout = (LinearLayout) findViewById(R.id.lay_birth);
        genderLayout = (LinearLayout) findViewById(R.id.lay_gender);
        followLayout = (LinearLayout) findViewById(R.id.lay_follow);


        //final int pos = findUserProfile();

        if (fromSignup){
            onEditEvent();
        } else {
            onDisplayEvent();
        }

    }

    /**
     * Used for editing profile
     */
    private void onEditEvent(){
        final Boolean find = findUser();
        if (find) {
            nameText.setText(userInfo.getName());
            birthdayText.setText(userInfo.getBirthday());
            byte[] imageBytes = userInfo.getPortrait();

        imageButton.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0,
                    imageBytes.length));
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(EditProfileActivity.this);
                View dialog = inflater.inflate(R.layout.select_portrait, null);

                AlertDialog.Builder dialog_build = new AlertDialog.Builder(EditProfileActivity.this);

                dialog_build.setView(dialog);
                dialog_build
                        .setTitle("Select One");
                final AlertDialog alertDialog = dialog_build.create();
                alertDialog.show();

                alertDialog.findViewById(R.id.camera_btn).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        Intent cameraPic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraPic, REQUEST_CAMERA);
                    }
                });
                alertDialog.findViewById(R.id.gallery_btn).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        Intent galleryPic = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryPic, REQUEST_GALLERY);
                    }
                });
                alertDialog.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

            }
        });

        birthdayText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Calendar date = Calendar.getInstance();
                int year = date.get(Calendar.YEAR);
                int month = date.get(Calendar.MONTH);
                int day = date.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(EditProfileActivity.this,
                        EditProfileActivity.this, year, month, day);
                datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
                datePickerDialog.show();

            }
        });


        genderSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //get extra user name
                Intent intent = getIntent();
                String user = intent.getStringExtra("username");
                String password = intent.getStringExtra("password");
                //get bitmap from iamgeButton then transfer it to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Bitmap bitmap;
                // If image is selected from camera or gallery, selected image will be converted
                // to bitmap for saving. Otherwise, image in imageview would be converted
                if (selected) {
                    bitmap = ((BitmapDrawable) imageButton.getDrawable()).getBitmap();
                } else {
                    bitmap = ((BitmapDrawable) imageV.getDrawable()).getBitmap();
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] imageByte = stream.toByteArray();

                String name = nameText.getText().toString();
                String birthday = birthdayText.getText().toString();
                String gender = genderSpn.getSelectedItem().toString();

                Boolean profileOk = checkInput();
                Account info;


                if (!find){
                    if (profileOk) {
                        userInfo = new Account(user, password, imageByte, name, birthday, gender);
                        saveUser(userInfo);
                        setResult(DummyMainActivity.VIEW_LOGIN, new Intent());
                        finish();
                    }
                } else {
                    if (profileOk) {
                        userInfo.setPortrait(imageByte);
                        userInfo.setName(name);
                        userInfo.setBirthday(birthday);
                        userInfo.setGender(gender);
                        saveUser(userInfo);
                        onDisplayEvent();
                    }

                }
            }
        });
    }

    /**
     * Send user back
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("username", currentUser);
        setResult(DummyMainActivity.VIEW_EDIT_PROFILE,intent);
        super.onBackPressed();
    }

    /**
     * Displays Profile
     */
    private void onDisplayEvent(){
        findUser();
        String name = userInfo.getName();
        String birthday = userInfo.getBirthday();
        String gender = userInfo.getGender();
        byte[] imageBytes = userInfo.getPortrait();

        displayVisibility();

        nameDisplay.setText(name);
        birthDisplay.setText(birthday);
        genderDisplay.setText(gender);
        //set up image
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        imageV.setImageBitmap(bitmap);

        logoutButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                setResult(DummyMainActivity.VIEW_HABIT);
                finish();
            }
        });

        followerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                followerEvent();
            }
        });

        followingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                followingEvent();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editVisibility();
                onEditEvent();
            }
        });
    }

    /**
     * Display visibility setting
     */
    private void displayVisibility(){
        imageButton.setVisibility(View.GONE);
        nmText.setVisibility(View.GONE);
        nameText.setVisibility(View.GONE);
        btText.setVisibility(View.GONE);
        birthdayText.setVisibility(View.GONE);
        gdText.setVisibility(View.GONE);
        genderSpn.setVisibility(View.GONE);
        confirmButton.setVisibility(View.GONE);

        imageV.setVisibility(View.VISIBLE);
        nameLayout.setVisibility(View.VISIBLE);
        birthLayout.setVisibility(View.VISIBLE);
        genderLayout.setVisibility(View.VISIBLE);
        followLayout.setVisibility(View.VISIBLE);
        editButton.setVisibility(View.VISIBLE);
        logoutButton.setVisibility(View.VISIBLE);
    }

    /**
     * Editing display visability
     */
    private void editVisibility(){
        imageButton.setVisibility(View.VISIBLE);
        nmText.setVisibility(View.VISIBLE);
        nameText.setVisibility(View.VISIBLE);
        btText.setVisibility(View.VISIBLE);
        birthdayText.setVisibility(View.VISIBLE);
        gdText.setVisibility(View.VISIBLE);
        genderSpn.setVisibility(View.VISIBLE);
        confirmButton.setVisibility(View.VISIBLE);

        nameLayout.setVisibility(View.GONE);
        birthLayout.setVisibility(View.GONE);
        genderLayout.setVisibility(View.GONE);
        followLayout.setVisibility(View.GONE);
        editButton.setVisibility(View.GONE);
        imageV.setVisibility(View.GONE);
        logoutButton.setVisibility(View.GONE);
    }

    /**
     * Sets birthday date for profile
     * @param view
     * @param year
     * @param month
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String userBirthday = Integer.toString(year) + '-' + Integer.toString(month) + '-' + Integer.toString(dayOfMonth);
        birthdayText.setText(userBirthday);
    }

    /**
     * Choose between camera and gallery for picture
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            selected = true;
            if(requestCode == REQUEST_CAMERA){
                selectFromCamera(data);
            } else if(requestCode == REQUEST_GALLERY){
                selectFromGallery(data);
            }
        }
    }

    /**
     * Select an image from gallery
     * @param data
     */
    private void selectFromGallery(Intent data){
        Uri imageUri = data.getData();
        try {
            pic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            Toast.makeText(EditProfileActivity.this, "Getting bitmap failed!", Toast.LENGTH_SHORT).show();
        }
        Bitmap compressedPic = getResizedBitmap(pic, PIC_MAX_SIZE);
        imageButton.setImageBitmap(compressedPic);
    }

    /**
     * Select image from camera
     * @param data
     */
    private void selectFromCamera(Intent data){
        pic = (Bitmap) data.getExtras().get("data");
        Bitmap compressedPic = getResizedBitmap(pic, PIC_MAX_SIZE);
        imageV = (ImageView) findViewById(R.id.imageView);
        imageButton.setImageBitmap(compressedPic);
    }

    /**
     * Finds user from server
     * @return
     */
    public Boolean findUser() {
        currentUser = getIntent().getStringExtra("username");
        ElasticsearchController.GetUsersTask getUsersTask = new ElasticsearchController.GetUsersTask();
        getUsersTask.execute(currentUser);
        try {
            if (!getUsersTask.get().isEmpty()) {
                userInfo = getUsersTask.get().get(0);
                Toast.makeText(EditProfileActivity.this, userInfo.getName(), Toast.LENGTH_SHORT).show();
                return true;
            }
        } catch (Exception e) {
            Log.i("Error", "Failed to get the user accounts from the async object");
        }
        return false;
    }

    /**
     * Checks for constraints on input and displays appropriate error message
     * @return
     */
    public Boolean checkInput() {
        Boolean correctness = true;
        String name = nameText.getText().toString();
        String date = birthdayText.getText().toString();
        String gender = genderSpn.getSelectedItem().toString();
        String[] part = date.split("-");

        if (name.isEmpty() || name.trim().equals("")) {
            correctness = false;
            Toast.makeText(EditProfileActivity.this, "Must Input a name", Toast.LENGTH_SHORT).show();
        } else if (!name.matches("[a-zA-Z]*")) {
            correctness = false;
            Toast.makeText(EditProfileActivity.this, "Input name must in a-z or A-Z", Toast.LENGTH_SHORT).show();
        } else if (gender.isEmpty()) {
            correctness = false;
            Toast.makeText(EditProfileActivity.this, "Must select a gender", Toast.LENGTH_SHORT).show();
        } else if (date.isEmpty()) {
            correctness = false;
            Toast.makeText(EditProfileActivity.this, "Must select a birthday", Toast.LENGTH_LONG).show();
        } else if (Integer.parseInt(part[1]) > 12 || Integer.parseInt(part[2]) > 31) {
            correctness = false;
            Toast.makeText(EditProfileActivity.this, "Invalid date",
                    Toast.LENGTH_LONG).show();
        }
        return correctness;
    }

    /**
     * Gets resized bitmap
     * @param pic
     * @param maxSize
     * @return
     */
    public Bitmap getResizedBitmap(Bitmap pic, int maxSize) {

        double div = 90.0;
        Bitmap image = pic.copy(pic.getConfig(), true);
        int size = image.getAllocationByteCount();

        int quality;
        while (size >= maxSize) {
            quality = (int) (div);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            pic.compress(Bitmap.CompressFormat.JPEG, quality, stream);

            byte[] byteArray = stream.toByteArray();
            image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            size = byteArray.length;
            //Log.d("PhotoSize|Quality", size + " | " + quality);
            div = div * 0.9;
        }
        return image;
    }


    /**
     * Saves user to server
     * @param info
     */
    public void saveUser(Account info) {
        //post user profile
        userInfo = info;
        ElasticsearchController.AddUsersTask addUsersTask = new ElasticsearchController.AddUsersTask();
        addUsersTask.execute(userInfo);
    }


    public void followerEvent(){
        //Not handling with this event for now
    }

    public void followingEvent() {
        //Not handling with this event for now
    }
}