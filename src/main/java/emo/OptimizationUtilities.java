/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package emo;

import static engines.AbstractGeneticEngine.EXTREME_POINTS_DEEP_DEBUG;
import utils.Mathematics;
import static engines.AbstractGeneticEngine.MAX_DOUBLE_VALUE;
import java.util.ArrayList;
import java.util.List;
import parsing.IndividualEvaluator;
import refdirs.ReferenceDirection;

/**
 * This class represents all utility operations related to optimization. Which
 * is different from other general utility operations.
 *
 * @author Haitham Seada
 */
public class OptimizationUtilities {

    public static Individual[] getFeasibleIndividuals(
            Individual[] individuals) {
        List<Individual> feasibleIndividuals = new ArrayList<Individual>();
        for (int i = 0; i < individuals.length; i++) {
            if (individuals[i].isFeasible()) {
                feasibleIndividuals.add(individuals[i]);
            }
        }
        Individual[] feasibleIndividualsArr
                = new Individual[feasibleIndividuals.size()];
        feasibleIndividuals.toArray(feasibleIndividualsArr);
        return feasibleIndividualsArr;
    }

    public static Individual[] getFeasibleIndividuals(
            List<List<Individual>> fronts) {
        List<Individual> feasibleIndividuals = new ArrayList<Individual>();
        for (List<Individual> front : fronts) {
            for (Individual individual : front) {
                if (individual.isFeasible()) {
                    feasibleIndividuals.add(individual);
                }
            }
        }
        Individual[] feasibleIndividualsArr
                = new Individual[feasibleIndividuals.size()];
        feasibleIndividuals.toArray(feasibleIndividualsArr);
        return feasibleIndividualsArr;
    }

    public static double getMinObjectiveValue(
            Individual[] individuals,
            int objectiveIndex) {
        double minValue = individuals[0].getObjective(objectiveIndex);
        for (int i = 1; i < individuals.length; i++) {
            if (minValue > individuals[i].getObjective(objectiveIndex)) {
                minValue = individuals[i].getObjective(objectiveIndex);
            }
        }
        return minValue;
    }

    public static double getMaxObjectiveValue(
            Individual[] individuals,
            int objectiveIndex) {
        double maxValue = individuals[0].getObjective(objectiveIndex);
        for (int i = 1; i < individuals.length; i++) {
            if (maxValue < individuals[i].getObjective(objectiveIndex)) {
                maxValue = individuals[i].getObjective(objectiveIndex);
            }
        }
        return maxValue;
    }

    public static Individual[] getNonDominatedIndividuals(
            Individual[] individuals,
            double epsilon) {
        List<Individual> nonDominatedList = new ArrayList<Individual>();
        outerLoop:
        for (int i = 0; i < individuals.length; i++) {
            if (!individuals[i].isFeasible() && individuals[i].getRank() == 1) {
                // This condition is useful only in the case if all the
                // solutions are infeasible and the solution occupying the
                // first front has other copies. This if statements protects
                // this individual from being removed. (Remember: there cannot
                // be more than one infeasible individual in any front at the
                // same time).
                nonDominatedList.add(individuals[i]);
                continue outerLoop;
            }
            for (int j = 0; j < individuals.length; j++) {
                if (i != j) {
                    if (individuals[j].dominates(individuals[i], epsilon)) {
                        continue outerLoop;
                    }
                }
            }
            nonDominatedList.add(individuals[i]);
        }
        Individual[] nonDominatedArr = new Individual[nonDominatedList.size()];
        nonDominatedList.toArray(nonDominatedArr);
        return nonDominatedArr;
    }

