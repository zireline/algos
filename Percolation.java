///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS info.picocli:picocli:4.7.5
//SOURCE ./percolation/GridSites.java

// import picocli.CommandLine;
// import picocli.CommandLine.Command;
// import picocli.CommandLine.Parameters;

// import java.util.concurrent.Callable;

// @Command(name = "hello", mixinStandardHelpOptions = true, version = "hello 0.1", description = "hello made with jbang")
// class Main implements Callable<Integer> {

class Percolation {

  // @Parameters(index = "0", description = "The greeting to print", defaultValue
  // = "World!")
  // private String greeting;

  public static void main(String... args) {
    GridSites grid = new GridSites(5);

    grid.open(2, 3);
    ;
    grid.printGrid();

    // int exitCode = new CommandLine(new Main()).execute(args);
    // System.exit(exitCode);
  }

  // @Override
  // public Integer call() throws Exception { // your business logic goes here...
  // System.out.println("Hello " + greeting);
  // return 0;
  // }
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