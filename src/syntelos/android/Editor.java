/*
 * Syntelos ENA
 * Copyright (C) 2017, John Pritchard, Syntelos
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


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;

import java.io.File;
import java.net.URI;

/**
 * Plain text editor to complement Manager, Paint, and Shell
 * activities.
 * 
 */
public class Editor
    extends Syntelos
{

    private EditText textView;
    private ScrollView scrollView;


    public Editor(){
	super();
    }


    public void open(){

	if (null != this.reference){

	    setTitle(this.reference.getFilename());

	    try {
		this.checkStoragePermissions();

		Reference.Reader reader = this.reference.reader(this.textView);

		reader.execute(this.reference);
	    }
	    catch (Exception exc){

		LE(exc,"Error fetching '%s'.",this.reference.toString());
	    }
	}
	else {
	    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	    intent.setType("text/plain");
	    startActivityForResult(Intent.createChooser(intent, null), 0);
	}
    }
    public void open(Uri u){

	if (null != u){

	    this.reference = new Reference(u);

	    this.open();
	}
    }
    public void open(Intent it){

	if (null != it){

	    this.open(it.getData());
	}
    }
    public void save(){

	if (null != this.reference){

	    try {
		this.checkStoragePermissions();

		Reference.Writer writer = this.reference.writer(this.textView);

		writer.execute(this.reference);
	    }
	    catch (Exception exc){

		LE(exc,"Error storing '%s'.",this.reference.toString());
	    }
	}
    }
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

	setContentView(R.layout.edit);

        textView = (EditText) findViewById(R.id.text);
        scrollView = (ScrollView) findViewById(R.id.vscroll);


	textView.setInputType(InputType.TYPE_CLASS_TEXT |
			      InputType.TYPE_TEXT_FLAG_MULTI_LINE |
			      InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        textView.setTextSize(10);
	textView.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
	{

            textView.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {

                @Override
                public void onFocusChange (View v, boolean hasFocus)
                {

                    InputMethodManager imm = (InputMethodManager)
                        getSystemService(INPUT_METHOD_SERVICE);

                    if (!hasFocus){

                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		    }
                }
            });

            textView.setOnLongClickListener(new View.OnLongClickListener()
            {

                @Override
                public boolean onLongClick (View v)
                {

		    textView.setInputType(InputType.TYPE_CLASS_TEXT |
                                          InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                                          InputType
                                          .TYPE_TEXT_FLAG_NO_SUGGESTIONS);


                    textView.setTextSize(8);
                    textView.setTextSize(10);

                    return false;
                }
            });
	}


        open(getIntent());

    }

}
