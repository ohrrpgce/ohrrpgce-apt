/*
Simple DirectMedia Layer
Java source code (C) 2009-2012 Sergii Pylypenko
  
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

package com.hamsterrepublic.ohrrpgce.game;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.InputDevice;
import android.view.Window;
import android.view.WindowManager;
import android.os.Environment;
import android.net.Uri;
import java.io.File;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.content.res.Resources;
import android.content.res.AssetManager;
import android.widget.Toast;
import android.util.Log;
import android.support.v4.content.FileProvider;

import android.widget.TextView;
import java.lang.Thread;
import java.util.concurrent.locks.ReentrantLock;
import android.os.Build;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.ArrayList;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import tv.ouya.console.api.OuyaController;

class Mouse
{
	public static final int LEFT_CLICK_NORMAL = 0;
	public static final int LEFT_CLICK_NEAR_CURSOR = 1;
	public static final int LEFT_CLICK_WITH_MULTITOUCH = 2;
	public static final int LEFT_CLICK_WITH_PRESSURE = 3;
	public static final int LEFT_CLICK_WITH_KEY = 4;
	public static final int LEFT_CLICK_WITH_TIMEOUT = 5;
	public static final int LEFT_CLICK_WITH_TAP = 6;
	public static final int LEFT_CLICK_WITH_TAP_OR_TIMEOUT = 7;
	
	public static final int RIGHT_CLICK_NONE = 0;
	public static final int RIGHT_CLICK_WITH_MULTITOUCH = 1;
	public static final int RIGHT_CLICK_WITH_PRESSURE = 2;
	public static final int RIGHT_CLICK_WITH_KEY = 3;
	public static final int RIGHT_CLICK_WITH_TIMEOUT = 4;

	public static final int SDL_FINGER_DOWN = 0;
	public static final int SDL_FINGER_UP = 1;
	public static final int SDL_FINGER_MOVE = 2;
	public static final int SDL_FINGER_HOVER = 3;

	public static final int ZOOM_NONE = 0;
	public static final int ZOOM_MAGNIFIER = 1;
	public static final int ZOOM_SCREEN_TRANSFORM = 2;
	public static final int ZOOM_FULLSCREEN_MAGNIFIER = 3;
}

abstract class DifferentTouchInput
{
	public abstract void process(final MotionEvent event);
	public abstract void processGenericEvent(final MotionEvent event);

	public static boolean ExternalMouseDetected = false;

	public static DifferentTouchInput getInstance()
	{
		boolean multiTouchAvailable1 = false;
		boolean multiTouchAvailable2 = false;
		// Not checking for getX(int), getY(int) etc 'cause I'm lazy
		Method methods [] = MotionEvent.class.getDeclaredMethods();
		for(Method m: methods) 
		{
			if( m.getName().equals("getPointerCount") )
				multiTouchAvailable1 = true;
			if( m.getName().equals("getPointerId") )
				multiTouchAvailable2 = true;
		}
		try {
			Log.i("SDL", "Device model: " + android.os.Build.MODEL);
			if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH )
			{
				return IcsTouchInput.Holder.sInstance;
			}
			if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD )
				return GingerbreadTouchInput.Holder.sInstance;
			if (multiTouchAvailable1 && multiTouchAvailable2)
				return MultiTouchInput.Holder.sInstance;
			else
				return SingleTouchInput.Holder.sInstance;
		} catch( Exception e ) {
			try {
				if (multiTouchAvailable1 && multiTouchAvailable2)
					return MultiTouchInput.Holder.sInstance;
				else
					return SingleTouchInput.Holder.sInstance;
			} catch( Exception ee ) {
				return SingleTouchInput.Holder.sInstance;
			}
		}
	}
	private static class SingleTouchInput extends DifferentTouchInput
	{
		private static class Holder
		{
			private static final SingleTouchInput sInstance = new SingleTouchInput();
		}
		@Override
		public void processGenericEvent(final MotionEvent event)
		{
			process(event);
		}
		public void process(final MotionEvent event)
		{
			int action = -1;
			if( event.getAction() == MotionEvent.ACTION_DOWN )
				action = Mouse.SDL_FINGER_DOWN;
			if( event.getAction() == MotionEvent.ACTION_UP )
				action = Mouse.SDL_FINGER_UP;
			if( event.getAction() == MotionEvent.ACTION_MOVE )
				action = Mouse.SDL_FINGER_MOVE;
			if ( action >= 0 )
				DemoGLSurfaceView.nativeMotionEvent( (int)event.getX(), (int)event.getY(), action, 0, 
												(int)(event.getPressure() * 1000.0),
												(int)(event.getSize() * 1000.0) );
		}
	}
	private static class MultiTouchInput extends DifferentTouchInput
	{
		public static final int TOUCH_EVENTS_MAX = 16; // Max multitouch pointers

		private class touchEvent
		{
			public boolean down = false;
			public int x = 0;
			public int y = 0;
			public int pressure = 0;
			public int size = 0;
		}
		
		protected touchEvent touchEvents[];
		
		MultiTouchInput()
		{
			touchEvents = new touchEvent[TOUCH_EVENTS_MAX];
			for( int i = 0; i < TOUCH_EVENTS_MAX; i++ )
				touchEvents[i] = new touchEvent();
		}
		
		private static class Holder
		{
			private static final MultiTouchInput sInstance = new MultiTouchInput();
		}

		public void processGenericEvent(final MotionEvent event)
		{
			process(event);
		}
		public void process(final MotionEvent event)
		{
			int action = -1;

			//Log.i("SDL", "Got motion event, type " + (int)(event.getAction()) + " X " + (int)event.getX() + " Y " + (int)event.getY());
			if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP ||
				(event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_CANCEL )
			{
				action = Mouse.SDL_FINGER_UP;
				for( int i = 0; i < TOUCH_EVENTS_MAX; i++ )
				{
					if( touchEvents[i].down )
					{
						touchEvents[i].down = false;
						DemoGLSurfaceView.nativeMotionEvent( touchEvents[i].x, touchEvents[i].y, action, i, touchEvents[i].pressure, touchEvents[i].size );
					}
				}
			}
			if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN )
			{
				action = Mouse.SDL_FINGER_DOWN;
				for( int i = 0; i < event.getPointerCount(); i++ )
				{
					int id = event.getPointerId(i);
					if( id >= TOUCH_EVENTS_MAX )
						id = TOUCH_EVENTS_MAX - 1;
					touchEvents[id].down = true;
					touchEvents[id].x = (int)event.getX(i);
					touchEvents[id].y = (int)event.getY(i);
					touchEvents[id].pressure = (int)(event.getPressure(i) * 1000.0);
					touchEvents[id].size = (int)(event.getSize(i) * 1000.0);
					DemoGLSurfaceView.nativeMotionEvent( touchEvents[id].x, touchEvents[id].y, action, id, touchEvents[id].pressure, touchEvents[id].size );
				}
			}
			if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE ||
				(event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN ||
				(event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP )
			{
				/*
				String s = "MOVE: ptrs " + event.getPointerCount();
				for( int i = 0 ; i < event.getPointerCount(); i++ )
				{
					s += " p" + event.getPointerId(i) + "=" + (int)event.getX(i) + ":" + (int)event.getY(i);
				}
				Log.i("SDL", s);
				*/
				int pointerReleased = -1;
				if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP )
					pointerReleased = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

				for( int id = 0; id < TOUCH_EVENTS_MAX; id++ )
				{
					int ii;
					for( ii = 0; ii < event.getPointerCount(); ii++ )
					{
						if( id == event.getPointerId(ii) )
							break;
					}
					if( ii >= event.getPointerCount() )
					{
						// Up event
						if( touchEvents[id].down )
						{
							action = Mouse.SDL_FINGER_UP;
							touchEvents[id].down = false;
							DemoGLSurfaceView.nativeMotionEvent( touchEvents[id].x, touchEvents[id].y, action, id, touchEvents[id].pressure, touchEvents[id].size );
						}
					}
					else
					{
						if( pointerReleased == id && touchEvents[pointerReleased].down )
						{
							action = Mouse.SDL_FINGER_UP;
							touchEvents[id].down = false;
						}
						else if( touchEvents[id].down )
						{
							action = Mouse.SDL_FINGER_MOVE;
						}
						else
						{
							action = Mouse.SDL_FINGER_DOWN;
							touchEvents[id].down = true;
						}
						touchEvents[id].x = (int)event.getX(ii);
						touchEvents[id].y = (int)event.getY(ii);
						touchEvents[id].pressure = (int)(event.getPressure(ii) * 1000.0);
						touchEvents[id].size = (int)(event.getSize(ii) * 1000.0);
						DemoGLSurfaceView.nativeMotionEvent( touchEvents[id].x, touchEvents[id].y, action, id, touchEvents[id].pressure, touchEvents[id].size );
					}
				}
			}
			if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_HOVER_MOVE ) // Support bluetooth/USB mouse - available since Android 3.1
			{
				// TODO: it is possible that multiple pointers return that event, but we're handling only pointer #0
				if( touchEvents[0].down )
					action = Mouse.SDL_FINGER_UP;
				else
					action = Mouse.SDL_FINGER_HOVER;
				touchEvents[0].down = false;
				touchEvents[0].x = (int)event.getX();
				touchEvents[0].y = (int)event.getY();
				touchEvents[0].pressure = 0;
				touchEvents[0].size = 0;
				DemoGLSurfaceView.nativeMotionEvent( touchEvents[0].x, touchEvents[0].y, action, 0, touchEvents[0].pressure, touchEvents[0].size );
			}
		}
	}
	private static class GingerbreadTouchInput extends MultiTouchInput
	{
		private static class Holder
		{
			private static final GingerbreadTouchInput sInstance = new GingerbreadTouchInput();
		}

		GingerbreadTouchInput()
		{
			super();
		}
		public void process(final MotionEvent event)
		{
			boolean hwMouseEvent = (	(event.getSource() & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE ||
										(event.getSource() & InputDevice.SOURCE_STYLUS) == InputDevice.SOURCE_STYLUS ||
										(event.getMetaState() & KeyEvent.FLAG_TRACKING) != 0 ); // Hack to recognize Galaxy Note Gingerbread stylus
			if( ExternalMouseDetected != hwMouseEvent )
			{
				ExternalMouseDetected = hwMouseEvent;
				DemoGLSurfaceView.nativeHardwareMouseDetected(hwMouseEvent ? 1 : 0);
			}
			super.process(event);
		}
		public void processGenericEvent(final MotionEvent event)
		{
			process(event);
		}
	}
	private static class IcsTouchInput extends GingerbreadTouchInput
	{
		private static class Holder
		{
			private static final IcsTouchInput sInstance = new IcsTouchInput();
		}
		private int buttonState = 0;
		public void process(final MotionEvent event)
		{
			//Log.i("SDL", "Got motion event, type " + (int)(event.getAction()) + " X " + (int)event.getX() + " Y " + (int)event.getY() + " buttons " + buttonState + " source " + event.getSource());
			int buttonStateNew = event.getButtonState();
			if( buttonStateNew != buttonState )
			{
				for( int i = 1; i <= MotionEvent.BUTTON_FORWARD; i *= 2 )
				{
					if( (buttonStateNew & i) != (buttonState & i) )
						DemoGLSurfaceView.nativeMouseButtonsPressed(i, ((buttonStateNew & i) == 0) ? 0 : 1);
				}
				buttonState = buttonStateNew;
			}
			super.process(event); // Push mouse coordinate first
		}
		public void processGenericEvent(final MotionEvent event)
		{
			// Joysticks are supported since Honeycomb, but I don't care about it, because very little devices have it
			if( (event.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) == InputDevice.SOURCE_CLASS_JOYSTICK )
			{
				int ouya_player = OuyaController.getPlayerNumByDeviceId(event.getDeviceId());
				// event.getAxisValue(AXIS_HAT_X) and event.getAxisValue(AXIS_HAT_Y) are joystick arrow keys, they also send keyboard events
				DemoGLSurfaceView.nativeGamepadAnalogJoystickInput(
					event.getAxisValue(MotionEvent.AXIS_X), event.getAxisValue(MotionEvent.AXIS_Y),
					event.getAxisValue(MotionEvent.AXIS_Z), event.getAxisValue(MotionEvent.AXIS_RZ),
					event.getAxisValue(MotionEvent.AXIS_RTRIGGER), event.getAxisValue(MotionEvent.AXIS_LTRIGGER),
					ouya_player );
				return;
			}
			// Process mousewheel
			if( event.getAction() == MotionEvent.ACTION_SCROLL )
			{
				int scrollX = Math.round(event.getAxisValue(MotionEvent.AXIS_HSCROLL));
				int scrollY = Math.round(event.getAxisValue(MotionEvent.AXIS_VSCROLL));
				DemoGLSurfaceView.nativeMouseWheel(scrollX, scrollY);
				return;
			}
			super.processGenericEvent(event);
		}
	}
}


