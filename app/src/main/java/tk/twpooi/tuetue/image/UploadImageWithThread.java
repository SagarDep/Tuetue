package tk.twpooi.tuetue.image;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tw on 2017-01-31.
 */

public class UploadImageWithThread extends Thread {

    private Bitmap image;
    private String name;
    private String SERVER;
    private String type;

    private String returnString;

    public UploadImageWithThread(String server, Bitmap image, String name, String type) {
        this.SERVER = server;
        this.image = image;
        this.name = name;
        this.type = type;
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

    public void run() {
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
            returnString = response;

            System.out.println(returnString);

        } catch (Exception e) {
            e.printStackTrace();
            returnString = null;
        }
    }

    public String getResult() {
        return returnString;
    }

}
