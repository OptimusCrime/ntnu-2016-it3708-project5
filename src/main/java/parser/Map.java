package parser;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

public class Map {
    ////////////////
    //  Singleton //
    ////////////////

    private static Map ourInstance = new Map();

    public static Map getInstance() {
        return ourInstance;
    }

    private Map() {
        // Create city object
        this.createCities();

        // Fill city objects
        this.generateCities();

        // Accumulate statistics
        this.calculateStatistics();
    }

    ///////////////
    //  Cities   //
    ///////////////

    private double[][] distanceMap;
    private double[][] costMap;

    private double totalDistance;
    private double avgDistance;
    private double totalCost;
    private double avgCost;
    private double maxCost;
    private double maxDistance;

    public double getAvgCost() {
        return avgCost;
    }

    public double getMaxCost() {
        return maxCost;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getAvgDistance() {
        return avgDistance;
    }

    public double getTotalCost() {
        return totalCost;
    }

    private void generateCities() {
        try {
            // Load the files
            FileInputStream[] streams = getFiles();

            // Parse out the date from the xlxs files
            loadCitiesFromXlsx(streams);


        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCities() {

        // Initialize map tables
        distanceMap = new double[48][];
        costMap = new double[48][];
        for (int i = 0; i < 48; i++) {
            distanceMap[i] = new double[48];
            costMap[i] = new double[48];
        }

    }

    private void loadCitiesFromXlsx(FileInputStream[] streams) throws IOException {
        // Fetch workbooks
        XSSFWorkbook[] workbooks = new XSSFWorkbook[2];
        workbooks[0] = new XSSFWorkbook(streams[0]);
        workbooks[1] = new XSSFWorkbook(streams[1]);

        // Fetch sheets
        XSSFSheet costSheet = workbooks[0].getSheetAt(0);
        XSSFSheet distanceSheet = workbooks[1].getSheetAt(0);

        // Loop the documents
        for (int row = 1; row <= 48; row++) {
            for (int column = 1; column <= 48; column++) {

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

    private FileInputStream[] getFiles() throws URISyntaxException, FileNotFoundException {
        FileInputStream[] files = new FileInputStream[2];

        File costFile = new File(this.getClass().getResource("/Cost_reformat.xlsx").toURI());
        File distanceFile = new File(this.getClass().getResource("/Distance_reformat.xlsx").toURI());

        files[0] = new FileInputStream(costFile);
        files[1] = new FileInputStream(distanceFile);

        return files;
    }

    private void calculateStatistics() {
        totalDistance = sum2d(distanceMap);
        totalCost = sum2d(costMap);
        avgDistance = totalDistance / (48 * 48);
        avgCost = totalCost / (48 * 48);

        maxCost = maxValue(costMap);
        maxDistance = maxValue(distanceMap);

    }

    private double maxValue(double[][] matrix) {
        double max = 0;
        for(int row = 0; row < matrix.length; row++) {
            for(int column = 0; column < matrix[row].length; column++) {
                if (matrix[row][column] > max) {
                    max = matrix[row][column];
                }
            }
        }
        return max;
    }

    private double sum2d(double[][] matrix) {
        double sum = 0;
        for(int row = 0; row < matrix.length; row++) {
            for(int column = 0; column < matrix[row].length; column++) {
                sum += matrix[row][column];
            }
        }
        return sum;
    }

    public double getDistance(int fromId, int toId) {
        int fromCity = fromId - 1;
        int toCity = toId - 1;
        return distanceMap[fromCity][toCity];
    }

    public double getCost(int fromId, int toId) {
        int fromCity = fromId - 1;
        int toCity = toId - 1;
        return costMap[fromCity][toCity];
    }


    public static void main(String[] args) {
        //Load everything
        Map m = Map.getInstance();

        for (int i = 0; i < 48; i++) {
            System.out.println(Arrays.toString(m.distanceMap[i]));
        }


        for (int i = 0; i < 48; i++) {
            System.out.println(Arrays.toString(m.costMap[i]));
        }
        System.out.println(m.distanceMap[0][8]);
        System.out.println(m.distanceMap[2][16]);
        System.out.println(m.distanceMap[8][22]);

        System.out.println(m.getTotalDistance());
        System.out.println(m.getAvgDistance());
        System.out.println(m.getTotalCost());
        System.out.println(m.getAvgCost());
    }
}

