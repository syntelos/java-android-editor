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

import java.io.PrintStream;

import java.nio.CharBuffer;

/**
 * Following the "octal dump" (unix "od") concept.
 */
public class Printer {

    /**
     * An ASCII named character table.
     */
    public static enum ASCII {
	NUL(0x00),
	SOH(0x01),
	STX(0x02),
	ETX(0x03),
	EOT(0x04),
	ENQ(0x05),
	ACK(0x06),
	BEL(0x07),
	BS(0x08),
	HT(0x09),
	LF(0x0A),
	VT(0x0B),
	FF(0x0C),
	CR(0x0D),
	SO(0x0E),
	SI(0x0F),
	DLE(0x10),
	DC1(0x11),
	DC2(0x12),
	DC3(0x13),
	DC4(0x14),
	NAK(0x15),
	SYN(0x16),
	ETB(0x17),
	CAN(0x18),
	EM(0x19),
	SUB(0x1A),
	ESC(0x1B),
	FS(0x1C),
	GS(0x1D),
	RS(0x1E),
	US(0x1F),
	SP(0x20),
	DEL(0x7F);


	public final int code;


	ASCII(int code){
	    this.code = code;
	}


	public final static ASCII valueOf(int code){
	    switch(code){
	    case 0x00: return NUL;
	    case 0x01: return SOH;
	    case 0x02: return STX;
	    case 0x03: return ETX;
	    case 0x04: return EOT;
	    case 0x05: return ENQ;
	    case 0x06: return ACK;
	    case 0x07: return BEL;
	    case 0x08: return BS;
	    case 0x09: return HT;
	    case 0x0A: return LF;
	    case 0x0B: return VT;
	    case 0x0C: return FF;
	    case 0x0D: return CR;
	    case 0x0E: return SO;
	    case 0x0F: return SI;
	    case 0x10: return DLE;
	    case 0x11: return DC1;
	    case 0x12: return DC2;
	    case 0x13: return DC3;
	    case 0x14: return DC4;
	    case 0x15: return NAK;
	    case 0x16: return SYN;
	    case 0x17: return ETB;
	    case 0x18: return CAN;
	    case 0x19: return EM;
	    case 0x1A: return SUB;
	    case 0x1B: return ESC;
	    case 0x1C: return FS;
	    case 0x1D: return GS;
	    case 0x1E: return RS;
	    case 0x1F: return US;
	    case 0x20: return SP;
	    case 0x7F: return DEL;
	    default:   return null;
	    }
	}
	public final static String printable(char ch){
	    ASCII a = valueOf(ch);
	    if (null != a)
		return a.name();
	    else {
		return String.valueOf(ch);
	    }
	}
    }
    /**
     * 
     */
    public static enum Offset
    {
	DEC,
	HEX;

	public final static Offset Default = HEX;
    }
    /**
     * 
     */
    public static enum Content
    {
	ASC,
	HEX;

	public final static Content Default = ASC;
    }
    /**
     * 
     */
    public static class Configuration {

	public final Offset offset;

	public final Content content;


	public Configuration(){
	    super();

	    this.offset = Offset.Default;
	    this.content = Content.Default;
	}
	public Configuration(Offset o, Content c){
	    super();

	    if (null != o){

		this.offset = o;
	    }
	    else {
		this.offset = Offset.Default;
	    }

	    if (null != c){

		this.content = c;
	    }
	    else {
		this.content = Content.Default;
	    }
	}
	public Configuration(String so, String sc){
	    super();

	    if (null != so){
		this.offset = Offset.valueOf(so.toUpperCase());
	    }
	    else {
		this.offset = Offset.Default;
	    }

	    if (null != sc){
		this.content = Content.valueOf(so.toUpperCase());
	    }
	    else {
		this.content = Content.Default;
	    }
	}

	public String toString(){

	    return ("offset: "+this.offset+", content: "+this.content);
	}
    }


    public final Offset offset;

    public final Content content;

    private int p = 0;


    public Printer(Offset o, Content c){
	super();

	if (null != o){

	    this.offset = o;
	}
	else {
	    this.offset = Offset.DEC;
	}

	if (null != c){

	    this.content = c;
	}
	else {
	    this.content = Content.ASC;
	}
    }
    public Printer(Configuration c){
	super();

	if (null != c){
	    this.offset = c.offset;
	    this.content = c.content;
	}
	else {
	    this.offset = Offset.Default;
	    this.content = Content.Default;
	}
    }


    protected void offset(PrintStream out){
	switch(this.offset){
	case DEC:
	    out.printf("%07d",p);
	    break;
	case HEX:
	    out.printf("%08X",p);
	    break;
	default:
	    throw new InternalError(this.offset.name());
	}
    }
    protected void content(int ch, PrintStream out){
	switch(this.content){

	case ASC:
	    ASCII as = ASCII.valueOf(ch);
	    if (null != as){

		out.printf(" %3s",as.name());
	    }
	    else if (0x20 < ch && 0x7F > ch){

		out.printf(" %3c",ch);
	    }
	    else {

		out.printf(" %03X",ch);
	    }
	    break;

	case HEX:
	    out.printf(" %02X",ch);
	    break;

	default:
	    throw new InternalError(this.content.name());
	}
    }
    public void reset(){

	this.seek(0);
    }
    public void seek(int p){

	this.p = p;
    }
    public void print(byte[] b, int i, int l){

	this.print(b,i,l,System.out);
    }
    public void print(byte[] b, int i, int l, PrintStream out){

	int z = (i+l);

	int c = 0;

	while (i < z){

	    offset(out);

	    for (c = 0; c < 20 && i < z; c++,p++,i++){

		content( (b[i] & 0xFF), out);
	    }

	    out.println();
	}

	offset(out);
	out.println();
    }


    public final static String fill(char ch, int count){
	if (0 < ch && 0 < count){
	    char[] cary = new char[count];
	    java.util.Arrays.fill(cary,ch);
	    return new String(cary);
	}
	else {
	    return "";
	}
    }
    public final static java.lang.String format(int hang, CharBuffer source, int start, int end){
	java.lang.StringBuilder string = new java.lang.StringBuilder();

	for (int ofs = start; ofs < end; ofs++){

	    char ch = source.get(ofs);

	    switch(ch){
	    case ' ':
	    case '\t':
		{
		    string.append('_');
		}
		break;
	    case '\r':
	    case '\n':
		{
		    string.append('\n');
		    for (int cc = 0; cc < hang; cc++){
			string.append(' ');
		    }
		}
		break;
	    default:
		string.append(ch);
		break;
	    }
	}
	return string.toString();
    }
    /**
     * 
     */
    public final static void StackTrace(Throwable t){
	int branch = 0;
	int frame = 0;
	do {
	    System.out.printf("/%d/ %s%n",branch,t.toString());

	    for (StackTraceElement stel : t.getStackTrace()){

		System.out.printf("/%d/%d/\t%s%n",branch,frame++,stel.toString());
	    }
	    t = t.getCause();
	    frame = 0;
	    branch++;
	}
	while (null != t);
    }
    public final static void StackTrace(java.lang.String p){
	StackTrace(p,1,Integer.MAX_VALUE);
    }
    public final static void StackTrace(java.lang.String p, int frix, int toix){
	frix = Math.max(frix,0);

	Throwable t = new Throwable();
	{
	    t.fillInStackTrace();
	}
	StackTraceElement[] tary = t.getStackTrace();

	toix = Math.min(toix,(tary.length-1));

	for (int tx = frix; tx <= toix; tx++){
	    StackTraceElement stel = tary[tx];
	    System.out.printf("%s %s%n",p,stel.toString());
	}
    }
}
