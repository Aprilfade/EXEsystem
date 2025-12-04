package com.ice.exebackend.utils;

import com.ice.exebackend.entity.BizQuestion;
// import lombok.Data; // 可以移除或保留，反正我们手动写了

import java.util.*;

/**
 * 遗传算法智能组卷工具类
 */
public class GeneticPaperUtil {

    // 进化参数
    private static final int POPULATION_SIZE = 20;   // 种群规模
    private static final int MAX_GENERATIONS = 100;  // 最大迭代次数
    private static final double MUTATION_RATE = 0.1; // 变异概率
    private static final double DIFF_WEIGHT = 0.60;  // 难度系数权重

    /**
     * 核心入口：根据题库和约束生成试卷
     */
    public static List<BizQuestion> evolution(
            Map<Integer, List<BizQuestion>> allCandidateQuestions,
            Map<Integer, Integer> typeCountMap,
            double targetDifficulty,
            double expectedTotalScore) {

        // 1. 初始化种群
        List<PaperIndividual> population = initPopulation(allCandidateQuestions, typeCountMap, POPULATION_SIZE);

        if (population.isEmpty()) return new ArrayList<>();

        PaperIndividual bestIndividual = population.get(0);

        // 2. 开始进化迭代
        for (int i = 0; i < MAX_GENERATIONS; i++) {
            // 计算适应度
            for (PaperIndividual paper : population) {
                calculateFitness(paper, targetDifficulty, expectedTotalScore);
                // 记录历史最优
                if (paper.getFitness() > bestIndividual.getFitness()) {
                    bestIndividual = paper;
                }
            }

            // 如果已经找到了非常完美的试卷（适应度 > 0.98），提前结束
            if (bestIndividual.getFitness() > 0.98) {
                break;
            }

            // 选择、交叉、变异生成下一代
            population = nextGeneration(population, allCandidateQuestions, typeCountMap);
        }

        return bestIndividual.getQuestions();
    }

    // --- 内部类：代表一张试卷个体 (手动添加 Getter/Setter) ---
    public static class PaperIndividual {
        private List<BizQuestion> questions = new ArrayList<>();
        private double fitness = 0.0;
        private double difficulty = 0.0;
        private double totalScore = 0.0;

        // === 手动添加的 Getter 和 Setter ===
        public List<BizQuestion> getQuestions() {
            return questions;
        }

        public void setQuestions(List<BizQuestion> questions) {
            this.questions = questions;
        }

        public double getFitness() {
            return fitness;
        }

        public void setFitness(double fitness) {
            this.fitness = fitness;
        }

        public double getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(double difficulty) {
            this.difficulty = difficulty;
        }

        public double getTotalScore() {
            return totalScore;
        }

        public void setTotalScore(double totalScore) {
            this.totalScore = totalScore;
        }
    }

