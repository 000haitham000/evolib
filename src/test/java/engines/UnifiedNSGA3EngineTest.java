package engines;

import emo.DoubleAssignmentException;
import emo.OptimizationProblem;
import evaluators.*;
import org.junit.Test;
import parsing.IndividualEvaluator;
import parsing.InvalidOptimizationProblemException;
import parsing.StaXParser;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

public class UnifiedNSGA3EngineTest {

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
                UnifiedNSGA3Engine.class,
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
                UnifiedNSGA3Engine.class,
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
                UnifiedNSGA3Engine.class,
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
                UnifiedNSGA3Engine.class,
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
                UnifiedNSGA3Engine.class,
                new int[]{3, 2},
                new String[]{"meta", "obj", "refdirs", "var"});
    }

    @Test
    public void solveSingleObjectiveRastrigin()
            throws
            Throwable {
        File configurationFile = new File(
                getClass().getClassLoader().getResource("configurations/single-objective-rastrigin.xml").toURI());
        // Read problem
        OptimizationProblem problem = StaXParser.readProblem(new FileInputStream(configurationFile));
        // Create Evaluator
        IndividualEvaluator evaluator = new SingleObjective_Rastrigin_Evaluator();
        // Optimize, save and assert
        AlgorithmTestUtilities.optimizeSaveAndAssert(
                problem,
                evaluator,
                UnifiedNSGA3Engine.class,
                new int[]{3, 2},
                new String[]{"meta", "obj", "refdirs", "var"});
    }

    @Test
    public void solveSingleObjectiveG1()
            throws
            Throwable {
        File configurationFile = new File(
                getClass().getClassLoader().getResource("configurations/single-objective-g01.xml").toURI());
        // Read problem
        OptimizationProblem problem = StaXParser.readProblem(new FileInputStream(configurationFile));
        // Create Evaluator
        IndividualEvaluator evaluator = new SingleObjective_G01_Evaluator();
        // Optimize, save and assert
        AlgorithmTestUtilities.optimizeSaveAndAssert(
                problem,
                evaluator,
                UnifiedNSGA3Engine.class,
                new int[]{3, 2},
                new String[]{"meta", "obj", "refdirs", "var"});
    }
}