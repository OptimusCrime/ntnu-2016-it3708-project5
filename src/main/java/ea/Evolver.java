package ea;

import nsga.Individual;
import parento.ParetoFront;
import parser.Map;
import sort.Sorter;

import java.util.*;

public class Evolver {

    private int generation;
    private ArrayList<Individual> childPool;
    private ArrayList<Individual> parentPool;
    private ArrayList<ParetoFront> paretoFronts;

    // Tables to penalize similar values
    private HashMap<Double, Boolean> distanceMap;
    private HashMap<Double, Boolean> costMap;

    // Remove duplicates
    private int duplicatesRemoves = 0;

    // All time highest
    private double dist = Double.POSITIVE_INFINITY;
    private double cost = Double.POSITIVE_INFINITY;

    /**
     * Constructor
     */

    public Evolver() {
        // Initialize unique maps
        clearMaps();

        // Load the map to make sure we have the correct pool size loaded
        Map.getInstance();

        // Set current generation
        this.generation = 0;

        // Create empty child and parent pool
        childPool = new ArrayList<>();
        parentPool = new ArrayList<>();
        paretoFronts = new ArrayList<>();
    }

    /**
     * Method for clearing fitness maps
     */

    private void clearMaps() {
        distanceMap = new HashMap<>();
        costMap = new HashMap<>();
    }

    private boolean existingFitness(Individual individual) {
        boolean distance = false;
        boolean cost = false;

        // -- Distance --
        // Check if other individual with this fitness exists
        if (distanceMap.get(individual.getDistance()) != null) {
            distance = true;
        } else {
            distanceMap.put(individual.getDistance(), true);
        }

        // -- Cost --
        // Check if other individual with this fitness exists
        if (costMap.get(individual.getCost()) != null) {
            cost = true;
        } else {
            costMap.put(individual.getCost(), true);
        }

        return (distance && cost);
    }

    /**
     * Main method
     *
     * @param args From sys
     */

    public static void main(String[] args) {
        // Initialize Map Parsing
        Map.getInstance();

        Evolver e = new Evolver();
        e.initialize();
        e.solve();
    }

    /**
     * Init the evolver
     */

    public void initialize() {
        // Add children to initial pool
        for (int i = 0; i < Settings.populationSize; i++) {
            childPool.add(new Individual());
        }

        // Add parents to initial pool
        for (int i = 0; i < Settings.populationSize; i++) {
            parentPool.add(new Individual());
        }
    }

    /**
     * Run one generation
     *
     * @return Returns false if we are finished running our evolver
     */

    public boolean runGeneration() {
        // Clear unique maps
        clearMaps();

        System.out.println("Generation #" + this.generation);
        if (this.generation < Settings.maxGeneration) {
            this.evolve();

            storeBestAndWorst();
            return true;
        }
        storeBestAndWorst();
        return false;
    }

    private void storeBestAndWorst() {
        ArrayList<Individual> bestAndWorst = getBestAndWorst(this.paretoFronts.get(0).getAllMembers());

        // TODO: fix
        double dBest = bestAndWorst.get(1).getDistance();
        double dWorst = bestAndWorst.get(0).getDistance();
        double cBest = bestAndWorst.get(3).getCost();
        double cWorst = bestAndWorst.get(2).getCost();

        if (dBest < dist) {
            dist = dBest;
        }

        if (cBest < cost) {
            cost = cBest;
        }

        System.out.println("----------------------------------------");
        System.out.println("[Distance] BEST: " + dist + " WORST: " + dWorst);
        System.out.println("[Cost]     BEST: " + cost + "   WORST: " + cWorst);
        System.out.println("----------------------------------------");


    }

    /**
     * Solve the problem
     */

    private void solve() {
        // Run n generations
        while (this.generation < Settings.maxGeneration) {
            // Evolve the current generation
            this.evolve();
        }
    }

    /**
     *  Method that removes duplicates
     */

    private void removeDups(ArrayList<Individual> population) {
        ArrayList<Individual> removees = new ArrayList<>();

        for (Individual individual: population) {
            if(individual.getCrowdingDistance() == 0.0) {
                removees.add(individual);
            }
        }

        population.removeAll(removees);
        this.duplicatesRemoves = removees.size();
    }

    /**
     * Implementation of the non-dominated-sort
     *
     * @param population Population to sort
     */

