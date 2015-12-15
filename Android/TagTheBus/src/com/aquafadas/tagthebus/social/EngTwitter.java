package com.aquafadas.tagthebus.social;

import android.content.Intent;

import com.aquafadas.tagthebus.PhotoActivity;

public class EngTwitter extends EngSocial {

	//static private final short PICTURE_SIZE = 200;

	public EngTwitter(PhotoActivity activity, boolean displayError) {

		mNetworkID = NETWORK_ID_TWITTER;
		mDisplayError = displayError;
		mActivity = activity;
		mUserGender = GENDER_NONE;
	}

	public void start() { }
	public void launch(int req, int res, Intent data) { }
	public void stop() { }

	@Override public boolean login() {

        if (mDisplayError)
    		alert("ERROR: Not implemented yet!");
		return false;
	}
	@Override public void logout() { }
	@Override public boolean isLogged() { return false; }
	@Override public boolean getUserPicture() { return false; }
	@Override public boolean getUserInfo() { return false; }
	@Override public boolean shareLink(String[] data) { return false; }
	@Override public boolean shareVideo(String[] data) { return false; }

    ////// Texture ID
    static public final short TEXTURE_ID = 252; // Reserved

}
