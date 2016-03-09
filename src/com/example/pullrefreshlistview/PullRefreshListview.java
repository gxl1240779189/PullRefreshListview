package com.example.pullrefreshlistview;

import java.util.Date;
import java.util.zip.Inflater;

import javax.crypto.spec.IvParameterSpec;

import android.content.Context;
import android.provider.SyncStateContract.Constants;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PullRefreshListview extends ListView implements OnScrollListener {

	LayoutInflater inflater;

	/*
	 * 设置布局变量参数
	 */
	int headerContentHeight;// 上拉显示框的宽度
	int state; // 当前状态
	final int REFRESHING = 2; // 正在刷新的状态值
	final int DONE = 3; // 表示加载完成
	int RATIO = 3;
	int LOADING = 4;
	public final int RELEASE_To_REFRESH = 0; // 下拉过程的状态值
	public final int PULL_To_REFRESH = 1; // 从下拉返回到不刷新的状态值
	int startY; // 记录一下上一次记录的y坐标
	boolean isBack;
	boolean isRecored;
	OnRefreshListener listener; // 设置刷新的接口
	boolean isRefreshable = true; // 当前是够能够刷新
	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;

	public void setListener(OnRefreshListener listener) {
		this.listener = listener;
	}

	/*
	 * Listview头部下拉刷新的布局
	 */
	private LinearLayout headerView;
	private TextView lvHeaderTipsTv;
	private TextView lvHeaderLastUpdatedTv;
	private ImageView lvHeaderArrowlv;
	private ProgressBar lvHeaderProgressBar;

	/*
	 * 定义头部下拉刷新的布局高度+++
	 */

	public PullRefreshListview(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		setCacheColorHint(getResources().getColor(R.color.red));
		inflater = LayoutInflater.from(context);
		headerView = (LinearLayout) inflater.inflate(R.layout.lv_header, null);
		lvHeaderTipsTv = (TextView) headerView
				.findViewById(R.id.lvHeaderTipsTv);
		lvHeaderLastUpdatedTv = (TextView) headerView
				.findViewById(R.id.lvHeaderLastUpdatedTv);
		lvHeaderArrowlv = (ImageView) headerView
				.findViewById(R.id.ivHeaderArrowlv);
		lvHeaderArrowlv.setMinimumHeight(70);
		lvHeaderArrowlv.setMinimumWidth(50);
		lvHeaderProgressBar = (ProgressBar) headerView
				.findViewById(R.id.ivHeaderProgressBar);
		measureView(headerView);
		headerContentHeight = headerView.getMeasuredHeight();
		headerView.setPadding(0, -1 * headerContentHeight, 0, 0);
		headerView.invalidate();
		addHeaderView(headerView);
		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(250);
		reverseAnimation.setFillAfter(true);
		setOnScrollListener(this);
		state = DONE;
		isRefreshable = false;

	}

	/**
	 * 
	 * @param child
	 *            该函数用来估计child的width和height
	 */
	private void measureView(View child) {
		ViewGroup.LayoutParams params = child.getLayoutParams();
		if (params == null) {
			params = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);

		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, params.width);
		int lpHeight = params.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);

	}

	public PullRefreshListview(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	public PullRefreshListview(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public PullRefreshListview(Context context) {
		super(context);
		init(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isRefreshable) {
			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (!isRecored) {
					isRecored = true;
					startY = (int) ev.getY();
					Log.i("startY", startY+"");
				}
				break;

			case MotionEvent.ACTION_UP:
				if (state != LOADING && state != REFRESHING) {
					if (state == PULL_To_REFRESH) {
						state = DONE;
						changeHeaderViewByState();
					}
					if (state == RELEASE_To_REFRESH) {
						state = REFRESHING;
						changeHeaderViewByState();
						onLvRefresh();
					}
				}
				isRecored = false;
				isBack = false;
				break;

			case MotionEvent.ACTION_MOVE:
				int tempY = (int) ev.getY();
				if (!isRecored) {
					isRecored = true;
					startY = tempY;
				}
				if (state != LOADING && state != REFRESHING) {
					if (state == RELEASE_To_REFRESH) {
						setSelection(0);
						if (((tempY - startY) / RATIO < headerContentHeight)
								&& (tempY - startY) > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();
						} else if ((tempY - startY) <= 0) {
							state = DONE;
							changeHeaderViewByState();
						}
					}
					if(state==PULL_To_REFRESH)
					{
						setSelection(0);
						if((tempY-startY)/RATIO>=headerContentHeight)
						{
							state=RELEASE_To_REFRESH;
							isBack=true;
							changeHeaderViewByState();
						}else if(tempY-startY<=0)
						{
							state=DONE;
							changeHeaderViewByState();
						}
					}
					
					// done状态下
					if (state == DONE) {
						if (tempY - startY > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();
						}
					}
					// 更新headView的size
					if (state == PULL_To_REFRESH) {
						headerView.setPadding(0, -1 * headerContentHeight
								+ (tempY - startY) / RATIO, 0, 0);

					}
					// 更新headView的paddingTop
					if (state == RELEASE_To_REFRESH) {
						headerView.setPadding(0, (tempY - startY) / RATIO
								- headerContentHeight, 0, 0);
					}				}

				break;
			}
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem == 0)
			isRefreshable = true;
		else
			isRefreshable = false;

	}

	interface OnRefreshListener {
		void onRefresh();
	}

	public void onRefreshComplete() {
		state = DONE;
		lvHeaderLastUpdatedTv.setText("最新更新:" + new Date().toLocaleString());
		headerView.setPadding(0, -1 * headerContentHeight, 0, 0);
	}

	public void onLvRefresh() {
		if (listener != null)
			listener.onRefresh();
	}

	public void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_To_REFRESH: // 表示当前的手势是下拉但是还没有释放，当前手还放在屏幕上
			lvHeaderArrowlv.setVisibility(View.VISIBLE);
			lvHeaderProgressBar.setVisibility(View.GONE);
			lvHeaderTipsTv.setVisibility(View.VISIBLE);
			lvHeaderLastUpdatedTv.setVisibility(View.VISIBLE);
			lvHeaderArrowlv.clearAnimation();
			lvHeaderArrowlv.startAnimation(animation);
			lvHeaderTipsTv.setText("松开刷新");
			break;

		case PULL_To_REFRESH:
			lvHeaderProgressBar.setVisibility(View.GONE);
			lvHeaderTipsTv.setVisibility(View.VISIBLE);
			lvHeaderLastUpdatedTv.setVisibility(View.VISIBLE);
			lvHeaderArrowlv.clearAnimation();
			lvHeaderArrowlv.setVisibility(View.VISIBLE);
			if (isBack) {// 这里表示该状态是从RELEASE_REFRESH状态这个状态
				isBack = false;
				lvHeaderArrowlv.clearAnimation();
				lvHeaderArrowlv.startAnimation(reverseAnimation);
				lvHeaderTipsTv.setText("下拉刷新");
			} else {
				lvHeaderTipsTv.setText("下拉刷新");
			}
			break;

		case REFRESHING:
			headerView.setPadding(0, 0, 0, 0);
			lvHeaderProgressBar.setVisibility(View.VISIBLE);
			lvHeaderArrowlv.clearAnimation();
			lvHeaderArrowlv.setVisibility(View.GONE);
			lvHeaderTipsTv.setText("正在刷新...");
			lvHeaderLastUpdatedTv.setVisibility(View.VISIBLE);
			break;

		case DONE:
			headerView.setPadding(0, -1 * headerContentHeight, 0, 0);
			lvHeaderProgressBar.setVisibility(View.GONE);
			lvHeaderArrowlv.clearAnimation();
			lvHeaderArrowlv.setImageResource(R.drawable.arrow);
			lvHeaderTipsTv.setText("下拉刷新");
			lvHeaderLastUpdatedTv.setVisibility(View.VISIBLE);
			break;

		}
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// TODO Auto-generated method stub
		onRefreshComplete();
		super.setAdapter(adapter);
	}

}
