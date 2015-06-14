package pl.ekhart.whack_a_mole;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Pair;
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

    private WidthHeigth<Integer> backgroundOrig;
    private WidthHeigth<Float> scale, drawScale;
    private Bitmap mask, mole;

    private final int MOLE_LENGTH = 7;
    private Point[] moles = new Point[MOLE_LENGTH];

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
                drawMoles(canvas);
                drawMask(canvas);
            } catch (Exception e) {

            }
        }

        private void drawMask(Canvas canvas) {
            int x = 50;
            for (int i = 0; i <= MOLE_LENGTH; ++i) {
                int y = isEven(i) ? 450 : 400;
                canvas.drawBitmap(mask, x * drawScale.width, y * drawScale.height, null);
                x += 100;
            }
        }

        private void drawMoles(Canvas canvas) {
            for (Point point : moles)
                canvas.drawBitmap(mole, point.x, point.y, null);
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
                            scale = getScale();
                            mask = getScaled(getResource(R.drawable.mask));
                            mole = getScaled(getResource(R.drawable.mole));
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
                drawScale = new WidthHeigth<>(
                        (float) screenWidth / 800,
                        (float) screenHeight / 600);
                moles = initMoles();
            }
        }

        public void setRunning(boolean value) {
            running = value;
        }
    }

    private Point[] initMoles() {
        int x = 55;
        for (int i = 0; i < moles.length; i++) {
            Point point = moles[i];

            point.x = (int) (x * drawScale.width);
            x += 100;

            int y = isEven(i) ? 475 : 425;
            point.y = (int) (y * drawScale.height);
        }
        return new Point[0];
    }

    private boolean isEven(int i) {
        return i % 2 == 0;
    }

    private WidthHeigth<Float> getScale() {
        return new WidthHeigth<>(
            (float) screenWidth / backgroundOrig.width,
            (float) screenHeight / backgroundOrig.height
        );
    }

    private Bitmap getScaledBitmap(Bitmap bitmap) {
        return Bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, true);
    }

    private Bitmap getScaled(Bitmap bitmap) {
        int width = (int) (bitmap.getWidth() * scale.width),
            height = (int) (bitmap.getHeight() * scale.height);
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private Bitmap getWhackAMoleBackground() {
        return getResource(R.drawable.background);
    }

    private Bitmap getResource(int id) {
        return BitmapFactory.decodeResource(myContext.getResources(), id);
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
