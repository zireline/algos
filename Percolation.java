///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS info.picocli:picocli:4.7.5
//SOURCE ./percolation/GridSites.java

// import picocli.CommandLine;
// import picocli.CommandLine.Command;
// import picocli.CommandLine.Parameters;

// import java.util.concurrent.Callable;

// @Command(name = "hello", mixinStandardHelpOptions = true, version = "hello 0.1", description = "hello made with jbang")
// class Main implements Callable<Integer> {

class Data {
  public static int gridSize = 200;
  public static int T = 100;
  public static int totalOpenSites = 0;
}

// main class
class Main {
  public static void main(String... args) {
    int gridSize = Data.gridSize;
    int T = Data.T;
    double[] thresholds = new double[T];

    for (int i = 0; i < T; i++) {
      Percolation percolation = new Percolation();
      percolation.create(gridSize);

      while (!percolation.percolates()) {
        int row = (int) (Math.random() * gridSize);
        int col = (int) (Math.random() * gridSize);
        if (!percolation.isOpen(row, col)) {
          percolation.open(row, col);
          Data.totalOpenSites++;
        }
      }

      thresholds[i] = (double) Data.totalOpenSites / (gridSize * gridSize);
      Data.totalOpenSites = 0; // reset for the next simulation
    }

    // Calculate the average threshold
    Stats stats = new Stats();
    double averageThreshold = stats.mean(thresholds);
    double stddev = stats.stddev(thresholds);
    double confidenceLo = stats.confidenceLo(thresholds);
    double confidenceHi = stats.confidenceHi(thresholds);

    System.out.println("Mean percolation threshold: " + averageThreshold);
    System.out.println("Stddev: " + stddev);
    System.out.println("95% confidence interval: [" + confidenceLo + ", " + confidenceHi + "]");
  }
}

class Percolation implements PercolationBase {
  private GridSitesGenerator gridGenerator;
  private WeightedQuickUnion wqu;
  private int gridSize;

  @Override
  public void create(int n) {
    gridSize = n;
    gridGenerator = new GridSitesGenerator(n);
    wqu = new WeightedQuickUnion(n * n + 2); // includes two virtual nodes
  }

  @Override
  public void open(int row, int col) {
    gridGenerator.open(row, col);
    int siteIndex = row * gridSize + col + 1; // +1 to account for the top virtual node

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
      wqu.union(siteIndex, 0); // connect to top virtual node
    }
    if (row == gridSize - 1) {
      wqu.union(siteIndex, gridSize * gridSize + 1); // connect to bottom virtual node
    }
  }

  @Override
  public boolean isFull(int row, int col) {
    int siteIndex = row * gridSize + col + 1;
    return wqu.connected(siteIndex, 0); // check if connected to top virtual node
  }

  @Override
  public int numberOfOpenSites() {
    return gridGenerator.getNumberOfOpenSites();
  }

  @Override
  public boolean percolates() {
    return wqu.connected(0, gridSize * gridSize + 1); // check if top and bottom virtual nodes are connected
  }

  @Override
  public boolean isOpen(int row, int col) {
    return gridGenerator.isOpen(row, col);
  }

}

// calculates mean, standard deviation, and 95% confidence interval
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

// displays grid in terminal
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

// the algorithm
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
      id[i] = id[id[i]]; // path compression
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

  // is the site (row, col) open?
  boolean isOpen(int row, int col);

  // is the site (row, col) full?
  boolean isFull(int row, int col);

  // returns the number of open sites
  int numberOfOpenSites();

  // does the system percolate?
  boolean percolates();
}