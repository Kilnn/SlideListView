package com.roamer.slidelistview;

/**
 * Listener to get callback notifications for the SlideListView
 */
public interface SlideItemListener {
	/**
	 * 
	 * @param position
	 * @param left
	 *            opend left back view or right back view.if true,opend left
	 *            back view,else right
	 */
	void onOpend(int position, boolean left);

	/**
	 * 
	 * @param position
	 * @param left
	 *            opend left back view or right back view.if true,opend left
	 *            back view,else right
	 */
	void onClosed(int position, boolean left);
}
