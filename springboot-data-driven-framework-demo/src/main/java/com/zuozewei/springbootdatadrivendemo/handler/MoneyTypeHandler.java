package com.zuozewei.springbootdatadrivendemo.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 在 Money 与 Long 之间转换的 TypeHandler，处理 CNY 人民币
 */

public class MoneyTypeHandler extends BaseTypeHandler<Money> {

    /**
     *  设置非空参数
     * @param ps
     * @param i
     * @param parameter
     * @param jdbcType
     * @throws SQLException
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Money parameter, JdbcType jdbcType) throws SQLException {
        ps.setLong(i, parameter.getAmountMinorLong());
    }

    /**
     * 根据列名，获取可以为空的结果
     * @param rs
     * @param columnName
     * @return
     * @throws SQLException
     */
    @Override
    public Money getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseMoney(rs.getLong(columnName));
    }

    /**
     * 根据列索引，获取可以为空的结果
     * @param rs
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    @Override
    public Money getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseMoney(rs.getLong(columnIndex));
    }

    /**
     *
     * @param cs
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    @Override
    public Money getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseMoney(cs.getLong(columnIndex));
    }

    /**
     *  处理 CNY 人民币
     * @param value
     * @return
     */
    private Money parseMoney(Long value) {
        return Money.of(CurrencyUnit.of("CNY"), value / 100.0);
    }
}
