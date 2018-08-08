/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Car Side Impact test problem
 *
 * @author Haitham Seada
 */
public class CarSideImpactEvaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;

        // Update constraint violations if constraints exist
        if (problem.constraints != null) {

            double[] yy = new double[2];
            yy[0] = 0.345;
            yy[1] = 0.192;
            // Four constraints
            double[] g = new double[10];
            g[0] = 1.16 - 0.3717 * x[1] * x[3] - 0.0092928 * x[2];
            g[1] = 0.261 - 0.0159 * x[0] * x[1] - 0.188 * x[0] * yy[0] - 0.019
                    * x[1] * x[6] + 0.0144 * x[2] * x[4] + 0.08045 * x[5]
                    * yy[1]; // [Right hand side (0.32) handled later]
            g[2] = 0.214 + 0.00817 * x[4] - 0.131 * x[0] * yy[0] - 0.0704 * x[0]
                    * yy[1] + 0.03099 * x[1] * x[5] - 0.018 * x[1] * x[6]
                    + 0.0208 * x[2] * yy[0] + 0.121 * x[2] * yy[1] - 0.00364
                    * x[4] * x[5] - 0.018 * x[1] * x[1]; // [Right hand side (0.32) handled later]
            g[3] = 0.74 - 0.61 * x[1] - 0.031296 * x[2] - 0.166 * x[6] * yy[1]
                    + 0.227 * x[1] * x[1];
            g[4] = 28.98 + 3.818 * x[2] - 4.2 * x[0] * x[1] + 6.63 * x[5]
                    * yy[1] - 7.77 * x[6] * yy[0]; // [Right hand side (32) handled later]
            g[5] = 33.86 + 2.95 * x[2] - 5.057 * x[0] * x[1] - 11 * x[1] * yy[0]
                    - 9.98 * x[6] * yy[0] + 22 * yy[0] * yy[1]; // [Right hand side (32) handled later]
            g[6] = 46.36 - 9.9 * x[1] - 12.9 * x[0] * yy[0]; // [Right hand side (32) handled later]
            g[7] = 4.72 - 0.5 * x[3] - 0.19 * x[1] * x[2]; // [Right hand side (4) handled later]
            g[8] = 10.58 - 0.674 * x[0] * x[1] - 1.95 * x[1] * yy[0]; // [Right hand side (9.9) handled later]
            g[9] = 16.45 - 0.489 * x[2] * x[6] - 0.843 * x[4] * x[5]; // [Right hand side (15.7) handled later]

            double obj0 = 1.98 + 4.9 * x[0] + 6.67 * x[1] + 6.98 * x[2] + 4.01
                    * x[3] + 1.78 * x[4] + 0.00001 * x[5] + 2.73 * x[6];
            double obj1 = g[7];
            double obj2 = (g[8] + g[9]) / 2.0;

            // Adjust right hand sides
            g[0] = 1 - g[0] / 1.0;
            g[1] = 1 - g[1] / 0.32;
            g[2] = 1 - g[2] / 0.32;
            g[3] = 1 - g[3] / 32;
            g[4] = 1 - g[4] / 32.0;
            g[5] = 1 - g[5] / 32.0;
            g[6] = 1 - g[6] / 32.0;
            g[7] = 1 - g[7] / 4.0;
            g[8] = 1 - g[8] / 9.9;
            g[9] = 1 - g[9] / 15.7;

            individual.setObjective(0, obj0);
            individual.setObjective(1, obj1);
            individual.setObjective(2, obj2);
            // Announce that objective function values are valid
            individual.validObjectiveFunctionsValues = true;

            /*
            for (int i = 0; i < g.length; i++) {
                g[i] = 1 - g[i];
            }
             */
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
                "Nadir point is not defined this problem.");
    }

    @Override
    public double[] getIdealPoint() {
        throw new UnsupportedOperationException(
                "Ideal point is not defined this problem.");
    }
}
