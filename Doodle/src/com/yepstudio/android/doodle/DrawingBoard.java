package com.yepstudio.android.doodle;

import com.yepstudio.android.doodle.Doodle.OnDoodleListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 画板
 * 
 * @author zhangzl@fund123.cn
 * @createDate 2013年11月27日
 */
public class DrawingBoard extends View {
	
	private static DoodleLog log = DoodleLog.getInstanceClazz(DrawingBoard.class);

	private Bitmap bitmap;
	private Canvas canvas;
	private Path path;//
	private Paint paint;//画笔
	private PorterDuffXfermode XfermodeRepeat;
	private PorterDuffXfermode XfermodeClear;
	
	private OnDoodleListener onDoodleListener; 

	public DrawingBoard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public DrawingBoard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DrawingBoard(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		path = new Path();
		
		XfermodeRepeat = new PorterDuffXfermode(Mode.SRC_OVER);
		XfermodeClear = new PorterDuffXfermode(Mode.CLEAR);

		paint = new Paint();// 创建画笔渲染对象
		paint.setAntiAlias(true);// 设置抗锯齿，让绘画比较平滑
		paint.setDither(true);// 设置递色
		paint.setXfermode(XfermodeRepeat);
		paint.setColor(getResources().getColor(android.R.color.black));// 设置画笔的颜色
		paint.setStyle(Paint.Style.STROKE);// 画笔的类型有三种（1.FILL 2.FILL_AND_STROKE 3.STROKE ）
		paint.setStrokeJoin(Paint.Join.ROUND);// 默认类型是MITER（1.BEVEL 2.MITER 3.ROUND ）
		paint.setStrokeCap(Paint.Cap.ROUND);// 默认类型是BUTT（1.BUTT 2.ROUND 3.SQUARE ）
		paint.setStrokeWidth(12);// 设置描边的宽度，如果设置的值为0那么边是一条极细的线
		paint.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.NORMAL));
	}
	
	/**
	 * 设置画笔的颜色
	 * @param color
	 */
	public void setPaintColor(int color) {
		paint.setColor(color);
		paint.setXfermode(XfermodeRepeat);
	}
	
	public void clearPaintColor() {
		paint.setXfermode(XfermodeClear);
		paint.setColor(Color.TRANSPARENT);
	}
	
	/**
	 * 设置画笔的宽度
	 * @param width
	 */
	public void setPaintStrokeWidth(int width) {
		paint.setStrokeWidth(width);
	}

	private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

	private void touch_start(float x, float y) {
		path.reset();
		path.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
	}

	private void touch_up() {
		path.lineTo(mX, mY);
		// commit the path to our offscreen
		canvas.drawPath(path, paint);
		// kill this so we don't double draw
		path.reset();
		
		if (onDoodleListener != null) {
			onDoodleListener.onDrawPaint(paint.getXfermode() == XfermodeClear);
		}
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touch_start(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			touch_move(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			touch_up();
			invalidate();
			break;
		}
        return true;
    }

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (bitmap != null) {
			bitmap.recycle();
		}
		bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
		log.d("onSizeChanged");
	}
	
	public void clear() {
		if (bitmap != null) {
			bitmap.recycle();
		}
		int w = this.getMeasuredWidth();
		int h = this.getMeasuredHeight();
		bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
		log.d("onSizeChanged");
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		this.canvas.drawPath(path, paint);
		canvas.drawColor(getResources().getColor(android.R.color.transparent));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		log.d("onDraw");
	}

	protected void setOnDoodleListener(OnDoodleListener onDoodleListener) {
		this.onDoodleListener = onDoodleListener;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public Paint getPaint() {
		return paint;
	}

	public void setPaint(Paint paint) {
		this.paint = paint;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

}
