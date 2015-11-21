package com.vsf.ga.functions;

import java.util.List;

public interface GenerationFeedback<GS> {
    public void feedback(int currentGeneration, List<GS> population);
}
