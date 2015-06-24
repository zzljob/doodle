package com.yepstudio.android.library.doodle;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * 画板
 * @author zzljob@gmail.com
 * @createDate 2013年11月28日
 */
public class Doodle extends RelativeLayout implements OnClickListener {

	private static String TAG = "Doodle-Widget";

	private AtomicBoolean isShowPaintOpt = new AtomicBoolean(false);
	private LayoutInflater inflater;

	private OnDoodleListener onDoodleListener;
	private File saveFile;
	private String[] suffixs = new String[] { ".png", ".jpg", ".jpeg" };

	private DrawingBoard mDrawingBoard;
	private ImageButton mImgbtnFinish;
	private ImageButton mImgbtnSettings;
	private LinearLayout mLayoutColor;
	private LinearLayout mLayoutPaint;

	public Doodle(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public Doodle(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public Doodle(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Resources res = context.getResources();

		mDrawingBoard = new DrawingBoard(context);
		int w = LayoutParams.MATCH_PARENT;
		int h = LayoutParams.MATCH_PARENT;
		mDrawingBoard.setLayoutParams(new LayoutParams(w, h));
		mDrawingBoard.setBackgroundColor(res.getColor(android.R.color.transparent));
		addView(mDrawingBoard);
		
		mImgbtnFinish = new ImageButton(context);
		mImgbtnFinish.setBackgroundResource(R.drawable.doodle_icon_flat_check);
		w = res.getDimensionPixelSize(R.dimen.doodle_imgbtn_width);
		h = res.getDimensionPixelSize(R.dimen.doodle_imgbtn_height);
		LayoutParams layoutParams = new LayoutParams(w, h);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutParams.rightMargin = res.getDimensionPixelSize(R.dimen.doodle_margin_right);
		layoutParams.topMargin = res.getDimensionPixelSize(R.dimen.doodle_margin_top);
		addView(mImgbtnFinish, layoutParams);
		mImgbtnFinish.setOnClickListener(this);
		
		mLayoutColor = (LinearLayout) inflater.inflate(R.layout.doodle_layout_color, null);
		w = LayoutParams.WRAP_CONTENT;
		h = LayoutParams.WRAP_CONTENT;
		layoutParams = new LayoutParams(w, h);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		layoutParams.leftMargin = res.getDimensionPixelSize(R.dimen.doodle_margin_left);
		layoutParams.topMargin = res.getDimensionPixelSize(R.dimen.doodle_margin_top);
		layoutParams.topMargin += res.getDimensionPixelSize(R.dimen.doodle_imgbtn_height);
		layoutParams.topMargin += res.getDimensionPixelSize(R.dimen.doodle_imgbtn_margin);
		addView(mLayoutColor, layoutParams);
		int count = mLayoutColor.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = mLayoutColor.getChildAt(i);
			if (ImageButton.class.equals(v.getClass())) {
				v.setOnClickListener(colorListener);
			}
		}
		
		mLayoutPaint = (LinearLayout) inflater.inflate(R.layout.doodle_layout_paint, null);
		w = LayoutParams.WRAP_CONTENT;
		h = LayoutParams.WRAP_CONTENT;
		layoutParams = new LayoutParams(w, h);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		layoutParams.topMargin = res.getDimensionPixelSize(R.dimen.doodle_margin_top);
		layoutParams.leftMargin = res.getDimensionPixelSize(R.dimen.doodle_margin_left);
		layoutParams.leftMargin += res.getDimensionPixelSize(R.dimen.doodle_imgbtn_width);
		layoutParams.leftMargin += res.getDimensionPixelSize(R.dimen.doodle_imgbtn_margin);
		addView(mLayoutPaint, layoutParams);
		count = mLayoutPaint.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = mLayoutPaint.getChildAt(i);
			if (ImageButton.class.equals(v.getClass())) {
				v.setOnClickListener(paintListener);
			}
		}
		
		mImgbtnSettings = new ImageButton(context);
		mImgbtnSettings.setBackgroundResource(R.drawable.doodle_icon_flat_setting);
		w = res.getDimensionPixelSize(R.dimen.doodle_imgbtn_width);
		h = res.getDimensionPixelSize(R.dimen.doodle_imgbtn_height);
		layoutParams = new LayoutParams(w, h);
		layoutParams.leftMargin = res.getDimensionPixelSize(R.dimen.doodle_margin_left);
		layoutParams.topMargin = res.getDimensionPixelSize(R.dimen.doodle_margin_top);
		addView(mImgbtnSettings, layoutParams);
		mImgbtnSettings.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if (v == mImgbtnFinish) {
			mImgbtnFinish.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.doodle_alpha));
			
