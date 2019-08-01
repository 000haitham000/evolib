/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import distancemetrics.DistanceMetric;
import distancemetrics.KKTPM2DistanceMetric;
import distancemetrics.PerpendicularDistanceMetric;
import emo.Individual;
import emo.OptimizationProblem;
import engines.AbstractGeneticEngine;
import engines.NSGA3Engine;
import evaluators.OSYEvaluator;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import evaluators.ZDT1Evaluator;
import parsing.IndividualEvaluator;
import parsing.StaXParser;
import parsing.XMLParser;

/**
 * This is a sample class showing the simplest way to use EvoLib
 *
 * @author Haitham Seada
 */
public class SampleScript extends TestScript {

    public static void main(String[] args)
            throws Throwable {
        // -------------------------------------------------------------------------------------------------------------
        String parametersFilePath = "samples/zdt1-02-30.xml";
        String problemDefinitionFilePath = "fullproblems/zdt1.xml";
        // Create Evaluator
        IndividualEvaluator individualEvaluator = new ZDT1Evaluator();
        // Create the distance metric
        URL url2 = SampleScript.class.getClassLoader().getResource(problemDefinitionFilePath);
        //DistanceMetric distanceMetric = new KKTPM2DistanceMetric(XMLParser.readXML(new File(url2.getFile())));
        DistanceMetric distanceMetric = new PerpendicularDistanceMetric();
        // -------------------------------------------------------------------------------------------------------------
        // Read problem
        URL url = SampleScript.class.getClassLoader().getResource(parametersFilePath);
        InputStream in = url.openStream();
        OptimizationProblem optimizationProblem = StaXParser.readProblem(in);
        // Create Engine
        AbstractGeneticEngine geneticEngine = new NSGA3Engine(
                optimizationProblem,
                individualEvaluator,
                distanceMetric);
        // Specify output directory
        File outDir = new File(String.format("%s%s%s%s%s",
                System.getProperty("user.home"),
                File.separator,
                "evolib",
                File.separator ,
                optimizationProblem.getProblemID()));
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
