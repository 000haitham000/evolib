package distancemetrics;

import emo.VirtualIndividual;

public class DistanceMetricUtilities {

    public static double[] getNormalizedObjectives(
            VirtualIndividual individual,
            double[] idealPoint,
            double[] intercepts,
            double utopianEpsilon) {
        double[] normalizedVector = new double[individual.getObjectivesCount()];
        for (int i = 0; i < individual.getObjectivesCount(); i++) {
            normalizedVector[i]
                    = (individual.getObjective(i) - idealPoint[i])
                    / intercepts[i] - utopianEpsilon;
        }
        return normalizedVector;
    }
}
