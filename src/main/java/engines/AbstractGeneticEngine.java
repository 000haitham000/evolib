/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package engines;

import utils.InputOutput;
import utils.Mathematics;
import utils.RandomNumberGenerator;
import emo.DoubleAssignmentException;
import emo.Individual;
import emo.IndividualsSet;
import emo.OptimizationProblem;
import emo.OptimizationUtilities;
import emo.RealVariableSpecs;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import org.apache.commons.lang3.SerializationUtils;
import parsing.IndividualEvaluator;

/**
 * This is the superclass of all evolutionary engines. It has the full
 * implementation of common evolutionary procedures along stubs for those
 * details that change from one engine to another. The class also provides a lot
 * of logging functionality.
 *
 * TODO: remove operators implementation to a separate package.
 *
 * @author Haitham Seada
 */
public abstract class AbstractGeneticEngine {

    public static final double MIN_DOUBLE_VALUE = Math.pow(10, -6);
    public static final double MAX_DOUBLE_VALUE = Math.pow(10, 12);
    public final OptimizationProblem optimizationProblem;
    public final IndividualEvaluator individualEvaluator;
    //Individual[] population;
    public static boolean DEBUG_ALL = false;
    public static boolean DEBUG_REFERENCE_DIRECTIONS = true;
    public static boolean DEBUG_POPULATIONS = false;
    public static boolean DEBUG_RANKING = false;
    public static boolean DEBUG_IDEAL_POINT = false;
    public static boolean DEBUG_TRANSLATION = false;
    public static boolean DEBUG_INTERCEPTS = false;
    public static boolean DEBUG_ASSOCIATION = false;
    public static boolean EXTREME_POINTS_DEEP_DEBUG = false;

    public static boolean DUMP_ALL_GENERATIONS_DECISION_SPACE = true;
    public static boolean DUMP_ALL_GENERATIONS_OBJECTIVE_SPACE = true;
    public static boolean DUMP_ALL_GENERATIONS_MATLAB_SCRIPTS = true;
    public static boolean DUMP_ALL_GENERATIONS_KKTPM = true;
    public static boolean DUMP_ALL_GENERATIONS_META_DATA = true;
    // If DUMP_ALL_GENERATIONS_NORMALIZED_MATLAB_SCRIPTS is true if and only if
    // DUMP_ALL_GENERATIONS_OBJECTIVE_SPACE is also true,
    // otherwise DUMP_ALL_GENERATIONS_NORMALIZED_MATLAB_SCRIPTS value will be
    // ignored.
    public final static boolean DUMP_ALL_GENERATIONS_NORMALIZED_MATLAB_SCRIPTS
            = false;
    // If DUMP_ANIMATED_MATLAB_SCRIPT is true if and only if
    // DUMP_ALL_GENERATIONS_OBJECTIVE_SPACE is also true,
    // otherwise DUMP_ANIMATED_MATLAB_SCRIPT value will be ignored.
    public final static boolean DUMP_ANIMATED_MATLAB_SCRIPT
            = false;

    // <editor-fold defaultstate="collapsed" desc="Fields">
    protected int currentGenerationIndex = -1;
    protected double[] currentIdealPoint;
    protected double[] previousIdealPoint;
    protected double currentHV;
    protected double currentKktMetric;
    protected double[] hvReferencePoint;
    protected double[] hvIdealPoint;
    protected Individual[] currentPopulation;
    protected double hvLimit;
    protected int funcEvaluationsLimit;
    protected double epsilon;
    protected int runIndex;
    // </editor-fold>

    public AbstractGeneticEngine(
            OptimizationProblem optimizationProblem,
            IndividualEvaluator individualEvaluator) {
        this.optimizationProblem = optimizationProblem;
        this.individualEvaluator = individualEvaluator;
        RandomNumberGenerator.setSeed(optimizationProblem.getSeed());
    }

    protected void initializeFields() {
        // Initializtions
        currentGenerationIndex = 0;
        currentIdealPoint = null;
        // Save the current ideal point for future use (we need to improve
        // on the current ideal points as generations go. Creating a completely
        // new ideal point from each generation independent from previous
        // generations, may cause degradation in the ideal point values)
        // We need to save the previous ideal point even after getting the new
        // one, because:
        // 1 - We might not get a better ideal point from the current
        //     generation. In such a case the previous ideal point is
        //     either fully maintained or partially used to update the
        //     new ideal point.
        // 2 - We need to know the difference between the current ideal point
        //     and the previous one so that we can re-translate the extreme
        //     if the ideal point is updated.
        previousIdealPoint = new double[optimizationProblem.objectives.length];
        currentHV = -1;
        currentKktMetric = Double.MAX_VALUE;
        // Get the actual reference point (to calculate hypervolume)
        try {
            hvReferencePoint = individualEvaluator.getReferencePoint();
            // Adjust reference point so that end points will contribute to the
            // hypervolume
            for (int i = 0; i < hvReferencePoint.length; i++) {
                hvReferencePoint[i] *= 1.01;
            }
        } catch (UnsupportedOperationException ex) {
            hvReferencePoint = null;
        }
        // Get the actual ideal point
        try {
            hvIdealPoint = individualEvaluator.getIdealPoint();
        } catch (UnsupportedOperationException ex) {
            hvIdealPoint = null;
        }
    }

