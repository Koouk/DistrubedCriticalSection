package monitor.broker;

import monitor.algorithm.Algorithm;

public class ReceiverThread  implements  Runnable{

    private final Algorithm algorithm;

    public ReceiverThread(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public void run() {
        System.out.println("Jej, odbieram");
        // tutaj bedzie obsluga przychodzacych
    }
}
