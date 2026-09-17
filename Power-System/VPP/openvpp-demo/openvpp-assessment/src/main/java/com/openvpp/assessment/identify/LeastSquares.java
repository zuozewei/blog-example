package com.openvpp.assessment.identify;

import java.util.List;

/**
 * 最小二乘线性拟合 —— 教学实现。
 *
 * 对点列 (x, y) 拟合 y = a + b·x，返回斜率 b 与截距 a。
 * 用于 ETP 参数辨识中「对数线性化」后的指数曲线参数反推。
 */
public final class LeastSquares {

    private LeastSquares() {
    }

    /**
     * 拟合结果。
     */
    public static final class FitResult {
        private final double slope;
        private final double intercept;

        public FitResult(double slope, double intercept) {
            this.slope = slope;
            this.intercept = intercept;
        }

        public double slope() { return slope; }
        public double intercept() { return intercept; }
    }

    /**
     * 对点列做最小二乘拟合。
     *
     * @param points 点列，至少 2 个点
     * @return 拟合结果（斜率 + 截距）
     */
    public static FitResult fit(List<double[]> points) {
        if (points == null || points.size() < 2) {
            throw new IllegalArgumentException("最小二乘至少需要 2 个点");
        }
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        int n = points.size();
        for (double[] p : points) {
            double x = p[0], y = p[1];
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }
        double denominator = n * sumXX - sumX * sumX;
        if (Math.abs(denominator) < 1e-12) {
            throw new IllegalStateException("x 值全相同，无法拟合");
        }
        double slope = (n * sumXY - sumX * sumY) / denominator;
        double intercept = (sumY - slope * sumX) / n;
        return new FitResult(slope, intercept);
    }
}
