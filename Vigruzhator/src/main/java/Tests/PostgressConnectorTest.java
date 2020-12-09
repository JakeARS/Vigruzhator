package Tests;

import org.example.Vigruzhator.PostgresObject;
import org.example.Vigruzhator.PostgressConnector;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;

public class PostgressConnectorTest {
    @BeforeClass
    public static void setUp() {

        try (FileInputStream inputStream = new FileInputStream("Settings.txt"); ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);) {
            PostgressConnector newConnection = (PostgressConnector) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getFullCode() {
        String recievedDDL = PostgressConnector.getFullCode("jakears", "test_table", "BASE TABLE");
        String expectedDDL = "do $$\n" +
                "begin\n" +
                "\tif exists (select * from information_schema.\"tables\"\n" +
                "\t\twhere table_schema = 'jakears' and table_name = 'test_table' and table_type = 'BASE TABLE') then \n" +
                "\t\tdrop table jakears.test_table;\n" +
                "\tend if;\n" +
                "end $$;\n" +
                "\n" +
                "CREATE TABLE jakears.test_table (\n" +
                "                    id numeric NOT NULL,\n" +
                "                    name text NULL,\n" +
                "                    CONSTRAINT test_table_pkey PRIMARY KEY (id))\n" +
                "             \n" +
                "                     ;";

        Assert.assertEquals(recievedDDL, expectedDDL);
        Assert.assertNotNull(recievedDDL);
    }

    @Test
    public void getObjectList() {

        ArrayList<PostgresObject> objectList = PostgressConnector.getObjectList();
        objectList.clear();

        Assert.assertTrue(objectList.size() == 0);
    }
}