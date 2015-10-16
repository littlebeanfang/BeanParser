package Service;

import Service.async.LoggableThread;
import Service.async.SharedBuffer;

/**
 * Created by wangyizhong on 2015/3/18.
 */
public abstract class ServiceThread extends LoggableThread {

    protected SharedBuffer<RequestWrapper> buffer;

    public ServiceThread() {
        buffer = new SharedBuffer<RequestWrapper>();
    }

    public SharedBuffer<RequestWrapper> getBuffer() {
        return buffer;
    }
}
