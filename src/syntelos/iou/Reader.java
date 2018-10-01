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
package syntelos.iou;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;

/**
 * Operator on {@link java.nio.CharBuffer CharBuffer} (position) state
 * is compatible with {@link syntelos.lex.Window LEX Window}
 * constructor (position) state absorption.
 */
public class Reader extends java.io.Reader {

    /**
     * 
     */
    public final CharBuffer source;

    /**
     * 
     */
    public Reader(CharBuffer source){
	super();
	if (null != source && 0 == source.position() && 0 < source.limit()){

	    this.source = source;
	}
	else {
	    throw new IllegalArgumentException();
	}
    }
    public Reader(CharSequence string){
	this(CharBuffer.wrap(string));
    }
    public Reader(char[] string){
	this(CharBuffer.wrap(string));
    }


    /**
     * @param rel Location index relative to source position 
     * 
     * @return Character in source at relative location.
     */
    public char peek(int rel){

	return source.get(source.position()+rel);
    }
    /**
     * The character before the current as the last returned by read,
     * or <code>peek(-2)</code>.  
     * 
     * <pre>
     * char ch = in.read();
     * if ( Q == ch &amp;&amp; '\\' != in.precendent()){
     * 
     * &nbsp;&nbsp;&nbsp;&nbsp;//(not a "quoted" Q)
     * }
     * </pre>
     */
    public char precedent(){

	return this.peek(-2);
    }
    @Override
    public int read() throws IOException {
	return source.get();
    }
    @Override
    public int read(char b[], int of, int ln) throws IOException {
	int p = source.position();

	source.get(b,of,ln);

	return (source.position()-p);
    }
    @Override
    public long skip(long n) throws IOException {
	if (Integer.MIN_VALUE <= n && n <= Integer.MAX_VALUE){
	    int p = source.position();

	    source.position(source.position()+ (int)n);

	    return (source.position()-p);
	}
	else {
	    throw new IllegalArgumentException(String.valueOf(n));
	}
    }
    /**
     * Skip the argument character class.
     * 
     * @param cc Character class
     * 
     * @return Number of characters skipped.
     */
    public int skip(char[] cc){
	int re = 0;
	int start = this.source.position();
	int end = this.source.limit();
	int p = start;
	while (p < end){

	    if (!in(this.source.get(p),cc)){

		break;
	    }
	    else {
		p += 1;
	    }
	}

	if (start < p){
	    this.source.position(p);
	    re = (p-start);
	}
	return re;
    }
    @Override
    public boolean ready() throws IOException {

	return (source.position() < source.limit());
    }
    @Override
    public boolean markSupported(){

	return true;
    }
    @Override
    public void mark(int reli) throws IOException {

	source.mark();
    }
    @Override
    public void reset() throws IOException {

	source.reset();
    }
    @Override
    public void close() {

	source.rewind();
    }
    /**
     * 
     */
    public java.lang.String substring(char[] cc){
	int start = this.source.position();
	int count = this.skip(cc);
	if (0 < count){
	    int p = start;
	    StringBuilder string = new StringBuilder();
	    for (int cx = 0; cx < count; cx++){
		string.append(this.source.get(p++));
	    }
	    return string.toString();
	}
	else {
	    return null;
	}
    }
    public int integer(long defv){
	java.lang.String string = substring(INT);
	if (null != string){
	    return java.lang.Integer.decode(string);
	}
	else if (Integer.MIN_VALUE <= defv && defv <= Integer.MAX_VALUE){
	    return (int)defv;
	}
	else {
	    throw new IllegalStateException(String.format("Integer not found at offset %d.",this.source.position()));
	}
    }

    public final static boolean in(char c, char[] cc){

	for (char ccc : cc){
	    if (c == ccc){
		return true;
	    }
	}
	return false;
    }
    private final static char[] INT = {
	'0','1','2','3','4','5','6','7','8','9'
    };
    public final static CharBuffer open(File file) throws IOException {
	return Chbuf.read(file);
    }
}
