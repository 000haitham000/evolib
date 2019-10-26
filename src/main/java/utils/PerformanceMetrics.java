/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import emo.Individual;
import emo.OptimizationProblem;
import emo.OptimizationUtilities;
import emo.VirtualIndividual;
import engines.AbstractGeneticEngine;

import static engines.AbstractGeneticEngine.DEBUG_ALL;
import static engines.AbstractGeneticEngine.DEBUG_INTERCEPTS;
import static engines.AbstractGeneticEngine.MIN_DOUBLE_VALUE;

import engines.NSGA3Engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

import parsing.IndividualEvaluator;
import parsing.InvalidOptimizationProblemException;
import refdirs.ReferenceDirection;

/**
 * Provides the functionality of calculating some performance metrics.
 * <p>
 * TODO: replace this implementation with an external dependency (library) that
 * provides a wider support for metric calculations.
 *
 * @author Haitham Seada
 */
public class PerformanceMetrics {

    /**
     * Note that the algorithm assumes that all the solutions are non-dominated.
     * No check is made to ensure this property i.e. if it is not true, the
     * method will return wrong result with NO exceptions or flags.
     *
     * @param geneticEngine
     * @param individuals
     * @param referencePoint
     * @return
     */
    public static double calculateHyperVolumeForTwoObjectivesOnly(
            AbstractGeneticEngine geneticEngine,
            Individual[] individuals,
            double[] referencePoint,
            double[] idealPoint,
            double epsilon) {
        // Create a copy of the reference ponit
        double[] referencePointCopy = Arrays.copyOf(
                referencePoint,
                referencePoint.length);
        // Remove points that are NOT dominating the reference point
        // (Note: the method individual.dominates() cannot be used here because
        // it takes into consideration feasibility of the two individuals,
        // which means that if the NADIR_POINT is an infeasible point, it
        // will be considered dominated by all our individuals. Although this
        // is the true concept of constrained-dominance, for the sake of
        // hypervolume calculations we only need to make sure that our
        // individuals are better that NADIR_POINT in terms of objectives.
        // The feasibility of the NADIR_POINT is irrelevant in this context.
        List<Individual> individualsList = new ArrayList<Individual>();
        outerLoop:
        for (Individual individual : individuals) {
            for (int i = 0;
                 i < geneticEngine.optimizationProblem.objectives.length;
                 i++) {
                if (Mathematics.compare(
                        individual.getObjective(i),
                        referencePoint[i])
                        != -1) {
                    continue outerLoop;
                }
            }
            individualsList.add(individual);
        }
        // If all individuals are not qualified just return -1
        if (individualsList.isEmpty()) {
            return -1;
        }
        // Copy list to array
        Individual[] individualsCopy = new Individual[individualsList.size()];
        individualsList.toArray(individualsCopy);
        individualsList.clear();
        // Make sure that all the points are non-dominated
        // (i.e. remove dominated points)
        geneticEngine.rankIndividuals(individualsCopy, epsilon, null);
        for (Individual individual : individualsCopy) {
            if (individual.getRank() == 1) {
                individualsList.add(individual);
            }
        }
        // Copy the now-all-non-dominated individuals to an array
        individualsCopy = new Individual[individualsList.size()];
        individualsList.toArray(individualsCopy);
        // Sort all the individuals according to the first objective
        for (int i = 0; i < individualsCopy.length - 1; i++) {
            for (int j = i + 1; j < individualsCopy.length; j++) {
                if (individualsCopy[i].getObjective(0)
                        > individualsCopy[j].getObjective(0)) {
                    Individual temp = individualsCopy[i];
                    individualsCopy[i] = individualsCopy[j];
                    individualsCopy[j] = temp;
                }
            }
        }
        // Initialize volume to Zero
        double hyperVolume = 0;
        // Start hypervolume calculations
        for (int i = 0; i < individualsCopy.length; i++) {
            hyperVolume
                    += (referencePointCopy[0]
                    - individualsCopy[i].getObjective(0)) / (referencePoint[0]
                    - idealPoint[0])
                    * (referencePointCopy[1]
                    - individualsCopy[i].getObjective(1)) / (referencePoint[1]
                    - idealPoint[1]);
            referencePointCopy[1] = individualsCopy[i].getObjective(1);
        }
        // Return the resulting volume
        return hyperVolume;
    }

