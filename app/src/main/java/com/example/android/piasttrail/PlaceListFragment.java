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

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.widget.*;
import android.view.View;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.LayerDrawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.example.android.piasttrail.utils.PictureUtils;
import java.util.List;

/* This fragment is hosted by the main activity */

public class PlaceListFragment extends Fragment {
    
    private RecyclerView mPlaceRecyclerView;
    private PlaceGridAdapter mAdapter;
    private List<Visitable> mPlaces;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_list, parent, false);
        
        mPlaceRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mPlaceRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mPlaceRecyclerView.setHasFixedSize(true);
        
        updateUI();
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
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
        private ImageView mPlaceImageView;
        private ImageView mIconImageView;
        private View mBackground;
        private Visitable mVisitable;
        
        public PlaceHolder(LayoutInflater inflater, ViewGroup parent, int layoutResId) {
            super(inflater.inflate(layoutResId, parent, false));
            itemView.setOnClickListener(this);
            
            mPlaceNameTextView = (TextView) itemView.findViewById(R.id.place_name);
            mPlaceImageView = (ImageView) itemView.findViewById(R.id.place_photo);
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
