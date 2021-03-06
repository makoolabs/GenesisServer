package com.genesis.dblog.logs;

import com.genesis.dblog.HumanDbLog;
import com.genesis.dblog.anno.ColumnView;
import com.genesis.dblog.anno.TableView;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@TableView("测试日志")
public class TestLog extends HumanDbLog {

    private static final long serialVersionUID = 1L;

    @ColumnView(value = "测试字段")
    @Column
    private String testField;

    public String getTestField() {
        return testField;
    }

    public void setTestField(String testField) {
        this.testField = testField;
    }

}
