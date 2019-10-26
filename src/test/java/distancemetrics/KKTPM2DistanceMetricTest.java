package distancemetrics;

import kktpm.KKTPMCalculator;
import org.junit.Assert;
import org.junit.Test;
import parsing.OptimizationProblem;
import parsing.XMLParser;

import java.io.File;

public class KKTPM2DistanceMetricTest {

    @Test
    public void testKKTPMDistanceMetric() throws Throwable {
        File configurationFile = new File(
                getClass().getClassLoader().getResource("problems/zdt1.xml").toURI());
        // Read problem
        OptimizationProblem problem = XMLParser.readXML(configurationFile);
        double[] x = {0.005065176101100577, 0.24228979916064308, 0.638051895030216, 0.028511432927259106, 0.6630621930951288, 0.5195760217178331, 0.8026922643429892, 0.5914570068228723, 0.27057779733556053, 0.5922785232042694, 0.9520708133983169, 0.38334556740659276, 0.1459470507434577, 0.6199474827869116, 0.14599438755486827, 0.9093004421866538, 0.7417522157525359, 0.5992397765005402, 0.511315215257181, 0.012403566406870126, 0.6919607667236274, 0.674203585728449, 0.3380080916453785, 0.6070230481955768, 0.31699579503823117, 0.8366015515673879, 0.6278242708580304, 0.5288788299923252, 0.2364277742638098, 0.4367130948868102};
        double[] w = {0.0203, 0.9798};
        double[] ideal = {-0.001, -0.001};
        problem.setVector("x", x);
        double kktpm2 = KKTPMCalculator.getKKTPM2(
                problem,
                ideal,
                w)
                .getKktpm();
        Assert.assertEquals(1.6801397306450323, kktpm2, 10e-10);
    }
}