package pl.ekhart.whack_a_mole;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Ekh on 2015-06-14.
 */
public class WhackAMoleView
        extends SurfaceView
        implements SurfaceHolder.Callback {

    private Context myContext;
    private SurfaceHolder mySurfaceHolder;
    private Bitmap background;
    private int screenWidth = 1,
        screenHeight = 1;
    private boolean running = false,
        onTitle = true;
    private WhackAMoleThread thread;

    public WhackAMoleView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        thread = new WhackAMoleThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message msg) {

            }
        });

        setFocusable(true);
    }

    public WhackAMoleThread getThread() {
        return thread;
    }

    class WhackAMoleThread extends Thread {

        public WhackAMoleThread(SurfaceHolder holder, Context context, Handler handler) {
            mySurfaceHolder = holder;
            myContext = context;
            background = getWhackAMoleBackground();
        }

        @Override
        public void run() {
            while (running) {
                Canvas canvas = null;
                try {
                    canvas = mySurfaceHolder.lockCanvas(null);
                    synchronized (mySurfaceHolder) {
                        draw(canvas);
                    }
                } finally {
                    if (canvas != null)
                        mySurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

        private void draw(Canvas canvas) {
            try {
                canvas.drawBitmap(background, 0, 0, null);
            } catch (Exception e) {

            }
        }

        boolean doTouchEvent(MotionEvent event) {
            synchronized (mySurfaceHolder) {
                int x = (int) event.getX(),
                    y = (int) event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;

                    case MotionEvent.ACTION_MOVE:
                        break;

                    case MotionEvent.ACTION_UP:
                        if (onTitle) {
                            background = getScaledBitmap(getWhackAMoleBackground());
                            onTitle = false;
                        }
                        break;
                }
            }
            return true;
        }

        public void setSurfaceSize(int width, int heigth) {
            synchronized (mySurfaceHolder) {
                screenWidth = width;
                screenHeight = heigth;
                background = getScaledBitmap(background);
            }
        }

        public void setRunning(boolean value) {
            running = value;
        }
    }

    private Bitmap getScaledBitmap(Bitmap bitmap) {
        return Bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, true);
    }

    private Bitmap getWhackAMoleBackground() {
        return BitmapFactory.decodeResource(myContext.getResources(), R.drawable.background);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return thread.doTouchEvent(event);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setSurfaceSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.setRunning(false);
    }
}
