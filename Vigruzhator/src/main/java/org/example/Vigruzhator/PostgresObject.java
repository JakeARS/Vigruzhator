package org.example.Vigruzhator;

public class PostgresObject {
    private String tableSchema;
    private String tableName;
    private String tableType;

    PostgresObject(String tableSchema, String tableName, String tableType) {
        this.tableSchema = tableSchema;
        this.tableName = tableName;
        this.tableType = tableType;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    @Override
    public String toString() {
        return tableName;
    }

    public String getDDL() {
        String ddlCode = new String();
        ddlCode = PostgressConnector.getFullCode(tableSchema, tableName, tableType);
        return ddlCode;
    }
}