class DemoRenderer extends GLSurfaceView_SDL.Renderer
{
	public DemoRenderer(MainActivity _context)
	{
		context = _context;
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.i("SDL", "libSDL: DemoRenderer.onSurfaceCreated(): paused " + mPaused + " mFirstTimeStart " + mFirstTimeStart );
		mGlSurfaceCreated = true;
		mGl = gl;
		if( ! mPaused && ! mFirstTimeStart )
			nativeGlContextRecreated();
		mFirstTimeStart = false;
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
		Log.i("SDL", "libSDL: DemoRenderer.onSurfaceChanged(): paused " + mPaused + " mFirstTimeStart " + mFirstTimeStart + " w " + w + " h " + h);
		if( w < h && Globals.HorizontalOrientation )
		{
			// Sometimes when Android awakes from lockscreen, portrait orientation is kept
			int x = w;
			w = h;
			h = x;
		}
		mWidth = w;
		mHeight = h;
		mGl = gl;
		nativeResize(w, h, Globals.KeepAspectRatio ? 1 : 0);
	}
	
	public void onSurfaceDestroyed() {
		Log.i("SDL", "libSDL: DemoRenderer.onSurfaceDestroyed(): paused " + mPaused + " mFirstTimeStart " + mFirstTimeStart );
		mGlSurfaceCreated = false;
		mGlContextLost = true;
		nativeGlContextLost();
	};

