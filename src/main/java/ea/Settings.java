package ea;

public class Settings {
    // Number of cities
    public static int cities = 48;

    // Population size
    public static int populationSize = 2000;

    // Max number of generations
    public static int maxGeneration = 250;

    // Chance for crossover
    public static double crossover = 0.9;

    // Chance for mutation
    public static double mutation = 0.7;

    // Chance for passthrou
    public static double e = 0.35;

    // Size of the tournament
    public static int tournamentSize = 40;

    // Zero crowdingDistance sexual prevention
    public static boolean sexualPreventionCrowdingDistance = false;

    // Refresh rate
    public static int tick = 50;

    // Remove duplicates
    public static boolean removeDuplicates = true;

    // Turn on and off weird plotting
    public static boolean weirdPlotting = false;

    // Use best first parent selection
    public static boolean bestParentSelection = false;
}

