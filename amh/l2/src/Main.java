import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class Main {


    private static final double T_MIN = 0.0000001D;
    private static final double ALPHA = 0.995;
    private static final double T_STARTING = 1.0D;

    private static long maxTime;

    private static int size;
    private static double[][] coordinates;
    private static double[][] distances;

    private static int[] bestRouteGlobal;
    private static double bestGlobalLength = Double.MAX_VALUE;




    public static void main(String[] args) {
        try {
            readData();
        } catch (IOException var7) {
            var7.printStackTrace();
        }

        createDistancesArray();

        long timeStart = System.nanoTime();
        runSimulatedAnnealingTSP();
        long timeStop = System.nanoTime();
        double time = ((double) timeStop - (double) timeStart) / 1.0E9D;
        //System.out.println("\n------------------------------\ntime: " + time);
        System.out.println(calculateDistance(bestRouteGlobal));
        errorPrintArray(bestRouteGlobal);

    }

    private static void runSimulatedAnnealingTSP() {

        long startTime = System.currentTimeMillis();
        findGreedySolution();

        int[] tempSolution = bestRouteGlobal.clone();
        double tempSolutionLength;
        int noImprovementCounter = 0;

        while(System.currentTimeMillis() - startTime < maxTime){
            tempSolutionLength = simulatedAnnealing(tempSolution);
            ++noImprovementCounter;
            if(bestGlobalLength > tempSolutionLength){
                noImprovementCounter = 0;
                bestGlobalLength = tempSolutionLength;
                bestRouteGlobal = tempSolution.clone();
                //System.out.println("new best solution: " + bestGlobalLength);
            }

            if(noImprovementCounter > 100){
                //System.out.println("no more improvement");
                break;
            }
        }


        //printArray(bestRouteGlobal);
        //System.out.println(bestGlobalLength);


    }

    private static double simulatedAnnealing(int[] solution){

        Random rand = new Random();
        int v1, v2;
        double solutionLength, newLength, ap, T = T_STARTING;
        solutionLength = calculateDistance(solution);

        while(T > T_MIN){
            for(int i = 0; i < 10; ++i){
                v1 = rand.nextInt(size);
                v2 = rand.nextInt(size);
                newLength = solutionLength + checkSwapResult(solution, v1, v2);
                ap = acceptanceProbability(solutionLength, newLength, T);

                if(ap > rand.nextDouble()){
                    //System.out.println("swap");
                    solutionLength += swapRouteVerticesCalculateLength(solution, v1, v2);
                    //System.out.println(newLength + " " + solutionLength);
                }
            }
            T *= ALPHA;
        }

        return solutionLength;
    }

    private static double acceptanceProbability(double tempSolutionLength, double newLength, double t_current) {
        return Math.pow(Math.E, (tempSolutionLength-newLength)/t_current);
    }


    private static int[] randomRoute() {
        int[] temp = new int[size + 1];
        findFirstSolution(temp);
        Random rand = new Random();

        for (int i = 0; i < size; ++i) {
            swapRouteVertices(temp, i, rand.nextInt(size));
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

    private static void printArray(double[][] a) {
        for (int i = 0; i < a.length; ++i) {
            for (int j = 0; j < a[i].length; ++j) {
                System.out.print(String.format("%.5f", new Object[]{Double.valueOf(a[i][j])}) + " ");
            }

            System.out.println();
        }

        System.out.println("\n");
    }

    private static void errorPrintArray(int[] a) {
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

    private static void readData() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String firstLine = br.readLine();
        size = Integer.parseInt(firstLine);
        coordinates = new double[size][2];
        bestRouteGlobal = new int[size + 1];

        for (int i = 0; i < size; ++i) {
            String[] data = br.readLine().split(" ");
            coordinates[i][0] = Double.parseDouble(data[1]);
            coordinates[i][1] = Double.parseDouble(data[2]);
        }

        maxTime = 1000 * Long.parseLong(br.readLine());

    }


}
