package nsga;

import com.sun.scenario.Settings;
import parser.Map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class Individual {

    /**
     * Fields
     */

    private static double mutation = 0.3;
    private static double crossover = 0.5;
    private static int size = 48;

    // Shared random
    private static Random random;
    // Domination counts
    private ArrayList<Individual> dominatedIndividuals;
    private int dominatedBy;

    private int[] route;
    private Double distance;
    private Double cost;
    private Integer paretoRank;
    private Double crowdingDistance;

    /**
     * Constructors
     */

    public Individual() {
        this.dominatedIndividuals = new ArrayList<Individual>();
        this.crowdingDistance = 0.0;

        // Initialize static random
        if (this.random == null) {
            this.random = new Random();
        }
        generateRandomRoute();
    }

    public Individual(int[] dna) {
        this.dominatedIndividuals = new ArrayList<Individual>();
        this.crowdingDistance = 0.0;

        // Initialize static random
        if (this.random == null) {
            this.random = new Random();
        }

        this.route = dna;
    }

    /**
     * Calculate objective functions
     */

    public double getDistance() {
        if (this.distance == null) {
            this.distance = 0.0;

            // Accumulate distances
            for (int i = 1; i < this.route.length; i++) {
                this.distance += Map.getInstance().getDistance(this.route[i - 1], this.route[i]);
            }
            // Add last element to first element to complete the circle
            this.distance += Map.getInstance().getDistance(this.route[this.route.length - 1], this.route[0]);
        }
        return this.distance;
    }

    public double getCost() {
        if (this.cost == null) {
            this.cost = 0.0;

            // Accumulate costs
            for (int i = 1; i < this.route.length; i++) {
                this.cost += Map.getInstance().getCost(this.route[i - 1], this.route[i]);
            }
            // Add last element to first element to complete the circle
            this.cost += Map.getInstance().getCost(this.route[route.length - 1], this.route[0]);
        }
        return this.cost;
    }

    /**
     * Pareto Methods
     */

    public int getParetoRank() {
        return paretoRank;
    }

    public void setParetoRank(int paretoRank) {
        this.paretoRank = paretoRank;
    }

    public double getCrowdingDistance() {
        return crowdingDistance;
    }

    public void setCrowdingDistance(double crowdingDistance) {
        this.crowdingDistance = crowdingDistance;
    }

    public boolean dominates(Individual other) {
        int dominate = dominatesDistance(other.getDistance()) + dominatesCost(other.getCost());
        return dominate > 0;
    }

    public int[] getRoute() {
        return this.route;
    }

    public ArrayList<Individual> getDominatedIndividuals() {
        return dominatedIndividuals;
    }

    public void addDominatedIndividuals(Individual individual) {
        this.dominatedIndividuals.add(individual);
    }

    public int getDominatedBy() {
        return dominatedBy;
    }

    public void setDominatedBy(int dominatedBy) {
        this.dominatedBy = dominatedBy;
    }

    public void increaseDominatedBy() {
        this.dominatedBy++;
    }

    public void decreaseDominatedBy() {
        this.dominatedBy--;
    }

    /**
     * Genetic operators
     */

    public void mutate() {
        if (random.nextDouble() > mutation) {
            return;
        }

        // Select two random nsga.cities to swap position
        int swap_1 = getRandomChromosone();
        int swap_2 = getRandomChromosone();

        // Store the citiIds
        int swapper_1 = this.route[swap_1];
        int swapper_2 = this.route[swap_2];

        // Do the swap
        this.route[swap_1] = swapper_2;
        this.route[swap_2] = swapper_1;

        // Reset all values
        this.reset();
    }

    public Individual[] crossover(Individual other) {

        // No breeding - return copies of parents
        if (random.nextDouble() > crossover) {
            Individual[] noOffspring = new Individual[2];
            noOffspring[0] = new Individual(this.getRoute());
            noOffspring[1] = new Individual(this.getRoute());
            return noOffspring;
        }

        int crossover = random.nextInt(size - 2) + 1; // Random crossover from 1 to 46 (46+1 is exclusive)

        int[] child1 = this.getRoute();
        int[] child2 = other.getRoute();

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

    public void resetDomination() {
        this.dominatedBy = 0;
        this.dominatedIndividuals = new ArrayList<>();
    }

    /**
     * Helpers
     */

    public void reset() {
        this.distance = null;
        this.cost = null;
        this.paretoRank = null;
        this.crowdingDistance = 0.0;
        this.dominatedIndividuals = new ArrayList<>();
        this.dominatedBy = 0;
    }

    private int[] cleanupCrossover(int[] dirty, int change, int to, int excludeIndex) {
        for (int i = 0; i < dirty.length; i++) {
            if (dirty[i] == change && i != excludeIndex) {
                dirty[i] = to;
            }
        }
        return dirty;
    }

    private int getRandomChromosone() {
        return random.nextInt((size - 1) + 1);

    }

    private int dominatesDistance(double otherDistance) {
        if (this.getDistance() < otherDistance) {
            return 1;
        } else if (this.getDistance() == otherDistance) {
            return 0;
        } else {
            // Dominated
            return -1;
        }
    }

    private int dominatesCost(double otherCost) {
        if (this.getCost() < otherCost) {
            return 1;
        } else if (this.getCost() == otherCost) {
            return 0;
        } else {
            // Dominated
            return -1;
        }
    }

    // Create random dna
    private void generateRandomRoute() {
        // Create and shuffle the nsga.cities
        List<Integer> cities = Stream.iterate(1, n -> n + 1).limit(size).collect(Collectors.toList());

        Collections.shuffle(cities);

        // Add as chromosomes
        this.route = new int[size];
        int counter = 0;
        for (int citiId : cities) {
            this.route[counter] = citiId;
            counter++;
        }
    }
}
