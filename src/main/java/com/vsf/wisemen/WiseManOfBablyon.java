package com.vsf.wisemen;

import com.vsf.ga.GeneticAlgorithm;
import com.vsf.ga.GeneticAlgorithmConfig;
import com.vsf.ga.functions.Tuple;
import com.vsf.wisemen.graphics.CJPFile;
import com.vsf.wisemen.graphics.ImageFile;
import com.vsf.wisemen.models.Chromosome;
import com.vsf.wisemen.models.GrowDirection;
import com.vsf.wisemen.models.Seed;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class WiseManOfBablyon {
    private static final String USERS_MARKGALEA_DEV_SOURCE_JAVA_IMAGE_FITTING_GA_EXAMPLES = "/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/";
    private static final String FOLDER = "pikachu";
    private String debugDirectory = null;
    private int seeds;
    private int rectangleCount;
    private int populationCount;
    List<ImageFile> samples = new ArrayList<>();
    ImageFile targetImage = null;
    private Random random = new Random();
    private List<Seed> seedBank = new ArrayList<>();
    private Map<String, Seed> mutationSeedCache = new HashMap<>();
    private GeneticAlgorithmConfig<Chromosome> geneticAlgorithmConfig;
    private Seed backgroundSeed = null;

    public void AddSample(ImageFile imageFile) {
        this.samples.add(imageFile);
    }

    public void SetTargetImage(ImageFile targetImage) {
        this.targetImage = targetImage;
        for (int i = 0; i < seeds; i++) {
            seedBank.add(new Seed("S" + i, random.nextInt(targetImage.getWidth()),
                    random.nextInt(targetImage.getHeight()),
                    targetImage.getWidth(), targetImage.getHeight()));
        }
        this.backgroundSeed = new Seed("SBG", 0,0,targetImage.getWidth(), targetImage.getHeight());
        for(int i = 0; i < targetImage.getWidth(); i++) backgroundSeed.grow(GrowDirection.RIGHT);
        for(int j = 0; j < targetImage.getHeight(); j++) backgroundSeed.grow(GrowDirection.BOTTOM);

    }

    public WiseManOfBablyon(int rectangleCount, int seedFactor, int populationCount, double mutationRate, double crossOverRate, double crossOverChromosomePercentage) {
        this.rectangleCount = rectangleCount;
        this.populationCount = populationCount;
        this.seeds = rectangleCount * seedFactor;
        geneticAlgorithmConfig = new GeneticAlgorithmConfig<>(populationCount, crossOverChromosomePercentage, crossOverRate, mutationRate);
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
            Collections.shuffle(seedBank);
            // Add the background.
            chromosome.seeds.add(backgroundSeed);
            // Add the different seeds.
            chromosome.seeds.addAll(seedBank.subList(0, rectangleCount - 1));
            population.add(chromosome);
        }

        geneticAlgorithmConfig.WithCrossOverOperator(
            (int currentGeneration, Chromosome mum, Chromosome dad, double splitPercentage) -> {
                List<Seed> seedUnion = new ArrayList<>();
                seedUnion.addAll(mum.seeds);
                seedUnion.addAll(dad.seeds);
                seedUnion.sort((x, y) -> new Double((x.similarityResult.score)).compareTo(x.similarityResult.score));

                int split = (int) Math.floor(mum.seeds.size() * splitPercentage) - 1;
                Chromosome child0 = new Chromosome(mum.width, mum.height);
                child0.seeds.addAll(dad.seeds.subList(0, split));
                child0.seeds.addAll(mum.seeds.subList(split, mum.seeds.size() - 1));

                Chromosome child1 = new Chromosome(mum.width, mum.height);
                child1.seeds.addAll(mum.seeds.subList(0, split));
                child1.seeds.addAll(dad.seeds.subList(split, dad.seeds.size() - 1));

                child0 = checkChild(dad.seeds.size(), child0, seedUnion);
                child1 = checkChild(dad.seeds.size(), child1, seedUnion);
                return new Tuple<>(child0, child1);
        }).WithMutation((int currentGeneration, Chromosome chromosome) -> {
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
//                chromosome.seeds.set(index, seed);
//                 TODO: Fix the caching
                if (mutationSeedCache.containsKey(key)) {
                    chromosome.seeds.set(index, mutationSeedCache.get(key));
                } else {
                    mutationSeedCache.put(key, seed);
                }
            }

            return mutantChromosome;
        }).ScoreOperator((int currentGeneration, Chromosome chromosome) -> {
                chromosome.seeds.forEach(
                    (seed) -> {
                        if (seed.similarityResult == null) {
                            seed.similarityResult = targetImage.findBestFittingImage(seed.x, seed.y, seed.width, seed.height, samples);
                        }
                    }
                );
                double value = chromosome.toCJPFile().score(0, 0, targetImage, 0, 0, targetImage.getWidth(), targetImage.getHeight());
                chromosome.score = value;
                return value;
            }
        ).WithCondition((current, score, currentPopulation)-> current == 100);


        GeneticAlgorithm<Chromosome> geneticAlgorithm = geneticAlgorithmConfig.Setup();
        geneticAlgorithm.AddWatcher(((currentGeneration, currentPopulation) -> {
            new File(debugDirectory).mkdirs();
            System.out.println("Generation: " + currentGeneration + " [" + currentPopulation.get(0).score + "]");
            // TODO: FIx 10 hardcoding
            for (int i = 0; i < currentPopulation.size() && i < 10; i++) {
                Chromosome chromosome = currentPopulation.get(i);
                CJPFile imageFile = (CJPFile)chromosome.toCJPFile();
                // String CJPFilename = debugDirectory + "/" + currentGeneration + "/" + i + ".cjp";
                new File(debugDirectory + "/" + currentGeneration ).mkdirs();
                String PNGFilename = debugDirectory + "/" + currentGeneration + "/" + i + "-"+ chromosome .score +  ".png";
                // imageFile.write(CJPFilename);
                imageFile.saveAsPNG(PNGFilename);
            }
        }));
        return geneticAlgorithm.Evolve(population);

    }


    public static void main(String[] args) {
        WiseManOfBablyon wiseManOfBablyon = new WiseManOfBablyon(11, 5, 1000, 0.8, 0.01, 0.5);

//        CJPFile targetFile = (CJPFile)(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/marilyn/marilyn.cjp").subsample(15));
//        wiseManOfBablyon.AddSample(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/marilyn/whiteSample.cjp").subsample(15));
//        wiseManOfBablyon.AddSample(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/marilyn/blackSample.cjp").subsample(15));
//        wiseManOfBablyon.AddSample(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/marilyn/redSample.cjp").subsample(15));
//        wiseManOfBablyon.AddSample(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/marilyn/yellowSample.cjp").subsample(15));
//        wiseManOfBablyon.SetTargetImage(targetFile);
//        targetFile.saveAsPNG("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/marilyn/debug/marilyn_small.png");
//        wiseManOfBablyon.SetDebugDirectory("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/marilyn/debug");

        CJPFile targetFile = (CJPFile)(new CJPFile().read(USERS_MARKGALEA_DEV_SOURCE_JAVA_IMAGE_FITTING_GA_EXAMPLES + FOLDER + "/pikachu.cjp").subsample(20));
        wiseManOfBablyon.AddSample(new CJPFile().read(USERS_MARKGALEA_DEV_SOURCE_JAVA_IMAGE_FITTING_GA_EXAMPLES + FOLDER + "/white.cjp").subsample(20));
        wiseManOfBablyon.AddSample(new CJPFile().read(USERS_MARKGALEA_DEV_SOURCE_JAVA_IMAGE_FITTING_GA_EXAMPLES + FOLDER + "/black.cjp").subsample(20));
        wiseManOfBablyon.AddSample(new CJPFile().read(USERS_MARKGALEA_DEV_SOURCE_JAVA_IMAGE_FITTING_GA_EXAMPLES + FOLDER + "/yellow_red.cjp").subsample(20));
        wiseManOfBablyon.SetTargetImage(targetFile);
        targetFile.saveAsPNG(USERS_MARKGALEA_DEV_SOURCE_JAVA_IMAGE_FITTING_GA_EXAMPLES + FOLDER + "/debug/pikachu_small.png");
        wiseManOfBablyon.SetDebugDirectory(USERS_MARKGALEA_DEV_SOURCE_JAVA_IMAGE_FITTING_GA_EXAMPLES + FOLDER + "/debug");

//        wiseManOfBablyon.AddSample(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/franceFlag/whiteSample.cjp").subsample(10));
//        wiseManOfBablyon.AddSample(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/franceFlag/blueSample.cjp").subsample(10));
//        wiseManOfBablyon.AddSample(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/franceFlag/redSample.cjp").subsample(10));
//        wiseManOfBablyon.SetTargetImage(new CJPFile().read("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/franceFlag/franceFlag.cjp").subsample(10));
//        wiseManOfBablyon.SetDebugDirectory("/Users/markgalea/Dev/Source/Java/ImageFittingGA/examples/franceFlag/debug");

        wiseManOfBablyon.Compute();
    }


}
