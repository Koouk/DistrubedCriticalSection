package section;

public interface StateInterface {

    String serialize();

    void deserialize(String body);

    void updateState(String body);
}
