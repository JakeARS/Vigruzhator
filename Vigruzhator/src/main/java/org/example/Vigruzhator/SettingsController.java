package org.example.Vigruzhator;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
//import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    private static final Logger logger = Logger.getLogger(SettingsController.class);

    public Button okButton;
    public Button cancelButton;
    public Button acceptButton;
    public TextField login;
    public PasswordField password;
    public TextField host;
    public TextField port;
    public TextField dataBase;

    private String hst;
    private String prt;
    private String db;
    private String lgn;
    private String pswd;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("Окно настроек запущено.\nСчитывание текущих параметров подключения.");
        host.setText(PostgressConnector.getHost());
        port.setText(PostgressConnector.getPort());
        dataBase.setText(PostgressConnector.getDataBase());

        login.setText(PostgressConnector.getUser());
        try {
            password.setText(Coder.decodePassword(PostgressConnector.getPassword()));
        } catch (Exception e) {

        }

        okButton.setDefaultButton(true);
    }

    public void ok(ActionEvent actionEvent) {
        accept(actionEvent);
        cancel(actionEvent);
    }

    public void cancel(ActionEvent actionEvent) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void accept(ActionEvent actionEvent) {
        hst = host.getText();
        prt = port.getText();
        db = dataBase.getText();
        lgn = login.getText();
        pswd = password.getText();

        String oldHost = PostgressConnector.getHost();
        String oldPort = PostgressConnector.getPort();
        String oldDataBase = PostgressConnector.getDataBase();
        String oldLgn = PostgressConnector.getUser();
        String oldPswd = PostgressConnector.getPassword();

        try {
            if (!hst.equals(oldHost) || !prt.equals(oldPort) || !db.equals(oldDataBase) || !lgn.equals(oldLgn) || !pswd.equals(oldPswd)) {
                logger.info("Параметры подключения изменились. Сохранение новых параметров.");
                try (FileOutputStream outputStream = new FileOutputStream("Settings.txt"); ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);) {
                    PostgressConnector postgressConnector = new PostgressConnector(hst, prt, db, lgn, pswd);
                    objectOutputStream.writeObject(postgressConnector);
                    logger.info("Параметры подключения записаны в файл Settings.txt.");
                    password.setText(PostgressConnector.getPassword());
                } catch (IOException e) {
                    logger.error(e.toString() + System.lineSeparator() + Caster.castStackTraceElementToString(e.getStackTrace()));
                }
            }
        } catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + Caster.castStackTraceElementToString(e.getStackTrace()));
            e.printStackTrace();
        }
    }

    public void callOk(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            ok(new ActionEvent());
        }
    }
}
