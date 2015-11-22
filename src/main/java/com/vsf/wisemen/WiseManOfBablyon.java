package com.vsf.wisemen;

import com.vsf.ga.GeneticAlgorithm;
import com.vsf.ga.GeneticAlgorithmConfig;
import com.vsf.ga.functions.CrossOverOperator;
import com.vsf.ga.functions.MutationOperator;
import com.vsf.ga.functions.Tuple;
import com.vsf.wisemen.graphics.CJPFile;
import com.vsf.wisemen.graphics.CLGFile;
import com.vsf.wisemen.graphics.ImageFile;
import com.vsf.wisemen.graphics.Pixel;
import com.vsf.wisemen.models.Chromosome;
import com.vsf.wisemen.models.GrowDirection;
import com.vsf.wisemen.models.Seed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class WiseManOfBablyon {
    //final submission variables
    public static String targetFileNameUrl;
    public static String compositionImagesUrl;
    public static String collageOutputFileUrl;
    public static int rectangleCount;
    public static int subSampling;

    public static Chromosome fittestChromosome = null;
    public static int TIME_TO_RUN = 57000; //57 seconds
    public static int PIXEL_DISTANCE;


    private String debugDirectory = null;
    private int seeds;
    private int populationCount;
    List<ImageFile> samples = new ArrayList<>();
    ImageFile targetImage = null;
    private Random random = new Random();
    private List<List<Seed>> partitionedSeedBank = new ArrayList<>();
    private List<Seed> seedBank = new ArrayList<>();
    private Map<String, Seed> mutationSeedCache = new HashMap<>();
    private GeneticAlgorithmConfig<Chromosome> geneticAlgorithmConfig;
    private Seed backgroundSeed = null;
    // We will leave 3 seconds for output.
    private long terminationTime = System.currentTimeMillis() + 57000;

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

    MutationOperator<Chromosome> RandomSeedMutationInGrowthArrayChromosome = (int currentGeneration, Chromosome chromosome) -> {
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
            Seed seed = tuple.getSecond();
            if (seed.growth.size() > 5) {
                seed.growth.set(random.nextInt(seed.growth.size()), randomDirection());
                seed.recalculate();
            } else {
                seed.grow(randomDirection());
            }
        }
        return mutantChromosome;
    };

    MutationOperator<Chromosome> SeedSwapper = (int currentGeneration, Chromosome chromosome) -> {
        Chromosome mutationChromosome = chromosome.clone();
        // This swaps two seeds in the the chromosome
        int sourceInt = random.nextInt(chromosome.seeds.size());
        int destinationInt = random.nextInt(chromosome.seeds.size());
        Seed sourceSeed = mutationChromosome.seeds.get(sourceInt);
        Seed destinationSeed = mutationChromosome.seeds.get(destinationInt);
        mutationChromosome.seeds.set(destinationInt, sourceSeed);
        mutationChromosome.seeds.set(sourceInt, destinationSeed);
        return mutationChromosome;
    };

    MutationOperator<Chromosome> GrowthMutation = (int currentGeneration, Chromosome chromosome) -> {
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
    };

    MutationOperator<Chromosome> TranslationMutation = (int currentGeneration, Chromosome chromosome) -> {
        Chromosome mutationChromosome = chromosome.clone();
        List<Tuple<Integer, Seed>> whichSeeds = new ArrayList<>();
        List<Seed> whichRawSeeds = new ArrayList<>();
        int mutantSeeds = 2;
        while (whichSeeds.size() != mutantSeeds) {
            int index = random.nextInt(mutationChromosome.seeds.size());
            Seed currentWhichSeed = mutationChromosome.seeds.get(index);
            if (!whichRawSeeds.contains(currentWhichSeed)) {
                whichSeeds.add(new Tuple<>(index, currentWhichSeed));
                whichRawSeeds.add(currentWhichSeed);
            }
        }

        for (Tuple<Integer, Seed> tuple : whichSeeds) {
            Seed seed = tuple.getSecond();
            Integer index = tuple.getFirst();
            Seed mutatedSeed = seed.clone();
            mutatedSeed.x += random.nextInt(11) - 5;
            mutatedSeed.y += random.nextInt(11) - 5;
            if (mutatedSeed.x < 0) mutatedSeed.x = 0;
            if (mutatedSeed.x > mutatedSeed.imageWidth - 1) mutatedSeed.x = mutatedSeed.imageWidth - 1;
            if (mutatedSeed.y < 0) mutatedSeed.y = 0;
            if (mutatedSeed.y > mutatedSeed.imageHeight - 1) mutatedSeed.y = mutatedSeed.imageHeight - 1;
            mutatedSeed.recalculate();
            mutationChromosome.seeds.set(index, mutatedSeed);
        }
        return mutationChromosome;
    };

    MutationOperator<Chromosome> RandomSeeds = (int currentGeneration, Chromosome chromosome) -> {
        Chromosome mutationChromosome = chromosome.clone();
        List<Tuple<Integer, Seed>> whichSeeds = new ArrayList<>();
        List<Seed> whichRawSeeds = new ArrayList<>();
        int mutantSeeds = 1;
        while (whichSeeds.size() != mutantSeeds) {
            int index = random.nextInt(mutationChromosome.seeds.size());
            Seed currentWhichSeed = mutationChromosome.seeds.get(index);
            if (!whichRawSeeds.contains(currentWhichSeed)) {
                whichSeeds.add(new Tuple<>(index, currentWhichSeed));
                whichRawSeeds.add(currentWhichSeed);
            }
        }

        for (Tuple<Integer, Seed> tuple : whichSeeds) {
            Integer index = tuple.getFirst();
            Seed seed = seedBank.get(random.nextInt(seedBank.size())).clone();
            if (currentGeneration != 0) {
                int growthLength = random.nextInt(currentGeneration);
                for (int i = 0; i < growthLength; i++) {
                    seed.grow(randomDirection());
                }
            }
            mutationChromosome.seeds.set(index, seed);
        }
        return mutationChromosome;
    };

    public void AddSample(ImageFile imageFile) {
        this.samples.add(imageFile);
    }

    public void SetTargetImage(CJPFile targetImage) {
        this.targetImage = targetImage;
//        int totalColors = 65535;
//        int totalColors = 255;
        int totalColors = 255 + 255 + 255;
        double totalPixels = 0;
        int mod = totalColors / seeds;
        mod = (mod == 0) ? 1 : mod;

        List<List<Tuple<Integer, Integer>>> bins = new ArrayList<>(mod);
        for (int i = 0; i < mod; i++) {
            bins.add(new ArrayList<>());
            partitionedSeedBank.add(new ArrayList<>());
        }


        // Bootstrap the seeds.
        for (int x = 0; x < targetImage.getWidth(); x++) {
            for (int y = 0; y < targetImage.getHeight(); y++) {
                Pixel p = targetImage.getPixel(x, y);

                if (!p.equals(targetImage.background)) {
                    // int bin = ((int)Math.floor(0.2989 * p.r + 0.5870 * p.g+ 0.1140 * p.b)) % mod;
                    // int bin = (p.r << 16 | p.g << 8 | p.b) % mod;
                    int bin = (p.r + p.g + p.b) % mod;
                    bins.get(bin).add(new Tuple<>(x, y));
                    totalPixels++;
                }
            }
        }
        bins.sort((x, y) -> new Integer(y.size()).compareTo(x.size()));

        // First try to take seeds from the bucket.
        int used = 0;
        for (int i = mod - 1; i >= 0 && used < seeds; i--) {
            int totalInBin = bins.get(i).size();
            if (totalInBin != 0) {
                int seedCount = (int) Math.floor((totalInBin / totalPixels) * seeds);
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
        for (int bin = 1; bin < partitionedSeedBank.size(); bin++) {
            // int factor = partitionedSeedBank.size() - bin;
            for (int j = 0; j < (bin + 1) * (bin + 1); j++) {
                seedBank.addAll(partitionedSeedBank.get(bin));
            }
        }


        // If the top left corner is not black we will take it as a background.
        Pixel topLeft = targetImage.getPixel(1, 1);
        Pixel topRight = targetImage.getPixel(targetImage.getWidth() - 2, 1);
        Pixel bottomLeft = targetImage.getPixel(0, targetImage.getHeight() - 2);
        Pixel bottomRight = targetImage.getPixel(targetImage.getWidth() - 2, targetImage.getHeight() - 2);
        // TODO Check this.
        boolean imageHasBackground = topLeft.getDistance(topRight) <= PIXEL_DISTANCE
                && topLeft.getDistance(bottomLeft) <= PIXEL_DISTANCE
                && topLeft.getDistance(bottomRight) <= PIXEL_DISTANCE;
        if (imageHasBackground) {
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
        List<Seed> distinctSeeds = chromosome.seeds.stream().distinct().collect(Collectors.toList());
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
            } while (seeds.size() != distinctSeeds.size());
            chromosome.seeds.addAll(seeds);
            population.add(chromosome);
        }

        geneticAlgorithmConfig
                .WithCrossOverOperator(CrossOverOperatorAB_CD_AD_CB)
                .WithMutation(GrowthMutation)
                .WithMutation(SeedSwapper)
                .WithMutation(TranslationMutation)
                .WithMutation(RandomSeeds)
                .WithMutation(RandomSeedMutationInGrowthArrayChromosome)
                .ScoreOperator((int currentGeneration, Chromosome chromosome) -> {
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
                ).WithCondition((current, score, currentPopulation) -> System.currentTimeMillis() > terminationTime);

        GeneticAlgorithm<Chromosome> geneticAlgorithm = geneticAlgorithmConfig.Setup();
        geneticAlgorithm.AddWatcher(((currentGeneration, currentPopulation) -> {
            if (debugDirectory != null) {
                new File(debugDirectory).mkdirs();
            }
            System.out.println(currentGeneration + "->" + currentPopulation.get(0).score);
            for (int i = 0; i < currentPopulation.size() && i < 10; i++) {
                Chromosome chromosome = currentPopulation.get(i);

                //checking if we should store this particular chromosome as the most fit chromosome
                if (fittestChromosome == null)
                    fittestChromosome = chromosome;
                else if (chromosome.score < fittestChromosome.score)
                    fittestChromosome = chromosome;
            }
        }));
        return geneticAlgorithm.Evolve(population);

    }


    public static void main(String[] args) {

        if (args.length != 10) { //4 according to spec and 5 ours
            System.out.println("Error: Missing command line arguments. Arguments should be as follows: " +
                    "targetFileNameUrl,compositionImagesUrl, rectangleCount, collageOutputFileUrl, " +
                    "seedFactor, populationCount, mutationRate, crossOverRate, subSampling, pixelDistance");
            System.exit(-1);
        }

        targetFileNameUrl = args[0];
        compositionImagesUrl = args[1];
        rectangleCount = Integer.parseInt(args[2]);
        collageOutputFileUrl = args[3];
        int seedFactor = Integer.parseInt(args[4]);
        int populationCount = Integer.parseInt(args[5]);
        double mutationRate = Double.parseDouble(args[6]);
        double crossOverRate = Double.parseDouble(args[7]);
        subSampling = Integer.parseInt(args[8]);
        PIXEL_DISTANCE = Integer.parseInt(args[9]);

        //Programming competition timer
        new Thread(()-> {
            try {
                Thread.sleep(TIME_TO_RUN);
                CLGFile outputFile = fittestChromosome.toCLGFile();
                outputFile.print(collageOutputFileUrl); //print clg file to disk
                System.exit(-1);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                CLGFile outputFile = fittestChromosome.toCLGFile();
                outputFile.print(collageOutputFileUrl); //print clg file to disk
                System.exit(-1);
            }
        }).start();

        //logic starts here
        WiseManOfBablyon wiseManOfBablyon = new WiseManOfBablyon(rectangleCount, seedFactor, populationCount, mutationRate, crossOverRate);

        //load target file
        CJPFile targetFile = (CJPFile) (new CJPFile().read(targetFileNameUrl).subsample(subSampling,-1));
        wiseManOfBablyon.SetTargetImage(targetFile);

        //load sample files
        BufferedReader br = null;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(compositionImagesUrl));
            int i = 0;
            while ((sCurrentLine = br.readLine()) != null) {
                wiseManOfBablyon.AddSample(new CJPFile().read(sCurrentLine).subsample(subSampling,i));
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("===============================================");
        Chromosome chromosome = wiseManOfBablyon.Compute();
        System.out.println(chromosome.toString());
    }


}
