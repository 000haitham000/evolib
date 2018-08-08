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
 * C3-DTLZ1 test problem
 * @author Haitham Seada
 */
public class GeneralC3DTLZ1Evaluator extends IndividualEvaluator {

    public static double[] NADIR_POINT;
    public static double[] IDEAL_POINT;

    public GeneralC3DTLZ1Evaluator(OptimizationProblem problem) {
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
            summation += Math.pow(x[i] - 0.5, 2)
                    - Math.cos(20 * Math.PI * (x[i] - 0.5));
        }
        double g = 100 * (n - m + 1 + summation);
        // Create objective functions
        for (int i = 0; i < m; i++) {
            double objValue = 0.5 * (1 + g);
            for (int j = 0; j < m - i - 1; j++) {
                objValue *= x[j];
            }
            if (i != 0) {
                objValue *= (1 - x[m - i - 1]);
            }
            individual.setObjective(i, objValue);
        }
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            // In C3-DTLZ1, the number of constraints is equal to the number of
            // objectives
            double[] constraints = new double[problem.objectives.length];
            for (int j = 0; j < constraints.length; j++) {
                constraints[j] = individual.getObjective(j) - 1;
                for (int i = 0; i < problem.objectives.length; i++) {
                    if (i != j) {
                        constraints[j] += individual.getObjective(i) / 0.5;
                    }
                }
            }
            // Set constraints vilations
            for (int i = 0; i < constraints.length; i++) {
                if (constraints[i] < 0) {
                    individual.setConstraintViolation(i, constraints[i]);
                } else {
                    individual.setConstraintViolation(i, 0);
                }
            }
            // Announce that objective function values are valid
            individual.validConstraintsViolationValues = true;
        }
        // Increase Evaluations Count by One (counted per individual)
        funEvaCount++;
    }
}
