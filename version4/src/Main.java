import sequential.SequentialHeatDiffusion;
import parallel.ParallelHeatDiffusion;
import distributed.Server;
import distributed.Client;

public class Main {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java Main <mode> <width> <height> <points>");
            return;
        }

        String mode = args[0];
        int width = Integer.parseInt(args[1]);
        int height = Integer.parseInt(args[2]);
        int points = Integer.parseInt(args[3]);

        switch (mode.toLowerCase()) {
            case "sequential":
                SequentialHeatDiffusion.run(width, height, points);
                break;
            case "parallel":
                ParallelHeatDiffusion.run(width, height, points);
                break;
            case "distributed":
                runDistributed(width, height, points);
                break;
            default:
                System.out.println("Unknown mode: " + mode);
        }
    }

    private static void runDistributed(int width, int height, int points) {
        try {
            Server server = new Server(width, height, points);
            java.rmi.Naming.rebind("//localhost/Master", server);
            System.out.println("Master ready.");

            // Start the client
            Client client = new Client();
            java.rmi.Naming.rebind("//localhost/Worker", client);
            System.out.println("Worker ready.");

            // Start the simulation
            server.distributeWork();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
