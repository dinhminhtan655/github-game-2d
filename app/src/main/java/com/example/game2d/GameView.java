package com.example.game2d;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying;
    private int screenX, screenY;
    public static float screenRatioX, screenRatioY;
    private Paint paint;
    private List<Bullet> bullets;
    private Flight flight;
    private Background background1, background2;


    public GameView(Context context, int screenX, int screenY) {
        super(context);
        this.screenX = screenX;
        this.screenY = screenY;
        screenRatioX = 2968f / screenX;
        screenRatioY = 1440f / screenY;
        background1 = new Background(screenX, screenY, getResources());
        background2 = new Background(screenX, screenY, getResources());

        flight = new Flight(this, screenY, getResources());

        bullets = new ArrayList<>();

        background2.x = screenX;

        paint = new Paint();


    }

    @Override
    public void run() {

        while (isPlaying) {
            update();
            draw();
            sleep();
        }

    }

    private void sleep() {
        try {
            // Lấy 1000/17 sẽ ra 58.8 gần bằng 60 frame trên 1 giây
            thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void update() {
        background1.x -= 10 * screenRatioX;
        background2.x -= 10 * screenRatioX;

        if (background1.x + background1.background.getWidth() < 0) {
            background1.x = screenX;
        }

        if (background2.x + background2.background.getWidth() < 0) {
            background2.x = screenX;
        }

        // isGoingUp bằng true thì -30 * với trục y màn hình
        if (flight.isGoingUp)
            flight.y -= 30 * screenRatioY;
            // isGoingUp bằng false thì +30 * với trục y màn hình
        else
            flight.y += 30 * screenRatioY;

        // nếu vị trí của flight đang ở vị trí nhỏ hơn 0 thì vị trí của flight gán = 0;
        if (flight.y < 0)
            flight.y = 0;


        // nếu vị trí flight đang ở vị trí chiều dọc mà lớn hơn của hiệu chiều dọc màn hình - chiều cao của bitmap flight thì flight.y sẽ = giá trị của screenY - chiều cao của bitmap flight
        if (flight.y > screenY - flight.height)
            flight.y = screenY - flight.height;

        List<Bullet> trash = new ArrayList<>();

        for (Bullet bullet : bullets) {

            if (bullet.x > screenRatioX) {
                trash.add(bullet);
            }

            bullet.x += 50 * screenRatioX;
        }
        
        for (int i = 0; i < trash.size(); i++)
            trash.remove(trash.get(i));

    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);

            canvas.drawBitmap(flight.getFlight(), flight.x, flight.y, paint);

            for (Bullet bullet : bullets)
                canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y, paint);

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();

    }

    public void pause() {
        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (event.getX() < screenX / 2) {
                    flight.isGoingUp = true;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                flight.isGoingUp = false;
                if (event.getX() > screenX / 2) {
                    flight.toShoot++;
                }
                break;
        }
        return true;
    }

    public void newBullet() {
        Bullet bullet = new Bullet(getResources());

        // Lấy vị trí điểm x hiện tại của flight lúc phát ra viên đạn + cho chiều ngang của flight để gán cho viên đạn
        bullet.x = flight.x + flight.width;
        // Lấy vị trí điểm y hiện tại của flight lúc phát ra viên đạn + cho chiều cao của flight để gán cho viên đạn
        bullet.y = flight.y + (flight.height / 2);
        bullets.add(bullet);

    }
}