    /**
     * Note that the algorithm assumes that all the solutions are non-dominated.
     * No check is made to ensure this property i.e. if it is not true, the
     * method will return wrong result with NO exceptions or flags.
     *
     * @param objCount
     * @param individuals
     * @param paretoFrontMembers
     * @param power
     * @return
     */
    public static double calculateGenerationalDistance(
            int objCount,
            VirtualIndividual[] individuals,
            VirtualIndividual[] paretoFrontMembers,
            int power) {
        double generationalDistance = 0.0;
        for (int i = 0; i < individuals.length; i++) {
            double minDistance = getDistanceBetween(
                    objCount,
                    individuals[i],
                    paretoFrontMembers[0]);
            for (int j = 1; j < paretoFrontMembers.length; j++) {
                double temp = getDistanceBetween(
                        objCount,
                        individuals[i],
                        paretoFrontMembers[j]);
                if (temp < minDistance) {
                    minDistance = temp;
                }
            }
            generationalDistance += Math.pow(minDistance, power);
        }
        double result = Math.pow(generationalDistance, 1.0 / power)
                / individuals.length;
        return result;
    }

    /**
     * Note that the algorithm assumes that all the solutions are non-dominated.
     * No check is made to ensure this property i.e. if it is not true, the
     * method will return wrong result with NO exceptions or flags.
     *
     * @param objCount
     * @param individuals
     * @param paretoFrontMembers
     * @param power
     * @return
     */
    public static double calculateInvertedGenerationalDistance(
            int objCount,
            VirtualIndividual[] individuals,
            VirtualIndividual[] paretoFrontMembers,
            int power) {
        // To calculate the IGD, just switch the two populations when calling 
        // the GD method.
        return calculateGenerationalDistance(
                objCount,
                paretoFrontMembers,
                individuals,
                power);
    }

    public static double calculateSetCoverageMetric(
            Individual[] setA,
            Individual[] setB) {
        double cAB = 0;
        for (Individual bIndividual : setB) {
            for (Individual aIndividual : setA) {
                if (aIndividual.weaklyDominates(bIndividual, 0)) {
                    cAB++;
                    break;
                }
            }
        }
        return cAB / setB.length;
    }

    /**
     * Get the Euclidean distance between two solutions
     *
     * @param optimizationProblem
     * @param individual1
     * @param individual2
     * @return
     */
    private static double getDistanceBetween(
            int objCount,
            VirtualIndividual individual1,
            VirtualIndividual individual2) {
        double distance = 0;
        for (int m = 0; m < objCount; m++) {
            distance += Math.pow(individual1.getObjective(m)
                    - individual2.getObjective(m), 2);
        }
        return Math.sqrt(distance);
    }

