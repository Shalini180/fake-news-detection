package com.fakenews.dto;

import lombok.Data;
import java.util.List;

/**
 * Uncertainty quantification metrics for ML predictions.
 * 
 * Provides formal uncertainty estimates using:
 * - Predictive entropy (normalized to [0,1])
 * - Monte Carlo dropout variance and standard deviation
 * - 95% confidence intervals
 */
@Data
public class UncertaintyDto {

    /**
     * Predictive entropy - measures prediction uncertainty.
     * Range: [0, 1]
     * 0 = completely certain
     * 1 = maximally uncertain
     */
    private Double entropy;

    /**
     * Variance across Monte Carlo dropout samples.
     * Higher variance indicates model disagreement.
     */
    private Double variance;

    /**
     * Standard deviation across MC samples.
     * Square root of variance, same interpretation.
     */
    private Double stdDev;

    /**
     * 95% confidence interval [lower, upper] for the prediction.
     * Based on MC dropout sampling distribution.
     */
    private List<Double> confidenceInterval;

    /**
     * Number of Monte Carlo dropout samples used.
     * Typically 20 for balance between accuracy and speed.
     */
    private Integer mcSamples;
}
