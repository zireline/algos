///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS info.picocli:picocli:4.7.5
//SOURCE ./percolation/GridSites.java

// import picocli.CommandLine;
// import picocli.CommandLine.Command;
// import picocli.CommandLine.Parameters;

// import java.util.concurrent.Callable;

// @Command(name = "hello", mixinStandardHelpOptions = true, version = "hello 0.1", description = "hello made with jbang")
// class Main implements Callable<Integer> {

import java.util.List;
import java.util.ArrayList;

class Stats {
  private final List<Double> numbers;

  public Stats(List<Double> numbers) {
    this.numbers = numbers;
  }

  public double mean() {
    double sum = 0;
    for (double num : numbers) {
      sum += num;
    }
    return sum / numbers.size();
  }

  public double stddev() {
    double mean = mean();
    double sum = 0;
    for (double num : numbers) {
      sum += Math.pow(num - mean, 2);
    }
    return Math.sqrt(sum / (numbers.size() - 1));
  }

  public double[] confidence() {
    double mean = mean();
    double stddev = stddev();
    double marginOfError = 1.96 * stddev / Math.sqrt(numbers.size());
    return new double[] { mean - marginOfError, mean + marginOfError };
  }

  public void displayStats() {
    System.out.println("Mean: " + mean());
    System.out.println("Standard deviation: " + stddev());
    double[] confidence = confidence();
    System.out.println("95% confidence interval: [" + confidence[0] + ", " + confidence[1] + "]");
  }
}

class Percolation {
  public static void main(String... args) {
    int gridSize = 20;
    int numExperiments = 1000;
    int totalOpenSites = 0;
    GridSites finalGrid = null; // variable to store the final state of the grid

    List<Double> percolationThresholds = new ArrayList<>();
    for (int i = 0; i < numExperiments; i++) {
      GridSites grid = new GridSites(gridSize);
      WeightedQuickUnion uf = new WeightedQuickUnion(gridSize * gridSize + 2);

      while (!percolates(grid, uf)) {
        int row = (int) (Math.random() * gridSize);
        int col = (int) (Math.random() * gridSize);
        if (!grid.isOpen(row, col)) {
          grid.open(row, col);
          unionWithNeighbors(row, col, grid, uf);
        }
        if (percolates(grid, uf)) {
          finalGrid = grid; // save the final state of the grid
        }
      }

      totalOpenSites += grid.getNumberOfOpenSites();

      double percolationThreshold = (double) totalOpenSites / (gridSize * gridSize);
      percolationThresholds.add(percolationThreshold);
    }

    if (finalGrid != null) {
      finalGrid.printGrid(); // print the final state of the grid
      System.out.println();
    }

    Stats stats = new Stats(percolationThresholds);
    stats.displayStats();
  }

  private static boolean percolates(GridSites grid, WeightedQuickUnion uf) {
    int top = 0;
    int bottom = grid.getSize() * grid.getSize() + 1;
    return uf.connected(top, bottom);
  }

  private static void unionWithNeighbors(int row, int col, GridSites grid, WeightedQuickUnion uf) {
    int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
    int n = grid.getSize();
    int p = n * row + col + 1;

    for (int[] dir : directions) {
      int newRow = row + dir[0];
      int newCol = col + dir[1];

      if (newRow >= 0 && newRow < n && newCol >= 0 && newCol < n && grid.isOpen(newRow, newCol)) {
        int q = n * newRow + newCol + 1;
        uf.union(p, q);
      }
    }

    if (row == 0) {
      uf.union(p, 0);
    }

    if (row == n - 1) {
      uf.union(p, n * n + 1);
    }
  }
}

class GridSites {
  private int[][] grid;
  private int size;

  public GridSites(int size) {
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