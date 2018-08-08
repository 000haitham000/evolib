/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import java.util.Arrays;
import parsing.IndividualEvaluator;

/**
 * Welded Beam test problem
 *
 * @author Haitham Seada
 */
public class WeldedBeamEvaluator extends IndividualEvaluator {

    public final static double[] NADIR_POINT = {40, 0.005};
    public final static double[] IDEAL_POINT = {0, 0};

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double td = 13600.0, sd = 30000.0, gmod = 12.0e06, emod = 30.0e06,
                force = 6000.0;

        double[] x = individual.real;
        double obj0 = (1.0 + 0.37 * 0.283) * x[0] * x[0] * x[1] + 0.17 * 0.283
                * x[2] * x[3] * (14.0 + x[1]);
        double obj1 = 4.0 * force * 14.0 * 14.0 * 14.0 / (emod * x[2] * x[2]
                * x[2] * x[3]);
        individual.setObjective(0, obj0);
        individual.setObjective(1, obj1);
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            double mom = force * (14.0 + x[1] / 2.0);
            double rad = Math.sqrt(x[1] * x[1] / 4.0 + Math.pow(0.5 * (x[2]
                    + x[0]), 2));
            double jmod = 2.0 * (x[0] * x[1] * (x[1] * x[1] / 12.0
                    + Math.pow(0.5 * (x[0] + x[2]), 2)) / Math.sqrt(2.0));

            double t1 = force / (Math.sqrt(2.0) * x[0] * x[1]);
            double t2 = mom * rad / jmod;
            double ctheta = x[1] / (2.0 * rad);
            double tx = Math.sqrt(Math.pow(t1, 2) + Math.pow(t2, 2) + 2.0 * t1
                    * t2 * ctheta);
            double sx = 6.0 * force * 14.0 / (x[3] * x[2] * x[2]);
            double ei = emod * x[2] * x[3] * x[3] * x[3] / 12.0;
            double alpha = gmod * x[2] * x[3] * x[3] * x[3] / 3.0;
            double pc = 4.013 * Math.sqrt(ei * alpha) * (1.0 - x[2]
                    * Math.sqrt(ei / alpha) / (2.0 * 14.0))
                    / (14.0 * 14.0);
            // Four constraints
            double[] g = new double[4];
            g[0] = 1.0 - tx / td;
            g[1] = 1.0 - sx / sd;
            g[2] = 1.0 - x[0] / x[3];
            g[3] = pc / force - 1.0;
            // Set constraints vilations
            for (int i = 0; i < g.length; i++) {
                if (g[i] < 0) {
                    individual.setConstraintViolation(i, g[i]);
                } else {
                    individual.setConstraintViolation(i, 0);
                }
            }
            // Announce that objective function values are valid
            individual.validConstraintsViolationValues = true;
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

}