    public static VirtualIndividual[] getNonDominatedIndividuals(
            VirtualIndividual[] individuals,
            double epsilon) {
        List<VirtualIndividual> nonDominatedList
                = new ArrayList<VirtualIndividual>();
        outerLoop:
        for (int ii = 0; ii < individuals.length; ii++) {
            for (int jj = 0; jj < individuals.length; jj++) {
                if (jj != ii) {
                    int indv1IsBetterInSomeObjective = 0;
                    int individual2IsBetterInSomeObjective = 0;
                    for (int i = 0;
                            i < individuals[ii].getObjectivesCount();
                            i++) {
                        if (Mathematics.compare(
                                individuals[ii].getObjective(i) * (1 - epsilon),
                                individuals[jj].getObjective(i)) == -1) {
                            indv1IsBetterInSomeObjective = 1;
                        } else {
                            if (Mathematics.compare(
                                    individuals[ii].getObjective(i),
                                    individuals[jj].getObjective(i)
                                    * (1 - epsilon)) == 1) {
                                individual2IsBetterInSomeObjective = 1;
                            }
                        }
                    }

                    if (indv1IsBetterInSomeObjective == 0
                            && individual2IsBetterInSomeObjective == 1) {
                        continue outerLoop;
                    }

                }
            }
            nonDominatedList.add(individuals[ii]);
        }
        VirtualIndividual[] nonDominatedArr
                = new VirtualIndividual[nonDominatedList.size()];
        nonDominatedList.toArray(nonDominatedArr);
        return nonDominatedArr;
    }

    /**
     * Get all the individuals that can possibly be accommodated in the next
     * population (some of them will be discarded later on) (Note: this method
     * can be modified so that it uses only the second parameters <i>fronts</i>.
     * The only difference is that the returned candidates will be sorted
     * according to domination. This will - most probably - cause NO difference,
     * but this needs more investigation).
     *
     * @param individuals All the individuals (those will-be-included,
     * might-be-included or will-NOT-be-included)
     * @param fronts The same individuals in <i>individuals</i> but each in its
     * fronts
     * @param populationSize
     * @return All individuals that will or might be accommodated in the new
     * front.
     */
    public static Individual[] getCandidates(
            Individual[] individuals,
            List<List<Individual>> fronts,
            int populationSize) {
        int candidatesCount = 0;
        int frontOrder = 1;
        // Calculate the number of candidate solutions
        while (candidatesCount < populationSize) {
            candidatesCount += fronts.get(frontOrder - 1).size();
            frontOrder++;
        }
        // Create an array containing all the candidate solutions
        Individual[] candidates = new Individual[candidatesCount];
        int i = 0;
        for (Individual individual : individuals) {
            if (individual.getRank() < frontOrder) {
                candidates[i] = individual;
                i++;
            }
        }
        // Return candidate solutions array
        return candidates;
    }

