package com.vsf.wisemen.graphics;

import java.util.List;

public interface ImageFile {
    Pixel getPixel(int x, int y);
    CJPFile subsample(int factor);
    List<Pixel> getPixelsFromPyramid(int x, int y, int stepsback);
    void read(String filename);
    void write(String filename);
    double score(int sx, int sy, CJPFile sampleCJP, int tx, int ty, int width, int height);

}
