/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import java.util.Arrays;
import javax.xml.stream.XMLStreamException;
import parsing.IndividualEvaluator;
import parsing.InvalidOptimizationProblemException;

/**
 * DTLZ7 test problem
 *
 * @author Haitham Seada
 */
public class GeneralDTLZ7Evaluator extends IndividualEvaluator {

    public static double[] NADIR_POINT;
    public static double[] IDEAL_POINT;

    public GeneralDTLZ7Evaluator(OptimizationProblem problem) {
        // Ideal Point: All objectives are Zero
        IDEAL_POINT = new double[problem.objectives.length];
        // Nadir Point: All objectives are 0.5
        NADIR_POINT = new double[problem.objectives.length];
        for (int i = 0; i < NADIR_POINT.length; i++) {
            NADIR_POINT[i] = 1;
        }
    }

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
        throw new UnsupportedOperationException("Pareto front unavailable");
    }

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {
        // Design Variables (in DTLZ problems all variables must be real)
        double[] x = individual.real;
        // Number of design variables
        int n = x.length;
        // number of objectives
        int m = problem.objectives.length;
        // (g) calculations
        double summation = 0;
        for (int i = m - 1; i < n; i++) {
            summation += x[i];
        }
        double g = 9 * summation / (n - m + 1);
        // Create objective functions
        double[] obj = new double[m];
        // Set the first m-1 objectives
        System.arraycopy(x, 0, obj, 0, m - 1);
        // Calculate h
        double h = 0;
        for (int j = 0; j < m - 1; j++) {
            h += obj[j] * (1 + Math.sin(3 * Math.PI * obj[j]));
        }
        h = m - (h / (1 + g));
        obj[m - 1] = (1 + g) * h;
        // Set the actual objective values
        for (int i = 0; i < m; i++) {
            individual.setObjective(i, obj[i]);
        }
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
