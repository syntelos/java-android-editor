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

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Plain text editor.
 * 
 * @author syntelos
 */
public class Editor
    extends Syntelos
{

    public Editor(){
	super();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

	setContentView(R.layout.editor);

        editor = (EditText) findViewById(R.id.editor);

	editor.addTextChangedListener(this);

        open(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	LI("onCreateOptionsMenu [%s]",this.state);

	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.editor, menu);

	return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

    	LI("onPrepareOptionsMenu [%s]",this.state);

    	super.onPrepareOptionsMenu(menu);
	/*
	 * Using the first action icon as file status
	 */
    	if (State.DIRTY == this.state){

	    MenuItem status = menu.getItem(0);
	    {
		status.setIcon(R.drawable.ic_action_save);
	    }
    	}
    	else {

	    MenuItem status = menu.getItem(0);
	    {
		status.setIcon(R.drawable.ic_action_file_drive);
	    }
    	}
    	return true;
    }
}
