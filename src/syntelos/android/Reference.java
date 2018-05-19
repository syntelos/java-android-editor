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

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Environment;
import android.text.Editable;
import android.widget.EditText;

import java.io.File;

/**
 * 
 */
public final class Reference
    extends java.lang.Object
{
    final static String ROOT = "syntelos";

    private static Syntelos context;

    private static ContentResolver resolver;

    private static File root;

    /**
     * 
     */
    static void Register(Syntelos context){

	Reference.context = context;
	Reference.resolver = context.getContentResolver();

	context.checkStoragePermissions();

	Reference.root = GetRoot(context);
    }
    /**
     * Checks if external storage is available for read and write.
     */
    public static boolean IsExternalStorageMountedWritable() {

	return (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()));
    }
    /**
     * Checks if external storage is available to at least read.
     */
    public static boolean IsExternalStorageMountedReadonly() {

	return (Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState()));
    }
    public static File GetRootExternalStorage(Context context){

	return Environment.getExternalStoragePublicDirectory(ROOT);
    }
    public static File GetRootInternalStorage(Context context){

	return (new File(context.getFilesDir(),ROOT));
    }
    public static File GetRoot(Context context){

	if (IsExternalStorageMountedWritable()){

	    File root = GetRootExternalStorage(context);
	    if ((root.isDirectory() && root.canWrite()) || root.mkdirs()){

		Syntelos.LI("GET-ROOT [EXT] '%s'.",root.getPath());

		return root;
	    }
	}

	File root = GetRootInternalStorage(context);
	if ((root.isDirectory() && root.canWrite()) || root.mkdirs()){

	    Syntelos.LI("GET-ROOT [INT] '%s'.",root.getPath());

	    return root;
	}
	else {
	    throw new IllegalStateException(String.format("Failed to create directory '%s'.",root.getPath()));
	}
    }

    protected final static String CONTENT_Q_COLUMN = "_data";

    protected final static String[] CONTENT_Q_PROJEC = {CONTENT_Q_COLUMN};

    protected final static File Resolve(Uri uri){

	final String scheme = uri.getScheme();

	if ("file".equalsIgnoreCase(scheme)){

	    return new File(uri.getPath());
	}
	else {
	    Cursor cursor = null;
	    try {
		cursor = Reference.resolver.query(uri,CONTENT_Q_PROJEC,null,null,null);

		if (null != cursor && cursor.moveToFirst()){

		    final int cix = cursor.getColumnIndex(CONTENT_Q_COLUMN);
		    if (-1 < cix){

			String path = cursor.getString(cix);

			if (null != path){

			    return new File(path);
			}
		    }
		}

		throw new IllegalArgumentException(String.format("Failed to resolve file for URI '%s'.",uri.toString()));
	    }
	    finally {
		if (null != cursor){

		    cursor.close();
		}		
	    }
	}
    }
    protected final static Uri Resolve(File file){

	return Uri.parse(file.toURI().toString());
    }

    private final static String TXT = "txt";

    private final static String FEXT(String name){

	final int las = (name.length()-1);
	if (0 < las){
	    final int lix = name.lastIndexOf('.');
	    if (0 < lix){

		if ((lix+1) < las){

		    final String fext = name.substring(lix+1);
		    if (!"txt".equals(fext)){

			name = name.substring(0,lix);

			return (name+'.'+TXT);
		    }
		    else {
			return name;
		    }
		}
		else if (lix == las){

		    return (name+TXT);
		}
		else {

		    return (name+'.'+TXT);
		}
	    }
	    else {

		return (name+'.'+TXT);
	    }
	}
	else {
	    throw new IllegalArgumentException();
	}
    }




    private final File file;
    private final Uri uri;


    public Reference(Uri uri){
	super();
	if (null != uri){
	    this.uri = uri;
	    this.file = Reference.Resolve(uri);
	}
	else {
	    throw new IllegalArgumentException();
	}
    }
    public Reference(Reference rel, String path){
	super();
	if (null != path && (!path.isEmpty())){

	    path = FEXT(path);

	    if (null != rel){
		this.file = new File(rel.file.getParentFile(),path);
	    }
	    else {
		this.file = new File(root,path);
	    }
	    this.uri = Reference.Resolve(this.file);
	}
	else {
	    throw new IllegalArgumentException();
	}
    }
    public Reference(String path){
	this(null,path);
    }


    public String getFilename(){
	return this.file.getName();
    }
    public String getFilepath(){
	return this.file.getPath();
    }
    public Reader reader(Syntelos context){

	return new Reader(context);
    }
    public Writer writer(Syntelos context){

	return new Writer(context);
    }


    /**
     * Mixed threading for I/O read task.
     * 
     * @see FileReader
     * @see UriReader
     */
    public final static class Reader
	extends android.os.AsyncTask<Reference,Integer,String>
    {
	private final Syntelos context;

	private final ContentResolver resolver;

	private transient String _string;


	/**
	 * From UI thread
	 */
	public Reader(Syntelos context){
	    super();
	    if (null != context){
		this.context = context;
		this.resolver = context.getContentResolver();
	    }
	    else {
		throw new IllegalArgumentException();
	    }
	}


	/**
	 * From UI thread
	 */
	protected void onPreExecute(){
	}
	/**
	 * From BG thread
	 */
	protected String doInBackground(final Reference... params){

	    final Uri uri = params[0].uri;

	    final StringBuilder strbuf = new StringBuilder();
	    try {
		copy(strbuf,new java.io.InputStreamReader(this.resolver.openInputStream(uri)));
	    }
	    catch (Exception exc){

		Syntelos.LE(exc,"Error reading '%s'.",uri.toString());
	    }
	    return (_string = strbuf.toString());
	}
	/**
	 * From BG thread
	 */
	private void copy(java.lang.StringBuilder strbuf, java.io.Reader in)
	    throws java.io.IOException
	{
	    try {
		char[] chbuf = new char[512];
		int cnt, ofs = 0;
		while (0 < (cnt = in.read(chbuf,0,512))){

		    strbuf.append(chbuf,0,cnt);

		    ofs += cnt;

		    if (isCancelled()){

			break;
		    }
		    else {

			publishProgress(ofs);
		    }
		}
	    }
	    finally {
		in.close();
	    }
	}
	/**
	 * From UI thread
	 */
	protected void onPostExecute(String result){

	    this.context.onPostExecute(result);
	}
    }

    /**
     * Mixed threading for I/O read task.
     */
    public final static class Writer
	extends android.os.AsyncTask<Reference,Float,Void>
    {
	private final Syntelos context;

	private final ContentResolver resolver;

	private final Editable source;

	private transient String _string;


	/**
	 * From UI thread
	 */
	public Writer(Syntelos context){
	    super();
	    if (null != context){
		this.context = context;
		this.resolver = context.getContentResolver();

		final EditText source = context.editor;
		if (null != source){
		    this.source = source.getText();
		}
		else {
		    throw new IllegalArgumentException();
		}
	    }
	    else {
		throw new IllegalArgumentException();
	    }
	}


	/**
	 * From UI thread
	 */
	protected void onPreExecute(){
	}
	/**
	 * From BG thread
	 */
	protected Void doInBackground(final Reference... params){

	    final Uri uri = params[0].uri;

	    if (0 < this.source.length()){

		final String text = this.source.toString();
		try {
		    copy(text,new java.io.OutputStreamWriter(this.resolver.openOutputStream(uri)));
		}
		catch (Exception exc){

		    Syntelos.LE(exc,"Error writing '%s'.",uri.toString());
		}
	    }
	    return null;
	}
	/**
	 * From BG thread
	 */
	private void copy(java.lang.String text, java.io.Writer out)
	    throws java.io.IOException
	{
	    try {
		out.write(text.toCharArray());
		out.flush();
	    }
	    finally {
		out.close();
	    }
	}
	/**
	 * From UI thread
	 */
	protected void onPostExecute(){

	    this.context.onPostExecute();
	}
    }

}
