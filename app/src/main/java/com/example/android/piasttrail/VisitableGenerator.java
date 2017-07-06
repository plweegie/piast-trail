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
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for initializing and maintaining
 * the list of places used by the app.
 */
public class VisitableGenerator {
    
    private static VisitableGenerator sGenerator;
    
    private List<Visitable> mPlacesList;
    
    public static VisitableGenerator get(Context context) {
        if (sGenerator == null) {
            sGenerator = new VisitableGenerator(context);
        }
        return sGenerator;
    }
    
    private VisitableGenerator(Context context) {
        mPlacesList = new ArrayList<>();
        
        mPlacesList.add(new Visitable(R.string.name_poznan, R.drawable.poznan, R.string.mon_poznan));
        mPlacesList.add(new Visitable(R.string.name_lednica, R.drawable.lednica, R.string.name_lednica));
        mPlacesList.add(new Visitable(R.string.name_gniezno, R.drawable.gniezno, R.string.mon_gniezno));
        mPlacesList.add(new Visitable(R.string.name_trzemeszno, R.drawable.trzemeszno, R.string.mon_trzemeszno));
        mPlacesList.add(new Visitable(R.string.name_mogilno, R.drawable.mogilno, R.string.mon_mogilno));
        mPlacesList.add(new Visitable(R.string.name_strzelno, R.drawable.strzelno, R.string.mon_strzelno));
        mPlacesList.add(new Visitable(R.string.name_kruszwica, R.drawable.kruszwica, R.string.mon_kruszwica));
        mPlacesList.add(new Visitable(R.string.name_biskupin, R.drawable.biskupin, R.string.mon_biskupin));
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
}
