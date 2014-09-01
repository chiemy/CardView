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
public class CardView extends FrameLayout{
	private static final int ITEM_SPACE = 40;
	private static final int DEF_MAX_VISIBLE = 5;
	
	private int mMaxVisible = DEF_MAX_VISIBLE;
	private int itemSpace = ITEM_SPACE;
	
	private float mTouchSlop;
	private ListAdapter mListAdapter;
	private int mNextAdapterPosition;
	private SparseArray<View> viewHolder = new SparseArray<View>();
	private OnCardClickListener mListener;
	private int topPosition;
	private Rect topRect;
	
	public interface OnCardClickListener{
		void onCardClick(View view,int position);
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
		if (mListAdapter != null){
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
			//添加剩余的View时，始终处在最后
			//View convertView = viewHolder.get(index);
			if(mNextAdapterPosition >= mMaxVisible){
				index = mMaxVisible - 1;
			}
			final View view = mListAdapter.getView(mNextAdapterPosition, null, this);
			//viewHolder.put(index, view);
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			int topMargin = (mMaxVisible - index - 1)*ITEM_SPACE + 20;
			params.setMargins(0, topMargin, 0, 0);
			view.setLayoutParams(params);
			ViewHelper.setScaleX(view, ((mMaxVisible - index - 1)/(float)mMaxVisible)*0.2f + 0.8f);
			//ViewHelper.setTranslationY(view, topMargin);
			ViewHelper.setAlpha(view, mNextAdapterPosition == 0 ? 1 : 0.5f);
			addViewInLayout(view,0, params);
			requestLayout();
			if(mNextAdapterPosition == 0){
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(mListener != null){
							mListener.onCardClick(view, 0);
						}
					}
				});
			}
			mNextAdapterPosition += 1;
		}
	}
	
	
	float downX,downY;
	boolean remove = false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float currentY = event.getY();
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			downX = event.getX();
			downY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float distance = currentY - downY;
			if(distance > mTouchSlop && !remove){
				if(goDown()){
					remove = true;
				}
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			
			break;
		}
		return super.onTouchEvent(event);
	}
	
	/**
	 * 下移所有视图
	 */
	private boolean goDown() {
		final View topView = getChildAt(getChildCount() - 1);
		if(topRect == null){
			topRect = new Rect();
			topView.getHitRect(topRect);
		}
		System.out.println(">>>" + topRect.left + "," + topRect.right + ","
				+ topRect.top + "," + topRect.bottom);
		System.out.println(">>>" + downX + "," + downY);
		if(!topRect.contains((int)downX, (int)downY)){
			return false;
		}
		ViewPropertyAnimator anim = ViewPropertyAnimator.animate(topView)
				.translationY(topView.getTranslationY() + topView.getHeight())
				.alpha(0.3f).scaleX(1)
				.setDuration(200);
		anim.setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				removeView(topView);
				ensureFull();
				final int count = getChildCount();
				for(int i = 0 ; i < count ; i++){
					final View view = getChildAt(i);
					float scaleX = ViewHelper.getScaleX(view) + ((float)1/mMaxVisible)*0.2f;
					float tranlateY = view.getTranslationY() + ITEM_SPACE;
					if(i == count - 1){
						bringToTop(view);
					}else{
						if((count == mMaxVisible && i != 0) || count < mMaxVisible){
							ViewPropertyAnimator.animate(view)
							.translationY(tranlateY)
							.scaleX(scaleX).setDuration(200);
						}
					}
				}
			}
		});
		return true;
	}
	
	private void bringToTop(final View view) {
		float scaleX = ViewHelper.getScaleX(view) + ((float)1/mMaxVisible)*0.2f;
		float tranlateY = view.getTranslationY() + ITEM_SPACE;
		ViewPropertyAnimator.animate(view)
		.translationY(tranlateY)
		.alpha(1).setInterpolator(new AccelerateInterpolator())
		.scaleX(scaleX).setDuration(200)
		.setListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animation) {
				topPosition++;
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(mListener != null){
							mListener.onCardClick(view, topPosition);
						}
					}
				});
				remove = false;
			}
		});
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		float currentY = ev.getY();
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			downY = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float distance = currentY - downY;
			if(distance > mTouchSlop){
				return true;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}
}
