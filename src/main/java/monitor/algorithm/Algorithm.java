package monitor.algorithm;

import monitor.Config;
import monitor.StateInterface;
import monitor.broker.Broker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Algorithm {

    private Broker broker;

    private final int uniqueConds;

    private final Lock lock = new ReentrantLock();
    private final List<Condition> conditions = new ArrayList<>();

    private final StateInterface state;


    private final int processIndex;
    private Token token = null;
    private final Object tokenLock = new Object();

    public final List<List<Request>>  rn;
    private int currentRequestNumber = 0;



    public Algorithm(int uniqueVariables, int processIndex, StateInterface state) {
        this.uniqueConds = uniqueVariables;
        this.state = state;
        this.processIndex = processIndex;
        rn = new ArrayList<>();
        for(int i = 0; i< Config.processes; i++) {
            rn.add(new ArrayList<Request>());
            rn.get(i).add(new Request(i,0,0,false));  // aka 0 w rn
        }
    }

    public void init() {
        for(int i = 0; i < uniqueConds; i++) {
            conditions.add(lock.newCondition());
        }

        broker = new Broker(this);
        broker.init();

        if(processIndex == 0 ) {
            token = new Token();
        }
    }


    public void sendEnterSectionRequest(int requiredId, boolean isFailed) {
        lock.lock();
        if(token != null) {
            token.setUsed(true);
            lock.unlock();
            return;
        }

        Request req = null;
        currentRequestNumber += 1;
        var rnI = rn.get(processIndex);
        System.out.println("Sending enter request with number " + currentRequestNumber);
        rnI.add(new Request(processIndex, currentRequestNumber, requiredId, isFailed));
        req = rnI.get(rnI.size() - 1);

        broker.sendRequestMessage(req);
        lock.unlock();
    }

    public boolean canEnterCriticalSection() {
        if(token != null) {
            token.setUsed(true);
            return true;
        }
        return false;

    }

    public void leaveCriticalSection(Integer producingId) {
        var rnI = rn.get(processIndex);
        var req = rnI.get(rnI.size() - 1);
        if(token == null ) {
            return;
        }
        updateToken(req);
        sendToken(producingId);
    }


    private void updateToken(Request req) {
        token.getLn()[processIndex] = req.number();
        var ln = token.getLn();
        List<Request> newRequests = new ArrayList<>();
        for(int i = 0; i < Config.processes; i++ ) {
            if(i == processIndex) {
                continue;
            }

            var rnK = rn.get(i);
            var lnI = ln[i];
            if(ln[i] < rnK.get(rnK.size() -1 ).number()) {
                newRequests.addAll(rnK.stream().filter(r -> r.number() >= lnI).toList()); //dodajemy wszystkie nowe requesty
            }
            ln[i] = rnK.get(rnK.size() - 1).number(); //aktualizujemy ln[i]
        }

        newRequests = newRequests.stream().sorted(Comparator.comparing(Request::number)).toList();  //dodajemy do Q
        token.getQueue().addAll(newRequests);
    }

    private void sendToken(Integer producingId) {
        var foundRequest = token.getQueue().stream().filter(p -> !(p.failed() && p.requiredId() != producingId)).findFirst(); // albo przyjmie wsyztsko albo potrzebuje prodID
        if(foundRequest.isPresent()) {

            var newReq = foundRequest.get();
            if(newReq.processId() == processIndex) {
                token.getQueue().remove(newReq);
                token.setUsed(false);
                return;
            }
            System.out.println("NUMBER " + newReq.number());
            token.getQueue().remove(newReq);
            int id = newReq.failed() ? producingId : newReq.requiredId();
            System.out.println("Sending token to " + newReq);
            broker.sendToken(newReq.processId(), id);
            token = null;
        } else {
            token.setUsed(false);
        }
    }
    private void updateTokenNotUsed() {
        var ln = token.getLn();
        List<Request> newRequests = new ArrayList<>();
        for(int i = 0; i < Config.processes; i++ ) {
            if(i == processIndex) {
                continue;
            }

            var rnK = rn.get(i);
            var lnI = ln[i];
            if(ln[i] < rnK.get(rnK.size() -1 ).number()) {
                newRequests.addAll(rnK.stream().filter(r -> r.number() >= lnI).toList()); //dodajemy wszystkie nowe requesty
            }
            ln[i] = rnK.get(rnK.size() - 1).number(); //aktualizujemy ln[i]
        }

        newRequests = newRequests.stream().sorted(Comparator.comparing(Request::number)).toList();  //dodajemy do Q
        token.getQueue().addAll(newRequests);
    }

    private void sendNotUsedToken(Request request) {
        updateTokenNotUsed();
        var foundRequest = token.getQueue().stream().findFirst();
        if(foundRequest.isPresent()) {
            if(foundRequest.get().number() < request.number()) {
                request = foundRequest.get();
            }
        }

        if(request.processId() == processIndex) {
            token.getQueue().remove(request);
            token.setUsed(false);
            return;
        }

        token.getQueue().remove(request);

        System.out.println("Sending token to " + request);

        broker.sendToken(request.processId(), request.requiredId());
        token = null;
    }

    public void handleRequestMessage(Request request) {
        lock.lock();
        System.out.println("received request mess " + request.number());
            var reqList = rn.get(request.processId());

            if( Collections.max(reqList.stream().map(Request::number).toList()) < request.number()) {
                reqList.add(request);
            } else {
                lock.unlock();
                return;
            }


            if(token != null && !token.isUsed()) {
                sendNotUsedToken(request);
            }
        lock.unlock();
    }

    public void handleTokenMessage(Token token, String state, int requiredId) {

        lock.lock();
        System.out.println("received token message ");
        this.token = token;
        this.state.updateState(state);
        System.out.println("SIGNALING " + requiredId);
        conditions.get(requiredId).signal();
        lock.unlock();
    }

    public Lock getLock() {
        return lock;
    }

    public Condition getRequiredCondition(int id) {
        return conditions.get(id);
    }

    public Broker getBroker() {
        return broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public int getUniqueConds() {
        return uniqueConds;
    }

    public List<Condition> getConditions() {
        return conditions;
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

    public void setToken(Token token) {
        this.token = token;
    }

    public Object getTokenLock() {
        return tokenLock;
    }

    public List<List<Request>> getRn() {
        return rn;
    }

    public int getCurrentRequestNumber() {
        return currentRequestNumber;
    }

    public void setCurrentRequestNumber(int currentRequestNumber) {
        this.currentRequestNumber = currentRequestNumber;
    }
}
