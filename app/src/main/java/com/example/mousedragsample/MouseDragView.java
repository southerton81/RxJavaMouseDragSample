package com.example.mousedragsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import rx.Observable;
import rx.subjects.PublishSubject;

public class MouseDragView extends View {
    private final PublishSubject<MotionEvent> mTouchSubject = PublishSubject.create();
    private final Observable<MotionEvent> mTouches = mTouchSubject.asObservable();
    private final Observable<MotionEvent> mDownObservable = mTouches.filter(ev -> ev.getActionMasked() == MotionEvent.ACTION_DOWN);
    private final Observable<MotionEvent> mUpObservable = mTouches.filter(ev -> ev.getActionMasked() == MotionEvent.ACTION_UP);
    private final Observable<MotionEvent> mMovesObservable = mTouches.filter(ev -> ev.getActionMasked() == MotionEvent.ACTION_MOVE);

    private Bitmap CustomBitmap;
    private Canvas CustomCanvas;
    private Paint CustomPaint;


    public MouseDragView(Context context, AttributeSet attr) {
        super(context, attr);

         setOnTouchListener((View v, MotionEvent event) -> {
            mTouchSubject.onNext(event);
            return true;
         });


        mDownObservable.subscribe(downEvent -> {
            final Path path = new Path();
            path.moveTo(downEvent.getX(), downEvent.getY());
            Log.i(downEvent.toString(), "Touch down");

             mMovesObservable
                     .takeUntil(mUpObservable
                             .doOnNext(upEvent -> {
                                 draw(path);
                                 path.close();
                                 Log.i(upEvent.toString(), "Touch up");
                             }))
                    .subscribe(motionEvent -> {
                        path.lineTo(motionEvent.getX(), motionEvent.getY());
                        draw(path);
                        Log.i(motionEvent.toString(), "Touch move");
                    });

        });

        /* Another possibility to do the same
        Observable<MotionEvent> MotionEventsObservable = mDownObservable.flatMap(downEvent -> {
            Log.i(downEvent.toString(), "Mouse down");

            final Path path = new Path();
            path.moveTo(downEvent.getX(), downEvent.getY());

            return mMovesObservable
                    .takeUntil( mUpObservable
                            .doOnNext( upEvent -> {
                                draw(path);
                                path.close();
                                Log.i(upEvent.toString(), "Mouse up");
                            }) )
                    .doOnNext( motionEvent -> {
                        path.lineTo(motionEvent.getX(), motionEvent.getY());
                        Log.i(motionEvent.toString(), "Mouse move");
                    });
        });

        MotionEventsObservable.subscribe();*/

    }


    private void draw(Path path) {
        if (CustomBitmap == null) {
            CustomBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            CustomCanvas = new Canvas(CustomBitmap);
            CustomPaint = new Paint();
            CustomPaint.setColor(Color.GREEN);
            CustomPaint.setStyle(Paint.Style.STROKE);
        }

        CustomCanvas.drawPath(path, CustomPaint);
        invalidate();
    }


    public void onDraw(Canvas canvas) {
        if (CustomBitmap != null)
        canvas.drawBitmap(CustomBitmap, 0, 0, CustomPaint);
    }
}
