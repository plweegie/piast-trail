        
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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 *
 * This activity displays the place details 
 */
public class PlaceDetailsActivity extends AppCompatActivity {
    
    private static final String EXTRA_PLACE_POSITION = "place_position";
    private static final int REQUEST_ERROR = 0;

    private static final String LOG_TAG = PlaceDetailsActivity.class.getName();
    private static final float MAP_ZOOM_LEVEL = 10f;
    
    private TextView mBackupEmptyView;
    private ProgressBar mIndicator;
    private ConnectivityManager mConnManager;
    private Context mContext;
    private Resources mResources;
    private GoogleMap mMap;
    private Location mLocation;
    
    private ImageView mPlaceImageViewFull;
    private TextView mPlaceCaption;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private CheckBox mVisitedCheckBox;
    private SupportMapFragment mMapFragment;
    private Visitable mPlace;
    private int mPlaceId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_details);
        mContext = this;
        mResources = getResources();
        
        mMapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_map);
        
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                mMap = map;
                updateUI();
            }
        });
        
        mPlaceId = getIntent().getIntExtra(EXTRA_PLACE_POSITION, -1);
        final VisitableGenerator generator = VisitableGenerator.get(this);
        mPlace = generator.getPlace(mPlaceId);
        mLocation = new Location("");
        mLocation.set(mPlace.getLocation());
        
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
        mWebView.loadUrl(mPlace.getWikiUrl());
        
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
        
        NetworkInfo activeNetwork = mConnManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        
        if (!isConnected) {
            mIndicator.setVisibility(View.INVISIBLE);
            mBackupEmptyView.setText(R.string.empty_placeholder);
        }
    }
    
    
    @Override
    protected void onResume() {
        super.onResume();
        
        //checking if Play Store app installed on device
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int errorCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = apiAvailability
                    .getErrorDialog(this, errorCode, REQUEST_ERROR,
                            new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    });
            errorDialog.show();
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
    
    private void updateUI() {
        if (mMap == null) {
            return;
        }
        
        LatLng placePoint = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        MarkerOptions placeMarker = new MarkerOptions().position(placePoint);
        
        mMap.clear();
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(placePoint, MAP_ZOOM_LEVEL);
        mMap.animateCamera(update);
        mMap.addMarker(placeMarker);
    }
}
