package monitor;

import monitor.algorithm.Algorithm;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class DisturbedMonitor {

    private final Algorithm algorithm;

    public DisturbedMonitor(int tokens) {
        algorithm = new Algorithm(tokens);
    }

    public void run() {
        algorithm.init();
    }


    public void execute(int requiredId, int producingId, PredicateInterface additionalCondition,CallbackInterface functionToExecute) {
        Lock lock = algorithm.getLock();
        Condition condition = algorithm.getRequiredCondition(requiredId);

        boolean executed = false;
        boolean firstTry = true;
        while(!executed) {
            lock.lock();
            if(firstTry) {
                algorithm.sendEnterSectionRequest(requiredId, false); // wysylamy ze nie czekamy na konkretny sygnal
            } else {
                algorithm.sendEnterSectionRequest(requiredId, true); // czekamy na konkretny sygnal
            }
            executed = tryToExecute(additionalCondition, functionToExecute, condition);
            algorithm.leaveCriticalSection(producingId); // wysylamy konkretny sygnal, ktos moze na niego czekac
            lock.unlock();
        }
    }

    private boolean tryToExecute(PredicateInterface additionalCondition, CallbackInterface functionToExecute, Condition condition) {
        boolean executed = false;
        try {
            while(algorithm.canEnterCriticalSection()) {
                condition.await();
            }
            if(additionalCondition.check()) {
                functionToExecute.execute();
                executed = true;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return executed;
    }


}
