/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * Ship Model Evaluator
 *
 * @author Haitham Seada
 */
public class ShipModelEvaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {
        double[] x = individual.real;

        double L, T, D, Cb, B, V;
        double zeta1 = -10847.2, zeta2 = 12817.0, zeta3 = -6960.32;
        double eta1 = 4977.06, eta2 = -8105.61, eta3 = 4456.51;
        double RoundTripMiles = 5000.0;
        double FuelPrice = 100.0;
        double CargoHandlingRate = 8000.0;
        double gm = 9.8065;

        L = x[0];
        T = x[1];
        D = x[2];
        Cb = x[3];
        B = x[4];
        V = x[5];

        double Fn = V / Math.pow((gm * L), 0.5);
        double displacement = 1.025 * L * B * T * Cb;
        double MassSteel = 0.034 * Math.pow(L, 1.7) * Math.pow(B, 0.7)
                * Math.pow(D, 0.4) * Math.pow(Cb, 0.5);
        double MassOutfit = 1.0 * Math.pow(L, 0.8) * Math.pow(B, 0.6)
                * Math.pow(D, 0.3) * Math.pow(Cb, 0.1);
        double a_Cb = eta1 * Math.pow(Cb, 2.0) + eta2 * Cb + eta3;
        double b_Cb = zeta1 * Math.pow(Cb, 2.0) + zeta2 * Cb + zeta3;
        double SeaDays = RoundTripMiles / (24 * V);
        double BM = (0.085 * Cb - 0.002) * Math.pow(B, 2.0) / (T * Cb);
        double KG = 1.0 + 0.52 * D;
        double KB = 0.53 * T;

        double P = Math.pow(displacement, (2.0 / 3.0)) * Math.pow(V, 3.0)
                / (b_Cb * Fn + a_Cb);
        double GM = KB + BM - KG;

        double ShipCost = 1.3 * (2000.0 * Math.pow(MassSteel, 0.85) + 3500.0
                * MassOutfit + 2400.0 * Math.pow(P, 0.8));
        double MassMachinery = 0.17 * Math.pow(P, 0.9);
        double DailyConsumption = P * 0.19 * 24.0 / 1000.0 + 0.2;

        double FuelCost = 1.05 * DailyConsumption * SeaDays * FuelPrice;
        double LightShipMass = MassSteel + MassMachinery + MassOutfit;
        double CapitalCharges = 0.2 * ShipCost;
        double FuelCarried = DailyConsumption * (SeaDays + 5.0);

        double DeltaDW = displacement - LightShipMass;

        double RunningCosts = 40000.0 * Math.pow(DeltaDW, 0.3);
        double PortCosts = 6.3 * Math.pow(DeltaDW, 0.8);
        double StoresWater = 2.0 * Math.pow(DeltaDW, 0.5);

        double VoyageCosts = FuelCost + PortCosts;
        double DeltaCargo = DeltaDW - FuelCarried - StoresWater;

        double PortDays = 2.0 * (DeltaCargo / CargoHandlingRate + 0.5);

        double RTPA = 350.0 / (SeaDays + PortDays);

        double AnnualCargo = DeltaCargo * RTPA;
        double AnnualCost = CapitalCharges + RunningCosts + VoyageCosts * RTPA;

        double TransportationCost = AnnualCost / AnnualCargo;

        individual.setObjective(0, TransportationCost);
        individual.setObjective(1, LightShipMass / 10000);
        individual.setObjective(2, -AnnualCargo / 1000000);

        // Announce that objective function values are valid
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {

            // Four constraints   
            // Sampling based Reliability measures
            double[] g = new double[13];
            g[0] = L / B - 5.99;
            g[1] = 15.01 - L / D;
            g[2] = 19.01 - L / T;
            g[3] = 0.45 * Math.pow(DeltaDW, 0.31) - T + 0.01;
            g[4] = 0.7 * D + 0.71 - T;
            g[5] = DeltaDW - 2999.99;
            g[6] = 500000.01 - DeltaDW;
            g[7] = Cb - 0.6299;
            g[8] = 0.75 - Cb;
            g[9] = V - 13.99;
            g[10] = 18.01 - V;
            g[11] = 0.32 - Fn;
            g[12] = GM - 0.07 * B + 0.01;

            // Set constraints vilations
            for (int i = 0; i < g.length; i++) {
                if (g[i] < 0) {
                    individual.setConstraintViolation(i, g[i]);
                } else {
                    individual.setConstraintViolation(i, 0);
                }
            }

        }
        // Announce that objective function values are valid
        individual.validConstraintsViolationValues = true;

        // Increase Evaluations Count by One (counted per individual)
        funEvaCount++;
    }

    @Override
    public double[] getReferencePoint() {
        throw new UnsupportedOperationException("This method is unsupported");
    }

    @Override
    public double[] getIdealPoint() {
        throw new UnsupportedOperationException("This method is unsupported");
    }
}