	public void onDrawFrame(GL10 gl) {

		mGl = gl;
		DrawLogo(mGl);
		SwapBuffers();

		nativeInitJavaCallbacks();
		
		// Make main thread priority lower so audio thread won't get underrun
		// Thread.currentThread().setPriority((Thread.currentThread().getPriority() + Thread.MIN_PRIORITY)/2);
		
		mGlContextLost = false;

		if(Globals.CompatibilityHacksStaticInit)
			MainActivity.LoadApplicationLibrary(context);

		Settings.Apply(context);
		accelerometer = new AccelerometerReader(context);
		// Tweak video thread priority, if user selected big audio buffer
		if(Globals.AudioBufferConfig >= 2)
			Thread.currentThread().setPriority( (Thread.NORM_PRIORITY + Thread.MIN_PRIORITY) / 2 ); // Lower than normal
		 // Calls main() and never returns, hehe - we'll call eglSwapBuffers() from native code
		nativeInit( Globals.DataDir,
					Globals.CommandLine,
					( (Globals.SwVideoMode && Globals.MultiThreadedVideo) || Globals.CompatibilityHacksVideo ) ? 1 : 0,
					android.os.Debug.isDebuggerConnected() ? 1 : 0 );
		System.exit(0); // The main() returns here - I don't bother with deinit stuff, just terminate process
	}

