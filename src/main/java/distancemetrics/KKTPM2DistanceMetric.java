package distancemetrics;

import emo.Individual;
import exceptions.MisplacedTokensException;
import exceptions.TooManyDecimalPointsException;
import kktpm.KKTPMCalculator;
import parsing.OptimizationProblem;
import refdirs.ReferenceDirection;

public class KKTPM2DistanceMetric implements DistanceMetric {

    private OptimizationProblem problem;

    public KKTPM2DistanceMetric(OptimizationProblem problem) {
        this.problem = problem;
    }

    @Override
    public double getDistance(
            Individual individual,
            ReferenceDirection referenceDirection,
            double[] idealPoint,
            double[] intercepts,
            double utopianEpsilon) {
        double[] normalizedObjectives = DistanceMetricUtilities.getNormalizedObjectives(
                individual, idealPoint, intercepts, utopianEpsilon);
        problem.setVector("x", individual.real);
        double kktpm2 = Double.NaN;
        try {
            kktpm2 = KKTPMCalculator.getKKTPM2(
                    problem,
                    idealPoint,
                    referenceDirection.direction)
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
}
