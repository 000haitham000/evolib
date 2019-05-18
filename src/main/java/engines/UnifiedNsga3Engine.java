/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package engines;

import distancemetrics.DistanceMetric;
import utils.InputOutput;
import utils.Mathematics;
import utils.RandomNumberGenerator;
import emo.Individual;
import emo.IndividualsSet;
import emo.OptimizationProblem;
import java.io.BufferedReader;
import refdirs.ReferenceDirection;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import parsing.IndividualEvaluator;

/**
 * This class represents the Unified NSGA-III algorithm. A detailed description
 * of the algorithm can be found at: Seada, Haitham, and Kalyanmoy Deb. "A
 * unified evolutionary optimization procedure for single, multiple, and many
 * objectives." IEEE Transactions on Evolutionary Computation 20.3 (2016):
 * 358-369.
 *
 * @author Haitham Seada
 */
public class UnifiedNsga3Engine extends NSGA3Engine {

    public UnifiedNsga3Engine(
            OptimizationProblem optimizationProblem,
            IndividualEvaluator individualEvaluator,
            DistanceMetric distanceMetric) {
        super(optimizationProblem, individualEvaluator, distanceMetric);
    }

    public UnifiedNsga3Engine(
            OptimizationProblem optimizationProblem,
            IndividualEvaluator individualEvaluator, int[] divisions, DistanceMetric distanceMetric) {
        super(optimizationProblem, individualEvaluator, divisions, distanceMetric);
    }

    public UnifiedNsga3Engine(
            OptimizationProblem optimizationProblem,
            IndividualEvaluator individualEvaluator,
            String directionsFilePath,
            DistanceMetric distanceMetric)
            throws
            FileNotFoundException,
            IOException {
        super(optimizationProblem, individualEvaluator, distanceMetric);
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
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        InputOutput.displayReferenceDirections(
                "Reference Directions",
                referenceDirectionsList);
    }

    public static int totalSelectionsCount = 0;
    public static int bothFeasibleCount = 0;

    @Override
    protected Individual tournamentSelect(IndividualsSet subset) {
        totalSelectionsCount++;
        Individual individual1 = subset.getIndividual1();
        Individual individual2 = subset.getIndividual2();
        // If only one of the solutions is infeasible, return the feasible
        // solution
        if (optimizationProblem.constraints != null
                && optimizationProblem.constraints.length != 0
                && (individual1.isFeasible() ^ individual2.isFeasible())) {
            // If the problem is constrained and one of the individuals
            // under investigation is feasible while the other is infeasible,
            // return the feasible one (which is normally the dominating
            // individual).
            if (individual1.isFeasible()) {
                return individual1;
            } else {
                return individual2;
            }
        } else if (!individual1.isFeasible() && !individual2.isFeasible()) {
            // If both the two solutions are infeasible, return the less
            // violating.
            if (Mathematics.compare(
                    individual1.getTotalConstraintViolation(),
                    individual2.getTotalConstraintViolation()) == 1) {
                // individual1 is less violating (remember: the more negative
                // the value, the more the violation)
                return individual1;
            } else {
                // individual2 is less violating
                return individual2;
            }
        } //else if (optimizationProblem.objectives.length == 1) {
        // If we have only one objective and both solutions are feasible,
        // return the better in terms of objective value (i.e. the
        // dominating solution). If the two ranks are equal this means that
        // the two individuals are identical so return any of them.
        // (Remember: in the case of single objective, one idividual must
        // dominate the other unless both are identical to each other. This
        // is the only case where they will have the same rank)
        //if (individual1.dominates(individual2)) {
        //return individual1;
        //} else {
        //return individual2;
        //}
        //}
        else if (currentGenerationIndex != 0
                && individual1.getReferenceDirection().equals(
                        individual2.getReferenceDirection())) {
            bothFeasibleCount++;
            // If both the two solutions are feasible. You have the
            // following two options:
            // If the two individuals belong to the same reference direction,
            // return the one with lower rank.
            if (individual1.getRank() < individual2.getRank()) {
                return individual1;
            } else if (individual2.getRank() < individual1.getRank()) {
                return individual2;
            } else {
                // If they both belong to the same rank return the one
                // closest to the reference direction.
                if (Mathematics.compare(
                        individual1.getPerpendicularDistance(),
                        individual2.getPerpendicularDistance()) == -1) {
                    return individual1;
                } else {
                    return individual2;
                }
            }
        } else {
            // If the two individuals are associated with two different 
            // reference directions, then return one of them randomly
            if (RandomNumberGenerator.next() <= 0.5) {
                return individual1;
            } else {
                return individual2;
            }
        }
    }

    @Override
    public String getAlgorithmName() {
        return "unified_nsga3";
    }
}
