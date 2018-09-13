/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parsing;

import core.MathExpressionParser;
import core.VariablesManager;
import emo.BinaryVariableSpecs;
import emo.CustomVariableSpecs;
import emo.Individual;
import emo.NumericVariable;
import emo.OptimizationProblem;
import emo.RealVariableSpecs;
import emo.VirtualIndividual;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import types.AbstractNode;

/**
 * This class uses the tx2ex library to parse textual mathematical expressions.
 *
 * @author Haitham Seada
 */
public class IndividualEvaluator implements Serializable {

    private AbstractNode[] objectivesArr;
    private AbstractNode[] constraintsArr;
    private VariablesManager vm = new VariablesManager();
    protected int funEvaCount = 0;

    protected IndividualEvaluator() {
    }

    public IndividualEvaluator(OptimizationProblem problem) {
        // Initialize your variables manager
        vm = new VariablesManager();
        for (int i = 0; i < problem.getVariablesSpecs().length; i++) {
            if (problem.getVariablesSpecs()[i] instanceof NumericVariable) {
                NumericVariable numericVariable
                        = (NumericVariable) problem.getVariablesSpecs()[i];
                vm.set(
                        numericVariable.getName(),
                        numericVariable.getMinValue());
            }
        }
        try {
            // Prepare objectives evaluators for future use
            objectivesArr = new AbstractNode[problem.objectives.length];
            for (int i = 0; i < problem.objectives.length; i++) {
                String func = problem.objectives[i].getExpression();
                objectivesArr[i] = MathExpressionParser.parse(func, vm);
            }
            if (problem.constraints != null) {
                // Prepare constraints evaluators for future use
                constraintsArr = new AbstractNode[problem.constraints.length];
                for (int i = 0; i < problem.constraints.length; i++) {
                    String func = problem.constraints[i].getExpression();
                    constraintsArr[i] = MathExpressionParser.parse(func, vm);
                }
            }
        } catch (Throwable ex) {
            Logger.getLogger(
                    IndividualEvaluator.class.getName()).log(Level.SEVERE, null,
                    ex);
            System.exit(-1);
        }
    }

    public double[] getReferencePoint() {
        throw new UnsupportedOperationException(
                "getReferencePoint() method must be overriden "
                + "with appropriate logic.");
    }

    public double[] getIdealPoint() {
        throw new UnsupportedOperationException(
                "getIdealPoint() method must "
                + "be overriden with appropriate logic.");
    }

    public VirtualIndividual[] getParetoFront(int objectivesCount, int n)
            throws
            InvalidOptimizationProblemException,
            XMLStreamException {
        throw new UnsupportedOperationException(
                "getParetoFront() method must be overriden "
                + "with appropriate logic.");
    }

    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {
        int realCounter = 0;
        int binaryCounter = 0;
        for (int i = 0; i < problem.getVariablesSpecs().length; i++) {
            // Get the name of the variable
            String varName = problem.getVariablesSpecs()[i].getName();
            // Get the value of the variable from the individual
            double value;
            if (problem.getVariablesSpecs()[i] instanceof BinaryVariableSpecs) {
                // If the variable is a binary variable get its corresponding
                // decimal value.
                value = individual.binary[binaryCounter].getDecimalValue();
                binaryCounter++;
            } else if (problem.getVariablesSpecs()[i] instanceof RealVariableSpecs) {
                // If the variable is real get its value directly
                value = individual.real[realCounter];
                realCounter++;
            } else  if (problem.getVariablesSpecs()[i] instanceof CustomVariableSpecs) {
                value = Double.NaN; // for custom values
            } else {
                throw new UnsupportedOperationException("Unknown variable type:"
                        + " Only real, custom and binary are supported.");
            }
            // Replace each variable in the expression with its value
            vm.set(varName, value);
        }
        // Evaluate the final expression and store the results as the
        // individual's objective values.
        for (int i = 0; i < problem.objectives.length; i++) {
            individual.setObjective(i, objectivesArr[i].evaluate());
        }
        // Increase Evaluations Count by One (counted per individual)
        funEvaCount++;
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            // Evaluate the final expression and store the results as the 
            // individual's constraints values.
            for (int i = 0; i < problem.constraints.length; i++) {
                double cv = constraintsArr[i].evaluate();
                if (cv > 0) {
                    cv = 0;
                }
                individual.setConstraintViolation(i, cv);
            }
            // Announce that objective function values are valid
            individual.validConstraintsViolationValues = true;
        }
    }

    /**
     * @return the funEvaCount
     */
    public int getFunctionEvaluationsCount() {
        return funEvaCount;
    }

    public void setFunctionEvaluationsCount(int individualEvaluationsCount) {
        this.funEvaCount = individualEvaluationsCount;
    }

    public void resetFunctionEvaluationsCount() {
        funEvaCount = 0;
    }
}
