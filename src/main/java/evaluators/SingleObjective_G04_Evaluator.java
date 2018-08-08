/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Single Objective G04 test problem
 *
 * @author Haitham Seada
 */
public class SingleObjective_G04_Evaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;
        double obj0 = 5.3578547 * Math.pow(x[2], 2) + 0.8356891 * x[0] * x[4]
                + 37.293239 * x[0] - 40792.141;
        individual.setObjective(0, obj0);
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            // Nine constraints
            double[] g = new double[6];
            g[0] = (-85.334407 - 0.0056858 * x[1] * x[4] - 0.0006262 * x[0]
                    * x[3] + 0.0022053 * x[2] * x[4] + 92) / 6.665593;
            g[1] = (+85.334407 + 0.0056858 * x[1] * x[4] + 0.0006262 * x[0]
                    * x[3] - 0.0022053 * x[2] * x[4]) / 85.334407;
            g[2] = (-80.51249 - 0.0071317 * x[1] * x[4] - 0.0029955 * x[0]
                    * x[1] - 0.0021813 * Math.pow(x[2], 2) + 110) / 29.48751;
            g[3] = (+80.51249 + 0.0071317 * x[1] * x[4] + 0.0029955 * x[0]
                    * x[1] + 0.0021813 * Math.pow(x[2], 2) - 90) / 9.48751;
            g[4] = (-9.300961 - 0.0047026 * x[2] * x[4] - 0.0012547 * x[0]
                    * x[2] - 0.0019085 * x[2] * x[3] + 25) / 15.699039;
            g[5] = (+9.300961 + 0.0047026 * x[2] * x[4] + 0.0012547 * x[0]
                    * x[2] + 0.0019085 * x[2] * x[3] - 20) / 10.699039;
            // Set constraints vilations
            for (int i = 0; i < g.length; i++) {
                if (g[i] < 0) {
                    individual.setConstraintViolation(i, g[i]);
                } else {
                    individual.setConstraintViolation(i, 0);
                }
            }
            // Announce that constraints violation values are valid
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