	public int swapBuffers() // Called from native code
	{
		if( ! super.SwapBuffers() && Globals.NonBlockingSwapBuffers )
		{
			if(mRatelimitTouchEvents)
			{
				synchronized(this)
				{
					this.notify();
				}
			}
			return 0;
		}

		if(mGlContextLost) {
			mGlContextLost = false;
			Settings.SetupTouchscreenKeyboardGraphics(context); // Reload on-screen buttons graphics
			DrawLogo(mGl);
			super.SwapBuffers();
		}

		// Unblock event processing thread only after we've finished rendering
		if(mRatelimitTouchEvents)
		{
			synchronized(this)
			{
				this.notify();
			}
		}
		if( context.isScreenKeyboardShown() )
		{
			try {
				Thread.sleep(50); // Give some time to the keyboard input thread
			} catch(Exception e) { };
		}
		return 1;
	}

	public void showScreenKeyboardWithoutTextInputField() // Called from native code
	{
		class Callback implements Runnable
		{
			public MainActivity parent;
			public void run()
			{
				parent.showScreenKeyboardWithoutTextInputField();
			}
		}
		Callback cb = new Callback();
		cb.parent = context;
		context.runOnUiThread(cb);
	}

	public void showScreenKeyboard(final String oldText, int sendBackspace) // Called from native code
	{
		class Callback implements Runnable
		{
			public MainActivity parent;
			public String oldText;
			public boolean sendBackspace;
			public void run()
			{
				parent.showScreenKeyboard(oldText, sendBackspace);
			}
		}
		Callback cb = new Callback();
		cb.parent = context;
		cb.oldText = oldText;
		cb.sendBackspace = (sendBackspace != 0);
		context.runOnUiThread(cb);
	}

