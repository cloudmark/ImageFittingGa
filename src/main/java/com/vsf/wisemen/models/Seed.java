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
    public int imageWidth;
    public int imageHeight;
    public SimilarityResult similarityResult;
    public List<GrowDirection> growth;
    public Seed(String name, int x, int y, int imageWidth, int imageHeight) {
        this.name = name;
        this.originalX = x;
        this.originalY = y;
        this.x = x;
        this.y = y;
        this.width = 1;
        this.height = 1;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.growth = new ArrayList<>();
        this.similarityResult = null;
    }

    public boolean grow(GrowDirection growDirection) {
        switch (growDirection) {
            case TOP: {
                if (y > 0) {
                    this.growth.add(growDirection);
                    this.y--;
                    this.height++;
                    return true;
                }
                break;
            }

            case LEFT:
                if (x > 0){
                    this.growth.add(growDirection);
                    this.x--;
                    this.width++;
                    return true;
                }
                break;

            case RIGHT:
                if (this.x + this.width < this.imageWidth) {
                    this.growth.add(growDirection);
                    this.width++;
                    return true;
                }
                break;

            case BOTTOM: {
                if (this.y + this.height < this.imageHeight) {
                    this.growth.add(growDirection);
                    this.height++;
                    return true;
                }
                break;
            }
        }
        return false;
    }

    public void recalculate() {
        List<GrowDirection> oldGrowth = growth;
        growth = new ArrayList<>();
        this.width = 1;
        this.height = 1;
        this.similarityResult = null;
        for (GrowDirection growth : oldGrowth) {
            if (!this.grow(growth)){
                break;
            }
        }
    }

    public Seed clone(){
        Seed clone = new Seed(this.name, this.x, this.y, this.imageWidth, this.imageHeight);
        clone.width = this.width;
        clone.height = this.height;
        clone.similarityResult = null;
        clone.growth = this.growth.stream().collect(Collectors.toList());
        return clone;
    }

    @Override
    public String toString() {
        String growthString = "";
        for (GrowDirection g : growth) {
            growthString += g.name().substring(0,1);
        }
        return this.name + "- (" + x + "," + y + ") [" + width + "," + height + "] [" + growthString + "]";
    }
}
