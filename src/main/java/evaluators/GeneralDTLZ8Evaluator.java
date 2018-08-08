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
 * DTLZ8 test problem
 *
 * @author Haitham Seada
 */
public class GeneralDTLZ8Evaluator extends IndividualEvaluator {

    public static double[] NADIR_POINT;
    public static double[] IDEAL_POINT;

    public GeneralDTLZ8Evaluator(OptimizationProblem problem) {
        if (problem.constraints.length != problem.objectives.length) {
            throw new IllegalArgumentException("For DTLZ8, the number of "
                    + "constraints must be equal to the number of objectives "
                    + "(by definition).");
        }
        // Ideal Point: All objectives are Zero
        IDEAL_POINT = new double[problem.objectives.length];
        // Nadir Point
        NADIR_POINT = new double[problem.objectives.length];
        for (int i = 0; i < NADIR_POINT.length - 1; i++) {
            NADIR_POINT[i] = 1.00;
        }
        NADIR_POINT[NADIR_POINT.length - 1] = 1;
    }

    private GeneralDTLZ8Evaluator() {
    }
    // For testing only (used inside the
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

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {
        // number of objectives
        int m = problem.objectives.length;
        // Design Variables (in DTLZ problems all variables must be real)
        double[] x = individual.real;
        // Calculate objective values
        double[] obj = getObjectives(x, m);
        // Copy objective values to the individual
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
            // In DTLZ8, the number of constraint must be equal to the number
            // of objectives (by definition).
            int conCount = problem.constraints.length;
            double[] cons = getConstraints(conCount, obj);
            // Copy constrained values to the individual
            for (int i = 0; i < cons.length; i++) {
                individual.setConstraintViolation(i, cons[i]);
            }
            // Announce that objective function values are valid
            individual.validConstraintsViolationValues = true;
        }
    }

    private double[] getConstraints(int conCount, double[] obj) {
        double[] cons = new double[conCount];
        // Set all but the last constraint.
        for (int i = 0; i < conCount - 1; i++) {
            cons[i] = obj[obj.length - 1] + 4 * obj[i] - 1;
        }
        // Set the last constraint
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < obj.length - 1; i++) {
            for (int j = 0; j < obj.length - 1; j++) {
                if (i != j) {
                    double sum = obj[i] + obj[j];
                    if (sum < min) {
                        min = sum;
                    }
                }
            }
        }
        cons[conCount - 1] = 2 * obj[obj.length - 1] + min - 1;
        return cons;
    }

    private double[] getObjectives(double[] x, int m)
            throws 
            UnsupportedOperationException {
        // Number of design variables
        int n = x.length;
        // Create objective functions
        double[] obj = new double[m];
        for (int i = 0; i < m; i++) {
            for (int j = (int) Math.floor(1.0 * i * n / m); 
                    j < (int) Math.floor(1.0 * (i + 1) * n / m);
                    j++) {
                obj[i] += x[j];
            }
            obj[i] = obj[i] / Math.floor(1.0 * n / m);
        }
        return obj;
    }
}
