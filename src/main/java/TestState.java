import com.google.gson.Gson;
import section.StateInterface;

import java.util.LinkedList;
import java.util.Queue;

public class TestState implements StateInterface {
    private Queue<String> testQueue = new LinkedList<>();

    private int capacity = 3;


    private transient Gson gson = new Gson();

    public void put(String a) {
        testQueue.add(a);
    }

    public String getA() {
        return testQueue.poll();
    }

    public boolean checkIfFull() {
        return testQueue.size() == capacity;
    }

    public boolean checkIfEmpty() {
        return testQueue.size() > 0;
    }

    @Override
    public String serialize() {

        String json = gson.toJson(this);
        return json;
    }

    @Override
    public void deserialize(String body) {
        TestState state = gson.fromJson(body, TestState.class);
    }

    @Override
    public void updateState(String state) {
        TestState newState = gson.fromJson(state, TestState.class);
        this.testQueue = newState.testQueue;
        this.capacity = newState.capacity;

    }

    @Override
    public String toString() {
        return "TestState{" +
                "testQueue=" + testQueue +
                ", capacity=" + capacity +
                ", gson=" + gson +
                '}';
    }
}
