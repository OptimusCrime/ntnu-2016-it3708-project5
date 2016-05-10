package nsga;

import ea.Settings;
import parser.Map;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Individual {

    // Domination counts
    private ArrayList<Individual> dominatedIndividuals;
    private int dominatedBy;

    // Various values
    private int[] route;
    private Double distance;
    private Double cost;
    private Integer paretoRank;
    private Double crowdingDistance;

    /**
     * Constructors
     */

    public Individual() {
        // Set empty values for domination
        this.dominatedIndividuals = new ArrayList<>();
        this.crowdingDistance = 0.0;

        // Generate random route for this individual
        this.generateRandomRoute();
    }

    /**
     * Constructor with provided dna
     *
     * @param dna Provided DNA for this individual
     */

    public Individual(int[] dna) {
        // Set empty values for domination
        this.dominatedIndividuals = new ArrayList<>();
        this.crowdingDistance = 0.0;

        // Copy our route
        this.route = dna;
    }

    /**
     * Check if this individual dominates another individual
     *
     * @param other The individual we are going to check if we dominates or not
     * @return True if we dominate, false otherwise
     */

    public boolean dominates(Individual other) {
        int dominate = dominatesDistance(other.getDistance()) + dominatesCost(other.getCost());
        return dominate > 0;
    }

    /**
     * Calculate objective function #1 (distance)
     *
     * @return The distance
     */

    public double getDistance() {
        // Only calculate if not already stored
        if (this.distance == null) {
            this.distance = 0.0;

            // Accumulate distances
            for (int i = 1; i < this.route.length; i++) {
                this.distance += Map.getInstance().getDistance(this.route[i - 1], this.route[i]);
            }

            // Add last element to first element to complete the circle
            this.distance += Map.getInstance().getDistance(this.route[this.route.length - 1], this.route[0]);
        }

        // Simply return the distance
        return this.distance;
    }

    /**
     * Calculate objective function #2 (cost)
     *
     * @return The cost
     */

    public double getCost() {
        // Only calculate if not already stored
        if (this.cost == null) {
            this.cost = 0.0;

            // Accumulate costs
            for (int i = 1; i < this.route.length; i++) {
                this.cost += Map.getInstance().getCost(this.route[i - 1], this.route[i]);
            }

            // Add last element to first element to complete the circle
            this.cost += Map.getInstance().getCost(this.route[route.length - 1], this.route[0]);
        }

        // Simply return the score
        return this.cost;
    }

    /**
     * Get this individuals' route/DNA
     *
     * @return The int array (phenotype) for this individual
     */

    public int[] getRoute() {
        return this.route;
    }

    /**
     * Get pareto rank
     *
     * @return The pareto rank
     */

    public int getParetoRank() {
        return paretoRank;
    }

    /**
     * Setter for pareto rank
     *
     * @param paretoRank The pareto rank for this individual
     */

    public void setParetoRank(int paretoRank) {
        this.paretoRank = paretoRank;
    }

    /**
     * Set crowding distance
     *
     * @return The crowding distance
     */

    public double getCrowdingDistance() {
        return crowdingDistance;
    }

    /**
     * Setter for crowding distance
     *
     * @param crowdingDistance The crowding distance for this individual
     */

    public void setCrowdingDistance(double crowdingDistance) {
        this.crowdingDistance = crowdingDistance;
    }

    /**
     * Individuals we dominate
     *
     * @return The list of individuals we dominate
     */

    public ArrayList<Individual> getDominatedIndividuals() {
        return dominatedIndividuals;
    }

    /**
     * Add an individual we dominate
     *
     * @param individual The individual we dominate
     */

    public void addDominatedIndividuals(Individual individual) {
        this.dominatedIndividuals.add(individual);
    }

    /**
     * Return the number of individuals we are dominated by
     *
     * @return The number of individuals we are dominated by
     */

    public int getDominatedBy() {
        return dominatedBy;
    }

    /**
     * Increase the number of individuals we are dominated by
     */

    public void increaseDominatedBy() {
        this.dominatedBy++;
    }

    /**
     * Decrease the number of individuals we are dominated by
     */

    public void decreaseDominatedBy() {
        this.dominatedBy--;
    }

    /**
     * Mutation
     */

    public void mutate() {
        // Note: Probability FOR mutation is used, e.i. 1.0 is always mutate, 0.0 is never
        Random r = new Random();
        if (Settings.mutation < r.nextDouble()) {
            return;
        }

        // Select random chromosome to swap
        int swap1Index = getRandomChromosome();
        int swap2Index = getRandomChromosome();

        // Store the citiIds
        int swap1Value = this.route[swap1Index];
        int swap2Value = this.route[swap2Index];

        // Do the swap
        this.route[swap1Index] = swap2Value;
        this.route[swap2Index] = swap1Value;

        // Reset all values
        this.reset();
    }

    /**
     * Crossover
     *
     * @param other Individual to do crossover with
     * @return The new individuals
     */

    public Individual[] crossover(Individual other) {
        // Note: Probability FOR crossover is used, e.i. 1.0 is always crossover, 0.0 is always clone
        Random r = new Random();
        if (Settings.crossover < r.nextDouble()) {
            Individual[] noOffspring = new Individual[2];
            noOffspring[0] = new Individual(this.getRoute());
            noOffspring[1] = new Individual(this.getRoute());
            return noOffspring;
        }

        int crossover = r.nextInt(Settings.cities - 2) + 1; // Random crossover from 1 to 46 (46+1 is exclusive)

        int[] child1 = this.getRoute().clone();
        int[] child2 = other.getRoute().clone();

        // First child
        for (int i = 0; i < crossover; i++) {
            int outNumber = child1[i];                // Number that will be removed
            int inNumber = other.getRoute()[i];       // Number that will take its place
            child1[i] = inNumber;
            child1 = cleanupCrossover(child1, inNumber, outNumber, i);
        }

        // Second child
        for (int j = 0; j < crossover; j++) {
            int outNumber = child2[j];
            int inNumber = this.getRoute()[j];
            child2[j] = inNumber;
            child2 = cleanupCrossover(child2, inNumber, outNumber, j);
        }

        Individual[] offspring = new Individual[2];
        offspring[0] = new Individual(child1);
        offspring[1] = new Individual(child2);

        return offspring;
    }

    /**
     * Make sure the crossover does not create invalid routes
     *
     * @param dirty Dirty array of cities
     * @param change Number in
     * @param to Number out
     * @param excludeIndex Index to exclude
     * @return The cleaned up crossover array
     */

    private int[] cleanupCrossover(int[] dirty, int change, int to, int excludeIndex) {
        for (int i = 0; i < dirty.length; i++) {
            if (dirty[i] == change && i != excludeIndex) {
                dirty[i] = to;
            }
        }
        return dirty;
    }

    /**
     * Reset values for this individual (important if mutated)
     */

    public void reset() {
        this.distance = null;
        this.cost = null;
        this.paretoRank = null;
        this.crowdingDistance = 0.0;
        this.dominatedIndividuals = new ArrayList<>();
        this.dominatedBy = 0;
    }

    /**
     * Return a random chromosome
     *
     * @return Number from 1 - [number of chromosomes - 1]
     */

    private int getRandomChromosome() {
        Random r = new Random();
        return r.nextInt((Settings.cities - 1) + 1);
    }

    /**
     * Check if we dominate another distance
     *
     * @param otherDistance Other distance to compare to
     * @return True if we dominates this distance, false otherwise
     */

    private int dominatesDistance(double otherDistance) {
        if (this.getDistance() < otherDistance) {
            return 1;
        }
        else if (this.getDistance() == otherDistance) {
            return 0;
        }
        else {
            // Dominated
            return -1;
        }
    }

    /**
     * Check if we dominate another cost
     *
     * @param otherCost Other cost to compare to
     * @return True if we dominates this cost, false otherwise
     */

    private int dominatesCost(double otherCost) {
        if (this.getCost() < otherCost) {
            return 1;
        }
        else if (this.getCost() == otherCost) {
            return 0;
        }
        else {
            // Dominated
            return -1;
        }
    }

    /**
     * Create a random route for this individual
     */

    private void generateRandomRoute() {
        // Create and shuffle the cities to create random DNA
        List<Integer> cities = Stream.iterate(1, n -> n + 1).limit(Settings.cities).collect(Collectors.toList());
        Collections.shuffle(cities);

        // Add as chromosomes
        this.route = new int[Settings.cities];
        int counter = 0;
        for (int cityId : cities) {
            this.route[counter] = cityId;
            counter++;
        }
    }

    /**
     * toString
     *
     * @return Stringification of the object
     */

    public String toString(){
        return "Distance: " + this.getDistance() + " Cost: " + this.getCost() + " DNA: " + Arrays.toString(this.getRoute());
    }
}
