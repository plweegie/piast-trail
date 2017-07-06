
package com.example.android.piasttrail;

import android.support.v4.app.Fragment;


public class PlaceListActivity extends SingleFragmentActivity {
    
    @Override
    protected Fragment createFragment() {
        return new PlaceListFragment();
    }
}
