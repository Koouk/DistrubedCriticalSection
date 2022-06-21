package monitor;

public interface StateInterface {

    String serialize();

    void deserialize(String body);

    void updateState(StateInterface state);
}
