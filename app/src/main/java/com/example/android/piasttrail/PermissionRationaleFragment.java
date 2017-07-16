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
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/* 
This Dialog gives rationale for location request if user initially denied the 
permission.
*/

public class PermissionRationaleFragment extends DialogFragment {
    
    public interface PermissionRationaleListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }
    
    PermissionRationaleListener mListener;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.permission_rationale)
               .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                    @Override   
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(PermissionRationaleFragment.this);
                    }
                });
        return builder.create();
    }
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (PermissionRationaleListener) context;
    }
}