/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import utils.PerformanceMetrics;
import emo.Individual;
import emo.OptimizationProblem;
import java.util.Arrays;
import javax.xml.stream.XMLStreamException;
import parsing.IndividualEvaluator;
import parsing.InvalidOptimizationProblemException;

/**
 * ZDT4 test problem
 *
 * @author Haitham Seada
 */
public class ZDT4Evaluator extends IndividualEvaluator {

    public final static double[] NADIR_POINT = {1.0, 1.0};
    public final static double[] IDEAL_POINT = {-0.0001, -0.0001};

    @Override
    public double[] getReferencePoint() {
        return Arrays.copyOf(NADIR_POINT, NADIR_POINT.length);
    }

    @Override
    public double[] getIdealPoint() {
        return Arrays.copyOf(IDEAL_POINT, IDEAL_POINT.length);
    }

    @Override
    public Individual[] getParetoFront(int objectivesCount, int n)
            throws
            InvalidOptimizationProblemException,
            XMLStreamException {
        if (objectivesCount != 2) {
            throw new UnsupportedOperationException(
                    "# of objectives must be 2.");
        }
        return PerformanceMetrics.getZDT4ParetoFront(this, n);
    }

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {
        double[] x = individual.real;
        double sum = 0;
        for (int i = 1; i < x.length; i++) {
            sum += Math.pow(x[i], 2) - 10 * Math.cos(4 * Math.PI * x[i]);
        }
        double g = 1.0 + 10 * (x.length - 1) + sum;
        individual.setObjective(0, x[0]);
        individual.setObjective(1, g * (1.0 - Math.sqrt(x[0] / g)));
        // Increase Evaluations Count by One (counted per individual)
        funEvaCount++;
        // Announce that objective function values are valid1
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
