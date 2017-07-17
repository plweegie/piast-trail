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

/**
 *
 * This class is a basic component of the model layer.
 * Because the app stores the header drawables and relevant strings locally,
 * private fields all refer to resource IDs.
 */
public class Visitable {
    
    private int mPlaceNameResId;
    private int mImgResourceId;
    private int mDetailsResId;
    private boolean mVisited;
    private double mLatitude;
    private double mLongitude;
    
    public Visitable(int name, int imgId, int details) {
        mPlaceNameResId = name;
        mImgResourceId = imgId;
        mDetailsResId = details;
        mVisited = false;
    }
    
    public int getPlaceNameResId() {
        return mPlaceNameResId;
    }
    
    public int getImgResourceId() {
        return mImgResourceId;
    }
    
    public int getDetailsResId() {
        return mDetailsResId;
    }

    public boolean isVisited() {
        return mVisited;
    }

    public void setVisited(boolean visited) {
        mVisited = visited;
    }
}