    public static VirtualIndividual[] getExtremePoints(
            VirtualIndividual[] previousExtremePoints,
            double[] idealPoint,
            double[] prevIdealPoint,
            VirtualIndividual[] individuals) {
        if (EXTREME_POINTS_DEEP_DEBUG) {
            System.out.println("---------------------------");
            System.out.println("Extreme Points Calculations");
            System.out.println("---------------------------");
        }
        // Calculate unit directions
        double[][] unitDirections
                = new double[idealPoint.length][idealPoint.length];
        for (int i = 0; i < idealPoint.length; i++) {
            for (int j = 0; j < idealPoint.length; j++) {
                if (i == j) {
                    unitDirections[i][j] = 1;
                } else {
                    unitDirections[i][j] = Math.pow(10, -6);
                }
            }
        }
        if (EXTREME_POINTS_DEEP_DEBUG) {
            System.out.format("* Unit Directions Created Successfully:%n");
            for (int i = 0; i < unitDirections.length; i++) {
                System.out.format("(");
                for (int j = 0; j < unitDirections[i].length; j++) {
                    System.out.format("%7.3f", unitDirections[i][j]);
                    if (j != unitDirections[i].length - 1) {
                        System.out.format(",");
                    }
                }
                System.out.format(")%n");
            }
        }
        // If the previous extreme points are not null then let's take them into
        // consideration because we might not get better extreme points from the
        // current population (remember: we are re-considering them because the
        // individual representing the extreme point might have been lost in
        // transition from the previous generation to this one)
        double[] previousMaxValues = new double[idealPoint.length];
        if (previousExtremePoints != null) {
            // Re-Calculate the previous MAX values of the previous extreme
            // points
            for (int i = 0; i < previousExtremePoints.length; i++) {
                // Set the unit direction (unit direction j)
                double[] wDirection = unitDirections[i];
                previousMaxValues[i]
                        = (previousExtremePoints[i].getObjective(0)
                        - idealPoint[0])
                        / wDirection[0];
                for (int k = 1; k < idealPoint.length; k++) {
                    double nextValue
                            = (previousExtremePoints[i].getObjective(k)
                            - idealPoint[k])
                            / wDirection[k];
                    if (nextValue > previousMaxValues[i]) {
                        previousMaxValues[i] = nextValue;
                    }
                }
            }
        }
        if (EXTREME_POINTS_DEEP_DEBUG) {
            System.out.format("* Previous MAX Values:%n");
            System.out.println("[");
            for (int k = 0; k < idealPoint.length; k++) {
                System.out.format("%12.3f", previousMaxValues[k]);
                if (k != idealPoint.length - 1) {
                    System.out.println(",");
                }
            }
            System.out.format("%n]%n");
        }
        // Create an empty 2D array to store the currentIntercepts
        VirtualIndividual[] extremePoints
                = new VirtualIndividual[idealPoint.length];
        // Iterate over all the basic directions (# of directions = # of 
        // objectives)
        for (int i = 0; i < unitDirections.length; i++) {
            if (EXTREME_POINTS_DEEP_DEBUG) {
                System.out.format("* OBJ(%d)%n", i);
            }
            // Set the unit direction (unit direction j)
            double[] wDirection = unitDirections[i];
            if (EXTREME_POINTS_DEEP_DEBUG) {
                System.out.format(
                        "Direction = (%6.2f, %6.2f, %6.2f)%n",
                        wDirection[0],
                        wDirection[1],
                        wDirection[2]);
            }
            // Each slot in the following array stores the largest value of
            // x.obj(k)/w(k) for 0 <= k <= #objectives-1, for some individual x.
            double[] maxArr = new double[individuals.length];
            // Iterate over all the members of the populations
            for (int j = 0; j < individuals.length; j++) {
                if (EXTREME_POINTS_DEEP_DEBUG) {
                    System.out.format("INDV(%d)%n", j);
                    System.out.format(
                            "  max(ind[%d].obj[0]/dir[0], "
                            + "ind[%d].obj[1]/dir[1], "
                            + "ind[%d].obj[2]/dir[2])%n",
                            j, j, j);
                    System.out.format(
                            "= max(%10.2f/%-9.2f, "
                            + "%10.2f/%-9.2f, "
                            + "%10.2f/%-9.2f)%n",
                            individuals[j].getObjective(0) - idealPoint[0],
                            wDirection[0],
                            individuals[j].getObjective(1) - idealPoint[1],
                            wDirection[1],
                            individuals[j].getObjective(2) - idealPoint[2],
                            wDirection[2]);
                    System.out.format("= max(%20.2f, %20.2f, %20.2f)%n",
                            (individuals[j].getObjective(0) - idealPoint[0])
                            / wDirection[0],
                            (individuals[j].getObjective(1) - idealPoint[1])
                            / wDirection[1],
                            (individuals[j].getObjective(2) - idealPoint[2])
                            / wDirection[2]);
                }
                double max
                        = (individuals[j].getObjective(0) - idealPoint[0])
                        / wDirection[0];
                for (int k = 1; k < idealPoint.length; k++) {
                    double nextValue
                            = (individuals[j].getObjective(k) - idealPoint[k])
                            / wDirection[k];
                    if (nextValue > max) {
                        max = nextValue;
                    }
                }
                maxArr[j] = max;
                if (EXTREME_POINTS_DEEP_DEBUG) {
                    System.out.format("= %6.2f%n%n", max);
                }
            }
            // Select the minimum value out of maxArr
            int minIndex = 0;
            for (int j = 1; j < maxArr.length; j++) {
                if (maxArr[j] < maxArr[minIndex]) {
                    minIndex = j;
                }
            }
            if (EXTREME_POINTS_DEEP_DEBUG) {
                System.out.format(
                        "Smallest Max Index = %d (value: %6.2f)%n",
                        minIndex,
                        maxArr[minIndex]);
            }
            if (previousExtremePoints != null
                    && previousMaxValues[i] < maxArr[minIndex]) {
                // This means that the previous extreme point was better than
                // the current extreme point and we should retain the previous
                // extreme point instead of replacing it with a new weaker one.
                // This conditions is possible only in the following two case:
                //  1 - If the reference directions are adjusted, the two
                //      extreme directions will be slightly inclined to the
                //      inside (e.g. in a bi-objective scenario, the f2 extreme
                //      direction (0,1) will be (0.001,1.001) instead).
                //      Consequently, a new point that is further than the
                //      current extreme point with respect to the original
                //      direction (0,1), might now be closer to the adjusted
                //      direction (0.001,1.001). This might cause the true
                //      extreme point to be lost while niching. Thus, this piece
                //      of code makes sure that even if it has been lost as an
                //      individual of the population, it is still retained as
                //      an extreme point.
                //  2 - In order to avoid divisions by Zero, this code replaces
                //      a Zero with a very small value. So, the weight vector
                //      (0,1) is rather represented as (10^-6,1), which can lead
                //      to some inaccurate calculations when it comes to values
                //      which differ by a margin smaller that 10^-6. So, in such
                //      a case, the new point actually represents a better
                //      extreme point, but because its objective value is less
                //      than the objective value of the current extreme point by
                //      a margin smaller than 10^-6, the code ignores it and
                //      maintains the original extreme point. This is however,
                //      a rare case.
                extremePoints[i] = previousExtremePoints[i];
                if (EXTREME_POINTS_DEEP_DEBUG) {
                    System.out.format(">>> Extreme Point remains unchanged%n");
                }
            } else {
                // Now the individual whose index in minIndex in the population
                // is the one representing the extreme factor in the current 
                // directions.
                if (individuals[minIndex] instanceof Individual) {
                    extremePoints[i]
                            = new Individual((Individual) individuals[minIndex]);
                } else {
                    extremePoints[i]
                            = new VirtualIndividual(individuals[minIndex]);
                }
                if (EXTREME_POINTS_DEEP_DEBUG) {
                    System.out.format(
                            ">>> Extreme Point(%d) = Ind(%d)%n",
                            i,
                            minIndex);
                }
            }
        }
        return extremePoints;
    }

