import com.google.protobuf.InvalidProtocolBufferException;
import monitor.DisturbedMonitor;

public class Test3 {

    public static void main(String[] args) throws InvalidProtocolBufferException {

        var state = new TestState();
        DisturbedMonitor monitor = new DisturbedMonitor(2, state, 2);
        monitor.run();
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            monitor.execute(1, 0,
                    () -> state.checkIfEmpty(),
                    () -> {
                        var x = state.getA();
                        System.out.println("\n Got" + x + "\n");
                    });
        }
        System.out.println("END");
    }
}


