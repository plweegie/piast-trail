/* MIT License

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

import android.location.Location;

/**
 *
 * This class is a basic component of the model layer.
 * Because the app stores the header drawables and relevant strings locally,
 * private fields refer to resource IDs, except for strings used to communicate
 * with Wikimedia API
 */
public class Visitable {
    
    private int mPlaceNameResId;
    private int mImgResourceId;
    private String mDetails;
    private boolean mVisited;
    private Location mLocation;
    private String mWikiUrl;
    
    public Visitable(int name, int imgId, String details) {
        mPlaceNameResId = name;
        mImgResourceId = imgId;
        mDetails = details;
        mVisited = false;
        mLocation = new Location("");
    }
    
    public int getPlaceNameResId() {
        return mPlaceNameResId;
    }
    
    public int getImgResourceId() {
        return mImgResourceId;
    }
    
    public String getDetails() {
        return mDetails;
    }

    public boolean isVisited() {
        return mVisited;
    }

    public void setVisited(boolean visited) {
        mVisited = visited;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(double lat, double lon) {
        mLocation.setLatitude(lat);
        mLocation.setLongitude(lon);
    }

    public String getWikiUrl() {
        return mWikiUrl;
    }
    
    public void setWikiUrl(String wikiUrl) {
        mWikiUrl = wikiUrl;
    }
}
