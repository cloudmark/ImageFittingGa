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
        chromosome.score = this.score;
        chromosome.scoreWithPenality = this.scoreWithPenality;
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
        int factor;
        CLGFile clgFile = new CLGFile(new ArrayList<CLGFileEntry>());
        for (int i=0; i<seeds.size(); i++){
            curImgFile = seeds.get(i).similarityResult.bestFittingSample;
            curSimResult = seeds.get(i).similarityResult;
            curSeed = seeds.get(i);
            factor = curImgFile.getFactor();

            int realwidth = curSeed.width*factor;
            int realheight = curSeed.height*factor;

            if (curSeed.originalX*factor +  realwidth > curImgFile.getParent().getWidth()) {
                realwidth = curImgFile.getParent().getWidth() - curSeed.originalX*factor;
            }

            if (curSeed.originalY*factor +  realheight> curImgFile.getParent().getHeight()) {
                realheight = curImgFile.getParent().getHeight() - curSeed.originalY*factor;
            }

            CLGFileEntry clgentry = new CLGFileEntry(curImgFile.getComposingImageId(),
                    curSeed.originalX*factor,curSeed.originalY*factor, realwidth,realheight,
                    curSimResult.tx*factor,curSimResult.ty*factor);
            clgFile.entries.add(clgentry);
        }
        return clgFile;
    }
}
