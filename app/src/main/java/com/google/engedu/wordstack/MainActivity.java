/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;
    private Stack<LetterTile> placedTiles = new Stack();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();

                // This condition ensures only words of specific final variable length are added.
                if (word.length() == WORD_LENGTH) {
                    words.add(word);
                }
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
        //word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());

        View word2LinearLayout = findViewById(R.id.word2);
        //word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }
                // When a character is removed from stackedLayout this line pushes it onto
                // placedTiles. To know which characters have been placed by the user.
                placedTiles.push(tile);

                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    tile.moveToViewGroup((ViewGroup) v);
                    if (stackedLayout.empty()) {
                        TextView messageBox = (TextView) findViewById(R.id.message_box);
                        messageBox.setText(word1 + " " + word2);
                    }
                    // When the user drops a character (tile) into a layout this line pushes
                    // the tile onto a placedTiles stack to keep track of them.
                    placedTiles.push(tile);

                    return true;
            }
            return false;
        }
    }

    /* This method begins the game by clearing the linear layouts. Then, two words are randomly
     * chosen from the list of available words. There is a condition to ensure the words are not
     * identical. The scrambled word is concatenated by randomly choosing a character from
     * either word. When all the characters of a word have been used up, the remaining letters
     * from the other word are added. Lastly an array of letter tiles is created to store the
     * characters as letterTile objects.*/
    public boolean onStartGame(View view) {
        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");

        ViewGroup word1LinearLayout = findViewById(R.id.word1);
        word1LinearLayout.removeAllViews();

        ViewGroup word2LinearLayout = findViewById(R.id.word2);
        word2LinearLayout.removeAllViews();

        stackedLayout.clear();

        Boolean t = true;
        int listLength = words.size();
        int rn, rn2;
        rn = rn2 = 0;

        while(t) {
            rn = random.nextInt(listLength);
            rn2 = random.nextInt(listLength);
            if(rn != rn2) {
                t = false;
            }
        }

        word1 = words.get(rn);
        word2 = words.get(rn2);

        int count1 = 0;
        int count2 = 0;

        String scramWord = "";

        Boolean loop = true;
        Boolean full1 = true;
        Boolean full2 = true;

        while (loop) {
            int rando = random.nextInt(2);
            if (rando == 0 && full1) {
                scramWord += word1.charAt(count1);
                if(count1 <= 4) {
                    count1++;
                }
                if (count1 > 4) {
                    full1 = false;
                }
            }
            else if (rando == 1 && full2) {
                scramWord += word2.charAt(count2);
                if (count2 <= 4) {
                    count2++;
                }
                if (count2 > 4) {
                    full2 = false;
                }
            }

            // Ends the while loop when all characters have been added to the scrambled word.
            if (!full1 && !full2) {
                loop = false;
            }
        }

        // Outputs to the log the words and scrambled string.
        Log.d("Word1", word1);
        Log.d("Word2", word2);
        Log.d("Scram", scramWord);

        int scramWordSize = scramWord.length();

        LetterTile[] letterTArray = new LetterTile[scramWordSize];

        for (int i = 0; i < scramWordSize; i++) {
            letterTArray[i] = new LetterTile(this, scramWord.charAt(i));
        }

        // Pushes each character from the scrambled word onto the stack layout that is
        // visible to the user.
        for (int j = scramWordSize - 1; j >= 0; j--) {
            stackedLayout.push(letterTArray[j]);
        }

        return true;
    }


    public boolean onUndo(View view) {
        LetterTile tile = placedTiles.pop();
        tile.moveToViewGroup(stackedLayout);
        /**
         **
         **
         **
         **/
        return true;
    }
}
