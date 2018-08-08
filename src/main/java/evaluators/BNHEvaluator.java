/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * BNH test problem
 *
 * @author Haitham Seada
 */
public class BNHEvaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {
        double[] x = individual.real;
        double obj0 = 4.0 * x[0] * x[0] + 4.0 * x[1] * x[1];
        double obj1 = (x[0] - 5) * (x[0] - 5) + (x[1] - 5) * (x[1] - 5);
        individual.setObjective(0, obj0);
        individual.setObjective(1, obj1);
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {

            // Four constraints   
            double[] g = new double[2];
            g[0] = 1.0 - ((x[0] - 5) * (x[0] - 5) + x[1] * x[1]) / 25.0;
            g[1] = ((x[0] - 8) * (x[0] - 8) + (x[1] + 3) * (x[1] + 3)) 
                    / 7.7 - 1.0;
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
        double[] refPoint = {137.037, 50};
        return refPoint;
    }

    @Override
    public double[] getIdealPoint() {
        //double[] refPoint = {0, 3.667};
        double[] refPoint = {-0.05, -0.05};
        return refPoint;
    }
}
