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
        this.seeds.forEach((s)->{
            Seed clonedSeed = s.clone();
            chromosome.seeds.add(clonedSeed);
        });
        return chromosome;
    }

    public ImageFile toCJPFile(){
        CJPFile cjpFile = new CJPFile(width, height);
        seeds.forEach((s) -> {
            SimilarityResult similarityResult = s.similarityResult;
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
        SimilarityResult curSimResult;
        Seed curSeed;
        CLGFile clgFile = new CLGFile();
        ImageFile chromosomeImageFile = toCJPFile();
        for (int i=0; i<seeds.size(); i++){
            curSimResult = seeds.get(i).similarityResult;
            curSeed = seeds.get(i);

            int realSampleWidth = curSeed. width * curSimResult.bestFittingSample.getFactor();
            int realSampleHeight = curSeed.height * curSimResult.bestFittingSample.getFactor();
            int realSampleOriginX = curSimResult.tx *  curSimResult.bestFittingSample.getFactor();
            int realSampleOriginY = curSimResult.ty *  curSimResult.bestFittingSample.getFactor();

            int realTargetWidth = chromosomeImageFile.getWidth() * curSimResult.bestFittingSample.getFactor();
            int realTargetHeight = chromosomeImageFile.getHeight() * curSimResult.bestFittingSample.getFactor();
            int realTargetOriginX =  curSeed.x *  curSimResult.bestFittingSample.getFactor();
            int realTargetOriginY = curSeed.y *  curSimResult.bestFittingSample.getFactor();


            if ( realSampleOriginX + realSampleWidth > realTargetWidth ) {
                realSampleWidth = realTargetWidth - realSampleOriginX;
                System.out.println(realSampleWidth);
            }

            if (realSampleOriginY + realSampleHeight > realTargetHeight) {
                realSampleHeight = realTargetHeight - realSampleOriginY;
                System.out.println(realSampleHeight);

            }

            CLGFileEntry clgEntry = new CLGFileEntry(curSimResult.bestFittingSample.getComposingImageId(),
                    realSampleOriginX,realSampleOriginY,
                    realSampleWidth,realSampleHeight,
                    realTargetOriginX,realTargetOriginY);

            clgFile.entries.add(clgEntry);
        }
        return clgFile;
    }
}
