package com.vsf.wisemen.models;

import com.vsf.wisemen.graphics.CJPFile;
import com.vsf.wisemen.graphics.ImageFile;
import com.vsf.wisemen.graphics.SimilarityResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Chromosome {
    public int width;
    public int height;
    public List<Seed> seeds;
    public double score;
    public double scoreWithPenality;

    public Chromosome(int width, int height) {
    this.width =width;
        this.height = height;
        this.seeds = new ArrayList<>();

    }

    public Chromosome clone() {
        Chromosome chromosome = new Chromosome(this.width, this.height);
        chromosome.seeds = this.seeds.stream().map(Seed::clone).collect(Collectors.toList());
        return chromosome;
    }

    public ImageFile toCJPFile(){
        CJPFile cjpFile = new CJPFile(width, height);
        seeds.forEach((s) -> {
            SimilarityResult similarityResult = s.similarityResult;
            // TODO: Fix the CJP typecast
            cjpFile.compose(s.x, s.y, (CJPFile) similarityResult.bestFittingSample, similarityResult.tx, similarityResult.ty, s.width, s.height);
        });

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
