package com.vsf.wisemen.models;

public enum GrowDirection {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM;

    @Override
    public String toString() {
        return this.name();
    }
}