	public void hideScreenKeyboard() // Called from native code
	{
		class Callback implements Runnable
		{
			public MainActivity parent;
			public void run()
			{
				parent.hideScreenKeyboard();
			}
		}
		Callback cb = new Callback();
		cb.parent = context;
		context.runOnUiThread(cb);
	}

	public int isScreenKeyboardShown() // Called from native code
	{
		return context.isScreenKeyboardShown() ? 1 : 0;
	}

	public void setScreenKeyboardHintMessage(String s)
	{
		context.setScreenKeyboardHintMessage(s);
	}

	// Open an app to send an email with 0 to 3 file attachments: each file may be null.
	// Called from native code
	public void emailFiles(String address, String subject, String message, String file1, String file2, String file3)
	{
		// The simplest way allow other apps to read our private files is this:
		// It doesn't affect the file permissions, but rather
		// sets permissions on the "abstract path associated with this URI".
		// (It isn't possible to make a file in the files directory world-readable by setting file permissions)
		//
		// File file = new File(file1);
		// file.setReadable(true, false);  //android 2.3+ only
		// uris.add(Uri.fromFile(file));

		// FileProvider will fail if a file is not in the files, cache, or external storage folder,
		// as defined in res/xml/fileprovider_paths.xml
		// But those pretty much cover anything that could come up except secondary external storage

		String authority = context.getPackageName() + ".fileprovider";

		ArrayList<Uri> uris = new ArrayList<Uri>();
		if (file1 != null) {
			uris.add(FileProvider.getUriForFile(context, authority, new File(file1)));
		}
		if (file2 != null) {
			uris.add(FileProvider.getUriForFile(context, authority, new File(file2)));
		}
		if (file3 != null) {
			uris.add(FileProvider.getUriForFile(context, authority, new File(file3)));
		}

		Intent email = new Intent(Intent.ACTION_SEND_MULTIPLE);
		email.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		email.putExtra(Intent.EXTRA_EMAIL, new String[] {address});
		email.putExtra(Intent.EXTRA_SUBJECT, subject);
		email.putExtra(Intent.EXTRA_TEXT, message);
		email.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		email.setType("text/plain");
		context.startActivity(Intent.createChooser(email, "Email the game developer how?"));
	}

	public void startAccelerometerGyroscope(int started)
	{
		accelerometer.openedBySDL = (started != 0);
		if( accelerometer.openedBySDL && !mPaused )
			accelerometer.start();
		else
			accelerometer.stop();
	}

	public int isRunningOnConsole() // Called from native code
	{
		/*
		We only currently handle 3 Android consoles.
		OUYA, GameStick, and Fire-TV.
		
		The only real use for this function is deciding whether or
		not to hide the on-screen touch controls. GamePad support is
		always enabled no matter what, and more specific testing is
		needed for enabling IAP on each different console.
		*/
		
		/* Detect OUYA and compatible */
		if( isRunningOnOUYA() != 0 ) return -1;
		
		String model = android.os.Build.MODEL.toLowerCase();
		String mfg = android.os.Build.MANUFACTURER.toLowerCase();
		String device = android.os.Build.DEVICE.toLowerCase();
		Log.i("SDL", "isRunningOnConsole(): model=\"" + model + "\" mfg=\"" + mfg + "\" device=\"" + device + "\"");
		
		/* Detect PlayJam Gamestick */
		if( model.startsWith("gamestick") ) return -1;
		
		/* Detect Amazon Fire-TV */
		if( mfg.equals("amazon") && model.startsWith("aft") ) return -1;
		
		/* Detect Sony/Ericson Xperia Play */
		if( mfg.startsWith("sony") && ( device.equals("zeus") || device.startsWith("r800") || device.startsWith("r88") ) ) return -1;
		
		return 0;
	}

	public int isRunningOnOUYA() // Called from native code
	{
		return context.isRunningOnOUYA() ? -1 : 0;
	}

	public void setOUYADeveloperId(String devId) //called from native code
	{
		context.setOUYADeveloperId(devId);
	}

	public void OUYAPurchaseRequest(String identifier, byte[] keyDerBytes) //called from native code
	{
		context.OUYAPurchaseRequest(identifier, keyDerBytes);
	}

	public int OUYAPurchaseIsReady() //called from native code
	{
		return context.OUYAPurchaseIsReady();
	}

