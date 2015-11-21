package com.vsf.wisemen.models;

import com.vsf.wisemen.graphics.CJPFile;
import com.vsf.wisemen.graphics.ImageFile;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Chromosome {
    public List<Seed> seeds;

    public Chromosome() {
        this.seeds = new ArrayList<>();
    }

    public Chromosome clone() {
        Chromosome chromosome = new Chromosome();
        chromosome.seeds = this.seeds.stream().map(Seed::clone).collect(Collectors.toList());
        return chromosome;
    }

    public ImageFile toCJPFile(int width, int height){
        CJPFile cjpFile = new CJPFile();
        return cjpFile;
    }

    @Override
    public String toString() {
        String buffer = "";
        for (Seed seed : seeds) {
            buffer += seed;
        }
        return buffer;
    }
}
