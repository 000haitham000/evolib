/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Single Objective G18 test problem
 *
 * @author Haitham Seada
 */
public class SingleObjective_G18_Evaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;
        double obj0 = -0.5 * (x[0] * x[3] - x[1] * x[2] + x[2] * x[8] - x[4]
                * x[8] + x[4] * x[7] - x[5] * x[6]);
        individual.setObjective(0, obj0);
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            // Nine constraints
            double[] g = new double[13];
            g[0] = Math.pow(x[2], 2) + Math.pow(x[3], 2) - 1;
            g[1] = Math.pow(x[8], 2) - 1;
            g[2] = Math.pow(x[4], 2) + Math.pow(x[5], 2) - 1;
            g[3] = Math.pow(x[0], 2) + Math.pow(x[1] - x[8], 2) - 1;
            g[4] = Math.pow(x[0] - x[4], 2) + Math.pow(x[1] - x[5], 2) - 1;
            g[5] = Math.pow(x[0] - x[6], 2) + Math.pow(x[1] - x[7], 2) - 1;
            g[6] = Math.pow(x[2] - x[4], 2) + Math.pow(x[3] - x[5], 2) - 1;
            g[7] = Math.pow(x[2] - x[6], 2) + Math.pow(x[3] - x[7], 2) - 1;
            g[8] = Math.pow(x[6], 2) + Math.pow(x[7] - x[8], 2) - 1;
            g[9] = x[1] * x[2] - x[0] * x[3];
            g[10] = -1 * x[2] * x[8];
            g[11] = x[4] * x[8];
            g[12] = x[5] * x[6] - x[4] * x[7];
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