	public int OUYAPurchaseSucceeded() //called from native code
	{
		return context.OUYAPurchaseSucceeded();
	}

	public void OUYAReceiptsRequest(byte[] keyDerBytes) //called from native code
	{
		context.OUYAReceiptsRequest(keyDerBytes);
	}

	public int OUYAReceiptsAreReady() //called from native code
	{
		return context.OUYAReceiptsAreReady();
	}

	public String OUYAReceiptsResult() //called from native code
	{
		return context.OUYAReceiptsResult();
	}

	public void exitApp()
	{
		 nativeDone();
	}

	public void getAdvertisementParams(int params[])
	{
		context.getAdvertisementParams(params);
	}
	public void setAdvertisementVisible(int visible)
	{
		context.setAdvertisementVisible(visible);
	}
	public void setAdvertisementPosition(int left, int top)
	{
		context.setAdvertisementPosition(left, top);
	}
	public void requestNewAdvertisement()
	{
		context.requestNewAdvertisement();
	}

	private int PowerOf2(int i)
	{
		int value = 1;
		while (value < i)
			value <<= 1;
		return value;
	}
	public void DrawLogo(GL10 gl)
	{
		/*
		// TODO: this not quite works, as it seems
		BitmapDrawable bmp = null;
		try
		{
			bmp = new BitmapDrawable(context.getAssets().open("logo.png"));
		}
		catch(Exception e)
		{
			bmp = new BitmapDrawable(context.getResources().openRawResource(R.drawable.publisherlogo));
		}
		int width = bmp.getBitmap().getWidth();
		int height = bmp.getBitmap().getHeight();
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * width * height);
		//byteBuffer.order(ByteOrder.BIG_ENDIAN);
		bmp.getBitmap().copyPixelsToBuffer(byteBuffer);
		byteBuffer.position(0);

		gl.glViewport(0, 0, mWidth, mHeight);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
		gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);
		gl.glPixelStorei(gl.GL_UNPACK_ALIGNMENT, 1);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		int textureName = -1;
		int mTextureNameWorkspace[] = new int[1];
		int mCropWorkspace[] = new int[4];
		gl.glGenTextures(1, mTextureNameWorkspace, 0);
		textureName = mTextureNameWorkspace[0];
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureName);
		gl.glActiveTexture(textureName);
		gl.glClientActiveTexture(textureName);
		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA,
				PowerOf2(width), PowerOf2(height), 0,
				GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, null);
		gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0,
				width, height,
				GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, byteBuffer);
		mCropWorkspace[0] = 0; // u
		mCropWorkspace[1] = height; // v
		mCropWorkspace[2] = width;
		mCropWorkspace[3] = -height;
		((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D,
				GL11Ext.GL_TEXTURE_CROP_RECT_OES, mCropWorkspace, 0);
		((GL11Ext) gl).glDrawTexiOES(0, -mHeight, 0, mWidth, mHeight);
		gl.glActiveTexture(0);
		gl.glClientActiveTexture(0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
		gl.glDeleteTextures(1, mTextureNameWorkspace, 0);

		gl.glFlush();
		*/
	}


	private native void nativeInitJavaCallbacks();
	private native void nativeInit(String CurrentPath, String CommandLine, int multiThreadedVideo, int isDebuggerConnected);
	private native void nativeResize(int w, int h, int keepAspectRatio);
	private native void nativeDone();
	private native void nativeGlContextLost();
	public native void nativeGlContextRecreated();
	public native void nativeGlContextLostAsyncEvent();
	public static native void nativeTextInput( int ascii, int unicode );
	public static native void nativeTextInputFinished();

	private MainActivity context = null;
	public AccelerometerReader accelerometer = null;
	
	private GL10 mGl = null;
	private EGL10 mEgl = null;
	private EGLDisplay mEglDisplay = null;
	private EGLSurface mEglSurface = null;
	private EGLContext mEglContext = null;
	private boolean mGlContextLost = false;
	public boolean mGlSurfaceCreated = false;
	public boolean mPaused = false;
	private boolean mFirstTimeStart = true;
	public int mWidth = 0;
	public int mHeight = 0;

	public static final boolean mRatelimitTouchEvents = true; //(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO);
}

