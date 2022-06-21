package monitor.broker;

import monitor.algorithm.Algorithm;

import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

public class Broker {

    private final Algorithm algorithm;

    public Broker(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public void init() {

        var executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new ReceiverThread(algorithm));
        executorService.shutdown();

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Jej, wysylam");
    }
}
