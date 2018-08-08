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
 * Sample Single Objective problem. min: f = x1^2 + x2^2

 * @author Haitham Seada
 */
public class SampleSingleObjectiveEvaluator extends IndividualEvaluator {

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

    @Override
    public Individual[] getParetoFront(int objectivesCount, int n) 
            throws 
            InvalidOptimizationProblemException, 
            XMLStreamException {
        throw new UnsupportedOperationException(
                "Single objective problems do not have a Pareto front");
    }

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;
        // The Single Objective
        double obj0 = Math.pow(x[0], 2) + Math.pow(x[1], 2);
        individual.setObjective(0, obj0);
        // Increase Evaluations Count by One (counted per individual)
        funEvaCount++;
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            // Evaluate the final expression and store the results as the 
            // individual's constraints values.
            for (int i = 0; i < problem.constraints.length; i++) {
                individual.setConstraintViolation(i, 0.0);
            }
            // Announce that objective function values are valid
            individual.validConstraintsViolationValues = true;
        }
    }
}
