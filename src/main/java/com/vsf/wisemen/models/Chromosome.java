package com.vsf.wisemen.models;

import com.vsf.wisemen.graphics.*;

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

    public CLGFile toCLGFile()
    {
        ImageFile curImgFile;
        SimilarityResult curSimResult;
        Seed curSeed;
        CLGFile clgFile = new CLGFile(new ArrayList<CLGFileEntry>());
        for (int i=0; i<seeds.size(); i++){
            curImgFile = seeds.get(i).similarityResult.bestFittingSample;
            curSimResult = seeds.get(i).similarityResult;
            curSeed = seeds.get(i);
            CLGFileEntry clgentry = new CLGFileEntry(curImgFile.getComposing_image_id(),
                    curSeed.originalX,curSeed.originalY, curSeed.width,curSeed.height,
                    curSimResult.tx,curSimResult.ty);
            clgFile.entries.add(clgentry);
        }
        return clgFile;
    }
}
