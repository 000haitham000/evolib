package engines;

import emo.DoubleAssignmentException;
import emo.OptimizationProblem;
import evaluators.OSYEvaluator;
import evaluators.SingleObjective_G01_Evaluator;
import evaluators.SingleObjective_Rastrigin_Evaluator;
import evaluators.ZDT1Evaluator;
import org.junit.Test;
import parsing.IndividualEvaluator;
import parsing.InvalidOptimizationProblemException;
import parsing.StaXParser;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

public class NSGA2EngineTest {

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
                NSGA2Engine.class,
                new int[]{problem.getSteps()},
                new String[]{"matlab", "meta", "obj", "var"});
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
                NSGA2Engine.class,
                new int[]{problem.getSteps()},
                new String[]{"matlab", "meta", "obj", "var"});
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
                NSGA2Engine.class,
                new int[]{3, 2},
                new String[]{"meta", "obj", "var"});
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
                NSGA2Engine.class,
                new int[]{3, 2},
                new String[]{"meta", "obj", "var"});
    }
}