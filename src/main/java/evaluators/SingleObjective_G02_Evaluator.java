/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Single Objective G02 test problem
 *
 * @author Haitham Seada
 */
public class SingleObjective_G02_Evaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;
        // Objective function
        double cosSum = 0;
        for (int i = 0; i < x.length; i++) {
            cosSum += Math.pow(Math.cos(x[i]), 4);
        }
        double cosProd = 2;
        for (int i = 0; i < x.length; i++) {
            cosProd *= Math.pow(Math.cos(x[i]), 2);
        }
        double powerSum = 0;
        for (int i = 0; i < x.length; i++) {
            powerSum += (i + 1) * Math.pow(x[i], 2);
        }
        double rootPowerSum = Math.sqrt(powerSum);
        individual.setObjective(0, -1 * Math.abs((cosSum - cosProd)
                / rootPowerSum));

        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            // Two constraints
            double[] g = new double[2];
            double xProd = 1;
            for (int i = 0; i < x.length; i++) {
                xProd *= x[i];
            }
            g[0] = 0.75 - xProd;
            double xSum = 0;
            for (int i = 0; i < x.length; i++) {
                xSum += x[i];
            }
            g[1] = xSum - 7.5 * x.length;
            // Reverse constraints (because here we are using >= notation and
            // the paper is using <= notation)
            g[0] = g[0] * -1;
            g[1] = g[1] * -1;
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