class DemoGLSurfaceView extends GLSurfaceView_SDL {
	public DemoGLSurfaceView(MainActivity context) {
		super(context);
		mParent = context;
		touchInput = DifferentTouchInput.getInstance();
		setEGLConfigChooser(Globals.VideoDepthBpp, Globals.NeedDepthBuffer, Globals.NeedStencilBuffer, Globals.NeedGles2);
		mRenderer = new DemoRenderer(context);
		setRenderer(mRenderer);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) 
	{
		touchInput.process(event);
		if( DemoRenderer.mRatelimitTouchEvents )
		{
			limitEventRate(event);
		}
		return true;
	};

	@Override
	public boolean onGenericMotionEvent (final MotionEvent event)
	{
		touchInput.processGenericEvent(event);
		if( DemoRenderer.mRatelimitTouchEvents )
		{
			limitEventRate(event);
		}
		return true;
	}
	
	public void limitEventRate(final MotionEvent event)
	{
		// Wait a bit, and try to synchronize to app framerate, or event thread will eat all CPU and we'll lose FPS
		// With Froyo the rate of touch events seems to be limited by OS, but they are arriving faster then we're redrawing anyway
		if((event.getAction() == MotionEvent.ACTION_MOVE ||
			event.getAction() == MotionEvent.ACTION_HOVER_MOVE))
		{
			synchronized(mRenderer)
			{
				try
				{
					mRenderer.wait(300L); // And sometimes the app decides not to render at all, so this timeout should not be big.
				} catch (InterruptedException e) { }
			}
		}
	}

	public void exitApp() {
		mRenderer.exitApp();
	};

	@Override
	public void onPause() {
		if(mRenderer.mPaused)
			return;
		mRenderer.mPaused = true;
		mRenderer.nativeGlContextLostAsyncEvent();
		if( mRenderer.accelerometer != null ) // For some reason it crashes here often - are we getting this event before initialization?
			mRenderer.accelerometer.stop();
		super.onPause();
	};
	
	public boolean isPaused() {
		return mRenderer.mPaused;
	}

	@Override
	public void onResume() {
		if(!mRenderer.mPaused)
			return;
		mRenderer.mPaused = false;
		super.onResume();
		Log.i("SDL", "libSDL: DemoGLSurfaceView.onResume(): mRenderer.mGlSurfaceCreated " + mRenderer.mGlSurfaceCreated + " mRenderer.mPaused " + mRenderer.mPaused);
		if( mRenderer.mGlSurfaceCreated && ! mRenderer.mPaused || Globals.NonBlockingSwapBuffers )
			mRenderer.nativeGlContextRecreated();
		if( mRenderer.accelerometer != null && mRenderer.accelerometer.openedBySDL ) // For some reason it crashes here often - are we getting this event before initialization?
			mRenderer.accelerometer.start();
	};

	// This seems like redundant code - it handled in MainActivity.java
	@Override
	public boolean onKeyDown(int keyCode, final KeyEvent event) {
		//On non-ouya hardware, player will always be -1
		int player = OuyaController.getPlayerNumByDeviceId(event.getDeviceId());
		//Log.i("SDL", "Got key down event, id " + keyCode + " meta " + event.getMetaState() + " event " + event.toString());
		if( nativeKey( keyCode, 1, player ) == 0 )
				return super.onKeyDown(keyCode, event);
		return true;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, final KeyEvent event) {
		//On non-ouya hardware, player will always be -1
		int player = OuyaController.getPlayerNumByDeviceId(event.getDeviceId());
		//Log.i("SDL", "Got key up event, id " + keyCode + " meta " + event.getMetaState());
		if( nativeKey( keyCode, 0, player ) == 0 )
				return super.onKeyUp(keyCode, event);
		return true;
	}

	DemoRenderer mRenderer;
	MainActivity mParent;
	DifferentTouchInput touchInput = null;

	public static native void nativeMotionEvent( int x, int y, int action, int pointerId, int pressure, int radius );
	public static native int nativeKey( int keyCode, int down, int ouya_player );
	public static native void nativeTouchpad( int x, int y, int down, int multitouch );
	public static native void initJavaCallbacks();
	public static native void nativeHardwareMouseDetected( int detected );
	public static native void nativeMouseButtonsPressed( int buttonId, int pressedState );
	public static native void nativeMouseWheel( int scrollX, int scrollY );
	public static native void nativeGamepadAnalogJoystickInput( float stick1x,  float stick1y, float stick2x, float stick2y, float rtrigger, float ltrigger, int ouya_player );
}


