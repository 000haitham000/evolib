/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parsing;

/**
 * An exception instance of this class is thrown if the XML input file
 * representing the optimization has invalid inputs(s).
 *
 * @author Haitham Seada
 */
public class InvalidOptimizationProblemException extends Exception {

    private String message;

    public InvalidOptimizationProblemException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Invalid Optimization Problem: " + getMessage();
    }
}
