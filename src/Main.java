import mpi.*;

/**
 * Sargunan Arujanan
 * Muin bin Abdullah
 * convert by @isfaaghyth (JVM)
 */
public class Main {

    //banyaknya baris dan kolom matriks
    private static int MATRIX_TOTAL = 10;

    //penampung
    private static double matA[][]; //penampung matriks A
    private static double matB[][]; //penampung matriks B

    private static double matC[][];

    //waktu
    private static double startTime; //waktu mulai
    private static double endTime; //waktu akhir

    private static int portion;

    private static int MASTER_TO_SLAVE_TAG = 1;
    private static int SLAVE_TO_MASTER_TAG = 4;

    public static void main(String[] args) {

        matA = new double[MATRIX_TOTAL][MATRIX_TOTAL];
        matB = new double[MATRIX_TOTAL][MATRIX_TOTAL];
        matC = new double[MATRIX_TOTAL][MATRIX_TOTAL];

        MPI.Init(args); //inisialisasi MPI
        int rank = MPI.COMM_WORLD.Rank(); //rank
        int size = MPI.COMM_WORLD.Size(); //size

        portion = (MATRIX_TOTAL / (size - 1)); //menghitung bagian tanpa master

        if (rank == 0) {
            makeAB(); //inisialisasi value untuk matriks A dan B
        }

        for (int i=0; i < MATRIX_TOTAL; i++) {
            MPI.COMM_WORLD.Bcast(matB[i], MATRIX_TOTAL*MATRIX_TOTAL, 0, MPI.INT, 0);
        }

        for (int i=0; i < size; i++) {
            for (int j=0; j < MATRIX_TOTAL; j++) {
                MPI.COMM_WORLD.Send(matA[j], MATRIX_TOTAL*portion, 0, MPI.INT, MASTER_TO_SLAVE_TAG, MASTER_TO_SLAVE_TAG);
                portion++;
            }
        }

        MPI.COMM_WORLD.Recv(matA, MATRIX_TOTAL*portion, 0, MPI.INT, MASTER_TO_SLAVE_TAG, MASTER_TO_SLAVE_TAG);

        startTime = MPI.Wtime();
        System.out.println("Start time: " + startTime);

        for (int i=0; i < MATRIX_TOTAL; i++) {
            for (int j=0; j < MATRIX_TOTAL; j++) {
                matC[i][j] = 0;
                for (int k=0; k < MATRIX_TOTAL; k++) {
                    matC[i][j] = matC[i][j] + matA[j][k] * matB[k][i];
                }
            }
        }

        endTime = MPI.Wtime();
        System.out.println("End time: " + endTime);


    }

    private static void makeAB() {
        for (int i = 0; i < MATRIX_TOTAL; i++) {
            for (int j = 0; j < MATRIX_TOTAL; j++) {
                matA[i][j] = i + j;
            }
        }
        for (int i = 0; i < MATRIX_TOTAL; i++) {
            for (int j = 0; j < MATRIX_TOTAL; j++) {
                matB[i][j] = i * j;
            }
        }
    }
}