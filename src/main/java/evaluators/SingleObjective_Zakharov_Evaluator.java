/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Single Objective Zakharov test problem
 *
 * @author Haitham Seada
 */
public class SingleObjective_Zakharov_Evaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;

        double sqSum = 0;
        for (int i = 0; i < x.length; i++) {
            sqSum += Math.pow(x[i], 2);
        }
        double iSum = 0;
        for (int i = 0; i < x.length; i++) {
            iSum += (i + 1) * x[i];
        }
        iSum *= 0.5;

        double obj0 = sqSum + Math.pow(iSum, 2) + Math.pow(iSum, 4);
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
