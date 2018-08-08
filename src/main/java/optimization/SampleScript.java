/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import emo.DoubleAssignmentException;
import emo.Individual;
import emo.OptimizationProblem;
import engines.AbstractGeneticEngine;
import engines.NSGA2Engine;
import evaluators.OSYEvaluator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.stream.XMLStreamException;
import parsing.IndividualEvaluator;
import parsing.InvalidOptimizationProblemException;
import parsing.StaXParser;

/**
 * This is a sample class showing the simplest way to use EvoLib
 *
 * @author Haitham Seada
 */
public class SampleScript extends TestScript {

    public static void main(String[] args)
            throws XMLStreamException,
            InvalidOptimizationProblemException,
            IOException,
            FileNotFoundException,
            DoubleAssignmentException {
        // Read problem
        URL url = SampleScript.class.getClassLoader().getResource(
                "samples/osy.xml");
        InputStream in = url.openStream();

        OptimizationProblem optimizationProblem = StaXParser.readProblem(in);
        // Create Evaluator
        IndividualEvaluator individualEvaluator = new OSYEvaluator();
        // Create Engine
        AbstractGeneticEngine geneticEngine = new NSGA2Engine(
                optimizationProblem,
                individualEvaluator);
        // Specify output directory
        File outDir = new File(System.getProperty("user.home") + "/evolib");
        outDir.mkdirs();
        // Optimize
        Individual[] finalPopulation
                = geneticEngine.start(
                        outDir,
                        0,
                        0,
                        Double.MAX_VALUE,
                        Integer.MAX_VALUE);
        // Display
        display(finalPopulation);
    }

    /**
     * Display all population members.
     *
     * @param population a population of individuals
     */
    private static void display(Individual[] population) {
        System.out.println();
        System.out.println("Results:");
        System.out.println();
        for (Individual individual : population) {
            System.out.println(individual.toString());
        }
    }
}