    private void nonDominatedSort(ArrayList<Individual> population) {
        // Resets dominated by and number of fronts it is dominated by
        for (Individual person : population) {
            person.reset();
        }

        // Create the first front
        paretoFronts = new ArrayList<>();
        paretoFronts.add(new ParetoFront(1));

        // Loop the population
        for (Individual current : population) {

            // Loop again
            for (Individual other : population) {
                // Filter out self
                if (current != other) {
                    if (current.dominates(other)) {
                        current.addDominatedIndividuals(other);
                    } else if (other.dominates(current)) {
                        current.increaseDominatedBy();
                    }
                }
            }

            if (current.getDominatedBy() == 0) {
                // Set rank to individual
                current.setParetoRank(1);

                // Add to first front
                paretoFronts.get(0).addMember(current);
            }
        }

        // Set the front counter
        int i = 1;

        // Loop until we have populated all the fronts
        while (i <= paretoFronts.size()) {
            ArrayList<Individual> frontMembers = new ArrayList<>();

            for (Individual current : paretoFronts.get(i - 1).getAllMembers()) {
                for (Individual other : current.getDominatedIndividuals()) {
                    other.decreaseDominatedBy();

                    if (other.getDominatedBy() == 0) {
                        other.setParetoRank(i + 1);
                        frontMembers.add(other);
                    }
                }
            }

            // Increase the front number here
            i++;

            // Create a new empty front
            if (frontMembers.size() > 0) {
                ParetoFront newFront = new ParetoFront(i);
                newFront.addAll(frontMembers);

                // Add to fronts
                paretoFronts.add(newFront);
            }
        }
    }

    /**
     * Parent selection and breeding
     */

    private void parentSelectionAndBreeding() {
        // Copy list of parents
        ArrayList<Individual> potentialParents = new ArrayList<>(this.parentPool);

        // Create empty list of children
        ArrayList<Individual> children = new ArrayList<>();

        // Loop until we have enough children
        while (children.size() < Settings.populationSize + duplicatesRemoves) {
            // Select parents
            Individual mother = selectParent(potentialParents);
            Individual father = selectParent(potentialParents);

            // Create offspring
            Individual[] offspring = mother.crossover(father);

            // Mutate offspring
            offspring[0].mutate();
            offspring[1].mutate();

            // Add to child pool
            children.add(offspring[0]);
            children.add(offspring[1]);
        }

        // Update child pool
        this.childPool = children;
    }

    /**
     * Parent selection with tournament selection
     *
     * @param parents List of potential parents
     * @return The lucky parent!
     */

    @SuppressWarnings("unchecked")
    private Individual selectParent(ArrayList<Individual> parents) {
        // TODO: In place ?
        ArrayList<Individual> candidates = new ArrayList<>(parents);
        Collections.shuffle(candidates);

        // Parent to be returned
        Individual parent = null;

        // Create sublist of parents with tournament size
        ArrayList<Individual> tournament = new ArrayList<>(candidates.subList(0, Math.min(parents.size(), Settings.tournamentSize)));

        // Check if we should select a random
        Random r = new Random();
        if (Settings.e <= r.nextDouble()) {
            // Random selection
            parent = tournament.get(0);
        } else {
            // Select parent with best crowding distance
            Collections.sort(tournament, Sorter.crowdingDistanceComparator());
            parent = tournament.get(0);
        }

        if (Settings.sexualPreventionCrowdingDistance) {
            // If it has a crowdinDistance of 0.0 don't chose it
            if (parent.getCrowdingDistance() == 0.0) {
                return selectParent(parents);
            }
        }
        return parent;

    }

    /**
     * Assign crowding distance to list of individuals
     *
     * @param members Members to calculate distances for
     */

    @SuppressWarnings("unchecked")
    private void crowdingDistanceAssignment(ArrayList<Individual> members) {

        //
        // DISTANCE
        //

        ArrayList<Individual> distanceSorted = new ArrayList<>(members);
        Collections.sort(distanceSorted, Sorter.distanceComparator());

        // Get min/max distances
        double minDistance = distanceSorted.get(0).getDistance();
        double maxDistance = distanceSorted.get(distanceSorted.size() - 1).getDistance();
        double minMaxDistance = maxDistance - minDistance;

        // Make sure to avoid null division
        if (Math.abs(minMaxDistance) < 1e-4) {
            minMaxDistance = 1;
        }

        // Set inf to extremes
        distanceSorted.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
        distanceSorted.get(distanceSorted.size() - 1).setCrowdingDistance(Double.POSITIVE_INFINITY);

        // Set distances for the other individuals in the list
        for (int i = 1; i < distanceSorted.size() - 1; i++) {

            // Get neighbour values
            double previous = distanceSorted.get(i - 1).getDistance();
            double next = distanceSorted.get(i + 1).getDistance();

            // Calculate the new distance
            double distanceCrowdingDistance = (next - previous) / minMaxDistance;
            distanceSorted.get(i).setCrowdingDistance(distanceCrowdingDistance);
        }

        //
        // COST
        //

        ArrayList<Individual> costSorted = new ArrayList<>(members);
        Collections.sort(distanceSorted, Sorter.costComparator());

        // Get min/max cost
        double minCost = costSorted.get(0).getCost();
        double maxCost = costSorted.get(costSorted.size() - 1).getCost();
        double minMaxCost = maxCost - minCost;

        // Make sure to avoid null division
        if (Math.abs(minMaxCost) < 1e-4) {
            minMaxCost = 1;
        }

        // Set inf to extremes
        costSorted.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
        costSorted.get(distanceSorted.size() - 1).setCrowdingDistance(Double.POSITIVE_INFINITY);

        // Set cost for the other individuals in the list
        for (int i = 1; i < costSorted.size() - 1; i++) {
            // Get neighbour values
            double previous = costSorted.get(i - 1).getCost();
            double next = costSorted.get(i + 1).getCost();

            // Get the current crowding distance
            double currentCrowdingDistance = costSorted.get(i).getCrowdingDistance();

            // Calculate the new cost
            double costCrowdingDistance = currentCrowdingDistance + ((next - previous) / minMaxCost);

            // Update the crowding distance
            costSorted.get(i).setCrowdingDistance(costCrowdingDistance);
        }

        if (Settings.fitnessConstraint) {

            // Penalize solution that do not have unique fitness
            // Set crowding distance to 0.0 if fitness is not unique
            for (Individual individual : members) {
                if (existingFitness(individual) && individual.getCrowdingDistance() != Double.POSITIVE_INFINITY) {
                    individual.setCrowdingDistance(0.0);
                } else {
                    double cd = individual.getCrowdingDistance();
                    individual.setCrowdingDistance(cd);
                }
            }
        }

    }

