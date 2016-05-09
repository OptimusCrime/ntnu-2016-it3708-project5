package ea;

import nsga.Individual;
import parento.ParetoFront;
import parser.Map;
import sort.Sorter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

public class Evolver {

    private int generation;
    private ArrayList<Individual> childPool;
    private ArrayList<Individual> parentPool;
    private ArrayList<ParetoFront> paretoFronts;
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
        paretoFronts = new ArrayList<>();
    }

    /**
     * Main method
     * @param args From sys
     */

    public static void main(String[] args) {
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
     * Solve the problem
     */

    public void solve() {
        // Run n generations
        while (this.generation < Settings.maxGeneration) {
            System.out.println("Generation " + this.generation);
            // Evolve the current generation
            this.evolve();

            // Debug
            System.out.println("Gen: " + this.generation + " " + this.bestIndividual);
        }
    }

    /**
     * Adult selection
     */

    private void nonDominatedSort(ArrayList<Individual> population) {
        // Create the first front
        paretoFronts.add(new ParetoFront(1));

        // Loop the population
        for (Individual current : population) {
            // Resets dominated by and number of fronts it is dominated by
            current.reset();

            // Loop again
            for (Individual other : population) {
                // Filter out self
                if (current != other) {
                    if (current.dominates(other)) {
                        current.addDominatedIndividuals(other);
                    }
                    else if (other.dominates(current)) {
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
        /*
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
        */
    }

    private void crodingDistanceAssignment(ArrayList<Individual> members) {
        // DISTANCE

    }

    /**
     * Runs the algorithm one generation
     */

    public void evolve() {
        // Combine P and Q in R
        ArrayList<Individual> population = new ArrayList<>(parentPool);
        population.addAll(childPool);

        // Select who survives into adulthood
        this.nonDominatedSort(population);

        // New population
        ArrayList<Individual> newPopulation = new ArrayList<>();
        int counter = 0;

        // Loop until we have filled up the new population
        while ((newPopulation.size() + paretoFronts.get(counter).getSize()) < Settings.populationSize) {

            // Get the members of this fronts
            ArrayList<Individual> paretoMembers = paretoFronts.get(counter).getAllMembers();

            // Assign crowding distance to these members
            this.crodingDistanceAssignment(paretoMembers);

            // Add the members to the new population
            newPopulation.addAll(paretoMembers);

            // Increase counter
            counter++;
        }

        // Get the members that populates the remaining space in the new population
        ArrayList<Individual> remainingMembers = paretoFronts.get(counter).getAllMembers();

        // Assign crowding distance to the remaining members
        this.crodingDistanceAssignment(remainingMembers);

        // Sort the remaining members
        Collections.sort(remainingMembers, Sorter.crowdingDistanceComparator());

        // Add the remaining individuals from the front members
        newPopulation.addAll(remainingMembers.subList(0,
                (Settings.populationSize - newPopulation.size())));

        System.out.println(newPopulation.size());

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
