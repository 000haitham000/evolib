/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package engines;

import utils.InputOutput;
import utils.Mathematics;
import utils.PerformanceMetrics;
import utils.RandomNumberGenerator;
import emo.DoubleAssignmentException;
import emo.Individual;
import emo.OptimizationProblem;
import emo.OptimizationUtilities;
import java.io.BufferedReader;
import refdirs.ReferenceDirection;
import refdirs.ReferenceDirectionsFactory;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import emo.VirtualIndividual;
import java.io.File;
import java.util.HashMap;
import parsing.IndividualEvaluator;
import refdirs.NestedReferenceDirectionsFactory;

/**
 * Implements the standard NSGA-III algorithm. Details of the algorithm can be
 * found at: (1) Deb, Kalyanmoy, and Himanshu Jain. "An evolutionary
 * many-objective optimization algorithm using reference-point-based
 * nondominated sorting approach, part I: Solving problems with box
 * constraints." IEEE Trans. Evolutionary Computation 18.4 (2014): 577-601. (2)
 * Jain, Himanshu, and Kalyanmoy Deb. "An Evolutionary Many-Objective
 * Optimization Algorithm Using Reference-Point Based Nondominated Sorting
 * Approach, Part II: Handling Constraints and Extending to an Adaptive
 * Approach." IEEE Trans. Evolutionary Computation 18.4 (2014): 602-622.
 *
 * @author Haitham Seada
 */
public abstract class NSGA3Engine extends AbstractGeneticEngine {

    public static final double UTOPIAN_EPSILON = -0.001; // -0.0001
    public static final boolean REMOVE_ADDITIONAL_INDIVIDUALS = false;
    public static final boolean DUMP_ALL_GENERATIONS_REF_DIRS = true;
    public static final boolean DUMP_ALL_GENERATIONS_EXTREME_POINTS = true;

    protected double[] currentIntercepts;
    protected VirtualIndividual[] currentExtremePoints;
    protected List<ReferenceDirection> referenceDirectionsList;
    protected File outDir;

    public NSGA3Engine(
            OptimizationProblem optimizationProblem,
            IndividualEvaluator individualEvaluator,
            int[] divisions) {
        super(optimizationProblem, individualEvaluator);
        // Create Reference Diretions
        referenceDirectionsList = new NestedReferenceDirectionsFactory(
                optimizationProblem.objectives.length)
                .generateDirections(divisions);
        //adjustRefDirs(referenceDirectionsList, UTOPIAN_EPSILON);
        if (DEBUG_ALL || DEBUG_REFERENCE_DIRECTIONS) {
            InputOutput.displayReferenceDirections(
                    "Reference Directions",
                    referenceDirectionsList);
            System.out.format(
                    "%d reference directions generated.%n",
                    referenceDirectionsList.size());
        }
    }

    public NSGA3Engine(
            OptimizationProblem optimizationProblem,
            IndividualEvaluator individualEvaluator) {
        super(optimizationProblem, individualEvaluator);
        // Create Reference Diretions
        referenceDirectionsList = new ReferenceDirectionsFactory(
                optimizationProblem.objectives.length)
                .generateDirections(optimizationProblem.getSteps());
        //adjustRefDirs(referenceDirectionsList, UTOPIAN_EPSILON);
        if (DEBUG_ALL || DEBUG_REFERENCE_DIRECTIONS) {
            InputOutput.displayReferenceDirections(
                    "Reference Directions",
                    referenceDirectionsList);
            System.out.format(
                    "%d reference directions generated.%n",
                    referenceDirectionsList.size());
        }
    }

    /**
     *
     * @param optimizationProblem
     * @param individualEvaluator
     * @param directionsFilePath
     */
    public NSGA3Engine(
            OptimizationProblem optimizationProblem,
            IndividualEvaluator individualEvaluator,
            String directionsFilePath)
            throws
            IOException {
        super(optimizationProblem, individualEvaluator);
        // Read Directions from File
        BufferedReader reader = null;
        try {
            referenceDirectionsList = new ArrayList<ReferenceDirection>();
            reader = new BufferedReader(new FileReader(directionsFilePath));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] splits = line.trim().split(" ");
                if (!line.isEmpty()) {
                    double[] dirValues = new double[splits.length];
                    for (int i = 0; i < splits.length; i++) {
                        dirValues[i] = Double.parseDouble(splits[i]);
                    }
                    referenceDirectionsList.add(
                            new ReferenceDirection(dirValues));
                }
            }
            adjustRefDirs(referenceDirectionsList, UTOPIAN_EPSILON);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        InputOutput.displayReferenceDirections(
                "Reference Directions",
                referenceDirectionsList);
        System.out.format(
                "%d reference directions generated.%n",
                referenceDirectionsList.size());
    }

    /**
     * Set the origin of the reference directions back to the utopian point
     * instead of the ideal point.
     *
     * @param referenceDirectionsList the list of reference directions to be
     * fixed
     * @param utopian_epsilon the distance between the ideal and the utopian
     * point in one dimension (assuming that this distance is the same across
     * all dimensions).
     */
    protected final void adjustRefDirs(
            List<ReferenceDirection> referenceDirectionsList,
            double utopian_epsilon) {
        for (ReferenceDirection refDir : referenceDirectionsList) {
            for (int i = 0; i < refDir.direction.length; i++) {
                refDir.direction[i] = refDir.direction[i] - utopian_epsilon;
            }
        }
    }

