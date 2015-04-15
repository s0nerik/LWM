package com.danh32.fontify;


import android.content.Context;
import android.util.AttributeSet;


public class ToggleButton extends android.widget.ToggleButton {
	public ToggleButton(Context context) {
		super(context);
	}

	public ToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		// return early for eclipse preview mode
		if (isInEditMode()) return;
		
		FontManager.getInstance().setFont(this, attrs);
	}
	
	public void setFont(String fontPath) {
		FontManager.getInstance().setFont(this, fontPath);
	}
	
	public void setFont(int resId) {
		String fontPath = getContext().getString(resId);
		setFont(fontPath);
	}
}
