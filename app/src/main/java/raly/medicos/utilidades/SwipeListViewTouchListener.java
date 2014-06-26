package raly.medicos.utilidades;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import raly.medicos.app.R;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

@SuppressLint("Recycle")
public class SwipeListViewTouchListener implements View.OnTouchListener {
	// Cached ViewConfiguration and system-wide constant values
	private int mSlop;
	private int mMinFlingVelocity;
	private int mMaxFlingVelocity;
	private long mAnimationTime;

	// Fixed properties
	private ListView mListView;
	private OnSwipeCallback mCallback;
	private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero
	private boolean dismissLeft = true;
	private boolean dismissRight = true;

	// Transient properties
	private List<PendingSwipeData> mPendingSwipes = new ArrayList<PendingSwipeData>();
	private int mDismissAnimationRefCount = 0;
	private float mDownX;
	private boolean mSwiping;
	private VelocityTracker mVelocityTracker;
	private int mDownPosition;
	private View mDownView;
	private boolean mPaused;

	private boolean onlyLeft = false;
	private boolean onlyRight = false;

	/**
	 * The callback interface used by {@link SwipeListViewTouchListener} to
	 * inform its client about a successful swipe of one or more list item
	 * positions.
	 */
	public interface OnSwipeCallback {
		/**
		 * Called when the user has swiped the list item to the left.
		 * 
		 * @param listView
		 *            The originating {@link ListView}.
		 * @param reverseSortedPositions
		 *            An array of positions to dismiss, sorted in descending
		 *            order for convenience.
		 */
		void onSwipeLeft(ListView listView, int[] reverseSortedPositions);

		void onSwipeRight(ListView listView, int[] reverseSortedPositions);
	}

	/**
	 * Constructs a new swipe-to-action touch listener for the given list view.
	 * 
	 * @param listView
	 *            The list view whose items should be dismissable.
	 * @param callback
	 *            The callback to trigger when the user has indicated that she
	 *            would like to dismiss one or more list items.
	 */
	public SwipeListViewTouchListener(ListView listView, OnSwipeCallback callback) {
		ViewConfiguration vc = ViewConfiguration.get(listView.getContext());
		mSlop = vc.getScaledTouchSlop();
		mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
		mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
		mAnimationTime = listView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
		mListView = listView;
		mCallback = callback;
	}

	/**
	 * Constructs a new swipe-to-action touch listener for the given list view.
	 * 
	 * @param listView
	 *            The list view whose items should be dismissable.
	 * @param callback
	 *            The callback to trigger when the user has indicated that she
	 *            would like to dismiss one or more list items.
	 * @param dismissLeft
	 *            set if the dismiss animation is up when the user swipe to the
	 *            left
	 * @param dismissRight
	 *            set if the dismiss animation is up when the user swipe to the
	 *            right
	 * @see #SwipeListViewTouchListener(ListView, OnSwipeCallback, boolean,
	 *      boolean)
	 */
	public SwipeListViewTouchListener(ListView listView, OnSwipeCallback callback, boolean dismissLeft, boolean dismissRight) {
		this(listView, callback);
		this.dismissLeft = dismissLeft;
		this.dismissRight = dismissRight;
	}

	public SwipeListViewTouchListener(ListView listView, OnSwipeCallback callback, boolean dismissLeft, boolean dismissRight, boolean onlyLeft,
			boolean onlyRight) {
		this(listView, callback, dismissLeft, dismissRight);
		this.onlyLeft = onlyLeft;
		this.onlyRight = onlyRight;
	}

	/**
	 * Enables or disables (pauses or resumes) watching for swipe-to-dismiss
	 * gestures.
	 * 
	 * @param enabled
	 *            Whether or not to watch for gestures.
	 */
	public void setEnabled(boolean enabled) {
		mPaused = !enabled;
	}

	/**
	 * Returns an {@link android.widget.AbsListView.OnScrollListener} to be
	 * added to the {@link ListView} using
	 * {@link ListView#setOnScrollListener(android.widget.AbsListView.OnScrollListener)}
	 * . If a scroll listener is already assigned, the caller should still pass
	 * scroll changes through to this listener. This will ensure that this
	 * {@link SwipeListViewTouchListener} is paused during list view
	 * scrolling.</p>
	 * 
	 * @see {@link SwipeListViewTouchListener}
	 */
	public AbsListView.OnScrollListener makeScrollListener() {
		return new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int scrollState) {
				setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
			}

