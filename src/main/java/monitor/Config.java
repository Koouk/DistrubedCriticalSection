package monitor;

import java.util.List;

public interface Config {

    List<String> address = List.of("localhost:9000", "localhost:9001", "localhost:9002");

    int processes = 3;
}
