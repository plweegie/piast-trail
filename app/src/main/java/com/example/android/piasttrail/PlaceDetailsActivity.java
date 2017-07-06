        
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
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.android.piasttrail.utils.PictureUtils;
import com.example.android.piasttrail.utils.QueryUtils;

/**
 *
 * This activity displays the place details 
 */
public class PlaceDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<String>{
    
    private static final String EXTRA_PLACE_POSITION = "place_position";
    
    private static final int WIKI_LOADER_ID = 1;

    private static final String LOG_TAG = PlaceDetailsActivity.class.getName();
    private static final String WIKI_REQUEST_URL = "https://pl.wikipedia.org/w/api.php";
    
    private TextView mBackupEmptyView;
    private ProgressBar mIndicator;
    private ConnectivityManager mConnManager;
    private Context mContext;
    private Resources mResources;
    
    private ImageView mPlaceImageViewFull;
    private TextView mPlaceCaption;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private CheckBox mVisitedCheckBox;
    private Visitable mPlace;
    private int mPlaceId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_details);
        mContext = this;
        mResources = getResources();
        
        mPlaceId = getIntent().getIntExtra(EXTRA_PLACE_POSITION, -1);
        final VisitableGenerator generator = VisitableGenerator.get(this);
        mPlace = generator.getPlace(mPlaceId);
        
        mPlaceImageViewFull = (ImageView) findViewById(R.id.place_image_full);
        mPlaceCaption = (TextView) findViewById(R.id.place_caption_full);
        mWebView = (WebView) findViewById(R.id.web_view);
        mBackupEmptyView = (TextView) findViewById(R.id.place_backup_empty_view);
        mIndicator = (ProgressBar) findViewById(R.id.indicator);
        mVisitedCheckBox = (CheckBox) findViewById(R.id.visited_check_box);
        
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setMax(100);
        
        mWebView.setWebChromeClient(new WebChromeClient() {
            
            @Override
            public void onProgressChanged(WebView webView, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }
        });
        mWebView.setWebViewClient(new WebViewClient());
        
        Bitmap bitmap = PictureUtils.decodeBitmapFromResource(getResources(),
                mPlace.getImgResourceId(), 400, 400);
        
        mPlaceImageViewFull.setImageBitmap(bitmap);
        mPlaceCaption.setText(mPlace.getPlaceNameResId());
        this.getSupportActionBar().setTitle(mPlace.getPlaceNameResId());
        
        mVisitedCheckBox.setChecked(mPlace.isVisited());
        mVisitedCheckBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                mPlace.setVisited(isChecked);
            }
        });
        
        mConnManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        LoaderManager loaderManager = getSupportLoaderManager();
        
        NetworkInfo activeNetwork = mConnManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        
        if (isConnected) {
            loaderManager.initLoader(WIKI_LOADER_ID, null, this);
        } else {
            mIndicator.setVisibility(View.INVISIBLE);
            mBackupEmptyView.setText(R.string.empty_placeholder);
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        //here we persist the visited (or not) status, to be read by PlaceListFragment
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putBoolean(mResources.getString(mPlace.getPlaceNameResId()), mPlace.isVisited())
                .apply();
    }
    
    //We make the activity self-contained by removing the need for
    //parent fragment to know anything about the intent extras
    public static Intent newIntent(Context packageContext, int id) {
        Intent intent = new Intent(packageContext, PlaceDetailsActivity.class);
        intent.putExtra(EXTRA_PLACE_POSITION, id);
        return intent;
    }
    
    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {

        String currentResourceId = getString(mPlace.getDetailsResId());

        Uri baseUri = Uri.parse(WIKI_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        
        //make an API call to pl.wikipedia.org
        //to receive full URL for given article title
        uriBuilder.appendQueryParameter("action", "query");
        uriBuilder.appendQueryParameter("prop", "info");
        uriBuilder.appendQueryParameter("inprop", "url");
        uriBuilder.appendQueryParameter("titles", currentResourceId);
        uriBuilder.appendQueryParameter("format", "json");

        return new PlaceDetailsLoader(PlaceDetailsActivity.this, uriBuilder.toString());
    }
    
    @Override
    public void onLoadFinished(Loader<String> loader, String result) {

        mIndicator.setVisibility(View.GONE);
        if (result == null) {
            mBackupEmptyView.setText(R.string.empty_placeholder);
            return;
        }
        mWebView.loadUrl(result);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

        mWebView.loadUrl(null);
    }

    public static class PlaceDetailsLoader extends AsyncTaskLoader<String> {

        private final String LOG_TAG = PlaceDetailsLoader.class.getName();

        private String mRequestUrl;

        public PlaceDetailsLoader(Context context, String url) {
            super(context);
            mRequestUrl = url;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public String loadInBackground() {

            if (mRequestUrl == null) {
                return null;
            }

            String result = QueryUtils.fetchPlaceNameUrl(mRequestUrl);
            return result;
        }
    }
}
