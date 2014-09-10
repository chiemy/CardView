package com.chiemy.cardview.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * @author chiemy
 * 
 */
public class CardView extends FrameLayout {
	private static final int ITEM_SPACE = 40;
	private static final int DEF_MAX_VISIBLE = 4;

	private int mMaxVisible = DEF_MAX_VISIBLE;
	private int itemSpace = ITEM_SPACE;

	private float mTouchSlop;
	private ListAdapter mListAdapter;
	private int mNextAdapterPosition;
	private SparseArray<View> viewHolder = new SparseArray<View>();
	private OnCardClickListener mListener;
	private int topPosition;
	private Rect topRect;

	public interface OnCardClickListener {
		void onCardClick(View view, int position);
	}

	public CardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public CardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CardView(Context context) {
		super(context);
		init();
	}

	private void init() {
		topRect = new Rect();
		ViewConfiguration con = ViewConfiguration.get(getContext());
		mTouchSlop = con.getScaledTouchSlop();
	}

	public void setMaxVisibleCount(int count) {
		mMaxVisible = count;
	}

	public int getMaxVisibleCount() {
		return mMaxVisible;
	}

	public void setItemSpace(int itemSpace) {
		this.itemSpace = itemSpace;
	}

	public int getItemSpace() {
		return itemSpace;
	}

	public ListAdapter getAdapter() {
		return mListAdapter;
	}

	public void setAdapter(ListAdapter adapter) {
		if (mListAdapter != null) {
			mListAdapter.unregisterDataSetObserver(mDataSetObserver);
		}
		mNextAdapterPosition = 0;
		mListAdapter = adapter;
		adapter.registerDataSetObserver(mDataSetObserver);
		removeAllViews();
		ensureFull();
	}

	public void setOnCardClickListener(OnCardClickListener listener) {
		mListener = listener;
	}

	private void ensureFull() {
		while (mNextAdapterPosition < mListAdapter.getCount()
				&& getChildCount() < mMaxVisible) {
			int index = mNextAdapterPosition % mMaxVisible;
			View convertView = viewHolder.get(index);
			final View view = mListAdapter.getView(mNextAdapterPosition,
					convertView, this);
			view.setOnClickListener(null);
			viewHolder.put(index, view);

			// 添加剩余的View时，始终处在最后
			index = Math.min(mNextAdapterPosition, mMaxVisible - 1);
			ViewHelper.setScaleX(view,((mMaxVisible - index - 1) / (float) mMaxVisible) * 0.2f + 0.8f);
			int topMargin = (mMaxVisible - index - 1) * itemSpace;
			ViewHelper.setTranslationY(view, topMargin);
			ViewHelper.setAlpha(view, mNextAdapterPosition == 0 ? 1 : 0.5f);

			LayoutParams params = (LayoutParams) view.getLayoutParams();
			if (params == null) {
				params = new LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT);
			}
			addViewInLayout(view, 0, params);

			mNextAdapterPosition += 1;
		}
		// requestLayout();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int childCount = getChildCount();
		int maxHeight = 0;
		int maxWidth = 0;
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			this.measureChild(child, widthMeasureSpec, heightMeasureSpec);
			int height = child.getMeasuredHeight();
			int width = child.getMeasuredWidth();
			if (height > maxHeight) {
				maxHeight = height;
			}
			if (width > maxWidth) {
				maxWidth = width;
			}
		}
		int desireWidth = widthSize;
		int desireHeight = heightSize;
		if (widthMode == MeasureSpec.AT_MOST) {
			desireWidth = maxWidth + getPaddingLeft() + getPaddingRight();
		}
		if (heightMode == MeasureSpec.AT_MOST) {
			desireHeight = maxHeight + (mMaxVisible - 1) * itemSpace + getPaddingTop() + getPaddingBottom();
		}
		setMeasuredDimension(desireWidth, desireHeight);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		View topView = getChildAt(getChildCount() - 1);
		if (topView != null) {
			topView.setOnClickListener(listener);
		}
	}

	float downX, downY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (goDown()) {
				downY = -1;
			}
			break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 下移所有视图
	 */
	private boolean goDown() {
		final View topView = getChildAt(getChildCount() - 1);
		if(!topView.isClickable()){
			return false;
		}
		// topView.getHitRect(topRect); 在4.3以前有bug，用以下方法代替
		topRect = getHitRect(topRect, topView);
		// 如果按下的位置不在顶部视图上，则不移动
		if (!topRect.contains((int) downX, (int) downY)) {
			return false;
		}
		topView.setClickable(false);
		ViewPropertyAnimator anim = ViewPropertyAnimator
				.animate(topView)
				.translationY(
						ViewHelper.getTranslationY(topView)
								+ topView.getHeight()).alpha(0).scaleX(1)
				.setListener(null).setDuration(200);
		anim.setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				topView.setClickable(true);
				removeView(topView);
				ensureFull();
				final int count = getChildCount();
				for (int i = 0; i < count; i++) {
					final View view = getChildAt(i);
					float scaleX = ViewHelper.getScaleX(view)
							+ ((float) 1 / mMaxVisible) * 0.2f;
					float tranlateY = ViewHelper.getTranslationY(view)
							+ itemSpace;
					if (i == count - 1) {
						bringToTop(view);
					} else {
						if ((count == mMaxVisible && i != 0)
								|| count < mMaxVisible) {
							ViewPropertyAnimator
									.animate(view)
									.translationY(tranlateY)
									.setInterpolator(
											new AccelerateInterpolator())
									.setListener(null).scaleX(scaleX)
									.setDuration(200);
						}
					}
				}
			}
		});
		return true;
	}

	/**
	 * 将下一个视图移到前边
	 * 
	 * @param view
	 */
	private void bringToTop(final View view) {
		topPosition++;
		float scaleX = ViewHelper.getScaleX(view) + ((float) 1 / mMaxVisible)
				* 0.2f;
		float tranlateY = ViewHelper.getTranslationY(view) + itemSpace;
		ViewPropertyAnimator.animate(view).translationY(tranlateY)
				.scaleX(scaleX).setDuration(200).alpha(1)
				.setInterpolator(new AccelerateInterpolator())
				.setListener(null).setListener(new AnimatorListenerAdapter() {
					public void onAnimationEnd(Animator animation) {
						
					}
				});
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		float currentY = ev.getY();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX = ev.getX();
			downY = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float distance = currentY - downY;
			if (distance > mTouchSlop) {
				return true;
			}
			break;
		}
		return false;
	}

	public static Rect getHitRect(Rect rect, View child) {
		rect.left = child.getLeft();
		rect.right = child.getRight();
		rect.top = (int) (child.getTop() + ViewHelper.getTranslationY(child));
		rect.bottom = (int) (child.getBottom() + ViewHelper
				.getTranslationY(child));
		return rect;
	}

	private final DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			super.onChanged();
		}

		@Override
		public void onInvalidated() {
			super.onInvalidated();
		}
	};

	private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mListener != null) {
				mListener.onCardClick(v, topPosition);
			}
		}
	};
}
