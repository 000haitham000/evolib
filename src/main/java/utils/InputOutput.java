/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import emo.Individual;
import emo.InvalidRankValue;
import emo.OptimizationProblem;
import emo.OptimizationUtilities;
import emo.RealVariableSpecs;
import emo.VirtualIndividual;
import refdirs.ReferenceDirection;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import parsing.IndividualEvaluator;
import parsing.InvalidOptimizationProblemException;
import parsing.StaXParser;

/**
 * Provides input/output functionality used throughout most of the other
 * packages.
 *
 * @author Haitham Seada
 */
public class InputOutput {

    public static OptimizationProblem getProblem(String problemFilePath) throws InvalidOptimizationProblemException, XMLStreamException {
        InputStream in;
        OptimizationProblem optimizationProblem = null;
        try {
            // Read the problem from the input XML file
            URL url = InputOutput.class.getResource(problemFilePath);
            in = url.openStream();
            optimizationProblem = StaXParser.readProblem(in);
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return optimizationProblem;
    }

    public static void dumpPopulation(String comment, OptimizationProblem optimizationProblem, Individual[] parentPopulation, String filename) throws FileNotFoundException {
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(filename);
            printer.println("# " + comment);
            printer.println("# Number of Objectives = " + optimizationProblem.objectives.length);
            printer.println("# Generations Count = " + optimizationProblem.getGenerationsCount());
            printer.println("# Population Size = " + optimizationProblem.getPopulationSize());
            printer.println("# Each columns represent one objective value (at all points)");
            printer.println("# Each row represents the values all objectives at one point");
            printer.println("# -----------------------------------------------------------");
            for (Individual individual : parentPopulation) {
                for (int i = 0; i < optimizationProblem.objectives.length; i++) {
                    printer.format("%-15.5f ", individual.getObjective(i));
                }
                printer.println();
            }
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static void generatePlot(
            OptimizationProblem optimizationProblem,
            double minX, double maxX,
            double minY, double maxY,
            String dataFileName,
            String gnuPlotFileName) throws FileNotFoundException {
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(gnuPlotFileName);
            printer.println("set samples 10, 10");
            printer.println("set isosamples 50, 50");
            printer.println("set ticslevel 0");
            int refDirsCount = Mathematics.nchoosek(
                    optimizationProblem.objectives.length + optimizationProblem.getSteps() - 1,
                    optimizationProblem.getSteps());
            printer.format("set title \"Generations(%3d)-Pop(%3d)-Ref(%3d)\"%n",
                    optimizationProblem.getGenerationsCount(),
                    optimizationProblem.getPopulationSize(),
                    refDirsCount);
            printer.println("set xlabel \"F1\"");
            printer.println("set xlabel  offset character -3, -2, 0 font \"\" textcolor lt -1 norotate");
            printer.format("set xrange [ %5.2f : %5.2f ] noreverse nowriteback%n", minX, maxX);
            printer.println("set ylabel \"F2\"");
            printer.println("set ylabel  offset character 3, -2, 0 font \"\" textcolor lt -1 rotate by -270");
            printer.format("set yrange [ %5.2f : %5.2f ] noreverse nowriteback%n", minY, maxY);
            String onlyDataFileName = dataFileName.substring(dataFileName.lastIndexOf("/"));
            printer.format("plot '../../../NSGA/results/%s' t \"Efficient Set\" with points ls 7 linecolor rgb 'red'%n", onlyDataFileName);
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static void dumpPerformanceMetrics(
            OptimizationProblem optimizationProblem,
            double[] hyperVolume,
            double[] gd,
            double[] igd,
            String metricsFileName) throws FileNotFoundException {
        int rowsCount;
        if (hyperVolume != null) {
            rowsCount = hyperVolume.length;
        } else if (gd != null) {
            rowsCount = gd.length;
        } else if (igd != null) {
            rowsCount = igd.length;
        } else {
            throw new IllegalArgumentException("Dumping failed: HV, GD & IGD are all NULLs");
        }
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(metricsFileName);
            printer.println("-------------------- Details --------------------");
            printer.println();
            // Header
            printer.format("%-10s", getCenteredString("Run", 10));
            if (hyperVolume != null) {
                printer.format("%-20s", getCenteredString("Hypervolume", 20));
            }
            if (gd != null) {
                printer.format("%-20s", getCenteredString("GD", 20));
            }
            if (igd != null) {
                printer.format("%-20s", getCenteredString("IGD", 20));
            }
            printer.println();
            printer.println();
            // Detailed Metrics
            for (int i = 0; i < rowsCount; i++) {
                printer.format("%-10s", getCenteredString(String.format("%03d", i), 10));
                if (hyperVolume != null) {
                    printer.format("%-20s", getCenteredString(String.format("%-10.7f", hyperVolume[i]), 20));
                }
                if (gd != null) {
                    printer.format("%-20s", getCenteredString(String.format("%-10.7f", gd[i]), 20));
                }
                if (igd != null) {
                    printer.format("%-20s", getCenteredString(String.format("%-10.7f", igd[i]), 20));
                }
                printer.println();
            }
            // Metrics
            int tempIndex;
            // Average Metrics
            printer.println();
            printer.println("--------------- Best --------------");
            printer.println();
            if (hyperVolume != null) {
                printer.format("%-4s= Run(%03d) %-10.7f%n", "HV", (tempIndex = Mathematics.getMaxIndex(hyperVolume)), hyperVolume[tempIndex]);
            }
            if (gd != null) {
                printer.format("%-4s= Run(%03d) %-10.7f%n", "GD", (tempIndex = Mathematics.getMinIndex(gd)), gd[tempIndex]);
            }
            if (igd != null) {
                printer.format("%-4s= Run(%03d) %-10.7f%n", "IGD", (tempIndex = Mathematics.getMinIndex(igd)), igd[tempIndex]);
            }
            printer.println();
            printer.println("--------------- Worst --------------");
            printer.println();
            if (hyperVolume != null) {
                printer.format("%-4s= Run(%03d) %-10.7f%n", "HV", (tempIndex = Mathematics.getMinIndex(hyperVolume)), hyperVolume[tempIndex]);
            }
            if (gd != null) {
                printer.format("%-4s= Run(%03d) %-10.7f%n", "GD", (tempIndex = Mathematics.getMaxIndex(gd)), gd[tempIndex]);
            }
            if (igd != null) {
                printer.format("%-4s= Run(%03d) %-10.7f%n", "IGD", (tempIndex = Mathematics.getMaxIndex(igd)), igd[tempIndex]);
            }
            printer.println();
            printer.println("-------------------- Means -------------------");
            printer.println();
            if (hyperVolume != null) {
                printer.format("%-4s= %-10.7f%n", "HV", Mathematics.getNonNegativesAverage(hyperVolume));
            }
            if (gd != null) {
                printer.format("%-4s= %-10.7f%n", "GD", Mathematics.getNonNegativesAverage(gd));
            }
            if (igd != null) {
                printer.format("%-4s= %-10.7f%n", "IGD", Mathematics.getNonNegativesAverage(igd));
            }
            printer.println();
            printer.println("--------- Standard Deviations ---------");
            printer.println();
            if (hyperVolume != null) {
                printer.format("%-4s= %-10.7f%n", "HV", Mathematics.getStandardDeviation(hyperVolume));
            }
            if (gd != null) {
                printer.format("%-4s= %-10.7f%n", "GD", Mathematics.getStandardDeviation(gd));
            }
            if (igd != null) {
                printer.format("%-4s= %-10.7f%n", "IGD", Mathematics.getStandardDeviation(igd));
            }
            printer.println();
            printer.println("------------------ Medians ------------------");
            printer.println();
            if (hyperVolume != null) {
                int index = Mathematics.getNonNegativesMedianIndex(hyperVolume);
                printer.format("%-4s= Run(%03d) %-10.7f%n", "HV", index, hyperVolume[index]);
            }
            if (gd != null) {
                int index = Mathematics.getNonNegativesMedianIndex(gd);
                printer.format("%-4s= Run(%03d) %-10.7f%n", "GD", index, gd[index]);
            }
            if (igd != null) {
                int index = Mathematics.getNonNegativesMedianIndex(igd);
                printer.format("%-4s= Run(%03d) %-10.7f%n", "IGD", index, igd[index]);
            }
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static void dumpFunctionEvaluations(
            OptimizationProblem optimizationProblem,
            int[] individualEvaluations,
            double[] hvValues,
            String funcEvalFileName) throws FileNotFoundException {
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(funcEvalFileName);
            printer.println("-------------------- Details --------------------");
            printer.println();
            // Header
            printer.format("%-10s", getCenteredString("Run", 10));
            if (funcEvalFileName != null) {
                printer.format("%-20s", getCenteredString("Func.Eval.", 20));
            }
            if (hvValues != null) {
                printer.format("%-20s", getCenteredString("Hypervolume", 20));
            }
            printer.println();
            printer.println();
            // Detailed Metrics
            for (int i = 0; i < individualEvaluations.length; i++) {
                printer.format("%-10s", getCenteredString(String.format("%03d", i), 10));
                if (individualEvaluations != null) {
                    printer.format("%-20s", getCenteredString(String.format("%-15d", individualEvaluations[i]), 20));
                }
                if (hvValues != null) {
                    printer.format("%-20s", getCenteredString(String.format("%-15.7f", hvValues[i]), 20));
                }
                printer.println();
            }
            // Metrics
            int tempIndex;
            // Average Metrics
            printer.println();
            printer.println("--------------- Best & Worst --------------");
            printer.println();
            if (individualEvaluations != null) {
                printer.format("Min. Evaluations = Run(%03d) (%-15d)%n", (tempIndex = Mathematics.getMinIndex(individualEvaluations)), individualEvaluations[tempIndex]);
                printer.format("Max. Evaluations = Run(%03d) (%-15d)%n", (tempIndex = Mathematics.getMaxIndex(individualEvaluations)), individualEvaluations[tempIndex]);
            }
            printer.println();
            printer.println("-------------------- Means -------------------");
            printer.println();
            if (individualEvaluations != null) {
                printer.format("Evaluations = %-10.7f%n", Mathematics.getNonNegativesAverage(individualEvaluations));
            }
            printer.println();
            printer.println("--------- Standard Deviations ---------");
            printer.println();
            if (individualEvaluations != null) {
                printer.format("Evaluations = %-10.7f%n", Mathematics.getStandardDeviation(individualEvaluations));
            }
            printer.println();
            printer.println("------------------ Medians ------------------");
            printer.println();
            if (individualEvaluations != null) {
                printer.format("Evaluations = Run(%03d)%n", Mathematics.getNonNegativesMedianIndex(individualEvaluations));
            }
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static String getCenteredString(String text, int length) {
        if (text.length() >= length) {
            return text;
        }
        int remaining = length - text.length();
        StringBuilder sb = new StringBuilder(text);
        for (int i = 0; i < remaining; i++) {
            if (i % 2 == 0) {
                sb.append(' ');
            } else {
                sb.insert(0, ' ');
            }
        }
        int size = sb.toString().length();
        return sb.toString();
    }

    /**
     *
     * @param optimizationProblem
     * @param populationLabel
     * @param individuals
     */
    public static void displayPopulation(OptimizationProblem optimizationProblem, String populationLabel, Individual[] individuals) {
        System.out.format("--------------%n");
        System.out.format("Population(%s)%n", populationLabel);
        System.out.format("--------------%n");
        int count = 0;
        for (Individual individual : individuals) {
            System.out.format("%2d:", count);
            // Binary Decision Variables
            if (individual.binary.length != 0) {
                System.out.format("BIN[");
                for (int binVarCounter = 0; binVarCounter < individual.binary.length; binVarCounter++) {
                    System.out.format("%10.2f", individual.binary[binVarCounter]);
                    if (binVarCounter != individual.binary.length - 1) {
                        System.out.format(",");
                    }
                }
                System.out.format("]");
            }
            // Real Decision Variables
            if (individual.real.length != 0) {
                System.out.format("%6s[", "REAL");
                for (int realVarCounter = 0; realVarCounter < individual.real.length; realVarCounter++) {
                    System.out.format("%10.2f", individual.real[realVarCounter]);
                    if (realVarCounter != individual.real.length - 1) {
                        System.out.format(",");
                    }
                }
                System.out.format("]");
            }
            // Custom decisioin variables
            if (individual.custom.length != 0) {
                System.out.format("CUS[");
                for (int cusVarCounter = 0; cusVarCounter < individual.custom.length; cusVarCounter++) {
                    System.out.format("%s", individual.custom[cusVarCounter].toString());
                    if (cusVarCounter != individual.custom.length - 1) {
                        System.out.format(",");
                    }
                }
                System.out.format("]");
            }
            // Objectives
            System.out.format("%5s[", "OBJ");
            for (int objCounter = 0; objCounter < optimizationProblem.objectives.length; objCounter++) {
                System.out.format("%20.10f", individual.getObjective(objCounter));
                if (objCounter != optimizationProblem.objectives.length - 1) {
                    System.out.format(",");
                }
            }
            System.out.format("] (%-20.15f)%n", individual.getTotalConstraintViolation());
            count++;
        }
    }

    public static void displayPopulationDesignSpace(OptimizationProblem optimizationProblem, String populationLabel, Individual[] individuals) {
        System.out.format("--------------%n");
        System.out.format("Population(%s)%n", populationLabel);
        System.out.format("--------------%n");
        int count = 0;
        for (Individual individual : individuals) {
            System.out.format("%2d:", count);
            // Binary Decision Variables
            if (individual.binary.length != 0) {
                System.out.format("BIN[");
                for (int binVarCounter = 0; binVarCounter < individual.binary.length; binVarCounter++) {
                    System.out.format("%10.2f", individual.binary[binVarCounter]);
                    if (binVarCounter != individual.binary.length - 1) {
                        System.out.format(",");
                    }
                }
                System.out.format("]");
            }
            // Real Decision Variables
            if (individual.real.length != 0) {
                System.out.format("%6s[", "REAL");
                for (int realVarCounter = 0; realVarCounter < individual.real.length; realVarCounter++) {
                    System.out.format("%10.2f", individual.real[realVarCounter]);
                    if (realVarCounter != individual.real.length - 1) {
                        System.out.format(",");
                    }
                }
                System.out.format("]%n");
            }
            // Custom Decision Variables
            if (individual.custom.length != 0) {
                System.out.format("CUS[");
                for (int cusVarCounter = 0; cusVarCounter < individual.custom.length; cusVarCounter++) {
                    System.out.format("%s", individual.custom[cusVarCounter].toString());
                    if (cusVarCounter != individual.custom.length - 1) {
                        System.out.format(",");
                    }
                }
                System.out.format("]");
            }
        }
    }

    public static void displayPopulationUndecoratedObjectiveSpace(OptimizationProblem optimizationProblem, String populationLabel, Individual[] individuals) {
        System.out.format("--------------%n");
        System.out.format("%s%n", populationLabel);
        System.out.format("--------------%n");
        int count = 0;
        for (Individual individual : individuals) {
            // Objectives
            for (int objCounter = 0; objCounter < optimizationProblem.objectives.length; objCounter++) {
                System.out.format("%-20.10f", individual.getObjective(objCounter));
                if (objCounter != optimizationProblem.objectives.length - 1) {
                    System.out.format(" ");
                }
            }
            System.out.println();
            count++;
        }
    }

    public static void displayPopulationDecoratedObjectiveSpace(OptimizationProblem optimizationProblem, String populationLabel, Individual[] individuals) {
        System.out.format("--------------%n");
        System.out.format("%s%n", populationLabel);
        System.out.format("--------------%n");
        int count = 0;
        for (Individual individual : individuals) {
            System.out.format("%2d:", count);
            // Objectives
            System.out.format("%5s[", "OBJ");
            for (int objCounter = 0; objCounter < optimizationProblem.objectives.length; objCounter++) {
                System.out.format("%20.10f", individual.getObjective(objCounter));
                if (objCounter != optimizationProblem.objectives.length - 1) {
                    System.out.format(",");
                }
            }
            System.out.format("] (%-7.2f)%n", individual.getTotalConstraintViolation());
            count++;
        }
    }

    public static void displayPopulationObjectiveSpace(
            OptimizationProblem optimizationProblem,
            String label,
            Individual[] individuals) {
        System.out.format("--------------%n");
        System.out.format("%s%n", label);
        System.out.format("--------------%n");
        int count = 0;
        for (Individual individual : individuals) {
            System.out.format("%2d: ", count);
            for (int objCounter = 0; objCounter < optimizationProblem.objectives.length; objCounter++) {
                System.out.format("%10.2f", individual.getObjective(objCounter));
                if (objCounter != optimizationProblem.objectives.length - 1) {
                    System.out.format(",");
                }
            }
            System.out.format(" (%-7.2f)%n", individual.getTotalConstraintViolation());
            count++;
        }
    }

    public static void displayExtremePoints(
            OptimizationProblem optimizationProblem,
            String populationLabel,
            VirtualIndividual[] extremePoints) {
        System.out.println("-----------------------------------");
        System.out.format("%s%n", populationLabel);
        System.out.println("-----------------------------------");
        for (VirtualIndividual individual : extremePoints) {
            System.out.print("(");
            for (int i = 0; i < optimizationProblem.objectives.length; i++) {
                System.out.format("%10.2f", individual.getObjective(i));
                if (i != optimizationProblem.objectives.length - 1) {
                    System.out.print(",");
                }
            }
            System.out.format(")%n");
        }
    }

    public static void displayIntercepts(String label, double[] intercepts) {
        System.out.format("--------------%n");
        System.out.format("%s%n", label);
        System.out.format("--------------%n");
        System.out.print("(");
        for (int i = 0; i < intercepts.length; i++) {
            System.out.format("%6.2f", intercepts[i]);
            if (i != intercepts.length - 1) {
                System.out.print(",");
            }
        }
        System.out.println(")");
    }

    public static void displayRanks(String populationLabel, Individual[] individuals) {
        System.out.format("------------------------%n");
        System.out.format("Ranking (Population(%s))%n", populationLabel);
        System.out.format("------------------------%n");
        for (int i = 0; i < individuals.length; i++) {
            System.out.format("Individual(%2d): %d%n", i, individuals[i].getRank());
        }
    }

    public static void displayIdealPoint(String populationLabel, double[] idealPoint) {
        System.out.format("----------------------------%n");
        System.out.format("Ideal Point (Population(%s))%n", populationLabel);
        System.out.format("----------------------------%n");
        System.out.print("(");
        for (int i = 0; i < idealPoint.length; i++) {
            System.out.format("%10.7f", idealPoint[i]);
            if (i != idealPoint.length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println(")");
    }

    public static void displayAssociationResluts(
            int objCount,
            String populationLabel,
            Individual[] individuals) {
        System.out.println("------------------------------------------------");
        System.out.format("%s association results%n", populationLabel);
        System.out.println("------------------------------------------------");
        for (int i = 0; i < individuals.length; i++) {
            double[] direction = individuals[i].getReferenceDirection().direction;
            System.out.format("Individual[%s] (rank=%d) is associated to direction (", individuals[i].getShortVariableSpace(), individuals[i].getRank());
            for (int objCounter = 0; objCounter < objCount; objCounter++) {
                System.out.format("%5.2f", direction[objCounter]);
                if (objCounter != objCount - 1) {
                    System.out.format(",");
                }
            }
            System.out.format(")%n");
        }
    }

    public static void displayReferenceDirections(String populationLabel, List<ReferenceDirection> referenceDirections) {
        System.out.println("------------------------------------------------");
        System.out.format("%s reference directions%n", populationLabel);
        System.out.println("------------------------------------------------");
        for (int i = 0; i < referenceDirections.size(); i++) {
            double[] direction = referenceDirections.get(i).direction;
            //System.out.format("Direction(%d) = (", i);
            for (int j = 0; j < direction.length; j++) {
                System.out.format("%7.5f", direction[j]);
                if (j != direction.length - 1) {
                    //System.out.format(",");
                    System.out.format(" ");
                }
            }
            //System.out.format(")%n");
            System.out.format("%n");
        }
    }

    public static void displayDistanceMatrix(
            int objCount,
            List<ReferenceDirection> referenceDirections,
            Individual[] individuals,
            double[][] distanceMatrix) {
        System.out.println("----------------");
        System.out.println("Distances Matrix");
        System.out.println("----------------");
        for (int i = 0; i < referenceDirections.size(); i++) {
            System.out.print("-Ref.Point(");
            for (int j = 0; j < objCount; j++) {
                System.out.format("%6.2f", referenceDirections.get(i).direction[j]);
                if (j != objCount - 1) {
                    System.out.print(",");
                }
            }
            System.out.format("):%n");
            for (int j = 0; j < individuals.length; j++) {
                System.out.format("\tCandidate(%d) = %6.2f%n", j, distanceMatrix[j][i]);
            }
        }
    }

    public static void displayGenerationCount(int generationIndex) {
        System.out.println("-----------------");
        System.out.format(" Generation(%d)%n", generationIndex);
        System.out.println("-----------------");
    }

    public static void dumpCurrentGeneration_proposed(
            OptimizationProblem optimizationProblem,
            String filePath,
            List<ReferenceDirection> referenceDirections,
            List<List<Individual>> fronts,
            IndividualEvaluator individualEvaluator) throws IOException {
        // Restore original objective values
        for (List<Individual> front : fronts) {
            for (Individual individual : front) {
                individualEvaluator.updateIndividualObjectivesAndConstraints(optimizationProblem, individual);
            }
        }
        // Dump generation data
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(filePath);
            // Dump reference directions
            for (ReferenceDirection direction : referenceDirections) {
                printer.print("d:");
                for (int i = 0; i < direction.direction.length; i++) {
                    printer.format("%07.4f", direction.direction[i]);
                    if (i != direction.direction.length - 1) {
                        printer.print(" ");
                    }
                }
                printer.println();
            }
            // Dump fronts
            for (int i = 0; i < fronts.size(); i++) {
                printer.format("*Front-%03d%n", i + 1);
                for (Individual individual : fronts.get(i)) {
                    for (int j = 0; j < optimizationProblem.objectives.length; j++) {
                        printer.format("%07.4f", individual.getObjective(j));
                        if (j != optimizationProblem.objectives.length - 1) {
                            printer.print(" ");
                        }
                    }
                    if (Mathematics.compare(individual.proposedCrowdingMeasure, 0) != 0) {
                        printer.print(String.format(" (%07.4f)", individual.proposedCrowdingMeasure));
                    }
                    printer.println();
                }
            }
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static void dumpCurrentGeneration_NSGA2(
            OptimizationProblem optimizationProblem,
            String filePath,
            List<List<Individual>> fronts,
            IndividualEvaluator individualEvaluator) throws IOException {
        // Dump generation data
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(filePath);
            // Dump fronts
            for (int i = 0; i < fronts.size(); i++) {
                printer.format("*Front-%03d%n", i + 1);
                for (Individual individual : fronts.get(i)) {
                    for (int j = 0; j < optimizationProblem.objectives.length; j++) {
                        printer.format("%07.4f", individual.getObjective(j));
                        if (j != optimizationProblem.objectives.length - 1) {
                            printer.print(" ");
                        }
                    }
                    if (Mathematics.compare(individual.getNsga2crowdingDistance(), 0) != 0) {
                        printer.print(String.format(" (%07.4f)", individual.getNsga2crowdingDistance()));
                    }
                    printer.println();
                }
            }
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static void displayFrontsDecisionSpace(List<List<Individual>> fronts) {
        System.out.println("------");
        System.out.println("FORNTS");
        System.out.println("------");
        for (int i = 0; i < fronts.size(); i++) {
            System.out.format("* Front(%d) *%n", i);
            for (int j = 0; j < fronts.get(i).size(); j++) {
                System.out.format("IND(%d):%s%n", i, fronts.get(i).get(j).getVariableSpace());
            }
        }
    }

    public static void displayFrontsObjectiveSpace(List<List<Individual>> fronts) {
        System.out.println("------");
        System.out.println("FORNTS");
        System.out.println("------");
        for (int i = 0; i < fronts.size(); i++) {
            System.out.format("* Front(%d) *%n", i);
            for (int j = 0; j < fronts.get(i).size(); j++) {
                System.out.format("IND(%d):%s%n", i, fronts.get(i).get(j).getObjectiveSpace());
            }
        }
    }

    public static void displayHVList(List<Double> hvList) {
        System.out.println("HV values for all generations");
        for (int i = 0; i < hvList.size(); i++) {
            System.out.format("Gen(%04d):%-7.4f%n", i + 1, hvList.get(i));
        }
    }

    public static void dumpSingleObjectivePopulation(
            String message,
            OptimizationProblem optimizationProblem,
            Individual[] individuals,
            String dataFileName) throws FileNotFoundException {
        Individual bestIndividual = null;
        //int rankOneCount = 0;
        for (Individual individual : individuals) {
            if (individual.getRank() == 1) {
                //if (bestIndividual == null) {
                bestIndividual = individual;
                break;
                //rankOneCount++;
                //} else if(!bestIndividual.equals(individual)) {
                //rankOneCount++;
                //}
            }
        }
        if (bestIndividual == null) {
            throw new UnsupportedOperationException("No individual ranked 1 !!!"
                    + " Something is wrong.");
            //} else if (rankOneCount > 1) {
            //    throw new UnsupportedOperationException("More than one individual"
            //            + " ranked 1 !!! This couldn't be a single objective"
            //            + " optimization problem.");
        } else {
            // Everything is fine
            PrintWriter printer = null;
            try {
                printer = new PrintWriter(dataFileName);
                printer.println(bestIndividual.toString());
            } finally {
                if (printer != null) {
                    printer.close();
                }
            }
        }
    }

    public static void dumpSingleObjectiveAverage(OptimizationProblem optimizationProblem, double[] objValues, String metricsFileName) throws FileNotFoundException {
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(metricsFileName);
            printer.format("Average Objective Value of All Runs: %f%n", Mathematics.getAverage(objValues));
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static void printDirectionMatlabCode(int objCount, List<ReferenceDirection> dirs, PrintWriter printer) {
        if(objCount == 2 || objCount == 3) {
            System.out.println("MATLAB CODE (for visualizing final reference directions)");
            StringBuilder[] dimBuilderArr = new StringBuilder[objCount];
            for (int i = 0; i < dimBuilderArr.length; i++) {
                dimBuilderArr[i] = new StringBuilder("[");
            }
            for (int i = 0; i < dirs.size(); i++) {
                for (int j = 0; j < dimBuilderArr.length; j++) {
                    dimBuilderArr[j].append(dirs.get(i).direction[j]);
                }
                if (i != dirs.size() - 1) {
                    for (StringBuilder dimBuilder : dimBuilderArr) {
                        dimBuilder.append(", ");
                    }
                }
            }
            for (StringBuilder dimBuilder : dimBuilderArr) {
                dimBuilder.append("]");
            }
            for (int i = 0; i < dimBuilderArr.length; i++) {
                printer.format("Dim%02d = %s;%n", i, dimBuilderArr[i].toString());
            }
            printer.println("xlim([0 1.1])");
            printer.println("ylim([0 1.1])");
            if (objCount == 3) {
                printer.println("zlim([0 1.1])");
            }
            if (objCount == 2) {
                printer.println("scatter(Dim00, Dim01);");
            } else if (objCount == 3) {
                printer.println("scatter3(Dim00, Dim01, Dim02);");
            }
            printer.flush();
        }
    }

    public static StringBuilder createMatlabScript3D(Individual[] individuals) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("clear all%n"));
        StringBuilder xDim = new StringBuilder("xDim = [");
        StringBuilder yDim = new StringBuilder("yDim = [");
        StringBuilder zDim = new StringBuilder("zDim = [");
        for (int i = 0; i < individuals.length; i++) {
            xDim.append(individuals[i].getObjective(0));
            yDim.append(individuals[i].getObjective(1));
            zDim.append(individuals[i].getObjective(2));
            if (i != individuals.length - 1) {
                xDim.append(", ");
                yDim.append(", ");
                zDim.append(", ");
            }
        }
        xDim.append("];");
        yDim.append("];");
        zDim.append("];");

        sb.append(String.format(xDim.toString() + "%n"));
        sb.append(String.format(yDim.toString() + "%n"));
        sb.append(String.format(zDim.toString() + "%n"));
        sb.append(String.format("plot3(xDim, yDim, zDim, 'ok', 'MarkerSize', 3, 'MarkerFaceColor','k');%n"));
        //sb.append(String.format("scatter3(xDim,yDim,zDim, 'filled', 'k');%n"));
        sb.append(String.format("view(125,25)%n"));
        sb.append(String.format("grid on"));
        return sb;
    }

    /**
     * Generate a MATLAB scatter plot of the population(s) passed as a
     * parameter. Each population takes a different color from the previous one
     * (see the implementation for specific colors). IMPORTANT NOTE: Individuals
     * of a later population will hide individuals of an earlier population if
     * plotted at the same position.
     *
     * @param sb
     * @param individualsArrays
     */
    public static void printMatlabCode2D(StringBuilder sb, VirtualIndividual[]... individualsArrays) {
        Color[] colors = {
            Color.BLACK, Color.RED, Color.BLUE, Color.GREEN,
            Color.YELLOW, Color.gray, Color.pink};
        int count = 0;
        sb.append(String.format("clear all%n"));
        sb.append(String.format("hold on%n"));
        for (VirtualIndividual[] individuals : individualsArrays) {
            if (individuals.length == 0) {
                throw new UnsupportedOperationException("Unable to generate "
                        + "Matlab script: Individuals array is empty");
            }
            StringBuilder xBuilder = new StringBuilder(String.format("X%02d = [", count));
            StringBuilder yBuilder = new StringBuilder(String.format("Y%02d = [", count));
            for (int i = 0; i < individuals.length; i++) {
                if (individuals[i].getObjectivesCount() != 2) {
                    throw new UnsupportedOperationException(
                            String.format("Unable to generate Matlab script: "
                                    + "Individual(%d) has (%d) objectives. "
                                    + "All individuals must have exactly 2 "
                                    + "objectives in order to be plotted on a "
                                    + "2D plot.",
                                    i, individuals[i].getObjectivesCount())
                    );
                }
                xBuilder.append(individuals[i].getObjective(0)).append(" ");
                yBuilder.append(individuals[i].getObjective(1)).append(" ");
            }
            xBuilder.append("];");
            yBuilder.append("];");
            sb.append(String.format(xBuilder.toString() + "%n"));
            sb.append(String.format(yBuilder.toString() + "%n"));
            String plot = String.format("scatter(X%02d, Y%02d, 30, [%3.2f %3.2f %3.2f], 'LineWidth', 2);",
                    count,
                    count,
                    colors[count % colors.length].getRed() / 255.0,
                    colors[count % colors.length].getGreen() / 255.0,
                    colors[count % colors.length].getBlue() / 255.0);
            sb.append(String.format(plot + "%n"));
            String clearXCommand = String.format("clear X%02d", count);
            String clearYCommand = String.format("clear Y%02d", count);
            sb.append(String.format(clearXCommand + "%n"));
            sb.append(String.format(clearYCommand + "%n"));
            count++;
        }
        boolean connectCorrespondingPoints = true;
        if (connectCorrespondingPoints) {
            for (int i = 0; i < individualsArrays.length - 1; i++) {
                sb.append(String.format("for i = 1:size(X%02d,2)%n", i));
                sb.append(String.format("    plot([X%02d(i),X%02d(i)],[Y%02d(i),Y%02d(i)])%n", i, i + 1, i, i + 1));
                sb.append(String.format("end%n"));
            }
        }
        sb.append(String.format("hold off%n"));
    }

    public static StringBuilder createMatlabScript2D(
            VirtualIndividual[]... individualsArray) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        printMatlabCode2D(sb, individualsArray);
        return sb;
    }

    public static VirtualIndividual[] loadIndividualsFromFile(File file) throws IOException {
        BufferedReader reader = null;
        try {
            List<VirtualIndividual> vIndividualsList = new ArrayList<VirtualIndividual>();
            int objCount = -1;
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                line = GeneralUtilities.replaceBlanksWithSingleSpace(line);
                String[] splits = line.trim().split(" ");
                if (objCount == -1) {
                    objCount = splits.length;
                } else if (objCount != splits.length) {
                    throw new UnsupportedOperationException("All individuals must have the same number of objectives");
                }
                VirtualIndividual vIndividual = new VirtualIndividual(objCount);
                for (int i = 0; i < objCount; i++) {
                    vIndividual.setObjective(i, Double.parseDouble(splits[i]));
                }
                vIndividualsList.add(vIndividual);
            }
            VirtualIndividual[] vIndividuals = new VirtualIndividual[vIndividualsList.size()];
            vIndividualsList.toArray(vIndividuals);
            return vIndividuals;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public static StringBuilder collectObjectiveSpace(
            OptimizationProblem optimizationProblem,
            Individual[] individuals)
            throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < individuals.length; i++) {
            for (int j = 0; j < optimizationProblem.objectives.length; j++) {
                // Write objective value (or NaN if the individual is infeasible)
                if (individuals[i].isFeasible()) {
                    sb.append(String.format("%7.7f", individuals[i].getObjective(j)));
                } else {
                    sb.append(String.format("%s", "NaN"));
                }
                // Write a space if this is not the last objective value
                if (j != optimizationProblem.objectives.length - 1) {
                    sb.append(" ");
                }
            }
            // Write a line separator (Enter)
            sb.append(String.format("%n"));
        }
        return sb;
    }

    /* DO NOT USE IF THE PROBLEM HAS BINARY VARIABLES */
    public static StringBuilder collectRealDecisionSpace(
            OptimizationProblem optimizationProblem,
            Individual[] individuals)
            throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        for (Individual individual : individuals) {
            int realCounter = 0;
            for (int j = 0; j < optimizationProblem.getVariablesSpecs().length; j++) {
                if (optimizationProblem.getVariablesSpecs()[j] instanceof RealVariableSpecs) {
                    // Write decision variable value
                    sb.append(String.format("%15.14f", individual.real[realCounter++]));
                }
                // Write a space if this is not the last variable
                if (j != optimizationProblem.getVariablesSpecs().length - 1) {
                    sb.append(" ");
                }
            }
            // Write a line separator (Enter)
            sb.append(String.format("%n"));
        }
        return sb;
    }

    public static void dumpSingleObjGenerationWiseAcrossRunsResults(double[][][] data, String filePath) throws FileNotFoundException {
        int runCount = data.length;
        int genCount = data[0].length;
        int popSize = data[0][0].length;
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(filePath);
            //printer.format("%s %s %s %s%n", "Gen#", "Best", "Median", "Worst");
            for (int g = 0; g < genCount; g++) {
                double[] resultsAcrossRuns = new double[runCount * popSize];
                for (int r = 0; r < data.length; r++) {
                    System.arraycopy(data[r][g], 0, resultsAcrossRuns, r * popSize, popSize);
                }
                int minIndex = Mathematics.getMinIndex(resultsAcrossRuns);
                double minValue;
                if (minIndex == -1) {
                    minValue = Double.NaN;
                } else {
                    minValue = resultsAcrossRuns[minIndex];
                }
                int medIndex = Mathematics.getMedianIndex(resultsAcrossRuns);
                double medianValue;
                if (medIndex == -1) {
                    medianValue = Double.NaN;
                } else {
                    medianValue = resultsAcrossRuns[medIndex];
                }
                int maxIndex = Mathematics.getMaxIndex(resultsAcrossRuns);
                double maxValue;
                if (maxIndex == -1) {
                    maxValue = Double.NaN;
                } else {
                    maxValue = resultsAcrossRuns[maxIndex];
                }
                printer.format("%03d %9.8f %9.8f %9.8f%n", g, minValue, medianValue, maxValue);
            }
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static void dumpSetCovergaeStatistics(List<double[]> setCoverageValuesList, int runIndex, String outputDir) throws FileNotFoundException {
        // Dump to the following file
        File outputFile = null;
        if (runIndex == -1) {
            outputFile = new File(
                    outputDir + String.format("set_coverage.dat"));
        } else {
            outputFile = new File(
                    outputDir + String.format("set_coverage_run%03d.dat", runIndex));
        }
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(outputFile);
            for (int i = 0; i < setCoverageValuesList.size(); i++) {
                printer.format("%05d %5.5f %5.5f%n",
                        i,
                        setCoverageValuesList.get(i)[0],
                        setCoverageValuesList.get(i)[1]);
            }
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static void dumpSetCovergaeStatistics(double[][] setCoverageValuesArr, int runIndex, String outputDir) throws FileNotFoundException {
        // Dump to the following file
        File outputFile = null;
        if (runIndex == -1) {
            outputFile = new File(
                    outputDir + String.format("set_coverage.dat"));
        } else {
            outputFile = new File(
                    outputDir + String.format("set_coverage_run%03d.dat", runIndex));
        }
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(outputFile);
            for (int i = 0; i < setCoverageValuesArr.length; i++) {
                printer.format("%05d %5.5f %5.5f%n",
                        i,
                        setCoverageValuesArr[i][0],
                        setCoverageValuesArr[i][1]);
            }
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static void dumpQSurvivingStatistics(List<Double> qSurvivalPercent, int runIndex, String outputDir) throws FileNotFoundException {
        // Dump to the following file
        File outputFile = null;
        if (runIndex == -1) {
            outputFile = new File(
                    outputDir + String.format("q_survival_percent.dat"));
        } else {
            outputFile = new File(
                    outputDir + String.format("q_survival_percent_run%03d.dat", runIndex));
        }
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(outputFile);
            for (int i = 0; i < qSurvivalPercent.size(); i++) {
                printer.format("%05d %5.5f%n",
                        i,
                        qSurvivalPercent.get(i));
            }
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static void dumpQSurvivingStatistics(double[] qSurvivalPercentArr, int runIndex, String outputDir) throws FileNotFoundException {
        // Dump to the following file
        File outputFile = null;
        if (runIndex == -1) {
            outputFile = new File(
                    outputDir + String.format("q_survival_percent.dat"));
        } else {
            outputFile = new File(
                    outputDir + String.format("q_survival_percent_run%03d.dat", runIndex));
        }
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(outputFile);
            for (int i = 0; i < qSurvivalPercentArr.length; i++) {
                printer.format("%05d %5.5f%n",
                        i,
                        qSurvivalPercentArr[i]);
            }
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static void dumpAnimatedMatlabScriptFor2dPoints(
            String animatedMatlabScriptFilePath,
            double[] idealPoint,
            double[] intercepts,
            List<ReferenceDirection> refDirsList) throws FileNotFoundException {
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(animatedMatlabScriptFilePath);
            String intializationPart
                    = "% Create a new figure\n"
                    + "fig_handle = figure\n"
                    + "% Use OpenGL renderer (faster than Painter's and Z-buffer for rendering\n"
                    + "% complex scenes. It is capable of showing both lighting and transparency).\n"
                    + "set(gcf,'Renderer','OpenGL');\n"
                    + "% Save the current intercepts before they get changed (remember that this \n"
                    + "% animation is normalized)\n"
                    + "current_x_limit = xlim;\n"
                    + "current_y_limit = ylim;\n";
            // Prepare refernce directions part
            String refDirsLines;
            StringBuilder refDirsX = new StringBuilder("ref_dirs_X = [");
            StringBuilder refDirsY = new StringBuilder("ref_dirs_Y = [");
            for (int i = 0; i < refDirsList.size(); i++) {
                refDirsX.append(refDirsList.get(i).direction[0]);
                refDirsY.append(refDirsList.get(i).direction[1]);
                if (i != refDirsList.size() - 1) {
                    refDirsX.append(" ");
                    refDirsY.append(" ");
                }
            }
            refDirsX.append("];");
            refDirsY.append("];");
            refDirsLines = String.format("%s%n%s%n%s%n", "% Reference Directions", refDirsX.toString(), refDirsY.toString());
            String refDirsPart
                    = "% Reference Directions\n"
                    + refDirsLines;
            String idealPointPart = String.format("%% Ideal Point\nideal = [%05.3f %05.3f];\n", idealPoint[0], idealPoint[1]);
            String intercetpsPart = String.format("%% Intercepts\nintercepts = [%05.3f %05.3f];\n", intercepts[0], intercepts[1]);
            String drawRefDirsPart
                    = "% Draw reference directions\n"
                    + "for i = 1 : size(ref_dirs_X,2)\n"
                    + "    if(ref_dirs_X(i) == 0)\n"
                    + "        x = 0;\n"
                    + "        y = current_y_limit(2);\n"
                    + "    else\n"
                    + "        x = current_x_limit(2);\n"
                    + "        y = ref_dirs_Y(i)/ref_dirs_X(i)*current_x_limit(2);\n"
                    + "    end\n"
                    + "    % Notice that this command also usually changes X and Y limits if X\n"
                    + "    % and Y values are not normalized. So, the original X and Y limits\n"
                    + "    % are re-set later.\n"
                    + "    plot([0.0 x],[0.0 y],'k-','LineWidth',1)\n"
                    + "end\n";
            String drawEmptyRedDirsPart
                    = "    % Empty reference directions\n"
                    + "    empty_dirs_data_file_name = strcat('gen_',num2str(file_index,'%04d'),'_empty_refdirs.dat');\n"
                    + "    if exist(empty_dirs_data_file_name, 'file') == 2\n"
                    + "        empty_dirs_data = dlmread(empty_dirs_data_file_name);\n"
                    + "        empty_dirs_X = empty_dirs_data(:,1)';\n"
                    + "        empty_dirs_Y = empty_dirs_data(:,2)';\n"
                    + "        % Re-Draw empty ref dirs\n"
                    + "        for i = 1 : size(empty_dirs_X,2)\n"
                    + "            if(empty_dirs_X(i) == 0)\n"
                    + "                x = 0;\n"
                    + "                y = current_y_limit(2);\n"
                    + "            else\n"
                    + "                x = current_x_limit(2);\n"
                    + "                y = empty_dirs_Y(i)/empty_dirs_X(i)*current_x_limit(2);\n"
                    + "            end\n"
                    + "            plot([0.0 x],[0.0 y],'LineWidth',2,'Color',[1-i/size(empty_dirs_X,2) 1 0]);\n"
                    + "        end\n"
                    + "    end\n";
            String animationLoop
                    = "% Loop over all the files until no more files exist or until the user stops\n"
                    + "% execution by closing the figure.\n"
                    + "file_index = 0;\n"
                    + "file_name = strcat('gen_',num2str(file_index,'%04d'),'_obj.dat');\n"
                    + "hold on\n"
                    + "while(exist(file_name,'file') == 2 && ishandle(fig_handle))\n"
                    + "    % Draw Generation Box\n"
                    + "    textPosX = 0.8;\n"
                    + "    textPosY = 0.9;\n"
                    + "    text(textPosX, textPosY, ...\n"
                    + "     strcat('generation: ',num2str(file_index,'%04d')), ...\n"
                    + "     'Color', 'black', ...\n"
                    + "     'BackgroundColor', 'white', ...\n"
                    + "     'EdgeColor', 'black', ...\n"
                    + "     'HorizontalAlignment', 'Center');\n"
                    + drawRefDirsPart
                    + drawEmptyRedDirsPart
                    + "    % Points (Individuals)\n"
                    + "    fid=fopen(file_name);\n"
                    + "    data = [];\n"
                    + "    while 1\n"
                    + "        tline = fgetl(fid);\n"
                    + "        if ~ischar(tline), break, end\n"
                    + "        celldata = textscan(tline,'%f %f');\n"
                    + "        matdata = cell2mat(celldata);\n"
                    + "        % match fails for text lines, textscan returns empty cells\n"
                    + "        data = [data; matdata];\n"
                    + "    end\n"
                    + "    fclose(fid);\n"
                    + "    X = data(:,1)';\n"
                    + "    Y = data(:,2)';\n"
                    + "    % Draw Points (This command usually changes X and Y limits. So, the\n"
                    + "    % original limits are restored later)\n"
                    + "    if(file_index == 0)\n"
                    + "        h = scatter((X-ideal(1))./intercepts(1),(Y-ideal(2))./intercepts(2),70,...\n"
                    + "            'MarkerEdgeColor',[.5 0 0],...\n"
                    + "            'MarkerFaceColor',[.9 .7 0],...\n"
                    + "            'LineWidth',1.5);\n"
                    + "        % Use normal erase mode. It is the slowest but the most accurate erase\n"
                    + "        % mode.\n"
                    + "        set(h,'EraseMode','normal');\n"
                    + "    else\n"
                    + "        set(h,'XData',(X-ideal(1))./intercepts(1),'YData',(Y-ideal(2))./intercepts(2));\n"
                    + "    end\n"
                    + "    % Move to the next file\n"
                    + "    file_index = file_index + 1;\n"
                    + "    file_name = strcat('gen_',num2str(file_index,'%04d'),'_obj.dat');\n"
                    + "    xlim([0 1])\n"
                    + "    ylim([0 1])\n"
                    + "    pause(0.1)\n"
                    + "end\n"
                    + "% Execute (hold off) only if the figure is still shown. Remeber that the\n"
                    + "% previous loop might have terminated bacause the user stopped execution by\n"
                    + "% closing the figure instead of waiting till the end of the loop. In this\n"
                    + "% case, calling (hold off) will create a new empty figure.\n"
                    + "if(ishandle(fig_handle))\n"
                    + "    hold off\n"
                    + "end";
            printer.print(intializationPart);
            printer.print(refDirsPart);
            printer.print(idealPointPart);
            printer.print(intercetpsPart);
            printer.print(animationLoop);
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static void dumpAnimatedMatlabScriptFor3dPoints(String animatedMatlabScriptFilePath, double[] idealPoint, double[] intercepts, List<ReferenceDirection> refDirsList) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void dumpRefDirs(String emptyRefDirsFilePath, List<ReferenceDirection> refDirsList) throws FileNotFoundException {
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(emptyRefDirsFilePath);
            for (ReferenceDirection referenceDirection : refDirsList) {
                for (int i = 0; i < referenceDirection.direction.length; i++) {
                    printer.format("%05.4f", referenceDirection.direction[i]);
                    if (i != referenceDirection.direction.length - 1) {
                        printer.print(" ");
                    }
                }
                printer.println();
            }
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static StringBuilder collectRefDirs(List<ReferenceDirection> refDirsList) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        for (ReferenceDirection referenceDirection : refDirsList) {
            for (int i = 0; i < referenceDirection.direction.length; i++) {
                sb.append(String.format("%05.4f", referenceDirection.direction[i]));
                if (i != referenceDirection.direction.length - 1) {
                    sb.append(" ");
                }
            }
            sb.append(String.format("%n"));
        }
        return sb;
    }

    public static void dumpMetaData(String metaDataFilePath, Individual[] individuals, double[] idealPoint, double[] intercepts, int funEvalCount, double utopianEpsilon) throws FileNotFoundException {
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(metaDataFilePath);
            printer.format("pop_count = %d%n", individuals.length);
            printer.format("feasible_count = %d%n", OptimizationUtilities.getFeasibleIndividuals(individuals).length);
            int unrankedIndividualsCount = 0; // See the comment inside the next catch{...}
            int frontsCount = 0;
            for (Individual individual : individuals) {
                try {
                    if (individual.getRank() > frontsCount) {
                        frontsCount = individual.getRank();
                    }
                } catch (InvalidRankValue ex) {
                    // This exception is expected here IF AND ONLY IF a local
                    // search has been performed at the end of the previous
                    // generation. In this case, the individual(s) resulting
                    // from this local search is(are) not ranked yet. We only
                    // need here to count them and print there count with the
                    // rest of the meat data.
                    unrankedIndividualsCount++;
                }
            }
            printer.format("fronts_count = %d%n", frontsCount);
            int[] individualsPerFront = new int[frontsCount];
            for (Individual individual : individuals) {
                try {
                    individualsPerFront[individual.getRank() - 1]++;
                } catch (InvalidRankValue ex) {
                    // Just catch. Read the comment in the previous catch.
                }
            }
            for (int i = 0; i < individualsPerFront.length; i++) {
                printer.format("front_%03d_ind_count = %d%n", i, individualsPerFront[i]);
            }
            printer.format("unranked_individuals = %d%n", unrankedIndividualsCount);
            printer.format("ideal = %s%n", idealPoint == null ? "N/A" : Arrays.toString(idealPoint));
            printer.format("intercepts = %s%n", intercepts == null ? "N/A" : Arrays.toString(intercepts));
            printer.format("utopian_epsilon = %f%n", utopianEpsilon);
            printer.format("fun_eval = %d%n", funEvalCount);
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static StringBuilder collectMetaData(
            Individual[] individuals,
            double[] idealPoint,
            double[] intercepts,
            int funEvalCount,
            double utopianEpsilon) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        // Append population count
        sb.append(String.format("pop_count = %d%n", individuals.length));
        // Append the number of feasible individuals only
        sb.append(String.format("feasible_count = %d%n", OptimizationUtilities.getFeasibleIndividuals(individuals).length));
        // Count the number of fronts and the number of unranked individuals
        int frontsCount = 0;
        int unrankedIndividualsCount = 0; // See the comment inside the next catch{...}
        for (Individual individual : individuals) {
            try {
                if (individual.getRank() > frontsCount) {
                    frontsCount = individual.getRank();
                }
            } catch (InvalidRankValue ex) {
                // This exception is expected here IF AND ONLY IF a local
                // search has been performed at the end of the previous
                // generation. In this case, the individual(s) resulting
                // from this local search is(are) not ranked yet. We only
                // need here to count them and print there count with the
                // rest of our meta data.
                unrankedIndividualsCount++;
            }
        }
        // Append the nuber of fronts
        sb.append(String.format("fronts_count = %d%n", frontsCount));
        // Count the number of individuals at each front
        int[] individualsPerFront = new int[frontsCount];
        for (Individual individual : individuals) {
            try {
                individualsPerFront[individual.getRank() - 1]++;
            } catch (InvalidRankValue ex) {
                // Just catch. Read the comment in the previous catch.
            }
        }
        // Append the number of individuals at each front
        for (int i = 0; i < individualsPerFront.length; i++) {
            sb.append(String.format("front_%03d_ind_count = %d%n", i, individualsPerFront[i]));
        }
        // Append the number of unranked indiviudals (Due to local search. Read the previous comments)
        sb.append(String.format("unranked_individuals = %d%n", unrankedIndividualsCount));
        // Append the curent ideal point
        sb.append(String.format("ideal = %s%n", idealPoint == null ? "N/A" : Arrays.toString(idealPoint)));
        // Append intercepts
        sb.append(String.format("intercepts = %s%n", intercepts == null ? "N/A" : Arrays.toString(intercepts)));
        // Append utopian epsilon
        sb.append(String.format("utopian_epsilon = %f%n", utopianEpsilon));
        // Append the number of function evaluations consumed so far
        sb.append(String.format("fun_eval = %d%n", funEvalCount));
        // Return the final StringBuilder
        return sb;
    }

    public static void dumpDoubleArr(double[] arr, String filePath) throws FileNotFoundException {
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(filePath);
            for (int i = 0; i < arr.length; i++) {
                printer.format("%8.7f%n", arr[i]);
            }
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }

    public static void printLocalSearchInfo(
            double[] x0,
            double[] weightVector,
            double[] idealPoint,
            double[] intercepts,
            double[] utopianDisplacementVector,
            double[] xOut,
            double[] fOut,
            int evalCount) {
        System.out.println("   X0: " + Arrays.toString(x0));
        System.out.println("    W: " + Arrays.toString(weightVector));
        System.out.println("Ideal: " + Arrays.toString(idealPoint));
        System.out.println("I'cpt: " + Arrays.toString(intercepts));
        System.out.println("Ut'pn: " + Arrays.toString(utopianDisplacementVector));
        System.out.println("-----------------");
        System.out.println("   Xs: " + Arrays.toString(xOut));
        System.out.println("f(Xs): " + Arrays.toString(fOut));
        System.out.println("#eval: " + evalCount);
    }

    static ReferenceDirection[] loadReferenceDirections(File file) throws IOException {
        List<ReferenceDirection> refDirsList = new ArrayList<ReferenceDirection>();
        BufferedReader dirsReader = null;
        try {
            dirsReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = dirsReader.readLine()) != null) {
                String[] splits = line.split(" ");
                double[] direction = new double[splits.length];
                for (int i = 0; i < splits.length; i++) {
                    direction[i] = Double.parseDouble(splits[i].trim());
                }
                refDirsList.add(new ReferenceDirection(direction));
            }
            ReferenceDirection[] refDirs = new ReferenceDirection[refDirsList.size()];
            refDirsList.toArray(refDirs);
            return refDirs;
        } finally {
            if (dirsReader != null) {
                dirsReader.close();
            }
        }
    }

    public static void dumpAll(File rootDir, HashMap<String, StringBuilder> dumpMap) throws IOException {
        for (Map.Entry<String, StringBuilder> entry : dumpMap.entrySet()) {
            String key = entry.getKey();
            if (!key.equals("gen_count")) {
                String prefix = key.split("\\.")[0];
                String suffix = key.split("\\.")[1];
                String fileContent = entry.getValue().toString();
                // Compose file full path
                File outFile = new File(rootDir.getPath() + File.separator + prefix + String.format("_gen_%04d." + suffix, Integer.parseInt(dumpMap.get("gen_count").toString())));
                // Dump info to file
                PrintWriter printer = null;
                try {
                    printer = new PrintWriter(outFile);
                    printer.print(fileContent);
                } finally {
                    if (printer != null) {
                        printer.close();
                    }
                }
            }
        }
    }

    public static void writeText2File(String text, File file) throws IOException {
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(file);
            printer.print(text);
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }
}
