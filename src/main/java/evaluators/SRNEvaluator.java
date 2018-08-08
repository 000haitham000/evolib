/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * SRN test problem
 *
 * @author Haitham Seada
 */
public class SRNEvaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {
        double[] x = individual.real;
        double obj0 = 2 + (x[0] - 2) * (x[0] - 2) + (x[1] - 1) * (x[1] - 1);
        double obj1 = 9.0 * x[0] - (x[1] - 1) * (x[1] - 1);
        individual.setObjective(0, obj0);
        individual.setObjective(1, obj1);
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {

            // Four constraints   
            double[] g = new double[2];
            g[0] = 1.0 - (x[0] * x[0] + x[1] * x[1]) / 225.0;
            g[1] = (3 * x[1] - x[0]) / 10.0 - 1.0;
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
        // Increase Evaluations Count by One (counted per individual)
        funEvaCount++;
    }

    @Override
    public double[] getReferencePoint() {
        double[] refPoint = {225, 0};
        return refPoint;
    }

    @Override
    public double[] getIdealPoint() {
        double[] refPoint = {0, -300};
        return refPoint;
    }
}
