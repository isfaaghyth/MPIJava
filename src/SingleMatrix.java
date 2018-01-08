import mpi.*;

import java.util.Random;

/**
 * Single Matrix with openMP
 * @porting isfaaghyth
 * @reference: https://github.com/JING-TIME/C-Programming/blob/master/141029/MatrixMul.c
 * @reference: https://gist.github.com/morris821028/1ee07f52d494217ae26933d352c7f07f
 */
public class SingleMatrix {

    private static final int N = 8;

    private static int matA[] = new int[N * N];
    private static int matB[] = new int[N * N];

    //penampung
    private static int matC[] = new int[N * N];

    private static int porsi; //pembagian task

    public static void main(String[] args) {
        MPI.Init(args); //inisialisasi openMP

        int rank = MPI.COMM_WORLD.Rank(); //get rank
        int size = MPI.COMM_WORLD.Size(); //get size

        //mengisi array matriks A dan B
        //@Using: random range dari 0 s/d 4
        if (rank == 0) {
            for (int i=0; i < N; i++) {
                for (int j=0; j < N; j++) {
                    matA[i * N + j] = new Random().nextInt(4);
                    matB[i * N + j] = new Random().nextInt(4);
                }
            }
            printMatrix("A", matA);
            printMatrix("B", matB);
        }

        porsi = N / size;

        int[] matAi = new int[porsi * N]; //penampung matA / 2
        int[] matBi = new int[N * N]; //penampung matB / 2
        int[] matCi = new int[porsi * N]; //penampung matC / 2

        //mengirim matriks A dari mainProcess(main) ke otherProcess (slave)
        if (rank == 0) {
            for (int i=0; i < porsi; i++) {
                for (int j=0; j < N; j++) {
                    matAi[i * N + j] = matA[i * N + j];
                }
            }
            //kirim :D
            for (int i=1; i < size; i++) {
                MPI.COMM_WORLD.Send(matA, i * porsi * N, porsi * N, MPI.INT, i, i);
            }
        } else {
            //kalau misal rank nya bukan master, di terima
            MPI.COMM_WORLD.Recv(matAi, 0, porsi * N, MPI.INT, 0, rank);
        }

        //mengirim matriks B dari mainProcess(main) ke otherProcess (slave)
        if (rank == 0) {
            for (int i=0; i < N; i++) {
                for (int j=0; j < N; j++) {
                    matBi[i * N + j] = matB[i * N + j];
                }
            }
            //kirim :D
            for (int i=1; i < size; i++) {
                MPI.COMM_WORLD.Send(matB, 0, N * N, MPI.INT, i, i);
            }
        } else {
            //kalau misal rank nya bukan master, di terima
            MPI.COMM_WORLD.Recv(matBi, 0, N * N, MPI.INT, 0, rank);
        }

        //kalkulasi !
        for (int i=0; i < porsi; i++) {
            for (int j=0; j < N; j++) {
                matCi[i * N + j] = 0;
                for (int k=0; k < N; k++) {
                    matCi[i * N + j] += matAi[i * N + k] * matBi[k * N + j];
                    //memastikan saja :))
                    //System.out.println("A =>" + matAi[i * N + k] + " B =>" + matBi[k * N + j]);
                    //System.out.print("HASIL A*B => " + matCi[i * N + j]);
                }
                //System.out.println();
            }
        }

        //kirim hasil dari otherProcess(slave) ke mainProcess(master)
        if (rank != 0) {
            MPI.COMM_WORLD.Send(matCi, 0, porsi * N, MPI.INT, 0, rank);
        } else {
            for (int i=0; i < porsi; i++) {
                for (int j=0; j < N; j++) {
                    matC[i * N + j] = matCi[i * N + j];
                }
            }
            for (int i=1; i < size; i++) {
                MPI.COMM_WORLD.Recv(matC, i * porsi * N, porsi * N, MPI.INT, i, i);
            }
        }

        //tampilkan hasil
        if (rank == 0) {
            System.out.println("Result");
            printMatrix("C", matC);
        }

        //done! :)
        MPI.Finalize();
    }

    private static void printMatrix(String pos, int arr[]) {
        System.out.println("MATRIKS " + pos);
        for (int i=0; i < N; i++) {
            for (int j=0; j < N; j++) {
                System.out.print(arr[i*N+j]+" ");
            }
            System.out.println();
        }
    }

}
