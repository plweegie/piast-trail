
package com.example.android.piasttrail.utils;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 *
 * @author jan
 */
public final class QueryUtils {
    
    public static final String LOG_TAG = QueryUtils.class.getSimpleName();
    
    /*
     * This class is only meant to hold static
     * variables and methods - make constructor private
     */
    private QueryUtils() {
    }
    
    /**
     * Query the wiki API for the relevant article URL
     */
    public static String fetchPlaceNameUrl(String requestUrl) {

        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        String placeNameUrl = parseJSON(jsonResponse);

        return placeNameUrl;
    }
    
    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }
    
    /**
     * Make an HTTP request to the given URL and return a String as the
     * response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the JSON results for that page.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }
    
    /**
     * Convert the InputStream into a String which contains the whole
     * JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        
        StringBuilder output = new StringBuilder();
        if(inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while(line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
    
    //Convert JSON response to String representing full URL of wiki article
    public static String parseJSON(String placeInfoJSON) {
        
        if (TextUtils.isEmpty(placeInfoJSON)) {
            return null;
        }
        
        //Initialize an empty string to built output upon
        String wikiText = "";
        
        try {
            
            JSONObject wikiResponse = new JSONObject(placeInfoJSON);
            JSONObject wikiQuery = wikiResponse.getJSONObject("query");
            JSONObject wikiPages = wikiQuery.getJSONObject("pages");
            
            Iterator<String> pagesKeys = wikiPages.keys();
            String pageName = pagesKeys.next();
            JSONObject wikiPlace = wikiPages.optJSONObject(pageName);
            wikiText = wikiPlace.getString("fullurl");
            
        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the JSON results", e);
        }
        
        return wikiText;
    }
}
