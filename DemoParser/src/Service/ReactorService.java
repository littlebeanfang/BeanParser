package Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by wangyizhong on 2015/3/18.
 */
public class ReactorService {
    public static void main(String[] args) throws IOException {
        ServerSocket socket = null;
        try {
            DispatchHandler handler = new DispatchHandler();
            String host = "127.0.0.1";
            int port = 5678;    //TODO: Any problem?
            InetSocketAddress address = new InetSocketAddress(host, port);
            socket = new ServerSocket();
            socket.bind(address);

            while (true) {
                Socket client = socket.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        client.getInputStream()));
                String service = br.readLine();
                String body = br.readLine();
                GgrLogger.log("accept from: " + client.getInetAddress()
                        + ", service: " + service);

                handler.handle(client, service, body);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // socket.close();
        }
    }
}
