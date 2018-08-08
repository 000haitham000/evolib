/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import utils.Mathematics;
import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Single Objective Thompson test problem
 *
 * @author Haitham Seada
 */
public class SingleObjective_Thompson_Evaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        final double KE = 1;
        final double E1 = 1;
        final double E2 = 1;

        double[] x = individual.real;

        double totalEnergy = 0;
        int m = 3;
        for (int i = 0; i < x.length - 2 * m; i += m) {

            double normI = norm(x, i, i + m - 1);
            double[] rI = new double[m];
            for (int j = 0; j < m; j++) {
                rI[j] = x[i + j] / normI;
            }

            for (int j = i + m; j < x.length - m; j += m) {
                double normJ = norm(x, j, j + m - 1);
                double[] rJ = new double[m];
                for (int k = 0; k < m; k++) {
                    rJ[k] = x[j + k] / normJ;
                }

                double[] r = new double[m];
                for (int k = 0; k < m; k++) {
                    r[k] = rI[k] - rJ[k];
                }

                int edgePointI = 0;
                for (int k = 0; k < rI.length; k++) {
                    if (Mathematics.compare(rI[k], 0, 0.001) == 0) {
                        edgePointI++;
                    }
                }
                int edgePointJ = 0;
                for (int k = 0; k < rJ.length; k++) {
                    if (Mathematics.compare(rJ[k], 0, 0.001) == 0) {
                        edgePointJ++;
                    }
                }

                totalEnergy += KE * (E1 + 1 * edgePointI)
                        * (E2 + 1 * edgePointJ) / norm(r, 0, m - 1);
            }
        }

        individual.setObjective(0, totalEnergy);
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
    }

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

    private static double norm(
            double[] v,
            int startIndexInclusive,
            int endIndexInclusive) {
        double sum = 0;
        for (int i = startIndexInclusive; i <= endIndexInclusive; i++) {
            sum += Math.pow(v[i], 2);
        }
        return Math.sqrt(sum);
    }
}
