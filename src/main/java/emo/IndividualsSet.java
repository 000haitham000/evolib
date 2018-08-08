/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package emo;

/**
 * A set if two individuals encapsulated in one object.
 *
 * @author Haitham Seada
 */
public class IndividualsSet {

    Individual individual1;
    Individual individual2;

    public IndividualsSet(Individual individual1, Individual individual2) {
        this.individual1 = individual1;
        this.individual2 = individual2;
    }

    public Individual getIndividual1() {
        return individual1;
    }

    public void setIndividual1(Individual individual1) {
        this.individual1 = individual1;
    }

    public Individual getIndividual2() {
        return individual2;
    }

    public void setIndividual2(Individual individual2) {
        this.individual2 = individual2;
    }
}
