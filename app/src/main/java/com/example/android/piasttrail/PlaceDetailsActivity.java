        
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

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.piasttrail.utils.PictureUtils;
import com.example.android.piasttrail.utils.QueryUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * This activity displays the place details 
 */
public class PlaceDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<JSONObject>,
        PermissionRationaleFragment.PermissionRationaleListener {
    
    private static final String EXTRA_PLACE_POSITION = "place_position";
    private static final int REQUEST_ERROR = 0;
    private static final int WIKI_LOADER_ID = 1;

    private static final String LOG_TAG = PlaceDetailsActivity.class.getName();
    private static final String WIKI_REQUEST_URL = "https://pl.wikipedia.org/w/api.php";
    private static final String PERMISSION_RATIONALE_DIALOG = "PermissionRationaleDialog";
    
    private static final String[] LOCATION_PERMISSIONS = new String[] {
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    
    private static final float MAP_ZOOM_LEVEL = 10f;
    
    private TextView mBackupEmptyView;
    private ProgressBar mIndicator;
    private ConnectivityManager mConnManager;
    private Context mContext;
    private Resources mResources;
    private GoogleApiClient mClient;
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
        
        mClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        PlaceDetailsActivity.this.invalidateOptionsMenu();
                    }       

                    @Override
                    public void onConnectionSuspended(int i) {
                        
                    }
                })
                .build();
        
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
    public void onStart() {
        super.onStart();
        this.invalidateOptionsMenu();
        mClient.connect();
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
    
    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
    }
        
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.place_details, menu);
        
        MenuItem searchItem = menu.findItem(R.id.action_locate);
        searchItem.setEnabled(mClient.isConnected());
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_locate:
                if (hasLocationPermission()) {
                    findCoords();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            PlaceDetailsActivity.this, LOCATION_PERMISSIONS[0])) {
                        DialogFragment dialog = new PermissionRationaleFragment();
                        dialog.show(getSupportFragmentManager(), PERMISSION_RATIONALE_DIALOG);
                        
                    } else {
                        requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS);
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] permissions,
            int[] grantResults) {
        switch (code) {
            case REQUEST_LOCATION_PERMISSIONS:
                if (hasLocationPermission()) {
                    findCoords();
                }
            default:
                super.onRequestPermissionsResult(code, permissions, grantResults);
        }
    }
    
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS);
    }
    
    //We make the activity self-contained by removing the need for
    //parent fragment to know anything about the intent extras
    public static Intent newIntent(Context packageContext, int id) {
        Intent intent = new Intent(packageContext, PlaceDetailsActivity.class);
        intent.putExtra(EXTRA_PLACE_POSITION, id);
        return intent;
    }
    
    private void findCoords() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);
        
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mClient, request, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        
                        //in kilometers
                        float distanceToAttraction = location.distanceTo(mLocation) / 1000.0f;
                        
                        Toast.makeText(mContext, "You are " + distanceToAttraction
                                + " km from this attraction.", Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private boolean hasLocationPermission() {
        int result = ContextCompat
                .checkSelfPermission(mContext, LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    
    private void updateUI() {
        if (mMap == null) {
            return;
        }
        
        LatLng placePoint = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(placePoint, MAP_ZOOM_LEVEL);
        mMap.animateCamera(update);
    }
    
    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {

        String currentResourceId = getString(mPlace.getDetailsResId());

        Uri baseUri = Uri.parse(WIKI_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        
        //make an API call to pl.wikipedia.org
        //to receive full URL and lat-lon coordinates for given article title
        uriBuilder.appendQueryParameter("action", "query");
        uriBuilder.appendQueryParameter("prop", "coordinates|info");
        uriBuilder.appendQueryParameter("inprop", "url");
        uriBuilder.appendQueryParameter("titles", currentResourceId);
        uriBuilder.appendQueryParameter("format", "json");

        return new PlaceDetailsLoader(PlaceDetailsActivity.this, uriBuilder.toString());
    }
    
    @Override
    public void onLoadFinished(Loader<JSONObject> loader, JSONObject result) {

        mIndicator.setVisibility(View.GONE);
        if (result == null) {
            mBackupEmptyView.setText(R.string.empty_placeholder);
            return;
        }
        
        String url = "";
        double resultLat = 0.0;
        double resultLon = 0.0;
        
        try {
            url = result.getString("fullurl");
            JSONObject coords = result.getJSONArray("coordinates").getJSONObject(0);
            resultLat = coords.getDouble("lat");
            resultLon = coords.getDouble("lon");
        }
        catch (JSONException je) {
            Log.e(LOG_TAG, "Problem parsing the JSON results", je);
        }
        
        mLocation.setLatitude(resultLat);
        mLocation.setLongitude(resultLon);
        mWebView.loadUrl(url);
        updateUI();
    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {
        
        mLocation.setLatitude(0.0);
        mLocation.setLongitude(0.0);

        mWebView.loadUrl(null);
    }

    public static class PlaceDetailsLoader extends AsyncTaskLoader<JSONObject> {

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
        public JSONObject loadInBackground() {

            if (mRequestUrl == null) {
                return null;
            }

            JSONObject result = QueryUtils.fetchPlaceNameUrl(mRequestUrl);
            return result;
        }
    }
}
