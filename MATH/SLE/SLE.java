import java.util.List;
import java.util.ArrayList;

public class SLE {
	// Transforms (A|b) in the reduced row echelon form
	public static double[][] calcNF2(double[][] Ab) {
		// Store the height of the matrix and the number of unknown variables
		int m = Ab.length, n = Ab[0].length - 1;

		// Indices for the pivot row and column
		int pivotRow = -1;
		int pivotCol = -1;

		// A threshold needed in the methods multiplyRowByScalar and addScalarMultipleOfOneRowToAnother to round values
		double epsilon = 1e-6;

		for (int col = 0; col < n; col++) {
			// Find the next pivot row and column and update the indices
			for (int row = pivotRow + 1; row < m; row++) {
				if (Ab[row][col] == 0) continue;

				pivotRow++;
				pivotCol = col;
				break;
			}

			// Only swap rows and multiply the pivot row by a scalar if the value at the current position isn't 1
			if (pivotRow != -1 && pivotCol != -1 && Ab[pivotRow][col] != 1) {
				// Get the row with the highest absolute value or one and swap this row with the current pivot row
				int rowToSwap = getRowIndexWithHighestAbsoluteValueOrOne(Ab, m, col, pivotRow);
				if (rowToSwap != -1) swapRows(Ab, rowToSwap, pivotRow);

				// Multiply the pivot row by a scalar to make its leading value a one
				if (col == pivotCol) {
					double scalar = 1 / Ab[pivotRow][col];
					if (scalar != 1) multiplyRowByScalar(Ab, n, pivotRow, scalar, epsilon);
				}
			}

			for (int row = 0; row < m; row++) {
				// If the value is 0, or we are at the index of the pivot row, don't do anything
				if (Ab[row][col] == 0 || row == pivotRow) continue;

				// If the column is a pivotal column, make all values above and below it 0
				if (col == pivotCol) {
					double scalar = Ab[row][col];

					// If the scalar is positive, make it negative, otherwise if it's negative, make it positive
					scalar = scalar < 0 ? scalar * (-1) : -scalar;

					// Add the values of the pivotal row multiplied with the scalar to the current row
					addScalarMultipleOfOneRowToAnother(Ab, n, row, scalar, pivotRow, epsilon);
				}
			}
		}

		return Ab;
	}

	// Finds the index of first row in the column below the pivot row which has an absolute value of 1 or the index of the row with the highest absolute value
	private static int getRowIndexWithHighestAbsoluteValueOrOne(double[][] Ab, int m, int col, int pivotRow) {
		// Initialize the index with -1 for the case that the column only contains zeros
		int index = -1;
		double maxAbsoluteValue = 0;

		for (int row = pivotRow; row < m; row++) {
			// If a value of one is found immediately set the index to return and break the for loop
			if (Math.abs(Ab[row][col]) == 1) {
				index = row;
				break;
			} else if (Math.abs(Ab[row][col]) > maxAbsoluteValue) {
				// Otherwise update the maximum absolute value found to this point as well as the row index that belongs to this value
				maxAbsoluteValue = Math.abs(Ab[row][col]);
				index = row;
			}
		}

		return index;
	}

	// Swaps the positions of two rows
	private static void swapRows(double[][] Ab, int row1, int row2) {
		// Prevent swapping a row with itself, because it is unnecessary
		if (row1 == row2) return;

		// Swap the rows by using a temporary variable to store row 1 before overwriting it with row 2
		double[] temp = Ab[row1];
		Ab[row1] = Ab[row2];
		Ab[row2] = temp;
	}

	// Multiplies a row by a non-zero scalar
	private static void multiplyRowByScalar(double[][] Ab, int n, int row, double scalar, double epsilon) {
		// Multiply each value from the row by the passed scalar value
		for (int col = 0; col < n + 1; col++) {
			double newValue = Ab[row][col] *= scalar;

			// Check if the absolute difference between the original value and its rounded form is very small and if so round it
			if (Math.abs(newValue - Math.round(newValue)) < epsilon) newValue = Math.round(newValue);

			Ab[row][col] = newValue;

			// Changes -0.0 to 0
			if (Ab[row][col] == -0.0) Ab[row][col] = 0;
		}
	}

	// Adds to one row a scalar multiple of another
	private static void addScalarMultipleOfOneRowToAnother(double[][] Ab, int n, int targetRow, double scalar, int sourceRow, double epsilon) {
		// Multiply each value from the source row by the passed scalar value and add the resulting product to corresponding value of the target row
		for (int col = 0; col < n + 1; col++) {
			double newValue = Ab[targetRow][col] += scalar * Ab[sourceRow][col];

			// Check if the absolute difference between the original value and its rounded form is very small and if so round it
			if (Math.abs(newValue - Math.round(newValue)) < epsilon) newValue = Math.round(newValue);

			Ab[targetRow][col] = newValue;

			// Changes -0.0 to 0
			if (Ab[targetRow][col] == -0.0) Ab[targetRow][col] = 0;
		}
	}

