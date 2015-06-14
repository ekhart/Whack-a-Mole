package pl.ekhart.whack_a_mole;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


public class WhackAMoleActivity extends Activity {

    private static final int TOGGLE_SOUND = 1;
    private boolean soundEnabled = true;
    private WhackAMoleView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullscreen();
        setContentView(R.layout.whackamole_layout);
        view = (WhackAMoleView) findViewById(R.id.mole);
        view.setKeepScreenOn(true);
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
                soundEnabled = !soundEnabled;
                String text = "Sound " + (soundEnabled ? "On" : "Off");
                Toast.makeText(this, text, Toast.LENGTH_SHORT)
                    .show();
                break;
        }
        return false;
    }
}
