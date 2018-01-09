import mpi.*;

/**
 * just backup :)
 */
public class Convert {

    public static void main(String[] args) {
        MPI.Init(args);
        int myRank = MPI.COMM_WORLD.Rank();
        if(myRank == 0) {
            char[]message = "Hello there".toCharArray();
            MPI.COMM_WORLD.Send(message, 0, message.length, MPI.CHAR, 1, 99);
        } else {
            char[] message = new char [20];
            MPI.COMM_WORLD.Recv(message, 0, 20, MPI.CHAR, 0, 99);
            System.out.println("received:" + new String(message) + ":");
        }
        MPI.Finalize();
    }

    public void helloWorldRes(String[] args) {
        MPI.Init(args);
        int myRank = MPI.COMM_WORLD.Rank();
        int mySize = MPI.COMM_WORLD.Size();
        System.out.println("Hello " + MPI.Get_processor_name() + " rank " + myRank + " size " + mySize);
        MPI.Finalize();
    }

}
