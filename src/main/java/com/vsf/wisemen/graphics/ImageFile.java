package com.vsf.wisemen.graphics;

import java.util.List;

public interface ImageFile {
    Pixel getPixel(int x, int y);
    CJPFile subsample(int factor);
    List<Pixel> getPixelsFromPyramid(int x, int y, int layer);
    CJPFile read(String filename);
    void write(CJPFile source, String filename);
    double score(int sx, int sy, CJPFile destination, int tx, int ty, int width, int height);

}
