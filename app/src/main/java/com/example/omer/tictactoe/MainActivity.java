package com.example.omer.tictactoe;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    GridLayout gridLayout;
    int counter;
    int [][] gameBoard = new int[3][3];//0-empty cell,1-yellow,2-red
    int player = 1;//1 for yellow,2 - red
    MediaPlayer sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get grid width and set the height to be the same for perfect square
        gridLayout = findViewById(R.id.gridLayout);
        gridLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.LayoutParams layoutparent = gridLayout.getLayoutParams();
                layoutparent.height = gridLayout.getWidth();
                gridLayout.setLayoutParams(layoutparent);

                //remove the observer by sdk version(deprecated method)
                if(Build.VERSION.SDK_INT>15){gridLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);}else{
                    gridLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);}
            }
        });


        //set imageview views in layout grid
        for(int i=0;i<3;i++){

            for(int j=0;j<3;j++){
                final ImageView imageView = new ImageView(this);
                imageView.setTag(i+","+j);

                GridLayout.LayoutParams param= new GridLayout.LayoutParams(GridLayout.spec(
                        GridLayout.UNDEFINED,GridLayout.FILL,1f),
                        GridLayout.spec(GridLayout.UNDEFINED,GridLayout.FILL,1f));
                param.height = 0;
                param.width = 0;
                imageView.setLayoutParams(param);
                imageView.setPadding(30,30,30,30);
                //imageView.setForegroundGravity(Gravity.CENTER);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        ImageView imagepress = (ImageView)view;
                        float originalPosition = imagepress.getY();

                        imagepress.setY(-imagepress.getHeight());

                        String[] move = imagepress.getTag().toString().split(",");

                        int row = Integer.parseInt(move[0]);
                        int column = Integer.parseInt(move[1]);
                        gameBoard[row][column] = player;
                        if(player==1){
                            imagepress.setImageResource(R.drawable.yellow);
                        }else{
                            imagepress.setImageResource(R.drawable.red);
                        }
                        imagepress.animate().translationYBy(originalPosition+imagepress.getHeight()).rotationBy(360).setDuration(500);

                        //play sound when animation stop - in a different thread by injecting context
                        new Timer().schedule(new PlaySound(getApplicationContext()),500);
                        imagepress.setClickable(false);
                        counter++;
                        checkWinner(row,column);

                    }
                });

                gridLayout.addView(imageView);
            }

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        sound = MediaPlayer.create(this, R.raw.victory);
    }

    public void resetBtn(View view) {
        resetGame(); }


    public void checkWinner(int row,int column){

        //first of all check if there are at least 5 moves - the minimum for winning
        if(counter>=5&&checkBoard(row,column)){

            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("You Won!");
            alertDialog.setMessage("Congratulation!");
            if(player==1){
                alertDialog.setIcon(R.drawable.yellow);}else{
                alertDialog.setIcon(R.drawable.red);
            }
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "reset game", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    resetGame();
                    alertDialog.dismiss();
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

            //disable all view clickable function when winning the game
            for(int i=0;i<gridLayout.getChildCount();i++){
                View v = gridLayout.getChildAt(i);
                v.setClickable(false);
            }

            sound.start();
            sound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                }
            });
        }else{//if the player didn't win

            //check if all moves are played
            if(counter==Math.pow(gameBoard.length,2)){
                sound = MediaPlayer.create(this, R.raw.fail);
                sound.start();
                sound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.release();
                    }
                });
                Toast.makeText(this, "It's A Tie!", Toast.LENGTH_SHORT).show();}

            if(player==1){player++;}else{player--;}

        }

    }

    public boolean checkBoard(int row,int column){

        int matches,i;
        //check row
        for(i=0,matches=0;i<gameBoard[row].length&&gameBoard[row][i]==player;i++){matches++;}
        if(matches==gameBoard[row].length){return true;}

        //check column
        for(i=0,matches=0;i<gameBoard.length&&gameBoard[i][column]==player;i++){matches++;}
        if(matches==gameBoard.length){return true;}

        //check if the chosen image is on the slant
        if(row==column||row+column==gameBoard.length-1){

            for(i=0,matches=0;i<gameBoard.length&&gameBoard[i][i]==player;i++){matches++;}
            if(matches==gameBoard.length){return true;}

            for(i=0;i<gameBoard.length&&gameBoard[gameBoard.length-1-i][i]==player;i++){matches++;}
            if(matches==gameBoard.length){return true;}

        }



        return false;

    }

    public void resetGame(){

        counter = 0;
        gameBoard = new int[3][3];
        onStart();
        for(int i=0;i<gridLayout.getChildCount();i++){
            ImageView child = (ImageView)gridLayout.getChildAt(i);
            child.setImageResource(0);
            child.setClickable(true);
        }

        Toast.makeText(getApplicationContext(),"The Game Has Been Reset!",Toast.LENGTH_LONG).show();

    }




}

class PlaySound extends TimerTask{

    Context context;

    //constractor for getting context
    PlaySound(Context context){
        this.context = context;
    }

    @Override
    public void run() {
        MediaPlayer mp = MediaPlayer.create(context, R.raw.bloops5);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
            }
        });

    }
}