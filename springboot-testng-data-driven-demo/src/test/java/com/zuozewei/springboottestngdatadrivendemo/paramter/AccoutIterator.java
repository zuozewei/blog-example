package com.zuozewei.springboottestngdatadrivendemo.paramter;

import java.util.Iterator;

/**
 * 描述:
 * 延迟数据提供
 *
 * @author zuozewei
 * @create 2019-11-23 19:56
 */

public class AccoutIterator implements Iterator {

    private int index =0;
    static private final int MAX  =4;

    @Override
    public boolean hasNext() {
        return index < MAX;
    }

    @Override
    public Object next() {
        return new Object[]{
                //这里就是放入要实现的对象或者一组数据
                "延迟数据提供："+ (index++)
        };
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
}