    public static Individual[] getActualExtremePoints(
            Individual[] previousExtremePoints,
            double[] idealPoint,
            double[] prevIdealPoint,
            Individual[] individuals,
            OptimizationProblem problem,
            IndividualEvaluator evaluator) {
        if (EXTREME_POINTS_DEEP_DEBUG) {
            System.out.println("---------------------------");
            System.out.println("Extreme Points Calculations");
            System.out.println("---------------------------");
        }
        // Calculate unit directions
        double[][] unitDirections
                = new double[idealPoint.length][idealPoint.length];
        for (int i = 0; i < idealPoint.length; i++) {
            for (int j = 0; j < idealPoint.length; j++) {
                if (i == j) {
                    unitDirections[i][j] = 1;
                } else {
                    unitDirections[i][j] = Math.pow(10, -6);
                }
            }
        }
        if (EXTREME_POINTS_DEEP_DEBUG) {
            System.out.format("* Unit Directions Created Successfully:%n");
            for (int i = 0; i < unitDirections.length; i++) {
                System.out.format("(");
                for (int j = 0; j < unitDirections[i].length; j++) {
                    System.out.format("%7.3f", unitDirections[i][j]);
                    if (j != unitDirections[i].length - 1) {
                        System.out.format(",");
                    }
                }
                System.out.format(")%n");
            }
        }
        // If the previous extreme points are not null then let's take them into
        // consideration because we might not get better extreme points from the
        // current population (remember: we are re-considering them because the
        // individual representing the extreme point might have been lost in
        // transition from the previous generation to this one)
        double[] previousMaxValues = new double[idealPoint.length];
        if (previousExtremePoints != null) {
            // Re-Calculate the previous MAX values of the previous extreme
            // points
            for (int i = 0; i < idealPoint.length; i++) {
                // Set the unit direction (unit direction j)
                double[] wDirection = unitDirections[i];
                previousMaxValues[i]
                        = previousExtremePoints[i].getObjective(0)
                        - idealPoint[0]
                        / wDirection[0];
                for (int k = 1; k < idealPoint.length; k++) {
                    double nextValue
                            = previousExtremePoints[i].getObjective(k)
                            - idealPoint[k]
                            / wDirection[k];
                    if (nextValue > previousMaxValues[i]) {
                        previousMaxValues[i] = nextValue;
                    }
                }
            }
        }
        if (EXTREME_POINTS_DEEP_DEBUG) {
            System.out.format("* Previous MAX Values:%n");
            System.out.println("[");
            for (int k = 0; k < idealPoint.length; k++) {
                System.out.format("%12.3f", previousMaxValues[k]);
                if (k != idealPoint.length - 1) {
                    System.out.println(",");
                }
            }
            System.out.format("%n]%n");
        }
        // Create an empty 2D array to store the currentIntercepts
        Individual[] extremePoints = new Individual[idealPoint.length];
        // Iterate over all the basic directions
        // (# of directions = # of objectives)
        for (int i = 0; i < idealPoint.length; i++) {
            if (EXTREME_POINTS_DEEP_DEBUG) {
                System.out.format("* OBJ(%d)%n", i);
            }
            // Set the unit direction (unit direction j)
            double[] wDirection = unitDirections[i];
            if (EXTREME_POINTS_DEEP_DEBUG) {
                System.out.format(
                        "Direction = (%6.2f, %6.2f, %6.2f)%n",
                        wDirection[0],
                        wDirection[1],
                        wDirection[2]);
            }
            // Each slot in the following array stores the largest value of
            // x.obj(k)/w(k) for 0 <= k <= #objectives-1, for some individual x.
            double[] maxArr = new double[individuals.length];
            // Iterate over all the members of the populations
            for (int j = 0; j < individuals.length; j++) {
                if (EXTREME_POINTS_DEEP_DEBUG) {
                    System.out.format("INDV(%d)%n", j);
                    System.out.format(
                            "  max(ind[%d].obj[0]/dir[0], "
                            + "ind[%d].obj[1]/dir[1], "
                            + "ind[%d].obj[2]/dir[2])%n",
                            j, j, j);
                    System.out.format(
                            "= max(%10.2f/%-9.2f, "
                            + "%10.2f/%-9.2f, "
                            + "%10.2f/%-9.2f)%n",
                            individuals[j].getObjective(0) - idealPoint[0],
                            wDirection[0],
                            individuals[j].getObjective(1) - idealPoint[1],
                            wDirection[1],
                            individuals[j].getObjective(2) - idealPoint[2],
                            wDirection[2]);
                    System.out.format("= max(%20.2f, %20.2f, %20.2f)%n",
                            (individuals[j].getObjective(0) - idealPoint[0])
                            / wDirection[0],
                            (individuals[j].getObjective(1) - idealPoint[1])
                            / wDirection[1],
                            (individuals[j].getObjective(2) - idealPoint[2])
                            / wDirection[2]);
                }
                double max
                        = (individuals[j].getObjective(0) - idealPoint[0])
                        / wDirection[0];
                for (int k = 1; k < idealPoint.length; k++) {
                    double nextValue
                            = (individuals[j].getObjective(k) - idealPoint[k])
                            / wDirection[k];
                    if (nextValue > max) {
                        max = nextValue;
                    }
                }
                maxArr[j] = max;
                if (EXTREME_POINTS_DEEP_DEBUG) {
                    System.out.format("= %6.2f%n%n", max);
                }
            }
            // Select the minimum value out of maxArr
            int minIndex = 0;
            for (int j = 1; j < maxArr.length; j++) {
                if (maxArr[j] < maxArr[minIndex]) {
                    minIndex = j;
                }
            }
            if (EXTREME_POINTS_DEEP_DEBUG) {
                System.out.format(
                        "Smallest Max Index = %d (value: %6.2f)%n",
                        minIndex,
                        maxArr[minIndex]);
            }
            if (previousExtremePoints != null
                    && previousMaxValues[i] < maxArr[minIndex]) {
                // This means that the previous extreme point was better than
                // the current extreme point and we should retain the previous
                // extreme point instead of replacing it with a new weaker one.
                extremePoints[i] = previousExtremePoints[i];
                if (EXTREME_POINTS_DEEP_DEBUG) {
                    System.out.format(">>> Extreme Point remains unchanged%n");
                }
            } else {
                // Now the individual whose index in minIndex in the population
                // is the one representing the extreme factor in the current
                // directions.
                extremePoints[i] = new Individual(
                        individuals[minIndex]);
                if (EXTREME_POINTS_DEEP_DEBUG) {
                    System.out.format(
                            ">>> Extreme Point(%d) = Ind(%d)%n",
                            i,
                            minIndex);
                }
            }
        }
        return extremePoints;
    }

