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

    public GS Evolve(List<GS> initialPopulation, GenerationFeedback<GS> generationFeedbackCallback) {
        List<GS> currentPopulation = initialPopulation;
        int currentGeneration = 0;
        double currentBestScore = Double.MAX_VALUE;
        while (!config.isGoodEnough.cond(currentGeneration, currentBestScore, currentPopulation)) {
            currentPopulation = prunePopulation(currentPopulation);
            if (generationFeedbackCallback != null){
                generationFeedbackCallback.feedback(currentGeneration, currentPopulation);
            }
            currentPopulation = crossover(currentPopulation);
            currentPopulation = mutate(currentPopulation);
            currentGeneration++;
        }
        return currentPopulation.get(0);
    }

    public GS Evolve(List<GS> initialPopulation) {
        return this.Evolve(initialPopulation, null);
    }


    private List<GS> prunePopulation(List<GS> currentPopulation) {
        Map<GS, Double> scoreCache = currentPopulation.parallelStream()
                .map((x) -> new Tuple<>(x, config.scoringOperator.score(x)))
                .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
        currentPopulation.sort((x, y) -> scoreCache.get(x).compareTo(scoreCache.get(y)));
        return currentPopulation.subList(0, config.initialPopulationCount);
    }

    private List<GS> mutate(List<GS> sourcePopulation) {
        List<GS> population = new ArrayList<>(sourcePopulation.size());
        sourcePopulation.parallelStream().forEach((chromosome) -> {
            if (random.nextDouble() < config.mutationRate) {
                GS mutatedChromosome = chromosome;
                for (MutationOperator<GS> mutationOperator : config.mutationOperators) {
                    mutatedChromosome = mutationOperator.mutate(mutatedChromosome);
                }
                population.add(mutatedChromosome);
            } else {
                population.add(chromosome);
            }
        });
        return population;
    }

    private List<GS> crossover(List<GS> sourcePopulation) {
        List<GS> population = new ArrayList<>(sourcePopulation.size());
        population.addAll(sourcePopulation);
        int currentPopulationCount = sourcePopulation.size();
        for (int i = 0; i < (currentPopulationCount / 2); i++) {
            int mumIndex = random.nextInt((int) (Math.floor((double) (currentPopulationCount / 2))));
            int dadIndex = random.nextInt((int) (Math.floor((double) (currentPopulationCount / 2))));
            GS mum = sourcePopulation.get(mumIndex);
            GS dad = sourcePopulation.get(dadIndex);
            config.crossOverOperators.stream().forEach((crossOverOperator) -> {
                Tuple<GS, GS> childrenTuple = crossOverOperator.crossOver(mum, dad, config.crossOverChromosomePercentage);
                population.add(childrenTuple.getFirst());
                population.add(childrenTuple.getSecond());
            });
        }
        return population;
    }
}
