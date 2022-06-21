package monitor.algorithm;

import monitor.broker.Broker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Algorithm {

    private Broker broker;

    private final int uniqueTokens;

    private final Lock lock = new ReentrantLock();
    private final List<Condition> conditions = new ArrayList<>();

    public Algorithm(int uniqueTokens) {
        this.uniqueTokens = uniqueTokens;
    }

    public void init() {
        for(int i = 0; i< uniqueTokens; i++) {
            conditions.add(lock.newCondition());
        }

        broker = new Broker(this);
        broker.init();
    }


    public Lock getLock() {
        return lock;
    }

    public Condition getRequiredCondition(int id) {
        return conditions.get(id);
    }

    public void sendEnterSectionRequest(int requiredId, boolean b) {
    }

    public boolean canEnterCriticalSection() {
        return false;
    }

    public void leaveCriticalSection(Integer producingId) {

    }


}
