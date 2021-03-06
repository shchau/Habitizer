/*
 *  Class Name: FeedTabActivity
 *  Version: 1.0
 *  Date: November 13th, 2017
 *  Copyright (c) TEAM SSMAD, CMPUT 301, University of Alberta - All Rights Reserved.
 *  You may use, distribute, or modify this code under terms and conditions of the
 *  Code of Students Behaviour at University of Alberta
 */
package ssmad.habitizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *Activity for displaying social related aspects of the app
 * @author Sadman
 * @version 1.0
 * @since 0.5
 */
public class FeedTabActivity extends AppCompatActivity {
    ListView myHabitEventsListView;
    public static final int VIEWING = 234;


    private static Boolean toClear = false;
    @Override
    protected void onStart() {
        super.onStart();
        myHabitEventsListView = (ListView) findViewById(R.id.feed_list);

        DummyMainActivity.myHabitEventsAdapter = new MyFeedAdapter(this, DummyMainActivity
                .myHabitEvents);
        myHabitEventsListView.setAdapter(DummyMainActivity.myHabitEventsAdapter);
        //TODO change here
        DummyMainActivity.myHabitEventsAdapter.notifyDataSetChanged();
        Intent intent = getIntent();
        if (intent.getBooleanExtra("fromHabit", false)){
            refreshFeed2();
        } else{
            refreshFeed(null);
        }


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_tab);
        myHabitEventsListView = (ListView) findViewById(R.id.feed_list);

        myHabitEventsListView.setAdapter(DummyMainActivity.myHabitEventsAdapter);
        Intent intent = getIntent();
        DummyMainActivity.initTabs(DummyMainActivity.VIEW_FEED, this, intent);

        final Button clearbtn = (Button) findViewById(R.id.clear_button);

        (findViewById(R.id.view_map)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedTabActivity.this, ViewMapActivity.class);
                startActivityForResult(intent, VIEWING);
            }
        });
        final Activity context = this;
        clearbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toClear = false;
                clearbtn.setVisibility(View.GONE);
                ((EditText) context.findViewById(R.id.search_input)).setText("");
                refreshFeed(null);
            }
        });
        (findViewById(R.id.search_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = ((EditText) context.findViewById(R.id.search_input)).getText().toString().trim();
                toClear = false;
                if (word.equals("")){
                    DummyMainActivity.toastMe("Please enter a valid word", context);
                }else if(word.contains(" ")){
                    DummyMainActivity.toastMe("Please enter only one word", context);
                }else{
                    refreshFeed(word);
                    toClear = true;
                }
                if(toClear){
                    clearbtn.setVisibility(View.VISIBLE);
                }else{
                    clearbtn.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DummyMainActivity.myHabitEventsAdapter.notifyDataSetChanged();
        if(requestCode == VIEWING){
        }
    }

    private void refreshFeed(String word) {
        Type listType;
        if(Utilities.isNetworkAvailable(FeedTabActivity.this)){
            ElasticsearchController.GetFeedTask getHabitsArrayGetTask = new ElasticsearchController.GetFeedTask();

            String query = String.format(
                    "{\"bool\":{" +
                            "\"must\":[" +
                            "{ \"match\" :" +
                            "{\"username\": \"%s\"}" +
                            "}", DummyMainActivity.currentUser);
            if(word != null){
                query = query +  String.format("," +
                        "{ \"match\" : " +
                        "{\"comment\": \"%s\"}" +
                        "}" +
                        "]}}", word);
            }else{
                query = query + "]}}";
            }
            Log.d("FEED.TAB.QUERY", query);
            getHabitsArrayGetTask.execute(DummyMainActivity.Event_Index, query);
            try{
                JsonArray jsonHabits =  getHabitsArrayGetTask.get();

                Log.d("FEED.TAB.QUERY", "Error with eventarray");
                DummyMainActivity.myHabitEvents.clear();
                Log.d("FEED.TAB.QUERY", "Error in loop");
                Gson g = new Gson();
                Log.i("cnmlgb", g.toJson(jsonHabits));
                for (int i = 0; i < jsonHabits.size(); i++){
                    Log.d("FEED.TAB.QUERY", "Error in event conversion");
                    HabitEvent h = new HabitEvent();
                    JsonObject job  = jsonHabits.get(i).getAsJsonObject();
                    h.fromJsonObject(job);
                    DummyMainActivity.myHabitEvents.add(h);
                }
                Log.d("FEED.TAB.QUERY", "Error in sorting");
                Collections.sort(DummyMainActivity.myHabitEvents, new Comparator<HabitEvent>() {
                    @Override
                    public int compare(HabitEvent o1, HabitEvent o2) {
                        return -o1.getCompletionDate().compareTo(o2.getCompletionDate());
                    }
                });

            }catch (Exception e){
                Log.d("FEED.TAB.QUERY", "Adding habits events in login.");
            }
        }else{
            // TODO fix
            /*listType = new TypeToken<ArrayList<HabitEvent>>(){}.getType();
            DummyMainActivity.myHabitEvents.clear();
            ArrayList arr =  FileController.loadFromFile(FeedTabActivity.this, DummyMainActivity.HABITEVENTFILENAME, listType);
            if ( DummyMainActivity.myHabitEvents != null){
                DummyMainActivity.myHabitEvents.addAll(arr);
            }*/
        }
        DummyMainActivity.myHabitEventsAdapter.notifyDataSetChanged();
    }
    //TODO change here
    private void refreshFeed2()  {
        String habit_id = getIntent().getStringExtra("currentHabitId");
        Type listType;
        if(Utilities.isNetworkAvailable(FeedTabActivity.this)){
            ElasticsearchController.GetFeedTask getHabitsArrayGetTask = new ElasticsearchController.GetFeedTask();

            String query = String.format( " {\"match\" :" +
                    "{\"habitid\": \"%s\"}" +
                    "}", habit_id);
            query = query + "";
            getHabitsArrayGetTask.execute(DummyMainActivity.Event_Index, query);
            try{
                JsonArray jsonHabits =  getHabitsArrayGetTask.get();
                DummyMainActivity.myHabitEvents.clear();
                Log.i("cnmlgb", habit_id);
                Log.i("cnmlgb", query);
                Gson g = new Gson();
                Log.i("cnmlgb", g.toJson(jsonHabits));
                for (int i = 0; i < jsonHabits.size(); i++){
                    Log.i("cnmlgb", "passing2");
                    HabitEvent h = new HabitEvent();
                    JsonObject job  = jsonHabits.get(i).getAsJsonObject();
                    h.fromJsonObject(job);
                    DummyMainActivity.myHabitEvents.add(h);
                }
                Collections.sort(DummyMainActivity.myHabitEvents, new Comparator<HabitEvent>() {
                    @Override
                    public int compare(HabitEvent o1, HabitEvent o2) {
                        return -o1.getCompletionDate().compareTo(o2.getCompletionDate());
                    }
                });

            }catch (Exception e){
                Log.d("cnmlgb", "Adding habits events in login.");
            }
        }
        DummyMainActivity.myHabitEventsAdapter.notifyDataSetChanged();
    }
}
