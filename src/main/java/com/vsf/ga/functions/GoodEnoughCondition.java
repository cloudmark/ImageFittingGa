package com.vsf.ga.functions;


import java.util.List;

public interface GoodEnoughCondition<GS> {
    boolean cond(int currentIteration, double score, List<GS> population);
}
