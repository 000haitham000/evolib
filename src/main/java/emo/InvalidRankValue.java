/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package emo;

/**
 * Represents the exception thrown when trying to use a rank value before
 * calculating it or when the rank value is outdated.
 *
 * @author Haitham Seada
 */
public class InvalidRankValue extends RuntimeException {

    private Individual individual;

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }

    public InvalidRankValue(Individual aThis) {
    }

    @Override
    public String toString() {
        return "The individual whose rank is being retreived is either "
                + "unranked yet, or his rank is outdated";
    }
}
