package com.vsf.wisemen.graphics;

import lombok.Data;

@Data
public class Pixel {
    int r;
    int g;
    int b;

    Pixel(int r, int g, int b){
        this.r = r;
        this.b = b;
        this.g = g;
    }

}