    // 1. 初始化种群
    private static List<PaperIndividual> initPopulation(
            Map<Integer, List<BizQuestion>> dbData,
            Map<Integer, Integer> constraints,
            int size) {
        List<PaperIndividual> population = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            PaperIndividual paper = new PaperIndividual();
            // 遍历每种题型，随机抽取指定数量
            for (Map.Entry<Integer, Integer> entry : constraints.entrySet()) {
                int type = entry.getKey();
                int count = entry.getValue();
                List<BizQuestion> candidates = dbData.get(type);

                if (candidates == null || candidates.size() < count) {
                    continue;
                }

                List<BizQuestion> selected = new ArrayList<>();
                Set<Integer> selectedIndices = new HashSet<>();
                Random random = new Random();
                while (selectedIndices.size() < count) {
                    int idx = random.nextInt(candidates.size());
                    if (!selectedIndices.contains(idx)) {
                        selectedIndices.add(idx);
                        selected.add(candidates.get(idx));
                    }
                }
                paper.getQuestions().addAll(selected);
            }
            if (!paper.getQuestions().isEmpty()) {
                population.add(paper);
            }
        }
        return population;
    }

    // 2. 计算适应度
    private static void calculateFitness(PaperIndividual paper, double targetDiff, double expectedScore) {
        double actualTotalDifficulty = 0;
        double actualTotalScore = 0;

        for (BizQuestion q : paper.getQuestions()) {
            int score = getScoreByType(q.getQuestionType());
            actualTotalScore += score;
            // 累加难度：难度 * 分数 (加权难度)
            // 注意：如果 BizQuestion 没有 difficulty 字段，请确保在 BizQuestion 实体中添加了 getter
            actualTotalDifficulty += (q.getDifficulty() == null ? 0.5 : q.getDifficulty()) * score;
        }

        paper.setTotalScore(actualTotalScore);
        // 试卷整体难度
        paper.setDifficulty(actualTotalScore == 0 ? 0 : actualTotalDifficulty / actualTotalScore);

        // 误差计算
        double diffError = Math.abs(paper.getDifficulty() - targetDiff);

        // 适应度函数
        paper.setFitness(1.0 - (diffError * DIFF_WEIGHT));
    }

    // 3. 产生下一代
    private static List<PaperIndividual> nextGeneration(
            List<PaperIndividual> currentPop,
            Map<Integer, List<BizQuestion>> candidatePool,
            Map<Integer, Integer> constraints) {

        // 按适应度排序 (大到小)
        currentPop.sort((o1, o2) -> Double.compare(o2.getFitness(), o1.getFitness()));
        int keepSize = currentPop.size() / 2;
        List<PaperIndividual> parents = new ArrayList<>(currentPop.subList(0, keepSize));
        List<PaperIndividual> nextGen = new ArrayList<>(parents);

        Random rand = new Random();

        // 补足种群数量
        while (nextGen.size() < POPULATION_SIZE) {
            PaperIndividual p1 = parents.get(rand.nextInt(parents.size()));
            PaperIndividual p2 = parents.get(rand.nextInt(parents.size()));

            PaperIndividual child = new PaperIndividual();
            // 交叉
            child.setQuestions(crossoverQuestions(p1.getQuestions(), p2.getQuestions()));

            // 变异
            if (rand.nextDouble() < MUTATION_RATE) {
                mutate(child, candidatePool, constraints);
            }

            nextGen.add(child);
        }
        return nextGen;
    }

    private static List<BizQuestion> crossoverQuestions(List<BizQuestion> q1, List<BizQuestion> q2) {
        List<BizQuestion> childQs = new ArrayList<>();
        int size = q1.size();
        int cutPoint = size / 2;

        Set<Long> usedIds = new HashSet<>();

        for(int i=0; i<cutPoint; i++) {
            childQs.add(q1.get(i));
            usedIds.add(q1.get(i).getId());
        }

        for(int i=cutPoint; i<size; i++) {
            // 防止数组越界，取 min
            if (i >= q2.size()) break;

            BizQuestion q = q2.get(i);
            if (usedIds.contains(q.getId())) {
                childQs.add(q1.get(i));
            } else {
                childQs.add(q);
            }
        }
        return childQs;
    }

    private static void mutate(PaperIndividual paper, Map<Integer, List<BizQuestion>> pool, Map<Integer, Integer> constraints) {
        Random rand = new Random();
        List<BizQuestion> qs = paper.getQuestions();
        if (qs.isEmpty()) return;

        int index = rand.nextInt(qs.size());
        BizQuestion oldQ = qs.get(index);
        int type = oldQ.getQuestionType();

        List<BizQuestion> candidates = pool.get(type);
        if (candidates != null && !candidates.isEmpty()) {
            BizQuestion newQ = candidates.get(rand.nextInt(candidates.size()));
            boolean exists = false;
            for (BizQuestion q : qs) {
                if (q.getId().equals(newQ.getId())) { exists = true; break; }
            }
            if (!exists) {
                qs.set(index, newQ);
            }
        }
    }

    private static int getScoreByType(int type) {
        if (type == 1) return 2; // 单选
        if (type == 2) return 4; // 多选
        if (type == 3) return 2; // 填空
        if (type == 4) return 2; // 判断
        if (type == 5) return 10; // 主观
        return 0;
    }
}