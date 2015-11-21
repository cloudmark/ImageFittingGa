package com.vsf.ga.functions;

public interface ScoringFunction<GS> {
    double score(GS chromosome);
}
