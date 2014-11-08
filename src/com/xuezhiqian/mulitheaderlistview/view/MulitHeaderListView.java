package com.xuezhiqian.mulitheaderlistview.view;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xuezhiqian.mulitheaderlistview.R;
import com.xuezhiqian.mulitheaderlistview.view.interf.OnPullRefreashingListener;

public class MulitHeaderListView extends ListView implements OnScrollListener,
		OnClickListener {
	private LinearLayout ll_header_root;
	private LinearLayout ll_inner_header;
	private ProgressBar pb_loading;
	private ImageView iv_arro;
	private TextView tv_date;
	private TextView tv_tips;
	private int innerHeaderHeight;
	private int firstVisibleItem;

	private static final int REFEASHING = 0;
	private static final int PULL = 1;
	private static final int RELEASE = 2;

	private int state = PULL;
	private OnPullRefreashingListener pullRefreashingListener;
	public boolean isAddHeader = true;
	public boolean isAddFooter = true;

	public void setPullRefreashingListener(
			OnPullRefreashingListener pullRefreashingListener) {
		this.pullRefreashingListener = pullRefreashingListener;
	}

	// ����xml�ļ���д����
	public MulitHeaderListView(Context context) {
		super(context);
		initHeader();
		initFooter();
		setOnScrollListener(this);
	}

	// ��xml�ļ���д����
	public MulitHeaderListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initFooter();
		initHeader();
		setOnScrollListener(this);
	}

	private void initHeader() {
		// ���ز���
		View mulitheader = View.inflate(getContext(),
				R.layout.mulitheader_item, null);
		// ��Ԫ��
		ll_header_root = (LinearLayout) mulitheader
				.findViewById(R.id.ll_header_root);
		// ��Ҫ���ص�ͷ
		ll_inner_header = (LinearLayout) mulitheader
				.findViewById(R.id.ll_inner_header);
		pb_loading = (ProgressBar) mulitheader.findViewById(R.id.pb_loading);
		pb_loading.setVisibility(View.INVISIBLE);
		iv_arro = (ImageView) mulitheader.findViewById(R.id.iv_arro);
		tv_date = (TextView) mulitheader.findViewById(R.id.tv_date);
		tv_tips = (TextView) mulitheader.findViewById(R.id.tv_tips);
		// ʱ������
		tv_date.setVisibility(View.INVISIBLE);
		// ����
		ll_inner_header.measure(0, 0);
		// �ó���Ҫ���ص��ڲ�ͷ�ĸ�
		innerHeaderHeight = ll_inner_header.getMeasuredHeight();
		// �����ڲ�͸
		ll_inner_header.setPadding(0, -innerHeaderHeight, 0, 0);
		// ��ͷ
		addHeaderView(mulitheader);
		initAnimation();
	}

	private void initFooter() {
		footer = View.inflate(getContext(), R.layout.footer_item, null);
		footer.measure(0, 0);
		footerHight = footer.getMeasuredHeight();
		footer.setPadding(0, -footerHight, 0, 0);
		footer.setOnClickListener(this);
		addFooterView(footer);
	}

	private void initAnimation() {
		pullAnimation = new RotateAnimation(-180, -360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		pullAnimation.setDuration(500);
		pullAnimation.setFillAfter(true);
		releaseAnimation = new RotateAnimation(0, -180,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		releaseAnimation.setDuration(500);
		releaseAnimation.setFillAfter(true);
	}

	private View customHeader;

	public void addCustomHeader(View v) {
		customHeader = v;
		ll_header_root.addView(v);
	}

	private int downY = -1;
	private int innerHeaderPadding;
	private RotateAnimation pullAnimation;
	private RotateAnimation releaseAnimation;
	private int footerHight;
	private View footer;

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (downY == -1) {
				downY = (int) ev.getY();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (!isAddHeader) {
				break;
			}
			if (downY == -1) {
				downY = (int) ev.getY();
			}
			if (firstVisibleItem != 0) {
				break;
			}
			int[] customHeadrAtScreenLocation = new int[2];
			customHeader.getLocationInWindow(customHeadrAtScreenLocation);
			int customHeaderAtScreenY = customHeadrAtScreenLocation[1];
			int[] mYmulitListViewAtScreenLocation = new int[2];
			this.getLocationInWindow(mYmulitListViewAtScreenLocation);
			int mYmulitListViewArScreenY = mYmulitListViewAtScreenLocation[1];
			if (mYmulitListViewArScreenY > customHeaderAtScreenY) {
				break;
			}
			int moveY = (int) ev.getY();
			int disY = moveY - downY;
			innerHeaderPadding = disY - innerHeaderHeight;
			if (innerHeaderPadding > -innerHeaderHeight) {
				if (state != REFEASHING) {
					if (state == PULL && innerHeaderPadding > 0) {
						state = RELEASE;
						iv_arro.startAnimation(releaseAnimation);
						tv_tips.setText("�ͷ�ˢ��");
					} else if (state == RELEASE && innerHeaderPadding < 0) {
						state = PULL;
						iv_arro.startAnimation(pullAnimation);
						tv_tips.setText("����ˢ��");
					}
					ll_inner_header.setPadding(0, innerHeaderPadding, 0, 0);
					// ����listViewȥ�������¼�
					return true;
				}
			}
			break;
		case MotionEvent.ACTION_UP:

			downY = -1;
			if (state == PULL) {
				ll_inner_header.setPadding(0, -innerHeaderHeight, 0, 0);
			} else if (state == RELEASE) {
				state = REFEASHING;
				ll_inner_header.setPadding(0, 0, 0, 0);
				iv_arro.clearAnimation();
				iv_arro.setVisibility(View.INVISIBLE);
				pb_loading.setVisibility(View.VISIBLE);
				tv_date.setVisibility(View.VISIBLE);
				tv_tips.setText("���ڼ���");
				tv_date.setText("���ˢ��ʱ����" + getCurrentTime());
				if (pullRefreashingListener != null) {
					pullRefreashingListener.onPullRefreash();
				}
			}
			int[] getCustomHeadrAtScreenLocationAgain = new int[2];
			customHeader
					.getLocationInWindow(getCustomHeadrAtScreenLocationAgain);
			int getCustomHeaderAtScreenYAgain = getCustomHeadrAtScreenLocationAgain[1];
			int[] getMYmulitListViewAtScreenLocationAgain = new int[2];
			this.getLocationInWindow(getMYmulitListViewAtScreenLocationAgain);
			int getMYmulitListViewArScreenYAgain = getMYmulitListViewAtScreenLocationAgain[1];
			if (getMYmulitListViewArScreenYAgain != getCustomHeaderAtScreenYAgain) {
				// �жϵڶ���ͷ����Ļ�еĸ߶��Ƿ��List����Ļ�еĸ߶���ͬ
				// ����Ⱦ�˵����Ҫ�����µ��¼��Լ����ѵ�.������ֽ�������������
				return true;
			}
			break;
		default:

			break;
		}
		return super.onTouchEvent(ev);
	}

	private String getCurrentTime() {
		long currentTimeMillis = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(currentTimeMillis);
		return time;
	}

	private boolean isAddMore = false;

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (!isAddFooter) {
			return;
		}
		if (!isAddMore) {
			if ((scrollState == OnScrollListener.SCROLL_STATE_FLING || scrollState == OnScrollListener.SCROLL_STATE_IDLE)
					&& getLastVisiblePosition() == getCount() - 1) {
				isAddMore = true;
				footer.setPadding(0, 0, 0, 0);
				setSelection(getCount());
				if (pullRefreashingListener != null) {
					pullRefreashingListener.onAddMore();
				}

			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.firstVisibleItem = firstVisibleItem;
	}

	public void finishLoading() {
		if (state == REFEASHING) {
			state = PULL;
			ll_inner_header.setPadding(0, -innerHeaderHeight, 0, 0);
			pb_loading.setVisibility(View.INVISIBLE);
			iv_arro.setVisibility(View.VISIBLE);
			tv_date.setText("����޸�ʱ��:" + getCurrentTime());
			tv_date.setVisibility(View.INVISIBLE);
		} else if (isAddMore) {
			isAddMore = false;
			footer.setPadding(0, -footerHight, 0, 0);
		}
	}

	@Override
	public void onClick(View v) {
		Toast.makeText(getContext(), "��ȴ�", Toast.LENGTH_SHORT).show();

	}
}
