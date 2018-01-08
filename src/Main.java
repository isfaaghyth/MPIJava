import mpi.*;

/**
 * Sargunan Arujanan
 * Muin bin Abdullah
 * convert by @isfaaghyth (JVM)
 */
public class Main {

    //banyaknya baris dan kolom matriks
    private static int AMOUNT_MATRIX = 10;

    private static int NUM_ROW_A = AMOUNT_MATRIX;
    private static int NUM_ROW_B = AMOUNT_MATRIX;
    private static int NUM_COLUMNS_A = AMOUNT_MATRIX;
    private static int NUM_COLUMNS_B = AMOUNT_MATRIX;

    //penampung
    private static double matA[][]; //penampung matriks A
    private static double matB[][]; //penampung matriks B

    //waktu
    private static double startTime; //waktu mulai
    private static double endTime; //waktu akhir

    private static int portion;

    public static void main(String[] args) {
        matA = new double[NUM_ROW_A][NUM_COLUMNS_A];
        matB = new double[NUM_ROW_B][NUM_COLUMNS_B];

        MPI.Init(args); //inisialisasi MPI
        int rank = MPI.COMM_WORLD.Rank(); //rank
        int size = MPI.COMM_WORLD.Size(); //size

        makeAB(); //inisialisasi value untuk matriks A dan B
        portion = (NUM_ROW_A / (size - 1)); //menghitung bagian tanpa master

        for (int i=0; i < AMOUNT_MATRIX; i++) {
            MPI.COMM_WORLD.Bcast(matB[i], AMOUNT_MATRIX*AMOUNT_MATRIX, 0, MPI.INT, 0);
        }
    }

    private static void makeAB() {
        for (int i = 0; i < NUM_ROW_A; i++) {
            for (int j = 0; j < NUM_COLUMNS_A; j++) {
                matA[i][j] = i + j;
            }
        }
        for (int i = 0; i < NUM_ROW_B; i++) {
            for (int j = 0; j < NUM_COLUMNS_B; j++) {
                matB[i][j] = i*j;
            }
        }
    }
}