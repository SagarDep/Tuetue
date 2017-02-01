package tk.twpooi.tuetue.image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Created by tw on 2017-01-31.
 */

public class UploadImage {

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_iMAGE = 2;
    private static final int PICK_FROM_CAMERA_WITHOUT_CROP = 4;
    private static final int PICK_FROM_ALBUM_WITHOUT_CROP = 5;

    private Uri mImageCaptureUri;
    private Bitmap bitmap;

    private Activity activity;

    public UploadImage(Activity activity) {
        this.activity = activity;
        this.bitmap = null;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Uri getUri() {
        return mImageCaptureUri;
    }

    private void startActivityForResult(Intent intent, int code) {
        activity.startActivityForResult(intent, code);
    }

    public void getPhotoFromCamera(boolean isCrop) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 임시로 사용할 파일의 경로를 생성
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));

        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        if (isCrop) {
            startActivityForResult(intent, PICK_FROM_CAMERA);
        } else {
            startActivityForResult(intent, PICK_FROM_CAMERA_WITHOUT_CROP);
        }

    }

    public void getPhotoFromAlbum(boolean isCrop) {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        if (isCrop) {
            startActivityForResult(intent, PICK_FROM_ALBUM);
        } else {
            startActivityForResult(intent, PICK_FROM_ALBUM_WITHOUT_CROP);
        }

    }

    private void cropImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");

        // CROP할 이미지를 200*200 크기로 저장
        intent.putExtra("outputX", 200); // CROP한 이미지의 x축 크기
        intent.putExtra("outputY", 200); // CROP한 이미지의 y축 크기
        intent.putExtra("aspectX", 1); // CROP 박스의 X축 비율
        intent.putExtra("aspectY", 1); // CROP 박스의 Y축 비율
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_FROM_iMAGE); // CROP_FROM_CAMERA case문 이동
    }

    private void cropImageNoSize(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_FROM_iMAGE); // CROP_FROM_CAMERA case문 이동
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        Bitmap bt = null;
        try {
            bt = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), mImageCaptureUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bt;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case PICK_FROM_ALBUM: {
                // 이후의 처리가 카메라와 같으므로 일단  break없이 진행합니다.
                // 실제 코드에서는 좀더 합리적인 방법을 선택하시기 바랍니다.
                mImageCaptureUri = data.getData();
                cropImage(mImageCaptureUri);
                break;
            }
            case PICK_FROM_ALBUM_WITHOUT_CROP:
                mImageCaptureUri = data.getData();
//                bitmap = getBitmapFromUri(mImageCaptureUri);
//                cropImageNoSize(mImageCaptureUri);
                break;
            case PICK_FROM_CAMERA: {
                // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정합니다.
                // 이후에 이미지 크롭 어플리케이션을 호출하게 됩니다.
                cropImage(mImageCaptureUri);
                break;
            }
            case PICK_FROM_CAMERA_WITHOUT_CROP:
//                bitmap = getBitmapFromUri(mImageCaptureUri);
//                cropImageNoSize(mImageCaptureUri);
                break;
            case CROP_FROM_iMAGE: {
                // 크롭이 된 이후의 이미지를 넘겨 받습니다.
                // 이미지뷰에 이미지를 보여준다거나 부가적인 작업 이후에
                // 임시 파일을 삭제합니다.
                if (resultCode != RESULT_OK) {
                    return;
                }

                final Bundle extras = data.getExtras();

                // CROP된 이미지를 저장하기 위한 FILE 경로

                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data"); // CROP된 BITMAP
                    bitmap = photo;
//                    img.setImageBitmap(photo); // 레이아웃의 이미지칸에 CROP된 BITMAP을 보여줌

                    break;

                }
                // 임시 파일 삭제
                File f = new File(mImageCaptureUri.getPath());
                if (f.exists()) {
                    f.delete();
                }
                break;
            }
        }
    }

    public void uploadImage(String server, Bitmap image, String name, String type) {
        new Upload(server, image, name, type).execute();
    }

    private String hashMapToUrl(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    //async task to upload image
    private class Upload extends AsyncTask<Void, Void, String> {
        private Bitmap image;
        private String name;
        private String SERVER;
        private String type;

        public Upload(String server, Bitmap image, String name, String type) {
            this.SERVER = server;
            this.image = image;
            this.name = name;
            this.type = type;
        }

        @Override
        protected String doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //compress the image to jpg format
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            /*
            * encode image to base64 so that it can be picked by saveImage.php file
            * */
            String encodeImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

            //generate hashMap to store encodedImage and the name
            HashMap<String, String> detail = new HashMap<>();
            detail.put("name", name);
            detail.put("image", encodeImage);
            detail.put("type", type);

            try {
                //convert this HashMap to encodedUrl to send to php file
                String dataToSend = hashMapToUrl(detail);
                //make a Http request and send data to saveImage.php file
                String response = Request.post(SERVER, dataToSend);

                //return the response
                return response;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        @Override
        protected void onPostExecute(String s) {
            //show image uploaded
            Toast.makeText(activity.getApplicationContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
        }
    }


}
