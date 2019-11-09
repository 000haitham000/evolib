package engines;

import distancemetrics.DistanceMetric;
import distancemetrics.KKTPM2DistanceMetric;
import distancemetrics.PerpendicularDistanceMetric;
import emo.DoubleAssignmentException;
import emo.Individual;
import emo.OptimizationProblem;
import emo.OptimizationUtilities;
import evaluators.GeneralC3DTLZ1Evaluator;
import evaluators.GeneralDTLZ1Evaluator;
import evaluators.OSYEvaluator;
import evaluators.ZDT1Evaluator;
import org.junit.Test;
import parsing.IndividualEvaluator;
import parsing.InvalidOptimizationProblemException;
import parsing.StaXParser;
import parsing.XMLParser;
import utils.PerformanceMetrics;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.URISyntaxException;

public class NSGA3EngineTest {

    @Test
    public void solveBiObjectiveZDT1()
            throws
            Throwable {
        File configurationFile = new File(
                getClass().getClassLoader().getResource("configurations/zdt1-02-30.xml").toURI());
        // Read problem
        OptimizationProblem problem = StaXParser.readProblem(new FileInputStream(configurationFile));
        // Create Evaluator
        IndividualEvaluator evaluator = new ZDT1Evaluator();
        // Optimize, save and assert
        AlgorithmTestUtilities.optimizeSaveAndAssert(
                problem,
                evaluator,
                NSGA3Engine.class,
                new int[]{problem.getSteps()},
                new String[]{"matlab", "meta", "obj", "refdirs", "var"});
    }

    @Test
    public void solveBiObjectiveOSY()
            throws
            Throwable {
        File configurationFile = new File(
                getClass().getClassLoader().getResource("configurations/osy.xml").toURI());
        // Read problem
        OptimizationProblem problem = StaXParser.readProblem(new FileInputStream(configurationFile));
        // Create Evaluator
        IndividualEvaluator evaluator = new OSYEvaluator();
        // Optimize, save and assert
        AlgorithmTestUtilities.optimizeSaveAndAssert(
                problem,
                evaluator,
                NSGA3Engine.class,
                new int[]{problem.getSteps()},
                new String[]{"matlab", "meta", "obj", "refdirs", "var"});
    }

    @Test
    public void solveMultiObjectiveDTLZ1()
            throws
            Throwable {
        File configurationFile = new File(
                getClass().getClassLoader().getResource("configurations/dtlz1_05var_03obj.xml").toURI());
        // Read problem
        OptimizationProblem problem = StaXParser.readProblem(new FileInputStream(configurationFile));
        // Create Evaluator
        IndividualEvaluator evaluator = new GeneralDTLZ1Evaluator(problem);
        // Optimize, save and assert
        AlgorithmTestUtilities.optimizeSaveAndAssert(
                problem,
                evaluator,
                NSGA3Engine.class,
                new int[]{problem.getSteps()},
                new String[]{"matlab", "meta", "obj", "refdirs", "var"});
    }

    @Test
    public void solveManyObjectiveDTLZ1()
            throws
            Throwable {
        File configurationFile = new File(
                getClass().getClassLoader().getResource("configurations/dtlz1_12var_10obj.xml").toURI());
        // Read problem
        OptimizationProblem problem = StaXParser.readProblem(new FileInputStream(configurationFile));
        // Create Evaluator
        IndividualEvaluator evaluator = new GeneralDTLZ1Evaluator(problem);
        // Optimize, save and assert
        AlgorithmTestUtilities.optimizeSaveAndAssert(
                problem,
                evaluator,
                NSGA3Engine.class,
                new int[]{3, 2},
                new String[]{"meta", "obj", "refdirs", "var"});
    }

    @Test
    public void solveManyObjectiveC3DTLZ1()
            throws
            Throwable {
        File configurationFile = new File(
                getClass().getClassLoader().getResource("configurations/c3-dtlz1_14var_10obj_10con.xml").toURI());
        // Read problem
        OptimizationProblem problem = StaXParser.readProblem(new FileInputStream(configurationFile));
        // Create Evaluator
        IndividualEvaluator evaluator = new GeneralC3DTLZ1Evaluator(problem);
        // Optimize, save and assert
        AlgorithmTestUtilities.optimizeSaveAndAssert(
                problem,
                evaluator,
                NSGA3Engine.class,
                new int[]{3, 2},
                new String[]{"meta", "obj", "refdirs", "var"});
    }

    @Test
    public void solveBiObjectiveZDT1_KKTPM2()
            throws
            Throwable {
        // Read problem configuration
        File configurationFile = new File(
                getClass().getClassLoader().getResource("configurations/zdt1-02-30.xml").toURI());
        OptimizationProblem problemConfig = StaXParser.readProblem(new FileInputStream(configurationFile));
        // Read detailed problem description
        File problemDescriptionFile = new File(
                getClass().getClassLoader().getResource("problems/zdt1.xml").toURI());
        parsing.OptimizationProblem problemDescription = XMLParser.readXML(problemDescriptionFile);
        // Create Evaluator
        IndividualEvaluator evaluator = new ZDT1Evaluator();
        // Optimize, save and assert
        // Create the distance metric
        DistanceMetric distanceMetric = new PerpendicularDistanceMetric();
        //DistanceMetric distanceMetric = new KKTPM2DistanceMetric(problemDescription);
        // Specify output directory
        File outDir = AlgorithmTestUtilities.getOutputDirectory(problemConfig, NSGA3Engine.class);
        // Create Engine
        AbstractGeneticEngine geneticEngine = new NSGA3Engine(
                problemConfig,
                evaluator,
                distanceMetric,
                outDir,
                new int[]{problemConfig.getSteps()});
        geneticEngine.addParameter("pareto-front-file", new File(
                getClass().getClassLoader().getResource("fronts/zdt1-pf.dat").toURI()));
        // Adjust logging parameters
        geneticEngine.DUMP_GD = true;
        geneticEngine.DUMP_GD_PLUS = true;
        geneticEngine.DUMP_IGD = true;
        geneticEngine.DUMP_IGD_PLUS = true;
        // Optimize
        Individual[] finalPopulation = geneticEngine.start();
        // Display
        OptimizationUtilities.display(finalPopulation, System.out);
    }
}
