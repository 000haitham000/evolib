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
 * Variable Density test problem
 *
 * @author Haitham Seada
 */
public class VariableDensityEvaluator extends IndividualEvaluator {

    public final static double[] NADIR_POINT = {1.0, 1.0};
    public final static double[] IDEAL_POINT = {-0.001, -0.001};

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
        return PerformanceMetrics.getZDT1ParetoFront(this, n);
    }

    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;

        double obj0 = Math.pow(x[0], 0.05);
        double sum = 0;
        for (int i = 1; i < x.length; i++) {
            sum += x[i];
        }
        double g = 1 + 1.0 / (x.length - 1) * sum;

        double u = 3.5;
        double n = 5;
        double total_max = 2;
        double total_min = -1 - Math.pow(u - 1, 3);

        double obj1 = g * ((Math.cos(n * Math.PI * obj0)
                - Math.pow(u * obj0 - 1, 3)) - total_min)
                / (total_max - total_min);

        individual.setObjective(0, obj0);
        individual.setObjective(1, obj1);
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