    protected boolean terminationCondition() {
        if (currentGenerationIndex == optimizationProblem.getGenerationsCount()
                || currentHV >= hvLimit
                || individualEvaluator.getFunctionEvaluationsCount()
                >= funcEvaluationsLimit) {
            return true;
        } else {
            return false;
        }
    }

    public Individual[] generateInitialPopulation(String displayMessage) {
        Individual[] population
                = new Individual[optimizationProblem.getPopulationSize()];
        for (int i = 0; i < population.length; i++) {
            population[i]
                    = new Individual(optimizationProblem, individualEvaluator);
        }
        if (DEBUG_ALL || DEBUG_POPULATIONS) {
            InputOutput.displayPopulation(
                    optimizationProblem,
                    displayMessage != null ? displayMessage : "", population);
        }
        return population;
    }

    protected Individual tournamentSelect(IndividualsSet subset) {
        Individual individual1 = subset.getIndividual1();
        Individual individual2 = subset.getIndividual2();
        // If the problem is constrained and at least one of the
        // individuals under investigation is infeasible return the feasible
        // one (which is the dominating individual).
        if (individual1.getRank() < individual2.getRank()) {
            // Individual-1 dominates Individual-2
            return individual1;
        } else if (individual1.getRank() > individual2.getRank()) {
            // Individual-2 dominates Individual-1
            return individual2;
        } else if (Mathematics.compare(
                individual1.getNsga2crowdingDistance(),
                individual2.getNsga2crowdingDistance()) == 1) {
            // Both Individual-1 & Individual-2 are non-dominated with respect 
            // to each other but Individual-1 has a larger crowding distance.
            return individual1;
        } else if (Mathematics.compare(
                individual1.getNsga2crowdingDistance(),
                individual2.getNsga2crowdingDistance()) == -1) {
            // Both Individual-1 & Individual-2 are non-dominated with respect 
            // to each other but Individual-2 has a larger crowding distance.
            return individual2;
        } else // Both Individual-1 & Individual-2 are non-dominated with respect 
        // to each other and have the same crodingDistance (a rare case).
        // In this case select one of the two individuals randomly.
        {
            if (RandomNumberGenerator.next() <= 0.5) {
                return individual1;
            } else {
                return individual2;
            }
        }
    }

    /**
     * Create a new population from an already existing population. This method
     * applies tournament selection then crossover to the input population.
     *
     * @param oldPopulation
     * @return
     */
    protected Individual[] getOffspringPopulation(Individual[] oldPopulation) {
        Individual[] newPopulation
                = new Individual[optimizationProblem.getPopulationSize()];
        int[] a1 = new int[optimizationProblem.getPopulationSize()];
        int[] a2 = new int[optimizationProblem.getPopulationSize()];
        int temp;
        int i;
        int rand;
        Individual parent1, parent2;
        IndividualsSet childrenSet;
        for (i = 0; i < optimizationProblem.getPopulationSize(); i++) {
            a1[i] = a2[i] = i;
        }
        for (i = 0; i < optimizationProblem.getPopulationSize(); i++) {
            rand = RandomNumberGenerator.nextInt(
                    i,
                    optimizationProblem.getPopulationSize() - 1);
            temp = a1[rand];
            a1[rand] = a1[i];
            a1[i] = temp;
            rand = RandomNumberGenerator.nextInt(
                    i,
                    optimizationProblem.getPopulationSize() - 1);
            temp = a2[rand];
            a2[rand] = a2[i];
            a2[i] = temp;
        }
        for (i = 0; i < optimizationProblem.getPopulationSize(); i += 4) {
            parent1 = tournamentSelect(
                    new IndividualsSet(oldPopulation[a1[i]],
                            oldPopulation[a1[i + 1]]));
            parent2 = tournamentSelect(
                    new IndividualsSet(oldPopulation[a1[i + 2]],
                            oldPopulation[a1[i + 3]]));
            childrenSet = crossover(new IndividualsSet(parent1, parent2));
            newPopulation[i] = childrenSet.getIndividual1();
            newPopulation[i + 1] = childrenSet.getIndividual2();

            parent1 = tournamentSelect(
                    new IndividualsSet(oldPopulation[a2[i]],
                            oldPopulation[a2[i + 1]]));
            parent2 = tournamentSelect(
                    new IndividualsSet(
                            oldPopulation[a2[i + 2]],
                            oldPopulation[a2[i + 3]]));
            childrenSet = crossover(new IndividualsSet(parent1, parent2));
            newPopulation[i + 2] = childrenSet.getIndividual1();
            newPopulation[i + 3] = childrenSet.getIndividual2();
        }
        // Nullify (set as null) all the reference directions of the offspring.
        // This is a protective step to make the code run into an exception
        // if an individual from the offspring was forgotten in the association
        // step. Remember that offsprings are results of crossovers and mutation
        // so most probably the direction they have (copied from their parents)
        // are no longer valid according to their new objective values.
        for (Individual individual : newPopulation) {
            individual.setReferenceDirection(null);
        }
        return newPopulation;
    }

    protected IndividualsSet crossover(IndividualsSet parents) {
        IndividualsSet children = new IndividualsSet(
                SerializationUtils.clone(parents.getIndividual1()),
                SerializationUtils.clone(parents.getIndividual2()));
//        IndividualsSet children = new IndividualsSet(
//                new Individual(parents.getIndividual1()),
//                new Individual(parents.getIndividual2()));
        realCrossover(parents, children);
        binaryCrossover(parents, children);
        customCrossover(parents, children);
        return children;
    }

