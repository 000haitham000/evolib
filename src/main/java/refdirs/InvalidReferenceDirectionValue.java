/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package refdirs;

import emo.Individual;

/**
 * An instance exception of this class is thrown if the individual is assumed to
 * be associated to some reference direction in the course of the algorithm but
 * it actually is not, or its association is outdated.
 *
 * @author Haitham Seada
 */
public class InvalidReferenceDirectionValue extends RuntimeException {

    private Individual individual;

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }

    public InvalidReferenceDirectionValue(Individual aThis) {
    }

    @Override
    public String toString() {
        return "The individual whose reference direction is being retreived is "
                + "either not associated to any direction yet, or his "
                + "association is outdated";
    }
}
