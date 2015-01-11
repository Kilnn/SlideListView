package com.example.slidelistviewsample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.roamer.slidelistview.SlideListView;
import com.roamer.slidelistview.SlideListView.SlideAction;
import com.roamer.slidelistview.SlideListView.SlideMode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private SlideListView mSlideListView;
	private SlideAdapter mAdapter;
	private ArrayList<String> mTestData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSlideListView = ((SlideListView) findViewById(R.id.list_view));
		mTestData = new ArrayList<String>();
		randomCreateTestData();
		mAdapter = new SlideAdapter(this, mTestData);

		TextView headView1 = new TextView(this);
		headView1.setHeight(60);
		TextView headView2 = new TextView(this);
		headView2.setHeight(60);
		TextView footerView1 = new TextView(this);
		footerView1.setHeight(60);
		headView1.setText("这是HeaderView1");
		headView2.setText("这是HeaderView2");
		footerView1.setText("这是FooterView1");
		mSlideListView.addHeaderView(headView1);
		mSlideListView.addHeaderView(headView2);
		mSlideListView.addFooterView(footerView1);

		mSlideListView.setAdapter(mAdapter);

		mSlideListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String s = (String) parent.getAdapter().getItem(position);
				if (!TextUtils.isEmpty(s)) {
					Toast.makeText(MainActivity.this, "OnItemClick:" + s, Toast.LENGTH_SHORT).show();
				}
			}
		});

		mSlideListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_IDLE) {
					Log.d("roamer", "no");
				} else {
					Log.d("roamer", "scroll");
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});

	}

	Random random = new Random();

	private void randomCreateTestData() {
		mTestData.clear();
		int length = random.nextInt(100);
		for (int i = 0; i < length; i++) {
			mTestData.add(getRandomString());
		}
	}

	private String getRandomString() {
		int length = random.nextInt(20);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append((char) (random.nextInt(26) + 97));
		}
		return sb.toString();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		// SlideMode
		MenuItem item = menu.findItem(R.id.menu_slide_mode);
		item.setTitle("切换SlideMode:" + mSlideListView.getSlideMode().toString());
		// SlideLeftAction
		item = menu.findItem(R.id.menu_slide_left_action);
		item.setTitle("切换SlideLeftAction:" + mSlideListView.getSlideLeftAction().toString());
		// SlideRightAction
		item = menu.findItem(R.id.menu_slide_right_action);
		item.setTitle("切换SlideRightAction:" + mSlideListView.getSlideRightAction().toString());
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.menu_slide_mode) {
			changeSlideMode(item);
		} else if (id == R.id.menu_slide_left_action) {
			changeSlideLeftAction(item);
		} else if (id == R.id.menu_slide_right_action) {
			changeSlideRightAction(item);
		} else if (id == R.id.menu_data_change) {
			randomCreateTestData();
			mAdapter.notifyDataSetChanged();
		} else if (id == R.id.menu_adapter_change) {
			randomCreateTestData();
			mAdapter = new SlideAdapter(this, mTestData);
			mSlideListView.setAdapter(mAdapter);
		}
		return super.onOptionsItemSelected(item);
	}

	private void changeSlideMode(MenuItem item) {
		if (mSlideListView.getSlideMode() == SlideMode.BOTH) {
			mSlideListView.setSlideMode(SlideMode.LEFT);
		} else if (mSlideListView.getSlideMode() == SlideMode.LEFT) {
			mSlideListView.setSlideMode(SlideMode.RIGHT);
		} else if (mSlideListView.getSlideMode() == SlideMode.RIGHT) {
			mSlideListView.setSlideMode(SlideMode.NONE);
		} else if (mSlideListView.getSlideMode() == SlideMode.NONE) {
			mSlideListView.setSlideMode(SlideMode.BOTH);
		}
		if (item != null) {
			item.setTitle("切换SlideMode:" + mSlideListView.getSlideMode().toString());
		}
		Toast.makeText(this, "切换SlideMode:" + mSlideListView.getSlideMode().toString(), Toast.LENGTH_SHORT).show();
	}

	private void changeSlideLeftAction(MenuItem item) {
		if (mSlideListView.getSlideLeftAction() == SlideAction.SCROLL) {
			mSlideListView.setSlideLeftAction(SlideAction.REVEAL);
		} else {
			mSlideListView.setSlideLeftAction(SlideAction.SCROLL);
		}
		if (item != null) {
			item.setTitle("切换SlideLeftAction:" + mSlideListView.getSlideLeftAction().toString());
		}
		Toast.makeText(this, "切换SlideLeftAction:" + mSlideListView.getSlideLeftAction().toString(), Toast.LENGTH_SHORT).show();
	}

	private void changeSlideRightAction(MenuItem item) {
		if (mSlideListView.getSlideRightAction() == SlideAction.SCROLL) {
			mSlideListView.setSlideRightAction(SlideAction.REVEAL);
		} else {
			mSlideListView.setSlideRightAction(SlideAction.SCROLL);
		}
		if (item != null) {
			item.setTitle("切换SlideRightAction:" + mSlideListView.getSlideRightAction().toString());
		}
		Toast.makeText(this, "切换SlideRightAction:" + mSlideListView.getSlideRightAction().toString(), Toast.LENGTH_SHORT).show();
	}
}
