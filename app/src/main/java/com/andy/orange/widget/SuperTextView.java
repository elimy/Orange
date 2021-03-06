package com.andy.orange.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.andy.orange.R;

/**
 * Created by Andy Lau on 2017/4/23.
 */

public class SuperTextView extends TextView {

    // Some Property Default Value
    private static final float DEFAULT_CORNER = 0f;
    private static final int DEFAULT_SOLID = Color.WHITE;
    private static final float DEFAULT_STROKE_WIDTH = 0f;
    private static final int DEFAULT_STROKE_COLOR = Color.BLACK;
    private static final int DEFAULT_STATE_DRAWABLE_MODE = 4;
    private static final int DEFAULT_TEXT_STROKE_COLOR = Color.BLACK;
    private static final int DEFAULT_TEXT_FILL_COLOR = Color.BLACK;
    private static final float DEFAULT_TEXT_STROKE_WIDTH = 0f;

    private float corner;
    private boolean leftTopCornerEnable;
    private boolean rightTopCornerEnable;
    private boolean leftBottomCornerEnable;
    private boolean rightBottomCornerEnable;
    private int solid;
    private float strokeWidth;
    private int strokeColor;
    private int stateDrawableMode;
    private boolean isShowState;

    private Paint paint;
    private int width;
    private int height;
    private Drawable drawable;
    private float density;
    private boolean autoAdjust;
    private Adjuster adjuster;
    private boolean textStroke;
    private int textStrokeColor;
    private int textFillColor;
    private float textStrokeWidth;
    private boolean runnable = false;
    private boolean needRun = false;
    private Thread animThread;
    private Path strokeWidthPath;
    private Path solidPath;
    private RectF strokeLineRectF;
    private RectF solidRectF;
    private float leftTopCorner[] = new float[2];
    private float rightTopCorner[] = new float[2];
    private float leftBottomCorner[] = new float[2];
    private float rightBottomCorner[] = new float[2];
    private float corners[] = new float[8];
    private float[] drawableBounds = new float[4];
    private float drawableWidth;
    private float drawableHeight;
    private float drawablePaddingLeft;
    private float drawablePaddingTop;
    private boolean cacheRunnableState;
    private boolean cacheNeedRunState;


    public SuperTextView(Context context) {
        super(context);
        init(null);
    }

    public SuperTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SuperTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SuperTextView(Context context, AttributeSet attrs, int defStyleAttr,
                         int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        density = getContext().getResources().getDisplayMetrics().density;
        initAttrs(attrs);
        paint = new Paint();
        initPaint();
    }

