/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Single Objective Rastrigin test problem
 *
 * @author Haitham Seada
 */
public class SingleObjective_Rastrigin_Evaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {
        double[] x = individual.real;
        double sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += Math.pow(x[i], 2) - 10 * Math.cos(2 * Math.PI * x[i]);
        }
        double obj0 = 10 * x.length + sum;
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