    /**
     * Get the Pareto front members (solutions) of the test problem ZDT1
     *
     * @param individualEvaluator
     * @param n
     * @return
     * @throws InvalidOptimizationProblemException
     * @throws XMLStreamException
     */
    public static Individual[] getZDT1ParetoFront(
            IndividualEvaluator individualEvaluator,
            int n)
            throws
            InvalidOptimizationProblemException,
            XMLStreamException {
        // Load ZDT1 problem
        OptimizationProblem optimizationProblem
                = InputOutput.getProblem("../configurations/zdt1-02-30.xml");
        // Create a random population
        Individual[] paretoFront = new Individual[n];
        for (int i = 0; i < paretoFront.length; i++) {
            paretoFront[i] = new Individual(
                    optimizationProblem,
                    individualEvaluator);
        }

        // Override the original random real variables of the population
        double x0 = 0; // Intialize the first real variable (the only non-zero 
        // variable in ZDT1)
        for (int i = 0; i < n; i++) {
            // Set the first real variable of the individual to x0
            paretoFront[i].real[0] = x0;
            // Assign Zero to all the other real variables of the individual
            for (int j = 1; j < paretoFront[i].real.length; j++) {
                paretoFront[i].real[j] = 0;
            }
            // Update objective values to reflect the new variables values
            individualEvaluator.updateIndividualObjectivesAndConstraints(
                    optimizationProblem,
                    paretoFront[i]);
            // Increment the first real variable
            x0 += 1.0 / (n - 1);
        }
        // Some housekeeping (remember that these individuals are not coming out
        // of any ranking or niching procedure. They are the ultimate 
        // non-dominated solutions of the problem). So, the following house
        // keeping is done just to prevent the user from misusing these
        // individuals in any unexpected way.
        for (Individual individual : paretoFront) {
            individual.validConstraintsViolationValues = false;
            individual.validRankValue = false;
            individual.validReferenceDirection = false;
        }
        // Return thr pareto front
        return paretoFront;
    }

    /**
     * Get the Pareto front members (solutions) of the test problem ZDT2
     *
     * @param individualEvaluator
     * @param n
     * @return
     * @throws InvalidOptimizationProblemException
     * @throws XMLStreamException
     */
    public static Individual[] getZDT2ParetoFront(
            IndividualEvaluator individualEvaluator,
            int n)
            throws
            InvalidOptimizationProblemException,
            XMLStreamException {
        // Load ZDT2 problem
        OptimizationProblem optimizationProblem
                = InputOutput.getProblem("../configurations/zdt2-02-30.xml");
        // Create a random population
        Individual[] paretoFront = new Individual[n];
        for (int i = 0; i < paretoFront.length; i++) {
            paretoFront[i] = new Individual(
                    optimizationProblem,
                    individualEvaluator);
        }
        // Override the original random real variables of the population
        double x0 = 0; // Intialize the first real variable (the only non-zero 
        // variable in ZDT1)
        for (int i = 0; i < n; i++) {
            // Set the first real variable of the individual to x0
            paretoFront[i].real[0] = x0;
            // Assign Zero to all the other real variables of the individual
            for (int j = 1; j < paretoFront[i].real.length; j++) {
                paretoFront[i].real[j] = 0;
            }
            // Update objective values to reflect the new variables values
            individualEvaluator.updateIndividualObjectivesAndConstraints(
                    optimizationProblem,
                    paretoFront[i]);
            // Increment the first real variable
            x0 += 1.0 / (n - 1);
        }
        // Some housekeeping (remember that these individuals are not coming out
        // of any ranking or niching procedure. They are the ultimate 
        // non-dominated solutions of the problem). So, the following house
        // keeping is done just to prevent the user from misusing these
        // individuals in any unexpected way.
        for (Individual individual : paretoFront) {
            individual.validConstraintsViolationValues = false;
            individual.validRankValue = false;
            individual.validReferenceDirection = false;
        }
        // Return thr pareto front
        return paretoFront;
    }

