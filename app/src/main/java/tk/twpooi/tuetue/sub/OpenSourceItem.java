package tk.twpooi.tuetue.sub;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import tk.twpooi.tuetue.R;

/**
 * Created by tw on 2017-02-06.
 */

public class OpenSourceItem {

    public static final String APACHE2_0 = "Apache 2.0";
    public static final String MIT = "MIT License";

    public static int APACHE_COLOR = R.color.pastel_orange;
    public static int MIT_COLOR = R.color.pastel_blue;

    private String title;
    private String author;
    private String url;
    private String license;

    OpenSourceItem(String t, String a, String u, String l) {
        title = t;
        author = a;
        url = u;
        license = l;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getUrl() {
        return url;
    }

    public String getLicense() {
        return license;
    }

    public static View makeView(Context context, OpenSourceItem item) {

        View v = LayoutInflater.from(context).inflate(R.layout.open_source_custom_item, null, false);


        TextView title = (TextView) v.findViewById(R.id.tv_title);
        TextView author = (TextView) v.findViewById(R.id.tv_author);
        TextView url = (TextView) v.findViewById(R.id.tv_url);
        TextView license = (TextView) v.findViewById(R.id.tv_license);

        title.setText(item.getTitle());
        author.setText(item.getAuthor());
        url.setText(item.getUrl());
        license.setText(item.getLicense());

        return v;

    }

    public View getCustomView(final Context context) {

        View v = LayoutInflater.from(context).inflate(R.layout.open_source_custom_item, null, false);

        CardView cardView = (CardView) v.findViewById(R.id.cv);
        switch (getLicense()) {
            case OpenSourceItem.APACHE2_0:
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, APACHE_COLOR));
                break;
            case OpenSourceItem.MIT:
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, MIT_COLOR));
                break;
        }
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getUrl()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        TextView title = (TextView) v.findViewById(R.id.tv_title);
        TextView author = (TextView) v.findViewById(R.id.tv_author);
        TextView url = (TextView) v.findViewById(R.id.tv_url);
        TextView license = (TextView) v.findViewById(R.id.tv_license);

        title.setText(getTitle());
        author.setText(getAuthor());
        url.setText(getUrl());
        license.setText(getLicense());

        return v;

    }

}
