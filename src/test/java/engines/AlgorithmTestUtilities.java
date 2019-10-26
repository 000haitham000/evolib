package engines;

import distancemetrics.DistanceMetric;
import distancemetrics.PerpendicularDistanceMetric;
import emo.DoubleAssignmentException;
import emo.Individual;
import emo.OptimizationProblem;
import emo.OptimizationUtilities;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import parsing.IndividualEvaluator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class AlgorithmTestUtilities {

    public static void optimizeSaveAndAssert(
            OptimizationProblem problem,
            IndividualEvaluator evaluator,
            Class<?> optimizationClass,
            int[] divisions,
            String[] outFilePrefixList)
            throws
            IOException,
            DoubleAssignmentException {
        // Create the distance metric
        DistanceMetric distanceMetric = new PerpendicularDistanceMetric();
        // Specify output directory
        File outDir = getOutputDirectory(problem, optimizationClass);
        // Create Engine
        AbstractGeneticEngine geneticEngine = null;
        if(optimizationClass.equals(NSGA3Engine.class)) {
            geneticEngine = new NSGA3Engine(problem, evaluator, distanceMetric, outDir, divisions);
        } else if(optimizationClass.equals(UnifiedNSGA3Engine.class)) {
            geneticEngine = new UnifiedNSGA3Engine(problem, evaluator, distanceMetric, outDir, divisions);
        } else if(optimizationClass.equals(NSGA2Engine.class)) {
            geneticEngine = new NSGA2Engine(problem, evaluator, outDir);
        }
        // Optimize
        Individual[] finalPopulation = geneticEngine.start();
        // Display
        OptimizationUtilities.display(finalPopulation, System.out);
        // Make sure output files exist
        assertOutputFilesCount(problem, outDir, outFilePrefixList);
    }

    public static void assertOutputFilesCount(OptimizationProblem problem, File outDir, String[] prefixList) {
        for (String prefix : prefixList) {
            Assert.assertEquals(
                    problem.getGenerationsCount() + 1,
                    getFilesWithPrefixExist(prefix, outDir).length);
        }
    }

    public static File getOutputDirectory(OptimizationProblem problem, Class<?> optimizationClass)
            throws IOException {
        // Specify output directory
        File outDir = new File(
                String.format("%s/%s/%s/%s",
                        System.getProperty("user.home"),
                        "evolib",
                        optimizationClass.getName(),
                        problem.getProblemID()));
        // Clean the directory if it exists
        if (outDir.exists()) {
            FileUtils.cleanDirectory(outDir);
        }
        return outDir;
    }

    public static File[] getFilesWithPrefixExist(String prefix, File parentDirectory) {
        if(parentDirectory != null) {
            return parentDirectory.listFiles(new PrefixFileNameFilter(prefix));
        } else {
            return new File[]{};
        }
    }

    private static class PrefixFileNameFilter implements FilenameFilter {

        private String prefix;

        public PrefixFileNameFilter(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public boolean accept(File dir, String name) {
            if (name.startsWith(prefix)) {
                return true;
            }
            return false;
        }
    }
}