    /**
     * Get the Pareto front members (solutions) of the test problem ZDT3
     *
     * @param individualEvaluator
     * @param n
     * @return
     * @throws InvalidOptimizationProblemException
     * @throws XMLStreamException
     */
    public static Individual[] getZDT3ParetoFront(
            IndividualEvaluator individualEvaluator,
            int n)
            throws
            InvalidOptimizationProblemException,
            XMLStreamException {
        // Load ZDT3 problem
        OptimizationProblem optimizationProblem
                = InputOutput.getProblem("../configurations/zdt3-02-30.xml");
        // Create a random population
        Individual[] paretoFront = new Individual[n];
        for (int i = 0; i < paretoFront.length; i++) {
            paretoFront[i] = new Individual(
                    optimizationProblem,
                    individualEvaluator);
        }
        // Calculate the span of each pareto-optimal interval of X0
        double[] intervalSapn = new double[5];
        intervalSapn[0] = 0.0830015349 - 0.0;
        intervalSapn[1] = 0.2577623634 - 0.1822287280;
        intervalSapn[2] = 0.4538821041 - 0.4093136748;
        intervalSapn[3] = 0.6525117038 - 0.6183967944;
        intervalSapn[4] = 0.8518328654 - 0.8233317983;
        // Calculate the combined (total) span of all x0 intervals
        double totalSpan
                = intervalSapn[0] + intervalSapn[0] + intervalSapn[0]
                + intervalSapn[0] + intervalSapn[0];
        // Calculate the number of solutions to be generated in each interval
        int[] intervalN = new int[5];
        for (int i = 0; i < 5; i++) {
            // The following casting is to convert from long to int (not from 
            // double to int)
            intervalN[i] = (int) Math.round(intervalSapn[i] / totalSpan * n);
        }
        // Due to the approximation above the sum of all intervalN values
        // might not be exactly the same as n. The following lines take care
        // of this issue.
        int intervalIndex = 0;
        while (intervalN[0] + intervalN[1] + intervalN[2] + intervalN[3]
                + intervalN[4] > n) {
            intervalN[intervalIndex]--;
            intervalIndex = (intervalIndex + 1) % 5;
        }
        intervalIndex = 4;
        while (intervalN[0] + intervalN[1] + intervalN[2] + intervalN[3]
                + intervalN[4] < n) {
            if (intervalIndex < 0) {
                intervalIndex = 4;
            }
            intervalN[intervalIndex]++;
            intervalIndex--;
        }
        // Override the original random real variables of the population
        // Interval-0
        double x0 = 0;
        for (int i = 0; i < intervalN[0]; i++) {
            // Set the first real variable of the individual to x0
            paretoFront[i].real[0] = x0;
            // Assign Zero to all the other real variables of the individual
            for (int j = 1; j < paretoFront[i].real.length; j++) {
                paretoFront[i].real[j] = 0;
            }
            // Update objective values to reflect the new variables values
            individualEvaluator.updateIndividualObjectivesAndConstraints(
                    optimizationProblem,
                    paretoFront[i]);
            // Increment the first real variable
            x0 += intervalSapn[0] / (intervalN[0] - 1);
        }
        // Interval-1
        x0 = 0.1822287280;
        for (int i = 0; i < intervalN[1]; i++) {
            // Set the first real variable of the individual to x0
            paretoFront[intervalN[0] + i].real[0] = x0;
            // Assign Zero to all the other real variables of the individual
            for (int j = 1;
                 j < paretoFront[intervalN[0] + i].real.length;
                 j++) {
                paretoFront[intervalN[0] + i].real[j] = 0;
            }
            // Update objective values to reflect the new variables values
            individualEvaluator.updateIndividualObjectivesAndConstraints(
                    optimizationProblem,
                    paretoFront[intervalN[0] + i]);
            // Increment the first real variable
            x0 += intervalSapn[1] / (intervalN[1] - 1);
        }
        // Interval-2
        x0 = 0.4093136748;
        for (int i = 0; i < intervalN[2]; i++) {
            // Set the first real variable of the individual to x0
            paretoFront[intervalN[0] + intervalN[1] + i].real[0] = x0;
            // Assign Zero to all the other real variables of the individual
            for (int j = 1;
                 j < paretoFront[intervalN[0] + intervalN[1] + i].real.length;
                 j++) {
                paretoFront[intervalN[0] + intervalN[1] + i].real[j] = 0;
            }
            // Update objective values to reflect the new variables values
            individualEvaluator.updateIndividualObjectivesAndConstraints(
                    optimizationProblem,
                    paretoFront[intervalN[0] + intervalN[1] + i]);
            // Increment the first real variable
            x0 += intervalSapn[2] / (intervalN[2] - 1);
        }
        // Interval-3
        x0 = 0.6183967944;
        for (int i = 0; i < intervalN[3]; i++) {
            // Set the first real variable of the individual to x0
            paretoFront[intervalN[0] + intervalN[1] + intervalN[2] + i].real[0] = x0;
            // Assign Zero to all the other real variables of the individual
            for (int j = 1;
                 j < paretoFront[intervalN[0] + intervalN[1] + intervalN[2]
                         + i].real.length;
                 j++) {
                paretoFront[intervalN[0] + intervalN[1] + intervalN[2]
                        + i].real[j] = 0;
            }
            // Update objective values to reflect the new variables values
            individualEvaluator.updateIndividualObjectivesAndConstraints(
                    optimizationProblem,
                    paretoFront[intervalN[0] + intervalN[1] + intervalN[2] + i]);
            // Increment the first real variable
            x0 += intervalSapn[3] / (intervalN[3] - 1);
        }
        // Interval-4
        x0 = 0.8233317983;
        for (int i = 0; i < intervalN[4]; i++) {
            // Set the first real variable of the individual to x0
            paretoFront[intervalN[0] + intervalN[1] + intervalN[2]
                    + intervalN[3] + i].real[0] = x0;
            // Assign Zero to all the other real variables of the individual
            for (int j = 1; j < paretoFront[intervalN[0] + intervalN[1]
                    + intervalN[2] + intervalN[3] + i].real.length; j++) {
                paretoFront[intervalN[0] + intervalN[1] + intervalN[2]
                        + intervalN[3] + i].real[j] = 0;
            }
            // Update objective values to reflect the new variables values
            individualEvaluator.updateIndividualObjectivesAndConstraints(
                    optimizationProblem,
                    paretoFront[intervalN[0] + intervalN[1] + intervalN[2]
                            + intervalN[3] + i]);
            // Increment the first real variable
            x0 += intervalSapn[4] / (intervalN[4] - 1);
        }
        // Some housekeeping (remember that these individuals are not coming out
        // of any ranking or niching procedure. They are the ultimate 
        // non-dominated solutions of the problem). So, the following house
        // keeping is done just to prevent the user from misusing these
        // individuals in any unexpected way.
        for (Individual individual : paretoFront) {
            individual.validConstraintsViolationValues = false;
            individual.validRankValue = false;
            individual.validReferenceDirection = false;
        }
        // Return thr pareto front
        return paretoFront;
    }

