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
 * across all integer and long values. </p>
 * 
 * @author jdp
 * @since 1.1
 */
public abstract class Bits
    extends Object
{

    public final static long Long( byte[] buf){
        return Long(buf,0);
    }
    /**
     * For eight bytes in big- endian order, return their integer
     * value.
     */
    public final static long Long( byte[] buf, int ofs){
        long ret = 0, reg;

        int len = buf.length;

        if ( 8 < len) len = 8;

        for ( int cc = ofs, sh = 56; cc < len; cc++, sh -= 8){

            reg = (buf[cc]&0xff);

            ret |= reg<<sh;
        }
        return ret;
    }

    /**
     * Eight bytes with the big- endian binary representation of the
     * argument value.
     */
    public final static byte[] Long( long num){
        byte[] ret = new byte[8];
        return Long(num,ret);
    }
    public final static byte[] Long( long num, byte[] ret){
        if (null == ret || 8 > ret.length)
            ret = new byte[8];
        //
        ret[0] = (byte)((num>>>56)&0xff);
        ret[1] = (byte)((num>>>48)&0xff);
        ret[2] = (byte)((num>>>40)&0xff);
        ret[3] = (byte)((num>>>32)&0xff);
        ret[4] = (byte)((num>>>24)&0xff);
        ret[5] = (byte)((num>>>16)&0xff);
        ret[6] = (byte)((num>>> 8)&0xff);
        ret[7] = (byte)((num>>> 0)&0xff);

        return ret;
    }
    public final static int Integer( byte[] buf){
        return Integer(buf,0);
    }
    /**
     * For eight bytes in big- endian order, return their integer
     * value.
     */
    public final static int Integer( byte[] buf, int ofs){
        int ret = 0, reg;

        int len = buf.length;

        if ( 4 < len) len = 4;

        for ( int cc = ofs, sh = 24; cc < len; cc++, sh -= 8){

            reg = (buf[cc]&0xff);

            ret |= reg<<sh;
        }
        return ret;
    }

    /**
     * Eight bytes with the big- endian binary representation of the
     * argument value.
     */
    public final static byte[] Integer( int num){
        byte[] ret = new byte[4];
        return Integer(num,ret);
    }
    public final static byte[] Integer( int num, byte[] ret){
        if (null == ret || 4 > ret.length)
            ret = new byte[4];
        //
        ret[0] = (byte)((num>>>24)&0xff);
        ret[1] = (byte)((num>>>16)&0xff);
        ret[2] = (byte)((num>>> 8)&0xff);
        ret[3] = (byte)((num>>> 0)&0xff);

        return ret;
    }

    public final static byte[] Float(float value){
        return Integer(java.lang.Float.floatToIntBits(value));
    }
    public final static float Float(byte[] bits){
        return java.lang.Float.intBitsToFloat(Integer(bits));
    }
    public final static byte[] Double(double value){
        return Long(java.lang.Double.doubleToLongBits(value));
    }
    public final static double Double(byte[] bits){
        return java.lang.Double.longBitsToDouble(Long(bits));
    }

    /**
     * Read the stream contents into memory.
     */
    public final static byte[] Read(java.io.InputStream in) throws java.io.IOException {

        int avail = in.available(), ic = 0, rd;
        int inlen = avail;

        if ( 0 < inlen){
            byte[] readbuf = new byte[inlen];

            while(-1 < (rd = in.read())){

                if ( ic >= inlen){
                    byte[] copier = new byte[inlen+avail];
                    System.arraycopy(readbuf,0,copier,0,inlen);
                    readbuf = copier;
                    inlen += avail;
                }

                readbuf[ic++] = (byte)rd;
            }
            if ( ic < inlen){
                int overflow = inlen-ic;
                int newlen = inlen-overflow;
                byte[] copier = new byte[newlen];
                System.arraycopy(readbuf,0,copier,0,newlen);
                return copier;
            }
            else
                return readbuf;
        }
        else
            return null;
    }
}
