package tk.twpooi.tuetue.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by tw on 2017-02-06.
 */

public class CustomScrollView extends ScrollView {

    private OnCustomScrollChangeListener listener;

    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnCustomScrollChangeListener(OnCustomScrollChangeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY);
        if (listener != null) {
            if (scrollY < 0 || oldScrollY < 0) {
                return;
            }
            listener.scrollChanged(scrollX, scrollY, scrollX - oldScrollX, scrollY - oldScrollY);
        }
    }

}
