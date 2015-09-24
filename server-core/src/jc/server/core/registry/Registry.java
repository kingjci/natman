package jc.server.core.registry;

/**
 * Created by ½ğ³É on 2015/9/23.
 */
public interface Registry<T> {

    public int register(String key, T value);

    public T get(String key);

    public int delete(String key);

}
