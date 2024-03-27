// Imports
import java.util.ArrayList;

public class Permutations {
    public static void main(String[] args) {
        // Task 1 output
        System.out.println("--------------------------------------------------");
        System.out.println("Task 1");
        int[] lehmerCode = getLehmerCode(new int[]{1,3,2,6,5,4});
        for (int number : lehmerCode) {
            System.out.print(number + " ");
        }
        System.out.println();

        // Task 2 output
        System.out.println("--------------------------------------------------");
        System.out.println("Task 2");
        int[] perm =  getPerm(new int[]{0,1,0,2,1,0});
        for (int number : perm) {
            System.out.print(number + " ");
        }
        System.out.println();

        // Task 3 output
        System.out.println("--------------------------------------------------");
        System.out.println("Task 3");
        showPerm(new int[]{2,4,6,5,1,3});
        System.out.println("--------------------------------------------------");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Helper methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Method to create an ArrayList of numbers from 1 to n
     * @param n - Highest/last numbers to be put into the ArrayList
     * @return ArrayList of numbers from 1 to n
     */
    public static ArrayList<Integer> getNumbersArrayList(int n) {
        // Create a new empty array list which should contain integers
        ArrayList<Integer> numbersArrayList = new ArrayList<>();

        // Fill the list with numbers
        for (int i = 0; i < n; i++) {
            numbersArrayList.add(i + 1);
        }

        // Return the list
        return numbersArrayList;
    }

    /**
     * Method to get the faculty of a passed integer n
     * @param n - Integer whose faculty is to be calculated
     * @return faculty of n as an integer
     */
    public static int getFaculty(int n) {
        // Create new variable and initialise it with the passed integer n, which gets returned in the end and is also the first factor to be multiplied with
        int facultyOfN = n;

        for (int i = 0; i < n - 1; i++) {
            // Decrease factor every iteration
            int factor = n - i - 1;

            // Multiply the current product with the next factor
            facultyOfN *= factor;
        }

        // Return the faculty as an integer
        return facultyOfN;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Task 1
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Method to get the lehmer code as an array of the passed array of numbers of a permutation
     * @param perm - Array of numbers of a permutation
     * @return array of the lehmer code numbers
     */
    public static int[] getLehmerCode(int[] perm) {
        // Create a new array with the same length of the perm array
        int[] lehmerCode = new int[perm.length];

        // Iterate over every position in the lehmerCode array
        for (int i = 0; i < perm.length; i++) {
            // Initialize the current lehmer number with 0
            int lehmerNumber = 0;

            // Iterate over every position after the position of the iterator i
            for (int j = i + 1; j < perm.length; j++) {
                // Increment the lehmer number every time an inversion is found
                if (perm[i] > perm[j]) {
                    lehmerNumber += 1;
                }
            }

            // Assign the current lehmer number to the lehmer code array at the position of the iterator i
            lehmerCode[i] = lehmerNumber;
        }

        // Return the lehmer code as an array
        return lehmerCode;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Task 2
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Method to get the perm belonging to the passed lehmer code
     * @param lehmerCode - Array of a lehmer code numbers
     * @return permutation belonging to the given lehmer code
     */
    public static int[] getPerm(int[] lehmerCode) {
        // Create a new array list of numbers
        ArrayList<Integer> numbers = getNumbersArrayList(lehmerCode.length);

        // Create a new array and initialise it with the length of the lehmer code
        int[] perm = new int[lehmerCode.length];

        // Iterate over every number of the lehmer code
        for (int i = 0; i < lehmerCode.length; i++) {
            int index = lehmerCode[i];

            // Put the number from the numbers array at the current index into the newly created permutation array
            perm[i] = numbers.get(index);

            // Remove this number afterwords, because we have already used it
            numbers.remove(index);
        }

        // Return the permutation as an array
        return perm;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Task 3
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Method to get the lexicographical order of any passed permutation as a two-dimensional array
     * @param perm - Array of numbers of a permutation
     * @return all n! permutations lexicographically as a two-dimensional array
     */
    public static int[][] getLexicographicalOrder(int[] perm) {
        // Get the number of different possibilities and initialize a new two-dimensional array afterwards
        int numberOfPossibilities = getFaculty(perm.length);
        int[][] lexicographicalOrder = new int[numberOfPossibilities][];

        // Iterate over every number between 0 and the number of possibilities
        for (int i = 0; i < numberOfPossibilities; i++) {
            int dividend = i;
            int[] lehmerCode = new int[perm.length];

            // Iterate over the length of the passed permutation
            for (int j = 0; j < perm.length; j++) {
                // Initialise a divisor
                int divisor = j + 1;

                // Build the lehmer code backwards from the remainders of the quotients
                lehmerCode[perm.length - j - 1] = dividend % divisor;

                // Divide the dividend by the divisor
                dividend /= divisor;
            }

            // Assign the permutation of the current lehmer code to the current position of the two-dimensional array
            lexicographicalOrder[i] = getPerm(lehmerCode);
        }

        // Return the array
        return lexicographicalOrder;
    }

    /**
     * Method to get the cycle representation of any passed permutation as a two-dimensional array
     * @param perm - Array of numbers of a permutation
     * @return two-dimensional array containing all cycles of the passed perm
     */
    public static int[][] getCycleRepresentation(int[] perm) {
        // Create a new empty two-dimensional array list
        ArrayList<ArrayList<Integer>> cycleRepresentationList = new ArrayList<>();

        // Get an array list with the numbers from 1 to the value of the length of the passed permutation
        ArrayList<Integer> numbers = getNumbersArrayList(perm.length);

        // Create a boolean array to keep track of which numbers we've already looked at
        boolean[] isVisited = new boolean[perm.length];

        // Loop over every number of the permutation
        for (int i = 0; i < perm.length; i++) {
            // Create a one dimensional array list for every cycle
            ArrayList<Integer> cycle = new ArrayList<>();

            // Start a new cycle by iterating through the permutation while the current number is not visited
            while (!isVisited[i]) {
                // Mark the current number as visited and add it to the cycle
                isVisited[i] = true;
                cycle.add(numbers.get(i));

                // Move to the next number in the cycle
                if (!isVisited[perm[i] - 1]) {
                    i = perm[i] - 1;
                } else {
                    // Break the cycle if we encounter a number that has already been visited
                    break;
                }
            }

            // Only add the cycle to the list if it contains more than 1 number, because we don't want to display numbers that map to themselves
            if (cycle.size() > 1) {
                cycleRepresentationList.add(cycle);
            }
        }

        // Convert the ArrayList of cycles into a two-dimensional array for the final result
        int[][] cycleRepresentation = new int[cycleRepresentationList.size()][];

        for (int i = 0; i < cycleRepresentation.length; i++) {
            // Get the cycle from the cycle representation list at the index of the iterator i
            ArrayList<Integer> cycleList = cycleRepresentationList.get(i);

            // Create a new empty cycle array with the length of the current cycle we look at
            int[] cycle = new int[cycleList.size()];

            // Fill the current cycle array with the values from the list at the index of the iterator i
            for (int j = 0; j < cycleList.size(); j++) {
                cycle[j] = cycleList.get(j);
            }

            // Add the current cycle to the cycle representation list at the index of iterator i
            cycleRepresentation[i] = cycle;
        }

        // Return the cycle representation in the form of a two-dimensional array
        return cycleRepresentation;
    }

    /**
     * Checks whether the number of inversions is even or odd and returns the signum of 1, if the number of inversions is even and the signum of -1, if the number of inversions is odd
     * @param perm - Array of numbers of a permutation
     * return the signum as an integer
     */
    public static int getSignum(int[] perm) {
        // Get the lehmer code of the permutation, because it contains the number of inversions
        int[] lehmerCode = getLehmerCode(perm);

        // Initialize a count for the number of inversions
        int inversions = 0;

        // Get the total amount of inversions by adding up the numbers of the lehmer code
        for (int number : lehmerCode) {
            inversions += number;
        }

        // Return an integer representing the signum
        return (inversions % 2 == 0) ? 1 : -1;
    }

    /**
     * Shows the passed permutation as well as its lehmer code, its cycle representation, its signum and its lexicographical order
     * @param perm - Array of numbers of a permutation
     */
    public static void showPerm(int[] perm) {
        // Outputs the passed permutation
        System.out.print("Permutation: ");
        for (int number : perm) {
            System.out.print(number + " ");
        }
        System.out.println();

        // Outputs the lehmer code of the passed permutation
        System.out.print("Lehmer code: ");
        int[] lehmerCode = getLehmerCode(perm);
        for (int number : lehmerCode) {
            System.out.print(number + " ");
        }
        System.out.println();

        // Outputs the cycle representation of the passed permutation
        System.out.print("Cycle representation: ");
        int[][] cycleRepresentation = getCycleRepresentation(perm);
        if (cycleRepresentation.length == 0) {
            System.out.print("id");
        } else {
            for (int[] row : cycleRepresentation) {
                System.out.print("(");
                for (int number : row) {
                    System.out.print(number);
                }
                System.out.print(")");
            }
        }
        System.out.println();

        // Outputs the signum of the passed permutation
        System.out.println("Signum: " + getSignum(perm));

        // Outputs the lexicographical order of the passed permutation
        System.out.println("Lexicographical order:");
        int[][] lexicographicalOrder = getLexicographicalOrder(perm);
        for (int[] row : lexicographicalOrder) {
            for (int number : row) {
                System.out.print(number + " ");
            }
            System.out.println();
        }
    }
}