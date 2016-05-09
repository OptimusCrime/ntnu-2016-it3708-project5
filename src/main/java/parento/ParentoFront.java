package parento;

import nsga.Individual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParentoFront {

    private ArrayList<Individual> members;
    private int index;

    /**
     * Constructor
     * @param index The layer id
     */

    public ParentoFront(int index) {
        // Create empty list of members
        this.members = new ArrayList<>();

        // The index
        this.index = index;
    }

    /**
     * Add member to the front
     * @param individual Individual to add
     */

    public void addMember(Individual individual) {
        // Add front to this individual
        individual.setParetoFront(this.index);

        // Add this individual to the front
        members.add(individual);
    }

    /**
     * Get member of this front by an id
     * @param index Index to fetch member of
     * @return The member with corresponding index
     */

    public Individual getMember(int index) {
        return this.members.get(index);
    }

    /**
     * Returns number of members of this front
     * @return Size of front
     */

    public int getSize() {
        return this.members.size();
    }

    /**
     * Return all members of this front
     * @return ArrayList with members
     */

    public ArrayList<Individual> getAllMembers() {
        return this.members;
    }

    /**
     * Returns the required number of element from the top of the
     * @param n Number of elements to return
     * @return List of best individuals ranked by crowd distance
     */

    public List<Individual> getNBestByCrowdingDistance(int n) {
        // Assign Crowding Distance if not set for some reason

        // Sort ascending, then reverse, as higher crowding distance is better
        ArrayList<Individual> crowdDistanceSorted = new ArrayList<>(this.members);

        // TODO SORT
        //Collections.sort(crowdDistanceSorted, Sorter.crowdingDistanceComparator());

        // Return the required elements+1, to is exclusive
        return crowdDistanceSorted.subList(0, n);
    }

    /**
     * Method for finding the pareto front from a list of solutions
     * @param population The population to use
     * @param counter TODO
     * @return TODO
     */

    private static Front nonDominatedSolutions(List<Individual> population, int counter) {
        // Find all routes that are not dominated by anyone else in the population (pareto front)
        Front front = new Front(counter);

        // Loop over all to all
        for (Individual currentRoute : population) {
            boolean isDominated = false;

            for (Individual otherRoute : population) {

                if (currentRoute != otherRoute) {
                    // Check if route is dominated by any other route
                    if (otherRoute.getRoute().dominates(currentRoute.getRoute())) {
                        isDominated = true;
                        break;
                    }
                }
            }

            // If it is not dominated, add it is pareto optimal
            if (!isDominated) {
                // Add to pareto front
                front.addMember(currentRoute);
            }
        }

        // Remove from population
        population.removeAll(front.getAllMembers());

        // Remove the final front
        return front;
    }
}
