package com.vsf.wisemen;

import com.vsf.ga.GeneticAlgorithm;
import com.vsf.ga.GeneticAlgorithmConfig;
import com.vsf.ga.functions.Tuple;
import com.vsf.wisemen.graphics.ImageFile;
import com.vsf.wisemen.graphics.Pixel;
import com.vsf.wisemen.graphics.SimilarityResult;
import com.vsf.wisemen.graphics.impl.RedGreenBlueImage;
import com.vsf.wisemen.graphics.impl.SingleColorImage;
import com.vsf.wisemen.models.Chromosome;
import com.vsf.wisemen.models.GrowDirection;
import com.vsf.wisemen.models.Seed;

import java.util.*;
import java.util.stream.Collectors;

public class WiseManOfBablyon {
    private int seeds;
    private int rectangleCount;
    private int populationCount;
    List<ImageFile> samples = new ArrayList<>();
    ImageFile targetImage = null;
    private Random random = new Random();
    private List<Seed> seedBank = new ArrayList<>();
    private Map<String, Seed> mutationSeedCache = new HashMap<>();
    private GeneticAlgorithmConfig<Chromosome> geneticAlgorithmConfig;

    public void AddSample(ImageFile imageFile) {
        this.samples.add(imageFile);
    }

    public void SetTargetImage(ImageFile targetImage) {
        this.targetImage = targetImage;
        // Create the Seed Bank
        for (int i = 0; i < seeds; i++) {
            seedBank.add(new Seed("S" + i, random.nextInt(targetImage.getWidth()), random.nextInt(targetImage.getHeight())));
        }
    }

    public WiseManOfBablyon(int rectangleCount, int seedFactor, int populationCount, double mutationRate, double crossOverChromosomePercentage) {
        this.rectangleCount = rectangleCount;
        this.populationCount = populationCount;
        this.seeds = rectangleCount * seedFactor;
        geneticAlgorithmConfig = new GeneticAlgorithmConfig<>(populationCount, crossOverChromosomePercentage, mutationRate);
    }


    private Chromosome checkChild(int seedCount, Chromosome chromosome, List<Seed> candidates) {
        List<Seed> distinctSeeds = chromosome.seeds.stream().distinct().collect(Collectors.toList());
        if (distinctSeeds.size() != seedCount) {
            int seedsMissing = seedCount - distinctSeeds.size();
            List<Seed> missingSeeds = candidates.stream().filter(distinctSeeds::contains).limit(seedsMissing).collect(Collectors.toList());
            Chromosome fixedChromosome = new Chromosome();
            fixedChromosome.seeds.addAll(distinctSeeds);
            fixedChromosome.seeds.addAll(missingSeeds);
            return fixedChromosome;
        }
        return chromosome;
    }

    private GrowDirection randomDirection() {
        return GrowDirection.values()[random.nextInt(GrowDirection.values().length)];
    }


    public Chromosome Compute() {
        List<Chromosome> population = new ArrayList<>();
        for (int i = 0; i < populationCount; i++) {
            Chromosome chromosome = new Chromosome();
            Collections.shuffle(seedBank);
            chromosome.seeds.addAll(seedBank.subList(0, rectangleCount));
        }

        geneticAlgorithmConfig.WithCrossOverOperator((Chromosome mum, Chromosome dad, double splitPercentage) -> {
            List<Seed> seedUnion = new ArrayList<>();
            seedUnion.addAll(mum.seeds);
            seedUnion.addAll(dad.seeds);
            seedUnion.sort((x, y) -> new Double((x.similarityResult.score)).compareTo(x.similarityResult.score));

            int split = (int) Math.floor(mum.seeds.size() * splitPercentage) - 1;
            Chromosome child0 = new Chromosome();
            child0.seeds.addAll(dad.seeds.subList(0, split));
            child0.seeds.addAll(mum.seeds.subList(split, mum.seeds.size() - 1));

            Chromosome child1 = new Chromosome();
            child1.seeds.addAll(mum.seeds.subList(0, split));
            child1.seeds.addAll(dad.seeds.subList(split, dad.seeds.size() - 1));

            child0 = checkChild(dad.seeds.size(), child0, seedUnion);
            child1 = checkChild(dad.seeds.size(), child1, seedUnion);
            return new Tuple<>(child0, child1);

        }).WithMutation((Chromosome chromosome) -> {
            // This swaps two seeds in the the chromosome
            int sourceInt = random.nextInt(chromosome.seeds.size());
            int destinationInt = random.nextInt(chromosome.seeds.size());
            Seed sourceSeed = chromosome.seeds.get(sourceInt);
            Seed destinationSeed = chromosome.seeds.get(destinationInt);
            chromosome.seeds.set(destinationInt, sourceSeed);
            chromosome.seeds.set(sourceInt, destinationSeed);
            return chromosome;
        }).WithMutation((Chromosome chromosome) -> {
            Chromosome mutantChromosome = chromosome.clone();

            List<Tuple<Integer, Seed>> whichSeeds = new ArrayList<>();
            int mutantSeeds = chromosome.seeds.size() - (chromosome.seeds.size() / 3);
            while (whichSeeds.size() != mutantSeeds) {
                int index = random.nextInt(mutantChromosome.seeds.size());
                Seed currentWhichSeed = mutantChromosome.seeds.get(index);
                if (!whichSeeds.contains(currentWhichSeed)) whichSeeds.add(new Tuple<>(index, currentWhichSeed));
            }

            for (Tuple<Integer, Seed> tuple : whichSeeds) {
                int index = tuple.getFirst();
                Seed seed = tuple.getSecond();
                int directionCount = 3 - random.nextInt(3);
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
        }).ScoreOperator((Chromosome chromosome) -> chromosome.seeds.parallelStream()
                .map((seed) -> {
                    if (seed.similarityResult != null) return seed.similarityResult;
                    else {
                        SimilarityResult similarityResult = targetImage.findBestFittingImage(seed.x, seed.y, seed.width, seed.height, samples);
                        seed.similarityResult = similarityResult;
                        return similarityResult;
                    }
                })
                .map(x -> x.score)
                .reduce((x, y) -> x + y).get()
        );


        GeneticAlgorithm<Chromosome> geneticAlgorithm = geneticAlgorithmConfig.Setup();
        Chromosome winningChromosome = geneticAlgorithm.Evolve(population, ((currentGeneration, evolvingPopulation) -> {
            Chromosome chromosome = evolvingPopulation.get(0);
        }));
        return winningChromosome;
    }


    public static void main(String[] args) {

        WiseManOfBablyon wiseManOfBablyon = new WiseManOfBablyon(5, 3, 100, 0.8, 0.5);
        wiseManOfBablyon.AddSample(new SingleColorImage(100, 100, new Pixel(255, 0, 0)));
        wiseManOfBablyon.AddSample(new SingleColorImage(100, 100, new Pixel(0, 255, 0)));
        wiseManOfBablyon.AddSample(new SingleColorImage(100, 100, new Pixel(0, 0, 255)));
        wiseManOfBablyon.SetTargetImage(new RedGreenBlueImage(100, 300));
    }
}
