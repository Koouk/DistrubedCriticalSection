package monitor.broker;

import monitor.algorithm.Algorithm;
import monitor.algorithm.Request;

import java.util.Collections;
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
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Jej, wysylam");
    }

    public void sendRequestMessage(Request request, int processIndex) {
        // wysylamy request do wszytskich, requst zamienic na protobuf

    }

    public void sendToken(Request newReq, Integer producingId) {
        //send token + state as json
    }




}
