/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Single Objective G07 test problem
 *
 * @author Haitham Seada
 */
public class SingleObjective_G07_Evaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;

        double obj0
                = Math.pow(x[0], 2)
                + Math.pow(x[1], 2)
                + x[0] * x[1]
                - 14 * x[0]
                - 16 * x[1]
                + Math.pow(x[2] - 10, 2)
                + 4 * Math.pow(x[3] - 5, 2)
                + Math.pow(x[4] - 3, 2)
                + 2 * Math.pow(x[5] - 1, 2)
                + 5 * Math.pow(x[6], 2)
                + 7 * Math.pow(x[7] - 11, 2)
                + 2 * Math.pow(x[8] - 10, 2)
                + Math.pow(x[9] - 7, 2)
                + 45;
        individual.setObjective(0, obj0);
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            // Nine constraints
            double[] g = new double[8];
            g[0] = -105 + 4 * x[0] + 5 * x[1] - 3 * x[6] + 9 * x[7];
            g[1] = 10 * x[0] - 8 * x[1] - 17 * x[6] + 2 * x[7];
            g[2] = -8 * x[0] + 2 * x[1] + 5 * x[8] - 2 * x[9] - 12;
            g[3] = 3 * Math.pow(x[0] - 2, 2) + 4 * Math.pow(x[1] - 3, 2)
                    + 2 * Math.pow(x[2], 2) - 7 * x[3] - 120;
            g[4] = 5 * Math.pow(x[0], 2) + 8 * x[1] + Math.pow(x[2] - 6, 2)
                    - 2 * x[3] - 40;
            g[5] = Math.pow(x[0], 2) + 2 * Math.pow(x[1] - 2, 2) - 2 * x[0]
                    * x[1] + 14 * x[4] - 6 * x[5];
            g[6] = 0.5 * Math.pow(x[0] - 8, 2) + 2 * Math.pow(x[1] - 4, 2) + 3
                    * Math.pow(x[4], 2) - x[5] - 30;
            g[7] = -3 * x[0] + 6 * x[1] + 12 * Math.pow(x[8] - 8, 2) - 7 * x[9];
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
