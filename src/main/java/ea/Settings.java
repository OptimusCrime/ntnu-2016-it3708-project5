package ea;

public class Settings {
    // Number of cities
    public static int cities = 48;

    // Population size
    public static int populationSize = 200;

    // Max number of generations
    public static int maxGeneration = 2000;

    // Chance for crossover
    public static double crossover = 0.9;

    // Chance for mutation
    public static double mutation = 0.9;

    // Chance for passthrou
    public static double e = 0.35;

    // Size of the tournament
    public static int tournamentSize = 75;

    // Use unique fitness constraint
    public static boolean fitnessConstraint = true;

    // Zero crowdingDistance sexual prevention
    public static boolean sexualPreventionCrowdingDistance = true;

    // Refresh rate
    public static int tick = 100;
    // Remove duplicates
    public static boolean removeDuplicates = true;
}

