import mpi.*;

/**
 * user3411002
 * stackoverflow: 29800480
 */
public class MultidimMatrix {

    public static final int N = 10;

    public static void main (String args[]){
        MPI.Init(args);
        long startTime = System.currentTimeMillis();

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int tag = 10, peer = (rank==0) ? 1:0;

        if(rank == 0) {
            double [][] a = new double [N][N];
            for(int i = 0; i < N; i++) {
                for(int j = 0; j < N; j++) {
                    a[i][j] = 10.0;
                }
            }
            Object[] sendObjectArray = new Object[1];
            sendObjectArray[0] = a;
            MPI.COMM_WORLD.Send(sendObjectArray, 0, 1, MPI.OBJECT, peer, tag);
        } else if(rank == 1){
            double [][] b = new double [N][N];
            for(int i = 0; i < N; i++) {
                for(int j = 0; j < N; j++) {
                    b[i][j] = 0;
                }
            }
            Object[] recvObjectArray = new Object[1];
            MPI.COMM_WORLD.Recv(recvObjectArray, 0, 1, MPI.OBJECT, peer, tag);
            b = (double[][]) recvObjectArray[0];
            for(int i = 0; i < N; i++){
                for(int j = 0; j < N; j++) {
                    long endTime = System.currentTimeMillis();
                    System.out.print(b[i][j]+"\t");
                    System.out.println("\n");
                    System.out.println("Calculated in " + (endTime - startTime) + " milliseconds");
                }
            }
        }
        MPI.Finalize();
    }
}