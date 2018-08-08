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
 * ZDT6 test problem
 *
 * @author Haitham Seada
 */
public class ZDT6Evaluator extends IndividualEvaluator {

    public final static double[] NADIR_POINT = {1.0, 1.0};
    public final static double[] IDEAL_POINT = {0.2807753191, 0};

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
        return PerformanceMetrics.getZDT6ParetoFront(this, n);
    }

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;

        double obj0 = 1 - Math.exp(-4 * x[0])
                * Math.pow(Math.sin(6 * Math.PI * x[0]), 6);
        double g = 0;
        for (int i = 1; i < 10; i++) {
            g += x[i];
        }
        g = 1 + 9 * Math.pow(g / 9, 0.25);
        double h = 1 - Math.pow(obj0 / g, 2);
        double obj1 = g * h;
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
