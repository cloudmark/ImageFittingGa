package com.vsf.ga;

import com.vsf.ga.functions.CrossOverOperator;
import com.vsf.ga.functions.GoodEnoughCondition;
import com.vsf.ga.functions.MutationOperator;
import com.vsf.ga.functions.ScoringFunction;

import java.util.ArrayList;
import java.util.List;

public class GeneticAlgorithmConfig<GS> {
    public double crossOverChromosomePercentage = 0.5;
    public double mutationRate = 0.4;
    public int initialPopulationCount;
    List<MutationOperator<GS>> mutationOperators = new ArrayList<>();
    List<CrossOverOperator<GS>> crossOverOperators = new ArrayList<>();
    ScoringFunction<GS> scoringOperator = null;
    GoodEnoughCondition<GS> isGoodEnough = null;

    public GeneticAlgorithmConfig(int initialPopulationCount, double crossOverChromosomePercentage, double mutationRate) {
        this.crossOverChromosomePercentage = crossOverChromosomePercentage;
        this.mutationRate = mutationRate;
        this.initialPopulationCount = initialPopulationCount;
    }

    public GeneticAlgorithmConfig<GS> WithMutation(MutationOperator<GS> mutationFn) {
        mutationOperators.add(mutationFn);
        return this;
    }

    public GeneticAlgorithmConfig<GS> WithCrossOverOperator(CrossOverOperator<GS> crossOverFn) {
        crossOverOperators.add(crossOverFn);
        return this;
    }

    public GeneticAlgorithmConfig<GS> ScoreOperator(ScoringFunction<GS> scoringFunction) {
        this.scoringOperator = scoringFunction;
        return this;
    }


    public GeneticAlgorithm<GS> Setup() {
        if (mutationOperators.size() == 0) throw new RuntimeException("There are no mutation operators");
        if (crossOverOperators.size() == 0) throw new RuntimeException("There are no crossover operators");
        if (isGoodEnough == null) throw new RuntimeException("There are is not a isGoodEnough function defined");
        return new GeneticAlgorithm<>(this);
    }

}
