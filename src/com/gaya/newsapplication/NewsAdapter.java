package com.gaya.newsapplication;

import java.util.List;

import com.gaya.newsapplication.imagecache.ImageLoader;
import com.gaya.newsapplication.news.News;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Adapter for the News Listview displayed on the main screen of the
 * application. This is a sub class of ArrayAdapter.
 * 
 * @author Gaya
 * 
 */
public class NewsAdapter extends ArrayAdapter<News> {

    private final Activity   context;
    private final List<News> news;
    private ViewHolder       holder = null;

    public NewsAdapter(Activity context, List<News> news) {
        super(context, R.layout.news_item, news);
        this.context = context;
        this.news = news;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.news_item, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = (TextView) rowView.findViewById(R.id.titleTextView);
            viewHolder.description = (TextView) rowView.findViewById(R.id.descriptionTextView);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.newsImageView);
            viewHolder.imageLayout = (LinearLayout) rowView.findViewById(R.id.imageLayout);
            rowView.setTag(viewHolder);
        }

        // fill data
        holder = (ViewHolder) rowView.getTag();
        final News newsItem = news.get(position);
        holder.title.setText(newsItem.getHeadLine());
        holder.description.setText(newsItem.getSlugLine());
        // imageHref might be not available for some news items. For them href
        // appears as empty string or word "null"
        if (!newsItem.getThumbnailImageHref().equals("")
                && !(newsItem.getThumbnailImageHref().equalsIgnoreCase("null"))) {
            String image_url = newsItem.getThumbnailImageHref();
            // use ImageLoader for lazy loading
            ImageLoader imgLoader = new ImageLoader(context);
            // Set default image to the ImageView till the actual image gets
            // loaded
            holder.image.setImageResource(R.drawable.default_image);
            // Sets visible since the default view is in the GONE state
            holder.imageLayout.setVisibility(View.VISIBLE);
            imgLoader.displayImage(image_url, holder.image);

        } else {
            // Due to reusable views the items which don't have an image should
            // have their ImageView visibility state to GONE. Then only the text
            // will fill up the entire space
            holder.imageLayout.setVisibility(View.GONE);
        }
        // set the onClick listener to the row which will open up the item's
        // tinyUrl on browser
        rowView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String url = newsItem.getTinyUrl();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);

            }
        });

        return rowView;

    }

    /**
     * This is to reuse the the views in the Listview.
     * 
     * @author Gaya
     * 
     */
    static class ViewHolder {
        public TextView     title;
        public TextView     description;
        public ImageView    image;
        public LinearLayout imageLayout;
    }

}
