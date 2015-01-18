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

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.text.Editable;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
import android.content.res.Configuration;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.view.View.OnKeyListener;
import android.view.MenuItem;
import android.view.Menu;
import android.view.Gravity;
import android.text.method.TextKeyListener;
import java.util.LinkedList;
import java.io.SequenceInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Set;
import android.text.SpannedString;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import android.view.inputmethod.InputMethodManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import java.util.concurrent.Semaphore;
import android.content.pm.ActivityInfo;
import android.view.Display;
import android.util.DisplayMetrics;
import android.text.InputType;
import android.util.Log;
import android.view.Surface;
import android.app.ProgressDialog;
import android.app.KeyguardManager;
import android.view.ViewTreeObserver;
import android.graphics.Rect;


public class RestartMainActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i("SDL", "Restarting main activity");
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		_layout = new LinearLayout(this);
		_layout.setOrientation(LinearLayout.VERTICAL);
		_layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
		
		//Get the display so we can know the screen size
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		_tv = new TextView(this);
		_tv.setMaxLines(2);
		_tv.setMinLines(2);
		_tv.setText(R.string.restarting_please_wait);
		_tv.setTextSize(30f);
		_tv.setPadding((int)(width * 0.1), (int)(height * 0.1), (int)(width * 0.1), 0);
		_layout.addView(_tv);

		_videoLayout = new FrameLayout(this);
		_videoLayout.addView(_layout);

		setContentView(_videoLayout);

		new Thread(new Runnable()
		{
			public void run()
			{
				try{
					Thread.sleep(2000);
				} catch (InterruptedException e) {}
				Intent intent = new Intent(RestartMainActivity.this, MainActivity.class);
				intent.putExtra(ACTIVITY_AUTODETECT_SCREEN_ORIENTATION, getIntent().getBooleanExtra(ACTIVITY_AUTODETECT_SCREEN_ORIENTATION, false));
				RestartMainActivity.this.startActivity(intent);
				try{
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
				System.exit(0);
			}
		}).start();
	}

	private TextView _tv = null;
	private LinearLayout _layout = null;
	private FrameLayout _videoLayout = null;

	public static final String ACTIVITY_AUTODETECT_SCREEN_ORIENTATION = "libsdl.org.ACTIVITY_AUTODETECT_SCREEN_ORIENTATION";
}
