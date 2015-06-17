package pl.ekhart.whack_a_mole;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


public class WhackAMoleActivity extends Activity {

    private static final int TOGGLE_SOUND = 1;
    public static final String SOUND_SETTING = "soundSetting";
    private boolean soundEnabled = true;
    private WhackAMoleView view;

    public static final String PREFERANCES_NAME = "MyPreferances";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullscreen();
        setContentView(R.layout.whackamole_layout);
        view = (WhackAMoleView) findViewById(R.id.mole);
        view.setKeepScreenOn(true);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        view.soundOn = soundEnabled = getSoundSetting();
    }

    private void setFullscreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int fullscreen = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(fullscreen, fullscreen);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem toggleSound = menu.add(0, TOGGLE_SOUND, 0, "Toggle Sound");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case TOGGLE_SOUND:
                view.soundOn = soundEnabled = !soundEnabled;
                setSoundSetting(soundEnabled);
                String text = "Sound " + (soundEnabled ? "On" : "Off");
                Toast.makeText(this, text, Toast.LENGTH_SHORT)
                    .show();
                break;
        }
        return false;
    }

    private void setSoundSetting(boolean soundEnabled) {
        SharedPreferences preferences = getSharedPreferences(PREFERANCES_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SOUND_SETTING, soundEnabled);
        editor.commit();
    }

    public boolean getSoundSetting() {
        SharedPreferences preferences = getSharedPreferences(PREFERANCES_NAME, 0);
        return preferences.getBoolean(SOUND_SETTING, true);
    }
}
