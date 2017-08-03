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
import android.content.DialogInterface;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.widget.*;
import android.view.View;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import com.example.android.piasttrail.utils.PictureUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/* This fragment is hosted by the main activity */

public class PlaceListFragment extends Fragment implements
        PermissionRationaleFragment.PermissionRationaleListener {
    
    private RecyclerView mPlaceRecyclerView;
    private PlaceGridAdapter mAdapter;
    private List<Visitable> mPlaces;
    private GoogleApiClient mClient;
    private Location mLocation;
    private boolean mWasLocationFixed;
    
    private static final String PERMISSION_RATIONALE_DIALOG = "PermissionRationaleDialog";

    private static final String[] LOCATION_PERMISSIONS = new String[]{
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,};
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    private static final int REQUEST_ERROR = 1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_list, parent, false);
        
        mPlaceRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mPlaceRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mPlaceRecyclerView.setHasFixedSize(true);
        mLocation = new Location("");
        mWasLocationFixed = false;
        
        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        getActivity().invalidateOptionsMenu();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
        
        updateUI();
        return view;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        getActivity().invalidateOptionsMenu();
        mClient.connect();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        
        //checking if Play Store app installed on device
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int errorCode = apiAvailability.isGooglePlayServicesAvailable(getActivity());

        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = apiAvailability
                    .getErrorDialog(getActivity(), errorCode, REQUEST_ERROR,
                            new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            getActivity().finish();
                        }
                    });
            errorDialog.show();
        }
    }
    
    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_place_list, menu);
        
        MenuItem searchItem = menu.findItem(R.id.action_locate);
        searchItem.setEnabled(mClient.isConnected());
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_locate:
                if (hasLocationPermission()) {
                    findCoords();
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        getActivity(), LOCATION_PERMISSIONS[0])) {
                    DialogFragment dialog = new PermissionRationaleFragment();
                    dialog.setTargetFragment(PlaceListFragment.this, 0);
                    dialog.show(getFragmentManager(), PERMISSION_RATIONALE_DIALOG);

                } else {
                    requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS);
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
                        mLocation.setLatitude(lat);
                        mLocation.setLongitude(lon);
                        mWasLocationFixed = true;
                        
                        //Sort the order in which places are shown
                        //depending on how far they are from us (= by distance ascending)
                        Collections.sort(mPlaces, new Comparator<Visitable>() {
                            @Override
                            public int compare(Visitable a, Visitable b) {
                                Location aLoc = a.getLocation();
                                Location bLoc = b.getLocation();
                                return (int) aLoc.distanceTo(mLocation) - (int) bLoc.distanceTo(mLocation);
                            }
                        });
                        mAdapter.setPlaces(mPlaces);
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }
    
    private boolean hasLocationPermission() {
        int result = ContextCompat
                .checkSelfPermission(getActivity(), LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    
    private void updateUI() {
        mPlaces = VisitableGenerator.get(getActivity()).getPlaces();
        
        // the visited (or not) status of places is persisted in preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        for (int i = 0; i < mPlaces.size(); i++) {
            Visitable place = mPlaces.get(i);
            String placeName = getString(place.getPlaceNameResId());

            if (prefs.contains(placeName)) {
                place.setVisited(prefs.getBoolean(placeName, false));
            }
        }
        
        if (mAdapter == null) {
            mAdapter = new PlaceGridAdapter(mPlaces);
            mAdapter.setHasStableIds(true);
            mPlaceRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setPlaces(mPlaces);
            mAdapter.notifyDataSetChanged();
        }
    }
    
    private class PlaceHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        
        private TextView mPlaceNameTextView;
        private TextView mVisitedTextView;
        private TextView mDistanceTextView;
        private ImageView mPlaceImageView;
        private ImageView mIconImageView;
        private View mBackground;
        private Visitable mVisitable;
        
        public PlaceHolder(LayoutInflater inflater, ViewGroup parent, int layoutResId) {
            super(inflater.inflate(layoutResId, parent, false));
            itemView.setOnClickListener(this);
            
            mPlaceNameTextView = (TextView) itemView.findViewById(R.id.place_name);
            mPlaceImageView = (ImageView) itemView.findViewById(R.id.place_photo);
            mDistanceTextView = (TextView) itemView.findViewById(R.id.place_distance);
            mBackground = itemView.findViewById(R.id.card_background);
            mVisitedTextView = (TextView) itemView.findViewById(R.id.visited_tv);
            mIconImageView = (ImageView) itemView.findViewById(R.id.check_visited);
        }
        
        @Override
        public void onClick(View view) {
            Intent intent = PlaceDetailsActivity.newIntent(getActivity(), getAdapterPosition());
            startActivity(intent);
        }
        
        public void bind(Visitable place) {
            mVisitable = place;
            mPlaceNameTextView.setText(mVisitable.getPlaceNameResId());
        }
    }
    
    private class PlaceGridAdapter extends RecyclerView.Adapter<PlaceHolder> {
        
        private List<Visitable> mPlaces;

        public PlaceGridAdapter(List<Visitable> places) {
            mPlaces = places;
        }

        @Override
        public PlaceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            return new PlaceHolder(inflater, parent, R.layout.grid_item);
        }

        @Override
        public void onBindViewHolder(PlaceHolder holder, int position) {
            
            Visitable place = mPlaces.get(position);
            holder.mPlaceNameTextView.setText(place.getPlaceNameResId());
            
            int imgResourceId = place.getImgResourceId();
            Bitmap imgBitmap = PictureUtils.decodeBitmapFromResource(getResources(),
                    imgResourceId, 100, 100);
            holder.mPlaceImageView.setImageBitmap(imgBitmap);
            
            LayerDrawable ld = (LayerDrawable) holder.mBackground.getBackground();
            
            //when the place has been visited, background and
            //two addiional views are overlaid on top of card
            
            boolean visited = place.isVisited();
            ld.getDrawable(1).setAlpha(visited ? 200 : 0);
            holder.mBackground.setBackground(ld);
            
            holder.mIconImageView.setVisibility(visited ? View.VISIBLE : View.GONE);
            holder.mVisitedTextView.setVisibility(visited ? View.VISIBLE : View.GONE);
            holder.mDistanceTextView.setVisibility(View.GONE);
            
            
            if (mWasLocationFixed) {
                
                //in kilometers
                int distanceToAttraction = 
                        (int) place.getLocation().distanceTo(mLocation) / 1000;
                holder.mDistanceTextView.setText(distanceToAttraction + " km");
                holder.mDistanceTextView.setVisibility(View.VISIBLE);
            }
        }
        
        @Override
        public int getItemCount() {
            return mPlaces.size();
        }
        
        //getItemId(int) needs to be implemented because setHasFixedSize(true)
        //was called on RecyclerView
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        public void setPlaces(List<Visitable> places) {
            mPlaces = places;
        }
    }
}
