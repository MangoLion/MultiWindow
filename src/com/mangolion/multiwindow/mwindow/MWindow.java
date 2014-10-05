package com.mangolion.multiwindow.mwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mangolion.multiwindow.R;
import com.mangolion.multiwindow.mdialog.MDialog;

public class MWindow extends FrameLayout implements OnClickListener {

	/**
	 * The glowing line background for the title bar
	 */
	public static Bitmap bgGlowLine;
	/**
	 * The id values that will be assigned by using the
	 * {@link #regenerateId(View)}, it will be incremented everytime its called
	 */
	public static int currentId = 100;
	/**
	 * The default color of all windows background
	 */
	public static int windowColor = Color.argb(90, 255, 128, 0);

	/**
	 * Whether or not the reflective glass on the window effect will be used
	 */
	public static boolean useAeroEffect = true;
	
	/**
	 * Create and inflate an {@link MWindow} object to the {@link LinearLayout}.
	 * 
	 * @param activity
	 *            The activity which context will be used to
	 */
	public static void initialize(Activity activity) {
		
			bgGlowLine = BitmapFactory.decodeResource(activity.getResources(),
					R.drawable.glowline);
		
	}

	/**
	 * Create and inflate an {@link MWindow} object to the {@link LinearLayout}.
	 * 
	 * @param relativeLayout
	 *            The parent relative layout that the created MWindow will be
	 *            inflated into
	 * @param title
	 *            The caption of the window
	 * @param width
	 *            The width of the window
	 * @param height
	 *            The height of the window
	 * @param icon
	 *            The icon, which will be drawn to the left of the icon
	 * @param fragment
	 *            The fragment that will be inflated into the window's container
	 *            view, set null if no fragment will be inflated
	 */
	public static MWindow makeWindow(RelativeLayout relativeLayout,
			String title, int width, int height, Bitmap icon, Fragment fragment) {
		Activity context = (Activity) relativeLayout.getContext();
		MWindow window = (MWindow) inflate(context, R.layout.window, null);
		relativeLayout.addView(window);
		window.setSize(width, height, true);
		window.title = title;
		window.icon = icon;
		window.fragment = fragment;
		window.rlParent = relativeLayout;
		window.activity = context;
		window.init();

		return window;
	}

	/**
	 * Window's parent, usually used for {@link MDialog}
	 */
	public MWindow parentWindow;
	/**
	 * Window's parent relative layout
	 */
	public RelativeLayout rlParent;
	/**
	 * Window's caption
	 */
	private String title;
	/**
	 * Window's textview that displays caption
	 */
	public TextView tvTitle;
	/**
	 * Displayed to the left of the {@link #tvTitle}
	 */
	public Bitmap icon;
	/**
	 * The Fragment that is displayed in the window's container, may be
	 * <B>null</B> if not used
	 */
	public Fragment fragment;
	/**
	 * one of the window's buttons, either used for minimizing,
	 * maximizing/windowed mode or closing and resizing
	 */
	public Button btMin, btMax, btClose, btSize;
	/**
	 * The Window's parent activity, used to get fragment manager to inflate the
	 * fragment
	 */
	public Activity activity;
	/**
	 * The container view used to hold the window's {@link #fragment}
	 */
	public FrameLayout containerView;
	/**
	 * The framelayout of the window used to hold the window's {@link #tvTitle},
	 * {@link #btMin}, {@link #btMax}, {@link #btClose}
	 */
	public FrameLayout llTitle;

	private boolean fullscreen = false, minimized = false, resizable,
			showMax = true, showMin = true, showClose = true;

	/**
	 * Temporary variables used to hold window's size, lastX and Y are used to
	 * keep last position of window before it move to fullscreen mode
	 */
	float width = -1, height = -1, lastX = 0, lastY = 0, dx, dy;

