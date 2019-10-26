package distancemetrics;

import emo.Individual;
import exceptions.MisplacedTokensException;
import exceptions.TooManyDecimalPointsException;
import kktpm.KKTPMCalculator;
import parsing.*;
import refdirs.ReferenceDirection;

import java.util.List;

public class KKTPM2DistanceMetric implements DistanceMetric {

    private OptimizationProblem problem;

    public KKTPM2DistanceMetric(OptimizationProblem problem) {
        this.problem = problem;
    }

    public static double getPopulationMetric(
            DistanceMetric distanceMetric,
            Individual[] individuals,
            List<ReferenceDirection> referenceDirections,
            double[] idealPoint) {
        double[][] directions2IndividualsMetricMap = new double[referenceDirections.size()][individuals.length];
        // Build the metric matrix
        for (int i = 0; i < referenceDirections.size(); i++) {
            for (int j = 0; j < individuals.length; j++) {
                directions2IndividualsMetricMap[i][j] =
                        distanceMetric.getDistance(
                                individuals[j],
                                referenceDirections.get(i),
                                idealPoint,
                                null,
                                Double.NaN);
            }
        }
        // Find the closest individual to each direction and add its metric value to the total
        double totalMetric = 0.0;
        for (int i = 0; i < referenceDirections.size(); i++) {
            double minValue = Double.POSITIVE_INFINITY;
            for (int j = 0; j < individuals.length; j++) {
                if (directions2IndividualsMetricMap[i][j] < minValue) {
                    minValue = directions2IndividualsMetricMap[i][j];
                }
            }
            if (Math.abs(Double.POSITIVE_INFINITY - minValue) < 0.1) {
                throw new IllegalArgumentException("PROBLEM!");
            }
            totalMetric += minValue;
        }
        // Return the mean
        return totalMetric / referenceDirections.size();
    }

    @Override
    public double getDistance(
            Individual individual,
            ReferenceDirection referenceDirection,
            double[] idealPoint,
            double[] intercepts,
            double utopianEpsilon) {
        problem.setVector("x", individual.real);
        double kktpm2 = Double.NaN;
        try {
            kktpm2 = KKTPMCalculator.getKKTPM2(
                    problem,
                    idealPoint,
                    changeMinValue(referenceDirection.direction, 1E-10))
                    .getKktpm();
        } catch (TooManyDecimalPointsException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (MisplacedTokensException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return kktpm2;
    }

    private static double[] changeMinValue(double[] v, double minValue) {
        double[] noZeroV = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            if (v[i] < minValue) {
                noZeroV[i] = minValue;
            } else {
                noZeroV[i] = v[i];
            }
        }
        return noZeroV;
    }
}
