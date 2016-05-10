package ea;

public class Settings {
    // Number of cities
    public static int cities = 48;

    // Population size
    public static int populationSize = 1000;

    // Max number of generations
    public static int maxGeneration = 1500;

    // Chance for crossover
    public static double crossover = 0.80;

    // Chance for mutation
    public static double mutation = 0.50;

    // Chance for passthrou
    public static double e = 0.3;

    // Size of the tournament
    public static int tournamentSize = 50;

    // Use unique fitness constraint
    public static boolean fitnessConstraint = true;

    // Zero crowdingDistance sexual prevention
    public static boolean sexualPreventionCrowdingDistance = true;
}