			Bitmap mBitmap = mDrawingBoard.getBitmap();
			if (onDoodleListener != null) {
				onDoodleListener.onSaveCanvas(mBitmap);
			}
			saveToFile(mBitmap);
		} else if (v == mImgbtnSettings) {
			if (isShowPaintOpt.get()) {
				closePaintAndColor(true);
			} else {
				showPaintAndColor(true);
			}
		}
	}
	
	/**
	 * 清除涂鸦
	 */
	public void clearDoodle() {
		mDrawingBoard.clear();
		invalidate();
	}
	
	public void destroy() {
		Bitmap bitmap = mDrawingBoard.getBitmap();
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}
	}
	
	private boolean saveToFile(Bitmap mBitmap) {
		if (saveFile != null) {
			BufferedOutputStream bos = null;
			try {
				if (saveFile.exists()) {
					saveFile.delete();
				}
				String suffix = saveFile.getAbsolutePath().substring(saveFile.getAbsolutePath().lastIndexOf(".") + 1);
				bos = new BufferedOutputStream(new FileOutputStream(saveFile));
				if ("jpg".equalsIgnoreCase(suffix)
						|| "jpeg".equalsIgnoreCase(suffix)) {
					mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
				} else if ("png".equalsIgnoreCase(suffix)) {
					mBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
				}
				bos.flush();
				Log.d(TAG, "saveToFile success:" + saveFile.getAbsolutePath());
				return true;
			} catch (Exception e) {
				Log.e(TAG, "save to file error", e);
			} finally {
				if (bos != null) {
					try {
						bos.close();
					} catch (IOException e1) {

					}
				}
			}
		}
		return false;
	}
	
	public void showPaintAndColor(boolean hasAnimation) {
		isShowPaintOpt.set(true);
		mLayoutColor.setVisibility(View.VISIBLE);
		mLayoutPaint.setVisibility(View.VISIBLE);
		if(hasAnimation) {
			mImgbtnSettings.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.doodle_rotate_right_open));
			mLayoutColor.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.doodle_push_down_out));
			mLayoutPaint.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.doodle_push_right_out));
		}
		if (onDoodleListener != null) {
			onDoodleListener.onTogglePaintAndColor(hasAnimation, isShowPaintOpt.get());
		}
	}
	
	public void closePaintAndColor(boolean hasAnimation) {
		isShowPaintOpt.set(false);
		if(hasAnimation) {
			mImgbtnSettings.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.doodle_rotate_left_close));
			mLayoutColor.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.doodle_push_up_in));
			mLayoutPaint.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.doodle_push_left_in));
		} else {
			mLayoutColor.setVisibility(View.GONE);
			mLayoutPaint.setVisibility(View.GONE);
		}
		if (onDoodleListener != null) {
			onDoodleListener.onTogglePaintAndColor(hasAnimation, isShowPaintOpt.get());
		}
	}

	private OnClickListener colorListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int count = mLayoutColor.getChildCount();
			for (int i = 0; i < count; i++) {
				View view = mLayoutColor.getChildAt(i);
				view.setSelected(false);
			}
			v.setSelected(true);
			String str = (String) v.getTag();
			if (!TextUtils.isEmpty(str)) {
				try {
					mDrawingBoard.setPaintColor(Color.parseColor(str));
				} catch (Exception e) {
					Log.e(TAG, "", e);
				}
			} else {
				mDrawingBoard.clearPaintColor();
			}
			closePaintAndColor(true);
		}
	};
	
	private OnClickListener paintListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int count = mLayoutPaint.getChildCount();
			for (int i = 0; i < count; i++) {
				View view = mLayoutPaint.getChildAt(i);
				view.setSelected(false);
			}
			v.setSelected(true);
			String str = (String) v.getTag();
			if (!TextUtils.isEmpty(str)) {
				try {
					mDrawingBoard.setPaintStrokeWidth(Integer.parseInt(str));
				} catch (Exception e) {
					Log.e(TAG, "", e);
				}
			}
			closePaintAndColor(true);
		}
	};
	
	public interface OnDoodleListener {
		public void onTogglePaintAndColor(boolean hasAnimation, boolean isShow);

		public void onDrawPaint(boolean isClear);

		public void onSaveCanvas(Bitmap mBitmap);
	}
	
	public static class SimpleDoodleListener implements OnDoodleListener {

		@Override
		public void onTogglePaintAndColor(boolean hasAnimation, boolean isShow) {
			
		}

		@Override
		public void onDrawPaint(boolean isClear) {
			
		}

		@Override
		public void onSaveCanvas(Bitmap mBitmap) {
			
		}
	}

	public void setOnDoodleListener(OnDoodleListener onDoodleListener) {
		this.onDoodleListener = onDoodleListener;
		mDrawingBoard.setOnDoodleListener(onDoodleListener);
	}

	public File getSaveFile() {
		return saveFile;
	}

	public boolean setSaveFile(String filePath, boolean force) {
		File file = new File(filePath);
		String suffix = "";
		if(filePath.lastIndexOf(".")> -1){
			suffix = filePath.substring(filePath.lastIndexOf("."));
		}
		boolean isSupport = false;
		for (String x : suffixs) {
			if (x.equals(suffix)) {
				isSupport = true;
				break;
			}
		}
		
		if (!isSupport) {
			Log.w(TAG, "File is not Support this file, suffix:" + suffix);
			return false;
		}
		if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
			Log.w(TAG, "Directory is not exists, and create it fail.");
			return false;
		}
		if (!force && file.exists()) {
			Log.w(TAG, "File is exists, so fail.");
			return false;
		}
		this.saveFile = file;
		return true;
	}

}
