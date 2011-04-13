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
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

public class Twitter extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
                
        final String url = getIntent().getData().toString();

        if(url.contains("twitter.com") && !url.contains("status") && !url.contains("direct_messages") && !url.contains("user_spam_reports") && !url.contains("account") && !url.contains("settings"))//Just if it is a link of a user profile
        {
        	new TwitterUser().execute();
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
    
	private class TwitterUser extends AsyncTask<Void, Void, Void> {
		
		ProgressDialog dialog;
    	Intent intent = new Intent();
		
        @Override 
        protected void onPreExecute() {
        	dialog = ProgressDialog.show(Twitter.this, "", 
                    "Loading. Please wait...", true);
        }

		@Override
		protected Void doInBackground(Void... params) {
        	String user = getIntent().getData().toString().replace("://twitter.com/", "").replace("https","").replace("http","").replaceAll("/.*$", "").replaceAll("\\?.*$", "");
			        	
	        HttpGet HttpGet = new HttpGet("http://api.twitter.com/1/users/show/"+user+".xml");
			HttpResponse response = Facebook.simpleHttp(HttpGet, getBaseContext());
		    Element root = Facebook.simpleParser(response);
		    
		    NodeList ids = root.getElementsByTagName("id");
		    Long id = Long.parseLong(ids.item(0).getFirstChild().getNodeValue());	
            intent.setClassName("com.twitter.android", "com.twitter.android.ProfileTabActivity");
            intent.putExtra("user_id", id);
            
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
		

    
	public static Element simpleParser(HttpResponse response)
	{
        Document docs=null;
		try {
			docs = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(response.getEntity().getContent());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        Element root=docs.getDocumentElement();	 
		
		return root;		
	}
	
	public static HttpResponse simpleHttp(HttpGet HttpGet, Context contexts)
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