    public void binaryCrossover(IndividualsSet parents, IndividualsSet children) {
        Individual parent1 = parents.getIndividual1();
        Individual parent2 = parents.getIndividual2();
        Individual child1 = children.getIndividual1();
        Individual child2 = children.getIndividual2();
        // Loop over all available binary variables
        int binaryVarCount = parent1.binary.length;
        for (int i = 0; i < binaryVarCount; i++) {
            int bitsCount = parent1.binary[i].getSpecs().getNumberOfBits();
            // Perform binary crossover according to the user defined probability
            if (RandomNumberGenerator.next()
                    < optimizationProblem.getBinaryCrossoverProbability()) {
                // Randomly pick two cut positions
                int startCutIndex = RandomNumberGenerator.nextInt(0, bitsCount);
                int endCutIndex = RandomNumberGenerator.nextInt(0, bitsCount);
                // Reorder cut points (ascendingly) if required
                if (startCutIndex > endCutIndex) {
                    int temp = startCutIndex;
                    startCutIndex = endCutIndex;
                    endCutIndex = temp;
                }
                // Binary crossover
                for (int bitIndex = 0; bitIndex < bitsCount; bitIndex++) {
                    if (bitIndex < startCutIndex || bitIndex > endCutIndex) {
                        // Copy bits from P1 to C1
                        if (parent1.binary[i].getValueOfBit(bitIndex) == 0) {
                            child1.binary[i].setBitToZero(bitIndex);
                        } else {
                            child1.binary[i].setBitToOne(bitIndex);
                        }
                        // Copy bits from P2 to C2
                        if (parent2.binary[i].getValueOfBit(bitIndex) == 0) {
                            child2.binary[i].setBitToZero(bitIndex);
                        } else {
                            child2.binary[i].setBitToOne(bitIndex);
                        }
                    } else {
                        // Copy bits from P1 to C2
                        if (parent1.binary[i].getValueOfBit(bitIndex) == 0) {
                            child2.binary[i].setBitToZero(bitIndex);
                        } else {
                            child2.binary[i].setBitToOne(bitIndex);
                        }
                        // Copy bits from P2 to C1
                        if (parent2.binary[i].getValueOfBit(bitIndex) == 0) {
                            child1.binary[i].setBitToZero(bitIndex);
                        } else {
                            child1.binary[i].setBitToOne(bitIndex);
                        }
                    }
                }
            } else {
                // Copy all bits from P1 to C1 & from P2 to C2
                for (int bitIndex = 0; bitIndex < bitsCount; bitIndex++) {
                    if (parent1.binary[i].getValueOfBit(bitIndex) == 0) {
                        child1.binary[i].setBitToZero(bitIndex);
                    } else {
                        child1.binary[i].setBitToOne(bitIndex);
                    }
                    if (parent2.binary[i].getValueOfBit(bitIndex) == 0) {
                        child2.binary[i].setBitToZero(bitIndex);
                    } else {
                        child2.binary[i].setBitToOne(bitIndex);
                    }
                }
            }
        }
    }

    public void realCrossover(IndividualsSet parents, IndividualsSet children) {
        Individual parent1 = parents.getIndividual1();
        Individual parent2 = parents.getIndividual2();
        Individual child1 = children.getIndividual1();
        Individual child2 = children.getIndividual2();
        // Get the number of real variables
        int realVarCount = parent1.real.length;

        double par1Value, par2Value, child1Value, child2Value, betaq, beta, alpha;
        double y1, y2, yu, yl, expp;

        //Check Whether the cross-over to be performed
        double rnd = RandomNumberGenerator.next();
        if (rnd <= optimizationProblem.getRealCrossoverProbability()) {

            //Loop over variables
            for (int j = 0; j < realVarCount; j++) {
                par1Value = parent1.real[j];
                par2Value = parent2.real[j];

                // Get the specs object of that real variable
                RealVariableSpecs specs = null;
                int realVarIndex = -1;
                for (int k = 0; k < optimizationProblem.variablesSpecs.length; k++) {
                    if (optimizationProblem.variablesSpecs[k] instanceof RealVariableSpecs) {
                        realVarIndex++;
                        if (realVarIndex == j) {
                            specs = (RealVariableSpecs) optimizationProblem.variablesSpecs[k];
                        }
                    }
                }

                yl = specs.getMinValue();
                yu = specs.getMaxValue();

                rnd = RandomNumberGenerator.next();

                // Check whether variable is selected or not
                if (rnd <= 0.5) {
                    //Variable selected
                    //if (Math.abs(par1Value - par2Value) > 0.000001) // changed by Deb (31/10/01)
                    if (Mathematics.compare(par1Value, par2Value) != 0) {
                        if (par2Value > par1Value) {
                            y2 = par2Value;
                            y1 = par1Value;
                        } else {
                            y2 = par1Value;
                            y1 = par2Value;
                        }

                        //Find beta value
                        if ((y1 - yl) > (yu - y2)) {
                            beta = 1 + (2 * (yu - y2) / (y2 - y1));
                            //printf("beta = %f\n",beta);
                        } else {
                            beta = 1 + (2 * (y1 - yl) / (y2 - y1));
                            //printf("beta = %f\n",beta);
                        }

                        //Find alpha
                        expp = optimizationProblem.getRealCrossoverDistIndex() + 1.0;

                        beta = 1.0 / beta;

                        alpha = 2.0 - Math.pow(beta, expp);

                        if (alpha < 0.0) {
                            System.out.format("ERRRROR %f %f %f", alpha, par1Value, par2Value);
                            System.exit(-1);
                        }

                        rnd = RandomNumberGenerator.next();

                        if (rnd <= 1.0 / alpha) {
                            alpha = alpha * rnd;
                            expp = 1.0 / (optimizationProblem.getRealCrossoverDistIndex() + 1.0);
                            betaq = Math.pow(alpha, expp);
                        } else {
                            alpha = alpha * rnd;
                            alpha = 1.0 / (2.0 - alpha);
                            expp = 1.0 / (optimizationProblem.getRealCrossoverDistIndex() + 1.0);
                            if (alpha < 0.0) {
                                System.out.format("ERRRROR %f %f %f", alpha, par1Value, par2Value);
                                System.out.println("ERRRORRR");
                                System.exit(-1);
                            }
                            betaq = Math.pow(alpha, expp);
                        }

                        //Generating two children
                        child1Value = 0.5 * ((y1 + y2) - betaq * (y2 - y1));
                        child2Value = 0.5 * ((y1 + y2) + betaq * (y2 - y1));

                    } else {

                        betaq = 1.0;
                        y1 = par1Value;
                        y2 = par2Value;

                        //Generation two children
                        child1Value = 0.5 * ((y1 + y2) - betaq * (y2 - y1));
                        child2Value = 0.5 * ((y1 + y2) + betaq * (y2 - y1));

                    }
                    // added by deb (31/10/01)
                    if (child1Value < yl) {
                        child1Value = yl;
                    }
                    if (child1Value > yu) {
                        child1Value = yu;
                    }
                    if (child2Value < yl) {
                        child2Value = yl;
                    }
                    if (child2Value > yu) {
                        child2Value = yu;
                    }
                } else {
                    //Copying the children to parents
                    child1Value = par1Value;
                    child2Value = par2Value;
                }
                child1.real[j] = child1Value;
                child2.real[j] = child2Value;
            }
        } else {
            for (int j = 0; j < realVarCount; j++) {
                par1Value = parent1.real[j];
                par2Value = parent2.real[j];
                child1Value = par1Value;
                child2Value = par2Value;
                child1.real[j] = child1Value;
                child2.real[j] = child2Value;
            }
        }
    }