    private void initAttrs(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray =
                    getContext().obtainStyledAttributes(attrs, R.styleable.SuperTextView);
            corner = typedArray.getDimension(R.styleable.SuperTextView_corner, DEFAULT_CORNER);
            leftTopCornerEnable =
                    typedArray.getBoolean(R.styleable.SuperTextView_left_top_corner, false);
            rightTopCornerEnable =
                    typedArray.getBoolean(R.styleable.SuperTextView_right_top_corner, false);
            leftBottomCornerEnable =
                    typedArray.getBoolean(R.styleable.SuperTextView_left_bottom_corner, false);
            rightBottomCornerEnable =
                    typedArray.getBoolean(R.styleable.SuperTextView_right_bottom_corner, false);
            solid = typedArray.getColor(R.styleable.SuperTextView_solid, DEFAULT_SOLID);
            strokeWidth = typedArray.getDimension(R.styleable.SuperTextView_stroke_width,
                    DEFAULT_STROKE_WIDTH);
            strokeColor =
                    typedArray.getColor(R.styleable.SuperTextView_stroke_color, DEFAULT_STROKE_COLOR);
            drawable = typedArray.getDrawable(R.styleable.SuperTextView_state_drawable);
            drawableWidth =
                    typedArray.getDimension(R.styleable.SuperTextView_state_drawable_width, 0);
            drawableHeight =
                    typedArray.getDimension(R.styleable.SuperTextView_state_drawable_height, 0);
            drawablePaddingLeft =
                    typedArray.getDimension(R.styleable.SuperTextView_state_drawable_padding_left, 0);
            drawablePaddingTop =
                    typedArray.getDimension(R.styleable.SuperTextView_state_drawable_padding_top, 0);
            isShowState = typedArray.getBoolean(R.styleable.SuperTextView_isShowState, false);
            stateDrawableMode = typedArray.getInteger(R.styleable.SuperTextView_state_drawable_mode,
                    DEFAULT_STATE_DRAWABLE_MODE);
            textStroke = typedArray.getBoolean(R.styleable.SuperTextView_text_stroke, false);
            textStrokeColor = typedArray.getColor(R.styleable.SuperTextView_text_stroke_color,
                    DEFAULT_TEXT_STROKE_COLOR);
            textFillColor = typedArray.getColor(R.styleable.SuperTextView_text_fill_color,
                    DEFAULT_TEXT_FILL_COLOR);
            textStrokeWidth = typedArray.getDimension(R.styleable.SuperTextView_text_stroke_width,
                    DEFAULT_TEXT_STROKE_WIDTH);
            autoAdjust = typedArray.getBoolean(R.styleable.SuperTextView_autoAdjust, false);
            typedArray.recycle();
        }
    }

    private void initPaint() {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setDither(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        width = getWidth();
        height = getHeight();
        drawStrokeLine(canvas);
        drawSolid(canvas);
        isNeedToAdjust(canvas, Adjuster.Opportunity.BEFORE_DRAWABLE);
        drawStateDrawable(canvas);
        isNeedToAdjust(canvas, Adjuster.Opportunity.BEFORE_TEXT);
        if (textStroke) {
            getPaint().setStyle(Paint.Style.STROKE);
            setTextColor(textStrokeColor);
            getPaint().setFakeBoldText(true);
            getPaint().setStrokeWidth(textStrokeWidth);
            super.onDraw(canvas);
            getPaint().setStyle(Paint.Style.FILL);
            getPaint().setFakeBoldText(false);
            setTextColor(textFillColor);
        }
        super.onDraw(canvas);
        isNeedToAdjust(canvas, Adjuster.Opportunity.AT_LAST);
    }

    private void drawStrokeLine(Canvas canvas) {
        if (strokeWidth > 0) {
            if (strokeWidthPath == null) {
                strokeWidthPath = new Path();
            } else {
                strokeWidthPath.reset();
            }
            if (strokeLineRectF == null) {
                strokeLineRectF = new RectF();
            } else {
                strokeLineRectF.setEmpty();
            }
            strokeLineRectF.set(strokeWidth / 2, strokeWidth / 2, width - strokeWidth / 2,
                    height - strokeWidth / 2);
            getCorners(corner);
            strokeWidthPath.addRoundRect(strokeLineRectF, corners, Path.Direction.CW);
            initPaint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(strokeColor);
            paint.setStrokeWidth(strokeWidth);
            canvas.drawPath(strokeWidthPath, paint);
        }
    }

    private void drawSolid(Canvas canvas) {
        if (solidPath == null) {
            solidPath = new Path();
        } else {
            solidPath.reset();
        }
        if (solidRectF == null) {
            solidRectF = new RectF();
        } else {
            solidRectF.setEmpty();
        }
        solidRectF.set(strokeWidth, strokeWidth, width - strokeWidth, height - strokeWidth);
        getCorners(corner - strokeWidth / 2);
        solidPath.addRoundRect(solidRectF, corners, Path.Direction.CW);

        initPaint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(solid);
        canvas.drawPath(solidPath, paint);
    }

    private float[] getCorners(float corner) {
        leftTopCorner[0] = 0;
        leftTopCorner[1] = 0;
        rightTopCorner[0] = 0;
        rightTopCorner[1] = 0;
        leftBottomCorner[0] = 0;
        leftBottomCorner[1] = 0;
        rightBottomCorner[0] = 0;
        rightBottomCorner[1] = 0;
        if (this.leftTopCornerEnable || this.rightTopCornerEnable || this.leftBottomCornerEnable
                || this.rightBottomCornerEnable) {
            if (this.leftTopCornerEnable) {
                leftTopCorner[0] = corner;
                leftTopCorner[1] = corner;
            }
            if (this.rightTopCornerEnable) {
                rightTopCorner[0] = corner;
                rightTopCorner[1] = corner;
            }
            if (this.leftBottomCornerEnable) {
                leftBottomCorner[0] = corner;
                leftBottomCorner[1] = corner;
            }
            if (this.rightBottomCornerEnable) {
                rightBottomCorner[0] = corner;
                rightBottomCorner[1] = corner;
            }
        } else {
            leftTopCorner[0] = corner;
            leftTopCorner[1] = corner;
            rightTopCorner[0] = corner;
            rightTopCorner[1] = corner;
            leftBottomCorner[0] = corner;
            leftBottomCorner[1] = corner;
            rightBottomCorner[0] = corner;
            rightBottomCorner[1] = corner;
        }
        corners[0] = leftTopCorner[0];
        corners[1] = leftTopCorner[1];
        corners[2] = rightTopCorner[0];
        corners[3] = rightTopCorner[1];
        corners[4] = rightBottomCorner[0];
        corners[5] = rightBottomCorner[1];
        corners[6] = leftBottomCorner[0];
        corners[7] = leftBottomCorner[1];
        return corners;
    }

    private void drawStateDrawable(Canvas canvas) {
        if (drawable != null && isShowState) {
            getDrawableBounds();
            drawable.setBounds((int) drawableBounds[0], (int) drawableBounds[1], (int) drawableBounds[2],
                    (int) drawableBounds[3]);
            drawable.draw(canvas);
        }
    }

    private float[] getDrawableBounds() {
        for (int i = 0; i < drawableBounds.length; i++) {
            drawableBounds[i] = 0;
        }
        drawableWidth = (drawableWidth == 0 ? width / 2f : drawableWidth);
        drawableHeight = (drawableHeight == 0 ? height / 2f : drawableHeight);
        switch (DrawableMode.valueOf(stateDrawableMode)) {
            case LEFT: // left
                drawableBounds[0] = 0 + drawablePaddingLeft;
                drawableBounds[1] = height / 2f - drawableHeight / 2f + drawablePaddingTop;
                drawableBounds[2] = drawableBounds[0] + drawableWidth;
                drawableBounds[3] = drawableBounds[1] + drawableHeight;
                break;
            case TOP: // top
                drawableBounds[0] = width / 2f - drawableWidth / 2f + drawablePaddingLeft;
                drawableBounds[1] = 0 + drawablePaddingTop;
                drawableBounds[2] = drawableBounds[0] + drawableWidth;
                drawableBounds[3] = drawableBounds[1] + drawableHeight;
                break;
            case RIGHT: // right
                drawableBounds[0] = width - drawableWidth + drawablePaddingLeft;
                drawableBounds[1] = height / 2 - drawableHeight / 2 + drawablePaddingTop;
                drawableBounds[2] = drawableBounds[0] + drawableWidth;
                drawableBounds[3] = drawableBounds[1] + drawableHeight;
                break;
            case BOTTOM: // bottom
                drawableBounds[0] = width / 2f - drawableWidth / 2f + drawablePaddingLeft;
                drawableBounds[1] = height - drawableHeight + drawablePaddingTop;
                drawableBounds[2] = drawableBounds[0] + drawableWidth;
                drawableBounds[3] = drawableBounds[1] + drawableHeight;
                break;
            case CENTER: // center
                drawableBounds[0] = width / 2f - drawableWidth / 2f + drawablePaddingLeft;
                drawableBounds[1] = height / 2 - drawableHeight / 2 + drawablePaddingTop;
                drawableBounds[2] = drawableBounds[0] + drawableWidth;
                drawableBounds[3] = drawableBounds[1] + drawableHeight;
                break;
            case FILL: // fill
                drawableBounds[0] = 0;
                drawableBounds[1] = 0;
                drawableBounds[2] = width;
                drawableBounds[3] = height;
                break;
            case LEFT_TOP: // leftTop
                drawableBounds[0] = 0 + drawablePaddingLeft;
                drawableBounds[1] = 0 + drawablePaddingTop;
                drawableBounds[2] = drawableBounds[0] + drawableWidth;
                drawableBounds[3] = drawableBounds[1] + drawableHeight;
                break;
            case RIGHT_TOP: // rightTop
                drawableBounds[0] = width - drawableWidth + drawablePaddingLeft;
                drawableBounds[1] = 0 + drawablePaddingTop;
                drawableBounds[2] = drawableBounds[0] + drawableWidth;
                drawableBounds[3] = drawableBounds[1] + drawableHeight;
                break;
            case LEFT_BOTTOM: // leftBottom
                drawableBounds[0] = 0 + drawablePaddingLeft;
                drawableBounds[1] = height - drawableHeight + drawablePaddingTop;
                drawableBounds[2] = drawableBounds[0] + drawableWidth;
                drawableBounds[3] = drawableBounds[1] + drawableHeight;
                break;
            case RIGHT_BOTTOM: // rightBottom
                drawableBounds[0] = width - drawableWidth + drawablePaddingLeft;
                drawableBounds[1] = height - drawableHeight + drawablePaddingTop;
                drawableBounds[2] = drawableBounds[0] + drawableWidth;
                drawableBounds[3] = drawableBounds[1] + drawableHeight;
                break;
        }

        return drawableBounds;
    }

    private void isNeedToAdjust(Canvas canvas, Adjuster.Opportunity currentOpportunity) {
        if (autoAdjust) {
            if (adjuster == null) {
                setAdjuster(new DefaultAdjuster());
            } else {
                if (currentOpportunity == adjuster.getOpportunity()) {
                    adjuster.adjust(this, canvas);
                }
            }
        }
    }

    public float getCorner() {
        return corner;
    }

    public void setCorner(float corner) {
        this.corner = corner;
        postInvalidate();

    }

    public int getSolid() {
        return solid;
    }

    public void setSolid(int solid) {
        this.solid = solid;
        postInvalidate();
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;

        postInvalidate();
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        postInvalidate();
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
        postInvalidate();
    }

    public boolean isShowState() {
        return isShowState;
    }

    public void setShowState(boolean showState) {
        isShowState = showState;
        postInvalidate();
    }

    public int getStateDrawableMode() {
        return stateDrawableMode;
    }

    public void setStateDrawableMode(int stateDrawableMode) {
        this.stateDrawableMode = stateDrawableMode;
        postInvalidate();
    }

    public void setAdjuster(Adjuster adjuster) {
        this.adjuster = adjuster;
        postInvalidate();
    }

    public Adjuster getAdjuster() {
        return adjuster;
    }

    public boolean isTextStroke() {
        return textStroke;
    }

    public void setTextStroke(boolean textStroke) {
        this.textStroke = textStroke;
        postInvalidate();

    }

    public int getTextStrokeColor() {
        return textStrokeColor;
    }

    public void setTextStrokeColor(int textStrokeColor) {
        this.textStrokeColor = textStrokeColor;
        postInvalidate();

    }

    public int getTextFillColor() {
        return textFillColor;
    }

    public void setTextFillColor(int textFillColor) {
        this.textFillColor = textFillColor;
        postInvalidate();

    }

    public float getTextStrokeWidth() {
        return textStrokeWidth;
    }

    public void setTextStrokeWidth(float textStrokeWidth) {
        this.textStrokeWidth = textStrokeWidth;
        postInvalidate();
    }

    public boolean isAutoAdjust() {
        return autoAdjust;
    }

    public void setAutoAdjust(boolean autoAdjust) {
        this.autoAdjust = autoAdjust;
    }

    public boolean isLeftTopCornerEnable() {
        return leftTopCornerEnable;
    }

    public void setLeftTopCornerEnable(boolean leftTopCornerEnable) {
        this.leftTopCornerEnable = leftTopCornerEnable;
        postInvalidate();

    }

    public boolean isRightTopCornerEnable() {
        return rightTopCornerEnable;
    }

    public void setRightTopCornerEnable(boolean rightTopCornerEnable) {
        this.rightTopCornerEnable = rightTopCornerEnable;
        postInvalidate();

    }

    public boolean isLeftBottomCornerEnable() {
        return leftBottomCornerEnable;
    }

    public void setLeftBottomCornerEnable(boolean leftBottomCornerEnable) {
        this.leftBottomCornerEnable = leftBottomCornerEnable;
        postInvalidate();

    }

    public boolean isRightBottomCornerEnable() {
        return rightBottomCornerEnable;
    }

    public void setRightBottomCornerEnable(boolean rightBottomCornerEnable) {
        this.rightBottomCornerEnable = rightBottomCornerEnable;
        postInvalidate();

    }

    public float getDrawableWidth() {
        return drawableWidth;
    }

    public void setDrawableWidth(float drawableWidth) {
        this.drawableWidth = drawableWidth;
        postInvalidate();

    }

    public float getDrawableHeight() {
        return drawableHeight;
    }

    public void setDrawableHeight(float drawableHeight) {
        this.drawableHeight = drawableHeight;
        postInvalidate();

    }

    public float getDrawablePaddingLeft() {
        return drawablePaddingLeft;
    }

    public void setDrawablePaddingLeft(float drawablePaddingLeft) {
        this.drawablePaddingLeft = drawablePaddingLeft;
        postInvalidate();

    }

    public float getDrawablePaddingTop() {
        return drawablePaddingTop;
    }

    public void setDrawablePaddingTop(float drawablePaddingTop) {
        this.drawablePaddingTop = drawablePaddingTop;
        postInvalidate();

    }

    public void startAnim() {
        needRun = true;
        runnable = false;
        if (animThread == null) {
            needRun = true;
            runnable = true;
            animThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (runnable) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                postInvalidate();
                            }
                        });
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            runnable = false;
                        }
                        Log.e("SuperTextView", " -> startAnim: " + Thread.currentThread().getId() + "-> "
                                + hashCode() + ": It's running!");
                    }
                    animThread = null;
                    if (needRun) {
                        startAnim();
                    }
                }
            });
            animThread.start();
        }
    }

    public void stopAnim() {
        runnable = false;
        needRun = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (adjuster != null) {
            if (adjuster.onTouch(this, event) && isAutoAdjust()) {
                super.onTouchEvent(event);
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != VISIBLE) {
            cacheRunnableState = runnable;
            cacheNeedRunState = needRun;
            stopAnim();
        } else if (cacheRunnableState && cacheNeedRunState) {
            startAnim();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnim();
        super.onDetachedFromWindow();
    }

    @Override
    protected void finalize() throws Throwable {
        stopAnim();
        super.finalize();
    }

    public static abstract class Adjuster {
        private Opportunity opportunity = Opportunity.BEFORE_TEXT;

        protected abstract void adjust(SuperTextView v, Canvas canvas);

        public boolean onTouch(SuperTextView v, MotionEvent event) {
            return false;
        }

        ;

        public Opportunity getOpportunity() {
            return opportunity;
        }

        public void setOpportunity(Opportunity opportunity) {
            this.opportunity = opportunity;
        }

        public static enum Opportunity {
            BEFORE_DRAWABLE, BEFORE_TEXT, AT_LAST
        }
    }

    public static class DefaultAdjuster extends Adjuster {

        @Override
        public void adjust(SuperTextView v, Canvas canvas) {
            int length = v.length();
            float scale = v.getWidth() / (116.28f * v.getResources().getDisplayMetrics().density);
            float[] textSizes = {
                    37.21f, 37.21f, 24.81f, 27.9f, 24.81f,
                    22.36f, 18.6f,
                    18.6f
            };
            switch (length) {
                case 1:
                    v.setTextSize(textSizes[0] * scale);
                    break;
                case 2:
                    v.setTextSize(textSizes[1] * scale);
                    break;
                case 3:
                    v.setTextSize(textSizes[2] * scale);
                    break;
                case 4:
                    v.setTextSize(textSizes[3] * scale);
                    break;
                case 5:
                case 6:
                    v.setTextSize(textSizes[4] * scale);
                    break;
                case 7:
                case 8:
                case 9:
                    v.setTextSize(textSizes[5] * scale);
                    break;
                case 10:
                case 11:
                case 12:
                    v.setTextSize(textSizes[6] * scale);
                    break;
                case 13:
                case 14:
                case 15:
                case 16:
                    v.setTextSize(textSizes[7] * scale);
                    break;
            }
        }

    }

    public static enum DrawableMode {
        LEFT(0), TOP(1), RIGHT(2), BOTTOM(3), CENTER(4), FILL(5), LEFT_TOP(6), RIGHT_TOP(7),
        LEFT_BOTTOM(8), RIGHT_BOTTOM(9);
        public int code;

        DrawableMode(int code) {
            this.code = code;
        }

        public static DrawableMode valueOf(int code) {
            for (DrawableMode mode : DrawableMode.values()) {
                if (mode.code == code) {
                    return mode;
                }
            }
            return CENTER;
        }
    }
}

