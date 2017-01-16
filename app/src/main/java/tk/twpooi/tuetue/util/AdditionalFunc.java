package tk.twpooi.tuetue.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tw on 2017-01-14.
 */

public class AdditionalFunc {

    public static HashMap<String, Object> getUserInfo(String data){

        HashMap<String, Object> item = new HashMap<>();

        try {
            // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
            JSONObject jObject = new JSONObject(data);
            // results라는 key는 JSON배열로 되어있다.
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

//                HashMap<String, String> hashTemp = new HashMap<>();
            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

//                        HashMap<String, String> hashTemp = new HashMap<>();
                item.put("userId", (String)temp.get("id"));
                item.put("name", (String)temp.get("name"));
                item.put("email", (String)temp.get("email"));
                item.put("img", (String)temp.get("img"));
                item.put("nickname", (String)temp.get("nickname"));
                item.put("contact", (String)temp.get("contact"));
                item.put("intro", (String)temp.get("intro"));

                ArrayList<String> in = new ArrayList<String>();
                String interest = (String)temp.get("interest");
                if(!interest.equals("")){
                    for(String s : interest.split(",")){
                        in.add(s);
                    }
                }
                item.put("interest", in);

            }

        } catch (JSONException e) {
            e.printStackTrace();
            item.clear();
        }

        return item;

    }

    public static ArrayList<HashMap<String, Object>> getTutorList(String data){

        ArrayList<HashMap<String, Object>> list = new ArrayList<>();

        try {
            // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
            JSONObject jObject = new JSONObject(data);
            // results라는 key는 JSON배열로 되어있다.
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                HashMap<String, Object> hashTemp = new HashMap<>();
                hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("userid", (String)temp.get("userid"));
                hashTemp.put("img", (String)temp.get("img"));
                hashTemp.put("email", (String)temp.get("email"));
                hashTemp.put("nickname", (String)temp.get("nickname"));
                hashTemp.put("category", (String)temp.get("category"));
                hashTemp.put("cost", Integer.parseInt((String)temp.get("cost")));
                hashTemp.put("count", Integer.parseInt((String)temp.get("count")));
                hashTemp.put("limit", Integer.parseInt((String)temp.get("limit")));
                hashTemp.put("interest", Integer.parseInt((String)temp.get("interest")));
                hashTemp.put("time", (String)temp.get("time"));
                hashTemp.put("contents", (String)temp.get("contents"));
                hashTemp.put("isFinish", (String)temp.get("isFinish"));

                String participant = (String)temp.get("participant");

                ArrayList<String> par = new ArrayList<>();

                if(!participant.equals("")){
                    String[] p = participant.split(",");

                    for(String s : p){
                        par.add(s);
                    }

                }
                if (par.size() > 0 && par.get(0).equals("")) {
                    par.remove(0);
                }
                hashTemp.put("participant", par);

                list.add(hashTemp);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static ArrayList<HashMap<String, Object>> getTuteeList(String data){

        ArrayList<HashMap<String, Object>> list = new ArrayList<>();

        try {
            // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
            JSONObject jObject = new JSONObject(data);
            // results라는 key는 JSON배열로 되어있다.
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                HashMap<String, Object> hashTemp = new HashMap<>();
                hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("userid", (String)temp.get("userid"));
                hashTemp.put("img", (String)temp.get("img"));
                hashTemp.put("email", (String)temp.get("email"));
                hashTemp.put("nickname", (String)temp.get("nickname"));
                hashTemp.put("category", (String)temp.get("category"));
                hashTemp.put("cost", Integer.parseInt((String)temp.get("cost")));
                hashTemp.put("time", (String)temp.get("time"));
                hashTemp.put("contents", (String)temp.get("contents"));
                hashTemp.put("isFinish", (String)temp.get("isFinish"));
                hashTemp.put("tutorId", (String)temp.get("tutorId"));

                String participant = (String)temp.get("participant");

                ArrayList<String> par = new ArrayList<>();

                if(!participant.equals("")){
                    String[] p = participant.split(",");

                    for(String s : p){
                        par.add(s);
                    }

                }
                if (par.size() > 0 && par.get(0).equals("")) {
                    par.remove(0);
                }
                hashTemp.put("participant", par);

                list.add(hashTemp);

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

}