    public static Individual[] selectOnlyOneIndividualForEachDirection(
            Individual[] individuals,
            List<ReferenceDirection> referenceDirectionsList) {
        // Create an empty list to which the selected individuals will be copied
        List<Individual> selectedIndividuals = new ArrayList<Individual>();
        // Create an empty list to which directions with no attached individuals
        // will be kept
        List<ReferenceDirection> isolatedDirsList
                = new ArrayList<ReferenceDirection>();
        // Create a new list of individuals
        List<Individual> feasibleIndividualsList = new ArrayList<Individual>();
        for (Individual individual : individuals) {
            if (individual.isFeasible()) {
                feasibleIndividualsList.add(individual);
            }
        }
        // For those directions that have points attached to them, select the
        // closest non-blacklisted individual then remove it from further 
        // consideration.
        for (ReferenceDirection dir : referenceDirectionsList) {
            // Find the individual with the shortest perpendicular distance
            // to the directory under investigation
            Individual minDistIndividual = null;
            int i;
            for (i = 0; i < feasibleIndividualsList.size(); i++) {
                if (feasibleIndividualsList.get(i).getReferenceDirection()
                        .equals(dir)) {
                    minDistIndividual = feasibleIndividualsList.get(i);
                    break;
                }
            }
            if (minDistIndividual == null) {
                isolatedDirsList.add(dir);
            } else {
                for (; i < feasibleIndividualsList.size(); i++) {
                    if (feasibleIndividualsList.get(i).getReferenceDirection()
                            .equals(dir)
                            && feasibleIndividualsList.get(i)
                                    .getPerpendicularDistance()
                            < minDistIndividual.getPerpendicularDistance()) {
                        minDistIndividual = feasibleIndividualsList.get(i);
                    }
                }
                selectedIndividuals.add(minDistIndividual);
                feasibleIndividualsList.remove(minDistIndividual);
            }
        }
        // Do the same for directions having no individuals attached to them
        for (int i = 0; i < referenceDirectionsList.size()
                && !feasibleIndividualsList.isEmpty(); i++) {
            // Skip non-isolated individuals
            if (!isolatedDirsList.contains(referenceDirectionsList.get(i))) {
                continue;
            }
            // Work on isolated individuals
            // Select the individual closest to the isolated direction
            Individual minDistIndividual = feasibleIndividualsList.get(0);
            for (int j = 1; j < feasibleIndividualsList.size(); j++) {
                if (feasibleIndividualsList.get(j).distancesFromDirs[i]
                        < minDistIndividual.distancesFromDirs[i]) {
                    minDistIndividual = feasibleIndividualsList.get(j);
                }
            }
            // Add the closest individual to the selected collection and
            // discard it from further consideration
            selectedIndividuals.add(minDistIndividual);
            feasibleIndividualsList.remove(minDistIndividual);
        }
        // Copy the list to an array
        Individual[] selectedIndividualsArr
                = new Individual[selectedIndividuals.size()];
        selectedIndividuals.toArray(selectedIndividualsArr);
        return selectedIndividualsArr;
    }

