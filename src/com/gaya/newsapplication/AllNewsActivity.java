package com.gaya.newsapplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gaya.newsapplication.http.HttpServiceHandler;
import com.gaya.newsapplication.news.News;
import com.gaya.newsapplication.util.AppUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;

/**
 * This is the main activity class of the application. This fetch data from the
 * server and display sorted news items in the main screen. The web service call
 * is done in a background thread to avoid performance issues in the UI thread.
 * 
 * @author Gaya
 * 
 */
public class AllNewsActivity extends ListActivity {

    private static final String TAG            = AllNewsActivity.class.getName();
    private ProgressDialog      progressDialog = null;
    private ListView            newsListView   = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_news);
        newsListView = getListView();
        new GetNews().execute();

        Button refreshButton = (Button) findViewById(R.id.buttonRefresh);
        refreshButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new GetNews().execute();
            }
        });

    }

    /**
     * Make a web service call to fetch data from the server and set them to the
     * adapter which will be used in Listview.
     * 
     * @author Gaya
     * 
     */
    private class GetNews extends AsyncTask<Void, Void, Boolean> {
        private List<News> newsList = new ArrayList<News>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Display a Progress dialog till data gets loaded into the Listview
            progressDialog = new ProgressDialog(AllNewsActivity.this);
            progressDialog.setMessage(AppUtils.PROGRESS_DIALOG_MESSAGE);
            progressDialog.setCancelable(false);
            progressDialog.show();
            // Clear list before adding updated news items
            if (0 < newsList.size()) {
                newsList.clear();
            }

        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            boolean isSuccess = false;
            HttpServiceHandler httpServiceHandler = new HttpServiceHandler();
            // Make web service call to the server to fetch news items
            String jsonString = httpServiceHandler.invokeRequest(AppUtils.URL);

            if (null != jsonString) {
                Log.d(TAG, jsonString);
                try {
                    JSONObject jsonObj = new JSONObject(jsonString);

                    // Get JSON Array node
                    JSONArray newsJsonArray = jsonObj.getJSONArray(AppUtils.ITEMS_ARRAY_NODE);

                    // looping through All Contacts
                    for (int i = 0; i < newsJsonArray.length(); i++) {
                        JSONObject newsObject = newsJsonArray.getJSONObject(i);
                        News newsItem = new News();
                        newsItem.setHeadLine(newsObject.getString(AppUtils.HEAD_LINE));
                        newsItem.setSlugLine(newsObject.getString(AppUtils.SLUG_LINE));
                        newsItem.setThumbnailImageHref(newsObject.getString(AppUtils.THUMBNAIL_IMAGE_HREF));
                        newsItem.setTinyUrl(newsObject.getString(AppUtils.TINY_URL));
                        String formattedDateString = newsObject.getString(AppUtils.DATE_LINE);
                        SimpleDateFormat formatter = new SimpleDateFormat(AppUtils.DATE_FORMAT);
                        try {
                            Date date = formatter.parse(formattedDateString);
                            newsItem.setDateTime(date.getTime());

                        } catch (ParseException e) {
                            Log.e(TAG, "Json Parse Exception");
                        }
                        newsList.add(newsItem);
                        isSuccess = true;
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Json Exception");
                }
            } else {
                Log.d(TAG, "Empty dataset");
            }
            
            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            // Dismiss the Progress dialog
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            
            if (isSuccess) {
                // Sort news items by dateline
                Comparator<News> comperator = new Comparator<News>() {

                    @Override
                    public int compare(News lhs, News rhs) {
                        return rhs.getDateTime().compareTo(lhs.getDateTime());
                    }
                };
                Collections.sort(newsList, comperator);

                // Set news items list to adapter
                BaseAdapter adapter = new NewsAdapter(AllNewsActivity.this, newsList);
                newsListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                
            } else {
                // Display an alert to notify user about the error.
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AllNewsActivity.this);
                alertDialogBuilder.setMessage(AppUtils.ALERT_DIALOG_MESSAGE).setCancelable(false)
                        .setPositiveButton(AppUtils.ALERT_DIALOG_OK_BUTTON, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }

        }

    }

}
