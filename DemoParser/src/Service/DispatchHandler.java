package Service;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangyizhong on 2015/3/18.
 */

public class DispatchHandler {
    private Map<String, ServiceThread> threads;

    public DispatchHandler() {
        threads = new HashMap<String, ServiceThread>();
        addService();
        for (ServiceThread t : threads.values()) {
            t.start();
        }
    }

    public void handle(Socket client, String service, String body) {
        ServiceThread thread = threads.get(service);
        if (thread != null) {
            thread.getBuffer().push(new RequestWrapper(client, body));
            notice(thread);
        } else { // no handler thread, simple close the client socket
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addService() {
        threads.put(ParseEnglishThread.SERVICE_NAME, new ParseEnglishThread());
    }

    private void notice(Object o) {
        synchronized (o) {
            o.notify();
        }
    }
}
