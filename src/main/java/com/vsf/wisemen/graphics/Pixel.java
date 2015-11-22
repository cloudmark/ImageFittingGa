package com.vsf.wisemen.graphics;

public class Pixel {
    public int r;
    public int g;
    public int b;

    public Pixel(int r, int g, int b){
        this.r = r;
        this.b = b;
        this.g = g;
    }

    public String toString(){
        return String.valueOf(r)+","+String.valueOf(g)+","+String.valueOf(b);
    }

    public String toHex(){
        String rstr = Integer.toHexString(r);
        if (rstr.length()==1)
        {
            rstr = "0"+rstr;
        }
        String gstr = Integer.toHexString(g);
        if (gstr.length()==1)
        {
            gstr = "0"+gstr;
        }
        String bstr = Integer.toHexString(b);
        if (bstr.length()==1)
        {
            bstr = "0"+bstr;
        }
        return rstr + gstr + bstr;
    }

    @Override
    public boolean equals(Object obj) {
        Pixel that = (Pixel)obj;
        return this.r == that.r && this.g == that.g && this.b == that.b;
    }
}
