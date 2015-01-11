package com.roamer.slidelistview;

import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.AbsListView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.roamer.slidelistview.SlideListView.SlideAction;
import com.roamer.slidelistview.SlideListView.SlideMode;
import com.roamer.slidelistview.wrap.FrontViewWrapLayout;
import com.roamer.slidelistview.wrap.SlideItemWrapLayout;

public class SlideTouchListener implements OnTouchListener {
	private static final int INVALID_POINTER = -1;
	// Sliding status
	private static final int SLIDING_STATE_NONE = 0;// no sliding
	private static final int SLIDING_STATE_MANUAL = 1;// manual sliding
	private static final int SLIDING_STATE_AUTO = 2;// auto sliding

	private SlideListView mSlideListView;
	private int mTouchSlop;
	private long mConfigShortAnimationTime;

	private int mDownPosition;
	private int mActivePointerId;
	private int mDownMotionX;
	private VelocityTracker mVelocityTracker;
	private int mScrollState = SLIDING_STATE_NONE;
	//
	private SlideItem mSlideItem;

	public SlideTouchListener(SlideListView slideListView) {
		mSlideListView = slideListView;
		ViewConfiguration configuration = ViewConfiguration.get(slideListView.getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mConfigShortAnimationTime = slideListView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
	}

	/**
	 * reset items status when adapter is modified
	 */
	void reset() {
		mSlideItem = null;
		mScrollState = SLIDING_STATE_NONE;
	}

	/**
	 * If there is a item in opend status,return the position.Else return
	 * AbsListView.INVALID_POSITION =-1
	 * 
	 * @return
	 */
	public int getOpendPosition() {
		if (isOpend()) {
			return mSlideItem.position;
		}
		return AbsListView.INVALID_POSITION;
	}

	/**
	 * 
	 * @return if return true,there is in sliding,else not
	 */
	boolean isInSliding() {
		return mScrollState != SLIDING_STATE_NONE;
	}

	/**
	 * If there is a item in opend status,close it.Else do no thing
	 */
	public void closeOpenedItem() {
		if (isOpend()) {
			autoScroll(mSlideItem.offset, false);
		}
	}

	public boolean isOpend() {
		return mSlideItem != null && mSlideItem.isOpend();
	}

	private long getAnimationTime() {
		long time = mSlideListView.getAnimationTime();
		if (time <= 0) {
			time = mConfigShortAnimationTime;
		}
		return time;
	}

	private void initOrResetVelocityTracker() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		} else {
			mVelocityTracker.clear();
		}
	}

	private void initVelocityTrackerIfNotExists() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
	}

	private class SlideItem {
		/**
		 * The slideItem's position
		 */
		private final int position;
		// slide item's view
		private SlideItemWrapLayout child;
		private FrontViewWrapLayout frontView;
		private View leftBackView;
		private View rightBackView;
		/**
		 * represent the offset of slide item(Actual,it is the front view's
		 * offset).<br/>
		 * The value must between {@link #minOffset} and {@link #maxOffset}.<br/>
		 * if the item has no sliding any more,offset==0.
		 */
		private int offset;

		/**
		 * if rightBackView!=null && rightBackView.getWidth()!=0,then the
		 * item(front view) can sliding to left.<br/>
		 * than the {@link #offset} will less than 0.So the minOffset=
		 * -rightBackView.getWidth();
		 */
		private final int minOffset;

		/**
		 * if leftBackView!=null && leftBackView.getWidth()!=0,then the
		 * item(front view) can sliding to right.<br/>
		 * than the {@link #offset} will greater than 0.So the maxOffset=
		 * leftBackView.getWidth();
		 */
		private final int maxOffset;

		/**
		 * Record the previous offset value.Used for notify
		 * {@link #SlideItemListener}
		 */
		private int previousOffset;

		private int preDelatX;

		private int gingerbread_mr1_Offset;// Use fro sdk_version<=2.3.3.Else
											// always be 0

		public SlideItem(int pos) {
			position = pos;
			child = (SlideItemWrapLayout) mSlideListView.getChildAt(position - mSlideListView.getFirstVisiblePosition());
			if (child == null) {
				throw new NullPointerException("At position:" + position
						+ "child(Item) cann't be null.Are your sure you have use createConvertView() method in your adapter");
			}
			frontView = child.getFrontView();
			if (frontView == null) {
				throw new NullPointerException("At position:" + position
						+ "front view cann't be null.Are your sure you have use createConvertView() method in your adapter");
			}
			leftBackView = child.getLeftBackView();
			rightBackView = child.getRightBackView();
			SlideMode slideMode = mSlideListView.getSlideAdapter().getSlideModeInPosition(position - mSlideListView.getHeaderViewsCount());
			if (rightBackView != null && (slideMode == SlideMode.RIGHT || slideMode == SlideMode.BOTH)) {
				minOffset = -rightBackView.getWidth();
			} else {
				minOffset = 0;
			}
			if (leftBackView != null && (slideMode == SlideMode.LEFT || slideMode == SlideMode.BOTH)) {
				maxOffset = leftBackView.getWidth();
			} else {
				maxOffset = 0;
			}
		}

		private boolean isOpend() {
			return offset != 0 /*
								 * && (xOffset == xMinOffset || xOffset ==
								 * xMaxOffset)
								 */;
		}
	}

	private int getPointerIndex(MotionEvent event) {
		int pointerIndex = event.findPointerIndex(mActivePointerId);
		if (pointerIndex == INVALID_POINTER) {
			pointerIndex = 0;
			mActivePointerId = event.getPointerId(pointerIndex);
		}
		return pointerIndex;
	}

	boolean onInterceptTouchEvent(MotionEvent event) {
		int action = MotionEventCompat.getActionMasked(event);
		switch (action) {
		case MotionEvent.ACTION_DOWN: {// All MotionEvent.ACTION_DOWN will
										// dispatch to here
			if (isInSliding()) {// if previous slideing has not finished,prevent
								// it
				return true;
			}
			// reset
			mDownPosition = AbsListView.INVALID_POSITION;
			mDownMotionX = 0;
			mActivePointerId = INVALID_POINTER;

			int position = mSlideListView.pointToPosition((int) event.getX(), (int) event.getY());
			if (position == AbsListView.INVALID_POSITION) {
				break;
			}
			// don't allow swiping if this is on the header or footer or
			// IGNORE_ITEM_VIEW_TYPE or enabled is false on the adapter
			boolean allowSlide = mSlideListView.getAdapter().isEnabled(position) && mSlideListView.getAdapter().getItemViewType(position) >= 0;
			if (allowSlide) {
				// below or equals 3.0,the OnScrollListener callback has
				// error,so we need check the ListView scroll state
				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
					mSlideListView.checkScrolling();
				}
				mDownPosition = position;
				mActivePointerId = event.getPointerId(0);
				mDownMotionX = (int) event.getX();
				initOrResetVelocityTracker();
				mVelocityTracker.addMovement(event);
			}
		}
			break;
		case MotionEvent.ACTION_MOVE: {// If MotionEvent.ACTION_DOWN in the sub
										// view,MotionEvent.ACTION_MOVE will
										// dispatch to here at
										// first.So if it is sliding some
										// distance on the x axis,we should
										// intercept the touch event
			if (mDownPosition == AbsListView.INVALID_POSITION) {
				break;
			}
			if (mSlideListView.isInScrolling()) {
				break;
			}
			int pointerIndex = getPointerIndex(event);
			// get scroll speed
			initVelocityTrackerIfNotExists();
			mVelocityTracker.addMovement(event);
			mVelocityTracker.computeCurrentVelocity(1000);
			float velocityX = Math.abs(mVelocityTracker.getXVelocity(mActivePointerId));
			float velocityY = Math.abs(mVelocityTracker.getYVelocity(mActivePointerId));
			// whether is scroll on x axis
			boolean isScrollX = velocityX > velocityY;
			// get scroll distance
			int distance = Math.abs((int) event.getX(pointerIndex) - mDownMotionX);

			if (isScrollX && distance > mTouchSlop) {
				ViewParent parent = mSlideListView.getParent();
				if (parent != null) {
					parent.requestDisallowInterceptTouchEvent(true);
				}
				mScrollState = SLIDING_STATE_MANUAL;
				return true;
			}
		}
			break;
		// case MotionEvent.ACTION_UP:
		// case MotionEvent.ACTION_CANCEL:
		// default:
		// mScrollState = SLIDING_STATE_NONE;
		// break;
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (!mSlideListView.isEnabled() || !mSlideListView.isSlideEnable()) {
			return false;
		}
		int action = MotionEventCompat.getActionMasked(event);
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			if (isInSliding()) {// if previous slideing has not finished,prevent
								// it
				return true;
			}
		}
			break;
		case MotionEvent.ACTION_MOVE: {// Handle the event which dispatch to
										// ListView.If is in
										// sliding,intercept(return true)
			if (mDownPosition == AbsListView.INVALID_POSITION) {
				break;
			}
			if (mSlideListView.isInScrolling()) {
				break;
			}
			int pointerIndex = getPointerIndex(event);

			if (mScrollState == SLIDING_STATE_MANUAL) {
				if (mSlideItem == null) {// start sliding and init mSlideItem
					mSlideItem = new SlideItem(mDownPosition);
				}

				int deltaX = (int) event.getX(pointerIndex) - mDownMotionX;
				int nextOffset = deltaX - mSlideItem.preDelatX + mSlideItem.offset;
				mSlideItem.preDelatX = deltaX;
				if (nextOffset < mSlideItem.minOffset) {
					nextOffset = mSlideItem.minOffset;
				}
				if (nextOffset > mSlideItem.maxOffset) {
					nextOffset = mSlideItem.maxOffset;
				}
				if (mSlideItem.offset != nextOffset) {
					mSlideItem.offset = nextOffset;
					move(nextOffset);
				}
				return true;
			} else {
				// See onInterceptTouchEvent() Method
				initVelocityTrackerIfNotExists();
				mVelocityTracker.addMovement(event);
				mVelocityTracker.computeCurrentVelocity(1000);
				float velocityX = Math.abs(mVelocityTracker.getXVelocity(mActivePointerId));
				float velocityY = Math.abs(mVelocityTracker.getYVelocity(mActivePointerId));
				// whether is scroll on x axis
				boolean isScrollX = velocityX > velocityY;
				// get scroll distance
				int distance = Math.abs((int) event.getX(pointerIndex) - mDownMotionX);

				if (isScrollX && distance > mTouchSlop) {
					ViewParent parent = mSlideListView.getParent();
					if (parent != null) {
						parent.requestDisallowInterceptTouchEvent(true);
					}
					mScrollState = SLIDING_STATE_MANUAL;
					return true;
				}
			}
		}
			break;
		case MotionEvent.ACTION_UP: {
			if (mDownPosition == AbsListView.INVALID_POSITION) {
				break;
			}
			if (mSlideItem == null) {
				break;
			}
			if (mScrollState == SLIDING_STATE_MANUAL) {
				int pointerIndex = getPointerIndex(event);

				int deltaX = (int) event.getX(pointerIndex) - mDownMotionX;
				if (deltaX == 0) {// sliding distance equals 0
					reset();
					return true;
				}
				/*
				 * Don't need automatic sliding, has already reached a fixed
				 * position
				 */
				if (mSlideItem.offset == 0 || mSlideItem.offset == mSlideItem.minOffset || mSlideItem.offset == mSlideItem.maxOffset) {
					slidingFinish();
					return true;
				}

				SlideMode slideMode = mSlideListView.getSlideAdapter().getSlideModeInPosition(
						mSlideItem.position - mSlideListView.getHeaderViewsCount());
				boolean doOpen = false;// open or close
				if (mSlideItem.offset > 0) {// left back view is showing
					if (slideMode == SlideMode.LEFT || slideMode == SlideMode.BOTH) {// SlideMode
																						// support
																						// left
						// the move distance greater than leftBackView's width/4
						boolean distanceGreater = Math.abs(mSlideItem.offset - mSlideItem.previousOffset) > Math.abs(mSlideItem.maxOffset)
								/ (float) 4;
						if (mSlideItem.offset - mSlideItem.previousOffset > 0) {
							doOpen = distanceGreater;
						} else {
							doOpen = !distanceGreater;
						}
					}
				} else {// right back view is showing
					if (slideMode == SlideMode.RIGHT || slideMode == SlideMode.BOTH) {// SlideMode
																						// support
																						// right
						// the move distance greater than rightBackView's
						// width/4
						boolean distanceGreater = Math.abs(mSlideItem.offset - mSlideItem.previousOffset) > Math.abs(mSlideItem.minOffset)
								/ (float) 4;
						if (mSlideItem.offset - mSlideItem.previousOffset > 0) {
							doOpen = !distanceGreater;
						} else {
							doOpen = distanceGreater;
						}
					}
				}
				autoScroll(mSlideItem.offset, doOpen);
				return true;
			} else {
				if (mSlideListView.isInScrolling()) {
					closeOpenedItem();
				}
			}
		}
			break;
		case MotionEvent.ACTION_CANCEL:
		default:
			mScrollState = SLIDING_STATE_NONE;
			break;
		}
		return false;
	}

	private void slidingFinish() {
		mScrollState = SLIDING_STATE_NONE;
		if (mSlideItem.previousOffset != mSlideItem.offset) {// notify
			if (mSlideItem.previousOffset != 0) {// Previous sliding has open
													// left or right back
													// view.So wo should norify
													// closed
				// if previousOffset between 0 and maxOffset.The left back view
				// is opend or half opend(exception) in previous sliding
				boolean left = mSlideItem.previousOffset > 0 && mSlideItem.previousOffset <= mSlideItem.maxOffset;
				mSlideListView.onClosed(mSlideItem.position, left);
			}
			if (mSlideItem.offset != 0) {// Current sliding has open left or
											// right back view.So wo should
											// norify opend
				boolean left = mSlideItem.offset > 0 && mSlideItem.offset <= mSlideItem.maxOffset;
				mSlideListView.onOpend(mSlideItem.position, left);
			}

			// sdk_version<=2.3.3
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
				mSlideItem.frontView.setAnimation(null);
				if (mSlideItem.leftBackView != null) {
					mSlideItem.leftBackView.setAnimation(null);
				}
				if (mSlideItem.rightBackView != null) {
					mSlideItem.rightBackView.setAnimation(null);
				}
				mSlideItem.child.setOffset(mSlideItem.offset);
				mSlideItem.gingerbread_mr1_Offset = mSlideItem.offset;
			}

		}

		if (mSlideItem.offset != 0) {
			mSlideItem.frontView.setOpend(true);
			mSlideItem.previousOffset = mSlideItem.offset;
			mSlideItem.preDelatX = 0;
		} else {
			mSlideItem.frontView.setOpend(false);
			mSlideItem.child.setLeftBackViewShow(false);
			mSlideItem.child.setRightBackViewShow(false);
			mSlideItem = null;
		}
	}

	private void autoScroll(final int offset, final boolean toOpen) {
		mScrollState = SLIDING_STATE_AUTO;
		int moveTo = 0;
		if (offset < 0) {// right back view is showing
			moveTo = toOpen ? mSlideItem.minOffset : 0;
			// if SlideRightAction==SCROLL,right back view will sliding with
			// front view
			SlideAction rightAction = mSlideListView.getSlideRightAction();
			if (mSlideItem.rightBackView != null && rightAction == SlideAction.SCROLL) {
				animate(mSlideItem.rightBackView).translationX(moveTo).setDuration(getAnimationTime());
			}
		} else {// left back view is showing
			moveTo = toOpen ? mSlideItem.maxOffset : 0;
			// if SlideLeftAction==SCROLL,left back view will sliding with front
			// view
			SlideAction leftAction = mSlideListView.getSlideLeftAction();
			if (mSlideItem.leftBackView != null && leftAction == SlideAction.SCROLL) {
				animate(mSlideItem.leftBackView).translationX(moveTo).setDuration(getAnimationTime());
			}
		}

		animate(mSlideItem.frontView).translationX(moveTo).setDuration(getAnimationTime()).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				// In some extreme cases,the mSlideItem will be null when the
				// animation end.
				// For example,when the item is in sliding or auto sliding,you
				// set a new Adapter to the listview.etc.
				// So,add this judgment to avoid NullPointerException
				if (mSlideItem == null) {
					if (SlideListView.DEUBG) {
						Log.d(SlideListView.TAG, "NullPointerException(onAnimationEnd,mSlideItem has been reset)");
					}
					return;
				}
				if (toOpen) {// to open
					if (offset < 0) {// right back view is opend
						mSlideItem.offset = mSlideItem.minOffset;
					} else {// left back view is opend
						mSlideItem.offset = mSlideItem.maxOffset;
					}
				} else {// to close
					mSlideItem.offset = 0;
				}
				slidingFinish();
			}
		});

	}

	private void move(int offset) {
		setTranslationX(mSlideItem.frontView,  offset - mSlideItem.gingerbread_mr1_Offset);
		if (offset < 0) {// offset less than 0,right back view is showing and
							// left dismiss
			if (mSlideItem.rightBackView != null) {
				mSlideItem.child.setRightBackViewShow(true);
				SlideAction rightAction = mSlideListView.getSlideRightAction();
				if (rightAction == SlideAction.SCROLL) {
					setTranslationX(mSlideItem.rightBackView,  offset - mSlideItem.gingerbread_mr1_Offset);
				}
			}
			if (mSlideItem.leftBackView != null) {
				mSlideItem.child.setLeftBackViewShow(false);
			}
		} else {// offset greater than 0,left back view is showing and right
				// dismiss
			if (mSlideItem.leftBackView != null) {
				mSlideItem.child.setLeftBackViewShow(true);
				SlideAction leftAction = mSlideListView.getSlideLeftAction();
				if (leftAction == SlideAction.SCROLL) {
					setTranslationX(mSlideItem.leftBackView,  offset - mSlideItem.gingerbread_mr1_Offset);
				}
			}
			if (mSlideItem.rightBackView != null) {
				mSlideItem.child.setRightBackViewShow(false);
			}
		}
	}

}
