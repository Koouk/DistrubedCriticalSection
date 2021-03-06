package section.algorithm;

import section.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Token {

    private final Integer[] ln;

    private final List<Request> queue;

    private boolean isUsed = false;

    public Token() {
        ln = new Integer[Config.processes];
        Arrays.fill(ln, 0);
        queue = new ArrayList<>();
    }

    public Token(List<Integer> ln, List<Request> queue) {
        this.ln = ln.toArray(new Integer[0]);
        this.queue = queue;
    }

    public Integer[] getLn() {
        return ln;
    }

    public List<Request> getQueue() {
        return queue;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    @Override
    public String toString() {
        return "Token{" +
                "ln=" + Arrays.toString(ln) +
                ", queue=" + queue +
                ", isUsed=" + isUsed +
                '}';
    }
}
