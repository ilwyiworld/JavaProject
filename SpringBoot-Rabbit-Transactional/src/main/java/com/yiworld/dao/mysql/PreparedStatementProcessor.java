package com.yiworld.dao.mysql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementProcessor {
    void process(PreparedStatement ps) throws SQLException;
}