	/*
    If Ax = b has solutions, solve calculates the solutions xh of the homogeneous system and a special solution xs of
    inhomogeneous system and returns this in the matrix xhs = (xh|xs).
    xhs is therefore an n x(d+1) matrix, where n is the number of indefinites and d = n – rg(A) is the dimension of the solution space of the homogeneous system.
    If Ax = b has no solutions, null is returned.
     */
	public static double[][] solve(double[][] Ab){
		// Store the number of rows, unknown variables and the rank of the extended coefficient matrix to first check whether
		// the system is solvable or not and if it's solvable, to calculate the dimension of the solution space later on
		int m = Ab.length, n = Ab[0].length - 1;
		int rankA = getRank(Ab, n);
		int rankAb = getRank(Ab, n + 1);

		// Check if the system is solvable and instantly return null if it's not
		if (rankA < rankAb) return null;

		// Equalize the matrix with the help of its pivotal columns
		Ab = getEqualizedMatrix(Ab, m, n, getPivotalCols(Ab, m, n), rankA);

		// Store the new height of the equalized extended coefficient matrix and calculate
		m = Ab.length;

		// Calculate the dimension of the solution space of the matrix and then get the solutions and return them
		int dimension = n - rankA;
		return getSolutions(Ab, m, n, dimension);
	}

	// Determines the rank of the passed extended coefficient matrix
	private static int getRank(double[][] Ab, int n) {
		int rank = 0;

		// Loop over all elements in the matrix A
		for (double[] row : Ab) {
			for (int col = 0; col < n; col++) {
				// If there is a number other than 0 found in the row, increment the rank and
				// break the inner loop to continue with the next row immediately
				if (row[col] != 0) {
					rank++;
					break;
				}
			}
		}

		return rank;
	}

	// Get the pivotal columns of the passed extended coefficient matrix
	private static List<Integer> getPivotalCols(double[][] Ab, int m, int n) {
		List<Integer> pivotalCols = new ArrayList<>();
		int step = 0;

		// Checks if the current row/step contains a 1 and if so, adds the index of the column where the value of 1 was found to
		// the pivotalCols list. Also, after finding a pivotal element, break the inner loop and increase the row/step, because
		// there can't be another pivotal element in the same row/step
		for (int row = step; row < m; row++) {
			for (int col = 0; col < n; col++) {
				if (Ab[row][col] == 1) {
					pivotalCols.add(col);
					step++;
					break;
				}
			}
		}

		return pivotalCols;
	}

	// Checks if a row of a matrix contains only zeros or not
	private static boolean isZeroRow(double[] row) {
		for (double val : row) {
			if (val != 0) return false;
		}

		return true;
	}

	// Returns the equalized matrix
	private static double[][] getEqualizedMatrix(double[][] Ab, int m, int n, List<Integer> pivotalCols, int rank) {
		List<Integer> indicesToAddZeroRowsTo = new ArrayList<>();

		// Get the complementary set between the indices of the unknown variables and the indices of the pivotal columns
		for (int col = 0; col < n; col++) {
			if (!pivotalCols.contains(col)) {
				indicesToAddZeroRowsTo.add(col);
			}
		}

		int zeroRowsBelowRank = 0;

		// Count the zero rows below the row with the index of the calculated rank
		for (int row = rank; row < m; row++) {
			if (isZeroRow(Ab[row])) {
				zeroRowsBelowRank++;
			}
		}

		// Create a new array and copy as many rows from the old matrix as how many there were minus the amount of
		// zero rows below the rank. Overwrite Ab with this new matrix in the end
		double[][] AbWithoutZeroRowsBelowRank = new double[m - zeroRowsBelowRank][n + 1];
		System.arraycopy(Ab, 0, AbWithoutZeroRowsBelowRank, 0, AbWithoutZeroRowsBelowRank.length);
		Ab = AbWithoutZeroRowsBelowRank;

		int zeroRowCount = 0;

		// Count the zero rows in Ab
		for (double[] row : Ab) {
			if (isZeroRow(row)) {
				zeroRowCount++;
			}
		}

		// Create a new array for the equalized matrix
		double[][] AbEqualized = new double[Ab.length + indicesToAddZeroRowsTo.size() - zeroRowCount][n + 1];

		// Keeps track of which row of Ab to copy next
		int rowInAb = 0;

		// Loop over all rows of the new matrix
		for (int row = 0; row < AbEqualized.length; row++) {
			// If the row index equals an index of the indicesToAddZeroRowsTo list, add zeros to this whole row
			if (indicesToAddZeroRowsTo.contains(row)) {
				for (int col = 0; col < AbEqualized[0].length; col++) {
					AbEqualized[row][col] = 0;
				}
			} else {
				// Otherwise copy the values from Ab
				System.arraycopy(Ab[rowInAb], 0, AbEqualized[row], 0, Ab[0].length);
				rowInAb++;
			}
		}

		return AbEqualized;
	}

	// Finds the solution(s) by looking at where there are zeros on the diagonal of the equalized extended coefficient matrix
	private static double[][] getSolutions(double[][] Ab, int m, int n, int dim) {
		// Create a new array to store found the solutions
		double[][] xhs = new double[m][dim + 1];

		// Keeps track of which column of the solutions array to fill next
		int solutionCol = 0;

		// Loop over the diagonal elements of matrix A
		for (int i = 0; i < Ab.length; i++) {
			// If there is a zero to be found, make it a -1 and store all values of the column to the solutions array
			if (Ab[i][i] == 0) {
				Ab[i][i] = -1;

				for (int row = 0; row < Ab.length; row++) {
					xhs[row][solutionCol] = Ab[row][i];
				}

				solutionCol++;
			}
		}

		// Adds the special solution xs from the last column to the solutions array
		for (int i = 0; i < m; i++) {
			xhs[i][dim] = Ab[i][n];
		}

		return xhs;
	}
}