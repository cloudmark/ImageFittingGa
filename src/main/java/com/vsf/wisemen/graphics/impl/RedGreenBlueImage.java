package com.vsf.wisemen.graphics.impl;

import com.vsf.wisemen.graphics.ImageFile;
import com.vsf.wisemen.graphics.Pixel;

public class RedGreenBlueImage extends ImageFile {
    int width;
    int height;

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    public RedGreenBlueImage(int width, int height) {
        this.width = width;
        this.height = height;
    }


    @Override
    public ImageFile getParent() {
        return null;
    }

    @Override
    public Pixel getPixel(int x, int y) {
        int div3height = height / 3;
        switch (div3height) {
            case 0:
                return new Pixel(255, 0, 0);
            case 1:
                return new Pixel(0, 255, 0);
            case 2:
                return new Pixel(0, 0, 255);
            default:
                return new Pixel(0, 0, 0);
        }
    }

    @Override
    public ImageFile subsample(int factor) {
        return new RedGreenBlueImage(width / factor, height / factor);
    }
}