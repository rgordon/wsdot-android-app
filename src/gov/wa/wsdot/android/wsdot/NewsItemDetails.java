/*
 * Copyright (c) 2010 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.TextView;

public class NewsItemDetails extends Activity {
	WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		((TextView)findViewById(R.id.sub_section)).setText("News");
		
		Bundle b = getIntent().getExtras();
			
		webview = (WebView)findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setPluginsEnabled(true);
		webview.loadDataWithBaseURL(null, formatText(b), "text/html", "utf-8", null);
	}
	
	private String formatText(Bundle b)	{
		StringBuilder sb = new StringBuilder();
		String heading = b.getString("heading");
		String description = b.getString("description");
		String link = b.getString("link");
		String publishDate = b.getString("publishDate");
		
		sb.append("<h3>"+ heading +"</h3>");
		sb.append("<p>" + publishDate + "</p>");
		sb.append("<p>"+ description +"</p>");
		sb.append("<p><a href='"+ link +"'>Read the full release</a></p>");
			
		return sb.toString();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
	        webview.goBack();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}	

}
