package com.vsf.ga.functions;

import java.util.List;

public interface GenerationFeedback<GS> {
    void feedback(int currentGeneration, List<GS> population);
}
