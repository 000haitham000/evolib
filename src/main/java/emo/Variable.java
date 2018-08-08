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
public class Variable implements Serializable {

    private String name;

    public Variable(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format(
                "%-6s",
                this.getName());
    }
}
