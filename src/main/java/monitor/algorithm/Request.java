package monitor.algorithm;

public record Request(int processId, int number, int requiredId, boolean failed) {
}
