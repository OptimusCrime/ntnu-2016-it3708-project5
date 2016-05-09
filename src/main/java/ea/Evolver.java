package ea;

import nsga.Individual;
import parento.ParetoFront;
import parser.Map;
import sort.Sorter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

public class Evolver {

    private int generation;
    private ArrayList<Individual> childPool;
    private ArrayList<Individual> parentPool;
    private ArrayList<ParetoFront> paretoFronts;
    private Individual bestIndividual;

    private Random random;

    /**
     * Constructor
     */

    public Evolver() {
        // Set current generation
        this.generation = 0;
        this.random = new Random();

        // Create empty child and parent pool
        childPool = new ArrayList<>();
        parentPool = new ArrayList<>();
        paretoFronts = new ArrayList<>();
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

        // Resets dominated by and number of fronts it is dominated by
        for (Individual person : population) {
            person.reset();
        }

        // Create the first front
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

        ArrayList<Individual> potentialParents = new ArrayList<>(this.parentPool);
        ArrayList<Individual> children = new ArrayList<>();

        while (children.size() < Settings.populationSize) {
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
        this.childPool = children;
    }

    private Individual selectParent(ArrayList<Individual> parents) {
        // TODO: In place ?
        ArrayList<Individual> candidates = new ArrayList<>(parents);
        Collections.shuffle(candidates);

        ArrayList<Individual> tournament = new ArrayList<>(candidates.subList(0, Math.min(parents.size(), Settings.tournamentSize)));

        if (Settings.e <= random.nextDouble()) {
            return tournament.get(0);
        } else {
            Collections.sort(tournament, Sorter.crowdingDistanceComparator());
            return tournament.get(0);
        }


    }

    private void crowdingDistanceAssignment(ArrayList<Individual> members) {
        // -- DISTANCE --
        ArrayList<Individual> distanceSorted = new ArrayList<Individual>(members);
        Collections.sort(distanceSorted, Sorter.distanceComparator());

        // Set inf to extremals - for good distribution
        distanceSorted.get(0).setCrowdingDistance(Double.MAX_VALUE);
        distanceSorted.get(distanceSorted.size() - 1).setCrowdingDistance(Double.MAX_VALUE);

        double minDistance = distanceSorted.get(0).getDistance();
        double maxDistance = distanceSorted.get(distanceSorted.size() - 1).getDistance();


        for (int i = 1; i < distanceSorted.size() - 1; i++) {
            // Get neighbour values
            double previous = distanceSorted.get(i - 1).getDistance();
            double next = distanceSorted.get(i + 1).getDistance();
            double currentCrowdingDistance = distanceSorted.get(i).getCrowdingDistance();

            double distanceCrowdingDistance = (next - previous) / (maxDistance - minDistance);
            distanceSorted.get(i).setCrowdingDistance(distanceCrowdingDistance);
        }


        // -- COST --
        ArrayList<Individual> costSorted = new ArrayList<Individual>(members);
        Collections.sort(distanceSorted, Sorter.costComparator());

        // Set inf to extremals - for good distribution
        costSorted.get(0).setCrowdingDistance(Double.MAX_VALUE);
        costSorted.get(costSorted.size() - 1).setCrowdingDistance(Double.MAX_VALUE);

        double minCost = costSorted.get(0).getCost();
        double maxCost = costSorted.get(costSorted.size() - 1).getCost();

        for (int i = 1; i < costSorted.size() - 1; i++) {
            // Get neighbour values
            double previous = costSorted.get(i - 1).getCost();
            double next = costSorted.get(i + 1).getCost();

            double currentCrowdingDistance = costSorted.get(i).getCrowdingDistance();

            double costCrowdinDistance = currentCrowdingDistance + ((next - previous) / (maxCost - minCost));
            costSorted.get(i).setCrowdingDistance(costCrowdinDistance);
        }

    }

    /**
     * Runs the algorithm one generation
     */

    public void evolve() {

        //////////////////////
        //  Adult selection //
        //////////////////////

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
        }

        // Assign crowding distance and Sord the remaining members
        this.crowdingDistanceAssignment(paretoFronts.get(counter).getAllMembers());
        Collections.sort(paretoFronts.get(counter).getAllMembers(), Sorter.crowdingDistanceComparator());

        // Add the remaining individuals from the front members
        newPopulation.addAll(paretoFronts.get(counter).getAllMembers().subList(0, (Settings.populationSize - newPopulation.size())));

        // Set new population as parents
        this.parentPool = newPopulation;

        ///////////////////////
        //  Parent selection //
        ///////////////////////

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
        ArrayList<Individual> bestIndividuals = new ArrayList<>(this.paretoFronts.get(0).getAllMembers());
        Collections.sort(bestIndividuals, Sorter.crowdingDistanceComparator());
        bestIndividual = bestIndividuals.get(0);

        for (Individual individual : this.parentPool) {
            // Check if this individual is better than best
        }
    }
}
