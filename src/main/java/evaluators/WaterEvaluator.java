/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Water test problem
 *
 * @author Haitham Seada
 */
public class WaterEvaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {

        double[] x = individual.real;
        double obj0 = 106780.37*(x[1]+x[2])+61704.67;
        double obj1 = 3000.0 * x[0];
        double obj2 = 305700*2289*x[1]/Math.pow(0.06 *2289,0.65);
        double obj3 = 250*2289*Math.exp(-39.75*x[1]+9.9*x[2]+2.74);
        double obj4 = 25*(1.39/(x[0]*x[1])+4940*x[2]-80);
        individual.setObjective(0, obj0);
        individual.setObjective(1, obj1);
        individual.setObjective(2, obj2);
        individual.setObjective(3, obj3);
        individual.setObjective(4, obj4);
        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            
            // Four constraints
            double[] g = new double[7];
            g[0] = 1.0-0.00139/(x[0]*x[1])-4.94*x[2]+0.08;
            g[1] = 1.0-0.000306/(x[0]*x[1])-1.082*x[2]+0.0986;
            g[2] = 50000.0-12.307/(x[0]*x[1])-49408.24*x[2]-4051.02;
            g[3] = 16000.0-2.098/(x[0]*x[1])-8046.33*x[2]+696.71;
            g[4] =10000.0-2.138/(x[0]*x[1])-7883.39*x[2]+705.04;
            g[5] =2000.0-0.417*(x[0]*x[1])-1721.26*x[2]+136.54;
            g[6] =550.0-0.164/(x[0]*x[1])-631.13*x[2]+54.48;
            // Set constraints vilations
            for (int i = 0; i < g.length; i++) {
                if(g[i] < 0) {
                    individual.setConstraintViolation(i, g[i]);
                } else {
                    individual.setConstraintViolation(i, 0);
                }
            }
            // Announce that objective function values are valid
            individual.validConstraintsViolationValues = true;
        }
    }

    @Override
    public double[] getReferencePoint() {
        throw new UnsupportedOperationException(
                "Nadir point is not defined for this problem.");
    }

    @Override
    public double[] getIdealPoint() {
        throw new UnsupportedOperationException(
                "Ideal point is not defined for this problem.");
    }
}
