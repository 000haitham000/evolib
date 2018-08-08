/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Single Objective G10 test problem
 *
 * @author Haitham Seada
 */
public class SingleObjective_G10_Evaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;
        double obj0 = x[0] + x[1] + x[2];
        individual.setObjective(0, obj0);
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            // Nine constraints
            double[] g = new double[6];
            g[0] = -1 + 0.0025 * (x[3] + x[5]);
            g[1] = -1 + 0.0025 * (x[4] + x[6] - x[3]);
            g[2] = -1 + 0.01 * (x[7] - x[4]);
            g[3] = (-1 * x[0] * x[5] + 833.33252 * x[3] + 100 * x[0]
                    - 83333.333) / 83333.333;
            g[4] = -1 * x[1] * x[6] + 1250 * x[4] + x[1] * x[3] - 1250 * x[3];
            g[5] = (-1 * x[2] * x[7] + 1250000 + x[2] * x[4] - 2500 * x[4])
                    / 1250000;
            // Reverse constraints (the original paper(technical report)
            // uses <= notation)
            for (int i = 0; i < g.length; i++) {
                g[i] = -1 * g[i];
            }
            // Set constraints vilations
            for (int i = 0; i < g.length; i++) {
                if (g[i] < 0) {
                    individual.setConstraintViolation(i, g[i]);
                } else {
                    individual.setConstraintViolation(i, 0);
                }
            }
            // Announce that objective function values are valid
            individual.validConstraintsViolationValues = true;
        }
    }

    @Override
    public double[] getReferencePoint() {
        throw new UnsupportedOperationException(
                "Single objective problems do not have a nadir point");
    }

    @Override
    public double[] getIdealPoint() {
        throw new UnsupportedOperationException(
                "Single objective problems do not have an ideal point");
    }
}
