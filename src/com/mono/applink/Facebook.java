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

import java.io.DataOutputStream;
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


public class Facebook extends Activity {
	
	Context context;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context=this;
                
        final String url = getIntent().getData().toString();

        if(url.contains("posts") || url.contains("profile.php") || (!url.contains("&") && !url.contains("=") && url.length()>24))
        	//1)Posts->It is impossible to launch FeedbackActivity because of permission denied.
        	//With root permission and "am start" it's impossible to pass a Long extra_key...
        	//2)Profile
        	//3)Nickname
        {
        	new FacebookUser().execute();
        }else if(url.contains("sk=inbox"))//Message
        {
        	new FacebookMessage().execute();
        }else if(url.contains("event.php"))//event
        {
        	new FacebookEvent().execute();
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
    
    private class FacebookMessage extends AsyncTask<String, String, String> {
		
		ProgressDialog dialog;
		
        @Override 
        protected void onPreExecute() {
        	dialog = ProgressDialog.show(Facebook.this, "", 
                    "Loading. Please wait...", true);
        }

		@Override
		protected String doInBackground(String... params) {
			//String tid = getIntent().getData().toString().replaceAll("^.*tid=", ""); //tid in facebook application != tid via web
            String commands = "am start -a android.intent.action.MAIN -a android.intent.action.VIEW -n com.facebook.katana/.activity.messages.MailboxTabHostActivity";
	        return commands;
		} 
		
        @Override 
		protected void onPostExecute(String commands)
		{ 

        	Process p;   
            try {   
               p = Runtime.getRuntime().exec("su");//it is impossible to launch MailboxTabHostActivity because of permission denied unless you have root permission
               DataOutputStream os = new DataOutputStream(p.getOutputStream());   
               os.writeBytes(commands);   
               os.flush();
            } catch (IOException e) {   
               // TODO Auto-generated catch block   
               e.printStackTrace();   
            }  
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           	dialog.cancel();
           	android.os.Process.killProcess(android.os.Process.myPid());
		}
}
    
    private class FacebookEvent extends AsyncTask<String, String, String> {
		
		ProgressDialog dialog;
		
        @Override 
        protected void onPreExecute() {
        	dialog = ProgressDialog.show(Facebook.this, "", 
                    "Loading. Please wait...", true);
        }

		@Override
		protected String doInBackground(String... params) {
			String eid = getIntent().getData().toString().replaceAll("^.*eid=", "").replaceAll("&.*$", ""); //tid in facebook application != tid via web
            String commands = "am start -a android.intent.action.MAIN -a android.intent.action.VIEW -n com.facebook.katana/.activity.events.EventDetailsActivity -d content://com.facebook.katana.provider.EventsProvider/events/eid/"+eid;
			return commands;
		} 
		
        @Override 
		protected void onPostExecute(String commands)
		{ 
        	Process p;   
            try {   
               p = Runtime.getRuntime().exec("su");//it is impossible to launch EventDetailsActivity because of permission denied unless you have root permission
               DataOutputStream os = new DataOutputStream(p.getOutputStream());   
               os.writeBytes(commands);   
               os.flush();
            } catch (IOException e) {   
               // TODO Auto-generated catch block   
               e.printStackTrace();   
            }  
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           	dialog.cancel();
           	android.os.Process.killProcess(android.os.Process.myPid());        	
		}
}
		
		private class FacebookUser extends AsyncTask<Void, Void, Void> {
			
			ProgressDialog dialog;
	    	Intent intent = new Intent();
			
	        @Override 
	        protected void onPreExecute() {
	        	dialog = ProgressDialog.show(Facebook.this, "", 
	                    "Loading. Please wait...", true);
	        }

			@Override
			protected Void doInBackground(Void... params) {
				String user=null;
				Long id=null;
				String getIntent=getIntent().getData().toString();
				if(getIntent.contains("&id=") || getIntent.contains("?id="))
				{
					user = getIntent.replaceFirst("^.*&id=", "").replaceFirst("^.*\\?id=", "").replaceAll("&.*$","");
				    id = Long.parseLong(user);
				}else
				{
					user = getIntent.replaceFirst("^.*/", "").replaceAll("^.*\\?", "").replaceAll("%2F.*$","");
					HttpGet HttpGet = new HttpGet("https://api.facebook.com/method/fql.query?query=select%20uid%20from%20user%20where%20username=%22"+user+"%22&format=xml");
					HttpResponse response = Facebook.simpleHttp(HttpGet);
				    Element root = Facebook.simpleParser(response);
				    
				    NodeList ids = root.getElementsByTagName("uid");
				    id = Long.parseLong(ids.item(0).getFirstChild().getNodeValue());
				}
	            
			    intent.setClassName("com.facebook.katana", "com.facebook.katana.ProfileTabHostActivity");
	            //intent.putExtra("optin_origin", checkin);
	            //intent.putExtra("user_profile", user);
	            intent.putExtra("extra_user_id", id);
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
