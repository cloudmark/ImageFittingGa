package com.vsf.wisemen.ga;

import java.awt.font.NumericShaper;
import java.lang.reflect.GenericArrayType;
import java.util.List;

public class GeneticAlgorithm<T, R> {

    private int generation = 1;
    private int totalGenerations = 50;
    private int initialPopulation = 100;
    double crossoverrate = 0.7;
    double mutationrate= 0.4;

    public GeneticAlgorithm(int initialPopulation, int generations){
        this.initialPopulation = initialPopulation;
        this.generation = generations;
        for (int i  = 0; i < initialPopulation; i++) {

        }
    }




}
