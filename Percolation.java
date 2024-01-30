///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS info.picocli:picocli:4.7.5
//SOURCE ./percolation/GridSites.java

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

// Main Class
@Command(name = "percolation", mixinStandardHelpOptions = true, version = "algo 0.1", description = "algo made with jbang")
class Questionaire implements Callable<Integer> {

  @Parameters(index = "0", defaultValue = "200")
  static String gridSize;

  @Parameters(index = "1", defaultValue = "100")
  static String T;

  @Parameters(index = "2", defaultValue = "0")
  static String totalOpenSites;

  // function to convert a string to an integer
  public static int stringToInt(String str) {
    try {
      return Integer.parseInt(str);
    } 
    
    catch (NumberFormatException e) {
      System.out.println("Invalid input. Please enter a valid integer: " + e.getMessage());

      return 0;
    }
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new Questionaire()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() throws Exception { 
    PercolationTest.start(stringToInt(gridSize), stringToInt(T), stringToInt(totalOpenSites));

    return 0;
  }
}

// tests the perculation
class PercolationTest {
  public static void start(
      int gridSizeParam,
      int triesParam,
      int totalOpenSitesParam) {
      int gridSize = gridSizeParam;
      int T = triesParam;
      int totalOpenSites = totalOpenSitesParam;

    double[] thresholds = new double[T];

    GridSitesGenerator gridGenerator = new GridSitesGenerator(gridSize);
    Percolation percolation = new Percolation(gridGenerator);

    for (int i = 0; i < T; i++) {
      gridGenerator = new GridSitesGenerator(gridSize);
      percolation = new Percolation(gridGenerator);
      percolation.create(gridSize);

      while (!percolation.percolates()) {
        int row = (int) (Math.random() * gridSize);
        int col = (int) (Math.random() * gridSize);
        if (!percolation.isOpen(row, col)) {
          percolation.open(row, col);
          totalOpenSites++;
        }
      }

      // simulation reset 
      thresholds[i] = (double) totalOpenSites / (gridSize * gridSize);
      totalOpenSites = 0;

    }

    // setter for the required solutions
    Stats stats = new Stats();
    double averageThreshold = stats.mean(thresholds);
    double stddev = stats.stddev(thresholds);
    double confidenceLo = stats.confidenceLo(thresholds);
    double confidenceHi = stats.confidenceHi(thresholds);

    gridGenerator.printGrid();
    System.out.println();

    //result displays on the terminal
    System.out.println("Mean percolation threshold: " + averageThreshold);
    System.out.println("Stddev: " + stddev);
    System.out.println("95% confidence interval: [" + confidenceLo + ", " + confidenceHi + "]");
  }
}

class Percolation implements PercolationBase {
  private GridSitesGenerator gridGenerator;
  private WeightedQuickUnion wqu;
  private int gridSize;

  public Percolation(GridSitesGenerator gridGenerator) {
    this.gridGenerator = gridGenerator;
  }

  // includes 2 viritual nodes as openings on top and bottom
  @Override
  public void create(int n) {
    gridSize = n;
    wqu = new WeightedQuickUnion(n * n + 2); 
  }

  @Override
  public void open(int row, int col) {
    gridGenerator.open(row, col);

    // add 1 to validate the top virtual node
    int siteIndex = row * gridSize + col + 1; 

    // Connect to open neighbors and virtual nodes
    if (row > 0 && isOpen(row - 1, col)) {
      wqu.union(siteIndex, (row - 1) * gridSize + col + 1);
    }

    if (row < gridSize - 1 && isOpen(row + 1, col)) {
      wqu.union(siteIndex, (row + 1) * gridSize + col + 1);
    }

    if (col > 0 && isOpen(row, col - 1)) {
      wqu.union(siteIndex, row * gridSize + (col - 1) + 1);
    }

    if (col < gridSize - 1 && isOpen(row, col + 1)) {
      wqu.union(siteIndex, row * gridSize + (col + 1) + 1);
    }

    if (row == 0) {

    // connects on the virtual node on top
    wqu.union(siteIndex, 0); 
  }
  
  wqu.union(siteIndex, gridSize * gridSize + 1); 
    // connects on the virtual node on bottom
    if (row == gridSize - 1) {
    }
  }