    /**
     * An ideal point of a group of individuals is the vector containing the
     * minimum objective values of all objective values found among this group
     * of individuals.
     *
     * @param individuals for which the ideal point is to be calculated
     * @return
     */
    public static double[] getIdealPoint(Individual[] individuals) {
        if (individuals.length == 0) {
            throw new UnsupportedOperationException(
                    "At least one individual is "
                    + "required to calculate the ideal point.");
        }
        int objectivesCount = individuals[0].objectiveFunction.length;
        double[] ideal = new double[objectivesCount];
        for (int i = 0; i < ideal.length; i++) {
            ideal[i] = MAX_DOUBLE_VALUE;
        }
        for (int i = 0; i < objectivesCount; i++) {
            for (Individual individual : individuals) {
                if (Mathematics.compare(
                        individual.getTotalConstraintViolation(), 0) == 0) {
                    if (individual.getObjective(i) < ideal[i]) {
                        ideal[i] = individual.getObjective(i);
                    }
                }
            }
        }
        return ideal;
    }

    /**
     * This method doe not check infeasibility. It treats all An ideal point of
     * a group of individuals is the vector containing the minimum objective
     * values of all objective values found among this group of individuals.
     *
     * @param individuals Virtual individuals for which the ideal point is to be
     * calculated
     * @return
     */
    public static double[] getIdealPoint(VirtualIndividual[] individuals) {
        if (individuals.length == 0) {
            throw new UnsupportedOperationException(
                    "At least one individual is "
                    + "required to calculate the ideal point.");
        }
        int objectivesCount = individuals[0].objectiveFunction.length;
        double[] ideal = new double[objectivesCount];
        for (int i = 0; i < ideal.length; i++) {
            ideal[i] = MAX_DOUBLE_VALUE;
        }
        for (int i = 0; i < objectivesCount; i++) {
            for (VirtualIndividual individual : individuals) {
                if (individual.getObjective(i) < ideal[i]) {
                    ideal[i] = individual.getObjective(i);
                }
            }
        }
        return ideal;
    }

    public static void fixVariableLimits(
            OptimizationProblem optimizationProblem, double[] x) {
        List<RealVariableSpecs> specs
                = new ArrayList<RealVariableSpecs>();
        for (Variable variableSpecs
                : optimizationProblem.variablesSpecs) {
            if (variableSpecs instanceof RealVariableSpecs) {
                specs.add((RealVariableSpecs) variableSpecs);
            }
        }
        for (int i = 0; i < x.length; i++) {
            if (x[i] < specs.get(i).getMinValue()) {
                x[i] = specs.get(i).getMinValue();
            } else if (x[i] > specs.get(i).getMaxValue()) {
                x[i] = specs.get(i).getMaxValue();
            }
        }
    }

    public static double[] pullPointBack(double[] point, double factor) {
        if (factor >= 0) {
            double[] pulledPoint = new double[point.length];
            for (int i = 0; i < point.length; i++) {
                pulledPoint[i] = point[i] - Math.abs(factor * point[i]);
            }
            return pulledPoint;
        } else {
            throw new IllegalArgumentException(
                    "Pull factor must be greater than or equal to Zero.");
        }
    }
}
