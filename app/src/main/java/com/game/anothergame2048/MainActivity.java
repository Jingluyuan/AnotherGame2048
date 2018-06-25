package com.game.anothergame2048;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements Game2048Layout.OnGame2048Listener{

    private Game2048Layout mGame2048Layout;

    private TextView mScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScore = findViewById(R.id.score);

        mGame2048Layout = findViewById(R.id.game_2048);
        mGame2048Layout.setOnGame2048Listener(this);
    }

    @Override
    public void onScoreChange(int score)
    {
        mScore.setText("SCORE: "+ score);
    }

    @Override
    public void onGameOver()
    {
        new AlertDialog.Builder(this).setTitle("GAME OVER")
                .setMessage("YOU HAVE GOT "+ mScore.getText())
                .setPositiveButton("RESTART", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mGame2048Layout.restart();
                    }
                })
                .setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).show();
    }
}
