package com.vsf.wisemen.graphics;


import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;


public abstract class ImageFile {

    public abstract ImageFile getParent();

    public abstract Pixel getPixel(int x, int y);

    public abstract ImageFile subsample(int factor);

    public List<Pixel> getPixelsFromPyramid(int x, int y, int stepsback){
        throw new UnsupportedOperationException();
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public void compose(int sx, int sy, CJPFile compositeFile, int tx, int ty, int width, int height){
        throw new UnsupportedOperationException();
    }

    public double score(int sx, int sy, ImageFile imageFile, int tx, int ty, int width, int height){
        double ans = 0.0;
        CJPFile sampleCJP = (CJPFile)imageFile;
        if (sx + width > sampleCJP.getWidth()) width = sampleCJP.getWidth() - sx;
        if (sy + height > sampleCJP.getHeight()) height = sampleCJP.getHeight() - sy;
        if (tx + width > this.getWidth()) width = this.getWidth() - tx;
        if (ty + height > this.getHeight()) height = this.getHeight()- ty;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Pixel p0 = this.getPixel(tx + j, ty + i);
                Pixel p1 = this.getPixel(sx + j, sy + i);
                ans += Math.sqrt(Math.pow((double) (p0.r - p1.r), 2.0D)
                        + Math.pow((double) (p0.g - p1.g), 2.0D)
                        + Math.pow((double) (p0.b - p1.b), 2.0D));

            }
        }

        return ans;
    }


    public void write(String filename){
        throw new UnsupportedOperationException();
    }

    public void read(String filename) {
        throw new UnsupportedOperationException();
    }

    public SimilarityResult findBestFittingImage(int sx, int sy, int width, int height, List<ImageFile> samples) {
        throw new NotImplementedException();


  }
}
