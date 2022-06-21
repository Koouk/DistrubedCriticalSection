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

    private final int uniqueTokens;

    private final Lock lock = new ReentrantLock();
    private final List<Condition> conditions = new ArrayList<>();

    private final StateInterface state;


    private final int processIndex;
    private Token token = null;
    private final Object tokenLock = new Object();

    public final List<List<Request>>  rn;
    private int currentRequestNumber = 0;



    public Algorithm(int uniqueVariables, int processIndex, StateInterface state) {
        this.uniqueTokens = uniqueVariables;
        this.state = state;
        this.processIndex = processIndex;
        rn = new ArrayList<>();
        for(int i = 0; i< Config.processes; i++) {
            rn.add(new ArrayList<Request>());
            rn.get(i).add(new Request(i,0,0,false));  // aka 0 w rn
        }
    }

    public void init() {
        for(int i = 0; i < uniqueTokens; i++) {
            conditions.add(lock.newCondition());
        }

        broker = new Broker(this);
        broker.init();

        if(processIndex == 0 ) {
            token = new Token();
        }
    }


    public void sendEnterSectionRequest(int requiredId, boolean isFailed) {
        synchronized (tokenLock) {
            if(token != null) {
                token.setUsed(true);
                return;
            }
        }
        Request req = null;
        synchronized (rn) {
            currentRequestNumber += 1;
            var rnI = rn.get(processIndex);
            rnI.add(new Request(processIndex, currentRequestNumber, requiredId, isFailed));
            req = rnI.get(rnI.size() - 1);
        }
        broker.sendRequestMessage(req, processIndex);
    }

    public boolean canEnterCriticalSection() {
        synchronized (tokenLock) {
            if(token != null) {
                token.setUsed(true);
                return true;
            }
            return false;
        }
    }

    public void leaveCriticalSection(Integer producingId) {
        synchronized (rn) {
            var rnI = rn.get(processIndex);
            var req = rnI.get(rnI.size() - 1);

            synchronized (tokenLock) {
                updateToken(req);
                sendToken(producingId);
            }
        }
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
                throw new RuntimeException("Same process after critical section ;( "); //todo ?
            }
            token.getQueue().remove(newReq);
            broker.sendToken(newReq, producingId);
            token = null;
        } else {
            token.setUsed(false);
        }
    }

    public void handleRequestMessage(Request request) {
        synchronized (rn) {
            var reqList = rn.get(request.processId());

            if( Collections.max(reqList.stream().map(Request::number).toList()) < request.number()) {
                reqList.add(request);
            } else {
                return;
            }
        }
        synchronized (tokenLock) {
            if(!token.isUsed()) {
                updateToken(request);
                sendToken(request.requiredId());
            }
        }
    }

    public void handleTokenMessage(Token token, StateInterface state, int requiredId) {
        // token
        // receive and save token (tocken lock), robimy update stanu,  budzimy z request requiredId, wszytsko to  w locku
    }

    public Lock getLock() {
        return lock;
    }

    public Condition getRequiredCondition(int id) {
        return conditions.get(id);
    }


}
