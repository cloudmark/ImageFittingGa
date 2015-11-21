package com.vsf.wisemen.models;
import com.vsf.wisemen.graphics.SimilarityResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Seed {
    public String name;
    public int originalX;
    public int originalY;
    public int x;
    public int y;
    public int width;
    public int height;
    public SimilarityResult similarityResult;

    List<GrowDirection> growth;
    public Seed(String name, int x, int y) {
        this.name = name;
        this.originalX = x;
        this.originalY = y;
        this.x = x;
        this.y = y;
        this.width = 1;
        this.height = 1;
        this.growth = new ArrayList<>();
        this.similarityResult = null;
    }

    public void grow(GrowDirection growDirection) {
        this.growth.add(growDirection);
        switch (growDirection) {
            case TOP: {
                if (y > 0) {
                    this.y--;
                    this.height++;
                }

            }

            case LEFT:
                if (x < 0){
                    this.x --;
                    this.width++;
                }
                break;

            case RIGHT:
                if (this.x < this.width - 1) {
                    this.width++;
                }
                break;

            case BOTTOM: {
                if (this.y < this.height - 1) {
                    this.height++;
                }
            }
        }
    }

    public void recalculate() {
        List<GrowDirection> oldGrowth = growth;
        growth = new ArrayList<>();
        this.width = 0;
        this.height = 0;
        for (GrowDirection growth : oldGrowth) {
            this.grow(growth);
        }
    }

    public Seed clone(){
        Seed clone = new Seed(this.name, this.x, this.y);
        clone.width = this.width;
        clone.height = this.height;
        clone.similarityResult = null;

        clone.growth = this.growth.parallelStream().collect(Collectors.toList());

        this.similarityResult = null;
        return clone;
    }

    @Override
    public String toString() {
        String growthString = "";
        for (GrowDirection g : growth) {
            growthString += g.name();
        }
        return this.name + "- (" + x + "," + y + ") [" + width + "," + height + "] [" + growthString + "]";
    }
}
