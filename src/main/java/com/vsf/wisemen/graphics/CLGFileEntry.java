package com.vsf.wisemen.graphics;

/**
 * Created by james on 11/22/2015.
 */
public class CLGFileEntry {

    public int composing_image_id;
    public int xcomp;
    public int ycomp;
    public int width;
    public int height;
    public int xtarg;
    public int ytarg;


    public CLGFileEntry(int composing_image_id, int xcomp, int ycomp, int width, int height, int xtarg, int ytarg){
        this.composing_image_id = composing_image_id;
        this.xcomp = xcomp;
        this.ycomp = ycomp;
        this.width  = width;
        this.height = height;
        this.xtarg  = xtarg;
        this.ytarg  = ytarg;
    }

    public String print(){
        return composing_image_id+","+xcomp+","+ycomp+","+width+","+height+","+xtarg+","+ytarg;
    }

}
