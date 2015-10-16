package Service;

import java.net.Socket;

/**
 * Created by wangyizhong on 2015/3/18.
 */


public class RequestWrapper {
    public final Socket client;
    public final String body;

    public RequestWrapper(Socket client, String body) {
        this.client = client;
        this.body = body;
    }

}
