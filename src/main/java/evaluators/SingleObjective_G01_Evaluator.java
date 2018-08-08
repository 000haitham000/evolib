/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Single Objective G01 test problem
 *
 * @author Haitham Seada
 */
public class SingleObjective_G01_Evaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;
        double obj0 = 5 * (x[0] + x[1] + x[2] + x[3]) - 5 * (Math.pow(x[0], 2)
                + Math.pow(x[1], 2) + Math.pow(x[2], 2) + Math.pow(x[3], 2))
                - (x[4] + x[5] + x[6] + x[7] + x[8] + x[9] + x[10] + x[11]
                + x[12]);
        individual.setObjective(0, obj0);
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            // Nine constraints
            double[] g = new double[9];
            g[0] = (-2 * x[0] - 2 * x[1] - x[9] - x[10] + 10) / 10;
            g[1] = (-2 * x[0] - 2 * x[2] - x[9] - x[11] + 10) / 10;
            g[2] = (-2 * x[1] - 2 * x[2] - x[10] - x[11] + 10) / 10;
            g[3] = 8 * x[0] - x[9];
            g[4] = 8 * x[1] - x[10];
            g[5] = 8 * x[2] - x[11];
            g[6] = 2 * x[3] + x[4] - x[9];
            g[7] = 2 * x[5] + x[6] - x[10];
            g[8] = 2 * x[7] + x[8] - x[11];
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
