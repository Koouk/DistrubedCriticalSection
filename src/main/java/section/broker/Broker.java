package section.broker;

import section.Config;
import section.algorithm.Algorithm;
import section.algorithm.Request;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.util.Arrays;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

public class Broker {

    private final Algorithm algorithm;

    private ZMQ.Context context;

    private ZMQ.Socket publisher;

    public Broker(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public void init() {

        var executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new ReceiverThread(algorithm));
        executorService.shutdown();

        context = ZMQ.context(1);
        publisher = context.socket(SocketType.PUB);
        publisher.bind("tcp://%s".formatted(Config.address.get(algorithm.getProcessIndex())));
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendRequestMessage(Request request) {
        //  System.out.println("Sending " + request);
        TokenProto.RequestMessage message = TokenProto.RequestMessage.newBuilder()
                .setNumber(request.number())
                .setFailed(request.failed())
                .setProcessId(request.processId())
                .setRequiredId(request.requiredId())
                .build();

        System.out.println("Sending req mess with number " + request.number());
        publisher.sendMore("ALL");
        publisher.send(message.toByteArray());
    }

    public void sendToken(int processId, Integer producingId) {
        var queueProto = algorithm.getToken().getQueue().stream().map(p ->
                TokenProto.RequestMessage.newBuilder()
                        .setFailed(p.failed())
                        .setNumber(p.number())
                        .setProcessId(p.processId())
                        .setRequiredId(p.requiredId())
                        .build()
        ).toList();

        var tokenProto = TokenProto.Token.newBuilder()
                .addAllLn(Arrays.asList(algorithm.getToken().getLn()))
                .addAllQueue(queueProto)
                .build();

        var tokenMsg = TokenProto.TokenMessage.newBuilder()
                .setToken(tokenProto)
                .setState(algorithm.getState().serialize())
                .setProducingId(producingId)
                .build();


        publisher.sendMore(String.valueOf(processId));
        publisher.send(tokenMsg.toByteArray());
    }


}
