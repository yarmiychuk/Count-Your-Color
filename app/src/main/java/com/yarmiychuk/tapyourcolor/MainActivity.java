package com.yarmiychuk.tapyourcolor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.jetbrains.annotations.Contract;

import java.util.Random;

/**
 * Created by Dmitriy Yarmiychuk on 28.12.17.
 * Создал Dmitriy Yarmiychuk 28.12.17.
 */

public class MainActivity extends AppCompatActivity {

    // Constants for colors
    static final int COLOR_WHITE = 0, COLOR_RED = 1, COLOR_YELLOW = 2,
            COLOR_GREEN = 3, COLOR_BLUE = 4;
    // Constants and variable for App UI mode
    final int MODE_MENU = 0, MODE_GAME = 1, MODE_SETTINGS = 2;
    // Constants for app's SharedPreferences
    final String SP_IS_PLAYING_NOW = "isPlayingNow",
            SP_SCORE_PLAYER_A = "scorePlayerA", SP_SCORE_PLAYER_B = "scorePlayerB",
            SP_COLOR_PLAYER_A = "colorPlayerA", SP_COLOR_PLAYER_B = "colorPlayerB",
            SP_COLOR_DEFAULT_A = "colorDefaultA", SP_COLOR_DEFAULT_B = "colorDefaultB",
            SP_NAME_PLAYER_A = "namePlayerA", SP_NAME_PLAYER_B = "namePlayerB";
    private int mode;
    // Player's names
    private String namePlayerA, namePlayerB;
    // Player's score
    private int scorePlayerA, scorePlayerB;
    // Player's color
    private int colorPlayerA, colorPlayerB;
    private int colorDefaultA, colorDefaultB;
    // Variable to save current game mode
    private boolean isPlayingNow = false;
    // Array for GridView
    private int[] cellsBackground = new int[16];
    // GridAdapter
    private GridAdapter adapter;
    // GameViews
    private LinearLayout llMenu, llGameName, llWin;
    private ScrollView svSettings;
    private GridView gvGame;
    private EditText etNamePlayerA, etNamePlayerB;
    private Button btnStartGame, btnSettingsOrRestart;
    private TextView tvNamePlayerA, tvNamePlayerB,
            tvColorPlayerA, tvColorPlayerB,
            tvScoreA, tvScoreB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Define views
        llMenu = findViewById(R.id.ll_game_menu);
        llGameName = findViewById(R.id.ll_game_name);
        llWin = findViewById(R.id.ll_game_win);
        svSettings = findViewById(R.id.sv_settings);
        gvGame = findViewById(R.id.game_grid_view);
        etNamePlayerA = findViewById(R.id.et_name_player_a);
        etNamePlayerB = findViewById(R.id.et_name_player_b);
        btnStartGame = findViewById(R.id.btn_start_game);
        btnSettingsOrRestart = findViewById(R.id.btn_settings_reset);
        tvNamePlayerA = findViewById(R.id.tv_player_name_a);
        tvNamePlayerB = findViewById(R.id.tv_player_name_b);
        tvColorPlayerA = findViewById(R.id.tv_color_player_a);
        tvColorPlayerB = findViewById(R.id.tv_color_player_b);
        tvScoreA = findViewById(R.id.tv_score_a);
        tvScoreB = findViewById(R.id.tv_score_b);
        // Prepare GridView
        initializeGridView();
        // Set default game mode
        mode = MODE_MENU;
        // Hide keyboard if need
        hideSoftKeyboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Read saved state if it is
        readGameState();
        // Prepare UI to resume
        invalidateUI();
    }

    @Override
    protected void onPause() {
        if (mode == MODE_GAME) {
            mode = MODE_MENU;
        }
        if (mode == MODE_SETTINGS) {
            saveSettings();
        } else {
            saveGameState();
        }
        super.onPause();
    }

    /**
     * Override Back system button
     */
    @Override
    public void onBackPressed() {

        // Hide keyboard, if it open
        hideSoftKeyboard();

        if (isPlayingNow) {
            if (mode == MODE_GAME) {
                // Save game state and Show menu
                saveGameState();
                goToMenu();
            } else if (mode == MODE_MENU) {
                // Exit game
                super.onBackPressed();
            }
        } else {
            if (mode == MODE_SETTINGS) {
                // Go back to Menu
                goToMenu();
            } else if (mode == MODE_GAME) {
                // State after som player wins
                resetGame();
            } else {
                // Exit game
                super.onBackPressed();
            }
        }
    }

    /**
     * Hide keyboard
     */
    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null && inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Return to Game menu
     */
    private void goToMenu() {
        mode = MODE_MENU;
        invalidateUI();
    }

    /**
     * Save current game state
     */
    private void saveGameState() {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putBoolean(SP_IS_PLAYING_NOW, isPlayingNow);
        editor.putInt(SP_SCORE_PLAYER_A, scorePlayerA);
        editor.putInt(SP_SCORE_PLAYER_B, scorePlayerB);
        editor.putInt(SP_COLOR_PLAYER_A, colorPlayerA);
        editor.putInt(SP_COLOR_PLAYER_B, colorPlayerB);
        editor.putInt(SP_COLOR_DEFAULT_A, colorDefaultA);
        editor.putInt(SP_COLOR_DEFAULT_B, colorDefaultB);
        editor.putString(SP_NAME_PLAYER_A, namePlayerA);
        editor.putString(SP_NAME_PLAYER_B, namePlayerB);
        editor.apply();
    }

    /**
     * Read saved game state
     */
    private void readGameState() {
        SharedPreferences mSet = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isPlayingNow = mSet.getBoolean(SP_IS_PLAYING_NOW, false);
        scorePlayerA = mSet.getInt(SP_SCORE_PLAYER_A, 0);
        scorePlayerB = mSet.getInt(SP_SCORE_PLAYER_B, 0);
        colorPlayerA = mSet.getInt(SP_COLOR_PLAYER_A, 0);
        colorPlayerB = mSet.getInt(SP_COLOR_PLAYER_B, 0);
        colorDefaultA = mSet.getInt(SP_COLOR_DEFAULT_A, 0);
        colorDefaultB = mSet.getInt(SP_COLOR_DEFAULT_B, 0);
        namePlayerA = mSet.getString(SP_NAME_PLAYER_A, "");
        namePlayerB = mSet.getString(SP_NAME_PLAYER_B, "");
    }

    /**
     * Save game settings
     */
    private void saveSettings() {
        namePlayerA = convertTextFromEditText(etNamePlayerA);
        namePlayerB = convertTextFromEditText(etNamePlayerB);
        saveGameState();
    }

    /**
     * Method initialize GridView
     */
    private void initializeGridView() {
        // Hide keyboard, if it open
        hideSoftKeyboard();

        // Clear all cells
        for (int i = 0; i < 16; i++) {
            cellsBackground[i] = COLOR_WHITE;
        }

        // Get size of colored game cell
        LinearLayout llGame = findViewById(R.id.game_linear_layout);
        int cellHeight = llGame.getHeight() / 4;
        int cellWidth = llGame.getWidth() / 4;
        int cellSize = cellWidth;
        if (cellWidth >= cellHeight && cellHeight != 0) {
            cellSize = cellHeight;
        }
        // Reset adapter for GridView
        adapter = new GridAdapter(getApplicationContext(), cellsBackground, cellSize);
        gvGame.setAdapter(adapter);
    }

    /**
     * Invalidate UI for different app modes
     */
    private void invalidateUI() {
        // Hide keyboard, if it open
        hideSoftKeyboard();
        switch (mode) {
            case MODE_MENU:
                llMenu.setVisibility(View.VISIBLE);
                svSettings.setVisibility(View.INVISIBLE);
                llWin.setVisibility(View.INVISIBLE);
                invalidatePlayersColorAndName();
                if (!isPlayingNow) {
                    // Game not started
                    btnStartGame.setText(getString(R.string.start_new_game));
                    btnSettingsOrRestart.setText(getString(R.string.settings));
                } else {
                    // Game just paused
                    btnStartGame.setText(getString(R.string.continue_game));
                    btnSettingsOrRestart.setText(getString(R.string.reset_game));
                }
                showAppName();
                break;
            case MODE_GAME:
                llMenu.setVisibility(View.INVISIBLE);
                llWin.setVisibility(View.INVISIBLE);
                svSettings.setVisibility(View.INVISIBLE);
                break;
            case MODE_SETTINGS:
                llMenu.setVisibility(View.INVISIBLE);
                llWin.setVisibility(View.INVISIBLE);
                svSettings.setVisibility(View.VISIBLE);
                // Fill name for player A
                fillEditText(etNamePlayerA, namePlayerA);
                // Fill name for player B
                fillEditText(etNamePlayerB, namePlayerB);
                // Set background for color TextView A
                setBackgroundOfView(tvColorPlayerA, colorDefaultA);
                // Set background for color TextView B
                setBackgroundOfView(tvColorPlayerB, colorDefaultB);
        }
    }

    /**
     * Fill EditText saved player's name or left it blank if name is default
     *
     * @param editText to fill
     * @param name     of player
     */
    private void fillEditText(EditText editText, String name) {
        if (isNameLikeDefault(name)) {
            editText.setText("");
        } else {
            editText.setText(name);
        }
    }

    /**
     * Set background Of View
     *
     * @param view  - selected view
     * @param color or background
     */
    private void setBackgroundOfView(View view, int color) {
        switch (color) {
            case COLOR_RED:
                view.setBackgroundResource(R.drawable.background_red);
                break;
            case COLOR_YELLOW:
                view.setBackgroundResource(R.drawable.background_yellow);
                break;
            case COLOR_GREEN:
                view.setBackgroundResource(R.drawable.backgournd_green);
                break;
            case COLOR_BLUE:
                view.setBackgroundResource(R.drawable.background_blue);
                break;
            default:
                view.setBackgroundResource(R.mipmap.ic_launcher);
        }
    }

    /**
     * Invalidate Players Names and colors and show it in menu
     */
    private void invalidatePlayersColorAndName() {
        initializePlayersColors();
        initializePlayersNames();
    }

    /**
     * Shows name of app in the menu
     */
    private void showAppName() {
        llGameName.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        String appName = getString(R.string.app_name);
        int position = 0;
        while (position < appName.length()) {
            TextView textView = (TextView) inflater.inflate(
                    R.layout.menu_caption_text_view, llGameName, false);
            llGameName.addView(textView);
            textView.setText(appName.substring(position, position + 1));
            textView.setTextColor(getResourceColor(getRandomNumber(4)));
            position++;
        }
    }

    /**
     * Method start new game
     */
    private void startNewGame() {
        resetScore();
        prepareGameSpace();
        isPlayingNow = true;
        mode = MODE_GAME;
        invalidateUI();
    }

    /**
     * Prepare game screen for play
     */
    private void prepareGameSpace() {
        changeButtonsColor();
        initializeGridView();
        updateScoreTextView(tvScoreA, scorePlayerA);
        updateScoreTextView(tvScoreB, scorePlayerB);
        replaceCellsColor(colorPlayerA);
        replaceCellsColor(colorPlayerB);
    }

    /**
     * Method reset all game scores for all players
     */
    private void resetScore() {
        scorePlayerA = 0;
        scorePlayerB = 0;
        updateScoreTextView(tvScoreA, scorePlayerA);
        updateScoreTextView(tvScoreB, scorePlayerB);
    }

    /**
     * Update score TextViews
     *
     * @param textView - to set new score value
     * @param score    - new score
     */
    private void updateScoreTextView(TextView textView, int score) {
        textView.setText(String.valueOf(score));
    }

    /**
     * Initialize color of players
     */
    private void initializePlayersColors() {
        // Restore player's color if need
        if (!isPlayingNow) {
            colorPlayerA = colorDefaultA;
            colorPlayerB = colorDefaultB;
        }
        // Check for undefined colors and change it if need
        if (colorPlayerA == 0 && colorPlayerB == 0) {
            colorPlayerA = getRandomColor(0);
            colorPlayerB = getRandomColor(colorPlayerA);
        } else if (colorPlayerA == 0) {
            colorPlayerA = getRandomColor(colorPlayerB);
        } else if (colorPlayerB == 0) {
            colorPlayerB = getRandomColor(colorPlayerA);
        }
    }

    /**
     * Define random game color for player
     *
     * @param excludeColor color to exclude
     * @return color from 1 to 4 without excluded
     */
    private int getRandomColor(int excludeColor) {
        int color = 0;
        while (color == 0 || color == excludeColor) {
            color = getRandomNumber(4);
        }
        return color;
    }

    /**
     * Method convert constant to resource color
     *
     * @param color - app constant
     * @return color from resources
     */
    private int getResourceColor(int color) {
        switch (color) {
            case COLOR_RED:
                return ContextCompat.getColor(getBaseContext(), R.color.colorRed);
            case COLOR_YELLOW:
                return ContextCompat.getColor(getBaseContext(), R.color.colorYellow);
            case COLOR_GREEN:
                return ContextCompat.getColor(getBaseContext(), R.color.colorGreen);
            case COLOR_BLUE:
                return ContextCompat.getColor(getBaseContext(), R.color.colorBlue);
        }
        return ContextCompat.getColor(getBaseContext(), R.color.colorAccent);
    }

    /**
     * Initialize players names from settings or defaults
     */
    private void initializePlayersNames() {
        if (namePlayerB.equals("") || isNameLikeDefault(namePlayerB)) {
            namePlayerB = getDefaultPlayerName(colorPlayerB);
        }
        // Update TextView for player A
        tvNamePlayerA.setText(namePlayerA);
        tvNamePlayerA.setTextColor(getResourceColor(colorPlayerA));
        // Update TextView for player B
        tvNamePlayerB.setText(namePlayerB);
        tvNamePlayerB.setTextColor(getResourceColor(colorPlayerB));
    }

    /**
     * Method set the default player names
     *
     * @param color - Color of player
     * @return default name of player
     */
    private String getDefaultPlayerName(int color) {
        String name = "";
        switch (color) {
            case COLOR_RED:
                name = getString(R.string.red);
                break;
            case COLOR_YELLOW:
                name = getString(R.string.yellow);
                break;
            case COLOR_GREEN:
                name = getString(R.string.green);
                break;
            case COLOR_BLUE:
                name = getString(R.string.blue);
                break;
        }
        return name;
    }

    /**
     * @param name - Name of player
     * @return Looks name of player like default or not
     */
    private boolean isNameLikeDefault(String name) {
        return name.equals(getString(R.string.red)) ||
                name.equals(getString(R.string.yellow)) ||
                name.equals(getString(R.string.green)) ||
                name.equals(getString(R.string.blue));
    }

    /**
     * Method change background color of game buttons for each player
     */
    private void changeButtonsColor() {
        // Change button's color for player A
        setButtonColor(findViewById(R.id.button_a_1), colorPlayerA);
        setButtonColor(findViewById(R.id.button_a_2), colorPlayerA);
        setButtonColor(findViewById(R.id.button_a_3), colorPlayerA);

        // Change button's color for player A
        setButtonColor(findViewById(R.id.button_b_1), colorPlayerB);
        setButtonColor(findViewById(R.id.button_b_2), colorPlayerB);
        setButtonColor(findViewById(R.id.button_b_3), colorPlayerB);
    }

    /**
     * Set button colors according to the motion event
     *
     * @param view  - button to set color
     * @param color - color constant of not-pressed button
     */
    private void setButtonColor(final View view, int color) {
        final int usualColor = getResourceColor(color);
        final int pressedColor = ContextCompat.getColor(getBaseContext(), R.color.colorButtonPressed);
        view.setBackgroundColor(usualColor);
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    v.setBackgroundColor(pressedColor);
                    return false;
                }
                v.setBackgroundColor(usualColor);
                return false;
            }
        });
    }

    /**
     * Get new score for player A after turn
     *
     * @param view what was clicked
     */
    public void onClickButtonA(View view) {
        if (isPlayingNow) {
            int countCellsA = getColorCount(colorPlayerA);
            switch (view.getId()) {
                case R.id.button_a_1:
                    scorePlayerA += getScoreForTurn(1, countCellsA == 1);
                    break;
                case R.id.button_a_2:
                    scorePlayerA += getScoreForTurn(2, countCellsA == 2);
                    break;
                case R.id.button_a_3:
                    scorePlayerA += getScoreForTurn(3, countCellsA == 3);
            }
            if (scorePlayerA < 0) {
                scorePlayerA = 0;
            }
            updateScoreTextView(tvScoreA, scorePlayerA);
            replaceCellsColor(colorPlayerA);
            checkForEndOfGame();
        }
    }

    /**
     * Get new score for player B after turn
     *
     * @param view what was clicked
     */
    public void onClickButtonB(View view) {
        if (isPlayingNow) {
            int countCellsB = getColorCount(colorPlayerB);
            switch (view.getId()) {
                case R.id.button_b_1:
                    scorePlayerB += getScoreForTurn(1, countCellsB == 1);
                    break;
                case R.id.button_b_2:
                    scorePlayerB += getScoreForTurn(2, countCellsB == 2);
                    break;
                case R.id.button_b_3:
                    scorePlayerB += getScoreForTurn(3, countCellsB == 3);
            }
            if (scorePlayerB < 0) {
                scorePlayerB = 0;
            }
            updateScoreTextView(tvScoreB, scorePlayerB);
            replaceCellsColor(colorPlayerB);
            checkForEndOfGame();
        }
    }

    /**
     * Get count of cells for special Color
     *
     * @param color - required color
     * @return count of cells required color
     */
    @Contract(pure = true)
    private int getColorCount(int color) {
        int count = 0;
        for (int i = 0; i < 16; i++) {
            if (cellsBackground[i] == color) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get score for this turn
     *
     * @param scoreValue    - points for this turn
     * @param isRightChoice - Player choice was right or not
     * @return score for this turn
     */
    @Contract(pure = true)
    private int getScoreForTurn(int scoreValue, boolean isRightChoice) {
        if (isRightChoice) {
            return scoreValue;
        }
        return -5;
    }

    /**
     * Replace special color in cells
     *
     * @param colorToReplace - color to replace
     */
    private void replaceCellsColor(int colorToReplace) {
        clearCellsColor(colorToReplace);
        int nextColorCount = getRandomNumber(3);
        while (nextColorCount > 0) {
            int randomCell = getRandomNumber(16) - 1;
            if (cellsBackground[randomCell] == 0) {
                cellsBackground[randomCell] = colorToReplace;
                nextColorCount--;
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Generate random int
     *
     * @param maxValue - max value
     * @return generated random int
     */
    private int getRandomNumber(int maxValue) {
        return new Random().nextInt(maxValue) + 1;
    }

    /**
     * Crear special color
     *
     * @param colorToClear what color need to clean
     */
    private void clearCellsColor(int colorToClear) {
        for (int i = 0; i < 16; i++) {
            if (cellsBackground[i] == colorToClear) {
                cellsBackground[i] = 0;
            }
        }
    }

    /**
     * Check if any of the players scored the required number of points,
     * if yes, then we display a message about the victory.
     */
    private void checkForEndOfGame() {
        if (scorePlayerA >= 50 || scorePlayerB >= 50) {
            isPlayingNow = false;
            llWin.setVisibility(View.VISIBLE);
            // Define view
            TextView gameResultA = findViewById(R.id.tv_game_result_a);
            TextView gameResultB = findViewById(R.id.tv_game_result_b);
            TextView gameScoreA = findViewById(R.id.tv_game_score_a);
            TextView gameScoreB = findViewById(R.id.tv_game_score_b);
            // Change text color
            gameResultA.setTextColor(getResourceColor(colorPlayerA));
            gameScoreA.setTextColor(getResourceColor(colorPlayerA));
            gameResultB.setTextColor(getResourceColor(colorPlayerB));
            gameScoreB.setTextColor(getResourceColor(colorPlayerB));
            // Set score message
            String scoreA = getString(R.string.your_score) + " " + scorePlayerA;
            String scoreB = getString(R.string.your_score) + " " + scorePlayerB;
            gameScoreA.setText(scoreA);
            gameScoreB.setText(scoreB);
            // Set win and lose messages
            if (scorePlayerA >= 50) {
                gameResultA.setText(getString(R.string.you_win));
                gameResultB.setText(getString(R.string.you_lose));
            } else {
                gameResultA.setText(getString(R.string.you_lose));
                gameResultB.setText(getString(R.string.you_win));
            }
        }
    }

    /**
     * Start new game or resume saved game
     *
     * @param view button
     */
    public void onClickStartResume(View view) {
        if (isPlayingNow) {
            // Resume game
            prepareGameSpace();
            mode = MODE_GAME;
            invalidateUI();
        } else {
            // Start new game
            startNewGame();
        }
    }

    /**
     * Open settings of app or reset game
     *
     * @param view button
     */
    public void onClickSettingsOrReset(View view) {
        if (!isPlayingNow) {
            // Open settings
            mode = MODE_SETTINGS;
            invalidateUI();
        } else {
            // Reset game
            resetGame();
        }
    }

    /**
     * Reset game
     */
    private void resetGame() {
        mode = MODE_MENU;
        resetScore();
        isPlayingNow = false;
        invalidatePlayersColorAndName();
        invalidateUI();
    }

    /**
     * Show dialog with "How to play" info
     *
     * @param view button
     */
    public void onClickAbout(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(getString(R.string.app_name));
        String message = getString(R.string.how_to_play_message1) + "\n\n" +
                getString(R.string.how_to_play_message2) + "\n\n" +
                getString(R.string.how_to_play_message3) + "\n" +
                getString(R.string.how_to_play_message4);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.string_ok), null);
        builder.create().show();
    }

    /**
     * Quit app
     *
     * @param view button
     */
    public void onClickExit(View view) {
        finish();
    }

    /**
     * Remove unused space from text in EditText
     *
     * @param editText to get text
     * @return text without space at begin and end of text
     */
    private String convertTextFromEditText(EditText editText) {
        String text = editText.getText().toString();
        while (text.length() > 0 && text.substring(0, 1).equals(" ")) {
            if (text.length() == 1) {
                text = "";
            } else {
                text = text.substring(1, text.length());
            }
        }
        while (text.length() > 0 && text.substring(text.length() - 1, text.length()).equals(" ")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    /**
     * Select color for Player A or B
     *
     * @param view - selected view
     */
    public void onClickSelectColor(final View view) {
        // Create dialog to pick a color
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(R.string.pick_color);
        String[] colors = {getString(R.string.random), getString(R.string.red),
                getString(R.string.yellow), getString(R.string.green), getString(R.string.blue)};
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichColor) {
                if (view.getId() == R.id.tv_color_player_a) {
                    colorDefaultA = whichColor;
                } else {
                    colorDefaultB = whichColor;
                }
                saveSettings();
                invalidateUI();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.create();
        builder.show();
    }

    /**
     * OnClick method for Finish button when some player Win
     */
    public void onClickFinish(View view) {
        resetGame();
    }

    /**
     * Reset settings to default and return to menu
     *
     * @param view - reset button
     */
    public void onClickResetSettings(View view) {
        colorDefaultA = 0;
        colorDefaultB = 0;
        namePlayerA = "";
        namePlayerB = "";
        etNamePlayerA.setText(namePlayerA);
        etNamePlayerB.setText(namePlayerB);
        saveSettings();
        invalidateUI();
    }

    /**
     * Save settings and go to menu
     *
     * @param view - "Done" button
     */
    public void onclickDoneSettings(View view) {
        saveSettings();
        goToMenu();
    }
}
