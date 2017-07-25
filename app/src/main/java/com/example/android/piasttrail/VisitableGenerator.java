/*MIT License

Copyright (c) 2017 Jan K Szymanski

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.example.android.piasttrail;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.example.android.piasttrail.utils.QueryUtils;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is responsible for initializing and maintaining
 * the list of places used by the app.
 */
public class VisitableGenerator {
    
    private static final String WIKI_REQUEST_URL = "https://pl.wikipedia.org/w/api.php";
    private static final String LOG_TAG = "VisitableGenerator";

    private static VisitableGenerator sGenerator;
    
    private List<Visitable> mPlacesList;
    private Uri mWikiUri;
    
    public static VisitableGenerator get(Context context) {
        if (sGenerator == null) {
            sGenerator = new VisitableGenerator(context);
        }
        return sGenerator;
    }
    
    private VisitableGenerator(Context context) {
        Resources resources = context.getResources();
        mPlacesList = new ArrayList<>();
        
        mPlacesList.add(new Visitable(R.string.name_poznan, R.drawable.poznan,
                resources.getString(R.string.mon_poznan)));
        mPlacesList.add(new Visitable(R.string.name_lednica, R.drawable.lednica,
                resources.getString(R.string.name_lednica)));
        mPlacesList.add(new Visitable(R.string.name_gniezno, R.drawable.gniezno,
                resources.getString(R.string.mon_gniezno)));
        mPlacesList.add(new Visitable(R.string.name_trzemeszno, R.drawable.trzemeszno,
                resources.getString(R.string.mon_trzemeszno)));
        mPlacesList.add(new Visitable(R.string.name_mogilno, R.drawable.mogilno,
                resources.getString(R.string.mon_mogilno)));
        mPlacesList.add(new Visitable(R.string.name_strzelno, R.drawable.strzelno,
                resources.getString(R.string.mon_strzelno)));
        mPlacesList.add(new Visitable(R.string.name_kruszwica, R.drawable.kruszwica,
                resources.getString(R.string.mon_kruszwica)));
        mPlacesList.add(new Visitable(R.string.name_biskupin, R.drawable.biskupin,
                resources.getString(R.string.mon_biskupin)));
        
        Uri baseUri = Uri.parse(WIKI_REQUEST_URL);
        Uri.Builder builder = baseUri.buildUpon();
        
        //make an API call to pl.wikipedia.org
        //to receive full URLs and lat-lon coordinates for given articles
        builder.appendQueryParameter("action", "query")
                    .appendQueryParameter("prop", "coordinates|info")
                    .appendQueryParameter("inprop", "url");
        
        mWikiUri = builder.build();
        
        ConnectivityManager connManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        
        if (isConnected) {
            new GetWikiInfoTask().execute(mPlacesList.toArray(new Visitable[mPlacesList.size()]));
        } else {
            Toast.makeText(context, resources.getString(R.string.empty_placeholder),
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    public List<Visitable> getPlaces() {
        return mPlacesList;
    }
    
    public Visitable getPlace(int position) {
        return mPlacesList.get(position);
    }
    
    public void updateVisited(int position, boolean visited) {
        mPlacesList.get(position).setVisited(visited);
    }
    
    /**
     * The AsyncTask takes care of fetching the coordinates and wikipedia URLs
     * for all attractions when the attraction list is loaded
     */
    private class GetWikiInfoTask extends AsyncTask<Visitable, Void, List<JSONObject>> {
        
        @Override
        protected List<JSONObject> doInBackground(Visitable... places) {
            
            List<JSONObject> result = new ArrayList<>();
            
            for (Visitable place: places) {
                Uri.Builder queryBuilder = mWikiUri.buildUpon();
                queryBuilder.appendQueryParameter("titles", place.getDetails())
                        .appendQueryParameter("format", "json");
                result.add(QueryUtils.fetchPlaceNameUrl(queryBuilder.toString()));
            }
            return result;
        }
        
        @Override
        protected void onPostExecute(List<JSONObject> result) {
            
            String url = "";
            double resultLat = 0.0;
            double resultLon = 0.0;
            
            for (int i = 0; i < result.size(); i++) {
                try {
                    url = result.get(i).getString("fullurl");
                    JSONObject coords = result.get(i).getJSONArray("coordinates")
                            .getJSONObject(0);
                    resultLat = coords.getDouble("lat");
                    resultLon = coords.getDouble("lon");
                }
                catch (JSONException je) {
                    Log.e(LOG_TAG, "Problem parsing the JSON results", je);
                }
                
                mPlacesList.get(i).setWikiUrl(url);
                mPlacesList.get(i).setLocation(resultLat, resultLon);
            }
        }
    }
}
