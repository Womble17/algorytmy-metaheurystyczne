
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;

public class Main {


    private static int POPULATION_SIZE = 200;
    private static double P = 0.5;
    private static double MAX_FAIL = 20000;



    private static long maxTime;

    private static int size;
    private static double[][] coordinates;
    private static double[][] distances;

    private static int[] greed;
    private static int[][] population;

    private static int[] bestRouteGlobal;
    private static double bestGlobalLength = Double.MAX_VALUE;

    private static long start;


    public static void main(String[] args) {


        try {
            readData();
        } catch (IOException var7) {
            var7.printStackTrace();
        }

        start = System.currentTimeMillis();
        createDistancesArray();

        long timeStart = System.nanoTime();
        runGeneticTSP();
        long timeStop = System.nanoTime();
        double time = ((double) timeStop - (double) timeStart) / 1.0E9D;
        //System.out.println("\n------------------------------\ntime: " + time);
        System.out.println(calculateDistance(bestRouteGlobal));
        //System.out.println("------------------------------\n");
        printArray(bestRouteGlobal);
        //System.out.println("------------------------------\n");
    }

    public static void runGeneticTSP(){
        findGreedySolution();

        setParams();

        //System.out.println("GREEDY: " + calculateDistance(bestRouteGlobal));
        greed = bestRouteGlobal.clone();
        for(int i = 0; i < POPULATION_SIZE; ++i){
            population[i] = bestRouteGlobal.clone();
        }

        kochajtaIRobtaCoChceta();

    }

    private static void setParams() {
        if(size < 500){
            MAX_FAIL = 200000;
        }
        else {
            MAX_FAIL = 20000;
        }

    }

    private static void kochajtaIRobtaCoChceta() {
        int[] child;
        Random rand = new Random();
        double childLength;
        int c1, c2, r1, r2, r3, temp, failCounter = 0;

        //-------------------------------------------ALGORYTM-------------------------------------------
        while(System.currentTimeMillis() - start < maxTime){

            for(int i = 0; i < POPULATION_SIZE; ++i){
                if(calculateDistance(population[i]) < bestGlobalLength){
                    bestGlobalLength = calculateDistance(population[i]);
                    bestRouteGlobal = population[i].clone();
                }
            }

            for(int pop = 0; pop < POPULATION_SIZE; ++pop){

                child = population[pop].clone();
                r1 = rand.nextInt(size-2) + 1;
                c1 = child[r1];
                while(System.currentTimeMillis() - start < maxTime) {
                    r2 = rand.nextInt(size-2) + 1;

                    if (rand.nextDouble() > P) {
                        c2 = child[r2];
                    } else {
                        r3 = rand.nextInt(POPULATION_SIZE);
                        int j = 0;
                        while(population[r3][j] != c1) ++j;
                        //System.out.println(j + " " + c1);
                        //printArray(population[r3]);
                        if(j == size){
                            continue;
                        }
                        c2 = population[r3][++j];

                    }
                    if(child[r1+1] == c2){
                        break;
                    }
                    int j = 1;
                    while(child[j] != c2) ++j;
                    if(j > r1){
                        for(int i = 0; r1+1+i < j - i; ++i){
                            temp = child[r1+1+i];
                            child[r1+1+i] = child[j-i];
                            child[j-i] = temp;
                        }
                    }
                    else{
                        temp = child[r1+1];
                        child[r1+1] = child[j];
                        child[j] = temp;
                    }

                    if(child[0] != child[size]){
                        child = greed.clone();
                    }
                }
                childLength = calculateDistance(child);

                if(childLength < calculateDistance(population[pop])) {
                    population[pop] = child.clone();
                    failCounter = 0;

                    if (childLength < bestGlobalLength) {
                        bestGlobalLength = childLength;
                        bestRouteGlobal = child.clone();
                        //System.out.println("new best: " + bestGlobalLength);
                        //printArray(child);
                    }

                }
                else{
                    ++failCounter;
                    if(failCounter > MAX_FAIL){
                        //System.out.println("no more improvement");
                        return;
                    }
                }

            }

        }
    }



    private static int[] randomRoute() {
        int[] temp = new int[size + 1];
        findFirstSolution(temp);
        Random rand = new Random();

        for (int i = 1; i < size; ++i) {
            swapRouteVertices(temp, i, rand.nextInt(size-1)+1);
        }

        return temp;
    }

    private static double swapRouteVerticesCalculateLength(int[] route, int v1, int v2) {
        if (v1 != 0 && v2 != 0) {
            double t = 0.0D;
            int temp;
            if (Math.abs(v1 - v2) == 1) {
                if (v1 > v2) {
                    temp = v1;
                    v1 = v2;
                    v2 = temp;
                }

                t -= distances[route[v1]][route[v1 - 1]] + distances[route[v2]][route[v2 + 1]];
                temp = route[v1];
                route[v1] = route[v2];
                route[v2] = temp;
                t += distances[route[v1]][route[v1 - 1]] + distances[route[v2]][route[v2 + 1]];
            } else {
                t -= distances[route[v1]][route[v1 - 1]] + distances[route[v1]][route[v1 + 1]] + distances[route[v2]][route[v2 - 1]] + distances[route[v2]][route[v2 + 1]];
                temp = route[v1];
                route[v1] = route[v2];
                route[v2] = temp;
                t += distances[route[v1]][route[v1 - 1]] + distances[route[v1]][route[v1 + 1]] + distances[route[v2]][route[v2 - 1]] + distances[route[v2]][route[v2 + 1]];
            }

            return t;
        } else {
            return 0.0D;
        }
    }

