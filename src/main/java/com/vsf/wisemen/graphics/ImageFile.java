package com.vsf.wisemen.graphics;


import com.vsf.ga.functions.Tuple;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;


public abstract class ImageFile {

    public abstract ImageFile getParent();

    public abstract Pixel getPixel(int x, int y);

    public abstract ImageFile subsample(int factor, int composingImageId);

    public List<Pixel> getPixelsFromPyramid(int x, int y, int stepsback){
        throw new UnsupportedOperationException();
    }

    public abstract int getFactor();

    public abstract int getComposingImageId();

    public abstract int getWidth();

    public abstract int getHeight();

    public void compose(int sx, int sy, CJPFile compositeFile, int tx, int ty, int width, int height){
        throw new UnsupportedOperationException();
    }

    // TODO: Fix Parameter Flips
    public double score(int tx, int ty, ImageFile imageFile, int sx, int sy, int width, int height){
        double ans = 0.0;
        CJPFile sampleCJP = (CJPFile)imageFile;
        if (sx + width > sampleCJP.getWidth()) width = sampleCJP.getWidth() - sx;
        if (sy + height > sampleCJP.getHeight()) height = sampleCJP.getHeight() - sy;
        if (tx + width > this.getWidth()) width = this.getWidth() - tx;
        if (ty + height > this.getHeight()) height = this.getHeight()- ty;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Pixel p0 = this.getPixel(tx + j, ty + i);
                Pixel p1 = sampleCJP.getPixel(sx + j, sy + i);
                if ((p0 == null )|| (p1 == null)){
                    throw new AssertionError();
                }
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

    public ImageFile read(String filename) {
        throw new UnsupportedOperationException();
    }

    public SimilarityResult findBestFittingImage(int sx, int sy, int width, int height, List<ImageFile> samples) {
        List<SimilarityResult> globalBest = new ArrayList<>();
        for (ImageFile sample : samples) {
            SimilarityResult localBest = new SimilarityResult();
            for( int tx = 0; tx < sample.getWidth(); tx++) {
                for( int ty = 0; ty < sample.getHeight(); ty++) {
                    double curr = this.score(sx, sy, sample , tx, ty, width, height);
                    if (curr <= localBest.score) {
                        localBest.score = curr;
                        localBest.bestFittingSample = sample;
                    }
                }
            }
            globalBest.add(localBest);
        }
        globalBest.sort((x, y) -> new Double(x.score).compareTo(y.score));
        return globalBest.get(0);
    }

    public Tuple<Integer, Double> overlapPercentage(){
        return new Tuple<>(0,0.0);
    }
}
