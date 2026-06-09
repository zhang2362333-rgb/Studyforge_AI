package com.studyforge.interaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HotScoreCalculatorTest {

    @Test
    @DisplayName("各权重按公式叠加：likes*2 + comments*3 + favorites*5 + views + reputation*0.2")
    void calculatesWeightedSum() {
        double score = HotScoreCalculator.calculate(10, 5, 3, 100, 50);
        // 10*2 + 5*3 + 3*5 + 100 + 50*0.2 = 20 + 15 + 15 + 100 + 10 = 160
        assertThat(score).isEqualTo(160.0, within(1e-9));
    }

    @Test
    @DisplayName("全为零时热度为零")
    void zeroWhenNoSignals() {
        assertThat(HotScoreCalculator.calculate(0, 0, 0, 0, 0)).isZero();
    }

    @Test
    @DisplayName("收藏权重(5)高于评论(3)高于点赞(2)")
    void favoriteWeighsMoreThanCommentAndLike() {
        double oneFavorite = HotScoreCalculator.calculate(0, 0, 1, 0, 0);
        double oneComment = HotScoreCalculator.calculate(0, 1, 0, 0, 0);
        double oneLike = HotScoreCalculator.calculate(1, 0, 0, 0, 0);
        assertThat(oneFavorite).isGreaterThan(oneComment);
        assertThat(oneComment).isGreaterThan(oneLike);
    }

    @Test
    @DisplayName("浏览量按 1:1 计入")
    void viewsCountOneToOne() {
        assertThat(HotScoreCalculator.calculate(0, 0, 0, 42, 0)).isEqualTo(42.0, within(1e-9));
    }

    @Test
    @DisplayName("声望按 0.2 系数计入")
    void reputationScaledByFactor() {
        assertThat(HotScoreCalculator.calculate(0, 0, 0, 0, 100)).isEqualTo(20.0, within(1e-9));
    }
}
