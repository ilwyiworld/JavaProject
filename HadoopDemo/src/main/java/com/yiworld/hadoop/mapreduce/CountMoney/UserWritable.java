package com.yiworld.hadoop.mapreduce.CountMoney;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by yiworld on 2017/7/19.
 */
public class UserWritable implements WritableComparable<UserWritable> {

    private Integer id;
    private Integer income;
    private Integer expense;
    private Integer sum;

    public Integer getId() {
        return id;
    }

    public UserWritable setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getIncome() {
        return income;
    }

    public UserWritable setIncome(Integer income) {
        this.income = income;
        return this;
    }

    public Integer getExpense() {
        return expense;
    }

    public UserWritable setExpense(Integer expense) {
        this.expense = expense;
        return this;
    }

    public Integer getSum() {
        return sum;
    }

    public void setSum(Integer sum) {
        this.sum = sum;
    }

    @Override
    public String toString() {
        return id + "\t" + income + "\t" + expense + "\t" + sum;
    }

    @Override
    public int compareTo(UserWritable o) {
        return this.id > o.getId() ? 1 : -1;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(id);
        out.writeInt(income);
        out.writeInt(expense);
        out.writeInt(sum);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.id = in.readInt();
        this.income = in.readInt();
        this.expense = in.readInt();
        this.sum = in.readInt();
    }
}
