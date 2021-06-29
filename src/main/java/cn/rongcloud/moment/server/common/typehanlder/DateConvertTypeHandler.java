package cn.rongcloud.moment.server.common.typehanlder;

import cn.rongcloud.moment.server.common.utils.ApplicationUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.core.env.Environment;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Clinton Begin
 */
public class DateConvertTypeHandler extends BaseTypeHandler<Date> {

    String dbName = ApplicationUtil.getBean(Environment.class).getProperty("db.name");

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setTimestamp(i, new Timestamp((parameter).getTime()));
    }

    @Override
    public Date getNullableResult(ResultSet rs, String columnName)
            throws SQLException {

        if (dbName.equals("gbase")) {
            String sqlTimestamp = rs.getString(columnName);
            if (sqlTimestamp != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
                Date date = null;
                try {
                    date = sdf.parse(sqlTimestamp);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return date;
            }
        } else {
            Timestamp sqlTimestamp = rs.getTimestamp(columnName);
            if (sqlTimestamp != null) {
                return new Date(sqlTimestamp.getTime());
            }
        }

        return null;
    }

    @Override
    public Date getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        Timestamp sqlTimestamp = rs.getTimestamp(columnIndex);
        if (sqlTimestamp != null) {
            return new Date(sqlTimestamp.getTime());
        }
        return null;
    }

    @Override
    public Date getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        Timestamp sqlTimestamp = cs.getTimestamp(columnIndex);
        if (sqlTimestamp != null) {
            return new Date(sqlTimestamp.getTime());
        }
        return null;
    }
}
