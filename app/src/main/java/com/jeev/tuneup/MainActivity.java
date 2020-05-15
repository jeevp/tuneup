package com.jeev.tuneup;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.jeev.tuneup.Connectors.SongService;
import com.jeev.tuneup.Model.Song;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = ""; // removed for safety
    private static final String REDIRECT_URI = "com.jeev.tuneup://callback";

    private TextView username;
    private TextView guess1;
    private TextView guess2;
    private TextView guess3;
    private TextView guess4;
    private TextView guessReveal;
    private TextView guessResponse;
    private TextView artistName;
    private ImageView albumArt;
    private TextView round;
    private TextView score;
    private TextView timer;
    private TextView gameCountdown;

    private Dialog guessPopup;

    private boolean isCounterRunning = false;
    private long timeRem;
    public static String genre = null;

    private Button addButton;
    private Song song;
    private SpotifyAppRemote mSpotifyAppRemote;
    private int correctGuesses = 0;
    private long scoreCount = 0;
    private int roundNum = 1;

    private SongService songService;
    private ArrayList<Song> recentlyPlayedTracks;
    private ArrayList<Song> recSongs;

    private MediaPlayer correctAnswerSound;
    private MediaPlayer wrongAnswerSound;

    CountDownTimer guessTimer;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        correctAnswerSound = MediaPlayer.create(this, R.raw.correct_answer);
        wrongAnswerSound = MediaPlayer.create(this, R.raw.wrong_answer);

        guessPopup = new Dialog(this);
        guessPopup.setContentView(R.layout.popup);

        guessResponse = (TextView) guessPopup.findViewById(R.id.guessResponse);
        guessReveal = (TextView) guessPopup.findViewById(R.id.guessReveal);
        artistName = (TextView) guessPopup.findViewById(R.id.artistName);
        albumArt = (ImageView) guessPopup.findViewById(R.id.albumArt);

        addButton = (Button) guessPopup.findViewById(R.id.addButton);
        addButton.setOnClickListener(addListener);

        Intent intent = getIntent();
        genre = intent.getStringExtra(MenuActivity.GENRE);

        songService = new SongService(getApplicationContext());
        //username = (TextView) findViewById(R.id.username);

        guess1 = (TextView) findViewById(R.id.guess1);
        guess1.setOnClickListener(guessListener);
        guess2 = (TextView) findViewById(R.id.guess2);
        guess2.setOnClickListener(guessListener);
        guess3 = (TextView) findViewById(R.id.guess3);
        guess3.setOnClickListener(guessListener);
        guess4 = (TextView) findViewById(R.id.guess4);
        guess4.setOnClickListener(guessListener);

        guess1.setVisibility(View.INVISIBLE);
        guess2.setVisibility(View.INVISIBLE);
        guess3.setVisibility(View.INVISIBLE);
        guess4.setVisibility(View.INVISIBLE);

        round = (TextView) findViewById(R.id.round);
        timer = (TextView) findViewById(R.id.timer);
        score = (TextView) findViewById(R.id.score);

        gameCountdown = (TextView) findViewById(R.id.gameCountdown);
        gameCountdown.bringToFront();

        guessTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRem = millisUntilFinished;
                timer.setText(String.format("%02d", (int) millisUntilFinished / 1000));
            }
            @Override
            public void onFinish() {
                isCounterRunning = false;
                wrongAnswerSound.start();
                guessResponse.setText("TIME OUT!");
                scoreCount -= 3000;
                guessReveal.setText(song.getName());
                guessPopup.show();
                roundNum++;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getRecTrack();
                        guessPopup.dismiss();
                    }
                }, 4000 );
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();

                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private void connected() {

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });

        mSpotifyAppRemote.getPlayerApi().pause();

        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                gameCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {

                getRecTrack();

                System.out.println("the countdown is over");
                guess1.setVisibility(View.VISIBLE);
                guess2.setVisibility(View.VISIBLE);
                guess3.setVisibility(View.VISIBLE);
                guess4.setVisibility(View.VISIBLE);

            }
        }.start();

    }

    private View.OnClickListener addListener = v -> {
        songService.addSongToLibrary(this.song);
        if (recSongs.size() > 0) {
            recSongs.remove(0);
        }
        System.out.println("ADDING SONG");
        //getRecTrack();
    };

    private View.OnClickListener guessListener = v -> {
        Button b = (Button) v;
        String buttonText = b.getText().toString();

        System.out.println("The guess was: " + buttonText + " but the answer was: " + song.getName());

        guessTimer.cancel();

        if (buttonText ==  song.getName()) {
            correctAnswerSound.start();
            guessResponse.setText("CORRECT!");
            scoreCount += timeRem / 10;
            correctGuesses++;
            guessPopup.getWindow().setBackgroundDrawableResource(android.R.color.holo_red_dark);
        }
        else {
            wrongAnswerSound.start();
            guessResponse.setText("WRONG!");
            scoreCount -= timeRem / 10;
            guessPopup.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
        }
        guessReveal.setText(song.getName());

        guessPopup.show();

        roundNum++;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getRecTrack();
                guessPopup.dismiss();
            }
        }, 4000 );

    };

    private void getRecTrack() {
        songService.getTrack(() -> {
            recSongs = songService.getRecSongs();
            updateRound();
        });
    }

    private void updateRound() {

        if (!isCounterRunning) {
            isCounterRunning = true;
            guessTimer.start();
        }
        else {
            guessTimer.cancel(); // cancel
            guessTimer.start();  // then restart
        }

        mSpotifyAppRemote.getPlayerApi().play("spotify:track:" + recSongs.get(0).getId());
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        artistName.setText(track.artist.name);
                        mSpotifyAppRemote.getImagesApi().getImage(track.imageUri).setResultCallback(new CallResult.ResultCallback<Bitmap>() {
                            @Override
                            public void onResult(Bitmap bitmap) {
                                albumArt.setImageBitmap(bitmap);
                            }
                        });
                    }
                });

        String paddedNum = String.format("%02d", roundNum);
        round.setText(paddedNum);

        score.setText(String.valueOf(scoreCount));

        ArrayList<String> songNames = new ArrayList<String>(
                Arrays.asList(recSongs.get(0).getName(), recSongs.get(1).getName(), recSongs.get(2).getName(), recSongs.get(3).getName()));

        Collections.shuffle(songNames);

        if (recSongs.size() > 0) {
            guess1.setText(songNames.get(0));
            guess2.setText(songNames.get(1));
            guess3.setText(songNames.get(2));
            guess4.setText(songNames.get(3));
            song = recSongs.get(0);
        }

        System.out.println(recSongs);
        recSongs.remove(0);
        recSongs.remove(0);
        recSongs.remove(0);
        recSongs.remove(0);
        System.out.println(recSongs);

    }
}
