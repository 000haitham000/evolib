/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Single Objective Rosenbrock test problem
 *
 * @author Haitham Seada
 */
public class SingleObjective_Rosenbrock_Evaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;
        double sum = 0;
        for (int i = 0; i < x.length - 1; i++) {
            sum += 100 * Math.pow(Math.pow(x[i], 2) - x[i + 1], 2)
                    + Math.pow(x[i] - 1, 2);
        }
        double obj0 = sum;
        individual.setObjective(0, obj0);
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
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
