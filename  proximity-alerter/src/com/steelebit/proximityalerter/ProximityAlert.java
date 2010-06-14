package com.steelebit.proximityalerter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

public class ProximityAlert extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("Tagged", "Alert received");
		Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
		vibrator.vibrate(getSosPattern(), -1);
	}

	private long[] getSosPattern()
	{
		int dot = 200;      // Length of a Morse Code "dot" in milliseconds
		int dash = 500;     // Length of a Morse Code "dash" in milliseconds
		int short_gap = 200;    // Length of Gap Between dots/dashes
		int medium_gap = 500;   // Length of Gap Between Letters
		int long_gap = 1000;    // Length of Gap Between Words
		long[] pattern = {
		    0,  // Start immediately
		    dot, short_gap, dot, short_gap, dot,    // s
		    medium_gap,
		    dash, short_gap, dash, short_gap, dash, // o
		    medium_gap,
		    dot, short_gap, dot, short_gap, dot,    // s
		    long_gap
		};
		
		return pattern;
	}
}