    public void customCrossover(
            IndividualsSet parents,
            IndividualsSet children) {
        if (parents.getIndividual1().custom.length != 0) {
            Individual parent1DeepCopy = SerializationUtils.clone(
                    parents.getIndividual1());
            Individual parent2DeepCopy = SerializationUtils.clone(
                    parents.getIndividual2());
            //Check Whether the cross-over to be performed
            double rnd = RandomNumberGenerator.next();
            if (rnd <= optimizationProblem.getRealCrossoverProbability()) {
                crossCustomVariables(
                        parent1DeepCopy,
                        parent2DeepCopy);
            }
            children.setIndividual1(parent1DeepCopy);
            children.setIndividual2(parent2DeepCopy);
        }
    }

    protected void crossCustomVariables(
            Individual parent1DeepCopy,
            Individual parent2DeepCopy) {
        throw new UnsupportedOperationException("Crossing custom variables is"
                + " not supported yet. Override \"void "
                + "crossCustomVariables(Individual parent1DeepCopy, Individual "
                + "parent2DeepCopy)\" in your engine.");
    }

    public void mutate(Individual[] individuals) {
        for (int i = 0; i < optimizationProblem.getPopulationSize(); i++) {
            mutationIndividual(individuals[i]);
        }
    }

    /* Function to perform mutation of an individual */
    void mutationIndividual(Individual individual) {
        if (individual.real.length != 0) {
            realMutateIndividual(individual);
        }
        if (individual.binary.length != 0) {
            binaryMutateIndividual(individual);
        }
        if (individual.custom.length != 0) {
            customMutateIndividual(individual);
        }
    }

    /* Routine for binary mutation of an individual */
    public void binaryMutateIndividual(Individual individual) {
        for (int j = 0; j < individual.binary.length; j++) {
            for (int k = 0; k < individual.binary[j].getSpecs().getNumberOfBits(); k++) {
                double prob = RandomNumberGenerator.next();
                if (prob <= optimizationProblem.getBinaryMutationProbabilty()) {
                    if (individual.binary[j].getValueOfBit(k) == 0) {
                        individual.binary[j].setBitToOne(k);
                    } else {
                        individual.binary[j].setBitToZero(k);
                    }
                }
            }
        }
    }

    /* Routine for real polynomial mutation of an individual */
    public void realMutateIndividual(Individual individual) {
        int j;
        double rnd, delta1, delta2, mut_pow, deltaq;
        double y, yl, yu, val, xy;
        for (j = 0; j < individual.real.length; j++) {
            if (RandomNumberGenerator.next() <= optimizationProblem.getRealMutationProbability()) {

                // Get the specs object of that real variable
                RealVariableSpecs specs = null;
                int realVarIndex = -1;
                for (int k = 0; k < optimizationProblem.variablesSpecs.length; k++) {
                    if (optimizationProblem.variablesSpecs[k] instanceof RealVariableSpecs) {
                        realVarIndex++;
                        if (realVarIndex == j) {
                            specs = (RealVariableSpecs) optimizationProblem.variablesSpecs[k];
                        }
                    }
                }
                y = individual.real[j];
                //originalY.add(y);
                yl = specs.getMinValue();
                yu = specs.getMaxValue();
                delta1 = (y - yl) / (yu - yl);
                delta2 = (yu - y) / (yu - yl);
                rnd = RandomNumberGenerator.next();
                mut_pow = 1.0 / (optimizationProblem.getRealMutationDistIndex() + 1.0);
                if (rnd <= 0.5) {
                    xy = 1.0 - delta1;
                    val = 2.0 * rnd + (1.0 - 2.0 * rnd) * (Math.pow(xy, (optimizationProblem.getRealMutationDistIndex() + 1.0)));
                    deltaq = Math.pow(val, mut_pow) - 1.0;
                } else {
                    xy = 1.0 - delta2;
                    val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (Math.pow(xy, (optimizationProblem.getRealMutationDistIndex() + 1.0)));
                    deltaq = 1.0 - (Math.pow(val, mut_pow));
                }
                y = y + deltaq * (yu - yl);
                if (y < yl) {
                    y = yl;
                }
                if (y > yu) {
                    y = yu;
                }
                individual.real[j] = y;
            }
        }
    }

