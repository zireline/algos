package percolation;

public class GridSites {
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
