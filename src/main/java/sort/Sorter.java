package sort;

import nsga.Individual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Sorter {


    public static Comparator distanceComparator() {
        return new Comparator<Individual>() {

            public int compare(Individual one, Individual two) {
                if (one.getDistance() < two.getDistance()) {
                    return -1;
                }
                else if (one.getDistance() > two.getDistance()) {
                    return 1;

                }
                else {
                    return 0;
                }
            }
        };
    }

    public static Comparator costComparator() {
        return new Comparator<Individual>() {

            public int compare(Individual one, Individual two) {
                if (one.getCost() < two.getCost()) {
                    return -1;
                }
                else if (one.getCost() > two.getCost()) {
                    return 1;

                }
                else {
                    return 0;
                }
            }
        };
    }

    public static Comparator crowdingDistanceComparator() {
        return new Comparator<Individual>() {

            public int compare(Individual one, Individual two) {
                if (one.getParetoRank() == two.getParetoRank()) {
                    if (one.getCrowdingDistance() < two.getCrowdingDistance()) {
                        return 1;
                    }
                    else if (one.getCrowdingDistance() > two.getCrowdingDistance()) {
                        return -1;
                    }
                    else {
                        return 0;
                    }
                }
                else {
                    if (one.getParetoRank() < two.getParetoRank()) {
                        return -1;
                    }
                    else {
                        return 1;
                    }
                }
            }
        };
    }

}
