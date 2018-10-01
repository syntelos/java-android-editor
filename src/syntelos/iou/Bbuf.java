/*
 * Syntelos ENA
 * Copyright (C) 1998, 2009, 2018  John Pritchard, Syntelos.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package syntelos.iou;



/**
 * <p> Read/ write byte buffer with extended
 * <code>`InputStream'</code> and <code>`OutputStream'</code> API.
 * </p>
 *
 * <h3>API</h3>
 *
 * <p> Reading and writing can occur in any order.  Writing adds data,
 * reading consumes data.  Any constructor can be used in any usage.
 * </p>
 *
 * <p> The <code>`reset()'</code> method as in
 * <code>`OutputStream'</code> positions the write pointer to the
 * beginning of the internal buffer, unless the
 * <code>`mark(int)'</code> method has been used.  </p>
 *
 * <p> The <code>`reset_read()'</code> method positions the read
 * pointer to the beginning of the internal buffer, unless the
 * <code>`mark_read(int)'</code> method has been used.  </p>
 *
 * <p> The <code>`flush()'</code> method positions the write pointer
 * (and mark) to the beginning of the buffer.  After writing has
 * occurred, reading will not proceed beyond the written buffer.  </p>
 *
 * <p> The <code>`close()'</code> method resets both pointers and
 * markers.  </p>
 *
 * <h3>Not MT Safe</h3>
 *
 * <p> This class is not synchronized, as most applications have only
 * one thread accessing an object, and many synchronizations raise
 * java internal and operating system issues.  </p>
 *
 * <p> Applications of this class as a resource shared by multiple
 * threads are strongly encouraged to use the instance object as a
 * synchronization monitor, as in
 * 
 * <pre>
 * synchronized(buf){
 *  buf.mark_read();
 *  value = buf.read();
 *  buf.reset_read();
 *  buf.write(new_value);
 * }
 * </pre>
 * 
 * Following this simple rule will permit safe and easy development.
 * And as demonstrated in this example, external synchronization is
 * effective in all cases while the overhead of method synchronization
 * is only rarely useful but always slow.  </p>
 *
 * @author John Pritchard (@syntelos)
 * 
 * @since 1.1
 */