			@Override
			public void onScroll(AbsListView absListView, int i, int i1, int i2) {
			}
		};
	}

	@SuppressLint("Recycle")
	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (mViewWidth < 2) {
			mViewWidth = mListView.getWidth();
		}

		switch (motionEvent.getActionMasked()) {
		case MotionEvent.ACTION_DOWN: {
			if (mPaused) {
				return false;
			}

			// TODO: ensure this is a finger, and set a flag

			// Find the child view that was touched (perform a hit test)
			Rect rect = new Rect();
			int childCount = mListView.getChildCount();
			int[] listViewCoords = new int[2];
			mListView.getLocationOnScreen(listViewCoords);
			int x = (int) motionEvent.getRawX() - listViewCoords[0];
			int y = (int) motionEvent.getRawY() - listViewCoords[1];
			View child;
			for (int i = 0; i < childCount; i++) {
				child = mListView.getChildAt(i);
				Log.v("suipw", child.isEnabled() ? "True" : "False");
				child.getHitRect(rect);
				if (rect.contains(x, y)) {
					if (child.isEnabled())
						mDownView = child;
					break;
				}
			}

			if (mDownView != null) {
				mDownX = motionEvent.getRawX();
				mDownPosition = mListView.getPositionForView(mDownView);

				mVelocityTracker = VelocityTracker.obtain();
				mVelocityTracker.addMovement(motionEvent);
			}
			view.onTouchEvent(motionEvent);
			return true;
		}

		case MotionEvent.ACTION_UP: {
			if (mVelocityTracker == null) {
				break;
			}

			float deltaX = motionEvent.getRawX() - mDownX;
			mVelocityTracker.addMovement(motionEvent);
			mVelocityTracker.computeCurrentVelocity(500); // 1000 by defaut but
															// it was too much
			float velocityX = Math.abs(mVelocityTracker.getXVelocity());
			float velocityY = Math.abs(mVelocityTracker.getYVelocity());
			boolean swipe = false;
			boolean swipeRight = false;

			if (Math.abs(deltaX) > mViewWidth / 2) {
				swipe = true;
				swipeRight = deltaX > 0;
			} else if (mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity && velocityY < velocityX) {
				swipe = true;
				swipeRight = mVelocityTracker.getXVelocity() > 0;
			}
			if (swipe) {
				// sufficent swipe value
				final View downView = mDownView; // mDownView gets null'd before
													// animation ends
				if (mDownView.isEnabled()) {
					final int downPosition = mDownPosition;
					final boolean toTheRight = swipeRight;
					++mDismissAnimationRefCount;
					restoreViews(mDownView);
					animate(mDownView).translationX(swipeRight ? (this.onlyRight ? mViewWidth : 0) : (this.onlyLeft ? -mViewWidth : 0))
							.alpha(swipeRight ? (this.onlyRight ? 0 : 1f) : (this.onlyLeft ? 0 : 1f)).setDuration(mAnimationTime)
							.setListener(new AnimatorListenerAdapter() {
								@Override
								public void onAnimationEnd(Animator animation) {
									performSwipeAction(downView, downPosition, toTheRight, toTheRight ? dismissRight : dismissLeft);
								}
							});
				}
			} else {
				// cancel
				restoreViews(mDownView);
				animate(mDownView).translationX(0).alpha(1).setDuration(mAnimationTime).setListener(null);
			}
			mVelocityTracker = null;
			mDownX = 0;
			mDownView = null;
			mDownPosition = ListView.INVALID_POSITION;
			mSwiping = false;
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			if (mVelocityTracker == null || mPaused) {
				break;
			}
			boolean swipeRight = false;
			mVelocityTracker.addMovement(motionEvent);
			float deltaX = motionEvent.getRawX() - mDownX;
			if (Math.abs(deltaX) > mSlop) {
				mSwiping = true;
				swipeRight = deltaX > 0;
				mListView.requestDisallowInterceptTouchEvent(true);

				// Cancel ListView's touch (un-highlighting the item)
				MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
				cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
				mListView.onTouchEvent(cancelEvent);
			}

			if (mSwiping) {
				if (mDownView.isEnabled()) {
					getViewVisible(mDownView, swipeRight);
					setTranslationX(mDownView, swipeRight ? (this.onlyRight ? deltaX : 0) : (this.onlyLeft ? deltaX : 0));
					setAlpha(mDownView, swipeRight ? (this.onlyRight ? Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)) : 1f)
							: (this.onlyLeft ? Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)) : 1f));
					return true;
				}
			}
			break;
		}
		}
		return false;
	}

	private void getViewVisible(View v, boolean m) {
		LinearLayout.LayoutParams params = null;
		View l = v.findViewById(R.id.swipe_left), i = v.findViewById(R.id.swipe_item);
		if (m) {
			if (l != null) {
				if (i != null) {
					i.setVisibility(View.GONE);
					params = new LinearLayout.LayoutParams(i.getWidth(), i.getHeight());
				}
				if (params != null)
					l.setLayoutParams(params);
				l.setVisibility(View.VISIBLE);
			}
		}

	}

	private void restoreViews(View v) {
		View l = v.findViewById(R.id.swipe_left), i = v.findViewById(R.id.swipe_item);
		if (l != null) {
			l.setVisibility(View.GONE);
			if (i != null)
				i.setVisibility(View.VISIBLE);
		}
	}

	class PendingSwipeData implements Comparable<PendingSwipeData> {
		public int position;
		public View view;

		public PendingSwipeData(int position, View view) {
			this.position = position;
			this.view = view;
		}

		@Override
		public int compareTo(PendingSwipeData other) {
			// Sort by descending position
			return other.position - position;
		}
	}

	private void performSwipeAction(final View swipeView, final int swipePosition, boolean toTheRight, boolean dismiss) {
		// Animate the dismissed list item to zero-height and fire the dismiss
		// callback when
		// all dismissed list item animations have completed. This triggers
		// layout on each animation
		// frame; in the future we may want to do something smarter and more
		// performant.

		@SuppressWarnings("unused")
		final ViewGroup.LayoutParams lp = swipeView.getLayoutParams();
		final int originalHeight = swipeView.getHeight();
		final boolean swipeRight = toTheRight;
		swipeView.getHeight();
		final SwipeListViewTouchListener self = this;
		ValueAnimator animator;
		if (dismiss)
			animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);
		else
			animator = ValueAnimator.ofInt(originalHeight, originalHeight - 1).setDuration(mAnimationTime);

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				--mDismissAnimationRefCount;
				if (mDismissAnimationRefCount == 0) {
					// No active animations, process all pending dismisses.
					// Sort by descending position
					Collections.sort(mPendingSwipes);

					int[] swipePositions = new int[mPendingSwipes.size()];
					for (int i = mPendingSwipes.size() - 1; i >= 0; i--) {
						swipePositions[i] = mPendingSwipes.get(i).position;
					}
					if (swipeRight)
						if (self.onlyRight)
							mCallback.onSwipeRight(mListView, swipePositions);
						else if (self.onlyLeft)
							mCallback.onSwipeLeft(mListView, swipePositions);

					@SuppressWarnings("unused")
					ViewGroup.LayoutParams lp;
					for (PendingSwipeData pendingDismiss : mPendingSwipes) {
						// Reset view presentation
						restoreViews(pendingDismiss.view);
						setAlpha(pendingDismiss.view, 1f);
						setTranslationX(pendingDismiss.view, 0);
						/*
						 * lp = pendingDismiss.view.getLayoutParams(); lp.height
						 * = originalHeight;
						 * pendingDismiss.view.setLayoutParams(
						 * swipeView.getLayoutParams());
						 */
					}

					mPendingSwipes.clear();
				}
			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				/*
				 * lp.height = (Integer) valueAnimator.getAnimatedValue();
				 * swipeView.setLayoutParams(lp);
				 */
			}
		});

		mPendingSwipes.add(new PendingSwipeData(swipePosition, swipeView));
		animator.start();
	}
}