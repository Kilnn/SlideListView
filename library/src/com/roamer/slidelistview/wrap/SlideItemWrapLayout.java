package com.roamer.slidelistview.wrap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.swiplistview.R;
import com.roamer.slidelistview.SlideListView.SlideAction;

/**
 * wrap the listview item,It may have three directly sub view(front view ,left back view,right back view).<br/>
 * front view must be not null.The other is not necessarily
 * 
 * @author Dean Tao
 * 
 */
public class SlideItemWrapLayout extends RelativeLayout {

	private View mLeftBackView;
	private View mRightBackView;
	private FrontViewWrapLayout mFrontView;

	private SlideAction mSlideLeftAction;
	private SlideAction mSlideRightAction;

	/**
	 * 
	 * @param context
	 * @param slideLeftAction
	 *            Decided where the left view placed.<br/>
	 *            if slideLeftAction==SCROLL,will placed to left of front view<br/>
	 *            if slideLeftAction==REVEAL,will placed below of front view<br/>
	 * @param slideRightAction
	 *            Decided where the right view placed.<br/>
	 *            if slideRightAction==SCROLL,will placed to right of front view<br/>
	 *            if slideRightAction==REVEAL,will placed below of front view<br/>
	 * @param frontViewId
	 *            front view layout id. Must be an effective
	 * @param leftBackViewId
	 *            left back view layout id.if leftBackViewId ==0,there is no left back view,and the slideLeftAction will be ignored
	 * @param rightBackViewId
	 *            right back view layout id.if rightBackViewId ==0,there is no right back view,and the slideRightAction will be ignored
	 */
	public SlideItemWrapLayout(Context context, SlideAction slideLeftAction, SlideAction slideRightAction, int frontViewId, int leftBackViewId,
			int rightBackViewId) {
		super(context);
		mSlideLeftAction = slideLeftAction;
		mSlideRightAction = slideRightAction;
		init(frontViewId, leftBackViewId, rightBackViewId);
	}

	private void init(int frontViewId, int leftBackViewId, int rightBackViewId) {
		setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		View frontView = null;
		if (frontViewId != 0) {
			frontView = LayoutInflater.from(getContext()).inflate(frontViewId, this, false);
		}
		if (frontView == null) {
			throw new NullPointerException("frontView can not be null");
		}
		View leftBackView = null;
		View rightBackView = null;
		if (leftBackViewId != 0) {
			leftBackView = LayoutInflater.from(getContext()).inflate(leftBackViewId, this, false);
		}

		if (rightBackViewId != 0) {
			rightBackView = LayoutInflater.from(getContext()).inflate(rightBackViewId, this, false);
		}

		addLeftBackView(leftBackView);
		addRightBackView(rightBackView);
		addFrontView(frontView);
	}

	private void addFrontView(View frontView) {
		RelativeLayout.LayoutParams params = (LayoutParams) frontView.getLayoutParams();
		if (params == null) {
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		}

		FrontViewWrapLayout wrapLayout = new FrontViewWrapLayout(getContext());
		wrapLayout.addView(frontView, params);
		wrapLayout.setId(R.id.slide_id_front_view);

		addView(wrapLayout, params);
		mFrontView = wrapLayout;
	}

	private void addLeftBackView(View leftBackView) {
		if (leftBackView == null) {
			return;
		}
		RelativeLayout.LayoutParams params = (LayoutParams) leftBackView.getLayoutParams();
		if (params == null) {// default LayoutParams
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		}
		switch (mSlideLeftAction) {
		case SCROLL:
			params.addRule(RelativeLayout.LEFT_OF, R.id.slide_id_front_view);
			break;
		case REVEAL:
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			break;
		default:
			break;
		}
		leftBackView.setLayoutParams(params);
		leftBackView.setId(R.id.slide_id_left_back_view);
		addView(leftBackView);
		mLeftBackView = leftBackView;
		/**
		 * must set INVISIBLE.<br/>
		 * When the slide item is not opend,The motion event could not be dispatch to left/right back view.<br/>
		 * So set left/right back view INVISIBLE,then we can response the OnItemClickListener.<br/>
		 * (Should not be GONE,because if it is GONE,the measure width and height will be 0)
		 */
		setLeftBackViewShow(false);
	}

