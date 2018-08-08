/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emo;

import java.io.Serializable;
import parsing.IndividualEvaluator;

/**
 * Represents a virtual object in the sense that it has objective values but it
 * does not have the actual design variables values corresponding to these
 * objective values. One use case of this class is to create aspirations points
 * e.g. Pareto points in the objective space.
 *
 * @author Haitham Seada
 */
public class VirtualIndividual implements Serializable {

    protected double[] objectiveFunction;
    // The following booleans must be set to false after any operation
    // that might introduce modification to any of the variables
    // (e.g. mutation or crossover).
    public boolean validObjectiveFunctionsValues = true;

    public VirtualIndividual(int objectivesCount) {
        objectiveFunction = new double[objectivesCount];
    }

    public VirtualIndividual(VirtualIndividual individual) {
        this(individual.objectiveFunction.length);
        System.arraycopy(
                individual.objectiveFunction,
                0,
                this.objectiveFunction,
                0,
                individual.objectiveFunction.length);
        this.validObjectiveFunctionsValues
                = individual.validObjectiveFunctionsValues;
    }

    public double getObjective(int objectiveIndex) {
        if (validObjectiveFunctionsValues) {
            return objectiveFunction[objectiveIndex];
        } else {
            throw new InvalidObjectiveValue();
        }
    }

    public int getObjectivesCount() {
        return objectiveFunction.length;
    }

    public void setObjective(int objectiveIndex, double objectiveValue) {
        objectiveFunction[objectiveIndex] = objectiveValue;
    }
}
