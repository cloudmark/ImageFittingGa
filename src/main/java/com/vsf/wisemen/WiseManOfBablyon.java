package com.vsf.wisemen;

import com.vsf.ga.GeneticAlgorithm;
import com.vsf.ga.GeneticAlgorithmConfig;
import com.vsf.ga.functions.CrossOverOperator;
import com.vsf.ga.functions.Tuple;
import com.vsf.wisemen.graphics.CJPFile;
import com.vsf.wisemen.graphics.ImageFile;
import com.vsf.wisemen.graphics.Pixel;
import com.vsf.wisemen.models.Chromosome;
import com.vsf.wisemen.models.GrowDirection;
import com.vsf.wisemen.models.Seed;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class WiseManOfBablyon {
    private static final String USERS_MARKGALEA_DEV_SOURCE_JAVA_IMAGE_FITTING_GA_EXAMPLES = "/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/";
    private static final String FOLDER = "pikachu";

    //final submission variables
    public static Chromosome fittestChromosome = null;
    public static int GENERATIONS = 1000000;

    private String debugDirectory = null;
    private int seeds;
    private int rectangleCount;
    private int populationCount;
    List<ImageFile> samples = new ArrayList<>();
    ImageFile targetImage = null;
    private Random random = new Random();
    private List<List<Seed>> partitionedSeedBank = new ArrayList<>();
    private List<Seed> seedBank = new ArrayList<>();
    private Map<String, Seed> mutationSeedCache = new HashMap<>();
    private GeneticAlgorithmConfig<Chromosome> geneticAlgorithmConfig;
    private Seed backgroundSeed = null;

    CrossOverOperator<Chromosome> CrossOverOperatorAB_CD_AD_CB = (int currentGeneration, Chromosome mum, Chromosome dad) -> {
        List<Seed> seedUnion = new ArrayList<>();
        seedUnion.addAll(mum.seeds);
        seedUnion.addAll(dad.seeds);
        seedUnion.sort((x, y) -> new Double((x.similarityResult.score)).compareTo(x.similarityResult.score));

        int split = mum.seeds.size() / 2;
        Chromosome child0 = new Chromosome(mum.width, mum.height);
        child0.seeds.addAll(dad.seeds.subList(0, split));
        child0.seeds.addAll(mum.seeds.subList(split, mum.seeds.size() - 1));

        Chromosome child1 = new Chromosome(mum.width, mum.height);
        child1.seeds.addAll(mum.seeds.subList(0, split));
        child1.seeds.addAll(dad.seeds.subList(split, dad.seeds.size() - 1));

        child0 = checkChild(dad.seeds.size(), child0, seedUnion);
        child1 = checkChild(dad.seeds.size(), child1, seedUnion);
        return new Tuple<>(child0, child1);

    };



    public void AddSample(ImageFile imageFile) {
        this.samples.add(imageFile);
    }

    public void SetTargetImage(CJPFile targetImage) {
        this.targetImage = targetImage;
        int totalColors = 65535;
        double totalPixels = 0;
        int mod = totalColors / seeds;
        mod = (mod == 0) ? 1 : mod;

        List<List<Tuple<Integer, Integer>>> bins = new ArrayList<>(mod);
        for (int i = 0; i < mod; i++) {
            bins.add(new ArrayList<>());
            partitionedSeedBank.add(new ArrayList<>());
        }


        // Bootstrap the seeds.
        for(int x = 0; x < targetImage.getWidth(); x++){
            for(int y = 0; y < targetImage.getWidth(); y++) {
                Pixel p = targetImage.getPixel(x, y);

                if (!p.equals(targetImage.background)) {
                    //  int bin = ((int)Math.floor(0.2989 * p.r + 0.5870 * p.g+ 0.1140 * p.b)) % mod;
                    int bin = (p.r << 16 | p.g << 8 | p.b) % mod;
                    bins.get(bin).add(new Tuple<>(x, y));
                    totalPixels++;
                }
            }
        }
        bins.sort((x, y) -> new Integer(y.size()).compareTo(x.size()));

        // First try to take seeds from the bucket.
        int used = 0;
        for(int i = mod - 1; i >= 0 && used < seeds; i--){
            int totalInBin = bins.get(i).size();
            if (totalInBin != 0){
                int seedCount = (int) Math.floor((totalInBin/totalPixels) * seeds);
                // There could be a feature which I want.
                seedCount = (seedCount == 0) ? 1 : seedCount;
                // Just in case the floor was useless.
                seedCount = Math.min(seedCount, totalInBin);

                if (seedCount != 0) {
                    Collections.shuffle(bins.get(i));
                    List<Tuple<Integer, Integer>> seedPixels = bins.get(i).subList(0, seedCount);
                    for (Tuple<Integer, Integer> position : seedPixels) {
                        Seed s = new Seed("S" + seedBank.size(), position.getFirst(), position.getSecond(),
                                targetImage.getWidth(), targetImage.getHeight());
                        partitionedSeedBank.get(i).add(s);
                        seedBank.add(s);
                    }
                    used += seedCount;
                }
            }
        }

        // Try a random approach.
        for (int i = used; i < seeds; i++) {
            Seed s = new Seed("S" + seedBank.size(), random.nextInt(targetImage.getWidth()),
                    random.nextInt(targetImage.getHeight()),
                    targetImage.getWidth(), targetImage.getHeight());
            seedBank.add(s);
            used++;
        }

        // Now we bias the seed bank - we don;t want the bin that contains everything.
        for (int bin = 0; bin < partitionedSeedBank.size(); bin++) {
            // int factor = partitionedSeedBank.size() - bin;
            for(int j = 0; j< bin; j++){
                seedBank.addAll(partitionedSeedBank.get(bin));
            }
        }



        // If the top left corner is not black we will take it as a background.
        Pixel topLeft = targetImage.getPixel(0,0);
        Pixel topRight = targetImage.getPixel(targetImage.getWidth() -1 ,0);
        Pixel bottomLeft = targetImage.getPixel(0,targetImage.getHeight()- 1);
        Pixel bottomRight = targetImage.getPixel(targetImage.getWidth() - 1,targetImage.getHeight()-1);
        // TODO Check this.
        boolean isThereBackground = topLeft.equals(topRight) && topLeft.equals(bottomLeft) && topLeft.equals(bottomRight);
        if (true) {
            this.backgroundSeed = new Seed("SBG", 0, 0, targetImage.getWidth(), targetImage.getHeight());
            for (int i = 0; i < targetImage.getWidth(); i++) backgroundSeed.grow(GrowDirection.RIGHT);
            for (int j = 0; j < targetImage.getHeight(); j++) backgroundSeed.grow(GrowDirection.BOTTOM);
        } else {
            this.backgroundSeed = new Seed("S" + seedBank.size(), random.nextInt(targetImage.getWidth()),
                    random.nextInt(targetImage.getHeight()),
                    targetImage.getWidth(), targetImage.getHeight());
        }
    }

    public WiseManOfBablyon(int rectangleCount, int seedFactor, int populationCount, double mutationRate, double crossOverRate) {
        this.rectangleCount = rectangleCount;
        this.populationCount = populationCount;
        this.seeds = rectangleCount * seedFactor;
        geneticAlgorithmConfig = new GeneticAlgorithmConfig<>(populationCount, crossOverRate, mutationRate);
    }


    private Chromosome checkChild(int seedCount, Chromosome chromosome, List<Seed> candidates) {
        List<Seed> distinctSeeds = chromosome.seeds.parallelStream().distinct().collect(Collectors.toList());
        if (distinctSeeds.size() != seedCount) {
            int seedsMissing = seedCount - distinctSeeds.size();
            List<Seed> missingSeeds = candidates.stream().filter(distinctSeeds::contains).limit(seedsMissing).collect(Collectors.toList());
            Chromosome fixedChromosome = new Chromosome(chromosome.width, chromosome.height);
            fixedChromosome.seeds.addAll(distinctSeeds);
            fixedChromosome.seeds.addAll(missingSeeds);
            return fixedChromosome;
        }
        return chromosome;
    }

    private WiseManOfBablyon SetDebugDirectory(String s) {
        debugDirectory = s;
        return this;
    }

    private GrowDirection randomDirection() {
        return GrowDirection.values()[random.nextInt(GrowDirection.values().length)];
    }


    public Chromosome Compute() {
        List<Chromosome> population = new ArrayList<>();
        for (int i = 0; i < populationCount; i++) {
            Chromosome chromosome = new Chromosome(targetImage.getWidth(), targetImage.getHeight());
            chromosome.seeds.add(backgroundSeed);
            List<Seed> seeds;
            List<Seed> distinctSeeds;
            do {
                Collections.shuffle(seedBank);
                seeds = seedBank.subList(0, rectangleCount - 1);
                distinctSeeds = seeds.stream().distinct().collect(Collectors.toList());
            } while(seeds.size() != distinctSeeds.size());
            chromosome.seeds.addAll(seeds);
            population.add(chromosome);
        }

        geneticAlgorithmConfig.WithCrossOverOperator(CrossOverOperatorAB_CD_AD_CB).WithMutation((int currentGeneration, Chromosome chromosome) -> {
            // This swaps two seeds in the the chromosome
            int sourceInt = random.nextInt(chromosome.seeds.size());
            int destinationInt = random.nextInt(chromosome.seeds.size());
            Seed sourceSeed = chromosome.seeds.get(sourceInt);
            Seed destinationSeed = chromosome.seeds.get(destinationInt);
            chromosome.seeds.set(destinationInt, sourceSeed);
            chromosome.seeds.set(sourceInt, destinationSeed);
            return chromosome;
        }).WithMutation((int currentGeneration, Chromosome chromosome) -> {
            Chromosome mutantChromosome = chromosome.clone();
            List<Tuple<Integer, Seed>> whichSeeds = new ArrayList<>();
            List<Seed> whichRawSeeds = new ArrayList<>();
            int mutantSeeds = chromosome.seeds.size() - (chromosome.seeds.size() / 3);
            while (whichSeeds.size() != mutantSeeds) {
                int index = random.nextInt(mutantChromosome.seeds.size());
                Seed currentWhichSeed = mutantChromosome.seeds.get(index);
                if (!whichRawSeeds.contains(currentWhichSeed)) {
                    whichSeeds.add(new Tuple<>(index, currentWhichSeed));
                    whichRawSeeds.add(currentWhichSeed);
                }
            }

            for (Tuple<Integer, Seed> tuple : whichSeeds) {
                int index = tuple.getFirst();
                Seed seed = tuple.getSecond();
                int directionCount = 3 - random.nextInt(2);
                for (int direction = 0; direction < directionCount; direction++) {
                    seed.grow(randomDirection());
                }

                String key = seed.toString();
                if (mutationSeedCache.containsKey(key)) {
                    chromosome.seeds.set(index, mutationSeedCache.get(key));
                } else {
                    mutationSeedCache.put(key, seed);
                }
            }

            return mutantChromosome;
        }).WithMutation((int currentGeneration, Chromosome chromosome) -> {
            Chromosome mutantChromosome = chromosome.clone();

            return mutantChromosome;
        }).ScoreOperator((int currentGeneration, Chromosome chromosome) -> {
                chromosome.seeds.forEach(
                    (seed) -> {
                        if (seed.similarityResult == null) {
                            seed.similarityResult = targetImage.findBestFittingImage(seed.x, seed.y, seed.width, seed.height, samples);
                        }
                    }
                );
                ImageFile chromoRepresentation = chromosome.toCJPFile();
                double value = chromoRepresentation.score(0, 0, targetImage, 0, 0, targetImage.getWidth(), targetImage.getHeight());
                Tuple<Integer, Double> overlapPenality = chromoRepresentation.overlapPercentage();
                chromosome.score = value;
                chromosome.scoreWithPenality = value + (overlapPenality.getFirst() * 441.6729559);
                return chromosome.scoreWithPenality;
            }
        ).WithCondition((current, score, currentPopulation)-> current == GENERATIONS);

        GeneticAlgorithm<Chromosome> geneticAlgorithm = geneticAlgorithmConfig.Setup();
        geneticAlgorithm.AddWatcher(((currentGeneration, currentPopulation) -> {
            new File(debugDirectory).mkdirs();
            System.out.println(currentPopulation.get(0).score);
            for (int i = 0; i < currentPopulation.size() && i < 10; i++) {
                Chromosome chromosome = currentPopulation.get(i);

                //checking if we should store this particular chromosome as the most fit chromosome
                if (fittestChromosome == null)
                    fittestChromosome = chromosome.clone();
                else if (chromosome.score < fittestChromosome.score)
                    fittestChromosome = chromosome.clone();

                //TODO cleanup to remove
                CJPFile imageFile = (CJPFile)chromosome.toCJPFile();
                // String CJPFilename = debugDirectory + "/" + currentGeneration + "/" + i + ".cjp";
                new File(debugDirectory + "/" + currentGeneration ).mkdirs();
                String PNGFilename = debugDirectory + "/" + currentGeneration + "/" + i + "-"+ chromosome .score +  ".png";
                // imageFile.write(CJPFilename);
                imageFile.saveAsPNG(PNGFilename);

                //---- remove till here
            }
        }));
        return geneticAlgorithm.Evolve(population);

    }


    public static void main(String[] args) {

        //final submission variables
        final Thread thisThread = Thread.currentThread();
        final int timeToRun = 52000; // 52 seconds;

        //Programming competition timer
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(timeToRun);

                    //TODO chromosome to clg

                    //TODO print clg file to disk

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //terminate other GA thread
                //thisThread.interrupt();
                thisThread.stop();
            }
        }).start();

        //while (!Thread.interrupted()) continue generating

        WiseManOfBablyon wiseManOfBablyon = new WiseManOfBablyon(11, 5, 1000, 0.01, 0.5);

