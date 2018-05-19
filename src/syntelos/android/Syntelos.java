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
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.widget.TextView;
import static android.widget.TextView.BufferType;

import java.io.File;

/**
 * 
 * 
 * @author syntelos
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
    protected TextView history;
    protected EditText editor;
    protected android.os.AsyncTask bgtask;


    public Syntelos(){
	super();
    }


    public void eval(){
    }
    public void view(){
    }
    public void open(Reference r){

	if (null != r){

	    this.reference = r;

	    setTitle(r.getFilename());

	    if (null == this.bgtask){
		try {
		    this.checkStoragePermissions();

		    Reference.Reader reader = this.reference.reader(this);
		    {
			this.bgtask = reader;
		    }
		    reader.execute(r);
		}
		catch (Exception exc){

		    LE(exc,"Error fetching '%s'.",r.toString());
		}
	    }
	    else {

		LE("Found BGTASK when opening '%s'.",r.toString());
	    }
	}
	else {
	    pick();
	}
    }
    public void open(Uri u){

	if (null != u){
	    try {
		Reference re = new Reference(u);

		open(re);
	    }
	    catch (RuntimeException exc){

		LE(exc,"Error constructing reference from '%s'.",u.toString());
	    }
	}
    }
    public void open(Intent it){

	if (null != it){

	    this.open(it.getData());
	}
    }
    public void save(Reference r){

	if (null != r){

	    this.reference = r;

	    this.setTitle(r.getFilename());

	    this.save();
	}
    }
    public void save(){

	if (null != this.reference){

	    if (null == this.bgtask){
		try {
		    this.checkStoragePermissions();

		    Reference.Writer writer = this.reference.writer(this);
		    {
			this.bgtask = writer;
		    }
		    writer.execute(this.reference);
		}
		catch (Exception exc){

		    LE(exc,"Error storing '%s'.",this.reference.toString());
		}
	    }
	    else {

		LE("Found BGTASK when saving '%s'.",this.reference.toString());
	    }
	}
    }
    public void clear(){

	setTitle(Reference.ROOT);

	EditText editor = this.editor;
	if (null != editor){

	    editor.getText().clear();
	}
    }

    protected void onPostExecute(String result){

	EditText target = this.editor;
	if (null != target){

	    target.setText(result,BufferType.EDITABLE);

	    target.requestFocus();
	}

	this.bgtask = null;
    }

    protected void onPostExecute(){

	this.bgtask = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

	LI("onCreate");

	Reference.Register(this);
    }

    @Override
    protected void onStart(){
	super.onStart();

	LI("onStart");
    }

    @Override
    protected void onRestart(){
	super.onRestart();

	LI("onRestart");
    }

    @Override
    protected void onResume(){
	super.onResume();

	LI("onResume");
    }

    @Override
    protected void onPause(){
	super.onPause();

	LI("onPause");
    }

    @Override
    protected void onStop(){
	super.onStop();

	LI("onStop");
    }

    @Override
    protected void onDestroy(){
	super.onDestroy();

	LI("onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
	super.onConfigurationChanged(newConfig);

	LI("onConfigurationChanged");
    }
    @Override
    public void onAttachFragment(Fragment fragment){

	LI("onAttachFragment");
    }
    @Override
    public void onContentChanged(){

	LI("onContentChanged");
    }

    @Override
    public void onBackPressed()
    {
	LI("onBackPressed");

	clear();

	finishAndRemoveTask();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {

        if (0 == requestCode && RESULT_OK == resultCode && null != data){

	    Uri uri = data.getData();

	    if (null != uri){

		LI("onActivityResult open('%s')",uri);

		open(uri);
	    }
        }
    }

    @Override
    public void onRestoreInstanceState (Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

	LI("onRestoreInstanceState");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()){
	case android.R.id.home:
	    onBackPressed();
	    break;
	case R.id.view:
	    view();
	    break;
	case R.id.name:
	    name();
	    break;
	case R.id.open:
	    pick();
	    break;
	case R.id.save:
	    save();
	    break;
	case R.id.eval:
	    eval();
	    break;
	}
        return true;
    }


    protected final void checkStoragePermissions(){

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
    /**
     * Open file picker
     */
    protected final void pick(){

	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	intent.setType("text/plain");
	startActivityForResult(Intent.createChooser(intent, "Open"), 0);
    }

    protected final boolean isNameLive(){
	boolean isRefNull = (null == this.reference);
	boolean isEditorLive = false;
	{
	    EditText editor = this.editor;
	    isEditorLive = (null != editor && (0 < editor.getText().length()));
	}
	return (isRefNull && isEditorLive);
    }

    protected final void name(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.file);

        builder.setPositiveButton(R.string.open, new DialogInterface.OnClickListener(){
		public void onClick(DialogInterface dialog, int id){
		    switch (id){
		    case DialogInterface.BUTTON_POSITIVE:
			EditText input =
			    (EditText) ((Dialog) dialog).findViewById(1);

			String name = input.getText().toString();

			if (!name.isEmpty()){

			    if (Syntelos.this.isNameLive()){
				try {

				    save(new Reference(name));
				}
				catch (RuntimeException exc){

				    Syntelos.LD(exc,"Error creating reference to '%s'.",name);
				}
			    }
			    else {
				try {

				    open(new Reference(Syntelos.this.reference,name));
				}
				catch (RuntimeException exc){

				    Syntelos.LD(exc,"Error creating reference to '%s'.",name);
				}
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
