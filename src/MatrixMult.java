/**
 * mpiJava Matrix Multiply:<p>
 *
 * The original program was written in C and converted to MPI. This program
 * adjusted all matrices' rows/columns to the same number, improved the
 * master program, and converted itself to Java.<p>
 *
 * <a href="http://web.cz3.nus.edu.sg/~yzchen/teach/cz3201/mpi3sub1.html">
 *  http://web.cz3.nus.edu.sg/~yzchen/teach/cz3201/mpi3sub1.html</a>
 *
 *
 * @author Ros Leibensperger / Blaise Barney. Converted to MPI: George L. 
 *         Converted to mpiJava: Munehiro Fukuda
 * @since  Original MPI (1/25/95), mpiJava (2/1/05)
 * @version Original MPI (12/14/95), mpiJava (2/1/05)
 */

import mpi.*;           // for mpiJava
import java.net.*;      // for InetAddress
import java.util.*;     // for Date

public class MatrixMult {
    // mpi-related values
    private int myrank = 0;;
    private int nprocs = 0;

    // matrices
    private double a[]; // array must be one dimensional in mpiJava.
    private double b[]; // array must be one dimensional in mpiJava.
    private double c[]; // array must be one dimensional in mpiJava.

    // message component
    private int averows;               // average #rows allocated to each rank
    private int extra;                 // extra #rows allocated to some ranks
    private int offset[] = new int[1]; // offset in row
    private int rows[] = new int[1];   // the actual # rows allocated to each rank
    private int mtype;                 // message type (tagFromMaster or tagFromWorker )

    private final static int tagFromMaster = 1;
    private final static int tagFromWorker = 2;
    private final static int master = 0;

    // print option
    private boolean printOption; // print out all array contents if true

    /**
     * Initializes matrices.
     * @param size the size of row/column for each matrix
     */
    private void init( int size ) {
        for ( int i = 0; i < size; i++ ) {
            for ( int j = 0; j < size; j++ ) {
                a[i * size + j] = i + j;       // a[i][j] = i + j;
            }
        }
        for ( int i = 0; i < size; i++ ) {
            for ( int j = 0; j < size; j++ ) {
                b[i * size + j] = i - j;       // b[i][j] = i - j;
            }
        }
        for ( int i = 0; i < size; i++ ) {
            for ( int j = 0; j < size; j++ ) {
                c[i * size + j] = 0;           // c[i][j] = 0
            }
        }
    }

    /**
     * Computes a multiplication for my allocated rows.
     * @param size the size of row/column for each matrix
     */
    private void compute( int size ) {
        for ( int k = 0; k < size; k++ ) {
            for ( int i = 0; i < rows[0]; i++ ) {
                for ( int j = 0; j < size; j++ ) {
                    // c[i][k] += a[i][j] * b[j][k]
                    c[i * size + k] += a[i * size + j] * b[j *size + k];
                }
            }
        }
    }

    /**
     * Prints out all elements of a given array.
     * @param array an array of doubles to print out
     */
    private void print( double array[] ) {
        if ( myrank == 0 && printOption) {
            int size = ( int )Math.sqrt( ( double )array.length );
            for ( int i = 0; i < size; i++ ) {
                for ( int j = 0; j < size; j++ ) {
                    // Sytem.out.println( array[i][j] );
                    System.out.println( "[" + i + "]"+ "[" + j + "] = "
                            + array[i * size + j] );
                }
            }
        }
    }