    private static double checkSwapResult(int[] route, int v1, int v2) {
        if (v1 != 0 && v2 != 0) {
            double t = 0.0D;
            if (Math.abs(v1 - v2) == 1) {
                if (v1 > v2) {
                    int temp = v1;
                    v1 = v2;
                    v2 = temp;
                }

                t -= distances[route[v1]][route[v1 - 1]] + distances[route[v2]][route[v2 + 1]];
                t += distances[route[v1]][route[v2 + 1]] + distances[route[v2]][route[v1 - 1]];
            } else {
                t += distances[route[v2]][route[v1 - 1]] + distances[route[v2]][route[v1 + 1]] + distances[route[v1]][route[v2 - 1]] + distances[route[v1]][route[v2 + 1]];
                t -= distances[route[v1]][route[v1 - 1]] + distances[route[v1]][route[v1 + 1]] + distances[route[v2]][route[v2 - 1]] + distances[route[v2]][route[v2 + 1]];
            }

            return t;
        } else {
            return 0.0D;
        }
    }

    private static void swapRouteVertices(int[] route, int v1, int v2) {
        int temp = route[v1];
        route[v1] = route[v2];
        route[v2] = temp;
    }

    private static void findFirstSolution(int[] route) {
        boolean best = false;

        for (int i = 0; i < route.length - 1; route[i] = i++) {
            ;
        }

        route[route.length - 1] = 0;
    }

    private static void findGreedySolution() {
        findFirstSolution(bestRouteGlobal);

        for (int i = 1; i < size - 1; ++i) {
            double bestDistance = distances[bestRouteGlobal[i]][bestRouteGlobal[i + 1]];
            int swapId = i + 1;

            for (int j = i + 2; j < size; ++j) {
                if (distances[bestRouteGlobal[i]][bestRouteGlobal[j]] < bestDistance) {
                    bestDistance = distances[bestRouteGlobal[i]][bestRouteGlobal[j]];
                    swapId = j;
                }
            }

            swapRouteVertices(bestRouteGlobal, i + 1, swapId);
        }

        bestGlobalLength = calculateDistance(bestRouteGlobal);
    }

    private static void createDistancesArray() {
        distances = new double[size][size];

        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < i; ++j) {
                distances[i][j] = distances[j][i] = Math.sqrt((coordinates[i][0] - coordinates[j][0]) * (coordinates[i][0] - coordinates[j][0]) + (coordinates[i][1] - coordinates[j][1]) * (coordinates[i][1] - coordinates[j][1]));
            }
        }

    }

    private static void printArray(int[][] a) {
        for (int i = 0; i < a.length; ++i) {
            for (int j = 0; j < a[i].length; ++j) {
                System.out.print(a[i][j] + 1 + " ");
            }

            System.out.println();
        }

        System.out.println("\n");
    }

    private static void printArray(double[][] a) {
        for (int i = 0; i < a.length; ++i) {
            for (int j = 0; j < a[i].length; ++j) {
                System.out.print(String.format("%.5f", new Object[]{Double.valueOf(a[i][j])}) + " ");
            }

            System.out.println();
        }

        System.out.println("\n");
    }

    private static void printArray(int[] a) {
        for (int i = 0; i < a.length; ++i) {
            System.err.print(a[i] + 1 + " ");
        }

        System.err.println("\n");
    }

    private static double calculateDistance(int[] route) {
        double distance = 0.0D;

        for (int i = 0; i < size; ++i) {
            distance += distances[route[i]][route[i + 1]];
        }

        return distance;
    }
/*
    private static void readDataFle() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("/home/marcin/Programming/GeneticTSP/src/com/company/in.txt"));
        Throwable var1 = null;

        try {
            String line = br.readLine();
            size = Integer.parseInt(line);
            coordinates = new double[size][2];
            bestRouteGlobal = new int[size + 1];
            population = new int[POPULATION_SIZE][];

            for(int i = 0; i < size; ++i) {
                line = br.readLine();
                String[] data = line.split(" ");
                coordinates[i][0] = Double.parseDouble(data[1]);
                coordinates[i][1] = Double.parseDouble(data[2]);
            }
            maxTime = 1000 * Long.parseLong(br.readLine());

        } catch (Throwable var12) {
            var1 = var12;
            throw var12;
        } finally {
            if(br != null) {
                if(var1 != null) {
                    try {
                        br.close();
                    } catch (Throwable var11) {
                        var1.addSuppressed(var11);
                    }
                } else {
                    br.close();
                }
            }

        }

    }
*/
    private static void readData() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String firstLine = br.readLine();
        size = Integer.parseInt(firstLine);
        coordinates = new double[size][2];
        bestRouteGlobal = new int[size + 1];
        population = new int[POPULATION_SIZE][];

        for (int i = 0; i < size; ++i) {
            String[] data = br.readLine().split(" ");
            coordinates[i][0] = Double.parseDouble(data[1]);
            coordinates[i][1] = Double.parseDouble(data[2]);
        }

        maxTime = 1000 * Long.parseLong(br.readLine());

    }


}
