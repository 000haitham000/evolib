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
 * Pressure Vessel test problem
 *
 * @author Haitham Seada
 */
public class PressureVesselEvaluator extends IndividualEvaluator {

    public final static double[] NADIR_POINT = {327000, -8000};
    public final static double[] IDEAL_POINT = {42, -62761000};

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double Ts = individual.real[0];
        double Th = individual.real[1];
        double R = individual.real[2];
        double L = individual.real[3];

        double obj0
                = 0.6224 * Ts * L * R + 1.7781 * Th * Math.pow(R, 2)
                + 3.1661 * Math.pow(Ts, 2) * L
                + 19.84 * Math.pow(Ts, 2) * R;
        double obj1
                = -(Math.PI * Math.pow(R, 2) * L
                + 1.333 * Math.PI * Math.pow(R, 3));
        individual.setObjective(0, obj0);
        individual.setObjective(1, obj1);
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            // Four constraints
            double[] g = new double[10];
            g[0] = Ts - 0.0193 * R;
            g[1] = Th - 0.00954 * R;
            g[2] = Ts - 0.0625;
            g[3] = 5 - Ts;
            g[4] = Th - 0.0625;
            g[5] = 5 - Th;
            g[6] = R - 10;
            g[7] = 200 - R;
            g[8] = L - 10;
            g[9] = 240 - L;
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
