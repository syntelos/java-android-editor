/*
 * Syntelos ENA
 * Copyright (C) 2018, John Pritchard, Syntelos
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package syntelos.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;

import java.io.File;

/**
 * 
 * 
 * 
 */
public abstract class Syntelos
    extends android.app.Activity
{

    protected final static String LOG_TAG = Reference.ROOT;

    protected final static void LI(String fmt, Object... args){

	final String m = String.format(fmt,args);

	Log.i(LOG_TAG,m);
    }
    protected final static void LI(Throwable t, String fmt, Object... args){

	final String m = String.format(fmt,args);

	Log.i(LOG_TAG,m,t);
    }
    protected final static void LD(String fmt, Object... args){

	final String m = String.format(fmt,args);

	Log.d(LOG_TAG,m);
    }
    protected final static void LD(Throwable t, String fmt, Object... args){

	final String m = String.format(fmt,args);

	Log.d(LOG_TAG,m,t);
    }
    protected final static void LE(String fmt, Object... args){

	final String m = String.format(fmt,args);

	Log.e(LOG_TAG,m);
    }
    protected final static void LE(Throwable t, String fmt, Object... args){

	final String m = String.format(fmt,args);

	Log.e(LOG_TAG,m,t);
    }


    /*
     * 
     */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String WRITE_MEDIA_STORAGE = "android.permission.WRITE_MEDIA_STORAGE";

    private static String[] PERMISSIONS_STORAGE_EXT = {
        WRITE_EXTERNAL_STORAGE
    };
    private static String[] PERMISSIONS_STORAGE_MED = {
        WRITE_MEDIA_STORAGE
    };


    protected Reference reference;


    public Syntelos(){
	super();
    }



    public abstract void open();

    public abstract void open(Uri u);

    public abstract void save();


    protected void checkStoragePermissions(){

	final int p_ext = this.checkSelfPermission(WRITE_EXTERNAL_STORAGE);

	if (PackageManager.PERMISSION_GRANTED != p_ext) {

	    this.requestPermissions(PERMISSIONS_STORAGE_EXT,
				    REQUEST_EXTERNAL_STORAGE);
	}

	final int p_med = this.checkSelfPermission(WRITE_MEDIA_STORAGE);

	if (PackageManager.PERMISSION_GRANTED != p_med) {

	    this.requestPermissions(PERMISSIONS_STORAGE_MED,
				    REQUEST_EXTERNAL_STORAGE);
	}
    }
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

	Reference.Register(this);
    }
    @Override
    public void onBackPressed()
    {
	finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu)
    {
        menu.findItem(R.id.save).setVisible (true);
        menu.findItem(R.id.open).setVisible (true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()){
	case android.R.id.home:
	    onBackPressed();
	    break;
	case R.id.open:
	    ask();
	    break;
	case R.id.save:
	    save();
	    break;
	}
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {

        if (0 == requestCode && RESULT_OK == resultCode){

            open(data.getData());
        }
    }

    @Override
    public void onRestoreInstanceState (Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);


	open();
    }

    protected void ask(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.file);

        builder.setPositiveButton(R.string.open, new DialogInterface.OnClickListener(){
		public void onClick(DialogInterface dialog, int id){
		    switch (id){
		    case DialogInterface.BUTTON_POSITIVE:
			EditText text =
			    (EditText) ((Dialog) dialog).findViewById(1);

			String name = text.getText().toString();

			if (!name.isEmpty()){

			    try {
				Syntelos.this.reference = new Reference(Syntelos.this.reference,name);

				open();
			    }
			    catch (RuntimeException exc){

				Syntelos.LD(exc,"Error creating reference to '%s'.",name);
			    }
			}
		    }
		}
	    });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
		public void onClick(DialogInterface dialog, int id){}
	    });


	Context context = builder.getContext();
	EditText text = new EditText(context);
	{
	    text.setId(1);
	    text.setText("");
	}
	AlertDialog dialog = builder.create();
	{
	    dialog.setView(text, 30, 0, 30, 0);
	    dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	    dialog.show();
	}
    }
}
