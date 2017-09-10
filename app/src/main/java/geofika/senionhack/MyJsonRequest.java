package geofika.senionhack;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Alexander on 2017-09-09.
 */

public class MyJsonRequest {

    private static final String TAG = "MyJsonRequest";
    private RequestQueue mQueue = null;
    private String mUrl = "";
    private HashMap<String,Integer> mTeamList = null;

    MyJsonRequest(Context context, String url) {

        this.mUrl = url;
        mQueue = Volley.newRequestQueue(context);
        mQueue.start();
    }

    public HashMap<String, Integer> getTeamList() {
        return mTeamList;
    }

    void makeRequest(final User user) {

        // Instantiate the RequestQueue.

        final HashMap<String,Integer> teamList = new HashMap<String,Integer>();

        Map<String, String> postParam = new HashMap<String, String>();
        postParam.put("operator", "userUpdate");
        postParam.put("userName", user.getName());
        postParam.put("region", user.getZone());
        postParam.put("team", user.getTeam());

        JSONObject jsonBody = new JSONObject(postParam);

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST, mUrl, jsonBody, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d(TAG, "onResponse: " + response);



                for (int i = 0; i < response.length(); i++) {
                    JSONObject jsonobject = null;
                    try {
                        jsonobject = response.getJSONObject(i);

                        String team = jsonobject.getString("teamID");
                        Integer score = Integer.parseInt(jsonobject.getString("score"));

                        if (jsonobject.getString("name").equals(user.getName())){
                            user.setScore(score);
                        }


                        if (teamList.containsKey(team)){
                            teamList.put(team, teamList.get(team) + score);
                        }else{
                            teamList.put(team, score);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                mTeamList = teamList;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error);
            }
        });

        Log.d(TAG, "post: " + jsonObjReq);

        jsonObjReq.setTag(TAG);
        // Add the request to the RequestQueue.
        mQueue.add(jsonObjReq);
    }


    public class CustomRequest extends JsonRequest<JSONArray> {

        protected static final String PROTOCOL_CHARSET = "utf-8";

        /**
         * Creates a new request.
         *
         * @param method        the HTTP method to use
         * @param url           URL to fetch the JSON from
         * @param requestBody   A {@link String} to post with the request. Null is allowed and
         *                      indicates no parameters will be posted along with request.
         * @param listener      Listener to receive the JSON response
         * @param errorListener Error listener, or null to ignore errors.
         */
        public CustomRequest(int method, String url, String requestBody, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
            super(method, url, requestBody, listener, errorListener);
        }

        /**
         * Creates a new request.
         *
         * @param url           URL to fetch the JSON from
         * @param listener      Listener to receive the JSON response
         * @param errorListener Error listener, or null to ignore errors.
         */
        public CustomRequest(String url, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
            super(Method.GET, url, null, listener, errorListener);
        }

        /**
         * Creates a new request.
         *
         * @param method        the HTTP method to use
         * @param url           URL to fetch the JSON from
         * @param listener      Listener to receive the JSON response
         * @param errorListener Error listener, or null to ignore errors.
         */
        public CustomRequest(int method, String url, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
            super(method, url, null, listener, errorListener);
        }

        /**
         * Creates a new request.
         *
         * @param method        the HTTP method to use
         * @param url           URL to fetch the JSON from
         * @param jsonRequest   A {@link JSONArray} to post with the request. Null is allowed and
         *                      indicates no parameters will be posted along with request.
         * @param listener      Listener to receive the JSON response
         * @param errorListener Error listener, or null to ignore errors.
         */
        public CustomRequest(int method, String url, JSONArray jsonRequest, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
            super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener, errorListener);
        }

        /**
         * Creates a new request.
         *
         * @param method        the HTTP method to use
         * @param url           URL to fetch the JSON from
         * @param jsonRequest   A {@link JSONObject} to post with the request. Null is allowed and
         *                      indicates no parameters will be posted along with request.
         * @param listener      Listener to receive the JSON response
         * @param errorListener Error listener, or null to ignore errors.
         */
        public CustomRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
            super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener, errorListener);
        }

        /**
         * Constructor which defaults to <code>GET</code> if <code>jsonRequest</code> is
         * <code>null</code>, <code>POST</code> otherwise.
         * <p>
         * // @see #MyjsonPostRequest(int, String, JSONArray, Listener, ErrorListener)
         */
        public CustomRequest(String url, JSONArray jsonRequest, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
            this(jsonRequest == null ? Method.GET : Method.POST, url, jsonRequest, listener, errorListener);
        }

        /**
         * Constructor which defaults to <code>GET</code> if <code>jsonRequest</code> is
         * <code>null</code>, <code>POST</code> otherwise.
         * <p>
         * // @see #MyjsonPostRequest(int, String, JSONObject, Listener, ErrorListener)
         */
        public CustomRequest(String url, JSONObject jsonRequest, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
            this(jsonRequest == null ? Method.GET : Method.POST, url, jsonRequest, listener, errorListener);
        }

        @Override
        protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                return Response.success(new JSONArray(jsonString), HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }
    }
}
