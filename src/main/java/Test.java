import com.google.protobuf.InvalidProtocolBufferException;
import monitor.DisturbedMonitor;

import static java.lang.Thread.sleep;

public class Test {

    public static void main(String[] args) throws InvalidProtocolBufferException, InterruptedException {

        var state = new TestState();
        DisturbedMonitor monitor = new DisturbedMonitor(2, state, 0);
        monitor.run();
        for (int i = 0; i < 20; i++) {
            int finalI = i;
            monitor.execute(0, 1,
                    () -> !state.checkIfFull(),
                    () -> {
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        state.put(String.valueOf(finalI));

                        System.out.println("\n Producing " + state + "\n");
                    });
        }

    }

}
