/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package refdirs;

import utils.InputOutput;
import utils.Mathematics;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * A Reference directions factory is used to create the set of equally
 * distributed reference directions used in algorithms like MOEA/D and NSGA-III.
 *
 * @author Haitham Seada
 */
public class ReferenceDirectionsFactory {

    private int level = 0;
    private double sum = 0;
    protected int objectivesCount; // (M) in the paper
    private double totalSum; // Usually this value is set to one. However with
    // higher dimentions the value one generates only edge points.
    private Stack<Double> stack;
    private List<ReferenceDirection> directionsList;

    public ReferenceDirectionsFactory(int objectivesCount, double totalSum) {
        this.stack = new Stack<Double>();
        this.objectivesCount = objectivesCount;
        this.totalSum = totalSum;
    }

    public ReferenceDirectionsFactory(int objectivesCount) {
        this(objectivesCount, 1);
    }

    /**
     * Only for testing purposes
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int objCount = 3;
        int step = 20;
        ReferenceDirectionsFactory referenceDirectionsFactory
                = new ReferenceDirectionsFactory(3, 1.0);
        List<ReferenceDirection> directionsList
                = referenceDirectionsFactory.generateDirections(step);
        double[] shiftingVector = {0.2, 0.2, 0.6};
        directionsList = referenceDirectionsFactory.shift(
                directionsList,
                shiftingVector);
        System.out.format(
                "Total Number of Generated Directions = %-10d%n",
                directionsList.size());
        for (ReferenceDirection dir : directionsList) {
            System.out.println(dir);
        }
        InputOutput.printDirectionMatlabCode(
                objCount,
                directionsList,
                new PrintWriter(System.out));
    }

    /**
     * Generate all valid permutations (directions whose values sum up to one).
     */
    public List<ReferenceDirection> generateDirections(int divisions) {
        directionsList = new ArrayList<ReferenceDirection>();
        if (divisions >= 0) {
            if (objectivesCount > 1) {
                // Multi- & Many-objective problems
                for (double i = 0;
                        Mathematics.compare(i, totalSum) != 1;
                        i += totalSum / divisions) {
                    generateDirectionsRecursively(i, divisions);
                    level = 0;
                }
            } else if (objectivesCount == 1) {
                // Single objective problems
                double[] directionArr = {totalSum};
                directionsList.add(new ReferenceDirection(directionArr));
            } else {
                // If the number of objectives is Zero or negative
                // (which is impossible)
                throw new UnsupportedOperationException(
                        "Dimensions (objectives) must be at least one");
            }
        }
        return directionsList;
    }

    /**
     * Recursively generate all valid permutations (directions whose values sum
     * up to one).
     *
     * @param coordinateValue
     */
    private void generateDirectionsRecursively(
            double coordinateValue,
            int divisions) {
        stack.push(coordinateValue);
        sum += coordinateValue;
        if (sum == totalSum) {
            // Create the direction
            generateDirectionFillWithZeroes();
            stack.pop();
            sum -= coordinateValue;
            level--;
        } else if (level == objectivesCount - 2) {
            // Create the direction
            generateDirectionFillLastPosition();
            stack.pop();
            sum -= coordinateValue;
            level--;
        } else {
            for (double i = 0;
                    Mathematics.compare(i, totalSum - sum, 1e-3) != 1;
                    i += totalSum / divisions) {
                level++;
                generateDirectionsRecursively(i, divisions);
            }
            stack.pop();
            sum -= coordinateValue;
            level--;
        }
    }

    /**
     * Utility method used to generate those directions whose remaining values
     * are Zeroes.
     */
    private void generateDirectionFillWithZeroes() {
        double[] directionArr = new double[objectivesCount];
        for (int i = 0; i < stack.size(); i++) {
            directionArr[i] = stack.elementAt(i);
        }
        for (int i = level + 1; i < objectivesCount; i++) {
            directionArr[i] = 0.0;
        }
        directionsList.add(new ReferenceDirection(directionArr));
    }

    /**
     * Utility method used to generate directions filling the last position with
     * the appropriate value that makes the whole values of the direction sum up
     * to one.
     */
    private void generateDirectionFillLastPosition() {
        double[] directionArr = new double[objectivesCount];
        for (int i = 0; i < stack.size(); i++) {
            directionArr[i] = stack.elementAt(i);
        }
        directionArr[objectivesCount - 1] = totalSum - sum;
        directionsList.add(new ReferenceDirection(directionArr));
    }

    public List<ReferenceDirection> shift(
            List<ReferenceDirection> dirs,
            double[] shiftingVector) {
        List<ReferenceDirection> shiftedList
                = new ArrayList<>();
        outerLoop:
        for (int i = 0; i < dirs.size(); i++) {
            double[] shiftedVector = new double[objectivesCount];
            for (int j = 0; j < shiftedVector.length; j++) {
                shiftedVector[j] = dirs.get(i).direction[j] - 1.0
                        / objectivesCount + shiftingVector[j];
                if (shiftedVector[j] > 1 || shiftedVector[j] < 0) {
                    continue outerLoop;
                }
            }
            shiftedList.add(new ReferenceDirection(shiftedVector));
        }
        return shiftedList;
    }
}