  @Override
  public boolean isFull(int row, int col) {
    int siteIndex = row * gridSize + col + 1;

    // checker if it is connected on the top virtual node
    return wqu.connected(siteIndex, 0); 
  }

  @Override
  public int numberOfOpenSites() {
    return gridGenerator.getNumberOfOpenSites();
  }

  @Override
  public boolean percolates() {

    // checker if both the top and bottom virtual nodes are connected
    return wqu.connected(0, gridSize * gridSize + 1); 
  }

  @Override
  public boolean isOpen(int row, int col) {
    return gridGenerator.isOpen(row, col);
  }
};

// solutions for mean, sd, confidence level
class Stats {
  public double mean(double[] numbers) {
    double sum = 0;
    for (double num : numbers) {
      sum += num;
    }
    return sum / numbers.length;
  }

  public double stddev(double[] numbers) {
    double mean = mean(numbers);
    double sum = 0;
    for (double num : numbers) {
      sum += Math.pow(num - mean, 2);
    }
    return Math.sqrt(sum / (numbers.length - 1));
  }

  public double confidenceLo(double[] numbers) {
    double mean = mean(numbers);
    double stddev = stddev(numbers);
    return mean - 1.96 * stddev / Math.sqrt(numbers.length);
  }

  public double confidenceHi(double[] numbers) {
    double mean = mean(numbers);
    double stddev = stddev(numbers);
    return mean + 1.96 * stddev / Math.sqrt(numbers.length);
  }
}

// displays the grid on the terminal
class GridSitesGenerator {
  private int[][] grid;
  private int size;

  public GridSitesGenerator(int size) {
    this.size = size;
    grid = new int[size][size];
    for (int i = 0; i < size; i++) {
      grid[i] = new int[size];
    }
  }

  public void open(int row, int col) {
    grid[row][col] = 1;
  }

  public boolean isOpen(int row, int col) {
    return grid[row][col] == 1;
  }

  public boolean isFull(int row, int col) {
    return grid[row][col] == 2;
  }

  public void fill(int row, int col) {
    grid[row][col] = 2;
  }

  public int getSize() {
    return size;
  }

  public int[][] getGrid() {
    return grid;
  }

  public void printGrid() {
    System.out.println();
    for (int i = 0; i < size; i++) {
      System.out.println();
      for (int j = 0; j < size; j++) {
        System.out.print(grid[i][j] + " ");
      }
    }
  }

  public int getNumberOfOpenSites() {
    int count = 0;
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (grid[i][j] == 1) {
          count++;
        }
      }
    }

    return count;

  }
}

// the algorithm Monte Carlo simulation
class WeightedQuickUnion {
  private int[] id;
  private int[] sz;

  public WeightedQuickUnion(int N) {
    id = new int[N];
    sz = new int[N];
    for (int i = 0; i < N; i++) {
      id[i] = i;
      sz[i] = 1;
    }
  }

  private int root(int i) {
    while (i != id[i]) {
    // path compression
      id[i] = id[id[i]]; 
      i = id[i];
    }

    return i;
  }

  public boolean connected(int p, int q) {
    return root(p) == root(q);
  }

  public void union(int p, int q) {
    int i = root(p);
    int j = root(q);
    if (i == j)
      return;
    if (sz[i] < sz[j]) {
      id[i] = j;
      sz[j] += sz[i];
    } else {
      id[j] = i;
      sz[i] += sz[j];
    }
  }
}

interface PercolationBase {

  // creates n-by-n grid, with all sites initially blocked
  void create(int n);

  // opens the site (row, col) if it is not open already
  void open(int row, int col);

  // checks ig the site on row and column are open
  boolean isOpen(int row, int col);
  
  // checks ig the site on row and column are full
  boolean isFull(int row, int col);

  // returns the amount of sites that are open
  int numberOfOpenSites();

  // checks if the system percolates
  boolean percolates();
}