/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emo;

import java.io.Serializable;

/**
 *
 * @author Haitham
 */
public class BinaryVariableSpecs extends NumericVariable {

    private int numberOfBits;

    public BinaryVariableSpecs(
            String name,
            double minValue,
            double maxValue,
            int numberOfBits) {
        super(name, minValue, maxValue);
        this.numberOfBits = numberOfBits;
    }

    /**
     * @return the numberOfBits
     */
    public int getNumberOfBits() {
        return numberOfBits;
    }

    /**
     * @param numberOfBits the numberOfBits to set
     */
    public void setNumberOfBits(int numberOfBits) {
        this.numberOfBits = numberOfBits;
    }

    public String toString() {
        return super.toString()
                + String.format("%3d bits", this.getNumberOfBits());
    }
}
