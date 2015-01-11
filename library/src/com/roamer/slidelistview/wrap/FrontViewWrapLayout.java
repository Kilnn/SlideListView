package com.roamer.slidelistview.wrap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * wrap the front view ,so we can handle motion event more simple
 * 
 * @author Dean Tao
 * 
 */
public class FrontViewWrapLayout extends LinearLayout {
	private boolean isOpend;// whether the front view is opend

	public FrontViewWrapLayout(Context context) {
		super(context);
	}

	public FrontViewWrapLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FrontViewWrapLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// if the front view is opend,drop all motion event(include sub view)
		if (isOpend) {
			return false;
		}
		return super.dispatchTouchEvent(ev);
	}

	public void setOpend(boolean isOpend) {
		this.isOpend = isOpend;
	}
}
