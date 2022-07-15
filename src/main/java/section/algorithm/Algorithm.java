package section.algorithm;

import section.Config;
import section.StateInterface;
import section.broker.Broker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Algorithm {

    public final List<Request> rn;
    private final int uniqueConds;

    private final Lock lock = new ReentrantLock();
    private final List<Condition> conditions = new ArrayList<>();

    private final StateInterface state;


    private final int processIndex;
    private Broker broker;
    private Token token = null;
    private int currentRequestNumber = 0;


    public Algorithm(int uniqueVariables, int processIndex, StateInterface state) {
        this.uniqueConds = uniqueVariables;
        this.state = state;
        this.processIndex = processIndex;
        rn = new ArrayList<>();
        for (int i = 0; i < Config.processes; i++) {
            rn.add(new Request(i, 0, 0, false));
        }
    }

    public void init() {
        for (int i = 0; i < uniqueConds; i++) {
            conditions.add(lock.newCondition());
        }

        broker = new Broker(this);
        broker.init();

        if (processIndex == 0) {
            token = new Token();
        }
    }


    public void sendEnterSectionRequest(int requiredId, boolean isFailed) {
        lock.lock();
        if (token != null) {
            token.setUsed(true);
            lock.unlock();
            return;
        }

        currentRequestNumber += 1;
        var request = new Request(processIndex, currentRequestNumber, requiredId, isFailed);
        rn.set(processIndex, request);
        broker.sendRequestMessage(request);
        lock.unlock();
    }

    public boolean canEnterCriticalSection() {
        if (token != null) {
            token.setUsed(true);
            return true;
        }
        return false;

    }

    public void leaveCriticalSection(Integer producingId) {
        if (token == null) {
            throw new RuntimeException("No token");
        }
        updateToken();
        var receiverId = getReceiver(producingId);
        sendToken(receiverId.orElse(null));
    }

    private Optional<Request> getReceiver(Integer producingId) {
        if (token.getQueue().isEmpty()) {
            return Optional.empty();
        }

        Request foundRequest = token.getQueue().stream()
                .filter(p -> !(p.failed() && p.requiredId() != producingId))
                .findFirst()
                .orElse(token.getQueue().get(0));
        token.getQueue().remove(foundRequest);
        return Optional.of(foundRequest);
    }


    private void updateToken() {
        var processRn = rn.get(processIndex);
        var ln = token.getLn();
        ln[processIndex] = processRn.number();
        var processesInQueue = token.getQueue().stream().map(Request::processId).toList();

        List<Request> newRequests = new ArrayList<>();
        for (int i = 0; i < Config.processes; i++) {
            if (i == processIndex) {
                continue;
            }
            var request = rn.get(i);
            if (ln[i] < request.number() && !processesInQueue.contains(request.processId())) {
                newRequests.add(request); //dodajemy nowy request
            }
        }

        newRequests = newRequests.stream().sorted(Comparator.comparing(Request::number)).toList();  //dodajemy do Q
        token.getQueue().addAll(newRequests);
    }

    private void sendToken(Request receiver) {

        if (receiver != null) {
            if (receiver.processId() == processIndex) {
                token.setUsed(false);
                return;
            }

            int id = receiver.requiredId();
            System.out.println("Sendign token to " + receiver);
            broker.sendToken(receiver.processId(), id);
            token = null;
        } else {
            token.setUsed(false);
        }
    }


    public void handleRequestMessage(Request request) {
        lock.lock();
        System.out.println("Receiving req mess " + request);
        var lastReq = rn.get(request.processId());
        if (lastReq.number() < request.number()) {
            rn.set(lastReq.processId(), request);
        }

        if (token != null && !token.isUsed() && token.getLn()[request.processId()] < request.number()) {
            sendToken(request);
        }
        lock.unlock();
    }

    public void handleTokenMessage(Token token, String state, int requiredId) {
        System.out.println("Receiving token " + requiredId);
        lock.lock();
        this.token = token;
        this.state.updateState(state);
        conditions.get(requiredId).signal();
        lock.unlock();
    }

    public Lock getLock() {
        return lock;
    }

    public Condition getRequiredCondition(int id) {
        return conditions.get(id);
    }

    public StateInterface getState() {
        return state;
    }

    public int getProcessIndex() {
        return processIndex;
    }

    public Token getToken() {
        return token;
    }

}
