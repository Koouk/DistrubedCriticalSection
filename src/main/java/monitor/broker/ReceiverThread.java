package monitor.broker;

import com.google.protobuf.InvalidProtocolBufferException;
import monitor.Config;
import monitor.algorithm.Algorithm;
import monitor.algorithm.Request;
import monitor.algorithm.Token;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ReceiverThread  implements  Runnable{

    private final Algorithm algorithm;

    private ZMQ.Context context;

    private ZMQ.Socket publisher;

    public ReceiverThread(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public void run() {

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket subscriber = context.socket(SocketType.SUB);
        subscriber.subscribe("ALL".getBytes(ZMQ.CHARSET));
        subscriber.subscribe(String.valueOf(algorithm.getProcessIndex()).getBytes(ZMQ.CHARSET));

        for (int i = 0 ; i < Config.processes; i++) {
            if(i == algorithm.getProcessIndex()) {
                continue;
            }
            subscriber.connect("tcp://" + Config.address.get(i));
        }

        while(true) {
            String topic = subscriber.recvStr();
            // Read message contents
            var contents = subscriber.recv();

            if(topic.equals("ALL")) {
                try {
                    var message = TokenProto.RequestMessage.parseFrom(contents);
                    algorithm.handleRequestMessage(new Request(message.getProcessId(), message.getNumber(), message.getRequiredId(), message.getFailed()));
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            } else if(topic.equals(String.valueOf(algorithm.getProcessIndex()))) {
                try {
                    var message = TokenProto.TokenMessage.parseFrom(contents);
                    var tokenProto = message.getToken();
                    var queue =  tokenProto.getQueueList().stream().map(p -> new Request(p.getProcessId(), p.getNumber(), p.getRequiredId(), p.getFailed())) .collect(Collectors
                            .toCollection(ArrayList::new));
                    queue = queue.stream().sorted(Comparator.comparing(Request::number)) .collect(Collectors
                            .toCollection(ArrayList::new));
                    var token = new Token(tokenProto.getLnList(), queue);
                    algorithm.handleTokenMessage(token, message.getState(), message.getProducingId());
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }

            }

        }
    }
}
