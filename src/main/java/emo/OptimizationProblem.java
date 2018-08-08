/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package emo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import parsing.InvalidOptimizationProblemException;
import utils.RandomNumberGenerator;

/**
 * This class represents an optimization problem. Parsing input into an object
 * of this class performed elsewhere.
 *
 * @author Haitham Seada
 */
public class OptimizationProblem implements Serializable {

    public static final int SINGLEOBJECTIVE = 0;
    public static final int MULTIOBJECTIVE = 1;
    public static final int MANYOBJECTIVE = 2;

    private String problemID;
    private int type;
    private double seed;
    public Variable[] variablesSpecs;
    public Objective[] objectives;
    public Constraint[] constraints;
    private int steps;
    private boolean adaptive;
    private int populationSize;
    private int generationsCount;
    private double realCrossoverProbability;
    private double realMutationProbability;
    private int realCrossoverDistIndex;
    private int realMutationDistIndex;
    private double binaryCrossoverProbability;
    private double binaryMutationProbabilty;
    private double customCrossoverProbability;
    private double customMutationProbability;

    public OptimizationProblem(
            String id,
            Variable[] variables,
            Objective[] objectives,
            Constraint[] constraints,
            int steps,
            boolean adaptive,
            int populationSize,
            int generationsCount,
            double realCrossoverProbabiltiy,
            int realCrossoverDistributionIndex,
            double realMutationProbability,
            int realMutationDistributionIndex,
            double binaryCrossoverProbability,
            double binaryMutationProbability,
            double customCrossoverProbability,
            double customMutationProbability,
            double seed) {
        this.problemID = id;
        this.variablesSpecs = variables;
        this.objectives = objectives;
        this.constraints = constraints;
        this.steps = steps;
        this.adaptive = adaptive;
        this.populationSize = populationSize;
        this.generationsCount = generationsCount;
        this.realCrossoverProbability = realCrossoverProbabiltiy;
        this.realCrossoverDistIndex = realCrossoverDistributionIndex;
        this.realMutationProbability = realMutationProbability;
        this.realMutationDistIndex = realMutationDistributionIndex;
        this.binaryCrossoverProbability = binaryCrossoverProbability;
        this.binaryMutationProbabilty = binaryMutationProbability;
        this.customCrossoverProbability = customCrossoverProbability;
        this.customMutationProbability = customMutationProbability;
        this.seed = seed;
    }

    /**
     * @return the id
     */
    public String getProblemID() {
        return problemID;
    }

