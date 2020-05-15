package com.jeev.tuneup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MenuActivity extends AppCompatActivity {

    public static final String GENRE = "com.example.tuneup.GENRE";
    private Spinner genre_spinner;
    private String chosen_genre;
    private String[] genres = {
            "acoustic",
            "afrobeat",
            "alt-rock",
            "alternative",
            "blues",
            "classical",
            "club",
            "country",
            "dance",
            "dancehall",
            "death-metal",
            "deep-house",
            "disco",
            "edm",
            "electronic",
            "emo",
            "folk",
            "funk",
            "garage",
            "grunge",
            "hard-rock",
            "hardcore",
            "heavy-metal",
            "hip-hop",
            "indian",
            "indie",
            "indie-pop",
            "industrial",
            "j-pop",
            "jazz",
            "k-pop",
            "kids",
            "latin",
            "metal",
            "new-release",
            "opera",
            "party",
            "pop",
            "psych-rock",
            "punk",
            "punk-rock",
            "r-n-b",
            "rainy-day",
            "reggae",
            "reggaeton",
            "rock",
            "rock-n-roll",
            "salsa",
            "samba",
            "singer-songwriter",
            "ska",
            "songwriter",
            "soul",
            "synth-pop",
            "techno",
            "trance",
            "trip-hop",
            "world-music"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        genre_spinner = (Spinner) findViewById(R.id.genre_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, genres);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        genre_spinner.setAdapter(adapter);

        System.out.println(chosen_genre);
    }

    public void sendGenre(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        chosen_genre = String.valueOf(genre_spinner.getSelectedItem());
        intent.putExtra(GENRE, chosen_genre);
        startActivity(intent);
    }

}

