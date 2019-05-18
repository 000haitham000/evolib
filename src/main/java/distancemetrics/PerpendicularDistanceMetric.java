package distancemetrics;

import emo.Individual;
import emo.VirtualIndividual;
import refdirs.ReferenceDirection;
import utils.Mathematics;

public class PerpendicularDistanceMetric implements DistanceMetric {

    private double[] idealPoint;
    private double[] intercepts;
    private double utopianEpsilon;

    public PerpendicularDistanceMetric() {
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
        double requiredDistance = Mathematics.getPerpendicularDistance(
                normalizedObjectives, referenceDirection.direction);
        return requiredDistance;
    }
}
