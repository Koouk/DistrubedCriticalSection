import com.google.protobuf.InvalidProtocolBufferException;
import monitor.DisturbedMonitor;

public class Test {

    public static void main(String[] args) throws InvalidProtocolBufferException {

        var state = new TestState();
        DisturbedMonitor monitor = new DisturbedMonitor(1, state, 1);
        monitor.run();
        TestState a = new TestState();
        monitor.execute(0,0,() -> {return true;}, () -> {a.incrementA();});
        System.out.println("lol");
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


/*
    public byte[] testProtoSerial() throws InvalidProtocolBufferException {
        QueueMessageProto.QueueMessage message = QueueMessageProto.QueueMessage.newBuilder()
                .setId(1)
                .setState("xddd")
                .build();

        var test = message.toByteArray();
        return test;
    }


    public void testProtoDeserial(byte[] bytes) throws InvalidProtocolBufferException {
        QueueMessageProto.QueueMessage deserialized
                = QueueMessageProto.QueueMessage.newBuilder()
                .mergeFrom(bytes).build();

        System.out.println(deserialized.getId());
        System.out.println(deserialized.getState());
    }


 */
