package tk.twpooi.tuetue.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import tk.twpooi.tuetue.R;

/**
 * Created by tw on 2017-01-15.
 */

public class RoundBlueButton extends Button {

    public RoundBlueButton(Context context){
        super(context);

        final float scale = getContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (25 * scale + 0.5f);

        LinearLayout.LayoutParams pm = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        pm.gravity = Gravity.CENTER;

        this.setLayoutParams(pm);
        this.setBackgroundResource(R.drawable.round_button_blue);
        this.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    public RoundBlueButton(Context context, AttributeSet atts){
        super(context, atts);

        final float scale = getContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (25 * scale + 0.5f);

        LinearLayout.LayoutParams pm = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        pm.gravity = Gravity.CENTER;

        this.setLayoutParams(pm);
        this.setBackgroundResource(R.drawable.round_button_blue);
        this.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

//    public RoundBlueButton(Context context, String title){
//        super(context);
//
//
//
//        this.setText(title);
//
//    }



}
