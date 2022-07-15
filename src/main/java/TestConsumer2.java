import com.google.protobuf.InvalidProtocolBufferException;
import section.DisturbedSection;

public class TestConsumer2 {

    public static void main(String[] args) throws InvalidProtocolBufferException {

        var state = new TestState();
        DisturbedSection monitor = new DisturbedSection(2, state, 2);
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


