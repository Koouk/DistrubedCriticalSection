import com.google.protobuf.InvalidProtocolBufferException;
import monitor.algorithm.Request;
import monitor.algorithm.Token;
import monitor.broker.TokenProto;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import static java.lang.Thread.sleep;

public class TestSub {

    public static void main(String[] args) throws InterruptedException, InvalidProtocolBufferException {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket subscriber = context.socket(SocketType.SUB);
        subscriber.subscribe("ALL".getBytes(ZMQ.CHARSET));
        subscriber.subscribe("B".getBytes(ZMQ.CHARSET));
        subscriber.connect("tcp://localhost:9001");

        while (!Thread.currentThread().isInterrupted()) {

            String topic = subscriber.recvStr();
            // Read message contents

            var contents = subscriber.recv();

            if(topic.equals("ALL")) {
                var message = TokenProto.RequestMessage.parseFrom(contents);
                var xd = (new Request(message.getProcessId(), message.getNumber(), message.getRequiredId(), message.getFailed()));
                System.out.println(xd);
                System.out.println(topic);
            } else {
                var message = TokenProto.TokenMessage.parseFrom(contents);
                var tokenProto = message.getToken();
                var queue =  tokenProto.getQueueList().stream().map(p -> new Request(p.getProcessId(), p.getNumber(), p.getRequiredId(), p.getFailed())).toList();
                var token = new Token(tokenProto.getLnList(), queue);

                System.out.println(token);
                System.out.println(message.getState());
                System.out.println(message.getProducingId());
                System.out.println(topic);
            }

            break;


        }
    }

}


