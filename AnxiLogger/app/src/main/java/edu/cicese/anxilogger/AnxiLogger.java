package edu.cicese.anxilogger;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cicese.anxilogger.api.AnxiApi;

/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p>
 * The main content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
 */
public class AnxiLogger extends Activity {

    /** {@link CardScrollView} to use as the main content view. */
    private CardScrollView mCardScroller;

    /** "Hello World!" {@link View} generated by {@link #buildView()}. */
    private View mView;

    private Context mContext;
    private boolean isRunning;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        isRunning =false;
        mContext  = getApplicationContext();
                mView = buildView();

        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardScrollAdapter() {
            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public Object getItem(int position) {
                return mView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return mView;
            }

            @Override
            public int getPosition(Object item) {
                if (mView.equals(item)) {
                    return 0;
                }
                return AdapterView.INVALID_POSITION;
            }
        });
        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Plays disallowed sound to indicate that TAP actions are not supported.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.DISALLOWED);
            }
        });
        setContentView(mCardScroller);
        getIRData();
    }
    Handler eyeHandler = new Handler(){
        public void handleMessage(Message msg) {

            Card newcard = new Card(mContext);

            newcard.setText(msg.obj.toString());
            setContentView(newcard.getView());

        }
    };
    private void getIRData(){
        Intent bindIndent = new Intent(AnxiLogger.this,
                LoggerService.class);

        mContext.startService(bindIndent);
        isRunning = true;
        Thread t = new Thread(new Runnable() {
            IRSensorLogger irlogger = new IRSensorLogger();


            @Override
            public void run() {
                String ir_value;
                AnxiApi api = new AnxiApi();
                    while(isRunning) {
                        ir_value = String.valueOf(irlogger.getIRSensorData());
                        Message msg = new Message();
                        msg.obj = ir_value;
                        eyeHandler.sendMessage(msg);

                        api.postDataToServer(ir_value);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
            }
        } );
        t.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.stop_menu, menu);
        return true;


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection. Menu items typically start another
        // activity, start a service, or broadcast another intent.
        switch (item.getItemId()) {
            case R.id.reply_menu_item:
                Intent bindIndent = new Intent(AnxiLogger.this,
                LoggerService.class);
            mContext.stopService(bindIndent);
            isRunning = false;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }



    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
        Intent bindIndent = new Intent(AnxiLogger.this,
                LoggerService.class);

        mContext.startService(bindIndent);
        getIRData();
    }

    @Override
    protected void onPause() {
        //mCardScroller.deactivate();
        super.onPause();
        //Intent bindIndent = new Intent(AnxiLogger.this,
        //        LoggerService.class);
        //mContext.stopService(bindIndent);
    }
    Card card;
    /**
     * Builds a Glass styled "Hello World!" view using the {@link Card} class.
     */
    private View buildView() {
        card = new Card(this);

        card.setText(R.string.hello_world);
        return card.getView();
    }



}
