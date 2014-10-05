package com.mangolion.multiwindow.mdialog;

import com.mangolion.multiwindow.R;
import com.mangolion.multiwindow.mwindow.MWindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MDialog extends MWindow {

	/**
	 * Create and inflate an {@link MDialog#} object to the {@link LinearLayout}
	 * 
	 * @param relativeLayout
	 *            The parent relative layout that the created MWindow will be
	 *            inflated into
	 * @param title
	 *            The caption of the window
	 * @param msg
	 *            The message of the dialog
	 * @param icon
	 *            The icon, which will be drawn to the left of the icon
	 * @param parent
	 *            The parent window of the dialog, if this is set, this window
	 *            and its contents will be disabled until the dialog is closed
	 *            if you want to unlock the parent window, call
	 *            {@link MDialog#enableViewGroup(parent, true)}
	 */
	public static MDialog makeDialog(RelativeLayout relativeLayout,
			String title, String msg, Bitmap icon, MWindow parent) {
		Activity context = (Activity) relativeLayout.getContext();
		MDialog window = (MDialog) inflate(context, R.layout.dialog, null);
		relativeLayout.addView(window);
		// the size of the dialog is automatically set to wrap content
		window.setSize(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				true);
		window.setTitle(title);
		window.icon = icon;
		window.fragment = null;
		window.rlParent = relativeLayout;
		window.activity = context;
		window.parentWindow = parent;
		window.message = msg;
		window.init();
		// the dialog's min/max/close buttons are hiden
		window.setMinMaxClose(false, false, false);
		window.setResizable(false);

		// attemp to set the dialog at the center of ites parent window
		if (parent != null) {
			window.enableViewGroup(window.parentWindow, false);
			window.setX(parent.getX() + parent.getWidth() / 2
					- window.getWidth() / 2);
			window.setY(parent.getY() + parent.getHeight() / 2);
		}
		return window;
	}

	/**
	 * The displayed message of the dialog
	 */
	private String message;
	/**
	 * The positive button, call
	 * {@link #setPositiveButton(String, OnClickListener)} to display it in the
	 * dialog
	 */
	public Button btPositive;
	/**
	 * The Negative button, call
	 * {@link #setNegativeButton(String, OnClickListener)} to display it in the
	 * dialog
	 */
	public Button btNegative;
	/**
	 * The Input Edittext, call {@link #setEdittext(String, String)} to display
	 * it in the dialog
	 */
	public EditText etInput;
	/**
	 * The TextView that display the message
	 */
	public TextView tvMsg;

	/**
	 * Inflate the dialog's content onto the window
	 */
	@Override
	public View onInflateContent(ViewGroup container) {
		View view = inflate(activity, R.layout.dialogcontent, container);
		btPositive = (Button) view.findViewById(R.id.btPositive);
		btNegative = (Button) view.findViewById(R.id.btNegative);
		etInput = (EditText) view.findViewById(R.id.etInput);
		tvMsg = (TextView) view.findViewById(R.id.tvMessage);

		tvMsg.setText(message);
		return view;
	}

	/**
	 * @return the dialog's message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the dialog's message
	 */
	public void setMessage(String message) {
		this.message = message;
		tvMsg.setText(message);
	}
	/**
	 * Show the positive button on the dialog
	 * @param caption the button's text
	 * @param listener the button's onClickListener
	 */
	public void setPositiveButton(String caption, View.OnClickListener listener) {
		btPositive.setVisibility(View.VISIBLE);
		btPositive.setText(caption);
		btPositive.setOnClickListener(listener);
	}
	/**
	 * Show the negative button on the dialog
	 * @param caption the button's text
	 * @param listener the button's onClickListener
	 */
	public void setNegativeButton(String caption, View.OnClickListener listener) {
		btNegative.setVisibility(View.VISIBLE);
		btNegative.setText(caption);
		btNegative.setOnClickListener(listener);
	}
	/**
	 * Show the Edittext in the dialog
	 * @param hint 
	 * @param defaultText
	 */
	public void setEdittext(String hint, String defaultText) {
		etInput.setVisibility(View.VISIBLE);
		etInput.setHint(hint);
		etInput.setText(defaultText);
	}
	/**
	 * When the dialog is closed, reenable the parent window if aplicable
	 */
	@Override
	public void close() {
		if (parentWindow != null)
			enableViewGroup(parentWindow, true);
		super.close();
	}

	public MDialog(Context context) {
		super(context);
		// init();
	}

	public MDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
		// init();
	}

	public MDialog(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// init();
	}
}
