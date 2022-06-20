import com.google.protobuf.InvalidProtocolBufferException;
import monitor.DisturbedMonitor;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import static java.lang.Thread.sleep;

public class TestPub {
    public static void main(String[] args) throws InterruptedException {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket publisher = context.socket(SocketType.PUB);
        publisher.bind("tcp://localhost:8001");

        while (!Thread.currentThread().isInterrupted()) {
            // Write two messages, each with an envelope and content
            publisher.sendMore("A");
            publisher.send("We don't want to see this");
            publisher.sendMore("B");
            publisher.send("We would like to see this");
        }
    }
}
