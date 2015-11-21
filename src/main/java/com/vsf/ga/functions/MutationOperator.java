package com.vsf.ga.functions;

public interface MutationOperator<S> {
    S mutate(int currentGeneration, S source);
}
