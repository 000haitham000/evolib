/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package emo;

import refdirs.InvalidReferenceDirectionValue;
import refdirs.ReferenceDirection;
import utils.Mathematics;
import utils.RandomNumberGenerator;
import engines.AbstractGeneticEngine;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import parsing.IndividualEvaluator;

/**
 * This is a fat individual class that supports multiple types of individuals.
 * It supports NSGA-II style individuals which are compared according to the
 * NSGA-II way of comparing its solutions (generally feasibility then domination
 * then crowding distance). It also supports NSGA-III type individuals which are
 * mostly attached to reference directions and are compared differently.
 *
 * TODO: Try to divided this fat class into a hierarchical group of classes each
 * supporting one type of individuals.
 *
 * @author Haitham Seada
 */
public class Individual extends VirtualIndividual
        implements Comparable<Individual> {

    private final OptimizationProblem optimizationProblem;
    private final IndividualEvaluator individualEvaluator;
    private int rank;
    public double[] real;
    public BinaryVariable[] binary;
    public CustomVariable[] custom;
    protected double[] constraintViolation;
    private ReferenceDirection referenceDirection;
    private double perpendicularDistance;
    private int dominatedByCount;
    List<Individual> dominatedList = new ArrayList<>();
    public boolean translated = false;
    // The following booleans must be set to false after any operation 
    // that might introduce modification to any of the variables
    // (e.g. mutation or crossover).
    public boolean validConstraintsViolationValues,
            validRankValue,
            validReferenceDirection;
    private boolean nsga2crowdingDistanceAlreadySet = false;
    private double nsga2crowdingDistance;
    private static int nsga2comparisonObjectiveIndex = -1;
    public double[] distancesFromDirs;

    /**
     * @return the nsga2comparisonObjectiveIndex
     */
    public static int getNsga2comparisonObjectiveIndex() {
        return nsga2comparisonObjectiveIndex;
    }

    /**
     * @param aNsga2comparisonObjectiveIndex the nsga2comparisonObjectiveIndex
     * to set
     */
    public static void setNsga2comparisonObjectiveIndex(
            int aNsga2comparisonObjectiveIndex) {
        nsga2comparisonObjectiveIndex = aNsga2comparisonObjectiveIndex;
    }

    public Individual(
            OptimizationProblem problem,
            IndividualEvaluator individualEvaluator) {
        this(problem, individualEvaluator, null);
    }

    public Individual(
            OptimizationProblem problem,
            IndividualEvaluator individualEvaluator,
            double[] realDesignVariables) {
        super(problem.objectives.length);
        this.optimizationProblem = problem;
        this.individualEvaluator = individualEvaluator;
        // Initialize real & binary variables
        // Create a list for each type of variables (real vs. binary)
        List<Double> realVariablesList = new ArrayList<>();
        List<BinaryVariable> binaryVariablesList = new ArrayList<>();
        List<CustomVariable> customVariablesList = new ArrayList<>();
        // The following counter is used only if the call specified design variables values
        int realCounter = 0;
        // Separate real from binary, each to its designated list.
        for (int i = 0; i < problem.getVariablesSpecs().length; i++) {
            if (problem.getVariablesSpecs()[i] instanceof BinaryVariableSpecs) {
                binaryVariablesList.add(
                        new BinaryVariable((BinaryVariableSpecs) problem.getVariablesSpecs()[i]));
            } else if (problem.getVariablesSpecs()[i] instanceof RealVariableSpecs) {
                if (realDesignVariables == null) {
                    // This part is responsible for randomly generating decision
                    // variables values (e.g. for initial random population)
                    double rndreal = RandomNumberGenerator.next(
                            ((NumericVariable) problem.getVariablesSpecs()[i]).getMinValue(),
                            ((NumericVariable) problem.getVariablesSpecs()[i]).getMaxValue());
                    realVariablesList.add(rndreal);
                } else {
                    // This part is responsible for creating and individual with
                    // the specifies decision variables values (e.g. generating 
                    // an object for an already known solution, like the 
                    // returned solution of the MATLAB ASF minimization script)
                    realVariablesList.add(realDesignVariables[realCounter]);
                    realCounter++;
                }
            } else if (problem.getVariablesSpecs()[i] instanceof CustomVariableSpecs) {
                customVariablesList.add(new CustomVariable(
                        (CustomVariableSpecs) problem.getVariablesSpecs()[i]));
            }
        }
        // Copy real variables from their temporary list to their original array
        real = new double[realVariablesList.size()];
        for (int i = 0; i < realVariablesList.size(); i++) {
            real[i] = realVariablesList.get(i);
        }
        // Copy binary variables from their temporary list to their original array
        binary = new BinaryVariable[binaryVariablesList.size()];
        binaryVariablesList.toArray(binary);
        // Copy the custom variables list
        custom = new CustomVariable[customVariablesList.size()];
        customVariablesList.toArray(custom);
        // Initialize objectives array
        objectiveFunction = new double[problem.objectives.length];
        // Initialize constraints violations array
        if (problem.constraints != null) {
            constraintViolation = new double[problem.constraints.length];
        }
        // Update objectives and constraints values
        individualEvaluator.updateIndividualObjectivesAndConstraints(problem, this);
    }

    public Individual(Individual individual) {
        super(individual.optimizationProblem.objectives.length);
        this.optimizationProblem = individual.optimizationProblem;
        this.individualEvaluator = individual.individualEvaluator;
        // Copy reals array
        this.real = new double[individual.real.length];
        System.arraycopy(individual.real, 0, this.real, 0, individual.real.length);
        // Copy binaries array
        this.binary = new BinaryVariable[individual.binary.length];
        for (int i = 0; i < this.binary.length; i++) {
            this.binary[i] = new BinaryVariable(individual.binary[i].getSpecs());
            System.arraycopy(individual.binary[i].representation,
                    0,
                    this.binary[i].representation,
                    0,
                    individual.binary[i].representation.length);
        }
        // Copy customs array
        this.custom = new CustomVariable[individual.custom.length];
        for (int i = 0; i < this.custom.length; i++) {
            this.custom[i] = new CustomVariable(individual.custom[i].getSpecs());
        }
        // Copy objectives array
        this.objectiveFunction = new double[individual.objectiveFunction.length];
        System.arraycopy(individual.objectiveFunction,
                0,
                this.objectiveFunction,
                0,
                individual.objectiveFunction.length);
        // Copy constraints violations
        if (individual.constraintViolation != null) {
            this.constraintViolation = new double[individual.constraintViolation.length];
            System.arraycopy(individual.constraintViolation,
                    0,
                    this.constraintViolation,
                    0,
                    this.constraintViolation.length);
        }
        // Copy rank
        this.rank = individual.rank;

        // Copy reference direction
        this.referenceDirection = individual.referenceDirection;

        // Copy some state flags
        this.translated = individual.translated;
        this.validReferenceDirection = individual.validReferenceDirection;
        this.validRankValue = individual.validRankValue;

        // Set the state of the new copy of the individual
        this.validConstraintsViolationValues = true;
        this.validObjectiveFunctionsValues = true;
        this.nsga2crowdingDistanceAlreadySet = false;
    }

    public double getPerpendicularDistance() {
        return perpendicularDistance;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Arrays.hashCode(this.real);
        hash = 89 * hash + Arrays.deepHashCode(this.binary);
        hash = 89 * hash + Arrays.deepHashCode(this.custom);
        return hash;
    }
    
    public void setPerpendicularDistance(double perpendicularDistance) {
        this.perpendicularDistance = perpendicularDistance;
    }

    /**
     * Return the total constraints violation in a single individual. Since,
     * only negative values are considered violations (negative values means
     * that the individual lies in the infeasible region), therefore, only
     * negative constraint violation values are added. Actually, positive
     * constraint violation values are meaningless (all positive values are
     * equivalent. They all mean that the individual is feasible with respect to
     * the constraint under investigation).
     *
     * @return
     */
    public double getTotalConstraintViolation() {
        if (optimizationProblem.constraints == null) {
            return 0;
        }
        double totalConstraintViolation = 0;
        for (int i = 0; i < constraintViolation.length; i++) {
            if (constraintViolation[i] < 0) {
                totalConstraintViolation += constraintViolation[i];
            }
        }
        return totalConstraintViolation;
    }

    public boolean isFeasible() {
        if (Mathematics.compare(
                this.getTotalConstraintViolation(),
                0,
                Mathematics.EPSILON) == 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getVariableSpace() {
        String result = "Variables:";
        int binaryCounter = 0;
        int realCounter = 0;
        int customCounter = 0;
        for (int i = 0; i < optimizationProblem.getVariablesSpecs().length; i++) {
            if (optimizationProblem.getVariablesSpecs()[i] instanceof BinaryVariableSpecs) {
                result += String.format(" {%-5s= %-7.3f (B)}",
                        optimizationProblem.getVariablesSpecs()[i].getName(),
                        binary[binaryCounter].getDecimalValue());
                binaryCounter++;
            } else if (optimizationProblem.getVariablesSpecs()[i] instanceof RealVariableSpecs) {
                result += String.format(" {%-5s= %-7.3f (R)}",
                        optimizationProblem.getVariablesSpecs()[i].getName(),
                        real[realCounter]);
                realCounter++;
            } else if (optimizationProblem.getVariablesSpecs()[i] instanceof CustomVariableSpecs) {
                result += String.format(" {%-5s= %s (C)}",
                        optimizationProblem.getVariablesSpecs()[i].getName(),
                        custom[customCounter].toString());
                binaryCounter++;
            }
        }
        return result;
    }

    public String getObjectiveSpace() {
        String result = "Objectives:";
        for (int i = 0; i < optimizationProblem.objectives.length; i++) {
            result += String.format(" {%-5s= %-7.3f}",
                    String.format("Obj(%02d)", i),
                    this.getObjective(i));
        }
        result += String.format(
                " - CV = %05.2f%n",
                this.getTotalConstraintViolation());
        return result;
    }

    public String getShortVariableSpace() {
        String result = "Variables {";
        int binaryCounter = 0;
        int realCounter = 0;
        int customCounter = 0;
        for (int i = 0; i < optimizationProblem.getVariablesSpecs().length; i++) {
            if (optimizationProblem.getVariablesSpecs()[i] instanceof BinaryVariableSpecs) {
                result += String.format("%7.2f(B)",
                        binary[binaryCounter].getDecimalValue());
                binaryCounter++;
            } else if (optimizationProblem.getVariablesSpecs()[i] instanceof RealVariableSpecs) {
                result += String.format("%7.2f(R)",
                        real[realCounter]);
                realCounter++;
            } else if (optimizationProblem.getVariablesSpecs()[i] instanceof CustomVariableSpecs) {
                result += String.format("%s(C)",
                        custom[customCounter]);
                customCounter++;
            }
        }
        result += "}";
        return result;
    }

    @Override
    public String toString() {
        // Variables (binary and real with their values)
        String result = "Variables:";
        int binaryCounter = 0;
        int realCounter = 0;
        int customCounter = 0;
        for (int i = 0; i < optimizationProblem.getVariablesSpecs().length; i++) {
            if (optimizationProblem.getVariablesSpecs()[i] instanceof BinaryVariableSpecs) {
                result += String.format(" {%-5s= %-10.5f (B)}",
                        optimizationProblem.getVariablesSpecs()[i].getName(),
                        binary[binaryCounter].getDecimalValue());
                binaryCounter++;
            } else if (optimizationProblem.getVariablesSpecs()[i] instanceof RealVariableSpecs) {
                result += String.format(" %-15.10f", real[realCounter]);
                /*
                 result += String.format(" {%-5s= %-7.3f (R)}",
                 optimizationProblem.variablesSpecs[i].getName(),
                 real[realCounter]);
                 */
                realCounter++;
            } else if (optimizationProblem.getVariablesSpecs()[i] instanceof CustomVariableSpecs) {
                result += String.format(" %s", custom[customCounter]);
                customCounter++;
            }
        }
        // Objective Functions Values
        result += " - Objectives:";
        if (validObjectiveFunctionsValues) {
            for (int i = 0; i < this.objectiveFunction.length; i++) {
                result += String.format(
                        " {Obj(%d) = %-15.10f}",
                        i + 1,
                        objectiveFunction[i]);
            }
        } else {
            result += "Invalid (outdated)";
        }
        // Constraints Violations Values
        result += " - Constraints Violations:";
        if (validConstraintsViolationValues && this.constraintViolation != null) {
            for (int i = 0; i < this.constraintViolation.length; i++) {
                result += String.format(
                        " {Constraint(%d) = %-15.10f}",
                        i + 1,
                        constraintViolation[i]);
            }
            // Total Constraints Violation
            result += String.format(
                    " (Total Violations: %-15.10f)",
                    getTotalConstraintViolation());
        } else {
            result += "Invalid (outdated)";
        }

        return result;
    }

    public int getRank() {
        if (validRankValue) {
            return rank;
        } else {
            throw new InvalidRankValue(this);
        }
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

//    @Override
//    public int hashCode() {
//        int hash = 5;
//        hash = 19 * hash + Arrays.hashCode(this.real);
//        hash = 19 * hash + Arrays.deepHashCode(this.binary);
//        return hash;
//    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Individual other = (Individual) obj;
        for (int i = 0; i < this.real.length; i++) {
            if (Math.abs(this.real[i] - other.real[i]) > Mathematics.EPSILON) {
                return false;
            }
        }
        for (int i = 0; i < this.binary.length; i++) {
            for (int j = 0; j < this.binary[i].representation.length; j++) {
                if (this.binary[i].representation[j]
                        != other.binary[i].representation[j]) {
                    return false;
                }
            }
        }
        for (int i = 0; i < this.custom.length; i++) {
            if(!this.custom[i].equals(other.custom[i])) {
                return false;
            }
        }
        return true;
    }

    public double getConstraintViolation(int constraintIndex) {
        if (validConstraintsViolationValues) {
            return constraintViolation[constraintIndex];
        } else {
            throw new InvalidObjectiveValue();
        }
    }

    public void setConstraintViolation(
            int constraintIndex,
            double constraintViolationValue) {
        constraintViolation[constraintIndex] = constraintViolationValue;
    }

    /**
     * @return the referenceDirection
     */
    public ReferenceDirection getReferenceDirection() {
        if (validReferenceDirection) {
            return new ReferenceDirection(
                    Arrays.copyOf(
                            this.referenceDirection.direction,
                            this.referenceDirection.direction.length));
        } else {
            throw new InvalidReferenceDirectionValue(this);
        }
    }

    /**
     * @param referenceDirection the referenceDirection to set
     */
    public void setReferenceDirection(ReferenceDirection referenceDirection) {
        if (referenceDirection == null) {
            this.referenceDirection = null;
        } else {
            this.referenceDirection = new ReferenceDirection(
                    Arrays.copyOf(
                            referenceDirection.direction,
                            referenceDirection.direction.length));
        }
    }

    /**
     * @return the dominatedByCount
     */
    public int getDominatedByCount() {
        return this.dominatedByCount;
    }

    /**
     * @param dominatedByCount the dominatedByCount to set
     */
    public void setDominatedByCount(int dominatedByCount) {
        this.dominatedByCount = dominatedByCount;
    }

    @Override
    public int compareTo(Individual individual) {
        if (nsga2comparisonObjectiveIndex == -1) {
            if (Mathematics.compare(
                    this.nsga2crowdingDistance,
                    AbstractGeneticEngine.MAX_DOUBLE_VALUE) == 0
                    && Mathematics.compare(individual.nsga2crowdingDistance,
                            AbstractGeneticEngine.MAX_DOUBLE_VALUE) == 0) {
                return 0;
            }
            double crowdedDistDiff
                    = this.nsga2crowdingDistance
                    - individual.nsga2crowdingDistance;
            if (Math.abs(crowdedDistDiff) < Mathematics.EPSILON) {
                return 0;
            }
            if (crowdedDistDiff < 0) {
                return 1;
            } else {
                return -1;
            }
        }
        double objDiff
                = this.getObjective(nsga2comparisonObjectiveIndex)
                - individual.getObjective(nsga2comparisonObjectiveIndex);
        if (Math.abs(objDiff) < Mathematics.EPSILON) {
            return 0;
        }
        if (objDiff < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    public boolean dominates(Individual individual, double epsilon) {
        if (this.isFeasible()) {
            if (individual.isFeasible()) {
                // Both are non-violating (feasible)
                // (the normal NSGA-II dominance principle is applied here)
                int indv1IsBetterInSomeObjective = 0;
                int individual2IsBetterInSomeObjective = 0;
                for (int i = 0; i < optimizationProblem.objectives.length; i++) {
                    if (Mathematics.compare(
                            this.getObjective(i) * (1 - epsilon),
                            individual.getObjective(i))
                            == -1) {
                        indv1IsBetterInSomeObjective = 1;
                    } else {
                        if (Mathematics.compare(
                                this.getObjective(i),
                                individual.getObjective(i) * (1 - epsilon))
                                == 1) {
                            individual2IsBetterInSomeObjective = 1;
                        }
                    }
                }
                if (indv1IsBetterInSomeObjective == 1
                        && individual2IsBetterInSomeObjective == 0) {
                    return true;
                } else {
                    if (indv1IsBetterInSomeObjective == 0
                            && individual2IsBetterInSomeObjective == 1) {
                        return false;
                    } else {
                        return false;
                    }
                }
            } else {
                // The current individual(this) is feasible while the parameter
                // is not (the current feasible individual dominates the 
                // unfeasible parameter)
                return true;
            }
        } else {
            if (individual.isFeasible()) {
                // The current individual(this) is infeasible while the 
                // parameter is feasible (the feasible parameter dominates the 
                // current infeasible individual)
                return false;
            } else {
                // Both are infeasible (check constraint violation)
                if (Mathematics.compare(
                        this.getTotalConstraintViolation(),
                        individual.getTotalConstraintViolation()) == 1) {
                    return true;
                } else if (Mathematics.compare(
                        this.getTotalConstraintViolation(),
                        individual.getTotalConstraintViolation()) == -1) {
                    return false;
                } else {
                    // This condition is only to prevent two identical infeasible
                    // solutions from being assigned to the same rank.
                    // REMEMBER: As per NSGA-III, each infeasible solution must
                    // occupy a front alone after all feasible solutions.
                    // Infeasible solutions plausibility is inversely
                    // proportional to their contsraint violation. So unless
                    // this condition is here, infeasible solutions with
                    // identical violations (which are rare, but exist) will be
                    // considered equal and will end up in the same front
                    // (same ranking), which contradicts with NSGA-III.
                    return true;
                }
            }
        }
    }

    public boolean weaklyDominates(Individual individual, double epsilon) {
        if (this.equals(individual)) {
            return false;
        }
        if (this.isFeasible()) {
            if (individual.isFeasible()) {
                // Both are feasible (check each objective)
                for (int i = 0; i < objectiveFunction.length; i++) {
                    if (Mathematics.compare(
                            this.objectiveFunction[i],
                            individual.objectiveFunction[i]) == 1) {
                        // Return immediately, because even if only one 
                        // objective value in "this" is worse than its
                        // corresponding value in "individual", "this" cannot
                        // dominate "individual".
                        return false;
                    }
                }
                return true;
            } else {
                // The current individual(this) is feasible while the parameter
                // is not (the current feasible individual dominates the 
                // unfeasible parameter)
                return true;
            }
        } else {
            if (individual.isFeasible()) {
                // The current individual(this) is infeasible while the
                // parameter is feasible (the feasible parameter dominates the
                // current infeasible individual)
                return false;
            } else {
                // Both are infeasible (check constraint violation)
                if (this.getTotalConstraintViolation()
                        > individual.getTotalConstraintViolation()) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    /**
     * @return the nsga2crowdedDistance
     */
    public double getNsga2crowdingDistance() {
        return nsga2crowdingDistance;
    }

    /**
     * @param nsga2crowdingDistance the nsga2crowdedDistance to set
     */
    public void setNsga2crowdingDistance(double nsga2crowdingDistance)
            throws
            DoubleAssignmentException {
        if (nsga2crowdingDistanceAlreadySet) {
            throw new DoubleAssignmentException(
                    "crowdingDistance of the current individual has been "
                    + "already calculated");
        } else {
            this.nsga2crowdingDistance = nsga2crowdingDistance;
        }
    }

    public void flagNSGA2CrowdingDistanceAlreadySet()
            throws
            DoubleAssignmentException {
        if (nsga2crowdingDistanceAlreadySet) {
            throw new DoubleAssignmentException(
                    "nsga2crowdingDistanceAlreadySet can only be assiged once "
                    + "for each individual (unless the "
                    + "restartCrowdingDistance(...) method has been "
                    + "called)");
        } else {
            nsga2crowdingDistanceAlreadySet = true;
        }
    }

    public static void restartCrowdingDistanceCalculations(
            Individual[] individuals)
            throws
            DoubleAssignmentException {
        for (Individual individual : individuals) {
            individual.nsga2crowdingDistance = 0;
            individual.nsga2crowdingDistanceAlreadySet = false;
        }
    }

    public static class CustomVariable implements Serializable {

        private CustomVariableSpecs specs;
        private Object content;

        public CustomVariable(CustomVariableSpecs specs) {
            this(specs, null);
        }

        public CustomVariable(CustomVariableSpecs specs, Object content) {
            this.specs = specs;
            this.content = content;
        }

        /**
         * @return the specs
         */
        public CustomVariableSpecs getSpecs() {
            return specs;
        }

        /**
         * @param specs the specs to set
         */
        public void setSpecs(CustomVariableSpecs specs) {
            this.specs = specs;
        }

        /**
         * @return the content
         */
        public Object getContent() {
            return content;
        }

        /**
         * @param content the content to set
         */
        public void setContent(Object content) {
            this.content = content;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + Objects.hashCode(this.content);
            return hash;
        }

        @Override
        public String toString() {
            return String.format(" (%s)", this.content.toString());
        }
 
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CustomVariable other = (CustomVariable) obj;
            if (!Objects.equals(this.content, other.content)) {
                return false;
            }
            return true;
        }
    }

    public static class BinaryVariable implements Serializable {

        private BinaryVariableSpecs specs;
        private byte[] representation;

        public BinaryVariable(
                BinaryVariableSpecs binaryVariableSpecs) {
            this.specs = binaryVariableSpecs;
            representation = new byte[binaryVariableSpecs.getNumberOfBits()];
            for (int i = 0; i < representation.length; i++) {
                //double random = RandomNumberGenerator.nextDouble();
                double random = RandomNumberGenerator.next();
                if (random < 0.5) {
                    representation[i] = 0;
                } else {
                    representation[i] = 1;
                }
            }
        }

        public double getDecimalValue() {
            int sum = 0;
            for (int i = 0; i < representation.length; i++) {
                if (representation[i] == 1) {
                    sum += Math.pow(2, representation.length - i - 1);
                }
            }
            return specs.getMinValue()
                    + sum * (specs.getMaxValue()
                    - specs.getMinValue())
                    / (Math.pow(2, representation.length) - 1);
        }

        public byte getValueOfBit(int bitIndex) {
            return representation[bitIndex];
        }

        public void setBitToOne(int bitIndex) {
            this.representation[bitIndex] = 1;
        }

        public void setBitToZero(int bitIndex) {
            this.representation[bitIndex] = 0;
        }

        /**
         * @return the specs
         */
        public BinaryVariableSpecs getSpecs() {
            return specs;
        }

        /**
         * @param specs the specs to set
         */
        public void setSpecs(BinaryVariableSpecs specs) {
            this.specs = specs;
        }

        @Override
        public String toString() {
            String result = "";
            for (int i = 0; i < representation.length; i++) {
                result += String.valueOf(representation[i]);
            }
            result += String.format(" (%7.3f)", getDecimalValue());
            return result;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + Arrays.hashCode(this.representation);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BinaryVariable other = (BinaryVariable) obj;
            if (!Arrays.equals(this.representation, other.representation)) {
                return false;
            }
            return true;
        }
    }

    public static void restartProposedCrowdingDistanceMeasures(
            Individual[] individuals) {
        for (Individual individual : individuals) {
            individual.proposedCrowdingMeasure = 0;
        }
    }
    public double proposedCrowdingMeasure;

    public int getConstarintsCount() {
        return constraintViolation.length;
    }
}