    /**
     * Get the Pareto front members (solutions) of the test problem ZDT2
     *
     * @param individualEvaluator
     * @param n
     * @return
     * @throws InvalidOptimizationProblemException
     * @throws XMLStreamException
     */
    public static Individual[] getZDT4ParetoFront(
            IndividualEvaluator individualEvaluator,
            int n)
            throws
            InvalidOptimizationProblemException,
            XMLStreamException {
        // Load ZDT4 problem
        OptimizationProblem optimizationProblem
                = InputOutput.getProblem("../configurations/zdt4-02-30.xml");
        // Create a random population
        Individual[] paretoFront = new Individual[n];
        for (int i = 0; i < paretoFront.length; i++) {
            paretoFront[i] = new Individual(
                    optimizationProblem,
                    individualEvaluator);
        }
        // Override the original random real variables of the population
        double x0 = 0; // Intialize the first real variable (the only non-zero 
        // variable in ZDT1)
        for (int i = 0; i < n; i++) {
            // Set the first real variable of the individual to x0
            paretoFront[i].real[0] = x0;
            // Assign Zero to all the other real variables of the individual
            for (int j = 1; j < paretoFront[i].real.length; j++) {
                paretoFront[i].real[j] = 0;
            }
            // Update objective values to reflect the new variables values
            individualEvaluator.updateIndividualObjectivesAndConstraints(
                    optimizationProblem,
                    paretoFront[i]);
            // Increment the first real variable
            x0 += 1.0 / (n - 1);
        }
        // Some housekeeping (remember that these individuals are not coming out
        // of any ranking or niching procedure. They are the ultimate 
        // non-dominated solutions of the problem). So, the following house
        // keeping is done just to prevent the user from misusing these
        // individuals in any unexpected way.
        for (Individual individual : paretoFront) {
            individual.validConstraintsViolationValues = false;
            individual.validRankValue = false;
            individual.validReferenceDirection = false;
        }
        // Return thr pareto front
        return paretoFront;
    }

