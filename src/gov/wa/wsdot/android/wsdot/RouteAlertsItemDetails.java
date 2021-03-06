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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.TextView;

public class RouteAlertsItemDetails extends Activity {
	WebView webview;
	DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		((TextView)findViewById(R.id.sub_section)).setText("Ferries Route Alerts");

		Bundle b = getIntent().getExtras();		
		String alertFullTitle = b.getString("AlertFullTitle");		
		Date date = new Date(Long.parseLong(b.getString("AlertPublishDate")));
		String alertPublishDate = displayDateFormat.format(date);
		String alertDescription = b.getString("AlertDescription");
		String alertFullText = b.getString("AlertFullText");
		String html_content = "<h3>"+ alertFullTitle + "</h3>" + "<p>" + alertPublishDate + "</p><p><b>" + alertDescription + "</b></p><p>" + alertFullText + "</p>";

		webview = (WebView)findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setPluginsEnabled(true);
		webview.loadDataWithBaseURL(null, html_content, "text/html", "utf-8", null);
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
