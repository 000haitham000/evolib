/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Single Objective G09 test problem
 *
 * @author Haitham Seada
 */
public class SingleObjective_G09_Evaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;
        double obj0 = Math.pow(x[0] - 10, 2)
                + 5 * Math.pow(x[1] - 12, 2)
                + Math.pow(x[2], 4)
                + 3 * Math.pow(x[3] - 11, 2)
                + 10 * Math.pow(x[4], 6)
                + 7 * Math.pow(x[5], 2)
                + Math.pow(x[6], 4)
                - 4 * x[5] * x[6]
                - 10 * x[5]
                - 8 * x[6];
        individual.setObjective(0, obj0);
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            // Nine constraints
            double[] g = new double[4];
            g[0] = (-127 + 2 * Math.pow(x[0], 2) + 3 * Math.pow(x[1], 4) + x[2]
                    + 4 * Math.pow(x[3], 2) + 5 * x[4]) / 127;
            g[1] = (-282 + 7 * x[0] + 3 * x[1] + 10 * Math.pow(x[2], 2) + x[3]
                    - x[4]) / 282;
            g[2] = (-196 + 23 * x[0] + Math.pow(x[1], 2) + 6 * Math.pow(x[5], 2)
                    - 8 * x[6]) / 196;
            g[3] = 4 * Math.pow(x[0], 2) + Math.pow(x[1], 2) - 3 * x[0] * x[1]
                    + 2 * Math.pow(x[2], 2) + 5 * x[5] - 11 * x[6];
            // Reverse constraints (the original paper(technical report) uses
            // <= notation)
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
