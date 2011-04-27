/*
 *      This file is part of AppLink <https://github.com/monossido/AppLink>
 *      
 *      AppLink is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *      
 *      AppLink is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *      
 *      You should have received a copy of the GNU General Public License
 *      along with Project Lumiére.  If not, see <http://www.gnu.org/licenses/>.
 *      
 */
package com.mono.applink;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

public class Foursquare extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
        final String url = getIntent().getData().toString();

        if(url.contains("user"))//user with id
        {
        	new FoursquareUser().execute();
        }else if(url.contains("venue"))
        {
        	new FoursquareVenue().execute();
        }else
        {
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("Sorry, but this type of link is not currently supported");
        	builder.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
        	      finish();
                }});
        	builder.setPositiveButton("Open in Browser", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	Intent i = new Intent();
                	i.setAction("android.intent.action.VIEW"); 
                	i.addCategory("android.intent.category.BROWSABLE");
                	i.setComponent(new ComponentName("com.android.browser", "com.android.browser.BrowserActivity"));
                	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                	i.setData(Uri.parse(url));
                	startActivity(i);
                	finish();
                }
            });
        	AlertDialog alert = builder.create();
        	alert.show();
        }
        
    }
    
	private class FoursquareUser extends AsyncTask<Void, Void, Void> {
		
		ProgressDialog dialog;
    	Intent intent = new Intent();
		
        @Override 
        protected void onPreExecute() {
        	dialog = ProgressDialog.show(Foursquare.this, "", 
                    "Loading. Please wait...", true);
        }

		@Override
		protected Void doInBackground(Void... params) {
			String user = getIntent().getData().toString().replaceAll("^.*user/", "");
			
		    intent.setClassName("com.joelapenna.foursquared", "com.joelapenna.foursquared.UserDetailsActivity");
            intent.putExtra("com.joelapenna.foursquared.UserDetailsActivity.EXTRA_USER_ID", user);
            
	        return null;
		}
		
        @Override 
		protected void onPostExecute(Void params)
		{ 
        	dialog.cancel();
            finish();
            startActivity(intent);
		}
	}
	
	private class FoursquareVenue extends AsyncTask<Void, Void, Void> {
		
		ProgressDialog dialog;
    	Intent intent = new Intent();
		
        @Override 
        protected void onPreExecute() {
        	dialog = ProgressDialog.show(Foursquare.this, "", 
                    "Loading. Please wait...", true);
        }

		@Override
		protected Void doInBackground(Void... params) {
			String venue = getIntent().getData().toString().replaceAll("^.*venue/", "");
			
		    intent.setClassName("com.joelapenna.foursquared", "com.joelapenna.foursquared.VenueActivity");
            intent.putExtra("com.joelapenna.foursquared.VenueActivity.INTENT_EXTRA_VENUE_ID", venue);
            
	        return null;
		}
		
        @Override 
		protected void onPostExecute(Void params)
		{ 
        	dialog.cancel();
            finish();
            startActivity(intent);
		}
	}
	
	public static HttpResponse simpleHttp(HttpGet HttpGet)
	{
        HttpResponse response=null;

        DefaultHttpClient client = new DefaultHttpClient();
        try {
            response = client.execute(HttpGet);

        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
		return response;		
	}
	
}