package distancemetrics;

import emo.Individual;
import refdirs.ReferenceDirection;

public interface DistanceMetric {

    double getDistance(
            Individual individual,
            ReferenceDirection referenceDirection,
            double[] idealPoint,
            double[] intercepts,
            double utopianEpsilon);
}