    public void customMutateIndividual(Individual individual) {
        for (int j = 0; j < individual.custom.length; j++) {
            if (RandomNumberGenerator.next()
                    <= optimizationProblem.getRealMutationProbability()) {
                mutateCustomVariables(individual);
            }
        }
    }

    protected void mutateCustomVariables(Individual individual) {
        throw new UnsupportedOperationException("Mutating custom variables is "
                + "not supported yet. Override "
                + "\"void mutateCustomVariableContents(Individual individual)"
                + "\" in  your engine.");
    }

    protected Individual[] merge(
            Individual[] oldPopulation,
            Individual[] newPopulation) {
        Individual[] combinedPopulation
                = new Individual[oldPopulation.length + newPopulation.length];
        System.arraycopy(oldPopulation, 0, combinedPopulation, 0, oldPopulation.length);
        System.arraycopy(newPopulation, 0, combinedPopulation, oldPopulation.length, newPopulation.length);
        // According to the crossover implementation used in NSGA-2 and NSGA-3,
        // there should not be any common references between the parents and
        // their offspring. Therefore there is no need to resolve common
        // references (the following two commented lines) and the following line
        // makes sure this is the case, otherwise it throws an exception.
        checkCommonReferences(combinedPopulation, "STOP, SOMETHING IS WRONG");
        if (DEBUG_ALL || DEBUG_POPULATIONS) {
            InputOutput.displayPopulation(optimizationProblem,
                    String.format("%d-population+%d-offspring",
                            currentGenerationIndex,
                            currentGenerationIndex),
                    combinedPopulation);
        }
        return combinedPopulation;
    }

    int checkDominance(
            Individual individual1,
            Individual individual2,
            double epsilon) {
        if (individual1.dominates(individual2, epsilon)) {
            return 1;
        } else if (individual2.dominates(individual1, epsilon)) {
            return -1;
        } else {
            return 0;
        }
    }

    public List<List<Individual>> rankIndividuals(
            Individual[] individuals,
            double epsilon,
            String optionalDisplayMessage) {
        int flag;
        int i;
        int end;
        int frontSize;
        int rank = 1;
        ArrayList<Individual> orig = new ArrayList<>();
        ArrayList<Individual> cur;
        cur = new ArrayList<Individual>();
        int temp1;
        int temp2;
        for (i = 0; i < individuals.length; i++) {
            orig.add(individuals[i]);
        }
        do {
            if (orig.size() == 1) {
                orig.get(0).setRank(rank);
                orig.get(0).validRankValue = true;
                break;
            }
            cur.add(0, orig.remove(0));
            temp1 = 0;
            temp2 = 0;
            frontSize = 1;
            do {
                temp2 = 0;
                do {
                    end = 0;
                    flag = checkDominance(
                            orig.get(temp1),
                            cur.get(temp2), epsilon);
                    if (flag == 1) {
                        orig.add(0, cur.remove(temp2));
                        temp1++;
                        frontSize--;
                    } else if (flag == -1) {
                        end = 1;
                    } else {
                        temp2++;
                    }
                } while (end != 1 && temp2 < cur.size());
                if (flag == 0 || flag == 1) {
                    cur.add(0, orig.get(temp1));
                    temp2++;
                    frontSize++;
                    orig.remove(temp1);
                }
                if (flag == -1) {
                    temp1++;
                }
            } while (temp1 < orig.size());
            for (Individual individual : cur) {
                individual.setRank(rank);
                individual.validRankValue = true;
            }
            cur.clear();
            rank++;
        } while (!orig.isEmpty());
        // Prepare return List of Lists
        List<List<Individual>> fronts = new ArrayList<>();
        // Get Max rank
        int maxRank = 1;
        for (Individual individual : individuals) {
            if (individual.getRank() > maxRank) {
                maxRank = individual.getRank();
            }
        }
        // Initialize the fronts lists
        for (int f = 0; f < maxRank; f++) {
            fronts.add(new ArrayList<Individual>());
        }
        // Add each individual to its corresponding rank
        for (Individual individual : individuals) {
            fronts.get(individual.getRank() - 1).add(individual);
        }
        // Debugging
        if (DEBUG_ALL || DEBUG_RANKING) {
            InputOutput.displayRanks(
                    optionalDisplayMessage != null
                            ? optionalDisplayMessage : "", individuals);
        }
        // Return the final list of fronts
        return fronts;
    }

