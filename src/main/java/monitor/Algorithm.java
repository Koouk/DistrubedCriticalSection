package monitor;

import java.util.ArrayList;
import java.util.List;

public class Algorithm {

    private Broker broker;

    private final int uniqueTokens;

    private final List<Object> locks = new ArrayList<>();

    public Algorithm(int uniqueTokens) {
        this.uniqueTokens = uniqueTokens;
    }

    public void init() {
        for(int i = 0; i< uniqueTokens; i++) {
            locks.add(new Object());
        }

        broker = new Broker(this);
        broker.init();
    }


}
