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
import android.content.Intent;

// Stub class for compiling without cloud save support
class CloudSave
{
	public CloudSave(MainActivity p)
	{
	}

	public void onStart() {
	}

	public void onStop() {
	}

	public void onActivityResult(int request, int response, Intent data) {
	}

	public boolean save(String filename, String saveId, String dialogTitle, String description, String imageFile, long playedTimeMs)
	{
		return false;
	}

	public boolean load(String filename, String saveId, String dialogTitle)
	{
		return false;
	}

	public boolean isSignedIn()
	{
		return false;
	}
}