public class Bbuf 
    extends Bits
{

    public final static int COPY = 0x200;//(512)

    private final static int GF  = 0x200;//(512)

    public final static byte zed = (byte)0;
    private final static byte[] zedary = new byte[]{zed,zed,zed,zed, zed,zed,zed,zed};
    private final static int zedary_length = zedary.length;

    private byte[] buf ;

    /**
     * buffer read position
     */
    private int rc = 0, rc_mark = 0;

    /**
     * buffer write position (avail == wc - rc)
     */
    private int wc = 0, wc_mark = 0;

    private int gf = GF;

    /** 
     * If reopening a completed stream via unread is a problem, set
     * this to false.  Otherwise, unread is accepted for all bytes,
     * including the last.  For example, a stream of length one should
     * be able to read and unread that byte.
     */
    protected boolean unwind_finished = true;

    /**
     * Input/ read constructor.
     * 
     * @param input Read- available data 
     */
    public Bbuf( byte[] input){
        super();
        if ( null == input){

            buf_gf (GF);

            this.buf = new byte[GF];
        }
        else {
            buf = input;
            wc = buf.length;
        }
    }

    /**
     * Read file into buffer.
     */
    public Bbuf ( java.io.File fin) throws java.io.IOException {
        this((int)fin.length());
        this.readFrom(fin);
    }
    /**
     * Copy input into buffer until EOF
     */
    public Bbuf ( java.io.InputStream in) throws java.io.IOException {
        this(GF);
        if ( null == in)
            throw new IllegalArgumentException("Null input stream for `Bbuf'.");
        else 
            this.readFrom(in);
    }
    /**
     * Copy input into buffer 
     */
    public Bbuf ( java.io.InputStream in, int many) throws java.io.IOException {
        this(many);
        if ( null == in)
            throw new IllegalArgumentException("Null input stream for `Bbuf'.");
        else 
            this.readFrom(in,many);
    }
    /**
     * Output/ write constructor.
     */
    public Bbuf(){
        this (GF);
    }

    /**
     * @param gf Internal buffer growth factor, set initial buffer
     * size to gf.  */
    public Bbuf( int gf){
        super();

        buf_gf (gf);

        this.buf = new byte[gf];
    }


    /**
     * @return Bytes available for reading.  (This is also the internal
     * reference for bytes available for the read interface.)  
     */
    public int available()
        throws java.io.IOException
    {

        return (this.wc - this.rc);
    }

    public final int length()
        throws java.io.IOException
    {
        return this.available();
    }

    /**
     * @return Offset of the last byte written, or negative one before
     * the first byte has been written
     */
    public int offset_write(){
        return (this.wc-1);
    }
    /**
     * @return Offset of the last byte read, or negative one before
     * the first byte has been read
     */
    public int offset_read(){
        return (this.rc-1);
    }
    /**
     * Get or set internal buffer growth factor.  Default `GF'.
     *
     * @param gf If greater than zero, set, otherwise just return
     * current value.  */
    public final int buf_gf( int gf){
        if ( 0 < gf)
            return (this.gf = gf);
        else
            return this.gf;
    }

    public void unread(){
        int rc = this.rc;
        if (this.unwind_finished){
            if (0 < rc)
                this.rc -= 1;
        }
        else {
            if (0 < rc && this.wc > rc)
                this.rc -= 1;
        }
    }
    public void unread(int ch){
        int rc = this.rc;
        if (this.unwind_finished){
            if (0 < rc)
                this.buf[(--this.rc)] = (byte)(ch & 0xff);
            else
                throw new IllegalStateException("Each 'unread' must follow a 'read'.");
        }
        else {
            if (0 < rc && this.wc > rc)
                this.buf[(--this.rc)] = (byte)(ch & 0xff);
            else
                throw new IllegalStateException("Each 'unread' must follow a 'read'.");
        }
    }
    public void unreadn( int n){
        int rc = this.rc;
        if (this.unwind_finished){

            if (0 < n && n <= rc)
                this.rc -= n;
        }
        else {
            if (0 < n && n <= rc && this.wc > rc)
                this.rc -= n;
        }
    }

    /**
     * Look at next byte to be read.
     */
    public int peek()
        throws java.io.IOException
    {

        int av = this.available();

        if ( 0 < av)
            return this.buf[this.rc]&0xff;
        else
            return -1;
    }

    public int read()
        throws java.io.IOException
    {

        int av = this.available();

        if ( 0 < av){
            int re = this.buf[this.rc++]&0xff;
            return re;
        }
        else
            return -1;
    }
    public int read(byte b[])
        throws java.io.IOException
    {

        return this.read(b,0,b.length);
    }
    /**
     * Copy into buffer as many as buffer- length bytes, or available
     * bytes.
     *
     * @param b Non null buffer to copy into.
     *
     * @param off Valid offset in buffer `b' to copy to.
     *
     * @param len Valid number of bytes to copy into `b' (from
     * offset).
     * 
     * @returns Number of copied bytes, `len' or `available'.  */
    public int read(byte b[], int off, int len)
        throws java.io.IOException
    {
        int av = this.available();
        if (1 > av)
            return -1;
        else if (1 > len)
            return 0;
        else {
            if ( len > av)
                len = av;

            System.arraycopy(this.buf,this.rc,b,off,len);

            this.rc += len;

            return len;
        }
    }
    public byte[] readMany(int many)
        throws java.io.IOException
    {
        int av = this.available();
        if (1 > av)
            return null;
        else if (1 > many)
            return null;
        else {
            if ( many > av)
                many = av;

            byte[] b = new byte[many];

            System.arraycopy(this.buf,this.rc,b,0,many);

            this.rc += many;

            return b;
        }
    }
    /**
     * Read two bytes into a short.
     *
     * @exception java.io.IOException If two bytes are not available.
     */
    public short read2() throws java.io.IOException {

        if ( 2 <= available()){

            short ch = (short)(buf[rc++]&0xff);

            ch <<= 8;

            ch |= buf[rc++]&0xff;

            return ch;
        }
        else
            throw new java.io.IOException("Unable to read two bytes.");
    }

    /**
     * Read three bytes into an int using network byte order (big endian).
     *
     * @exception java.io.IOException If three bytes are not available.
     */
    public int read3() throws java.io.IOException {

        if ( 3 <= available()){

            int ch = buf[rc++]&0xff, ch2;

            ch <<= 16;

            ch2 = buf[rc++]&0xff;

            ch2 <<= 8;

            ch |= ch2;

            ch |= buf[rc++]&0xff;

            return ch;
        }
        else
            throw new java.io.IOException("Unable to read three bytes.");
    }

    /**
     * Read four bytes into an int using network byte order (big endian).
     *
     * @exception java.io.IOException If four bytes are not available.
     */
    public int read4() throws java.io.IOException {

        if ( 4 <= available()){

            int ch = buf[rc++]&0xff, ch2;

            ch <<= 24;

            ch2 = buf[rc++]&0xff;

            ch2 <<= 16;

            ch |= ch2;

            ch2 = buf[rc++]&0xff;

            ch2 <<= 8;

            ch |= ch2;

            ch |= buf[rc++]&0xff;

            return ch;
        }
        else
            throw new java.io.IOException("Unable to read four bytes.");
    }

    /**
     * Read eight bytes into a long using network byte order (big endian).
     *
     * @exception java.io.IOException If eight bytes are not available.
     */
    public long read8() throws java.io.IOException {

        if ( 8 <= available()){

            long ch = buf[rc++]&0xff, ch2;

            ch <<= 56;

            ch2 = buf[rc++]&0xff;

            ch2 <<= 48;

            ch |= ch2;

            ch2 = buf[rc++]&0xff;

            ch2 <<= 40;

            ch |= ch2;

            ch2 = buf[rc++]&0xff;

            ch2 <<= 32;

            ch |= ch2;

            ch2 = buf[rc++]&0xff;

            ch2 <<= 24;

            ch |= ch2;

            ch2 = buf[rc++]&0xff;

            ch2 <<= 16;

            ch |= ch2;

            ch2 = buf[rc++]&0xff;

            ch2 <<= 8;

            ch |= ch2;

            ch |= buf[rc++]&0xff;

            return ch;
        }
        else
            throw new java.io.IOException("Unable to read eight bytes.");
    }

    public String read_ascii( int len)
        throws java.io.IOException
    {
        if (len < available()){
            String ret = new String(buf, 0, rc, len);
            rc += len;
            return ret;
        }
        else return null;
    }

    /**
     * Skip read bytes, or available bytes.
     *
     * @param n Number of bytes to skip.
     *
     * @returns Number of bytes skipped.
     */
    public int skip_read(int n){

        if (0 < n){

            rc += (n);

            return n;
        }
        else
            return 0;
    }
    /**
     * Skip write bytes, or available bytes.
     *
     * @param n Number of bytes to skip.
     *
     * @returns Number of bytes skipped.
     */
    public int skip_write(int n){

        if (0 < n){

            wc += (n);

            return n;
        }
        else 
            return 0;
    }
    /**
     * Reset reading back to first byte of the current buffer, or to
     * the last marked position.  Also reset the reading mark to zero
     * so that two (or more) calls to reset clears any mark.
     */
    public void reset_read(){
        rc = rc_mark;
        rc_mark = 0;
    }
    /**
     * Sets "reset read" (reading) mark to the current position,
     * ignoring "limit" because this is a buffer -- we don't need a
     * buffer size.
     *
     * @see limit The limit argument is ignored because this is a
     * buffer
     */
    public void mark_read(int limit) {
        rc_mark = rc;
    }
    public boolean markSupported_read() {
        return true;
    }

    /**
     * Sets "reset" (reading) mark to current position.
     */
    public void mark_read() {
        rc_mark = rc;
    }
    /**
     * Return bytes read or skipped since last "mark".
     */
    public byte[] marked_read(){

        int many = rc- rc_mark;

        if ( 0 < many){

            byte[] ret = new byte[many];

            System.arraycopy( buf, rc_mark, ret, 0, many);

            return ret;
        }
        else
            return null;
    }
    /**
     * Return bytes read or skipped since last "mark".  If drop is
     * greater than zero, exclude last drop bytes from returned array
     * with no internal effect on read or mark pointers.
     */
    public byte[] marked_read(int drop){
        if (0 > drop)
            throw new java.lang.IllegalArgumentException(String.valueOf(drop));
        else {
            int many = rc- rc_mark- drop;
            if ( 0 < many){
                byte[] ret = new byte[many];
                System.arraycopy( buf, rc_mark, ret, 0, many);
                return ret;
            }
            else
                return null;
        }
    }
    public final String readLine() 
        throws java.io.IOException 
    {
        int ch, drop = 0;
        this.mark_read();
        readl:
        while (true) {
            switch (ch = this.read()) {
            case -1:
            case '\n':
                drop += 1;
                break readl;
            case '\r':
                drop += 1;
                switch (ch = this.read()){
                case -1:
                    break readl;
                case '\n':
                    drop += 1;
                    break readl;
                default:
                    this.unread();
                    break readl;
                }
                //break readl;//(unreachable)
            default:
                break;
            }
        }
        byte[] buf = this.marked_read(drop);
        if (null == buf)
            return null;
        else {
            char[] cary = Utf8.decode(buf);
            return new java.lang.String(cary);
        }
    }
    /**
     * Reset writing back to first byte of the current buffer, or to
     * the last marked position.  Also reset the writing mark to zero
     * so that two (or more) calls to reset clears any mark.
     */
    public void reset(){
        wc = wc_mark;
        wc_mark = 0;
    }
    /**
     * Same as "reset".
     */
    public void reset_write(){
        wc = wc_mark;
        wc_mark = 0;
    }
    /**
     * Reset reading and writing.
     */
    public void resetall(){
        this.reset();
        this.reset_read();
    }
    /**
     * Sets "reset" (writing) mark to current position.  Identical to
     * OutputStream's <tt>`mark(int)'</tt>.  
     */
    public void mark_write() {
        wc_mark = wc;
    }
    /**
     * Return bytes written since last "mark".
     */
    public byte[] marked_write(){

        int many = wc- wc_mark;

        if ( 0 < many){

            byte[] ret = new byte[many];

            System.arraycopy( buf, wc_mark, ret, 0, many);

            return ret;
        }
        else
            return null;
    }

    /**
     * Write 16 bits in big endian network byte order.
     * @param b 16 bits */
    public void write2 ( int b){

        if ( wc+2 >= buf.length){

            if ( 2 > gf)
                buf = growbuf(buf,buf.length+2);
            else
                buf = growbuf(buf,buf.length+gf);
        }

        buf[wc++] = (byte)((b & 0xff00)>>>8);

        buf[wc++] = (byte)(b & 0xff);

        return ;
    }
    /**
     * Write 24 bits in big endian network byte order.
     * @param b 24 bits */
    public void write3 ( int b){

        if ( wc+3 >= buf.length){

            if ( 3 > gf)
                buf = growbuf(buf,buf.length+3);
            else
                buf = growbuf(buf,buf.length+gf);
        }

        buf[wc++] = (byte)((b & 0xff0000)>>>16);
        buf[wc++] = (byte)((b & 0xff00)>>>8);
        buf[wc++] = (byte)(b & 0xff);

        return ;
    }
    /**
     * Write 32 bits in big endian network byte order.
     * @param b 32 bits */
    public void write4 ( int b){

        if ( wc+4 >= buf.length){

            if ( 4 > gf)
                buf = growbuf(buf,buf.length+4);
            else
                buf = growbuf(buf,buf.length+gf);
        }

        buf[wc++] = (byte)((b & 0xff000000)>>>24);

        buf[wc++] = (byte)((b & 0xff0000)>>>16);

        buf[wc++] = (byte)((b & 0xff00)>>>8);

        buf[wc++] = (byte)(b & 0xff);

        return ;
    }
    /**
     * Write 64 bits in big endian network byte order.
     * @param b 64 bits */
    public void write8 ( long b){

        if ( wc+8 >= buf.length){

            if ( 8 > gf)
                buf = growbuf(buf,buf.length+8);
            else
                buf = growbuf(buf,buf.length+gf);
        }

        buf[wc++] = (byte)((b & 0xff00000000000000L)>>>56);

        buf[wc++] = (byte)((b & 0xff000000000000L)>>>48);

        buf[wc++] = (byte)((b & 0xff0000000000L)>>>40);

        buf[wc++] = (byte)((b & 0xff00000000L)>>>32);

        buf[wc++] = (byte)((b & 0xff000000L)>>>24);

        buf[wc++] = (byte)((b & 0xff0000L)>>>16);

        buf[wc++] = (byte)((b & 0xff00L)>>>8);

        buf[wc++] = (byte)(b & 0xffL);

        return ;
    }



    public void write2 ( short[] ary){
        if ( null == ary)
            return ;
        else {

            int alen = ary.length;

            for ( int cc = 0; cc < alen; cc++)

                write2(ary[cc]);

            return ;
        }
    }
    public void write4 ( int[] ary){
        if ( null == ary)
            return ;
        else {

            int alen = ary.length;

            for ( int cc = 0; cc < alen; cc++){

                write4(ary[cc]);
            }

            return ;
        }
    }
    public void write8 ( long[] ary){
        if ( null == ary)
            return ;
        else {

            int alen = ary.length;

            for ( int cc = 0; cc < alen; cc++)

                write8(ary[cc]);

            return ;
        }
    }
    /**
     * Look at last byte written.
     */
    public int peek_write(){
        int wc = (this.wc-1);
        if (-1 < wc)
            return this.buf[wc]&0xff;
        else
            return -1;
    }
    /**
     * @param b Eight bit byte value */
    public void write(int b)
        throws java.io.IOException
    {

        if ( wc >= buf.length){

            buf = growbuf(buf,buf.length+gf);
        }

        byte bb = (byte)(b & 0xff);//for debugging

        buf[wc++] = bb;

        return ;
    }
    /**
     * @param b Non null buffer to copy into the internal buffer.
     *
     * @returns Number of bytes written.  */
    public void write(byte b[])
        throws java.io.IOException
    {
        if ( null == b || 0 >= b.length)
            return ;
        else 
            this.write(b,0,b.length);
    }
    /**
     * @param b Non null input buffer to copy into the internal buffer.
     *
     * @param off Offset in input buffer `b' from which to copy
     *
     * @param len Number of bytes to copy from input buffer `b'.
     *
     * @returns Number of bytes written.  */
    public void write(byte b[], int off, int len)
        throws java.io.IOException
    {
        if (0 < len){
            int ni = wc+len;

            if ( ni >= buf.length){

                if ( len > gf)
                    buf = growbuf(buf,ni);
                else 
                    buf = growbuf(buf,buf.length+gf);
            }

            System.arraycopy(b,off,buf,wc,len);

            wc += len;
        }
    }

    /**
     * Repeat the byte into the buffer.
     */
    public int nwrite ( byte ch, int many){
        if (0 < many){
	    
            int ni = wc+many;

            if ( ni >= buf.length){

                if ( many > gf)
                    buf = growbuf(buf,ni);
                else 
                    buf = growbuf(buf,buf.length+gf);
            }

            for ( int cc = wc; cc < ni; cc++)
                buf[cc] = ch;

            wc += many;
        }
        return many;
    }

    public void write_ascii( String s)
        throws java.io.IOException
    {
        if ( s == null) 
            return;
        else {
            int len = s.length();
            if (0 == len) 
                return;
            else {
                byte[] asc = new byte[len];

                s.getBytes(0,len,asc,0);

                this.write(asc,0,len);
            }
        }
    }

    public void print(char ch)
        throws java.io.IOException
    {
        if (0 < ch){
            if (ch < 128)
                this.write(ch);
            else {
                byte[] u = Utf8.encode(new char[]{ch});
                this.write(u,0,u.length);
            }
        }
    }
    public void println(char ch)
        throws java.io.IOException
    {
        if (0 < ch){
            if (ch < 128){
                this.write(ch);
                this.write(crlf);
            }
            else {
                byte[] u = Utf8.encode(new char[]{ch});
                this.write(u,0,u.length);
                this.write(crlf);
            }
        }
    }
    /**
     * Encode string in UTF-8 and append to buffer.  Return number of
     * bytes appended to buffer.  */
    public void print ( String s)
        throws java.io.IOException
    {
        if ( s == null || 0 == s.length())
            return ;
        else {
            byte[] bb = Utf8.encode(s);
            this.write(bb);
        }
    }

    /**
     * Encode string with CRLF newline in UTF-8 and append to buffer.
     */
    public void println ( String s)
        throws java.io.IOException
    {
        if ( s == null || 0 == s.length())
            return ;
        else {
            print( s);
            write(crlf);
            return ;
        }
    }

    /**
     * Write CRLF newline to buffer.  */
    public void println ()
        throws java.io.IOException
    {

        write( crlf);
    }

    /**
     * Encode to string (UTF-8) with CRLF newline.
     */
    public void println ( Object obj)
        throws java.io.IOException
    {

        append ( obj);

        write( crlf);
    }

    /**
     * Add CRLF after each.
     */
    public void println ( Object[] objs)
        throws java.io.IOException
    {

        if ( null == objs)
            return ;
        else {
            int many = objs.length;

            if ( 1 == many)

                println(objs[0]);
	    
            else {
                for ( int cc = 0; cc < many; cc++){
		    
                    println(objs[cc]);
                }
            }
        }
    }

    public void append ( boolean[] ary)
        throws java.io.IOException
    {
        if ( null == ary)
            return ;
        else {

            int alen = ary.length;

            write(setopen); 

            for ( int cc = 0; cc < alen; cc++){

                if ( 0 < cc)
                    write(comma);  

                if ( ary[cc])

                    write(trueb);
                else 
                    write(falseb);

            }
            write(setclose);

            return ;
        }
    }
    public void append ( short[] ary)
        throws java.io.IOException
    {
        if ( null == ary)
            return ;
        else {

            int alen = ary.length;

            write(setopen);

            for ( int cc = 0; cc < alen; cc++){

                if ( 0 < cc)
                    write(comma);

                print(Integer.toString(ary[cc]));
            }
            write(setclose);

            return ;
        }
    }
    public void append ( int[] ary)
        throws java.io.IOException
    {
        if ( null == ary)
            return ;
        else {

            int alen = ary.length;

            write(setopen);

            for ( int cc = 0; cc < alen; cc++){

                if ( 0 < cc)
                    write(comma);

                print(Integer.toString(ary[cc]));
            }
            write(setclose);

            return ;
        }
    }
    public void append ( long[] ary)
        throws java.io.IOException
    {
        if ( null == ary)
            return ;
        else {

            int alen = ary.length;

            write(setopen);

            for ( int cc = 0; cc < alen; cc++){

                if ( 0 < cc)
                    write(comma);

                print(Long.toString(ary[cc]));
            }
            write(setclose);

            return ;
        }
    }
    public void append ( float[] ary, Bbuf bb)
        throws java.io.IOException
    {
        if ( null == ary)
            return ;
        else {

            int alen = ary.length;

            write(setopen);

            for ( int cc = 0; cc < alen; cc++){

                if ( 0 < cc)
                    write(comma);

                print(Float.toString(ary[cc]));
            }
            write(setclose);

            return ;
        }
    }
    public void append ( double[] ary, Bbuf bb)
        throws java.io.IOException
    {
        if ( null == ary)
            return ;
        else {

            int alen = ary.length;

            write(setopen);

            for ( int cc = 0; cc < alen; cc++){

                if ( 0 < cc)
                    write(comma);

                print(Double.toString(ary[cc]));
            }
            write(setclose);

            return ;
        }
    }

    public void append ( Object[] oary)
        throws java.io.IOException
    {

        if ( null == oary)
            return ;
        else {

            int oalen = oary.length;

            for ( int cc = 0; cc < oalen; cc++)
                append( oary[cc]);

            return ;
        }
    }

    public void append ( Object o)
        throws java.io.IOException
    {

        if ( null == o)
            return ;

        else if ( o instanceof byte[]){
            write( (byte[])o);
            return ;
        }
        else if ( o instanceof String){
            write( Utf8.encode( (String)o));
            return ;
        }
        else if ( o instanceof boolean[]){
            append( (boolean[])o);
            return ;
        }
        else if ( o instanceof short[]){
            append( (short[])o);
            return ;
        }
        else if ( o instanceof int[]){
            append( (int[])o);
            return ;
        }
        else if ( o instanceof long[]){
            append( (long[])o);
            return ;
        }
        else if ( o instanceof float[]){
            append( (float[])o);
            return ;
        }
        else if ( o instanceof double[]){
            append( (double[])o);
            return ;
        }
        else if ( o instanceof char[]){
            write( Utf8.encode( (char[])o));
            return ;
        }
        else if ( null != o){
            write( Utf8.encode( o.toString()));
            return ;
        }
    }
    /**
     * Positions the write pointer (and mark) to the beginning of the
     * buffer
     */
    public void flush()
        throws java.io.IOException
    {

        wc = 0;
        rc = 0;

        wc_mark = 0;
    }
    /**
     * Resets both pointers and markers
     */
    public void close()
        throws java.io.IOException
    { 

        wc = 0;
        rc = 0;

        rc_mark = 0;
        wc_mark = 0;
    }
    /**
     * @return A string for a non- empty (UTF-8) buffer, otherwise
     * return null for an empty buffer.  Uses
     * <code>`toByteArray()'.</code>
     *
     * @see #toByteArray() 
     * @see utf8
     */
    public final String bufString()
        throws java.io.IOException
    {
        byte[] bb = toByteArray();

        if ( null == bb)
            return null;
        else {
            char[] cary = Utf8.decode(bb);
            return new String(cary,0,cary.length);
        }
    }
    /**
     * Copy readable (available) bytes from buffer.  If there are no
     * available bytes, return null.  The same bytes remain readable
     * (available).  Has no effect on the state of reading or writing.
     */
    public final byte[] toByteArray()
        throws java.io.IOException
    {

        int av = available();

        if ( 0 >= av)

            return null;

        else {

            byte[] ret = new byte[av];

            System.arraycopy(buf,rc,ret,0,av);

            return ret;
        }
    }

    /**
     * @return Internal buffer in its optimistic extent, without
     * copying.
     */
    public final byte[] verbatim(){
        return this.buf;
    }

    /**
     * Return a copy of the internal buffer, verbatim.
     */
    public final byte[] dump(){
        if ( 0 >= buf.length)
            return null;
        else {
            int bl = buf.length;
            byte[] ret = new byte[bl];
            System.arraycopy(buf,0,ret,0,bl);
            return ret;
        }
    }

    /**
     * Discard the internal buffer.  Any calls on this object will
     * produce null pointer exceptions after this method has been
     * called.  */
    public void destroy(){
        this.buf = null;
    }

    /**
     * Unsynchronized raw buffer copy to output stream using a loop
     * for TCP based destinations.  This should not be done at the
     * same time as read and write ops on this buffer.
     *
     * <p> Copies from present read point without changing the read or
     * write points.  */
    public int copyOutLoop( java.io.OutputStream out) throws java.io.IOException {

        int c0 = rc, length = available();

        if ( 0 < length){

            byte[] bb = buf;

            for ( int cc = c0; cc < length; cc++){

                out.write(bb[cc]);
            }

            return length;
        }
        else
            return 0;
    }

    /**
     * Unsynchronized raw buffer copy to output stream using a loop
     * for block devices, <i>etc.</i>.  This should not be done at the
     * same time as read and write ops on this buffer.
     *
     * <p> Copies from present read point without changing the read or
     * write points.  */
    public int copyOutArray( java.io.OutputStream out) throws java.io.IOException {

        int c0 = rc, length = available();

        if ( 0 < length){

            byte[] bb = buf;

            out.write(bb,c0,length);

            return length;
        }
        else
            return 0;
    }

    /**
     * Pass the available bits through UTF-8
     */
    public String toString(){
        try {
            byte[] bits = toByteArray();

            if ( null == bits)

                return null;

            else {
                char[] str = Utf8.decode(bits);

                if ( null == str)

                    return null;
                else
                    return new String(str);
            }
        }
        catch (java.io.IOException ignore){
            throw new IllegalStateException("Not reached");
        }
    }

    /**
     * Read the stream contents into memory.
     * 
     * @param in Source stream.
     */
    public Bbuf readFrom(java.io.InputStream in) throws java.io.IOException {
        byte[] iobuf = new byte[COPY];
        int count;
        while (0 < (count = in.read(iobuf,0,COPY)))
            this.write(iobuf,0,count);
        return this;
    }

    /**
     * Read the stream contents into memory.
     * 
     * @param in Source stream.
     */
    public Bbuf readFrom(java.io.InputStream in, int many) throws java.io.IOException {
        byte[] iob = new byte[COPY];
        for ( int cc = 0, count; cc < many;){
            count = Math.min((many - cc),COPY);
            int re = in.read(iob,0,count);
            if (0 < re){
                this.write(iob,0,re);
                cc += re;
            }
            else
                return this;
        }
        return this;
    }

    /**
     * Read the stream contents into memory.
     * 
     * @param in Source stream.

    public Bbuf readFrom(alto.io.Input in, int many) throws java.io.IOException {
        byte[] iob = new byte[COPY];
        for ( int cc = 0, count; cc < many;){
            count = Math.min((many - cc),COPY);
            int re = in.read(iob,0,count);
            if (0 < re){
                this.write(iob,0,re);
                cc += re;
            }
            else
                return this;
        }
        return this;
    }
     */

    /**
     * Write the buffer contents to the stream.
     * 
     * @param out Destination stream.
     */
    public int writeTo(java.io.OutputStream out) throws java.io.IOException {
        int total = 0;
        byte[] iobuf = new byte[COPY];
        int count;
        while(0 < (count = this.read(iobuf,0,COPY))){
            out.write(iobuf,0,count);
            total += count;
        }
        return total;
    }

    /**
     * Read file contents into this buffer.
     * 
     * @param fi Source file.  If the file exists, it is read.
     * 
     * @exception java.io.IOException If file permissions don't allow reading.
     */
    public Bbuf readFrom( java.io.File fi) throws java.io.IOException {
        if (fi.exists()){

            long filen = fi.length();

            int buflen;

            if (filen > Integer.MAX_VALUE)

                throw new IllegalArgumentException("File is too large to read into a single buffer.");
            else {

                buflen = (int)filen;

                if ( 4096 < buflen)

                    buflen = 4096;
            }

            java.io.FileInputStream in = new java.io.FileInputStream(fi);
            try {
                int read;

                byte[] readbuf = new byte[buflen];

                while (0 < filen){
		
                    read = in.read(readbuf,0,buflen);

                    if (0 < read){

                        this.write(readbuf,0,read);

                        filen -= read;
                    }
                    else
                        return this;
                }
            }
            finally {
                in.close();
            }
        }
        return this;
    }

    /**
     * Read the file contents into memory.
     * 
     * @param buf Optional buffer to fill from offset zero.  
     * 
     * @param fi File to read, if it exists.
     * 
     * @exception IllegalArgumentException If the buffer is provided
     * but is shorter than the file contents.
     * 
     * @exception java.io.IOException Reading file.
     */
    public final static byte[] readFrom( byte[] buf, java.io.File fi) throws java.io.IOException {

        if (fi.exists()){

            long filen = fi.length();

            int buflen;

            if (filen > Integer.MAX_VALUE)

                throw new IllegalArgumentException("File is too large to read into a single buffer.");
            else {
                buflen = (int)filen;

                if ( buflen > buf.length)

                    throw new IllegalArgumentException("File is too large ("+buflen+" bytes) to read into argument buffer ("+buf.length+" bytes).");

                else if ( null == buf)

                    buf = new byte[buflen];
            }

            java.io.FileInputStream in = new java.io.FileInputStream(fi);
            try {
                int read, bp = 0;

                while (0 < buflen){
		
                    read = in.read(buf,bp,buflen);

                    if ( read < buflen){

                        bp += read;

                        buflen -= read;

                        continue;
                    }
                    else
                        return buf;
                }
                return buf;
            }
            finally {
                in.close();
            }
        }
        else
            return buf;
    }

    /**
     * Write buffer to file.  File's parent directory will be tested
     * for existance and created if necessary.
     * 
     * @param fi Output file.
     * 
     * @exception java.io.IOException Writing file.
     */
    public void writeTo( java.io.File fi) throws java.io.IOException {

        writeTo( toByteArray(), fi);
    }

    /**
     * Write buffer to file.  File's parent directory will be tested
     * for existance and created if necessary.
     * 
     * @param buf Buffer (may be null).
     *
     * @param fi Output file.
     */
    public final static void writeTo( byte[] buf, java.io.File fi) throws java.io.IOException {

        String p = fi.getParent();

        if ( null == p){
            fi = new java.io.File(fi.getAbsolutePath());
            p = fi.getParent();
        }
        java.io.File pdir = new java.io.File(p);

        if (!pdir.exists())
            pdir.mkdirs();

        java.io.FileOutputStream out = new java.io.FileOutputStream(fi);
        try {
            if ( null != buf)
                out.write(buf,0,buf.length);
        }
        finally {
            out.close();
        }
    }




    /**
     * Array stretch function.
     * 
     * @param src Array, null or extant.
     * 
     * @param to_idx Index that must be accomodated in the extent of
     * the source array. */
    public final static byte[] growbuf ( byte[] src, int to_idx){

        if ( 0 > to_idx)
            return src;
        else if ( null == src)
            return new byte[to_idx+1];

        else if ( to_idx >= src.length){
            byte[] copier = new byte[to_idx+1];
            System.arraycopy(src,0,copier,0,src.length);
            return copier;
        }
        else
            return src;
    }

    private final static byte[] crlf = {(byte)'\r',(byte)'\n'};
    private final static byte[] setopen = {(byte)'['};
    private final static byte[] setclose = {(byte)' ',(byte)']'};
    private final static byte[] comma = {(byte)' '};
    private final static byte[] trueb = {(byte)'t',(byte)'r',(byte)'u',(byte)'e'};
    private final static byte[] falseb = {(byte)'f',(byte)'a',(byte)'l',(byte)'s',(byte)'e'};

    public final static byte[] toByteArray ( Object[] oary)
        throws java.io.IOException
    {
        Bbuf bbu = new Bbuf();
        bbu.append(oary);
        return bbu.toByteArray();
    }

    public final static byte[] toByteArray ( Object obj)
        throws java.io.IOException
    {
        Bbuf bbu = new Bbuf();
        bbu.append(obj);
        return bbu.toByteArray();
    }

    public final static Bbuf toByteArray ( Object obj, Bbuf bbu)
        throws java.io.IOException
    {

        if ( null == bbu)
            bbu = new Bbuf();

        bbu.append(obj);

        return bbu;
    }

    public final static Bbuf toByteArray ( Object[] objs, Bbuf bbu)
        throws java.io.IOException
    {

        if ( null == bbu)
            bbu = new Bbuf();

        bbu.append(objs);

        return bbu;
    }
    /**
     * @param value Lower eight bits
     * @param 
     */
    public final static byte[] ngen (int value, int count){
        if (0 < count){
            byte bvalue = (byte)(value & 0xff);
            byte[] re = new byte[count];
            for (int cc = 0; cc < count; cc++)
                re[cc] = bvalue;
            return re;
        }
        else
            return null;
    }
    public final static byte[] ngen(java.lang.String value, int count){
        if (null != value && 0 < count){
            byte[] bvalue = Utf8.encode(value);
            int blen = bvalue.length;
            int nlen = (blen * count);
            byte[] re = new byte[nlen];
            for (int idx = 0; idx < nlen; idx += blen){
                java.lang.System.arraycopy(bvalue,0,re,idx,blen);
            }
            return re;
        }
        else
            return null;
    }
    public final static boolean equals(byte[] a, byte[] b){
        if (null == a){
            if (null == b)
                return true;
            else
                return false;
        }
        else if (null == b)
            return false;
        else {
            if (a.length == b.length){
                for (int cc = 0, count = a.length; cc < count; cc++){
                    if (a[cc] != b[cc])
                        return false;   
                }
                return true;
            }
            else
                return false;
        }
    }
    public final static byte[] cat ( byte[] a, byte[] b){

        if ( null == a)

            return b;

        else if ( null == b)

            return a;

        else {
            byte[] rcary;

            int aclen = a.length, bclen = b.length;

            rcary = new byte[aclen+bclen];

            System.arraycopy( a, 0, rcary, 0, aclen);

            System.arraycopy( b, 0, rcary, aclen, bclen);

            return rcary;
        }
    }
    public final static byte[] cat ( byte[] a, byte[] b, byte[] c){

        if ( null == a)

            return cat(b,c);

        else if ( null == b)

            return cat(a,c);

        else if ( null == c)

            return cat(a,b);

        else {
            byte[] rcary;

            int aclen = a.length, bclen = b.length, cclen = c.length;

            rcary = new byte[aclen+bclen+cclen];

            System.arraycopy( a, 0, rcary, 0, aclen);

            System.arraycopy( b, 0, rcary, aclen, bclen);

            System.arraycopy( c, 0, rcary, (aclen+bclen), cclen);

            return rcary;
        }
    }

    public final static void debugPrint ( byte[] buf, int ofs, int len, java.io.PrintWriter out){
        if ( null == buf)
            out.println("<null buffer>");
        else {

            len = ((buf.length > len)?(len):(buf.length));

            byte bb ;

            for ( int fmtc = 0, cc = ofs; cc < len; fmtc++, cc++){

                bb = buf[cc];

                if ( 0 > bb)
                    out.print( Integer.toHexString(bb & 0xFF));
                else if (  0x20 > bb || 0x7e < bb)
                    out.print( String.valueOf((int)bb));
                else if ( 0x20 == bb)
                    out.print( '_');
                else 
                    out.print( (char)buf[cc]);

                if ( 0 < fmtc && 0 == (fmtc % 5))
                    out.println();
                else
                    out.write('\t');
            }
            out.println();
        }
    }

    public final static byte[] bytes(String string){
        if (null == string)
            return null;
        else {
            char[] cary = string.toCharArray();
            int len = cary.length;
            if (0 < len){
                byte[] bary = new byte[len];
                for (int cc = 0; cc < len; cc++){

                    bary[cc] = (byte)(cary[cc] & 0xff);
                }
                return bary;
            }
            else
                return null;
        }
    }
    /**
     * Block copier suitable for network I/O.
     */
    public final static int copy(java.io.InputStream in, java.io.OutputStream out)
        throws java.io.IOException
    {
        int count = 0, read;
        byte[] iobuf = new byte[COPY];

        while (0 < (read = in.read(iobuf,0,COPY))){

            out.write(iobuf,0,read);

            count += read;
        }
        return count;
    }
}
