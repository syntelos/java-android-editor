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
 * <p> Fast hexidecimal numeric coding correct (as to recode itself)
 * across all integer and long values.  Output A-F characters are
 * lower case. </p>
 * 
 * @author jdp (@syntelos)
 * @since 1.1
 */
public abstract class Hex 
    extends Bits
{

    private final static char[] chars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * From ASCII to binary
     */
    public final static byte[] decode ( String h){
        if (null != h){
            int len = h.length();
            byte[] ascii = new byte[len];
            h.getBytes(0,len,ascii,0);
            return decode( ascii, 0, len);
        }
        else
            return null;
    }
    /**
     * From ASCII to binary
     */
    public final static byte[] decode ( byte[] b){
        if (null != b)
            return decode( b, 0, b.length);
        else
            return null;
    }
    /**
     * From ASCII to binary
     */
    public final static byte[] decode ( byte[] cary, int ofs, int len){
        int olen = len;
        int term = len-1;

        if ( 1 == (len&1)){  //(odd number of digits in `cary')

            len += 1;

            term = len-1;
        }

        byte[] buffer = new byte[len/2];

        int bcm = 1;

        if ( 0 < ofs){

            if (1 == (ofs&1))

                bcm = 2;
            else
                bcm = 1; 

            len += ofs;
        }

        byte ch;

        for ( int cc = ofs, bc = 0; cc < len; cc++){

            if ( cc == term && cc == olen)

                break; //(odd number of digits in `cary')
            else 
                ch = cary[cc];

            if ( ofs < cc && (0 == (cc & bcm)))
                bc += 1;

            if ( '0' <= ch){

                if ( '9' >= ch){

                    if ( 1 == (cc&1))

                        buffer[bc] += (byte)(ch-'0');
                    else
                        buffer[bc] += (byte)((ch-'0')<<4);
                }
                else if ( 'a' <= ch && 'f' >= ch){

                    if ( 1 == (cc&1))

                        buffer[bc] += (byte)((ch-'a')+10);
                    else
                        buffer[bc] += (byte)(((ch-'a')+10)<<4);
                }
                else if ( 'A' <= ch && 'F' >= ch){

                    if ( 1 == (cc&1))

                        buffer[bc] += (byte)((ch-'A')+10);
                    else
                        buffer[bc] += (byte)(((ch-'A')+10)<<4);
                }
                else
                    throw new IllegalArgumentException("String '"+new String(cary,0,ofs,olen)+"' is not hex.");
            }
            else
                throw new IllegalArgumentException("String '"+new String(cary,0,ofs,olen)+"' is not hex.");

        }
        return buffer;
    }
    /**
     * <p> Basic HEX encoding primitives. </p>
     * @param val An eight bit value.
     * @return Low four bits encoded to a hex character.
     */
    public final static char encode8Low(int val){
        return chars[val & 0xf];
    }
    /**
     * <p> Basic HEX encoding primitives. </p>
     * @param val An eight bit value.
     * @return High four bits encoded to a hex character.
     */
    public final static char encode8High(int val){
        return chars[(val >>> 4) & 0xf];
    }

    public final static String encode(byte ch){
        char[] string = new char[]{
            encode8High(ch),
            encode8Low(ch)
        };
        return new String(string);
    }

    public final static String encode(java.math.BigInteger bint){
        if (null == bint)
            return null;
        else
            return encode(bint.toByteArray());
    }
    /**
     * <p> binary to hexadecimal</p>
     */
    public final static String encode ( byte[] buffer){
        if ( null == buffer)
            return null;
        else {
            int len = buffer.length;
            char[] cary = new char[(len*2)];
            int val, ca = 0;

            for ( int cc = 0; cc < len; cc++){

                val = (buffer[cc]&0xff);

                cary[ca++] = (chars[(val>>>4)&0xf]);
                cary[ca++] = (chars[ val&0xf]);
            }
            return new java.lang.String(cary);
        }
    }

    /**
     * <p> binary to hexadecimal</p>
     * @return Seven bit ASCII hexidecimal
     */
    public final static byte[] encode2 ( byte[] buffer){
        if ( null == buffer)
            return null;
        else {
            int len = buffer.length;
            byte[] bary = new byte[(len*2)];
            int val, ca = 0;

            for ( int cc = 0; cc < len; cc++){

                val = (buffer[cc]&0xff);

                bary[ca++] = (byte)(chars[(val>>>4)&0xf]);
                bary[ca++] = (byte)(chars[ val&0xf]);
            }
            return bary;
        }
    }

    public final static String encode( long value){
        byte[] bvalue = Long(value);
        return encode(bvalue);
    }
    public final static String encode( int value){
        byte[] bvalue = Integer(value);
        return encode(bvalue);
    }
}