    public double[] getInitialIdealPoint(
            Individual[] individuals,
            String optionalDisplayMessage) {
        double[] idealPoint = new double[optimizationProblem.objectives.length];
        // Let the ideal point be the first point in the population (just as a start)
        for (int i = 0; i < idealPoint.length; i++) {
            idealPoint[i] = /*individuals[0].getObjective(i)*/ Math.pow(10, 12);
        }
        // Update the value of each objective in the population if a smaller value
        // is found in any subsequent population member
        for (int i = 0; i < individuals.length; i++) {
            if (Mathematics.compare(individuals[i].getTotalConstraintViolation(), 0) == 0) {
                for (int j = 0; j < idealPoint.length; j++) {
                    if (individuals[i].getObjective(j) < idealPoint[j]) {
                        idealPoint[j] = individuals[i].getObjective(j);
                    }
                }
            }
        }
        // Debugging
        if (DEBUG_ALL || DEBUG_IDEAL_POINT) {
            InputOutput.displayIdealPoint(optionalDisplayMessage != null ? optionalDisplayMessage : "", idealPoint);
        }
        // Return the ideal point
        return idealPoint;
    }

    /**
     * This method calculates the new ideal point from the set of individuals
     * passed to it and compares the new ideal point with the current one to
     * make sure that the new point is smaller than or equal to its predecessor
     * in every objective. Remember that one of the points used to calculate the
     * current ideal point might have been lost in the course of evolution. In
     * such a case the updated ideal point might be worse (greater) than its
     * predecessor (current ideal point) in some objectives. That's why this
     * method compares the two ideal points to get the best of the two worlds.
     *
     * @param individuals used to calculate the new (updated) ideal point
     * @param idealPoint used to make sure that the new (updated) ideal point is
     * at least as good as the current ideal point in all objectives.
     * @param optionalDisplayMessage An optional message for display
     */
    public void updateIdealPoint(Individual[] individuals, double[] idealPoint, String optionalDisplayMessage) {
        double[] updatedIdealPoint = OptimizationUtilities.getIdealPoint(individuals);
        for (int i = 0; i < idealPoint.length; i++) {
            if (idealPoint[i] < updatedIdealPoint[i]) {
                updatedIdealPoint[i] = idealPoint[i];
            }
        }
        previousIdealPoint = currentIdealPoint;
        currentIdealPoint = updatedIdealPoint;
        if (DEBUG_ALL || DEBUG_IDEAL_POINT) {
            InputOutput.displayIdealPoint(optionalDisplayMessage != null ? "" : optionalDisplayMessage, currentIdealPoint);
        }
    }

    public int getRemainingCount(List<List<Individual>> fronts) {
        int individualsCount = 0;
        // Get the last front Fl index
        int lastFrontIndex = -1;
        while (individualsCount < optimizationProblem.getPopulationSize()) {
            lastFrontIndex++;
            individualsCount += fronts.get(lastFrontIndex).size();
        }
        // Determine the number of individuals required to complete the population (REMAINING)
        int remaining;
        if (individualsCount == optimizationProblem.getPopulationSize()) {
            remaining = 0;
        } else {
            individualsCount -= fronts.get(lastFrontIndex).size();
            remaining = optimizationProblem.getPopulationSize() - individualsCount;
        }
        return remaining;
    }

    public int getLimitingFrontIndex(List<List<Individual>> fronts) {
        int individualsCount = 0;
        // Get the last front Fl index
        int lastFrontIndex = -1;
        while (individualsCount <= optimizationProblem.getPopulationSize()) {
            lastFrontIndex++;
            individualsCount += fronts.get(lastFrontIndex).size();
        }
        return lastFrontIndex;
    }

    /**
     * This method fills the destination population with individuals from the
     * source population in two steps: (1)STEP-1: Add any individual in
     * sourcePopulation whose front will be completely accommodated in the
     * destinationPopulation. (2)Step-2: Add all the individuals of
     * lastFrontSubset to the destinationPopulation. (Note: this method is not
     * responsible for selecting individuals from the last front to be
     * accommodated. It is assumed that selection has already been made and that
     * the individuals of lastFrontSubset are the result of that selection).
     * (Note: any previous items in destinationPopulation will be overwritten)
     *
     * @param destinationPopulation population at which selected members are
     * stored
     * @param sourcePopulation population from which top-ranked members are
     * selected
     * @param lastFrontSubset contains selected individuals from the last front
     * to be accommodated
     * @param lastFrontIndex the index of the limiting front (the front before
     * @throws EvaluationException
     */
    public void reFillPopulation(
            Individual[] destinationPopulation,
            Individual[] sourcePopulation,
            Individual[] lastFrontSubset,
            int lastFrontIndex) {
        int index = 0;
        for (Individual individual : sourcePopulation) {
            if (individual.getRank() - 1 < lastFrontIndex) { // Remember that ranks starts at 1 not at Zero
                destinationPopulation[index] = individual;
                index++;
            }
        }
        for (int i = 0; i < lastFrontSubset.length; i++) {
            destinationPopulation[index] = /*new Individual(optimizationProblem,*/ lastFrontSubset[i]/*, individualEvaluator)*/;
            index++;
        }
    }

