package com.vsf.ga.functions;

public interface CrossOverOperator<S> {
    Tuple<S, S> crossOver(S mum, S dad, double crossOverChromosomePercentage);
}
