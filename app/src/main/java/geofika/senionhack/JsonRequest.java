package geofika.senionhack;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alexander on 2017-09-09.
 */

public class JsonRequest {

    private static final String TAG = "JsonRequest";
    private RequestQueue mQueue = null;
    private String mUrl = "";

    JsonRequest(Context context, String url){

        this.mUrl = url;
        mQueue = Volley.newRequestQueue(context);
        mQueue.start();
    }

    void makeRequest(User user){

        // Instantiate the RequestQueue.

        Map<String, String> postParam= new HashMap<String, String>();
        postParam.put("operator", "userUpdate");
        postParam.put("userName", user.getName());
        postParam.put("region", user.getZone());

        JSONObject jsonBody = new JSONObject(postParam);



        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                mUrl, jsonBody,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.getMessage());
            }
        }){

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }};

        Log.d(TAG, "post: " + jsonObjReq);

        jsonObjReq.setTag(TAG);
        // Add the request to the RequestQueue.
        mQueue.add(jsonObjReq);
    }
}