	/**
	 * Initialize the window, getting its view id, setting backgrounds,
	 * inflating fragments
	 */
	public void init() {
		containerView = (FrameLayout) findViewById(R.id.frWindow);
		btMin = (Button) findViewById(R.id.btMin);
		btMax = (Button) findViewById(R.id.btMax);
		btClose = (Button) findViewById(R.id.btClose);
		btSize = (Button) findViewById(R.id.btSize);
		llTitle = (FrameLayout) findViewById(R.id.frTitle);
		tvTitle = (TextView) findViewById(R.id.tvTitle);

		regenerateId(this);

		// set the small black outline of the window
		setBackgroundResource(R.drawable.outlinetransparent);
		// the glow background of title text
		tvTitle.setBackgroundResource(R.drawable.bgglow);
		// the glow line background of title bar
		llTitle.setBackgroundResource(R.drawable.glowline);
		// I had to give it a background so the view's onDraw method is called
		containerView.setBackgroundColor(Color.TRANSPARENT);

		// inflate the fragment into the container
		if (fragment != null)
			activity.getFragmentManager().beginTransaction()
					.add(containerView.getId(), fragment).commit();

		btMax.setOnClickListener(this);
		btMin.setOnClickListener(this);
		btClose.setOnClickListener(this);

		tvTitle.setText(title);
		setMinMaxClose(showMin, showMax, showClose);
		setMinimized(minimized);

		onInflateContent(containerView);

		// resizing the window's size
		btSize.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					dx = event.getX();
					dy = event.getY();
					break;

				case MotionEvent.ACTION_MOVE:
					float x = event.getX();
					float y = event.getY();
					setSize(width + (x - dx), height + (y - dy), true);
					break;
				}
				return true;
			}
		});

		// allow the user to drag the window with the title bar
		llTitle.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (fullscreen)
					return false;

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					dx = event.getX();
					dy = event.getY();
					break;

				case MotionEvent.ACTION_MOVE:
					float x = event.getX();
					float y = event.getY();
					setX(getX() + (x - dx));
					setY(getY() + (y - dy));
					invalidate();
					break;
				}
				return true;
			}
		});

		setOnClickListener(this);
	}

	/**
	 * Get the window's caption
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the view's caption
	 * 
	 * @param title
	 *            The view's caption
	 */
	public void setTitle(String title) {
		this.title = title;
		if (tvTitle != null) {
			tvTitle.setText(title);
		}
	}

	/**
	 * Catch all of the window's touch event to bring the window to focus and
	 * infront of other views
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		bringToFront();
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * Set the window's size
	 * <p>
	 * If you set using {@link LayoutParams#WRAP_CONTENT}, the view's width and
	 * height will be set when it is first inflated, because Android will only
	 * adjust wrap_content once.
	 * </p>
	 * <p>
	 * If you think the window's content will be changed later on, consider
	 * using a scroll view
	 * </p>
	 * 
	 * @param width
	 *            The width of the window
	 * @param height
	 *            The height of the window
	 * @param save
	 *            Whether or not this newly set values will be saved in the
	 *            view's width and height vars. Set it to false if the size
	 *            change is only temporary(Such as setting fullscreen mode)a
	 */
	public void setSize(float width, float height, boolean save) {
		ViewGroup.LayoutParams params = getLayoutParams();
		params.width = (int) width;
		params.height = (int) height;
		requestLayout();

		if (save) {
			this.width = width;
			this.height = height;
		}

		if (width == LayoutParams.WRAP_CONTENT
				|| height == LayoutParams.WRAP_CONTENT) {
			width = getWidth();
			height = getHeight();
		}
	}

	@Override
	public void onClick(View v) {
		bringToFront();

		if (v.getTag() == null)
			return;
		String tag = (String) v.getTag();
		if (tag.equals("min")) {
			setMinimized(!minimized);
		} else if (tag.equals("max")) {
			setFullscreen(!fullscreen);

		} else if (tag.equals("close")) {
			close();
		}
	}

	/**
	 * Remove the window from the parent {@link MWindow#rlParent}, remove the
	 * fragment if there are any
	 */
	public void close() {
		if (fragment != null)
			activity.getFragmentManager().beginTransaction().remove(fragment)
					.commit();
		rlParent.removeView(this);
	}

	/**
	 * Get the window's minimized state
	 */
	public boolean getMinimized() {
		return minimized;
	}

	/**
	 * Set the minimized state by resizing the view to its title bar
	 * 
	 * @param minimized
	 *            set Minimized state
	 */
	public void setMinimized(boolean minimized) {
		this.minimized = minimized;

		if (minimized) {
			if (fullscreen) {
				setFullscreen(!fullscreen);
			}
			setSize(300, llTitle.getHeight(), false);

			// hide the resize and maximize button
			btSize.setVisibility(View.GONE);
			btMax.setVisibility(View.GONE);
		} else {
			setSize(width, height, false);
			// return the visibility state of the mini/maximized and close
			// button
			setMinMaxClose(showMin, showMax, showClose);
		}

	}

	/**
	 * Get the window's fullscreen state
	 */
	public boolean getFullscreen() {
		return fullscreen;
	}

	/**
	 * Get the window's minimized state
	 * 
	 * @param fullscreen
	 *            fullscreen state
	 */
	public void setFullscreen(boolean fullscreen) {
		this.fullscreen = fullscreen;
		bringToFront();
		if (fullscreen) {
			setSize(rlParent.getWidth(), rlParent.getHeight(), false);
			// save windowed pos
			lastX = getX();
			lastY = getY();
			setX(0);
			setY(0);

			btSize.setVisibility(View.GONE);
		} else {

			setSize(width, height, false);
			setX(lastX);
			setY(lastY);

			setMinMaxClose(showMin, showMax, showClose);
		}

	}

	/**
	 * Set the visibility of the min/maximized and close buttons
	 * 
	 * @param min
	 *            visibility of Minimized button
	 * @param max
	 *            visibility of Maximized/Windowed button
	 * @param close
	 *            visibility of Close button
	 */
	public void setMinMaxClose(boolean min, boolean max, boolean close) {
		btMin.setVisibility(View.GONE);
		btMax.setVisibility(View.GONE);
		btClose.setVisibility(View.GONE);

		if (min)
			btMin.setVisibility(View.VISIBLE);
		if (max)
			btMax.setVisibility(View.VISIBLE);
		if (close)
			btClose.setVisibility(View.VISIBLE);
	}

	/**
	 * Disable/enable the view and if it is a viewgroup, disable/enable all of
	 * its children, and its viewgroups children if nessesary
	 */
	public void enableViewGroup(View viewy, boolean enabled) {
		viewy.setEnabled(enabled);
		if (!(viewy instanceof ViewGroup))
			return;

		int childCount = (int) ((ViewGroup) viewy).getChildCount();
		for (int i = 0; i < childCount; i++) {
			View view = ((ViewGroup) viewy).getChildAt(i);
			view.setEnabled(enabled);
			if (view instanceof ViewGroup) {
				enableViewGroup((ViewGroup) view, enabled);
			}
		}

		// if mwindow, deactivate everything in fragment
		if (viewy instanceof MWindow) {
			View fragView = ((MWindow) viewy).fragment.getView();
			if (fragView != null)
				enableViewGroup(((MWindow) viewy).fragment.getView(), enabled);
		}
	}

	/**
	 * Change the id of this view and if it is a view group, change the id of
	 * all of its childs, and its viewgroups childs if any.
	 * <p>
	 * Only use this after you have used {@link View#findviewbyid()}!
	 * </p>
	 */
	public static void regenerateId(View view) {
		view.setId(currentId);
		currentId++;
		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			for (int i = 0; i < group.getChildCount(); i++) {
				regenerateId(group.getChildAt(i));
			}
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// draw the background color of the window
		canvas.drawColor(windowColor);
	
		
		GradientDrawable winBorder = (GradientDrawable) activity
				.getResources().getDrawable(R.drawable.outlinetransparent);
		winBorder.setBounds(0, 0, getWidth(), getHeight());
		winBorder.draw(canvas);
	}

	public boolean isResizable() {
		return resizable;
	}

	/**
	 * Set whether or not the user can resize the window's size by
	 * hiding/showing the resize button
	 * 
	 * @param resizable
	 *            the view's resizability
	 */
	public void setResizable(boolean resizable) {
		this.resizable = resizable;
		if (resizable)
			btSize.setVisibility(View.VISIBLE);
		else
			btSize.setVisibility(View.GONE);
	}

	/**
	 * Called when the content of the window is inflated, use it to inflate
	 * content of window
	 * <p>
	 * I suggest that you use fragments instead though
	 * 
	 * @param container
	 *            the container view that the inflated content will be inflated
	 *            into
	 * @return the inflated view that will be used in the window's content
	 */
	public View onInflateContent(ViewGroup container) {
		return null;
	}

	public MWindow(Context context) {
		super(context);
		// init();
	}

	public MWindow(Context context, AttributeSet attrs) {
		super(context, attrs);
		// init();
	}

	public MWindow(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// init();
	}

}
