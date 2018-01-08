import mpi.*;

/**
 * Source: https://www.daniweb.com/programming/software-development/code/334470/matrix-multiplication-using-mpi-parallel-programming-approach
 */
public class SourceTyrantbrian {

    //banyaknya baris dan kolom matriks
    private static int NUM_ROW_A = 10;
    private static int NUM_ROW_B = 10;
    private static int NUM_COLUMNS_A = 10;
    private static int NUM_COLUMNS_B = 10;

    //penampung
    private static double matA[][]; //penampung matriks A
    private static double matB[][]; //penampung matriks B

    //waktu
    private static double startTime; //waktu mulai
    private static double endTime; //waktu akhir

    private static int upperBound; //batas atas jumlah baris [A] yanng dialokasikan ke slave
    private static int lowBound; //batas bawah jumlah baris [A] yanng dialokasikan ke slave
    private static int portion; //bagian dari jumlah baris [A] untuk di alokasikan ke slave

    private static int MASTER_TO_SLAVE_TAG = 1;
    private static int SLAVE_TO_MASTER_TAG = 4;

    public static void mainTest(String[] args) {
        matA = new double[NUM_ROW_A][NUM_COLUMNS_A];
        matB = new double[NUM_ROW_B][NUM_COLUMNS_B];

        MPI.Init(args); //inisialisasi MPI
        int rank = MPI.COMM_WORLD.Rank(); //rank
        int size = MPI.COMM_WORLD.Size(); //size
        if (rank == 0) {
            makeAB(); //inisialisasi value untuk matriks A dan B
            startTime = MPI.Wtime();
            for (int i=0; i < size; i++) { //loop untuk setiap slave selain master
                portion = (NUM_ROW_A / (size - 1)); //menghitung bagian tanpa master
                lowBound = (i - 1) * portion;
                //jika baris dari [A] tidak bisa dibagi rata antara slaves
                if (((i + 1) == size) && ((NUM_ROW_A % (size - 1)) != 0)) {
                    upperBound = NUM_ROW_A; //slave terakhir dapetin semua baris yang tersisa
                } else {
                    //klau ini, baris [A] bisa habis dibagi antara slaves
                    upperBound = lowBound + portion;
                }
                //terlebih dahulu, ngirimin batas bawah tanpa bloking, ke tujuan slave
                MPI.COMM_SELF.Send(lowBound, 1, i, MPI.INT, MASTER_TO_SLAVE_TAG, MASTER_TO_SLAVE_TAG);
                //selanjutnya, kirim batas atas
                MPI.COMM_SELF.Send(upperBound, 1, i, MPI.INT, MASTER_TO_SLAVE_TAG + 1, MASTER_TO_SLAVE_TAG);
                //dan finally! kirim alokasi bagian dari baris [A] tanpa bloking ke slave
                MPI.COMM_SELF.Send(matA[lowBound][0], (upperBound - lowBound) * NUM_COLUMNS_A, i, MPI.DOUBLE, MASTER_TO_SLAVE_TAG + 2, MASTER_TO_SLAVE_TAG);
            }
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

    private static void printMatrix() {
        for (int i = 0; i < NUM_ROW_A; i++) {
            System.out.print("\n");
            for (int j = 0; j < NUM_COLUMNS_A; j++) {
                System.out.print(matA[i][j] + " ");
            }
        }
        System.out.print("\n\n\n");
        System.out.print("MATRIS B:\n");
        for (int i = 0; i < NUM_ROW_B; i++) {
            System.out.print("\n");
            for (int j = 0; j < NUM_COLUMNS_B; j++) {
                System.out.print(matB[i][j] + " ");
            }
        }
        System.out.print("\n\n\n");
        /* for (int i = 0; i < NUM_ROW_A; i++) {
            printf("\n");
            for (int j = 0; j < NUM_COLUMNS_B; j++)
                printf("%8.2f  ", mat_result[i][j]);
        }
        printf("\n\n"); */
    }
}
