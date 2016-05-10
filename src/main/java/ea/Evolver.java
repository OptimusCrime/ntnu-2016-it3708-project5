package ea;

import nsga.Individual;
import parento.ParetoFront;
import parser.Map;
import sort.Sorter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

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
        System.out.println("Generation #" + this.generation);
        if (this.generation < Settings.maxGeneration) {
            this.evolve();

            return true;
        }

        return false;
    }

    /**
     * Solve the problem
     */

    public void solve() {
        // Run n generations
        while (this.generation < Settings.maxGeneration) {
            // Evolve the current generation
            this.evolve();
        }
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
        while (children.size() < Settings.populationSize) {
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

    private Individual selectParent(ArrayList<Individual> parents) {
        // TODO: In place ?
        ArrayList<Individual> candidates = new ArrayList<>(parents);
        Collections.shuffle(candidates);

        // Create sublist of parents with tournament size
        ArrayList<Individual> tournament = new ArrayList<>(candidates.subList(0, Math.min(parents.size(), Settings.tournamentSize)));

        // Check if we should select a random
        Random r = new Random();
        if (Settings.e <= r.nextDouble()) {
            // Random selection
            return tournament.get(0);
        }
        else {
            // Select parent with best crowding distance
            Collections.sort(tournament, Sorter.crowdingDistanceComparator());
            return tournament.get(0);
        }
    }

    /**
     * Assign crowding distance to list of individuals
     *
     * @param members Members to calculate distances for
     */

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

    }

    /**
     * Runs the algorithm one generation
     */

    public void evolve() {

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
        }

        // Assign crowding distance and Sord the remaining members
        this.crowdingDistanceAssignment(paretoFronts.get(counter).getAllMembers());

        // Sort them
        Collections.sort(paretoFronts.get(counter).getAllMembers(), Sorter.crowdingDistanceComparator());

        // Add the remaining individuals from the front members
        newPopulation.addAll(paretoFronts.get(counter).getAllMembers().subList(0, (Settings.populationSize - newPopulation.size())));

        // Set new population as parents
        this.parentPool = newPopulation;

        //
        // PARENT SELECTION
        //

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
        for (Individual member : this.childPool) {
            System.out.println(member);
        }
        System.out.println(" ");
        System.out.println("---------------------------------");
        System.out.println(" ");

        /*
        // Get the first individual
        //ArrayList<Individual> bestIndividuals = new ArrayList<>(this.paretoFronts.get(0).getAllMembers());
        //Collections.sort(bestIndividuals, Sorter.crowdingDistanceComparator());
        //bestIndividual = bestIndividuals.get(0);

        bestIndividual = this.parentPool.get(0);

        for (Individual individual : this.parentPool) {
            // Check if this individual is better than best
        }*/
    }

    public ArrayList<ParetoFront> getParetoFronts() {
        return paretoFronts;
    }

    public ArrayList<Individual> getChildren() {
        return childPool;
    }
}
