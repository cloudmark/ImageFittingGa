package com.vsf.ga;

import com.vsf.ga.functions.*;
import java.util.*;
import java.util.stream.Collectors;

public class GeneticAlgorithm<GS> {
    private Random random = new Random();
    private GeneticAlgorithmConfig<GS> config;
    public GeneticAlgorithm(GeneticAlgorithmConfig<GS> config) {
        this.config = config;
    }
    public List<GenerationFeedback<GS>> feedbacks = new ArrayList<>();

    public GeneticAlgorithm<GS> AddWatcher(GenerationFeedback<GS> watch) {
        this.feedbacks.add(watch);
        return this;
    }

    public GS Evolve(List<GS> initialPopulation) {
        List<GS> currentPopulation = initialPopulation;
        int currentGeneration = 0;
        double currentBestScore = Double.MAX_VALUE;
        while (!config.isGoodEnough.cond(currentGeneration, currentBestScore, currentPopulation)) {
            final int currentGenerationFinal = currentGeneration;
            currentPopulation = prunePopulation(currentGeneration, currentPopulation);
            final List<GS> currentPopulationFinal = currentPopulation;
            feedbacks.forEach((x) -> x.feedback(currentGenerationFinal, currentPopulationFinal));
            currentPopulation = crossover(currentGeneration, currentPopulation);
            currentPopulation = mutate(currentGeneration, currentPopulation);
            currentGeneration++;
        }
        return currentPopulation.get(0);
    }

    private List<GS> prunePopulation(int currentGeneration, List<GS> currentPopulation) {
        Map<GS, Double> scoreCache = currentPopulation.parallelStream()
                .map((x) -> new Tuple<>(x, config.scoringOperator.score(currentGeneration, x)))
                .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
        currentPopulation.sort((x, y) -> scoreCache.get(x).compareTo(scoreCache.get(y)));
        return currentPopulation.subList(0, config.initialPopulationCount);
    }

    private List<GS> mutate(int currentGeneration, List<GS> sourcePopulation) {
        List<GS> population = new ArrayList<>(sourcePopulation.size());
        sourcePopulation.parallelStream().forEach((chromosome) -> {
            if (random.nextDouble() < config.mutationRate) {
                GS mutatedChromosome = chromosome;
                for (MutationOperator<GS> mutationOperator : config.mutationOperators) {
                    mutatedChromosome = mutationOperator.mutate(currentGeneration, mutatedChromosome);
                }
                population.add(mutatedChromosome);
            } else {
                population.add(chromosome);
            }
        });
        return population;
    }

    private List<GS> crossover(int currentGeneration, List<GS> sourcePopulation) {
        List<GS> population = new ArrayList<>(sourcePopulation.size());
        population.addAll(sourcePopulation);
        int currentPopulationCount = sourcePopulation.size();
        for (int i = 0; i < (currentPopulationCount / 2); i++) {
            int mumIndex = random.nextInt((int) (Math.floor((double) (currentPopulationCount / 2))));
            int dadIndex = random.nextInt((int) (Math.floor((double) (currentPopulationCount / 2))));
            GS mum = sourcePopulation.get(mumIndex);
            GS dad = sourcePopulation.get(dadIndex);
            config.crossOverOperators.stream().forEach((crossOverOperator) -> {
                Tuple<GS, GS> childrenTuple = crossOverOperator.crossOver(currentGeneration, mum, dad, config.crossOverChromosomePercentage);
                population.add(childrenTuple.getFirst());
                population.add(childrenTuple.getSecond());
            });
        }
        return population;
    }
}
