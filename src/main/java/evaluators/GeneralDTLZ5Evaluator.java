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
 * DTLZ5 test problem
 *
 * @author Haitham Seada
 */
public class GeneralDTLZ5Evaluator extends IndividualEvaluator {

    public static double[] NADIR_POINT;
    public static double[] IDEAL_POINT;
    private boolean scaled = false;

    public GeneralDTLZ5Evaluator(OptimizationProblem problem) {
        // Ideal Point: All objectives are Zero
        IDEAL_POINT = new double[problem.objectives.length];
        // Nadir Point: All objectives are 0.5
        NADIR_POINT = new double[problem.objectives.length];
        for (int i = 0; i < NADIR_POINT.length; i++) {
            NADIR_POINT[i] = 1;
        }
    }

    private GeneralDTLZ5Evaluator() {
    }

    ; // For testing only (used inside the
    // main method of this class)

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

    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {
        // Design Variables (in DTLZ problems all variables must be real)
        double[] x = individual.real;
        // number of objectives
        int m = problem.objectives.length;
        double[] obj = getObjectives(x, m);
        // Copy the objective values to the actual individuals
        for (int i = 0; i < obj.length; i++) {
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

    private double[] getObjectives(double[] x, int m)
            throws
            UnsupportedOperationException {
        // Number of design variables
        int n = x.length;
        // (g) calculations
        double summation = 0;
        for (int i = m - 1; i < n; i++) {
            summation += Math.pow(x[i] - 0.5, 2);
        }
        double g = summation;
        // Calculate theta (specific to DTLZ5 & DTLZ6)
        double t = Math.PI / (4 * (1 + g));
        double[] theta = new double[m - 1];
        theta[0] = x[0] * Math.PI / 2;
        for (int i = 1; i < m - 1; i++) {
            theta[i] = t * (1 + 2 * g * x[i]);
        }
        // Create objective functions
        double[] obj = new double[m];
        for (int i = 0; i < m; i++) {
            double objValue = 1 + g;
            for (int j = 0; j < m - i - 1; j++) {
                objValue *= Math.cos(theta[j]);
            }
            if (i != 0) {
                objValue *= Math.sin(theta[m - i - 1]);
            }
            obj[i] = objValue;
        }
        // The following deals only with the scaled version of the problem
        if (isScaled()) {
            double base = 1;
            if (m <= 5) {
                base = 10;
            } else if (m <= 10) {
                base = 3;
            } else if (m <= 15) {
                base = 2;
            } else {
                throw new UnsupportedOperationException("Scaling is not "
                        + "supported at this number of objectives");
            }
            for (int i = 0; i < m; i++) {
                obj[i] = obj[i] * Math.pow(base, i);
            }
        }
        return obj;
    }

    /**
     * @return the scaled
     */
    public boolean isScaled() {
        return scaled;
    }

    /**
     * @param scaled the scaled to set
     */
    public void setScaled(boolean scaled) {
        this.scaled = scaled;
    }
}
