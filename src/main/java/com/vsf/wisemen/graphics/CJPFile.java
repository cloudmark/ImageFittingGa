package com.vsf.wisemen.graphics;

import lombok.Data;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

@Data
public class CJPFile implements ImageFile {
    
    private CJPFile parent;
    private int row;
    private int column;
    private List<String> raw;
    private int factor;
    private int layer;

    public CJPFile(){
        this.parent = null;
        this.factor = 1;
    }

    public CJPFile(CJPFile parent, int factor){
        this.parent = parent;
        this.factor = factor;
    }

    @Override
    public Pixel getPixel(int x, int y){
        if ((x > this.column) || (y > this.row)) new Pixel(0,0,0);
        throw new NotImplementedException();
    }

    @Override
    public CJPFile subsample(int factor){
        // Here call new CJPFile(this, factor);
        // Increment layer;
        throw new NotImplementedException();
    }

    @Override
    public List<Pixel> getPixelsFromPyramid(int x, int y, int layer){
        if (layer == this.layer) return null;
        else return parent.getPixelsFromPyramid(x * factor, y*factor, layer);
    }

    @Override
    public CJPFile read(String filename) {
        return null;
    }

    @Override
    public void write(CJPFile source, String filename) {

    }

    @Override
    public double score(int sx, int sy, CJPFile destination, int tx, int ty, int width, int height) {
        return 0;
    }

}