	private void addRightBackView(View rightBackView) {
		if (rightBackView == null) {
			return;
		}
		RelativeLayout.LayoutParams params = (LayoutParams) rightBackView.getLayoutParams();
		if (params == null) {// default LayoutParams
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		}
		switch (mSlideRightAction) {
		case SCROLL:
			params.addRule(RelativeLayout.RIGHT_OF, R.id.slide_id_front_view);
			break;
		case REVEAL:
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			break;
		default:
			break;
		}
		rightBackView.setLayoutParams(params);
		rightBackView.setId(R.id.slide_id_right_back_view);
		addView(rightBackView);
		mRightBackView = rightBackView;
		/**
		 * must set INVISIBLE.<br/>
		 * When the slide item is not opend,The motion event could not be dispatch to left/right back view.<br/>
		 * So set left/right back view INVISIBLE,then we can response the OnItemClickListener.<br/>
		 * (Should not be GONE,because if it is GONE,the measure width and height will be 0)
		 */
		setRightBackViewShow(false);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int parentWidthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
		int parentHeightSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
		if (mLeftBackView != null ) {
			LayoutParams params = (LayoutParams) mLeftBackView.getLayoutParams();
			int widthSpec = ViewGroup.getChildMeasureSpec(parentWidthSpec, getPaddingLeft() + getPaddingRight() + params.leftMargin
					+ params.rightMargin, params.width);
			int heightSpec = ViewGroup.getChildMeasureSpec(parentHeightSpec, getPaddingTop() + getPaddingBottom() + params.topMargin
					+ params.bottomMargin, params.height);
			mLeftBackView.measure(widthSpec, heightSpec);
		}
		if (mRightBackView != null ) {
			LayoutParams params = (LayoutParams) mRightBackView.getLayoutParams();
			int widthSpec = ViewGroup.getChildMeasureSpec(parentWidthSpec, getPaddingLeft() + getPaddingRight() + params.leftMargin
					+ params.rightMargin, params.width);
			int heightSpec = ViewGroup.getChildMeasureSpec(parentHeightSpec, getPaddingTop() + getPaddingBottom() + params.topMargin
					+ params.bottomMargin, params.height);
			mRightBackView.measure(widthSpec, heightSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mLeftBackView != null) {
			// Always let backView in center of the item
			int top = (b - t - mLeftBackView.getMeasuredHeight()) / 2;
			if (mSlideLeftAction == SlideAction.SCROLL) {
				mLeftBackView.layout(mFrontView.getLeft() - mLeftBackView.getMeasuredWidth(), top, mLeftBackView.getRight(),
						top + mLeftBackView.getMeasuredHeight());
			} else {
				mLeftBackView.layout(mLeftBackView.getLeft(), top, mLeftBackView.getRight(), top + mLeftBackView.getMeasuredHeight());
			}

		}

		if (mRightBackView != null) {
			// Always let backView in center of the item
			int top = (b - t - mRightBackView.getMeasuredHeight()) / 2;
			if (mSlideRightAction == SlideAction.SCROLL) {
				mRightBackView.layout(mFrontView.getRight(), top, mFrontView.getRight() + mRightBackView.getMeasuredWidth(),
						top + mRightBackView.getMeasuredHeight());
			} else {
				mRightBackView.layout(mRightBackView.getLeft(), top, mRightBackView.getRight(), top + mRightBackView.getMeasuredHeight());
			}

		}
	}

	/**
	 * front view must not be null
	 * 
	 * @return
	 */
	public FrontViewWrapLayout getFrontView() {
		return mFrontView;
	}

	/**
	 * left back view could be null
	 * 
	 * @return
	 */
	public View getLeftBackView() {
		return mLeftBackView;
	}

	/**
	 * right back view could be null
	 * 
	 * @return
	 */
	public View getRightBackView() {
		return mRightBackView;
	}

	public void setLeftBackViewShow(boolean show) {
		setViewShow(mLeftBackView, show);
	}

	public void setRightBackViewShow(boolean show) {
		setViewShow(mRightBackView, show);
	}

	private void setViewShow(View view, boolean show) {
		if (view == null) {
			return;
		}
		if (show) {
			if (view.getVisibility() != View.VISIBLE) {
				view.setVisibility(View.VISIBLE);
			}
		} else {
			if (view.getVisibility() != View.INVISIBLE) {
				view.setVisibility(View.INVISIBLE);
			}
		}
	}

}
