package org.example.Vigruzhator;

import Exceptions.ParamsException;
import javafx.scene.control.TreeItem;
import org.apache.log4j.Logger;
//import org.apache.log4j.Logger;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class PostgressConnector extends Observable implements Externalizable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(PostgressConnector.class);
    private static String host;
    private static String port;
    private static String dataBase;
    private static String user;
    private static String password;

    private static List<ConnectorObserver> observers = new ArrayList<>();

    public PostgressConnector() {

    }

    public PostgressConnector (String host, String port, String dataBase, String user, String password) throws Exception {
        this.host = host;
        this.port = port;
        this.dataBase = dataBase;
        this.user = user;
        this.password = Coder.encodePassword(password);
        notifyObservers("Connection is changed");
    }

    public static String getHost() {
        return host;
    }

    public static String getPort() {
        return port;
    }

    public static String getDataBase() {
        return dataBase;
    }

    public static String getUser() {
        return user;
    }

    public static String getPassword() { return password; }

    public static String getFullCode(String schema, String tableName, String tableType) {
        String textCode;
        String ddlText;
        String type = tableType.equals("BASE TABLE") ? "table" : "view";
        String scriptTenplate = "do $$\n" +
                "begin\n" +
                "\tif exists (select * from information_schema.\"tables\"\n" +
                "\t\twhere table_schema = '%1$s' and table_name = '%2$s' and table_type = '%3$s') then \n" +
                "\t\tdrop %4$s %1$s.%2$s;\n" +
                "\tend if;\n" +
                "end $$;\n\n" +
                "%5$s";

        if (type.equals("table")) {
            ddlText = getDDLForTable(schema, tableName);
        } else {
            ddlText = getDDLForView(schema, tableName);
        }

        textCode = String.format(scriptTenplate, schema, tableName, tableType, type, ddlText);

        return textCode;
    }

    private static String getDDLForTable(String schema, String tableName) {
        ArrayList<String> arrResults = new ArrayList<>();
        String sqlStatement = "select public.get_table_ddl('" + schema + "." + tableName + "') definition";
        logger.info("Подключение к БД и получение DDL для таблицы " + schema + "." + tableName + ".");
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+dataBase+"?", user, Coder.decodePassword(password)); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sqlStatement)) {
            result.next();
            String ddlCode = result.getString("definition");
            logger.info("DDL для таблицы " + schema + "." + tableName + " получен.");
            return ddlCode;
        } catch (SQLException e) {
            logger.error(e.toString() + System.lineSeparator() + Caster.castStackTraceElementToString(e.getStackTrace()));
            e.printStackTrace();
        } catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + Caster.castStackTraceElementToString(e.getStackTrace()));
            e.printStackTrace();
        }
        return null;
    }

    private static String getDDLForView(String schema, String tableName) {
        ArrayList<String> arrResults = new ArrayList<>();
        String createExpr = "create or replace view " + schema + "." + tableName + " as\n";
        String sqlStatement = "select view_definition from information_schema.\"views\" v\n" +
                "where table_catalog = '" + dataBase + "' and table_schema = '" + schema + "' and table_name = '" + tableName + "'";
        logger.info("Подключение к БД и получение DDL для представления " + schema + "." + tableName + ".");
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+dataBase+"?", user, Coder.decodePassword(password)); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sqlStatement)) {
            result.next();
            String ddlCode = createExpr + result.getString("view_definition");
            logger.info("DDL для представления " + schema + "." + tableName + " получен.");
            return ddlCode;
        } catch (SQLException e) {
            logger.error(e.toString() + System.lineSeparator() + Caster.castStackTraceElementToString(e.getStackTrace()));
            e.printStackTrace();
        } catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + Caster.castStackTraceElementToString(e.getStackTrace()));
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<PostgresObject> getObjectList() {
        ArrayList<PostgresObject> objectList = new ArrayList<>();
        String sqlStatement = "select table_schema, table_name, table_type from information_schema.\"tables\" t \n" +
                "where table_type in ('BASE TABLE', 'VIEW') and table_catalog = '" + dataBase + "'\n" +
                "order by table_schema, table_name";

        logger.info("Подключение к БД и получение списка объектов.");
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+dataBase+"?", user, Coder.decodePassword(password)); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sqlStatement)) {

            System.out.println("jdbc:postgresql://"+host+":"+port+"/"+dataBase+"?");
            System.out.println(user);
            System.out.println(Coder.decodePassword(password));

            while (result.next()) {
                objectList.add(new PostgresObject(result.getString("table_schema"), result.getString("table_name"), result.getString("table_type")));
            }
            logger.info("Список объектов получен.");
        }  catch (SQLException e) {
            logger.error(e.toString() + System.lineSeparator() + Caster.castStackTraceElementToString(e.getStackTrace()));
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + Caster.castStackTraceElementToString(e.getStackTrace()));
            e.printStackTrace();
            return null;
        }

        return objectList;
    }

    public static TreeItem<String> getObjectTree() {
        ArrayList<PostgresObject> objectList;
        TreeItem<String> baseName = new TreeItem<>(dataBase);
        ArrayList<String> schemasNames = new ArrayList<>();

        objectList = getObjectList();

        if (objectList != null) {
            for (PostgresObject obj : objectList) {
                TreeItem<String> tables = new TreeItem<>("Таблицы");
                TreeItem<String> views = new TreeItem<>("Представления");
                if (!schemasNames.contains(obj.getTableSchema())) {
                    schemasNames.add(obj.getTableSchema());
                    TreeItem<String> schemaName = new TreeItem<>(obj.getTableSchema());
                    schemaName.getChildren().addAll(tables, views);
                    baseName.getChildren().add(schemaName);
                }

                TreeItem<String> tableName = new TreeItem<>(obj.getTableName());

                for (TreeItem<String> i : baseName.getChildren()) {
                    if (obj.getTableSchema().equals(i.getValue())) {
                        for (TreeItem<String> j : i.getChildren()) {
                            if (j.getValue().equals("Таблицы") && obj.getTableType().equals("BASE TABLE")) {
                                j.getChildren().add(tableName);
                            } else if (j.getValue().equals("Представления") && obj.getTableType().equals("VIEW")) {
                                j.getChildren().add(tableName);
                            }
                        }
                    }
                }
            }
        } else {
            return null;
        }

        return baseName;
    }

    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeObject(this.getHost());
        objectOutput.writeObject(this.getPort());
        objectOutput.writeObject(this.getDataBase());
        objectOutput.writeObject(this.getUser());
        objectOutput.writeObject(this.getPassword());
    }

    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        host = (String) objectInput.readObject();
        port = (String) objectInput.readObject();
        dataBase =(String) objectInput.readObject();
        user = (String) objectInput.readObject();
        password = (String) objectInput.readObject();
        notifyObservers("Connection is changed");
    }

    public static void registerObserver(ConnectorObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers(String var1) {
        for (ConnectorObserver observer : observers) {
            observer.update(var1);
        }
    }

    public static boolean checkParams() throws Exception {
        if (host.length() != 0 && port.length() != 0 && dataBase.length() != 0 && user.length() != 0 && Coder.decodePassword(password).length() != 0) {
            return true;
        } else {
            throw new ParamsException("Не все параметры заполнены.");
        }
    }
}
