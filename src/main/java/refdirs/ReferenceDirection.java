/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package refdirs;

import utils.Mathematics;
import emo.Individual;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An instance of this class represents one reference direction.
 *
 * @author Haitham Seada
 */
public class ReferenceDirection implements Serializable {

    public static final double DIR_EPSILON = 10e-5;

    public double[] direction;
    public List<Individual> surroundingIndividuals
            = new ArrayList<>();

    /*
    public double[] getDirection() {
        return direction;
    }

    public void setDirection(double[] direction) {
        this.direction = direction;
    }
     */
    public ReferenceDirection(double[] direction) {
        for (int i = 0; i < direction.length; i++) {
            direction[i] = Mathematics.approximate(
                    direction[i],
                    Mathematics.getPrecisionDecimalPlacesCount(DIR_EPSILON));
        }
        this.direction = direction;
    }

    @Override
    public String toString() {
        String result = "(";
        for (int i = 0; i < direction.length; i++) {
            result += String.format("%5.2f", direction[i]);
            if (i != direction.length - 1) {
                result += ",";
            }
        }
        result += ")";
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ReferenceDirection other = (ReferenceDirection) obj;
        for (int i = 0; i < this.direction.length; i++) {
            // Relax the precision a bit. See the comment inside the hashcode
            // function for more details.
            double approxValue1 = Mathematics.approximate(
                    this.direction[i],
                    Mathematics.getPrecisionDecimalPlacesCount(DIR_EPSILON));
            double approxValue2 = Mathematics.approximate(
                    other.direction[i],
                    Mathematics.getPrecisionDecimalPlacesCount(DIR_EPSILON));
            if (Mathematics.compare(
                    approxValue1,
                    approxValue2) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        // Approximate the values
        double[] temp = Arrays.copyOf(this.direction, this.direction.length);
        for (int i = 0; i < temp.length; i++) {
            temp[i] = Mathematics.approximate(
                    temp[i],
                    Mathematics.getPrecisionDecimalPlacesCount(DIR_EPSILON) // - 1
            /* It was found that using the exact same precision used in the
                    enitre application is is too much!
            Two equal reference directions will be considered different just
            because their coordinates are not exactly the same. For example,
            In a two objectives problem the following two directions will be
            considered different if the default precision EPSILON is 1e-10:
            (0.0000000001,0.9999999999) & (0.0000000000,1.0000000000)
            This kind of relaxed accuracy is necessary when trying to remove
            duplicate directions in the NestedReferenceDirectionsFactory class.
             */);
        }
        int hash = 7;
        hash = 29 * hash + Arrays.hashCode(temp);
        return hash;
    }
}
