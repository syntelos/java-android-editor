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

import syntelos.iou.Chbuf;

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
import android.os.ParcelFileDescriptor;
import android.text.Editable;
import android.widget.EditText;

import java.io.File;
import java.io.FileDescriptor;
import java.nio.CharBuffer;


/**
 * 
 */
public final class Reference
    extends java.lang.Object
{
    public static class Post {

	public static enum Status {
	    SUCCESS,
	    FAILURE;
	}
	public final static class Read
	    extends Post
	{

	    public final String text;

	    public Read(String text){
		super(Post.Status.SUCCESS);
		if (null != text){
		    this.text = text;
		}
		else {
		    throw new IllegalArgumentException();
		}
	    }
	    public Read(Throwable t, String m){
		super(t,m);
		this.text = "";
	    }
	}
	public final static class Write
	    extends Post
	{

	    public Write(){
		super(Post.Status.SUCCESS);
	    }
	    public Write(Throwable t, String m){
		super(t,m);
	    }
	}

	public final Status status;

	public final Throwable thrown;

	public final String error;


	protected Post(Status s){
	    super();
	    if (null != s){
		this.status = s;
		this.thrown = null;
		this.error = null;
	    }
	    else {
		throw new IllegalArgumentException();
	    }
	}
	protected Post(Throwable t, String e){
	    super();
	    if (null != t && null != e){
		this.status = Post.Status.FAILURE;
		this.thrown = t;
		this.error = e;
	    }
	    else {
		throw new IllegalArgumentException();
	    }
	}
    }

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
    protected static File GetRoot(){
	Context context = Reference.context;

	if (null != context){

	    return GetRoot(context);
	}
	else {

	    throw new IllegalStateException();
	}
    }

    protected final static String CONTENT_Q_COLUMN = "_data";

    protected final static String[] CONTENT_Q_PROJEC = {CONTENT_Q_COLUMN};

    protected final static File Resolve(Uri uri){

	final String scheme = uri.getScheme();

	if ("file".equalsIgnoreCase(scheme)){

	    File file = new File(uri.getPath());

	    Syntelos.LD("Reference.Resolve(%s) -> File(%s)",uri.toString(),file.getPath());

	    return file;
	}
	else {
	    /*
	     * 
	     */
	    String path = uri.getPath();

	    if (null != path){

		int six = path.indexOf("syntelos/");
		if (0 < six){
		    path = path.substring(six+"syntelos/".length());

		    File file = new File(GetRoot(),path);

		    Syntelos.LD("Reference.Resolve(%s) -> (File:Root+Uri:Path) -> File(%s)",uri.toString(),file.getPath());

		    return file;
		}
	    }

	    /*
	     * 
	     */
	    Cursor cursor = null;
	    try {
		cursor = Reference.resolver.query(uri,CONTENT_Q_PROJEC,null,null,null);

		if (null != cursor && cursor.moveToFirst()){

		    final int cix = cursor.getColumnIndex(CONTENT_Q_COLUMN);
		    if (-1 < cix){

			path = cursor.getString(cix);

			if (null != path){

			    Syntelos.LD("Reference.Resolve(%s) -> (ContentResolver) -> File(%s)",uri.toString(),path);

			    return new File(path);
			}
		    }
		}
	    }
	    finally {
		if (null != cursor){

		    cursor.close();
		}		
	    }

	    throw new IllegalArgumentException(String.format("Failed to resolve file for URI '%s'.",uri.toString()));
	}
    }
    protected final static Uri Resolve(File file){

	Uri uri = Uri.parse(file.toURI().toString());

	Syntelos.LD("Reference.Resolve(%s) -> Uri(%s)",file.getPath(),uri.toString());

	return uri;
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
		this.file = new File(Reference.root,path);
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
	extends android.os.AsyncTask<Reference,Integer,Post.Read>
    {
	private final Syntelos context;

	private final ContentResolver resolver;


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
	protected Post.Read doInBackground(final Reference... params){

	    final Uri uri = params[0].uri;
	    try {
		final StringBuilder strbuf = new StringBuilder();

		copy(strbuf,new java.io.InputStreamReader(this.resolver.openInputStream(uri)));

		return new Post.Read(strbuf.toString());
	    }
	    catch (Throwable t){

		String m = String.format("Error reading '%s'.",uri.toString());

		Syntelos.LE(t,m);

		return new Post.Read(t,m);
	    }
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
	protected void onPostExecute(Post.Read r){

	    Syntelos.LI("Reference.Reader.onPostExecute");

	    this.context.onPostReader(r);
	}

	public String toString(){
	    StringBuilder string = new StringBuilder();

	    android.os.AsyncTask.Status status = this.getStatus();
	    {
		string.append(this.getClass().getName());
		string.append(" [");
		string.append(status.toString());
		string.append(']');
	    }
	    return string.toString();
	}
    }

    /**
     * Mixed threading for I/O read task.
     */
    public final static class Writer
	extends android.os.AsyncTask<Reference,Float,Post.Write>
    {
	private final Syntelos context;

	private final ContentResolver resolver;

	private final Editable source;


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
	protected Post.Write doInBackground(final Reference... params){

	    Reference ref = params[0];
	    Uri uri = ref.uri;
	    File fil = ref.file;

	    final String text = this.source.toString();
	    try {
		/*
		 * Copy asset file descriptor
		 */
		java.io.Writer os = new java.io.OutputStreamWriter(this.resolver.openOutputStream(uri));
		try {
		    os.write(text.toCharArray());
		    os.flush();
		}
		finally {
		    os.close();
		}
		return new Post.Write();
	    }
	    catch (SecurityException sec){
		/*
		 * Copy parcel file descriptor
		 */
		ParcelFileDescriptor pfd = null;
		try {
		    pfd = ParcelFileDescriptor.open(fil,ParcelFileDescriptor.MODE_WRITE_ONLY);
		    if (null != pfd){

			FileDescriptor fd = pfd.getFileDescriptor();

			java.io.FileOutputStream os = new java.io.FileOutputStream(fd);

			java.nio.channels.FileChannel fc = os.getChannel();

			CharBuffer cb = CharBuffer.wrap(text);
			{
			    cb.rewind();
			}
			Chbuf.write(cb,fc);

			return new Post.Write();
		    }
		    else {
			throw new java.io.FileNotFoundException(uri.toString());
		    }
		}
		catch (Throwable t){

		    String m = String.format("Error writing '%s'.",uri.toString());

		    Syntelos.LE(t,m);

		    return new Post.Write(t,m);
		}
		finally {
		    if (null != pfd){
			try {
			    pfd.close();
			}
			catch (java.io.IOException iox){

			    Syntelos.LE(iox,"Error closing '%s'.",uri.toString());
			}
		    }
		}
	    }
	    catch (Throwable t){

		String m = String.format("Error writing '%s'.",uri.toString());

		Syntelos.LE(t,m);

		return new Post.Write(t,m);
	    }
	}
	/**
	 * From UI thread
	 */
	protected void onPostExecute(Post.Write w){

	    Syntelos.LI("Reference.Writer.onPostExecute");

	    this.context.onPostWriter(w);
	}

	public String toString(){
	    StringBuilder string = new StringBuilder();

	    android.os.AsyncTask.Status status = this.getStatus();
	    {
		string.append(this.getClass().getName());
		string.append(" [");
		string.append(status.toString());
		string.append(']');
	    }
	    return string.toString();
	}
    }

}
