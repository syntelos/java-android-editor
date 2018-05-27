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
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import static android.widget.TextView.BufferType;

import java.io.File;

import static syntelos.android.Reference.Post.Status.*;

/**
 * 
 * 
 * @author syntelos
 */
public abstract class Syntelos
    extends android.app.Activity
    implements android.text.TextWatcher
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

    /**
     * An enum and a simple linear state list.
     */
    public static enum State {
	EMPTY,
	CLEAN,
	DIRTY,
	POST;

	private State p;


	public boolean is(State s){

	    return (s == this);
	}
	public boolean isnot(State s){

	    return (s != this);
	}
	private boolean has(State s){

	    if (s == this){

		return true;
	    }
	    else {
		State p = this.p;
		while (null != p){

		    if (s == p){
			return true;
		    }
		    else if (p == p.p){
			throw new IllegalStateException();
		    }
		    else {
			p = p.p;
		    }
		}
		return false;
	    }
	}
	private void discard(State s){

	    if (null == s){

		throw new IllegalArgumentException();
	    }
	    else if (s != this){

		State x = this, y;

		while (null != x){
   		
		    y = x;
       	
		    x = x.p;

		    if (s == x){

			y.p = x.p;
			x.p = null;

			break;
		    }
		}
	    }
	}
	public State clear(){
	    for (State s : values()){

		s.p = null;
	    }
	    return EMPTY;
	}
	/**
	 * Inclusive combination ensures that the argument is present
	 * <pre>
	 * state = state.push(S)
	 * </pre>
	 * and the list is not cyclic.
	 */
	public State push(State s){

	    if (null == s){

		throw new IllegalArgumentException();
	    }
	    else if (s != this){

		if (this.has(s)){

		    this.discard(s);
		}

		if (this != s && this != s.p){

		    s.p = this;
		}
		else {
		    throw new IllegalStateException();
		}
	    }
	    return s;
	}
	/**
	 * Exclusive combination ensures that the argument is present
	 * top
	 * <pre>
	 * state = state.push(S)
	 * </pre>
	 * and the list is not cyclic.
	 */
	public State pop(State s){

	    if (null == s){

		throw new IllegalArgumentException();
	    }
	    else if (s == this){

		State p = this.p;
		{
		    this.p = null;
		}
		return p;
	    }
	    else if (this.has(s)){

		this.discard(s);

		return this;
	    }
	    else {

		throw new IllegalStateException(this.toString()+" pop("+s.toString()+')');
	    }
	}
    };


    /*
     * 
     */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static final String PERM_WRITE_EXTERNAL = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String PERM_WRITE_MEDIA = "android.permission.WRITE_MEDIA_STORAGE";
    private static final String PERM_MANAGE_DOCUMENTS = "android.permission.MANAGE_DOCUMENTS";

    private static String[] PERMISSIONS_STORAGE_EXT = {
        PERM_WRITE_EXTERNAL
    };
    private static String[] PERMISSIONS_STORAGE_MED = {
        PERM_WRITE_MEDIA
    };
    private static String[] PERMISSIONS_STORAGE_DOC = {
        PERM_MANAGE_DOCUMENTS
    };


    protected Reference reference;
    protected TextView history;
    protected EditText editor;

    protected State state = State.EMPTY;

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

	    checkBg("opening");

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

	    this.save();
	}
    }
    public void save(){

	if (null != this.reference){

	    checkBg("saving");
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
    }
    public void clear(){

	setTitle(Reference.ROOT);

	EditText editor = this.editor;
	if (null != editor){

	    editor.getText().clear();
	}

	checkBg("clearing");

	this.state = this.state.clear();
    }

    protected void checkBg(String when){

	android.os.AsyncTask bg = this.bgtask;

	if (null != bg){

	    this.bgtask = null;

	    if (bg.cancel(true)){

		LE("Found cancelled BGTASK (%s) when %s '%s'.",this.bgtask,when,this.reference.toString());
	    }
	    else {

		LE("Found live BGTASK (%s) when %s '%s'.",this.bgtask,when,this.reference.toString());
	    }
	}
    }

    /**
     * @see android.text.TextWatcher
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after){
    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count){

	if (this.state.is(State.POST)){

	    this.state = this.state.push(State.CLEAN);

	}
	else if (this.state.isnot(State.DIRTY)){

	    this.state = this.state.push(State.DIRTY);

	    invalidateOptionsMenu();
	}

	//LI("onTextChanged [%s]",this.state);
    }
    @Override
    public void afterTextChanged(Editable s){
    }

    protected void onPostReader(Reference.Post.Read r){

	switch(r.status){

	case SUCCESS:

	    String result = r.text;

	    //LI("onPostReader [%s]",this.state);

	    this.state = this.state.push(State.POST);

	    //LI("onPostReader [%s]",this.state);

	    EditText target = this.editor;
	    if (null != target){

		target.setText(result,BufferType.EDITABLE);

		target.requestFocus();
	    }

	    this.bgtask = null;

	    this.state = this.state.pop(State.POST);

	    //LI("onPostReader [%s]",this.state);

	    setTitle(this.reference.getFilename());

	    invalidateOptionsMenu();
	    break;

	case FAILURE:
	    break;
	}
    }

    protected void onPostWriter(Reference.Post.Write w){

	switch(w.status){

	case SUCCESS:

	    //LI("onPostExecute [%s]",this.state);

	    this.bgtask = null;

	    this.state = this.state.push(State.CLEAN);

	    //LI("onPostExecute [%s]",this.state);

	    setTitle(this.reference.getFilename());

	    invalidateOptionsMenu();
	    break;

	case FAILURE:
	    break;
	}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

	//LI("onCreate [%s]",this.state);

	Reference.Register(this);
    }

    @Override
    protected void onStart(){
	super.onStart();

	//LI("onStart [%s]",this.state);
    }

    @Override
    protected void onRestart(){
	super.onRestart();

	//LI("onRestart [%s]",this.state);
    }

    @Override
    protected void onResume(){
	super.onResume();

	//LI("onResume [%s]",this.state);
    }

    @Override
    protected void onPause(){
	super.onPause();

	//LI("onPause [%s]",this.state);
    }

    @Override
    protected void onStop(){
	super.onStop();

	//LI("onStop [%s]",this.state);
    }

    @Override
    protected void onDestroy(){
	super.onDestroy();

	//LI("onDestroy [%s]",this.state);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
	super.onConfigurationChanged(newConfig);

	//LI("onConfigurationChanged [%s]",this.state);
    }
    @Override
    public void onAttachFragment(Fragment fragment){

	//LI("onAttachFragment [%s]",this.state);
    }
    @Override
    public void onContentChanged(){

	//LI("onContentChanged [%s]",this.state);
    }

    @Override
    public void onBackPressed()
    {
	//LI("onBackPressed [%s]",this.state);

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

		//LI("onActivityResult open('%s') [%s]",uri,this.state);

		open(uri);
	    }
        }
    }

    @Override
    public void onRestoreInstanceState (Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

	//LI("onRestoreInstanceState [%s]",this.state);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()){
	case android.R.id.home:
	    onBackPressed();
	    break;
	case R.id.view:
	    /*
	     * Using the first action icon as file status
	     */
	    switch(this.state){

	    case DIRTY:
		save();
		break;

	    default:
		name();
		break;
	    }
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

	int p_ext = this.checkSelfPermission(PERM_WRITE_EXTERNAL);

	if (PackageManager.PERMISSION_GRANTED != p_ext) {

	    this.requestPermissions(PERMISSIONS_STORAGE_EXT,
				    REQUEST_EXTERNAL_STORAGE);
	}

	int p_med = this.checkSelfPermission(PERM_WRITE_MEDIA);

	if (PackageManager.PERMISSION_GRANTED != p_med) {

	    this.requestPermissions(PERMISSIONS_STORAGE_MED,
				    REQUEST_EXTERNAL_STORAGE);
	}

	int p_doc = this.checkSelfPermission(PERM_MANAGE_DOCUMENTS);

	if (PackageManager.PERMISSION_GRANTED != p_doc) {

	    this.requestPermissions(PERMISSIONS_STORAGE_DOC,
				    REQUEST_EXTERNAL_STORAGE);
	}
    }
    /**
     * Open file picker
     */
    protected final void pick(){

	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	intent.setType("text/plain");
	intent.addCategory(Intent.CATEGORY_OPENABLE);
	intent.putExtra(Intent.EXTRA_LOCAL_ONLY,Boolean.TRUE);
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