    /**
     * Runs the algorithm one generation
     */

    @SuppressWarnings("unchecked")
    private void evolve() {

        //
        // ADULT SELECTION
        //

        // Combine P and Q in R
        ArrayList<Individual> population = new ArrayList<>(parentPool);
        population.addAll(childPool);

        // Sort population
        this.nonDominatedSort(population);

        // New population
        ArrayList<Individual> newPopulation = new ArrayList<>();
        int counter = 0;

        // Loop until we have filled up the new population
        while ((newPopulation.size() + paretoFronts.get(counter).getSize()) < Settings.populationSize) {

            // Get the members of this fronts
            ArrayList<Individual> paretoMembers = paretoFronts.get(counter).getAllMembers();

            // Assign crowding distance to these members
            this.crowdingDistanceAssignment(paretoMembers);

            // Add the members to the new population
            newPopulation.addAll(paretoMembers);

            // Increase counter
            counter++;

            // Make sure to avoid index out of bounds exception
            if (paretoFronts.size() == counter) {
                break;
            }
        }

        // Assign crowding distance and Sord the remaining members
        this.crowdingDistanceAssignment(paretoFronts.get(counter).getAllMembers());

        // Sort them
        Collections.sort(paretoFronts.get(counter).getAllMembers(), Sorter.crowdingDistanceComparator());

        // Add the remaining individuals from the front members
        newPopulation.addAll(paretoFronts.get(counter).getAllMembers().subList(0,
                (Settings.populationSize - newPopulation.size())));

        // Set new population as parents
        this.parentPool = newPopulation;

        //
        // PARENT SELECTION
        //

        // Remove duplicates
        if(Settings.removeDuplicates) {
            this.removeDups(this.parentPool);
        }

        // Select who gets to mate, and mate them
        this.parentSelectionAndBreeding();

        // Increase the generation number
        generation++;
    }

    /**
     * From a population, pick the best and worst for distance and cost. Used for plotting
     *
     * @param population Population to pick from
     * @return List on this form: 1 - best distance, 0 - worst distance, 3 - best cost, 2 - worst cost
     */

    public static ArrayList<Individual> getBestAndWorst(ArrayList<Individual> population) {
        // Prepopulate
        ArrayList<Individual> bestAndWorst = new ArrayList<>();
        bestAndWorst.add(population.get(0)); // Best distance
        bestAndWorst.add(population.get(0)); // Worst distance
        bestAndWorst.add(population.get(0)); // Best cost
        bestAndWorst.add(population.get(0)); // Worst cost

        // Loop the population
        for (Individual member : population) {
            // Get the values
            double distance = member.getDistance();
            double cost = member.getCost();

            // Check if we should swap out current worst/best
            if (distance > bestAndWorst.get(0).getDistance()) {
                bestAndWorst.set(0, member);
            }
            if (distance < bestAndWorst.get(1).getDistance()) {
                bestAndWorst.set(1, member);
            }
            if (cost > bestAndWorst.get(2).getCost()) {
                bestAndWorst.set(2, member);
            }
            if (cost < bestAndWorst.get(3).getCost()) {
                bestAndWorst.set(3, member);
            }
        }

        // Return the best and worst
        return bestAndWorst;
    }

    /**
     * Get generation as an int
     *
     * @return The generation
     */

    public int getGeneration() {
        return this.generation;
    }

    /**
     * Get all the pareto fronts
     *
     * @return List of pareto fronts
     */

    public ArrayList<ParetoFront> getParetoFronts() {
        return paretoFronts;
    }

    /**
     * Get all children
     *
     * @return List of children
     */

    public ArrayList<Individual> getChildren() {
        return childPool;
    }

    public ArrayList<Individual> getParents() {
        return parentPool;
    }
}
