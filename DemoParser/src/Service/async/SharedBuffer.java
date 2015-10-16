package Service.async;

import java.util.LinkedList;

public class SharedBuffer<T> {

    private LinkedList<T> list;

    public SharedBuffer() {
        list = new LinkedList<T>();
    }

    public synchronized void push(T pumper) {
        list.add(pumper);
    }

    public synchronized int length() {
        return list.size();
    }

    public synchronized T pop() {
        return list.size() == 0 ? null : list.removeFirst();
    }
}
