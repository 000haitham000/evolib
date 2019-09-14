package distancemetrics;

import emo.Individual;
import evaluators.ZDT1Evaluator;
import exceptions.MisplacedTokensException;
import exceptions.TooManyDecimalPointsException;
import kktpm.KKTPMCalculator;
import optimization.SampleScript;
import parsing.*;
import refdirs.ReferenceDirection;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
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
                if(directions2IndividualsMetricMap[i][j] < minValue) {
                    minValue = directions2IndividualsMetricMap[i][j];
                }
            }
            if(Math.abs(Double.POSITIVE_INFINITY - minValue) < 0.1) {
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
        if (Double.isNaN(kktpm2)) {
            System.out.println("STOP!");
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

    public static void main(String[] args) throws Throwable {
        String problemDefinitionFilePath = "fullproblems/zdt1.xml";
        // Create the distance metric
        URL url2 = SampleScript.class.getClassLoader().getResource(problemDefinitionFilePath);
        OptimizationProblem problem = XMLParser.readXML(new File(url2.getFile()));
        double[] x = {0.005065176101100577, 0.24228979916064308, 0.638051895030216, 0.028511432927259106, 0.6630621930951288, 0.5195760217178331, 0.8026922643429892, 0.5914570068228723, 0.27057779733556053, 0.5922785232042694, 0.9520708133983169, 0.38334556740659276, 0.1459470507434577, 0.6199474827869116, 0.14599438755486827, 0.9093004421866538, 0.7417522157525359, 0.5992397765005402, 0.511315215257181, 0.012403566406870126, 0.6919607667236274, 0.674203585728449, 0.3380080916453785, 0.6070230481955768, 0.31699579503823117, 0.8366015515673879, 0.6278242708580304, 0.5288788299923252, 0.2364277742638098, 0.4367130948868102};
        double[] w = {0.0203, 0.9798};
        double[] ideal = {-0.001, -0.001};
        problem.setVector("x", x);
        double kktpm2 = KKTPMCalculator.getKKTPM2(
                problem,
                ideal,
                w)
                .getKktpm();
        System.out.println(kktpm2);
    }
}
