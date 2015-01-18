/*
Simple DirectMedia Layer
Java source code (C) 2009-2014 Sergii Pylypenko

This software is provided 'as-is', without any express or implied
warranty.  In no event will the authors be held liable for any damages
arising from the use of this software.

Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not
   claim that you wrote the original software. If you use this software
   in a product, an acknowledgment in the product documentation would be
   appreciated but is not required. 
2. Altered source versions must be plainly marked as such, and must not be
   misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/

package com.hamsterrepublic.game;

import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.AssetManager;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.InputDevice;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.content.Intent;
import android.view.View;
import android.view.Display;


public abstract class Clipboard
{
	public static Clipboard get()
	{
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
			return NewerClipboard.Holder.Instance;
		return OlderClipboard.Holder.Instance;
	}
	public abstract void set(final Context context, final String text);
	public abstract String get(final Context context);
	public abstract void setListener(final Context context, final Runnable listener);

	private static class NewerClipboard extends Clipboard
	{
		private static class Holder
		{
			private static final NewerClipboard Instance = new NewerClipboard();
		}
		public void set(final Context context, final String text)
		{
			try {
				ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
				if( clipboard != null )
					clipboard.setText(text);
			} catch (Exception e) {
				Log.i("SDL", "setClipboardText() exception: " + e.toString());
			}
		}
		public String get(final Context context)
		{
			String ret = "";
			try {
				ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
				if( clipboard != null && clipboard.getText() != null )
					ret = clipboard.getText().toString();
			} catch (Exception e) {
				Log.i("SDL", "getClipboardText() exception: " + e.toString());
			}
			return ret;
		}
		public void setListener(final Context context, final Runnable listener)
		{
			Log.i("SDL", "Cannot set clipboard listener on Android 2.3 or older");
			ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
			clipboard.addPrimaryClipChangedListener(new OnPrimaryClipChangedListener()
			{
				public void onPrimaryClipChanged()
				{
					listener.run();
				}
			});
		}
	}

	private static class OlderClipboard extends Clipboard
	{
		private static class Holder
		{
			private static final OlderClipboard Instance = new OlderClipboard();
		}
		public void set(final Context context, final String text)
		{
			try {
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
				if( clipboard != null )
					clipboard.setText(text);
			} catch (Exception e) {
				Log.i("SDL", "setClipboardText() exception: " + e.toString());
			}
		}
		public String get(final Context context)
		{
			String ret = "";
			try {
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
				if( clipboard != null && clipboard.getText() != null )
					ret = clipboard.getText().toString();
			} catch (Exception e) {
				Log.i("SDL", "getClipboardText() exception: " + e.toString());
			}
			return ret;
		}
		public void setListener(final Context context, final Runnable listener)
		{
			Log.i("SDL", "Cannot set clipboard listener on Android 2.3 or older");
		}
	}
}