//    @Override
//    protected Individual tournamentSelect(IndividualsSet subset) {
//        Individual individual1 = subset.getIndividual1();
//        Individual individual2 = subset.getIndividual2();
//        if (individual1.isFeasible() && !individual2.isFeasible()) {
//            return individual1;
//        } else if (!individual1.isFeasible() && individual2.isFeasible()) {
//            return individual2;
//        } else if (!individual1.isFeasible() && !individual2.isFeasible()) {
//            if (individual1.getTotalConstraintViolation() < individual2.getTotalConstraintViolation()) {
//                return individual1;
//            } else {
//                return individual2;
//            }
//        } else if (RandomNumberGenerator.nextInt() <= 0.5) {
//            return individual1;
//        } else {
//            return individual2;
//        }
//    }
    /**
     * Update the extreme points from one generation to the nextInt. Notice that
     * the code makes sure that a better extreme point (from an earlier
     * generation) is kept until a better one is encountered.
     *
     * @param individuals an array of solutions
     * @param previousExtremePoints the list of extreme points we have so far
     * @param idealPoint the ideal point we have so far
     * @param prevIdealPoint the ideal point prior to the current one
     * @param optionalDisplayMessage a message to be displayed for debugging
     * purposes
     */
    protected void updateExtremePoints(
            VirtualIndividual[] individuals,
            VirtualIndividual[] previousExtremePoints,
            double[] idealPoint,
            double[] prevIdealPoint,
            String optionalDisplayMessage) {
        VirtualIndividual[] extremePoints
                = OptimizationUtilities.getExtremePoints(
                        previousExtremePoints,
                        idealPoint,
                        prevIdealPoint,
                        individuals);
        // update currentExtremePoints (in all basic directions)
        currentExtremePoints = extremePoints;
        if (DEBUG_ALL || DEBUG_INTERCEPTS) {
            InputOutput.displayExtremePoints(
                    optimizationProblem,
                    optionalDisplayMessage != null ? optionalDisplayMessage : "",
                    currentExtremePoints);
        }
    }

    /**
     * Updates the current intercepts (used for normalization) according to the
     * spread of these individuals.
     *
     * @param individuals an array of solutions
     * @param extremePoints extreme points to be used
     * @param optionalDisplayMessage a message to be displayed for debugging
     * purposes
     */
    protected void updateIntercepts(
            Individual[] individuals,
            VirtualIndividual[] extremePoints,
            String optionalDisplayMessage) {
        double[] intercepts = getIntercepts(
                individuals,
                extremePoints,
                optionalDisplayMessage);
        // Update currentIntercepts
        currentIntercepts = intercepts;
    }

    /**
     * Calculates the intercepts of the current set of individuals using the
     * provided extreme points.
     *
     * @param individuals an array of solutions
     * @param extremePoints current extreme points
     * @param optionalDisplayMessage a message to be displayed for debugging
     * purposes
     * @return
     */
    protected double[] getIntercepts(
            Individual[] individuals,
            VirtualIndividual[] extremePoints,
            String optionalDisplayMessage) {
        // Calculating the vector of maximum objective values & the Nadir point
        // Initialize the structures & set all their initial values to
        // Negative Infinity
        double[] maxObjValues = new double[optimizationProblem.objectives.length];
        double[] nadirPoint = new double[optimizationProblem.objectives.length];
        for (int i = 0; i < nadirPoint.length; i++) {
            maxObjValues[i] = -1 * Double.MIN_VALUE;
            nadirPoint[i] = -1 * Double.MIN_VALUE;
        }
        // Traverse all the individuals of the population and get their maximum
        // value of objective (The simplest way of calculating the nadir point 
        // is to get these maximum values among the first front individuals)
        for (int i = 0; i < individuals.length; i++) {
            for (int j = 0; j < optimizationProblem.objectives.length; j++) {
                if (maxObjValues[j]
                        < individuals[i].getObjective(j)
                        - currentIdealPoint[j]) {
                    maxObjValues[j]
                            = individuals[i].getObjective(j)
                            - currentIdealPoint[j];
                }
                if (individuals[i].getRank() == 1) {
                    if (nadirPoint[j]
                            < individuals[i].getObjective(j)
                            - currentIdealPoint[j]) {
                        nadirPoint[j]
                                = individuals[i].getObjective(j)
                                - currentIdealPoint[j];
                    }
                }
            }
        }
        if (DEBUG_ALL || DEBUG_INTERCEPTS) {
            System.out.println("-----------");
            System.out.println("Nadir Point");
            System.out.println("-----------");
            System.out.print("(");
            for (int i = 0; i < nadirPoint.length; i++) {
                System.out.format("%5.2f", nadirPoint[i]);
                if (i != nadirPoint.length - 1) {
                    System.out.print(",");
                }
            }
            System.out.println(")");

            System.out.println("-----------");
            System.out.println("Max Vector");
            System.out.println("-----------");
            System.out.print("(");
            for (int i = 0; i < maxObjValues.length; i++) {
                System.out.format("%5.2f", maxObjValues[i]);
                if (i != maxObjValues.length - 1) {
                    System.out.print(",");
                }
            }
            System.out.println(")");
        }
        // Caculating the currentIntercepts
        double[] intercepts = new double[optimizationProblem.objectives.length];
        // Create the hyperplane
        // Prepare your arrays for gaussian elimination
        double[][] Z = new double[optimizationProblem.objectives.length][optimizationProblem.objectives.length];
        for (int i = 0; i < Z.length; i++) {
            for (int j = 0; j < Z[i].length; j++) {
                Z[i][j] = extremePoints[i].getObjective(j)
                        - currentIdealPoint[j];
            }
        }
        double[] u = new double[optimizationProblem.objectives.length];
        for (int i = 0; i < u.length; i++) {
            u[i] = 1;
        }
        boolean useNadir = false;
        // Solve the system of equations using gaussian elimination
        try {
            intercepts = Mathematics.gaussianElimination(Z, u);
        } catch (Mathematics.SingularMatrixException ex) {
            useNadir = true;
        }
        // If gaussian elimination resulted in -ve or infinite currentIntercepts,
        // again use the nadir point.
        if (useNadir) {
            for (double intercept : intercepts) {
                if (intercept < MIN_DOUBLE_VALUE) {
                    useNadir = true;
                    break;
                }
            }
        }
        // Now get the intercept = 1/alpha
        if (!useNadir) {
            for (int i = 0; i < intercepts.length; i++) {
                intercepts[i] = 1 / intercepts[i];
                if (intercepts[i] == Double.NaN) {
                    useNadir = true;
                }
            }
        }
        // If the follwing condition is true this means that you have to resort 
        // to the nadir point
        if (useNadir) {
            System.arraycopy(nadirPoint, 0, intercepts, 0, intercepts.length);
        }
        // If any of the currentIntercepts is still Zero (which means that one
        // of the nadir values is Zero), then use the maximum value of each
        // objective instead (remember that these values were calculated among
        // all the individuals, not just the first-front individuals)
        for (double intercept : intercepts) {
            if (intercept < MIN_DOUBLE_VALUE) {
                System.arraycopy(
                        maxObjValues,
                        0,
                        intercepts,
                        0,
                        intercepts.length);
                break;
            }
        }
        // Debugging
        if (DEBUG_ALL || DEBUG_INTERCEPTS) {
            InputOutput.displayIntercepts(
                    optionalDisplayMessage != null ? optionalDisplayMessage : "",
                    intercepts);
        }
        return intercepts;
    }

    /**
     * Associate each individual to one reference direction. Notice that the
     * association is performed in the normalized space i.e. all individuals are
     * normalized before association. Notice that, reference directions by
     * definition are already in the normalized space.
     *
     * @param individuals an array of solutions
     * @param referenceDirections a list of reference directions
     * @param idealPoint current ideal point
     * @param intercepts current intercepts
     * @param utopianEpsilon distance between ideal and utopian points
     * @param optionalDisplayMessage a message to be displayed for debugging
     * purposes
     * @return a 2D array whose (i, j) cell represents the perpendicular
     * distance from individual i to reference direction j.
     */
    public static double[][] associate(
            Individual[] individuals,
            List<ReferenceDirection> referenceDirections,
            double[] idealPoint,
            double[] intercepts,
            double utopianEpsilon,
            String optionalDisplayMessage) {
        // Reset Reference Directions Information
        for (ReferenceDirection referenceDirection : referenceDirections) {
            referenceDirection.surroundingIndividuals.clear();
        }
        double[][] distanceMatrix
                = new double[individuals.length][referenceDirections.size()];
        for (Individual individual : individuals) {
            // Initialize the array of distances
            individual.distancesFromDirs
                    = new double[referenceDirections.size()];
        }
        /* Perpendicular distance calculation */
        for (int i = 0; i < referenceDirections.size(); i++) {
            for (int j = 0; j < individuals.length; j++) {
                double perpendicularDistance
                        = getPerpendicularDistance(
                                individuals[j],
                                referenceDirections.get(i),
                                idealPoint,
                                intercepts,
                                utopianEpsilon);
                // Store the distance in the matrix and in the individual object
                distanceMatrix[j][i] = perpendicularDistance;
                individuals[j].distancesFromDirs[i] = distanceMatrix[j][i];
            }
        }
        if (DEBUG_ALL || DEBUG_ASSOCIATION) {
            InputOutput.displayDistanceMatrix(
                    idealPoint.length,
                    referenceDirections,
                    individuals,
                    distanceMatrix);
        }
        // Find the closest reference direction to each individual
        for (int i = 0; i < individuals.length; i++) {
            double minDistance = distanceMatrix[i][0];
            ReferenceDirection refDir = referenceDirections.get(0);
            for (int j = 1; j < referenceDirections.size(); j++) {
                if (distanceMatrix[i][j] < minDistance) {
                    minDistance = distanceMatrix[i][j];
                    refDir = referenceDirections.get(j);
                }
            }
            individuals[i].setReferenceDirection(refDir);
            individuals[i].setPerpendicularDistance(minDistance);
            refDir.surroundingIndividuals.add(individuals[i]);
            individuals[i].validReferenceDirection = true;
        }
        if (DEBUG_ALL || DEBUG_ASSOCIATION) {
            InputOutput.displayAssociationResluts(
                    idealPoint.length,
                    optionalDisplayMessage != null ? optionalDisplayMessage : "",
                    individuals);
        }
        return distanceMatrix;
    }

    public static double getPerpendicularDistance(
            VirtualIndividual individual,
            ReferenceDirection referenceDirection,
            double[] idealPoint,
            double[] intercepts,
            double utopianEpsilon) {
        double[] normalizedObjectives = getNormalizedObjectives(
                individual, idealPoint, intercepts, utopianEpsilon);
        double requiredDistance = Mathematics.getPerpendicularDistance(
                normalizedObjectives, referenceDirection.direction);
        return requiredDistance;
    }

    protected static double[] getNormalizedObjectives(
            VirtualIndividual individual,
            double[] idealPoint,
            double[] intercepts,
            double utopianEpsilon) {
        double[] normalizedVector = new double[individual.getObjectivesCount()];
        for (int i = 0; i < individual.getObjectivesCount(); i++) {
            normalizedVector[i]
                    = (individual.getObjective(i) - idealPoint[i])
                    / intercepts[i] - utopianEpsilon;
        }
        return normalizedVector;
    }

    /**
     * Performs NSGA-III niching. It selects which individuals will make it to
     * the nextInt population out of all individuals found in the last front
     * that cannot be fully accommodated (in the nextInt population).
     *
     * @param fronts a list of Pareto fronts where front i must have at least
     * one individual that dominates at least another individual in front i+1.
     * @param remainingIndvsCount the number of still-available slots in the
     * nextInt population
     * @return an array of those individuals that will make it to the nextInt
     * population
     */
    @Override
    public Individual[] niching(
            List<List<Individual>> fronts, int remainingIndvsCount) {
        List<Individual> lastFront = getLastPartiallyAccomodatedFront(fronts);
        // Make a copy of the directionsList (is this necessary?)
        List<ReferenceDirection> referenceDirectionsListCopy
                = new ArrayList<ReferenceDirection>(referenceDirectionsList);
        // Make a copy of the last front Fl (is this necessary?)
        List<Individual> lastFrontCopy = new ArrayList<Individual>(lastFront);
        // Create associationCountArr array (the number of individuals
        // associated with each Direction in P(t+1)):
        // Remember that the value refDirection.surroundingIndividuals is not
        // what we want here, because it contains the number of individuals
        // associated with the direction out of all the candidates (St), while
        // what we want here is the number of individuals associated with the
        // direction out of the confirmed members only (Pt+1).
        // Remember: (Pt+1) represents those fronts that will be fully
        // accommodated. In other words, the last partially accommodated front
        // is not included in (Pt+1).
        List<Integer> associationCountList = getConfirmedAssociationCount(
                referenceDirectionsListCopy, fronts, lastFront);
        // Find the associations of (Fl), the last front that will be partially
        // accommodated.
        List<List<Individual>> lastFrontRefAssociations
                = getLastFrontAssociations(
                        referenceDirectionsListCopy, lastFrontCopy);
        // Resolving common references (is this necessary?)
        checkCommonReferences(
                lastFrontRefAssociations,
                "lastFrontRefAssociations");
        // Create the final array of members that will complete the remaining
        // part of the population.
        Individual[] remainingIndividuals = new Individual[remainingIndvsCount];
        // Repeat until REMAINING members are added to the final list
        while (remainingIndvsCount > 0) {
            // Find the reference directions with the lowest number of
            // associated members.
            int minDirClusterSize = associationCountList.get(0);
            for (int i = 1; i < referenceDirectionsListCopy.size(); i++) {
                if (associationCountList.get(i) < minDirClusterSize) {
                    minDirClusterSize = associationCountList.get(i);
                }
            }
            // The following part is unique to this method and is not available
            // in the deterministic version of it. Just get all the directions
            // having the same minimum associationCount and pick one of them
            // randomly.
            List<Integer> minAssociationsDirsIndices = new ArrayList<Integer>();
            for (int i = 0; i < referenceDirectionsListCopy.size(); i++) {
                if (associationCountList.get(i) == minDirClusterSize) {
                    minAssociationsDirsIndices.add(i);
                }
            }
            // Select a random direction
            int dirIndex = minAssociationsDirsIndices.get(
                    RandomNumberGenerator.nextInt(0,
                            minAssociationsDirsIndices.size() - 1));
            // If no points in (Fl) are associated to the selected direction,
            // discard this direction from the set of ref. dirs. of the current
            // genetaion only, i.e. this direction will be re-included in the
            // following generation.
            if (lastFrontRefAssociations.get(dirIndex).isEmpty()) {
                referenceDirectionsListCopy.remove(dirIndex);
                associationCountList.remove(dirIndex);
                lastFrontRefAssociations.remove(dirIndex);
            } else {
                int newMemberIndex;
                if (associationCountList.get(dirIndex) == 0) {
                    // If the number of members(of Pt+1) associated with the
                    // direction is Zero, select the closest member of (Fl) to
                    // the direction.
                    int minDistanceIndividualIndex;
                    minDistanceIndividualIndex = 0;
                    double minDistance
                            = lastFrontRefAssociations
                                    .get(dirIndex)
                                    .get(0)
                                    .getPerpendicularDistance();
                    for (int i = 1;
                            i < lastFrontRefAssociations.get(dirIndex).size();
                            i++) {
                        if (lastFrontRefAssociations
                                .get(dirIndex)
                                .get(i)
                                .getPerpendicularDistance() < minDistance) {
                            minDistanceIndividualIndex = i;
                            minDistance = lastFrontRefAssociations
                                    .get(dirIndex)
                                    .get(i)
                                    .getPerpendicularDistance();
                        }
                    }
                    newMemberIndex = minDistanceIndividualIndex;
                } else {
                    // Otherwise, select any of those members attached to the
                    // direction in (Fl). (here we take the first as the list
                    // is not ordered in any way).
                    newMemberIndex = 0;
                }
                // Add the selected member to the final list
                remainingIndividuals[remainingIndividuals.length - remainingIndvsCount]
                        = lastFrontRefAssociations
                                .get(dirIndex)
                                .get(newMemberIndex);
                // Add the new member to the list of members associated with the
                // direction under consideration.
                associationCountList.set(
                        dirIndex,
                        associationCountList.get(dirIndex).intValue() + 1);
                // Exclude the new member from the the last front that
                // eventually will be partially accommodated (Fl).
                lastFrontRefAssociations.get(dirIndex).remove(newMemberIndex);
                // Decrement the counter of remaining members
                remainingIndvsCount--;
            }
        }
        return remainingIndividuals;
    }

    /**
     * Calculates associations of the individuals of the last front.
     *
     * @param referenceDirectionsList list of reference directions
     * @param lastFront last front Fl
     * @return a list of lists where element list.get(i).get(j) is an individual
     * that is associated with direction i.
     */
    private List<List<Individual>> getLastFrontAssociations(
            List<ReferenceDirection> referenceDirectionsList,
            List<Individual> lastFront) {
        // The following list of lists contains all the associations of the last
        // front Fl
        List<List<Individual>> lastFrontRefAssociations
                = new ArrayList<List<Individual>>();
        for (int d = 0; d < referenceDirectionsList.size(); d++) {
            // Find the set of members in Fl associated with direction d
            // (nextFrontAssociatedMembers)
            List<Individual> lastFrontAssociatedMembersList
                    = new ArrayList<Individual>();
            for (Individual individual : lastFront) {
                if (individual
                        .getReferenceDirection()
                        .equals(referenceDirectionsList.get(d))) {
                    lastFrontAssociatedMembersList.add(individual);
                }
            }
            lastFrontRefAssociations.add(lastFrontAssociatedMembersList);
        }
        return lastFrontRefAssociations;
    }

    /**
     *
     * @param referenceDirectionsList
     * @param fronts
     * @param lastFront
     * @return
     */
    private List<Integer> getConfirmedAssociationCount(
            List<ReferenceDirection> referenceDirectionsList,
            List<List<Individual>> fronts,
            List<Individual> lastFront) {
        List<Integer> associationCountList = new ArrayList<Integer>();
        for (int i = 0; i < referenceDirectionsList.size(); i++) {
            associationCountList.add(new Integer(0));
            for (List<Individual> front : fronts) {
                if (front.equals(lastFront)) {
                    break;
                }
                for (Individual individual : front) {
                    if (individual.getReferenceDirection()
                            .equals(referenceDirectionsList.get(i))) {
                        associationCountList
                                .set(i, associationCountList.get(i) + 1);
                    }
                }
            }
        }
        return associationCountList;
    }

    private List<Individual> getLastPartiallyAccomodatedFront(
            List<List<Individual>> fronts) {
        int individualsCount = 0;
        // Get the last front Fl index
        int lastFrontIndex = -1;
        while (individualsCount < optimizationProblem.getPopulationSize()) {
            lastFrontIndex++;
            individualsCount += fronts.get(lastFrontIndex).size();
        }
        // Get the last front Fl (using the index)
        List<Individual> lastFront = fronts.get(lastFrontIndex);
        return lastFront;
    }

    @Override
    public Individual[] start(
            File outputDir,
            int runIndex,
            double epsilon,
            double hvLimit,
            int funcEvaluationsLimit
    )
            throws
            FileNotFoundException,
            DoubleAssignmentException,
            IOException {
        // Set necessary fields
        this.runIndex = runIndex;
        this.epsilon = epsilon;
        this.hvLimit = hvLimit;
        this.funcEvaluationsLimit = funcEvaluationsLimit;
        this.outDir = outputDir;
        // Generate the initial population and perform all necessary
        // initializations before starting the main loop
        currentPopulation = initialize();
        report(outputDir);
        // Each loop represents one generation
        do {
            // Whatever logic is needed at the beginning of every iteration
            iterationStart();
            // Create the offspring (tournament selection & crossover)
            Individual[] offspringPopulation
                    = getOffspringPopulation(currentPopulation);
            // Mutation (binary & real)
            mutate(offspringPopulation);
            // Update objective values & constraints violation values of the 
            // offspring
            updateObjectives(offspringPopulation);
            // Whatever logic is necessary after creating the offspring
            postOffspringCreation(offspringPopulation);
            // Merge the parents and the offspring in a single population
            Individual[] mergedPopulation = merge(currentPopulation, offspringPopulation);
            // Non-dominated sorting
            List<List<Individual>> fronts
                    = rankIndividuals(
                            mergedPopulation,
                            epsilon,
                            String.format(
                                    "%d-population+%d-offspring",
                                    currentGenerationIndex,
                                    currentGenerationIndex));
            // Whatever logic is necessary after the non-dominated sorting
            postNondominatedSorting(fronts);
            // Update ideal point
            updateIdealPoint(
                    mergedPopulation,
                    currentIdealPoint,
                    String.format("Ideal Point(merged population)"));
            // Translate objective values
            //translate(mergedPopulation, currentIdealPoint);
            // Create a new population for the nextInt generation, according to the
            // non-dominated sorting performed and using niching if necessary.
            fillUpPopulation(fronts, mergedPopulation);
            // Reset the values of the objective values for the nextInt iteration
            //unTranslate(currentPopulation);
            // Display the population at the end of this generation if debugging
            // flags are set.
            displayPopulationIfDebug(currentGenerationIndex, currentPopulation);
            // Update all the metrics that might be used for termination.
            updateTerminationMetrics();
            // Whatever logic is needed at the end of every iteration
            iterationEnd();
            // Report generation-wise information
            report(outputDir);
        } while (!terminationCondition());
        // Prepare the final population.
        Individual[] finalPopulation = prepareFinalPopulation();
        // Whatever logic is needed before returning the final population
        finalize(finalPopulation);
        // Return the final population
        return finalPopulation;
    }

    /**
     * Prepare the final population. Usually dominated individuals are removed.
     * Other preparations might be required as well e.g. removing individuals
     * that do not represent any reference direction i.e. those individuals
     * whose directions are represented by other better (closer) individuals.
     *
     * @return
     */
    @Override
    protected Individual[] prepareFinalPopulation() {
        Individual[] finalPopulation;
        if (REMOVE_ADDITIONAL_INDIVIDUALS) {
            finalPopulation = OptimizationUtilities.getNonDominatedIndividuals(
                    OptimizationUtilities.selectOnlyOneIndividualForEachDirection(
                            currentPopulation,
                            referenceDirectionsList),
                    epsilon);
        } else {
            finalPopulation
                    = OptimizationUtilities.getNonDominatedIndividuals(
                            currentPopulation,
                            epsilon);
        }
        return finalPopulation;
    }

    @Override
    protected void updateTerminationMetrics() throws UnsupportedOperationException {
        // Update the current hypervolume (if hypervolume is used as a stopping criterion)
        updateCurrentHV(hvLimit, currentPopulation, epsilon);
        // Increment generationsIndex
        currentGenerationIndex++;
    }

    private void updateCurrentHV(
            double hvLimit,
            Individual[] currentPopulation,
            double epsilon)
            throws
            UnsupportedOperationException {
        if (hvLimit != Double.MAX_VALUE) {
            // Calculate current hypervolume
            if (optimizationProblem.objectives.length == 2) {
                if (REMOVE_ADDITIONAL_INDIVIDUALS) {
                    currentHV = PerformanceMetrics
                            .calculateHyperVolumeForTwoObjectivesOnly(
                                    this,
                                    currentPopulation,
                                    referenceDirectionsList,
                                    hvReferencePoint,
                                    hvIdealPoint,
                                    epsilon);
                } else {
                    currentHV = PerformanceMetrics
                            .calculateHyperVolumeForTwoObjectivesOnly(
                                    this,
                                    currentPopulation,
                                    hvReferencePoint,
                                    hvIdealPoint,
                                    epsilon);
                }
            } else {
                throw new UnsupportedOperationException(
                        "Hypervolume can only be calculated for 2 objectives");
            }
        }
    }

    @Override
    protected void fillUpPopulation(
            List<List<Individual>> fronts,
            Individual[] mergedPopulation) {
        // Important Note 1:
        // Association and Normalization: Notice that in NSGA-III normalization
        // & association need not be performed unless the final front cannot be
        // fully accomodated. However, in U-NSGA-III association (and 
        // consequently normalization) must be performed in all cases, because
        // association is used in tournament selection. So, here we perform
        // association (and consequently normalization) no matter what the case
        // is i.e. we normalize and associate all feasible candidates even if
        // the final front can be fully accomodated.
        // Important Note 2:
        // Infeasible solutions should not go through normalization,
        // association and niching. This destroys normalization and
        // degrades results significantly.
        // Retrieve all the individuals in the fronts to be accomodated,
        // even those individuals of the last front that might be partially
        // accommodated.
        Individual[] candidates
                = OptimizationUtilities.getCandidates(
                        mergedPopulation,
                        fronts,
                        optimizationProblem.getPopulationSize());
        // Retrieve the feasible solutions only from all the candidates
        Individual[] feasibleCandidates
                = OptimizationUtilities.getFeasibleIndividuals(candidates);
        if (feasibleCandidates.length > 0) {
            updateExtremePoints(
                    feasibleCandidates,
                    currentExtremePoints,
                    currentIdealPoint,
                    previousIdealPoint,
                    "Merged Population Extreme Points");
            updateIntercepts(
                    feasibleCandidates,
                    currentExtremePoints,
                    "Merged Population Intercepts");
            // Association (Actual normalization is performed in this step
            // ON-THE-FLY to perform association in the normalized space.
            // The actual normlized objective values are never stored)
            associate(
                    feasibleCandidates,
                    referenceDirectionsList,
                    currentIdealPoint,
                    currentIntercepts,
                    UTOPIAN_EPSILON, "Merged Population");
            // Get reamining individuals i.e. the number of individuals needed
            // using niching in order to completely fill the population.
            int remainingIndividualsCount = getRemainingCount(fronts);
            if (remainingIndividualsCount == 0) {
                // Re-fill the new population in the case when niching is not
                // required.
                reFillPopulation(currentPopulation, fronts);
            } else {

                // This is where normalization & association used to be. Now they
                // are performed before the if/else block. See the note comment
                // found at the beginning of this method.
                // Get the index of the last front to be accommodated (either fully
                // or partially)
                int limitingFrontIndex = getLimitingFrontIndex(fronts);
                // Niching is required only if the number of feasible solutions in
                // the merged population is greater the population size, and the
                // number of remaining individuals to complete the population is
                // greater than Zero. (Notice that having a number of feasible
                // solutions greater than the population size does not mean that
                // niching is necessary. Assume that population size is 8 and the
                // first front contains 5 individuals while the second front
                // contains 3 individuals. In this case even if there exist more
                // feasible individuals in subsequent fronts, they are useless,
                // because the new population is now full and niching is not
                // required.
                // Niching
                Individual[] lastFrontSubset
                        = niching(fronts, remainingIndividualsCount);
                if (DEBUG_ALL) {
                    System.out.println("---------------");
                    System.out.println("Niching Results");
                    System.out.println("---------------");
                    for (Individual individual : lastFrontSubset) {
                        System.out.println(individual.getShortVariableSpace());
                    }
                }
                // Re-fill the new population after niching
                reFillPopulation(
                        currentPopulation,
                        mergedPopulation,
                        lastFrontSubset,
                        limitingFrontIndex);
            }
        }
    }

    protected Individual[] initialize() {
        // Initialize necessary fields
        initializeFields();
        // Generate initial population
        Individual[] initialPopulation = generateInitialPopulation("0");
        // Perform initial ranking
        rankIndividuals(initialPopulation, epsilon, "0");
        // Create the initial ideal point
        currentIdealPoint = getInitialIdealPoint(initialPopulation, "0");
        // Translate all the objective values
        //translate(initialPopulation, currentIdealPoint);
        if (OptimizationUtilities
                .getFeasibleIndividuals(initialPopulation).length > 0) {
            // Calculate extreme points (needed to calculate currentIntercepts)
            updateExtremePoints(
                    OptimizationUtilities
                            .getFeasibleIndividuals(initialPopulation),
                    currentExtremePoints,
                    currentIdealPoint,
                    previousIdealPoint,
                    null);
            // Cacluate currentIntercepts (needed for normalization)
            updateIntercepts(
                    OptimizationUtilities
                            .getFeasibleIndividuals(initialPopulation),
                    currentExtremePoints,
                    "Initial Population Intercepts");
        }
        // Reset obj. values to the pre-translation values (in order to start
        // the loop with valid objective values)
        //unTranslate(initialPopulation);
        return initialPopulation;
    }

    @Override
    protected void initializeFields() {
        super.initializeFields();
        currentIntercepts = null;
        currentExtremePoints = null;
    }

    @Override
    public HashMap<String, StringBuilder> reportGenerationWiseInfo()
            throws
            IOException {
        HashMap<String, StringBuilder> dumpMap
                = super.reportGenerationWiseInfo();
        // <editor-fold desc="Dumping reference directions data" defaultstate="collapsed">
        if (DUMP_ALL_GENERATIONS_REF_DIRS && DUMP_ALL_GENERATIONS_META_DATA) {
            // Dump all reference directions list
            // This section needs to be modified to write ref-dirs files in
            // a separate directory.
            // See the desired directiory structure at:
            // D:/corrected plotting script/version_2_11March2015
            StringBuilder refDirsSb
                    = InputOutput.collectRefDirs(this.referenceDirectionsList);
            dumpMap.put("refdirs.dat", refDirsSb);
        }
        // </editor-fold>
        // <editor-fold desc="Append extreme points to the meta data" defaultstate="collapsed">
        if (DUMP_ALL_GENERATIONS_EXTREME_POINTS && DUMP_ALL_GENERATIONS_META_DATA) {
            StringBuilder metaDataSb = dumpMap.get("meta.txt");
            if (this.currentExtremePoints != null) {
                for (int i = 0; i < this.currentExtremePoints.length; i++) {
                    String extPts = String.format("extreme_point_%02d = [", i);
                    for (int j = 0;
                            j < this.currentExtremePoints[i]
                                    .getObjectivesCount();
                            j++) {
                        extPts += this.currentExtremePoints[i].getObjective(j);
                        if (j != this.currentExtremePoints[i]
                                .getObjectivesCount() - 1) {
                            extPts += ", ";
                        }
                    }
                    extPts += String.format("]%n");
                    metaDataSb.append(extPts);
                }
            } else {
                for (int i = 0;
                        i < optimizationProblem.objectives.length;
                        i++) {
                    String extPts = String.format("extreme_point_%02d = [", i);
                    for (int j = 0;
                            j < optimizationProblem.objectives.length;
                            j++) {
                        extPts += "NaN";
                        if (j != optimizationProblem.objectives.length - 1) {
                            extPts += ", ";
                        }
                    }
                    extPts += String.format("]%n");
                    metaDataSb.append(extPts);
                }
            }
        }
        // </editor-fold>
        return dumpMap;
    }

    @Override
    public String getAlgorithmName() {
        return "nsga3";
    }
}
