package com.vsf.ga.functions;

public interface ScoringFunction<GS> {
    double score(int generation, GS chromosome);
}
