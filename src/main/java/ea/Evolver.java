package ea;

import nsga.Individual;
import parento.ParentoFront;

import java.util.ArrayList;

public class Evolver {

    private int generation;
    private ArrayList<Individual> childPool;
    private ArrayList<Individual> parentPool;
    private Individual bestIndividual;

    /**
     * Constructor
     */

    public Evolver() {
        // Set current generation
        this.generation = 0;

        // Create empty child and parent pool
        childPool = new ArrayList<>();
        parentPool = new ArrayList<>();
    }

    /**
     * Main method
     * @param args From sys
     */

    public static void main(String[] args) {
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
     * Solve the problem
     */

    public void solve() {
        // Run n generations
        while (this.generation < Settings.maxGeneration) {
            // Evolve the current generation
            this.evolve();

            // Debug
            System.out.println("Gen: " + this.generation + " " + this.bestIndividual);
        }
    }

    /**
     * Adult selection
     */

    private void adultSelection() {
        // Combine population Q + P = R
        ArrayList<Individual> population = new ArrayList<>();
        population.addAll(childPool);

        // Divide into pareto fronts
        ArrayList<ParentoFront> nonDominatedFronts = rankParetoSort(population);

        // Clean the adult pool
        parentPool = new ArrayList<>();

        // Parento pointer number
        int paretoFrontPointer = 0;

        // Loop until we have gone through all the adults
        while ((parentPool.size() + nonDominatedFronts.get(paretoFrontPointer).getSize()) < Settings.populationSize)) {
            // Get best front
            ParentoFront front = nonDominatedFronts.get(paretoFrontPointer);

            // Add front members to population
            parentPool.addAll(front.getAllMembers());

            // Increment pointer
            paretoFrontPointer++;
        }

        // Add parents from last front if required, these compete with crowding distance
        int k = Settings.populationSize - parentPool.size();
        if (k > 0) {
            parentPool.addAll(nonDominatedFronts.get(paretoFrontPointer).getNBestByCrowdingDistance(k));
        }
    }

    /**
     * Parent selection and breeding
     */

    private void parentSelectionAndBreeding() {
        // Select breeders and breed children until child population is filled
        this.childPool = new ArrayList<>();

        // Loop until we have filled up the child pool
        while (this.childPool.size() < Settings.populationSize) {
            // Create new list with the remaining parents
            ArrayList<Individual> parents = new ArrayList<>(this.parentPool);

            // Pick the first parent
            Individual parent1 = ParetoHelper.crowdingDistanceTournamentSelection(parents, Settings.tournamentSize, Settings.e);
            parents.remove(parent1);

            // Pick the second parent
            Individual parent2 = ParetoHelper.crowdingDistanceTournamentSelection(parents, Settings.tournamentSize, Settings.e);

            // Breed
            Individual[] offspring = parent1.breed(parent2);

            // Check if we should create one or two children
            if ((this.childPool.size() + 2) > Settings.populationSize) {
                // Create one child
                this.childPool.add(offspring[0]);
            }
            else {
                // Create two children
                this.childPool.add(offspring[0]);
                this.childPool.add(offspring[1]);
            }
        }
    }

    /**
     * Runs the algorithm one generation
     */

    public void evolve() {
        // Select who survives into adulthood
        this.adultSelection();

        // Log the best individual
        this.logBestIndividual();

        // Select who gets to mate, and mate them
        this.parentSelectionAndBreeding();

        // Increase the generation number
        generation++;
    }

    /**
     * Logs the best individual for each generation
     */

    private void logBestIndividual() {
        // Get the first individual
        bestIndividual = this.parentPool.get(0);

        for (Individual individual : this.parentPool) {
            // Check if this individual is better than best
        }
    }
}
