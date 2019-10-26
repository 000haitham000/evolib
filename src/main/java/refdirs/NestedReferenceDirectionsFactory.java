/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package refdirs;

import utils.InputOutput;
import utils.Mathematics;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This factory is used to created nested reference directions. Nesting is
 * useful in higher dimensions (e.g. 5+ objectives).
 *
 * @author Haitham Seada
 */
public class NestedReferenceDirectionsFactory
        extends
        ReferenceDirectionsFactory {

    public final boolean AUTO_ADD_CENTRAL_REFDIR = false;

    public NestedReferenceDirectionsFactory(int objectivesCount) {
        super(objectivesCount);
    }

    /**
     *
     * @param divisions the number of steps in each layer of the nested layers mode
     * @return a list of reference directions
     */
    public List generateDirections(int[] divisions) {
        // Create a list to accumulate all final reference directions
        List<ReferenceDirection> directions
                = new ArrayList<>();
        // Loof creating a circle of directions at each iteration
        // (starting from the outer circle and ending at the inner circle)
        for (int i = 0; i < divisions.length; i++) {
            // (sum) is the value to which the values of any direction in the
            // current level must sum up to.
            double sum = 1 - (1.0 * i / divisions.length);
            // Create a generator for the current level with the corresponding
            // number of divisions and the corresponding sum.
            ReferenceDirectionsFactory directionsGenerator
                    = new ReferenceDirectionsFactory(objectivesCount, sum);
            // Generate the directions (un-augmented)
            List<ReferenceDirection> levelDirections
                    = directionsGenerator.generateDirections(divisions[i]);
            // Create an augementation vector for the current level.
            // (The augmentation vector is a vector of size equal to the
            // number of objectives i.e. equal to the size of any reference
            // direction. All the values of this vector are the same. The
            // augmentation vector is added to the un-augmented directions
            // of the current level in order to make their values sum up to 1.
            // Thus producing the final directions (augmented directions).
            double[] levelAugmentationVector = getAugmentationVector(
                    objectivesCount,
                    (1.0 - sum) / objectivesCount);
            // Augment the un-augmented directions of the current level.
            for (ReferenceDirection dir : levelDirections) {
                augment(dir, levelAugmentationVector);
            }
            // Add the final (now augmented) directions to the final list.
            for (ReferenceDirection dir : levelDirections) {
                directions.add(dir);
            }
        }
        // Remove repeated directions.
        // Important Note-1: If you have repeated directions this means that you
        // are using divisions that are greater than or equal to the number of
        // objectives in more than one level, and that the directions are more
        // dense in some places than the others. In this case, it could have
        // been better to use only one level of directions where (p) > (M), or
        // to use more levels and keep (p) < (M) while generating the 
        // directions of each level.
        // Important Note-2: Using hashset to remove duplicates is easy however
        // it destroys the order of the elements in the list. However, elements
        // order will NOT harm our purpose (until the time of writing this
        // comment).
        Set<ReferenceDirection> set = new HashSet<>();
        set.addAll(directions);
        directions.clear();
        directions.addAll(set);
        // If the final set of directions does not include a directions whose
        // all coordinates have the same value, then this one should be
        // added to the directions list.
        boolean centralDirFound = false;
        double[] centralVector = getAugmentationVector(
                objectivesCount,
                1.0 / objectivesCount);
        ReferenceDirection centralDir = new ReferenceDirection(centralVector);
        for (ReferenceDirection dir : directions) {
            if (dir.equals(centralDir)) {
                centralDirFound = true;
                break;
            }
        }
        if (!centralDirFound && AUTO_ADD_CENTRAL_REFDIR) {
            directions.add(centralDir);
        }
        // Return the final list of directions directions
        return directions;
    }

    private double[] getAugmentationVector(int size, double value) {
        double[] v = new double[size];
        for (int i = 0; i < v.length; i++) {
            v[i] = value;
        }
        return v;
    }

    protected void augment(
            ReferenceDirection dir,
            double[] augmentationVector) {
        for (int i = 0; i < dir.direction.length; i++) {
            dir.direction[i] += augmentationVector[i];
        }
    }
}
