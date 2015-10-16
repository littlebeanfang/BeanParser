package Service.async;

import java.util.Date;

/**
 * a thread with a log function
 *
 * @author Jack Sun(jacksunwei@gmail.com)
 */
public abstract class LoggableThread extends Thread {
    private StringBuilder sb = new StringBuilder(100);

    protected void log(String msg) {
        sb.setLength(0);
        sb.append("[").append(new Date(System.currentTimeMillis()).toString())
                .append(" tid: ").append(getId()).append("] ").append(msg);
        System.out.println(sb.toString());
    }

    public abstract void run();
}
