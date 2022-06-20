import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class TestSub2 {


    public static void main(String[] args) throws InterruptedException {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket subscriber = context.socket(SocketType.SUB);
        subscriber.subscribe("A".getBytes(ZMQ.CHARSET));
        subscriber.subscribe("C".getBytes(ZMQ.CHARSET));
        subscriber.connect("tcp://localhost:8001");

        while (!Thread.currentThread().isInterrupted()) {
            // Read envelope with address
            String address = subscriber.recvStr();
            // Read message contents
            String contents = subscriber.recvStr();
            System.out.println(address + " : " + contents);
            // Read envelope with address
             address = subscriber.recvStr();
            // Read message contents
             contents = subscriber.recvStr();
            System.out.println(address + " : " + contents);
            break;
        }
    }
}