//        CJPFile targetFile = (CJPFile)(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/marilyn/marilyn.cjp").subsample(15));
//        wiseManOfBablyon.AddSample(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/marilyn/whiteSample.cjp").subsample(15));
//        wiseManOfBablyon.AddSample(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/marilyn/blackSample.cjp").subsample(15));
//        wiseManOfBablyon.AddSample(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/marilyn/redSample.cjp").subsample(15));
//        wiseManOfBablyon.AddSample(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/marilyn/yellowSample.cjp").subsample(15));
//        wiseManOfBablyon.SetTargetImage(targetFile);
//        targetFile.saveAsPNG("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/marilyn/debug/marilyn_small.png");
//        wiseManOfBablyon.SetDebugDirectory("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/marilyn/debug");

        CJPFile targetFile = (CJPFile)(new CJPFile().read(USERS_MARKGALEA_DEV_SOURCE_JAVA_IMAGE_FITTING_GA_EXAMPLES + FOLDER + "/pikachu.cjp").subsample(20,-1));
        wiseManOfBablyon.AddSample(new CJPFile().read(USERS_MARKGALEA_DEV_SOURCE_JAVA_IMAGE_FITTING_GA_EXAMPLES + FOLDER + "/white.cjp").subsample(20,0));
        wiseManOfBablyon.AddSample(new CJPFile().read(USERS_MARKGALEA_DEV_SOURCE_JAVA_IMAGE_FITTING_GA_EXAMPLES + FOLDER + "/black.cjp").subsample(20,1));
        wiseManOfBablyon.AddSample(new CJPFile().read(USERS_MARKGALEA_DEV_SOURCE_JAVA_IMAGE_FITTING_GA_EXAMPLES + FOLDER + "/yellow_red.cjp").subsample(20,2));
        wiseManOfBablyon.SetTargetImage(targetFile);
        targetFile.saveAsPNG(USERS_MARKGALEA_DEV_SOURCE_JAVA_IMAGE_FITTING_GA_EXAMPLES + FOLDER + "/debug/pikachu_small.png");
        wiseManOfBablyon.SetDebugDirectory(USERS_MARKGALEA_DEV_SOURCE_JAVA_IMAGE_FITTING_GA_EXAMPLES + FOLDER + "/debug");

//        wiseManOfBablyon.AddSample(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/franceFlag/whiteSample.cjp").subsample(10));
//        wiseManOfBablyon.AddSample(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/franceFlag/blueSample.cjp").subsample(10));
//        wiseManOfBablyon.AddSample(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/franceFlag/redSample.cjp").subsample(10));
//        wiseManOfBablyon.SetTargetImage(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/franceFlag/franceFlag.cjp").subsample(10));
//        wiseManOfBablyon.SetDebugDirectory("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/franceFlag/debug");

        System.out.println("===============================================");
        Chromosome chromosome = wiseManOfBablyon.Compute();
        System.out.println(chromosome.toString());
    }


}
