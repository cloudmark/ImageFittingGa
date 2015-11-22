package com.vsf.wisemen.graphics;

public class SimilarityResult {
    public int tx;
    public int ty;
    public double score;
    public ImageFile bestFittingSample;

    public SimilarityResult(){
        this.tx = 0;
        this.ty = 0;
        this.score = Double.MAX_VALUE;
        this.bestFittingSample = null;
    }
}
