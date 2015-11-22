package com.vsf.ga.functions;

public interface CrossOverOperator<S> {
    Tuple<S, S> crossOver(int currentGeneration, S mum, S dad);
}