    /**
     * Is the constructor that implements master-worker matrix transfers and
     * matrix multiplication.
     * @param option the size of row/column for each matrix
     * @param size   the option to print out all matrices ( print if true )
     */
    public MatrixMult( int size, boolean option ) throws MPIException {
        myrank = MPI.COMM_WORLD.Rank( );
        nprocs = MPI.COMM_WORLD.Size( );

        a = new double[size * size];  // a = new double[size][size]
        b = new double[size * size];  // b = new double[size][size]
        c = new double[size * size];  // c = new double[size][size]

        printOption = option;

        if ( myrank == 0 ) {
            // I'm a master.

            // Initialize matrices.
            init( size );
            System.out.println( "array a:" );
            print( a );
            System.out.println( "array b:" );
            print( b );

            // Construct message components.
            averows = size / nprocs;
            extra = size % nprocs;
            offset[0] = 0;
            mtype = tagFromMaster;

            // Start timer.
            Date startTime = new Date( );

            // Trasfer matrices to each worker.
            for ( int rank = 0; rank < nprocs; rank++ ) {
                rows[0] = ( rank < extra ) ? averows + 1 : averows;
                System.out.println( "sending " + rows[0] + " rows to rank " + rank );
                if ( rank != 0 ) {
                    MPI.COMM_WORLD.Send( offset, 0, 1, MPI.INT, rank, mtype );
                    MPI.COMM_WORLD.Send( rows, 0, 1, MPI.INT, rank, mtype );
                    MPI.COMM_WORLD.Send( a, offset[0] * size, rows[0] * size,
                            MPI.DOUBLE, rank, mtype );
                    MPI.COMM_WORLD.Send( b, 0, size * size, MPI.DOUBLE, rank,
                            mtype );
                }
                offset[0] += rows[0];
            }

            // Perform matrix multiplication.
            compute( size );

            // Collect results from each worker.
            int mytpe = tagFromWorker;
            for ( int source = 1; source < nprocs; source++ ) {
                MPI.COMM_WORLD.Recv( offset, 0, 1, MPI.INT, source, mtype );
                MPI.COMM_WORLD.Recv( rows, 0, 1, MPI.INT, source, mtype );
                MPI.COMM_WORLD.Recv( c, offset[0] * size, rows[0] * size,
                        MPI.DOUBLE, source, mtype );
            }

            // Stop timer.
            Date endTime = new Date( );

            // Print out results
            System.out.println( "result c:" );
            print( c );

            System.out.println( "time elapsed = " +
                    ( endTime.getTime( ) - startTime.getTime( ) ) +
                    " msec" );
        }
        else {
            // I'm a worker.

            // Receive matrices.
            int mtype = tagFromMaster;
            MPI.COMM_WORLD.Recv( offset, 0, 1, MPI.INT, master, mtype );
            MPI.COMM_WORLD.Recv( rows, 0, 1, MPI.INT, master, mtype );
            MPI.COMM_WORLD.Recv( a, 0, rows[0] * size, MPI.DOUBLE, master,
                    mtype );
            MPI.COMM_WORLD.Recv( b, 0, size * size, MPI.DOUBLE, master,
                    mtype );

            // Perform matrix multiplication.
            compute( size );

            // Send results to the master.
            MPI.COMM_WORLD.Send( offset, 0, 1, MPI.INT, master, mtype );
            MPI.COMM_WORLD.Send( rows, 0, 1, MPI.INT, master, mtype );
            MPI.COMM_WORLD.Send( c, 0, rows[0] * size, MPI.DOUBLE, master,
                    mtype );
        }

        try {
            // Print out a complication message.
            InetAddress inetaddr = InetAddress.getLocalHost( );
            String ipname = inetaddr.getHostName( );
            System.out.println( "rank[" + myrank + "] at " + ipname +
                    ": multiplication completed" );
        } catch ( UnknownHostException e ) {
            System.err.println( e );
        }
    }

    /**
     *
     * @param args Receive the matrix size and the print option in args[0] and
     *             args[1]
     */
    public static void main( String[] args ) throws MPIException {
        // Check # args.

        // Start the MPI library.
        MPI.Init( args );

        // Will initialize size[0] with args[1] and option with args[2] (y | n)
        int size[] = new int[1];
        boolean option[] = new boolean[1];
        option[0] =  false;

        // args[] are only available at rank 0. Don't check args[] at other
        // ranks
        if ( MPI.COMM_WORLD.Rank( ) == 0 ) {
            // arg[] starts from args[1] in mpiJava.
            size[0] = Integer.parseInt( args[1] );
            if ( args.length == 5 ) // args.length increment by 2
                option[0] = true;
        }

        // Broadcast size and option to all workers.
        MPI.COMM_WORLD.Bcast( size, 0, 1, MPI.INT, master );
        MPI.COMM_WORLD.Bcast( option, 0, 1, MPI.BOOLEAN, master );

        // Compute matrix multiplication in both master and workers.
        new MatrixMult( size[0], option[0] );

        // Terminate the MPI library.
        MPI.Finalize( );
    }
}