package pl.ekhart.whack_a_mole;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.nio.channels.Pipe;
import java.util.Random;

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

    private int activeMole = 0,
        moleRate = 5;
    private boolean moleRising = true,
        moleSinking = false,
        moleJustHit = false;

    private Bitmap whack;
    private boolean whacking = false;
    private int molesWhacked = 0,
        molesMissed;

    private Point finger;

    private Paint blackPaint;

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
                        animateMoles();
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
                drawText(canvas);
                drawMoles(canvas);
                drawMask(canvas);
                drawWhack(canvas);
            } catch (Exception e) {

            }
        }

        private void drawText(Canvas canvas) {
            canvas.drawText("Whacked: " + molesWhacked, 10,
                blackPaint.getTextSize() + 10, blackPaint);
            canvas.drawText("Missed: " + molesMissed,
                screenWidth - (int) (200 * drawScale.width),
                blackPaint.getTextSize() + 10, blackPaint);
        }

        private void drawWhack(Canvas canvas) {
            if (whacking) {
                canvas.drawBitmap(whack,
                    finger.x - (whack.getWidth() / 2),
                    finger.y - (whack.getHeight() / 2),
                    null);
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
                        finger.x = x;
                        finger.y = y;
                        if (!onTitle && detectMoleContact()) {
                            whacking = true;
                            molesWhacked++;
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        break;

                    case MotionEvent.ACTION_UP:
                        if (onTitle) {
                            background = getScaledBitmap(getWhackAMoleBackground());
                            scale = getScale();
                            mask = getScaled(getResource(R.drawable.mask));
                            mole = getScaled(getResource(R.drawable.mole));
                            whack = getScaled(getResource(R.drawable.whack));
                            onTitle = false;
                            pickActiveMole();
                        }
                        whacking = false;
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
                blackPaint = getPaint();
            }
        }

        public void setRunning(boolean value) {
            running = value;
        }

        private void animateMoles() {
            for (int i = 0; i < MOLE_LENGTH; ++i)
                setMoleIfActive(i, getMoleY(i), getIfEven(i, 300, 250));
        }
    }

    private Paint getPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(drawScale.width * 30);
        return paint;
    }

    private int getIfEven(int i, int a, int b) {
        return isEven(i) ? a : b;
    }

    private void setMoleIfActive(int i, int activeHeight, int sinkingHeigth) {
        if (activeMole == i + 1) {
            int y = moles[i].y;
            if (moleRising) {
                moles[i].y -= moleRate;
            } else if (moleSinking) {
                moles[i].y += moleRate;
            }

            if (y >= getDrawScaleHeigth(activeHeight) || moleJustHit) {
                moles[i].y = getDrawScaleHeigth(activeHeight);
                pickActiveMole();
            }

            if (y <= getDrawScaleHeigth(sinkingHeigth)) {
                moles[i].y = getDrawScaleHeigth(sinkingHeigth);
                moleRising = false;
                moleSinking = true;
            }
        }
    }

    private int getDrawScaleHeigth(int i) {
        return (int) (i * drawScale.height);
    }

    private Point[] initMoles() {
        int x = 55;
        for (int i = 0; i < moles.length; i++) {
            Point point = moles[i];

            point.x = (int) (x * drawScale.width);
            x += 100;

            point.y = (int) (getMoleY(i) * drawScale.height);
        }
        return new Point[0];
    }

    private int getMoleY(int i) {
        return isEven(i) ? 475 : 425;
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

    private void pickActiveMole() {
        if (!moleJustHit && activeMole > 0) {
            molesMissed++;
        }
        activeMole = new Random().nextInt(MOLE_LENGTH) + 1;
        moleRising = true;
        moleSinking = false;
        moleRate = 5 + molesWhacked / 10;
    }

    private boolean detectMoleContact() {
        boolean contact = false;
        for (int i = 0; i < MOLE_LENGTH; ++i) {
            moleJustHit = contact = activeMoleClicked(i);
        }
        return contact;
    }

    private boolean activeMoleClicked(int i) {
        Point mole = moles[i];
        int x = mole.x + (int) (88 * drawScale.width),
            y = (int) (getIfEven(i, 450, 400) * drawScale.height);
        return activeMole == i + 1
            && finger.x >= mole.x
            && finger.x < x
            && finger.y > mole.y
            && finger.y < y;
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
