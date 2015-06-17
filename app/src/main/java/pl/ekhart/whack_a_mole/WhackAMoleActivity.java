package pl.ekhart.whack_a_mole;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.sql.SQLException;


public class WhackAMoleActivity extends Activity {

    private static final int TOGGLE_SOUND = 1;
    public static final String SOUND_SETTING = "soundSetting";
    public static final String SETTINGS_XML = "settings.xml";
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

        //1
        //view.soundOn = soundEnabled = getSoundSetting();
        //2
//        try {
//            readXML();
//        } catch (XmlPullParserException | IOException e) {
//            e.printStackTrace();
//        }
        //3
        DatabaseAdapter db = new DatabaseAdapter(this);
        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Cursor cursor = null;
        try {
            cursor = db.getRecord(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        startManagingCursor(cursor);
        if (cursor.moveToFirst()) {
            do {
                soundEnabled = Boolean.parseBoolean(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        db.close();

        view.soundOn = soundEnabled;
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

                //1
                //setSoundSetting(soundEnabled);
                //2
                //writeXML(soundEnabled);
                //3
                DatabaseAdapter db = new DatabaseAdapter(this);
                try {
                    db.open();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                db.insertOrUpdateRecord(Boolean.toString(soundEnabled));
                db.close();

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

    public void writeXML(boolean soundEnabled) {
        try {
            FileOutputStream out = openFileOutput(SETTINGS_XML, MODE_WORLD_WRITEABLE);
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.write("<sound_setting>" + this.soundEnabled + "<sound_setting>\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readXML()
        throws XmlPullParserException, IOException {

        String tag = "";
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();

        try {
            InputStream in = openFileInput(SETTINGS_XML);
            InputStreamReader inputReader = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(inputReader);
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            in.close();

            parser.setInput(new StringReader(buffer.toString()));
            for (int eventType = parser.getEventType();
                eventType != XmlPullParser.END_DOCUMENT;
                eventType = parser.next()) {

                if (eventType == XmlPullParser.START_DOCUMENT) {

                } else if (eventType == XmlPullParser.START_TAG) {
                    tag = parser.getName();
                } else if (eventType == XmlPullParser.END_TAG) {

                } else if (eventType == XmlPullParser.TEXT) {
                    if (tag.contains("sound_setting")) {
                        soundEnabled = Boolean.parseBoolean(parser.getText());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("File not found");
        }
    }
}