    /**
     * This method fills the destination fronts with individuals from the fronts
     * in order i.e. individuals of smaller-rank fronts are taken before
     * individuals of larger-rank fronts. Individuals are taken from the first
     * front as much as needed and in order of their presence (storage) in that
     * front. It is worth noting that there is no preference information taken
     * into account when selecting individuals from the last front. They are
     * taken in order as mentioned before. So, this method should only be used
     * when selecting individuals from a group of fronts that has a number of
     * feasible solutions less than (case 1) or exactly equal to (case) the
     * number of individuals required in the destination population. Here we use
     * it only for case 1. In case 1, each of the last (k) fronts, where (k) is
     * smaller than the population size (N), contains only one infeasible
     * solution. (Remember: in normal domination sorting, each infeasible
     * solution will lie in a separate front) (Note: any previous items in
     * destinationPopulation will be overwritten)
     *
     * @param destinationPopulation population at which selected members are
     * stored
     * @param fronts fronts from which individuals are selected
     */
    public void reFillPopulation(Individual[] destinationPopulation, List<List<Individual>> fronts) {
        int count = 0;
        int frontIndex = 0;
        int withinFrontIndex = 0;
        while (count < destinationPopulation.length) {
            if (withinFrontIndex < fronts.get(frontIndex).size()) {
                destinationPopulation[count] = fronts.get(frontIndex).get(withinFrontIndex);
                count++;
                withinFrontIndex++;
            } else {
                frontIndex++;
                withinFrontIndex = 0;
            }
        }
    }

    /**
     * Refills the destination population with the top ranked individuals of the
     * source population. The method should be used when the number of
     * individuals in the pre-limiting fronts is exactly equal to the number of
     * individuals required in the destination fronts. (Note: any previous items
     * in destinationPopulation will be overwritten)
     *
     * @param destinationPopulation population at which selected members are
     * stored
     * @param sourcePopulation population from which top-ranked members are
     * selected
     * @param lastFrontIndex the index of the limiting front (the front before
     * which the method should stop).
     * @throws EvaluationException
     */
    public void reFillPopulation(
            Individual[] destinationPopulation,
            Individual[] sourcePopulation,
            int lastFrontIndex) {
        int index = 0;
        for (Individual individual : sourcePopulation) {
            if (individual.getRank() - 1 < lastFrontIndex) { // Remember that ranks starts at 1 not at Zero
                destinationPopulation[index] = individual;
                index++;
            }
        }
    }

    /**
     * Resolve common references in a population of individuals. Selection may
     * cause some individuals to be shallow copied from the parent population to
     * the offspring population. In such a case The two populations will have
     * two references that share the same object in memory. Consequently, the
     * merged population will have multiple references pointing to the same
     * object. Imagine now that we need to do perform some logic (like
     * translation or normalization) on the individuals of the merged structure.
     * If we traversed the merged structure performing the logic on each
     * individual during our traversal, this logic will performed more than one
     * time on this shared object. This happens because each time we encounter a
     * reference to the same object, the logic will be performed on this object.
     * These references must be disjoint by creating a separate unique object
     * for each.
     *
     * @param individuals an array of individuals
     * @return
     */
    protected Individual[] resolveCommonReferences(Individual[] individuals) {
        Individual[] resolvedIndividuals = new Individual[individuals.length];
        for (int i = 0; i < individuals.length; i++) {
            resolvedIndividuals[i] = new Individual(individuals[i]);
        }
        return resolvedIndividuals;
    }

    protected void checkCommonReferences(
            Individual[] individuals,
            String message) {
        for (int i = 0; i < individuals.length - 1; i++) {
            for (int j = i + 1; j < individuals.length; j++) {
                if (individuals[i] == individuals[j]) {
                    throw new UnsupportedOperationException(String.format(
                            "Common Reference (IND(%d) & IND(%d)): %s",
                            i, j, message));
                }
            }
        }
    }

    protected void checkCommonReferencesList(
            List<Individual> individualsList,
            String message) {
        Individual[] individuals = new Individual[individualsList.size()];
        individualsList.toArray(individuals);
        for (int i = 0; i < individuals.length - 1; i++) {
            for (int j = i + 1; j < individuals.length; j++) {
                if (individuals[i] == individuals[j]) {
                    throw new UnsupportedOperationException(String.format(
                            "Common Reference (IND(%d) & IND(%d)): %s",
                            i, j, message));
                }
            }
        }
    }

    protected void checkCommonReferences(
            List<List<Individual>> fronts,
            String message) {
        List<Individual> individualsList = new ArrayList<Individual>();
        for (List<Individual> front : fronts) {
            for (Individual individual : front) {
                individualsList.add(individual);
            }
        }
        Individual[] arr = new Individual[individualsList.size()];
        individualsList.toArray(arr);
        checkCommonReferences(arr, message);
    }

    protected abstract void updateTerminationMetrics()
            throws
            UnsupportedOperationException;

    protected void displayPopulationIfDebug(
            int generationsCounter,
            Individual[] currentPopulation) {
        if (DEBUG_ALL || DEBUG_POPULATIONS) {
            System.out.format("--------------------------------------%n");
            System.out.format("Final Population After Generation(%d)%n",
                    generationsCounter);
            System.out.format("--------------------------------------%n");
            InputOutput.displayPopulation(
                    optimizationProblem, String.valueOf(generationsCounter + 1),
                    currentPopulation);
        }
    }

    protected void updateObjectives(Individual[] individuals) {
        for (Individual individual : individuals) {
            individualEvaluator.updateIndividualObjectivesAndConstraints(
                    optimizationProblem,
                    individual);
        }
        if (DEBUG_ALL || DEBUG_POPULATIONS) {
            InputOutput.displayPopulation(
                    optimizationProblem,
                    currentGenerationIndex + "-offspring",
                    individuals);
        }
    }

