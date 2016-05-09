package parento;

import nsga.Individual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ParentoHelper {

    /**
     * TODO CHECK THIS
     * @param adults
     * @param k
     * @param e
     * @return
     */

    public static Individual crowdingDistanceTournamentSelection(ArrayList<Individual> adults, int k, double e) {
        // Copy and shuffle adults
        ArrayList<Individual> copy = new ArrayList<>(adults);
        Collections.shuffle(copy);

        ArrayList<Individual> tournament = new ArrayList<Individual>(copy.subList(0, k));

        // If passthrough, then return random winner
        Random random = new Random();
        if (random.nextDouble() < e) {
            // Return random winner
            return tournament.get(random.nextInt(tournament.size()));
        } else {
            // Sort the tournament and the best wins
            //Collections.sort(tournament, Sorter.crowdingDistanceComparator());
            return tournament.get(0);
        }


    }

    /**
     * Reset crowding distance for elites or reoccuring individuals
     * TODO check
     * @param population
     */

    private static void resetCrowdingDistance(ArrayList<Individual> population) {
        for (Individual member : population) {
            member.setCrowdDistance(0.0);
        }
    }

    /**
     * TODO
     * @param population
     */

    public static void assignCrowdingDistance(ArrayList<Individual> population) {
        // First reset all crowding distances
        resetCrowdingDistance(population);

        /*
        DISTANCE
         */

        // Copy and sort
        ArrayList<Individual> distanceSorted = new ArrayList<>(population);
        Collections.sort(distanceSorted, Sorter.distanceComparator());

        // Assign distance CD - First & Last is infinity (so extremes are always selected)
        distanceSorted.get(0).setCrowdDistance(Double.MAX_VALUE);
        distanceSorted.get(distanceSorted.size() - 1).setCrowdDistance(Double.MIN_VALUE);

        // Get min and max for normalization
        double minDistance = distanceSorted.get(0).getDistance();
        double maxDistance = distanceSorted.get(distanceSorted.size() - 1).getDistance();

        for (int i = 1; i < distanceSorted.size() - 1; i++) {
            // Get before, current and after, calculate and normalize
            double before = distanceSorted.get(i - 1).getDistance();
            double current = distanceSorted.get(i).getCrowdDistance();
            double after = distanceSorted.get(i + 1).getDistance();
            double distance = current + ((after - before) / (maxDistance - minDistance));

            //System.out.println(distance);

            // Update crowding distance
            distanceSorted.get(i).setCrowdDistance(distance);
        }

        /*
        COST
         */

        // Copy and sort
        ArrayList<Individual> costSorted = new ArrayList<>(population);
        // Collections.sort(costSorted, Sorter.costComparator());

        // Assign cost CD - First & Last is infinity (so extremes are always selected)
        costSorted.get(0).setCrowdDistance(Double.MAX_VALUE);
        costSorted.get(costSorted.size() - 1).setCrowdDistance(Double.MIN_VALUE);

        // Get min and max for normalization
        double minCost = costSorted.get(0).getCost();
        double maxCost = costSorted.get(costSorted.size() - 1).getCost();

        for (int i = 1; i < distanceSorted.size() - 1; i++) {
            // Get before, current and after, calculate and normalize
            double before = costSorted.get(i - 1).getCost();
            double current = costSorted.get(i).getCrowdDistance();
            double after = costSorted.get(i + 1).getCost();
            double cost = current + ((after - before) / (maxCost - minCost));

            // Update crowding distance
            costSorted.get(i).setCrowdDistance(cost);
        }
    }
}