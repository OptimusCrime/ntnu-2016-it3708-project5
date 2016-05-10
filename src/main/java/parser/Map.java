package parser;

import ea.Settings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

public class Map {

    // Instance variable
    private static Map ourInstance = new Map();

    // Distance and cost maps
    private double[][] distanceMap;
    private double[][] costMap;

    /**
     * Get instance of map
     *
     * @return Singleton instance
     */

    public static Map getInstance() {
        return ourInstance;
    }

    /**
     * Singleton constructor
     */

    private Map() {
        try {
            // Load the files
            FileInputStream[] streams = getFiles();

            // Parse out the date from the xlxs files
            this.loadCitiesFromXlsx(streams);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get files from resources that holds the costs and distances
     *
     * @return Stream of files
     * @throws URISyntaxException Error
     * @throws FileNotFoundException Error
     */

    private FileInputStream[] getFiles() throws URISyntaxException, FileNotFoundException {
        FileInputStream[] files = new FileInputStream[2];

        File costFile = new File(this.getClass().getResource("/Cost_reformat.xlsx").toURI());
        File distanceFile = new File(this.getClass().getResource("/Distance_reformat.xlsx").toURI());

        files[0] = new FileInputStream(costFile);
        files[1] = new FileInputStream(distanceFile);

        return files;
    }

    /**
     * Create cities from the source file
     *
     * @param size Size of the matrix to create
     */

    private void createCities(int size) {

        // Initialize map tables
        distanceMap = new double[size][];
        costMap = new double[size][];
        for (int i = 0; i < size; i++) {
            distanceMap[i] = new double[size];
            costMap[i] = new double[size];
        }

    }

    /**
     * Load the cities cost and distance from the xlsx files
     *
     * @param streams Stream to read from
     * @throws IOException If file is not existing
     */

    private void loadCitiesFromXlsx(FileInputStream[] streams) throws IOException {
        // Fetch workbooks
        XSSFWorkbook[] workbooks = new XSSFWorkbook[2];
        workbooks[0] = new XSSFWorkbook(streams[0]);
        workbooks[1] = new XSSFWorkbook(streams[1]);

        // Fetch sheets
        XSSFSheet costSheet = workbooks[0].getSheetAt(0);
        XSSFSheet distanceSheet = workbooks[1].getSheetAt(0);

        // Find size of document
        int idx = 0;
        while (true) {
            Cell cell = costSheet.getRow(idx + 1).getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            if (cell == null) {
                break;
            }
            else {
                idx++;
            }
        }

        // Store number of cities in the settings
        Settings.cities = idx;

        // Create the cities
        this.createCities(idx);

        // Loop the documents
        for (int row = 1; row <= Settings.cities; row++) {
            for (int column = 1; column <= Settings.cities; column++) {

                // Fecth each cell
                Cell costCell = costSheet.getRow(row).getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell distanceCell = distanceSheet.getRow(row).getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                // Get cost and update both ways, i.e 3 -> 6 and 6 -> 3 **
                if (costCell != null) {
                    if (costCell.getCellType() == 0) {
                        double cost = costCell.getNumericCellValue();

                        costMap[row - 1][column - 1] = cost;
                        costMap[column - 1][row - 1] = cost;
                    }
                }

                // Get distance and update both ways, i.e 3 -> 6 and 6 -> 3 **
                if (distanceCell != null) {
                    if (distanceCell.getCellType() == 0) {
                        double distance = distanceCell.getNumericCellValue();

                        distanceMap[row - 1][column - 1] = distance;
                        distanceMap[column - 1][row - 1] = distance;

                    }
                }
            }
        }
    }

    /**
     * Get the distance between two cities
     *
     * @param fromId From id
     * @param toId To id
     * @return Distance
     */

    public double getDistance(int fromId, int toId) {
        int fromCity = fromId - 1;
        int toCity = toId - 1;
        return distanceMap[fromCity][toCity];
    }

    /**
     * Get the cost between two cities
     *
     * @param fromId From id
     * @param toId To id
     * @return Cost
     */

    public double getCost(int fromId, int toId) {
        int fromCity = fromId - 1;
        int toCity = toId - 1;
        return costMap[fromCity][toCity];
    }
}