    /**
     * @param id the id to set
     */
    public void setProblemID(String id) {
        this.problemID = id;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the populationSize
     */
    public int getPopulationSize() {
        return populationSize;
    }

    /**
     * @param populationSize the populationSize to set
     */
    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    /**
     * @return the generationsCount
     */
    public int getGenerationsCount() {
        return generationsCount;
    }

    /**
     * @param generationsCount the generationsCount to set
     */
    public void setGenerationsCount(int generationsCount) {
        this.generationsCount = generationsCount;
    }

    /**
     * @return the sbxCrossoverProbability
     */
    public double getRealCrossoverProbability() {
        return realCrossoverProbability;
    }

    /**
     * @param sbxCrossoverProbability the sbxCrossoverProbability to set
     */
    public void setRealCrossoverProbability(double sbxCrossoverProbability) {
        this.realCrossoverProbability = sbxCrossoverProbability;
    }

    /**
     * @return the sbxMutationProbability
     */
    public double getRealMutationProbability() {
        return realMutationProbability;
    }

    /**
     * @param sbxMutationProbability the sbxMutationProbability to set
     */
    public void setRealMutationProbability(double sbxMutationProbability) {
        this.realMutationProbability = sbxMutationProbability;
    }

    /**
     * @return the sbxCrossoverDistIndex
     */
    public int getRealCrossoverDistIndex() {
        return realCrossoverDistIndex;
    }

    /**
     * @param sbxCrossoverDistIndex the sbxCrossoverDistIndex to set
     */
    public void setRealCrossoverDistIndex(int sbxCrossoverDistIndex) {
        this.realCrossoverDistIndex = sbxCrossoverDistIndex;
    }

    /**
     * @return the sbxMutationDistIndex
     */
    public int getRealMutationDistIndex() {
        return realMutationDistIndex;
    }

    /**
     * @param sbxMutationDistIndex the sbxMutationDistIndex to set
     */
    public void setRealMutationDistIndex(int sbxMutationDistIndex) {
        this.realMutationDistIndex = sbxMutationDistIndex;
    }

    /**
     * @return the binaryCrossoverProbability
     */
    public double getBinaryCrossoverProbability() {
        return binaryCrossoverProbability;
    }

    /**
     * @param binaryCrossoverProbability the binaryCrossoverProbability to set
     */
    public void setBinaryCrossoverProbability(double binaryCrossoverProbability) {
        this.binaryCrossoverProbability = binaryCrossoverProbability;
    }

    /**
     * @return the binaryMutationProbabilty
     */
    public double getBinaryMutationProbabilty() {
        return binaryMutationProbabilty;
    }

    /**
     * @param binaryMutationProbabilty the binaryMutationProbabilty to set
     */
    public void setBinaryMutationProbabilty(double binaryMutationProbabilty) {
        this.binaryMutationProbabilty = binaryMutationProbabilty;
    }

    /**
     * @return the seed
     */
    public double getSeed() {
        return seed;
    }

    /**
     * @param seed the seed to set
     */
    public void setSeed(double seed) {
        this.seed = seed;
        RandomNumberGenerator.setSeed(seed);
    }

    /**
     * @return the steps
     */
    public int getSteps() {
        return steps;
    }

    /**
     * @param steps the steps to set
     */
    public void setSteps(int steps) {
        this.steps = steps;
    }

    /**
     * @return the adaptive
     */
    public boolean isAdaptive() {
        return adaptive;
    }

    /**
     * @param adaptive the adaptive to set
     */
    public void setAdaptive(boolean adaptive) {
        this.adaptive = adaptive;
    }

    private String getVariablesAsString() {
        // Variables string
        String variablesString = "Variables {";
        for (int i = 0; i < variablesSpecs.length; i++) {
            String varType;
            if (variablesSpecs[i] instanceof BinaryVariableSpecs) {
                int bitsCountIfBinary
                        = ((BinaryVariableSpecs) variablesSpecs[i])
                                .getNumberOfBits();
                varType = String.format("binary:%d bits", bitsCountIfBinary);
            } else if (variablesSpecs[i] instanceof RealVariableSpecs) {
                varType = "real";
            } else if (variablesSpecs[i] instanceof CustomVariableSpecs) {
                varType = "custom";
            } else {
                varType = "unknown";
            }
            if (variablesSpecs[i] instanceof NumericVariable) {
                NumericVariable numericVariable
                        = (NumericVariable) variablesSpecs[i];
                variablesString
                        += String.format(
                                "%s(%s) %8.3f:%-8.3f",
                                numericVariable.getName(),
                                varType,
                                numericVariable.getMinValue(),
                                numericVariable.getMaxValue());
            }
            if (i < variablesSpecs.length - 1) {
                variablesString += ", ";
            }
        }
        variablesString += "}";
        return variablesString;
    }

    private String getObjectivesAsString() {
        // Objectives string
        String objectivesString = "Objectives {";
        for (int i = 0; i < objectives.length; i++) {
            objectivesString += String.format(
                    "%s %s",
                    (objectives[i].getType() == Objective.MIN) ? "Min." : "Max.",
                    objectives[i].getExpression());
            if (i < objectives.length - 1) {
                objectivesString += ", ";
            }
        }
        objectivesString += "}";
        return objectivesString;
    }

    private String getConstraintsAsString() {
        // Constraints string
        String constraintsString = "Constraints {";
        for (int i = 0; i < constraints.length; i++) {
            constraintsString += String.format(
                    "%s %s",
                    (constraints[i].getType() == Constraint.INEQUALITY) ? "Ineq." : "Eq.",
                    constraints[i].getExpression());
            if (i < constraints.length - 1) {
                constraintsString += ", ";
            }
        }
        constraintsString += "}";
        return constraintsString;
    }

    public int getRealVariablesCount() {
        int realVarCount = 0;
        for (Variable variablesSpec : variablesSpecs) {
            if (variablesSpec instanceof RealVariableSpecs) {
                realVarCount++;
            }
        }
        return realVarCount;
    }

    public int getBinaryVariablesCount() {
        int binaryVarCount = 0;
        for (Variable variablesSpec : variablesSpecs) {
            if (variablesSpec instanceof BinaryVariableSpecs) {
                binaryVarCount++;
            }
        }
        return binaryVarCount;
    }

    public int getCustomVariablesCount() {
        int customVarCount = 0;
        for (Variable variablesSpec : variablesSpecs) {
            if (variablesSpec instanceof CustomVariableSpecs) {
                customVarCount++;
            }
        }
        return customVarCount;
    }

    public static class Objective implements Serializable {

        public static final int MIN = 0;
        public static final int MAX = 1;

        private String expression;
        private int type;

        /**
         * @return the objectiveExpression
         */
        public String getExpression() {
            return expression;
        }

        /**
         * @param objectiveExpression the objectiveExpression to set
         */
        public void setObjectiveExpression(String objectiveExpression) {
            this.expression = objectiveExpression;
        }

        /**
         * @return the type
         */
        public int getType() {
            return type;
        }

        /**
         * @param type the type to set
         */
        public void setType(int type) {
            this.type = type;
        }

        public Objective(int type, String expression) {
            this.type = type;
            this.expression = expression;
        }

        @Override
        public String toString() {
            return String.format(
                    "%-3s: %-50s", (type == MIN) ? "MIN" : "MAX", expression);
        }
    }

    public static class Constraint implements Serializable {

        public static final int INEQUALITY = 0;
        public static final int EQUALITY = 1;

        private String expression;
        private int type;

        /**
         * @return the expression
         */
        public String getExpression() {
            return expression;
        }

        /**
         * @param expression the expression to set
         */
        public void setExpression(String expression) {
            this.expression = expression;
        }

        /**
         * @return the type
         */
        public int getType() {
            return type;
        }

        /**
         * @param type the type to set
         */
        public void setType(int type) {
            this.type = type;
        }

        public Constraint(int type, String expression) {
            this.type = type;
            this.expression = expression;
        }

        @Override
        public String toString() {
            return String.format(
                    "%-3s: %-50s",
                    (getType() == INEQUALITY) ? "INEQUALITY" : "EQUALITY",
                    getExpression());
        }
    }

    /**
     * This functions creates a default optimization problem instance directly
     * using the data passed to it as arguments. In other words, unlike
     * <em>StaxParser.readProblem(InputStream xmlIn)</em> this method does not
     * parse an XML file. The returned default problem is a minimization
     * unconstrained problem and all its variables are real (no binary
     * variables). Reference directions are fixed throughout the whole
     * optimization run.
     *
     * @param realVarMinValues all variables lower bounds
     * @param realVarMaxValues all variables upper bound
     * @param objCount number of objectives
     * @param conCount number of constraints
     * @param steps steps required to initialize evenly distributed reference
     * directions in a multiobjective optimization objective space using Das and
     * Dennis approach.
     * @param populationSize Population size
     * @param generationsCount Maximum number of generations
     * @param realCrossoverDistributionIndex eta_c parameter of the Simulated
     * Binary Crossover (SBX) operator
     * @param realMutationDistributionIndex eta_m parameter of the Polynomial
     * Mutation (PM) operator
     * @param seed Random seed. Notice that a Zero value is not allowed. If a
     * Zero value is passed another one (in the range [0,1]) is generated
     * @param realCrossoverProbability Probability of real crossover (SBX)
     * @param realMutationProbability Probability of real mutation (PM)
     * @param problemID A label representing the problem
     * @return
     * @throws InvalidOptimizationProblemException
     */
    public static OptimizationProblem getDefaultRealProblem(
            double[] realVarMinValues,
            double[] realVarMaxValues,
            int objCount,
            int conCount,
            int steps,
            int populationSize,
            int generationsCount,
            int realCrossoverDistributionIndex,
            int realMutationDistributionIndex,
            double seed, // Zero values are not allowed. If a Zero value is 
            // passed another one is generated instead (between Zero and One)
            double realCrossoverProbability,
            double realMutationProbability,
            String problemID
    ) throws InvalidOptimizationProblemException {

        // <editor-fold defaultstate="collapsed" desc=" Create necessary variablesSpecs ">
        // Initialize 3 lists & arrays for variablesSpecs, objectives and constraints
        List<NumericVariable> varList
                = new ArrayList<>();
        List<OptimizationProblem.Objective> objList
                = new ArrayList<>();
        List<OptimizationProblem.Constraint> consList
                = new ArrayList<>();

        NumericVariable[] variables;
        OptimizationProblem.Objective[] objectives;
        OptimizationProblem.Constraint[] constraints;
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Intialize a default random seed ">
        do {
            seed = new Random().nextDouble();
        } while (seed == 0); // Just to make sure that the seed is not Zero (which is very Un-likely)
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Variables ">
        // Variables (all real)
        for (int i = 0; i < realVarMinValues.length; i++) {
            // Add this variable to the list of variablesSpecs
            varList.add(new RealVariableSpecs(
                    "x" + i,
                    realVarMinValues[i],
                    realVarMaxValues[i]));
        }
        variables = new NumericVariable[varList.size()];
        varList.toArray(variables);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Objectives ">        
        // Objectives (all minimization)
        for (int i = 0; i < objCount; i++) {
            // Add this objective to the list of objectives
            objList.add(new OptimizationProblem.Objective(OptimizationProblem.Objective.MIN, "0"));
        }
        objectives = new OptimizationProblem.Objective[objList.size()];
        objList.toArray(objectives);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Constraints ">
        // Constraints (all inequalities in the form g(x) >= 0
        for (int i = 0; i < conCount; i++) {
            consList.add(new OptimizationProblem.Constraint(OptimizationProblem.Constraint.INEQUALITY, "0"));
        }
        constraints = new OptimizationProblem.Constraint[consList.size()];
        consList.toArray(constraints);
        // </editor-fold>

        // Create the optimization problem object
        OptimizationProblem optimizationProblem
                = new OptimizationProblem(
                        problemID,
                        variables,
                        objectives,
                        constraints,
                        steps,
                        false, // Reference directions are fixed by default
                        populationSize,
                        generationsCount,
                        realCrossoverProbability,
                        realCrossoverDistributionIndex,
                        realMutationProbability,
                        realMutationDistributionIndex,
                        0.0, // No binary variables
                        0.0, // No binary variables
                        0.0, // No custom variables
                        0.0, // No custom variables
                        seed);
        // Check for data integrity
        checkOptimizationProblem(optimizationProblem);
        // Return the optimization problem
        return optimizationProblem;
    }

    public static void checkOptimizationProblem(
            OptimizationProblem optimizationProblem)
            throws
            InvalidOptimizationProblemException {
        String message = null;
        if (optimizationProblem.getPopulationSize() <= 1) {
            message = "Population size must be > 1";
        } else if (optimizationProblem.getGenerationsCount() <= 0) {
            message = "Number of generations must be > 0";
        } else if (optimizationProblem.getRealCrossoverProbability() < 0
                || optimizationProblem.getRealCrossoverProbability() > 1) {
            message = "Real crossover probability must be in the range [0,1]";
        } else if (optimizationProblem.getRealMutationProbability() < 0
                || optimizationProblem.getRealMutationProbability() > 1) {
            message = "Real mutation probability must be in the range [0,1]";
        } else if (optimizationProblem.getBinaryCrossoverProbability() < 0
                || optimizationProblem.getBinaryCrossoverProbability() > 1) {
            message = "Binary crossover probability must be in the range [0,1]";
        } else if (optimizationProblem.getBinaryMutationProbabilty() < 0
                || optimizationProblem.getBinaryMutationProbabilty() > 1) {
            message = "Binary mutation probability must be in the range [0,1]";
        } else if (optimizationProblem.getRealCrossoverDistIndex() < 0) {
            message = "Real crossover distribution index cannot be negative";
        } else if (optimizationProblem.getRealMutationDistIndex() < 0) {
            message = "Real mutation distribution index cannot be negative";
        } else if (optimizationProblem.getSeed() < 0
                || optimizationProblem.getSeed() > 1) {
            message = "The seed must be in the range [0,1]";
        } else if (optimizationProblem.variablesSpecs.length == 0) {
            message = "There must be at least one variable in the "
                    + "optimization problem";
        } else if (optimizationProblem.objectives.length == 0) {
            message = "There must be at least one objective in the "
                    + "optimization problem";
        } else if (optimizationProblem.variablesSpecs.length > 0) {
            for (int i = 0; i < optimizationProblem.variablesSpecs.length; i++) {
                if (optimizationProblem.variablesSpecs[i] instanceof NumericVariable) {
                    NumericVariable numericVariable
                            = (NumericVariable) optimizationProblem.variablesSpecs[i];
                    if (numericVariable.getMaxValue()
                            < numericVariable.getMinValue()) {
                        message = String.format(
                                "Variable(%s): minimum value(%10.4f) cannot be "
                                + "greater than maximum value(%10.4f)",
                                numericVariable.getName(),
                                numericVariable.getMinValue(),
                                numericVariable.getMaxValue());
                        break;
                    }
                }
                if (optimizationProblem.variablesSpecs[i] instanceof BinaryVariableSpecs) {
                    if (((BinaryVariableSpecs) optimizationProblem.variablesSpecs[i])
                            .getNumberOfBits() < 1) {
                        BinaryVariableSpecs binVar
                                = ((BinaryVariableSpecs) optimizationProblem.variablesSpecs[i]);
                        message = String.format(
                                "Variable(%s): number of bits(%d) must be "
                                + "greater than 0",
                                binVar.getName(),
                                binVar.getNumberOfBits());
                        break;
                    }
                }
            }
        }
        if (message != null) {
            throw new InvalidOptimizationProblemException(message);
        }
    }

    @Override
    public String toString() {
        String variablesString = getVariablesAsString();
        String objectivesString = getObjectivesAsString();
        String constraintsString = getConstraintsAsString();
        return String.format("%s - %s - %s - "
                + "steps: %d - "
                + "population: %d - "
                + "generations: %d - "
                + "realCrossoverProb: %7.2f - "
                + "realMutationProb: %7.2f - "
                + "realCrossoverDistributionIndex: %d - "
                + "realMutationDistributionIndex: %d - "
                + "binaryCrossoverProb: %7.2f - "
                + "binaryMutationProb: %7.2f - "
                + "seed: %7.2f",
                variablesString,
                objectivesString,
                constraintsString,
                getSteps(),
                populationSize,
                generationsCount,
                realCrossoverProbability,
                realMutationProbability,
                realCrossoverDistIndex,
                realMutationDistIndex,
                binaryCrossoverProbability,
                binaryMutationProbabilty,
                seed);
    }

    /**
     * @return the customCrossoverProbability
     */
    public double getCustomCrossoverProbability() {
        return customCrossoverProbability;
    }

    /**
     * @param customCrossoverProbability the customCrossoverProbability to set
     */
    public void setCustomCrossoverProbability(double customCrossoverProbability) {
        this.customCrossoverProbability = customCrossoverProbability;
    }

    /**
     * @return the customMutationProbability
     */
    public double getCustomMutationProbability() {
        return customMutationProbability;
    }

    /**
     * @param customMutationProbability the customMutationProbability to set
     */
    public void setCustomMutationProbability(double customMutationProbability) {
        this.customMutationProbability = customMutationProbability;
    }
}
