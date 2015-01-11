package com.example.slidelistviewsample;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.roamer.slidelistview.SlideBaseAdapter;
import com.roamer.slidelistview.SlideListView.SlideMode;

public class SlideAdapter extends SlideBaseAdapter {
	private ArrayList<String> mData;

	public SlideAdapter(Context context, ArrayList<String> data) {
		super(context);
		mData = data;
	}

	
	@Override
	public SlideMode getSlideModeInPosition(int position) {
		if (position == 1) {
			return SlideMode.LEFT;
		}
		if (position == 2) {
			return SlideMode.NONE;
		}
		return super.getSlideModeInPosition(position);
	}

	@Override
	public int getFrontViewId(int position) {
		return R.layout.row_front_view;
	}

	@Override
	public int getLeftBackViewId(int position) {
		if (position % 2 == 0) {
			return R.layout.row_left_back_view;
		}
		return R.layout.row_right_back_view;
	}

	@Override
	public int getRightBackViewId(int position) {
		return R.layout.row_right_back_view;
	}

	@Override
	public int getItemViewType(int position) {
		if (position % 2 == 0) {
			return 0;
		}
		return 1;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = createConvertView(position);
			holder = new ViewHolder();
			holder.title = (Button) convertView.findViewById(R.id.title);
			holder.edit = (Button) convertView.findViewById(R.id.edit);
			holder.delete = (Button) convertView.findViewById(R.id.delete);
			holder.detail = (Button) convertView.findViewById(R.id.detail);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		String text = mData.get(position);
		holder.title.setText(text);
		holder.title.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(mContext, "Click title:" + position, Toast.LENGTH_SHORT).show();
			}
		});

		if (holder.edit != null) {
			holder.edit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(mContext, "Click edit:" + position, Toast.LENGTH_SHORT).show();
				}
			});
		}

		if (holder.delete != null) {
			holder.delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mData.remove(position);
					notifyDataSetChanged();
					Toast.makeText(mContext, "Click delete:" + position, Toast.LENGTH_SHORT).show();
				}
			});
		}

		if (holder.detail != null) {
			holder.detail.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(mContext, "Click detail:" + position, Toast.LENGTH_SHORT).show();
				}
			});
		}
		return convertView;
	}

	class ViewHolder {
		Button title;
		Button edit;
		Button delete;
		Button detail;
	}

}