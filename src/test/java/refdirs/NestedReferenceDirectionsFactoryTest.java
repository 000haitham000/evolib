package refdirs;

import org.junit.Assert;
import org.junit.Test;
import utils.InputOutput;
import utils.Mathematics;

import java.io.PrintWriter;
import java.util.List;

import static org.junit.Assert.*;

public class NestedReferenceDirectionsFactoryTest {

    @Test
    public void testRefDirs() {
        int objCount = 3;
        int[] divisionsCountArr = {12, -1, -1, 3};
        NestedReferenceDirectionsFactory nestedReferenceDirectionsFactory
                = new NestedReferenceDirectionsFactory(objCount);
        List<ReferenceDirection> dirs = nestedReferenceDirectionsFactory
                .generateDirections(divisionsCountArr);
        double[] shiftingVector = {0.0,0.5,0.5};
        dirs = nestedReferenceDirectionsFactory.shift(dirs, shiftingVector);
        int passCount = 0;
        int count = 0;
        for (ReferenceDirection dir : dirs) {
            System.out.format("%03d - %s", count, dir.toString());
            try {
                check(dir, objCount);
                System.out.println(" - PASSED");
                passCount++;
            } catch (IllegalArgumentException ex) {
                System.out.println(
                        " - FAILED (coordinates do not sum up to one)");
            }
            count++;
        }
        System.out.format(
                "%n%d passed the validation out of %d%n",
                passCount,
                dirs.size());
        System.out.format(
                "# directions = %d%n%n",
                dirs.size());
        try {
            checkRepetitions(dirs);
            System.out.println("No repeated directions");
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.toString());
        }
        System.out.println();
        InputOutput.printDirectionMatlabCode(
                objCount,
                dirs,
                new PrintWriter(System.out));
    }

    private static void check(ReferenceDirection dir, int objectivesCount) {
        double sum = 0;
        for (int j = 0; j < objectivesCount; j++) {
            sum += dir.direction[j];
        }
        Assert.assertEquals(1.0, sum, 10e-5);
    }

    private static void checkRepetitions(List<ReferenceDirection> dirs) {
        StringBuilder repetitionReport = new StringBuilder();
        repetitionReport.append(String.format("%n"));
        int repetitionCount = 0;
        for (int i = 0; i < dirs.size() - 1; i++) {
            for (int j = i + 1; j < dirs.size(); j++) {
                if (dirs.get(i).equals(dirs.get(j))) {
                    repetitionCount++;
                    repetitionReport.append(String.format(
                            "Directions (%d) & (%d) are repeated: %s%n",
                            i,
                            j,
                            dirs.get(i).toString()));
                }
            }
        }
        Assert.assertEquals(0, repetitionCount);
    }
}