    public static Individual[] getZDT6ParetoFront(
            IndividualEvaluator individualEvaluator,
            int n)
            throws
            InvalidOptimizationProblemException,
            XMLStreamException {
        // Load ZDT6 problem
        OptimizationProblem optimizationProblem
                = InputOutput.getProblem("../configurations/zdt6-02-10.xml");
        // Create a random population
        Individual[] paretoFront = new Individual[n];
        for (int i = 0; i < paretoFront.length; i++) {
            paretoFront[i] = new Individual(
                    optimizationProblem,
                    individualEvaluator);
        }
        // Override the original random real variables of the population
        double x0 = 0; // Intialize the first real variable (the only non-zero 
        // variable in ZDT1)
        for (int i = 0; i < n; i++) {
            // Set the first real variable of the individual to x0
            paretoFront[i].real[0] = x0;
            // Assign Zero to all the other real variables of the individual
            for (int j = 1; j < paretoFront[i].real.length; j++) {
                paretoFront[i].real[j] = 0;
            }
            // Update objective values to reflect the new variables values
            individualEvaluator.updateIndividualObjectivesAndConstraints(
                    optimizationProblem,
                    paretoFront[i]);
            // Increment the first real variable
            x0 += 1.0 / (n - 1);
        }
        // Some housekeeping (remember that these individuals are not coming out
        // of any ranking or niching procedure. They are the ultimate 
        // non-dominated solutions of the problem). So, the following house
        // keeping is done just to prevent the user from misusing these
        // individuals in any unexpected way.
        for (Individual individual : paretoFront) {
            individual.validConstraintsViolationValues = false;
            individual.validRankValue = false;
            individual.validReferenceDirection = false;
        }
        // Return thr pareto front
        return paretoFront;
    }

    /**
     * Get the Pareto front members (solutions) of the test problem OSY
     *
     * @param individualEvaluator
     * @param n1
     * @param n2
     * @param n3
     * @param n4
     * @param n5
     * @return
     * @throws InvalidOptimizationProblemException
     * @throws XMLStreamException
     */
    public static Individual[] getOSYParetoFront(
            IndividualEvaluator individualEvaluator,
            int n1, int n2, int n3, int n4, int n5)
            throws
            InvalidOptimizationProblemException,
            XMLStreamException {
        // Load problem
        OptimizationProblem optimizationProblem
                = InputOutput.getProblem("../configurations/osy_full.xml");
        // Create a random population
        Individual[] paretoFront = new Individual[n1 + n2 + n3 + n4 + n5];
        for (int i = 0; i < paretoFront.length; i++) {
            paretoFront[i] = new Individual(
                    optimizationProblem,
                    individualEvaluator);
        }
        // Override the original random real variables of the population
        double x3 = 0;
        double x5 = 0;

        // Section AB of the front (see Deb's book)
        double x0 = 5;
        double x1 = 1;
        double x4 = 5;
        // x2 is changing in this section in the range [1,5]
        double x2 = 1;
        for (int i = 0; i < n1; i++) {
            // Set the first real variable of the individual to x0
            paretoFront[i].real[0] = x0;
            paretoFront[i].real[1] = x1;
            paretoFront[i].real[3] = x3;
            paretoFront[i].real[4] = x4;
            paretoFront[i].real[5] = x5;
            // Assign Zero to all the other real variables of the individual
            paretoFront[i].real[2] = x2;
            // Update objective values to reflect the new variables values
            individualEvaluator.updateIndividualObjectivesAndConstraints(
                    optimizationProblem,
                    paretoFront[i]);
            // Increment the first real variable
            x2 += 4.0 / (n1 - 1);
        }

        // Section AB of the front (see Deb's book)
        x0 = 5;
        x1 = 1;
        x4 = 1;
        // x2 is changing in this section in the range [1,5]
        x2 = 1;
        for (int i = n1; i < n2; i++) {
            // Set the first real variable of the individual to x0
            paretoFront[i].real[0] = x0;
            paretoFront[i].real[1] = x1;
            paretoFront[i].real[3] = x3;
            paretoFront[i].real[4] = x4;
            paretoFront[i].real[5] = x5;
            // Assign Zero to all the other real variables of the individual
            paretoFront[i].real[2] = x2;
            // Update objective values to reflect the new variables values
            individualEvaluator.updateIndividualObjectivesAndConstraints(
                    optimizationProblem,
                    paretoFront[i]);
            // Increment the first real variable
            x2 += 4.0 / (n1 - 1);
        }
        // Some housekeeping (remember that these individuals are not coming out
        // of any ranking or niching procedure. They are the ultimate 
        // non-dominated solutions of the problem). So, the following house
        // keeping is done just to prevent the user from misusing these
        // individuals in any unexpected way.
        for (Individual individual : paretoFront) {
            individual.validConstraintsViolationValues = false;
            individual.validRankValue = false;
            individual.validReferenceDirection = false;
        }
        // Return thr pareto front
        return paretoFront;
    }

