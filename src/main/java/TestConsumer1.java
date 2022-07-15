import com.google.protobuf.InvalidProtocolBufferException;
import section.DisturbedSection;

import static java.lang.Thread.sleep;

public class TestConsumer1 {

    public static void main(String[] args) throws InterruptedException {
        sleep(3000);
        var state = new TestState();
        DisturbedSection monitor = new DisturbedSection(2, state, 1);
        monitor.run();
        for (int i = 0; i < 10; i++) {
            monitor.execute(1, 0,
                    state::checkIfEmpty,
                    () -> {
                        var x = state.getA();
                        System.out.println("\n Got" + x + "\n");
                    });
        }
        System.out.println("END");
    }


}


