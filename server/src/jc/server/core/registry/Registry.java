package jc.server.core.registry;

/**
 * Created by ��� on 2015/9/23.
 */
public interface Registry<T> {

    public int register(String key, T value);

    public T get(String key);

    public int delete(String key);

}
