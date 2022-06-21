import com.google.gson.Gson;
import monitor.StateInterface;

import java.util.ArrayList;
import java.util.List;

public class TestState implements StateInterface {
    private List<String> testList = new ArrayList<>();

    private int a = 0;

    private transient Gson gson = new Gson();

    public void incrementA() {
        a++;
    }

    public int getA() {
        return a;
    }


    @Override
    public String serialize() {

        String json = gson.toJson(this);
        System.out.println(json);
        return json;
    }

    @Override
    public void deserialize(String body) {
        TestState state = gson.fromJson(body, TestState.class);
        System.out.println(state);

    }

    @Override
    public void updateState(StateInterface state) {
        TestState xd = (TestState) state;

    }

    @Override
    public String toString() {
        return "TestState{" +
                "testList=" + testList +
                ", a=" + a +
                '}';
    }
}
