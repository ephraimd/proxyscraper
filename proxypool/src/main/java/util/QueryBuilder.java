package util;

import contracts.ProxyItem;

import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {
    private StringBuilder sb = new StringBuilder();
    public QueryBuilder(){
        //
    }
    public QueryBuilder insertInto(String table){
        this.sb.append("INSERT INTO `".concat(table.concat("` ")));
        return this;
    }
    public QueryBuilder inColumns(List<String> columns){
        this.sb.append("(");
        for(String c : columns){
            sb.append("`").append(c).append("`, ");
        }
        //remove the last ', '
        this.sb.delete(sb.length()-2, sb.length()).append(")  VALUES ");
        return this;
    }
    public QueryBuilder end(){
        sb.append("; ");
        return this;
    }
    public QueryBuilder clear(){
        sb.setLength(0);
        return this;
    }
    public QueryBuilder proxyValue(boolean willAddMore, ProxyItem v){
        this.sb.append("(");
        sb.append("'").append(v.ipAddress).append("', ");
        sb.append("'").append(v.port).append("', ");
        sb.append(v.quality).append(", ");
        sb.append("'").append(v.worksOnGoogle).append("', ");
        sb.append("'").append(v.location.toLowerCase()).append("', ");
        sb.append("'").append(v.city.toLowerCase()).append("', ");
        sb.append("'").append(v.type.toLowerCase()).append("', ");
        sb.append("'").append(v.speed).append("', ");
        sb.append("'").append(v.security.toLowerCase()).append("', ");
        sb.append("'").append(v.state).append("', ");
        sb.append("NOW()");
        sb.append(willAddMore ? "), ": ") ");
        return this;
    }
    public List<String> insertProxyValues(List<ProxyItem> vals){
        List<String> qList = new ArrayList<>(vals.size());
        for(ProxyItem v : vals){
            if(v.ipAddress == null || v.ipAddress.isEmpty()){
                continue;
            }
            clear();
            insertInto("proxies");
            inColumns(ProxyItem.databaseColumns);
            sb.append("(");
            sb.append("'").append(v.ipAddress).append("', ");
            sb.append("'").append(v.port).append("', ");
            sb.append(v.quality).append(", ");
            sb.append("'").append(v.worksOnGoogle).append("', ");
            sb.append("'").append(v.location.toLowerCase()).append("', ");
            sb.append("'").append(v.city.toLowerCase()).append("', ");
            sb.append("'").append(v.type.toLowerCase()).append("', ");
            sb.append("'").append(v.speed).append("', ");
            sb.append("'").append(v.security.toLowerCase()).append("', ");
            sb.append("'").append(v.state).append("', ");
            sb.append("NOW()");
            sb.append(")");
            qList.add(sb.toString());
        }
        return qList;
    }
    public QueryBuilder proxyValues(boolean willAddMore, List<ProxyItem> vals){
        //orders as the columns are ordered in the DB. see ProxyItem.databaseColumns
        for(ProxyItem v : vals){
            this.sb.append("(");
            sb.append("'").append(v.ipAddress).append("', ");
            sb.append("'").append(v.port).append("', ");
            sb.append(v.quality).append(", ");
            sb.append("'").append(v.worksOnGoogle).append("', ");
            sb.append("'").append(v.location.toLowerCase()).append("', ");
            sb.append("'").append(v.city.toLowerCase()).append("', ");
            sb.append("'").append(v.type.toLowerCase()).append("', ");
            sb.append("'").append(v.speed).append("', ");
            sb.append("'").append(v.security.toLowerCase()).append("', ");
            sb.append("'").append(v.state).append("', ");
            sb.append("NOW()");
            sb.append("), ");
        }
        //remove the last ', '
        if(!willAddMore) {
            this.sb.delete(sb.length() - 2, sb.length());
        }
        return this;
    }
    public String build(){
        return sb.toString();
    }

    public static enum ValueType{INTEGER, STRING, CUR_DATE, CUR_TIME, DATE_STAMP, NULL}
    public static class Value{
        public ValueType type_ = ValueType.NULL;
        private Object value;

        public Value(Object value, ValueType type){
            this.type_ = type;
            this.value = value;
        }
        public Object getValue(){
            return value;
        }
    }
}
