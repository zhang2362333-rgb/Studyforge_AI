package com.studyforge.interaction.support;

public final class HotScoreCalculator {
    private HotScoreCalculator() {
    }

    public static double calculate(int likes, int comments, int favorites, int views, int authorReputation) {
        return likes * 2.0 + comments * 3.0 + favorites * 5.0 + views + authorReputation * 0.2;
    }
}
