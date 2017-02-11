package tk.twpooi.tuetue.sub;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import tk.twpooi.tuetue.R;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by tw on 2016-08-16.
 */
public class IntroduceFragment extends Fragment {


    // UI
    private View view;
    private Context context;

    private String image;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        image = getArguments().getString("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // UI
        view = inflater.inflate(R.layout.fragment_image_viewer, container, false);
        context = container.getContext();

        init();

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void init() {

        PhotoView photoView = (PhotoView) view.findViewById(R.id.image);

        Picasso.with(context)
                .load(image)
                .into(photoView);

    }

}
