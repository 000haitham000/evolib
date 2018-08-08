/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Single Objective Ackley test problem
 *
 * @author Haitham Seada
 */
public class SingleObjective_Ackley_Evaluator extends IndividualEvaluator {
    
    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {
        
        double[] x = individual.real;
        
        double sum1 = 0;
        for (int i = 0; i < x.length; i++) {
            sum1 += Math.pow(x[i], 2);
        }
        double sum2 = 0;
        for (int i = 0; i < x.length; i++) {
            sum2 += Math.cos(2 * Math.PI * x[i]);
        }
        
        double obj0
                = -20 * Math.exp(-0.2 * Math.sqrt(1.0/x.length * sum1))
                - Math.exp(1.0/x.length * sum2)
                + 20
                + Math.exp(1);
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
