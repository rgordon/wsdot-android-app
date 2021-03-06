/*
 * Copyright (c) 2012 Washington State Department of Transportation
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

import gov.wa.wsdot.android.wsdot.shared.VideoItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Video extends ListActivity {
	private static final int IO_BUFFER_SIZE = 4 * 1024;
	private static final String DEBUG_TAG = "Video";
	private ArrayList<VideoItem> videoItems = null;
	private VideoItemAdapter adapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AnalyticsUtils.getInstance(this).trackPageView("/News & Social Media/Video");
        
        setContentView(R.layout.main);
        videoItems = new ArrayList<VideoItem>();
        this.adapter = new VideoItemAdapter(this, R.layout.video_row, videoItems);
        ((TextView)findViewById(R.id.sub_section)).setText("Video");
        setListAdapter(this.adapter);
        new GetVideoItems().execute();
    }
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.refresh_menu_items, menu);
    	
    	return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_refresh:
			this.adapter.clear();
			videoItems.clear();
			new GetVideoItems().execute();
		}
		
		return super.onOptionsItemSelected(item);
	}    
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String videoId = videoItems.get(position).getId();
		String url = "http://www.youtube.com/watch?v=" + videoId;
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}
	   
    private class GetVideoItems extends AsyncTask<String, Integer, String> {
    	private final ProgressDialog dialog = new ProgressDialog(Video.this);

		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Retrieving latest videos ...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        this.dialog.setMax(10);
			this.dialog.setOnCancelListener(new OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	                cancel(true);
	            }				
			});
	        this.dialog.show();
		}
    	
	    protected void onCancelled() {
	        Toast.makeText(Video.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			this.dialog.incrementProgressBy(progress[0]);
		}

		@Override
		protected String doInBackground(String... params) {
	    	//DateFormat parseDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); //e.g. 2011-02-22T22:59:12.000Z
	    	//DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
	    	//TimeZone tz = TimeZone.getDefault();
	    	//parseDateFormat.setTimeZone(TimeZone.getTimeZone(tz.getID())); // Set TimeZone to what the local device is
	    	
			try {
				URL url = new URL("http://gdata.youtube.com/feeds/api/users/wsdot/uploads?v=2&alt=jsonc&max-results=10");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject data = obj.getJSONObject("data");			
				JSONArray items = data.getJSONArray("items");
				videoItems = new ArrayList<VideoItem>();
				VideoItem i = null;
				
				for (int j=0; j < items.length(); j++) {
					if (!this.isCancelled()) {
						JSONObject item = items.getJSONObject(j);
						JSONObject thumbnail = item.getJSONObject("thumbnail");
						i = new VideoItem();
						i.setId(item.getString("id"));
						i.setTitle(item.getString("title"));
						i.setDescription(item.getString("description"));
						i.setViewCount(item.getString("viewCount"));
						
						BufferedInputStream ins = new BufferedInputStream(new URL(thumbnail.getString("sqDefault")).openStream(), IO_BUFFER_SIZE);
	                    final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
	                    BufferedOutputStream out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
	                    copy(ins, out);
	                    out.flush();
	                    final byte[] rawData = dataStream.toByteArray();
	                    Bitmap bitmap = BitmapFactory.decodeByteArray(rawData, 0, rawData.length);                        
	                    final Drawable image = new BitmapDrawable(bitmap);
	                    i.setThumbNail(image);
						
						videoItems.add(i);
						publishProgress(1);
					} else {
						break;
					}
				}				
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
            if (videoItems != null && videoItems.size() > 0){
                adapter.notifyDataSetChanged();
                for(int i=0;i<videoItems.size();i++)
                adapter.add(videoItems.get(i));
            }
            adapter.notifyDataSetChanged();
		}   
    }
	
	private class VideoItemAdapter extends ArrayAdapter<VideoItem> {
        private ArrayList<VideoItem> items;

        public VideoItemAdapter(Context context, int textViewResourceId, ArrayList<VideoItem> items) {
        	super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        View v = convertView;
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.video_row, null);
	        }
	        VideoItem o = items.get(position);
	        if (o != null) {
	        	ImageView ic = (ImageView) v.findViewById(R.id.icon);
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                if (ic != null) {
                	ic.setImageDrawable(o.getThumbNail());
                }
                if (tt != null) {
                	tt.setText(o.getTitle());
                }
                if(bt != null) {
                	bt.setText(o.getDescription());
                }
	        }
	        return v;
        }
	}
	
    /**
     * Copy the content of the input stream into the output stream, using a
     * temporary byte array buffer whose size is defined by
     * {@link #IO_BUFFER_SIZE}.
     * 
     * @param in The input stream to copy from.
     * @param out The output stream to copy to.
     * @throws IOException If any error occurs during the copy.
     */
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }	
}
