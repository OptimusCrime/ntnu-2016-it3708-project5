package sort;

import nsga.Individual;

import java.util.Comparator;

public class Sorter {

    /**
     * Compare distances
     *
     * @return The comparator
     */

    public static Comparator distanceComparator() {
        return new Comparator<Individual>() {
            public int compare(Individual one, Individual two) {
                // Order in ascending order (lower is better)
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

    /**
     * Compare cost
     *
     * @return The comparator
     */

    public static Comparator costComparator() {
        return new Comparator<Individual>() {
            public int compare(Individual one, Individual two) {
                // Order in ascending order (lower is better)
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

    /**
     * Compare crowding distances using the following approach. If the two pareto ranks are equal, we compare crowding
     * distance. Higher crowding distance is better. If the ranks are unequal, we want the one with the lowest rank.
     *
     * @return The comparator
     */

    public static Comparator crowdingDistanceComparator() {
        return new Comparator<Individual>() {
            public int compare(Individual one, Individual two) {
                // Check if pareto rank is equal
                if (one.getParetoRank() == two.getParetoRank()) {
                    // Compare crowding distance. Higher is better
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
                    // Compare pareto rank. Lower is better
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