    public static VirtualIndividual[] getDTLZ1ParetoFront(
            ReferenceDirection[] refDirs) {
        // Create a random population
        VirtualIndividual[] paretoFront = new VirtualIndividual[refDirs.length];
        // Create the individuals so that the square values of all objectives
        // of the individual sum up to 1.
        for (int i = 0; i < refDirs.length; i++) {
            paretoFront[i] = new VirtualIndividual(refDirs[i].direction.length);
            for (int j = 0; j < refDirs[i].direction.length; j++) {
                paretoFront[i].setObjective(j, refDirs[i].direction[j] * 0.5);
            }
        }
        // Return thr pareto front
        return paretoFront;
    }

    public static VirtualIndividual[] getDTLZ2ParetoFront(
            ReferenceDirection[] refDirs) {
        // Create a random population
        VirtualIndividual[] paretoFront = new VirtualIndividual[refDirs.length];
        // Create the individuals so that the square values of all objectives
        // of the individual sum up to 1.
        for (int i = 0; i < refDirs.length; i++) {
            double objectivesSum = 1.0;
            double sumOfSquares = 0.0;
            for (int j = 0; j < refDirs[i].direction.length; j++) {
                sumOfSquares += Math.pow(refDirs[i].direction[j], 2);
            }
            // Solve for x (x is the value multiplied by the direction to
            // extend it until it reaches the Pareto Front).
            double x = Math.sqrt(objectivesSum / sumOfSquares);
            paretoFront[i] = new VirtualIndividual(refDirs[i].direction.length);
            for (int j = 0; j < refDirs[i].direction.length; j++) {
                paretoFront[i].setObjective(j, refDirs[i].direction[j] * x);
            }
        }
        // Return thr pareto front
        return paretoFront;
    }

    public static VirtualIndividual[] getDTLZ4ParetoFront(
            ReferenceDirection[] refDirs) {
        return getDTLZ2ParetoFront(refDirs);
    }

    public static double calculateHyperVolumeForTwoObjectivesOnly(
            AbstractGeneticEngine engine,
            Individual[] individuals,
            List<ReferenceDirection> referenceDirectionsList,
            double[] hvReferencePoint,
            double[] hvIdealPoint,
            double epsilon) {
        // Select only one individual for each reference direction
        Individual[] selectedIndividualsArr
                = OptimizationUtilities
                .selectOnlyOneIndividualForEachDirection(
                        individuals,
                        referenceDirectionsList);
        // Now call hypervolume only on the selected individuals
        return calculateHyperVolumeForTwoObjectivesOnly(
                engine,
                selectedIndividualsArr,
                hvReferencePoint,
                hvIdealPoint,
                epsilon);
    }
}
