package com.zuozewei.springboottextdatadrivendemo.paramter;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Iterator;

/**
 * 描述:
 * 延迟数据提供
 *
 * @author zuozewei
 * @create 2019-11-23 19:56
 */

@Slf4j
public class TxtIterator implements Iterator<Object[]> {

    /**
     *  数据文件
     */
    File txtFile;
    BufferedReader bs;
    String currentLine;

    public TxtIterator(File txtFile) throws IOException {
        super();
        this.txtFile = txtFile;
        try {
            bs = new BufferedReader(new FileReader(txtFile));
        } catch (FileNotFoundException e) {
            log.error("文件找不到");
            e.printStackTrace();
        }
        currentLine = bs.readLine();
    }

    @Override
    public boolean hasNext() {
        if (currentLine != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String[] next() {
        String returnLine = currentLine;
        try {
            currentLine = bs.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnLine.split(",");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
}
