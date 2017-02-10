package tk.twpooi.tuetue.image;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.NormalListDialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;

import tk.twpooi.tuetue.R;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by tw on 2016-08-15.
 */
public class ShowImageActivity extends AppCompatActivity {

    // UI
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private TextView closeBtn;
    private TextView editBtn;
    private RelativeLayout rl_background;
    private PhotoView photoView;

    private boolean isEnableEdit;
    private boolean isNeedCrop;
    private boolean isProfile;
    private String defaultProfileImageUrl;
    private int code;
    private UploadImage uploadImage;

    private String title = "image";
    private Bitmap originalBitmap = null;
    private Bitmap bitmap = null;
    private Uri imageUri = null;
    private boolean isImageChange;

    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        setStatusColor();

        initData();

        initUI();

    }


    private void initData() {
        Intent intent = getIntent();
        bitmap = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("bitmap"), 0, getIntent()
                        .getByteArrayExtra("bitmap").length);
        originalBitmap = bitmap;
        isEnableEdit = intent.getBooleanExtra("edit", false);
        isNeedCrop = intent.getBooleanExtra("crop", false);
        isProfile = intent.getBooleanExtra("isProfile", false);
        if (isProfile) {
            defaultProfileImageUrl = intent.getStringExtra("defaultProfile");
        }
        code = intent.getIntExtra("code", -1);
        uploadImage = new UploadImage(this);

        progressDialog = new MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();
    }

    private void initUI() {

        rl_background = (RelativeLayout) findViewById(R.id.layout_background);
        photoView = (PhotoView) findViewById(R.id.image);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(title);
        closeBtn = (TextView) findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnableEdit && isImageChange) {
                    Intent intent = new Intent();
                    if (isNeedCrop) {
                        ByteArrayOutputStream bs = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bs);
                        intent.putExtra("bitmap", bs.toByteArray());
                    } else {
                        intent.putExtra("uri", imageUri);
                    }
                    setResult(code, intent);
                }
                finish();
            }
        });
        editBtn = (TextView) findViewById(R.id.edit_btn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showListDialog();
            }
        });
        if (isEnableEdit) {
            editBtn.setVisibility(View.VISIBLE);
        } else {
            editBtn.setVisibility(View.GONE);
        }

        if (bitmap != null) {
            photoView.setImageBitmap(bitmap);
        }

    }

    public void showListDialog() {
        String[] list = {"앨범에서 가져오기", "초기화", "기본 이미지로 설정하기"};
        final NormalListDialog dialog = new NormalListDialog(this, list);
        dialog.title("선택")
                .titleTextSize_SP(14.5f)
                .show();

        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // 앨범
                        uploadImage.getPhotoFromAlbum(isNeedCrop);
                        break;
//                    case 1: // 카메라
//                        uploadImage.getPhotoFromCamera(isNeedCrop);
//                        break;
                    case 1:
                        bitmap = originalBitmap;
                        photoView.setImageBitmap(bitmap);
                        isImageChange = false;
                        setCloseBtnTitle();
                        break;
                    case 2:
                        if (isProfile) {
                            progressDialog.show();
                            Picasso.with(getApplicationContext())
                                    .load(defaultProfileImageUrl)
                                    .into(new Target() {
                                        @Override
                                        public void onBitmapLoaded(Bitmap bm, Picasso.LoadedFrom from) {
                                            bitmap = bm;
                                            isImageChange = true;
                                            setCloseBtnTitle();
                                            photoView.setImageBitmap(bm);
                                            progressDialog.hide();
                                        }

                                        @Override
                                        public void onBitmapFailed(Drawable errorDrawable) {

                                        }

                                        @Override
                                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                                        }
                                    });
                        }
                        break;
                    default:
                        break;
                }

                dialog.dismiss();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uploadImage.onActivityResult(requestCode, resultCode, data);

        bitmap = uploadImage.getBitmap();
        imageUri = uploadImage.getUri();

        if (isNeedCrop) {
            photoView.setImageBitmap(bitmap);
        } else {
            Picasso.with(getApplicationContext())
                    .load(imageUri)
                    .into(photoView);
        }
        isImageChange = true;
        setCloseBtnTitle();
    }

    public void setCloseBtnTitle() {
        if (isImageChange) {
            closeBtn.setText("저장");
        } else {
            closeBtn.setText("닫기");
        }
    }

    public Toolbar getToolbar() {
        return this.toolbar;
    }

    public void hideToolbar() {
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        rl_background.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        setStatusColorDark();
    }

    public void showToolbar() {
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        rl_background.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        setStatusColor();
    }

    private void setStatusColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }
    }

    private void setStatusColorDark() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
