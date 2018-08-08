/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import javax.xml.stream.XMLStreamException;
import parsing.IndividualEvaluator;
import parsing.InvalidOptimizationProblemException;

/**
 * Single Objective G24 test problem
 *
 * @author Haitham Seada
 */
public class SingleObjective_G24_Evaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;
        double obj0 = -1 * x[0] - x[1];
        individual.setObjective(0, obj0);
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            // Nine constraints
            double[] g = new double[2];
            g[0] = (-2 * Math.pow(x[0], 4) + 8 * Math.pow(x[0], 3) - 8
                    * Math.pow(x[0], 2) + x[1] - 2) / 2;
            g[1] = (-4 * Math.pow(x[0], 4) + 32 * Math.pow(x[0], 3) - 88
                    * Math.pow(x[0], 2) + 96 * x[0] + x[1] - 36) / 36;
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
