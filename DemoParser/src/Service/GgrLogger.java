package Service;

import java.io.PrintStream;
import java.util.Date;

/**
 * Created by wangyizhong on 2015/3/18.
 */


public class GgrLogger {

    public static void log(String msg) {
        _log(System.out, msg);
    }

    public static void warn(String msg) {
        _log(System.err, msg);
    }

    private static void _log(PrintStream ps, String msg) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("[").append(new Date(System.currentTimeMillis()).toString())
                .append("] ").append(msg);
        ps.println(sb.toString());
    }
}
