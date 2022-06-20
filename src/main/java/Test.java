import com.google.protobuf.InvalidProtocolBufferException;
import monitor.DisturbedMonitor;

public class Test {

    public static void main(String[] args) throws InvalidProtocolBufferException {

        DisturbedMonitor monitor = new DisturbedMonitor();
        TestState a = new TestState();
        monitor.testProtoSerial();
/*
        var ref = new Object() {
            int w = 0;
        };

        monitor.get(() -> {
            System.out.println(a.getA());
            a.incrementA();
            System.out.println(a.getA());
            ref.w = a.getA();
        });

        System.out.println("xd " + ref.w);

        var b = a.serialize();
        a.deserialize(b);*/
    }

    void serialize(Object o) {

    }

}
