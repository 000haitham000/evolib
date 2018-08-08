/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package emo;

/**
 * This class represents the exception that is thrown if - in the course of
 * NSGA-II - the crowding distance of a single individual is calculated more
 * than once.
 * 
 * @author Haitham Seada
 */
public class DoubleAssignmentException extends Exception {

    private String message;
    
    public DoubleAssignmentException(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
