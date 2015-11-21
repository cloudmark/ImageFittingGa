package com.vsf.wisemen.graphics.impl;

import com.vsf.wisemen.graphics.ImageFile;
import com.vsf.wisemen.graphics.Pixel;

public class SingleColorImage extends ImageFile {
    private int width;
    private int height;
    private Pixel foregroundColor;

    public SingleColorImage(int width, int height, Pixel foregroundColor) {
        this.width = width;
        this.height = height;
        this.foregroundColor = foregroundColor;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public ImageFile getParent() {
        return null;
    }

    @Override
    public Pixel getPixel(int x, int y) {
        return ((x < width) && (y < height)) ? foregroundColor : new Pixel(0, 0, 0);
    }

    @Override
    public ImageFile subsample(int factor) {
        return new SingleColorImage(this.getWidth() / factor, this.getHeight() / factor, this.foregroundColor);
    }
}
