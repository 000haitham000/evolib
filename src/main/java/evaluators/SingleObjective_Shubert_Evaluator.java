/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Single Objective Shubert test problem
 *
 * @author Haitham Seada
 */
public class SingleObjective_Shubert_Evaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {
        double[] x = individual.real;
        double s1 = 0;
        double s2 = 0;
        for (int i = 0; i < 5; i++) {
            s1 = s1 + (i + 1) * Math.cos(((i + 1) + 1) * x[0] + (i + 1));
            s2 = s2 + (i + 1) * Math.cos(((i + 1) + 1) * x[1] + (i + 1));
        }
        double obj0 = s1 * s2;
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