    public abstract Individual[] niching(
            List<List<Individual>> fronts,
            int remainingIndvsCount)
            throws
            DoubleAssignmentException;

    public Individual[] start(
            File outputDir,
            int runIndex,
            double epsilon)
            throws
            FileNotFoundException,
            DoubleAssignmentException,
            IOException {
        return start(outputDir, runIndex, epsilon, Double.MAX_VALUE);
    }

    public Individual[] start(
            File outputDir,
            int runIndex,
            double epsilon,
            double hvLimit
    ) throws FileNotFoundException, DoubleAssignmentException, IOException {
        return start(outputDir, runIndex, epsilon, hvLimit, Integer.MAX_VALUE);
    }

    public abstract Individual[] start(
            File outputDir,
            int runIndex,
            double epsilon,
            double hvLimit,
            int funcEvaluationsLimit
    )
            throws
            FileNotFoundException,
            DoubleAssignmentException,
            IOException;

    protected abstract void fillUpPopulation(
            List<List<Individual>> fronts,
            Individual[] mergedPopulation)
            throws
            DoubleAssignmentException;

    protected abstract Individual[] prepareFinalPopulation();

    public abstract String getAlgorithmName();

    protected void iterationStart() {
        System.out.println("-----");
        System.out.format("Generation (%04d)%n", currentGenerationIndex);
    }

    protected void iterationEnd() {
    }

    protected void postOffspringCreation(Individual[] offspringPopulation) {
    }

    protected void postNondominatedSorting(List<List<Individual>> fronts) {
    }

    protected void finalize(Individual[] finalPopulation) {
    }

    protected void report(File outputDir) throws IOException {
        HashMap<String, StringBuilder> dumpMap = reportGenerationWiseInfo();
        dumpMap.put("gen_count",
                new StringBuilder(String.valueOf(this.currentGenerationIndex)));
        InputOutput.dumpAll(outputDir, dumpMap);
    }

    public HashMap<String, StringBuilder> reportGenerationWiseInfo() throws IOException {
        // Create the hash map to be returned
        HashMap<String, StringBuilder> dumpMap = new HashMap<>();
        if (DUMP_ALL_GENERATIONS_DECISION_SPACE
                || DUMP_ALL_GENERATIONS_OBJECTIVE_SPACE
                || DUMP_ALL_GENERATIONS_MATLAB_SCRIPTS
                || DUMP_ALL_GENERATIONS_NORMALIZED_MATLAB_SCRIPTS) {

            // <editor-fold desc="Extract non-dominated solutions of the current generation" defaultstate="collapsed">
            // Extract non-dominated solutions of the current generation
            Individual[] individualsToBePrinted
                    = OptimizationUtilities.getNonDominatedIndividuals(
                            this.currentPopulation,
                            epsilon);
            // </editor-fold>

            // <editor-fold desc="Collecting objective-space data" defaultstate="collapsed">
            if (DUMP_ALL_GENERATIONS_OBJECTIVE_SPACE) {
                StringBuilder objSpaceSb = InputOutput.collectObjectiveSpace(
                        optimizationProblem, individualsToBePrinted);
                dumpMap.put("obj.dat", objSpaceSb);
            }
            // </editor-fold>

            // <editor-fold desc="Dumping meta-data" defaultstate="collapsed">
            // Dump meta data
            // This section needs to be modified to write meta-data files in
            // a separate directory.
            // See the desired directiory structure at:
            // D:/corrected plotting script/version_2_11March2015
            if (DUMP_ALL_GENERATIONS_META_DATA) {
                StringBuilder metaDataSb = InputOutput.collectMetaData(
                        this.currentPopulation, hvIdealPoint, hvReferencePoint,
                        this.individualEvaluator.getFunctionEvaluationsCount(),
                        epsilon);
                dumpMap.put("meta.txt", metaDataSb);
            }
            // </editor-fold>

            // <editor-fold desc="Generate NON-normalized Matlab plots for each generation" defaultstate="collapsed">
            if (DUMP_ALL_GENERATIONS_MATLAB_SCRIPTS && optimizationProblem.objectives.length <= 3) {
                // Dump Matlab plotting script of the current generation
                // (ONLY NON_DOMINATED POINTS ARE CONSIDERED HERE)
                // Dump the Matlab plotting script
                StringBuilder sb = null;
                if (optimizationProblem.objectives.length == 2) {
                    sb = InputOutput.createMatlabScript2D(
                            OptimizationUtilities
                                    .getNonDominatedIndividuals(
                                            individualsToBePrinted,
                                            epsilon));
                } else if (optimizationProblem.objectives.length == 3) {
                    sb = InputOutput.createMatlabScript3D(
                            OptimizationUtilities
                                    .getNonDominatedIndividuals(
                                            individualsToBePrinted,
                                            epsilon));
                }
                if (sb != null) {
                    dumpMap.put("matlab.m", sb);
                }
            }
            // </editor-fold>

            // <editor-fold desc="Dumping variable-space data">
            if (DUMP_ALL_GENERATIONS_DECISION_SPACE) {
                // Dump Decision Space
                StringBuilder desSpace = InputOutput.collectRealDecisionSpace(
                        optimizationProblem,
                        individualsToBePrinted);
                dumpMap.put("var.dat", desSpace);
            }
            // </editor-fold>
        }
        // Finally, return the map containing all the information to dump
        return dumpMap;
    }
}
