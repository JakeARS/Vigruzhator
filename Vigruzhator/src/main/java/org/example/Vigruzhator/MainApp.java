package org.example.Vigruzhator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.log4j.Logger;


public class MainApp extends Application {
    private static final String title = "Выгружатр";

    /*
    Логирование с помощью org.apache.log4j.rolling.RollingFileAppender
    файл log4j.properties:
    #level logging
    log4j.rootLogger=INFO, file

    #Appender for work with files
    log4j.appender.file=org.apache.log4j.rolling.RollingFileAppender
    log4j.appender.file.rollingPolicy=org.apache.log4j.rolling.TimeBasedRollingPolicy
    #path to keep logs and mask
    log4j.appender.file.rollingPolicy.FileNamePattern=logs\\log-.%d{yyyy-MM-dd}.log

    #configure pattern of output of logs into file
    log4j.appender.file.layout=org.apache.log4j.PatternLayout
    log4j.appender.file.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
     */
    private static final Logger logger = Logger.getLogger(MainApp.class);

    @Override
    public void start(Stage stage) throws Exception {
        logger.info("Старт приложения");
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        /*
        Обработка события ALT. Проблема была с тем, что при сворачивании с ALT+TAB подсвечивался MenuBar
         */
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isAltDown()) {
                event.consume();
            }
        });
        stage.setTitle(title);
        stage.setMinWidth(1330);
        stage.setMinHeight(770);
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icon.ico")));
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
