/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package emo;

/**
 * Represents the exception thrown when trying to use an objective value before
 * calculating it or when the objective value is outdated.
 *
 * @author Haitham Seada
 */
public class InvalidObjectiveValue extends RuntimeException {

    public InvalidObjectiveValue() {
        super("The objective function being retreived is either not "
                + "calculated yet or outdated.");
    }

    public InvalidObjectiveValue(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
