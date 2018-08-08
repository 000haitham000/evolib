/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emo;

/**
 *
 * @author Haitham
 */
public abstract class NumericVariable extends Variable {

    private double minValue;
    private double maxValue;

    public NumericVariable(String name, double minValue, double maxValue) {
        super(name);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * @return the minValue
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * @param minValue the minValue to set
     */
    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    /**
     * @return the maxValue
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * @param maxValue the maxValue to set
     */
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public String toString() {
        return String.format(
                "%-6s (%-10.3f:%10.3f)",
                this.getName(),
                this.getMinValue(),
                this.getMaxValue());
    }

}
