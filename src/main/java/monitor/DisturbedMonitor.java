package monitor;

import com.google.protobuf.InvalidProtocolBufferException;

public class DisturbedMonitor {

    private final Algorithm algorithm;

    public DisturbedMonitor(int tokens) {
        algorithm = new Algorithm(tokens);
    }

    public void run(){
        algorithm.init();
    }

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



    public void get(CallbackInterface fun) {
        fun.execute();
    }


}
