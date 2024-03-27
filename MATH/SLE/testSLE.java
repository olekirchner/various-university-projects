import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

    /**
        Lineare Gleichungssysteme  
        In der Klasse SLE.java müssen Sie folgende Methoden schreiben
            
        public static double[][] calcNF2(double[][] Ab) und
        public static double[][] solve(double[][] Ab)
            
        Eine Testdatei ist Test.txt; Format: s. Programmieraufgaben.pdf.
          
        SLE:  System of Linear Equations Ax = b
        NF2:  Zweite Normalform
        xhs:  xhs = Lös(A, b) = (xh|xs), wenn xh Lösungsvektoren des homogenen Systems Ax = 0 und xs spezielle Lösung des 
              inhomogenen Systems sind.
     */

public class testSLE {
    public testSLE(String filename){
        test(filename);
    }

    public void test(String filename) {
        int nrOfDigits = 1; // für die Formatierung der Anzeige
        double eps = 0.000001; // Fehlerschranke für die Testmethode solutionOK
        
        double[][] Ab = readMatrixFromFile(filename);
        if (Ab == null) return;
        System.out.println("A|b:");
        showMatrix(Ab, nrOfDigits);
        System.out.println();
        System.out.println("Â in 2.NF:");
        double[][] NF2 = SLE.calcNF2(Ab);
        showMatrix(NF2, nrOfDigits);
        System.out.println();
        
        double[][] xhs = SLE.solve(Ab);
        if (xhs==null){
            System.out.println("Das Gleichungssystem hat keine Lösung.");
        } else {
            System.out.println("Lös(A, b)");
            showMatrix(xhs, nrOfDigits);
            System.out.println();
            if (solutionOK(Ab, xhs, eps)) System.out.println("Test bestanden");
            else System.out.println("Test nicht bestanden");
        }

        System.out.println("––––––––––––––––––––––––––––––––––––––––––––––––––");
    }
    
    // Liest die quadratische Matrix aus einer Textdatei; s. Programmieraufgaben.pdf bezüglich des Formats.
    public static double[][] readMatrixFromFile(String filename){
        ArrayList<String> stringList = new ArrayList<String>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();
            while (line!=null){
                stringList.add(line);
                line = br.readLine();
            }
            br.close();
            
            String[] parts = stringList.get(0).split("  ");
            int m = stringList.size(), n = parts.length;
            double[][] M = new double[m][n];
            for (int i=0; i<m; i++){
                parts = stringList.get(i).split("  ");
                for (int j=0; j<n; j++) M[i][j] = Double.valueOf(parts[j]);
            }
            return M;
        }
        catch(IOException e){
            System.out.println(""+e);
            return null;
        }
    }

    // Schreibt die Matrix M in die Konsole; die Koeffizienten werden auf nrOfDigits Stellen gerundet.
    public static void showMatrix(double[][] M, int nrOfDigits){
        int m = M.length, n = M[0].length;
        //boolean hasNoNegativeEntry = true;
        double max = 0.0;
        for (int j=0; j<n; j++){
            for (int i=0; i<m; i++){
                if (M[i][j]>max) max = M[i][j];
                //if (M[i][j]<0.0) hasNoNegativeEntry = false; 
            }
        }
        int l;
        if (max==0.0) l = 5;
        else l = (int) Math.log10(Math.abs(max))+nrOfDigits+4; // +1: log, +1: sign, +1: point, +1
        if (nrOfDigits==0) l--;
        //if (hasNoNegativeEntry) l--;

        String f, s;
        f = "%"+l+"."+nrOfDigits+"f";
        for (int i=0; i<m; i++){
            s = "";
            for (int j=0; j<n; j++){
                s = s+String.format(f, M[i][j]);    
            }
            System.out.println(s);
        }
    }

    public static boolean solutionOK(double[][] Ab, double[][] xhs, double eps){
        int m = Ab.length, n = Ab[0].length-1, dim = xhs[0].length-1;
        double sum;
        for (int k=0; k<dim; k++){
            for (int i=0; i<m; i++){
                sum = 0.0;
                for (int j=0; j<n;j++){
                    sum = sum+Ab[i][j]*xhs[j][k];
                }
                if (sum!=0.0){
                    System.out.println("Lösung von A·x = 0: (A·xh["+k+"])["+i+"] = "+sum);
                    System.out.println();
                    if (Math.abs(sum)<eps){
                        System.out.println("Fehler < "+eps);
                        return true;
                    }
                    return false;
                }
            }
        }
        for (int i=0; i<m; i++){
            sum = 0.0;
            for (int j=0; j<n;j++){
                sum = sum+Ab[i][j]*xhs[j][dim];
            }
            if (sum!=Ab[i][n]){
                System.out.println("Spezielle Lösung: (A·xs)["+i+"] = "+sum+" != b["+i+"]!");
                System.out.println();
                if (Math.abs(sum-Ab[i][n])<eps){
                    System.out.println("Fehler < "+eps);
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        new testSLE("Test.txt");
        new testSLE("Test1.txt");
        new testSLE("Test2.txt");
        new testSLE("Test3.txt");
        new testSLE("Test4.txt");
        new testSLE("Test5.txt");
        new testSLE("Test6.txt");
        new testSLE("Test7.txt");
        new testSLE("Test8.txt");
        new testSLE("Test9.txt");
    }
}
