package org.example.Vigruzhator;

import Exceptions.ParamsException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
//import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.*;

public class FXMLController implements Initializable, ConnectorObserver/*, Observer*/ {
    private static final Logger logger = Logger.getLogger(FXMLController.class);

    //static PostgressConnector newConnection;// = new PostgressConnector();
    public TextArea ddlText;
    public TreeView<String> objectTree;
    public Button addButton;
    public TextField granteeName;
    public ListView<Grant> granteesList = new ListView<Grant>();
    public CheckBox isSelect;
    public CheckBox isInsert;
    public CheckBox isUpdate;
    public CheckBox isDelete;
    public CheckBox isTruncate;
    public CheckBox isReferences;
    public CheckBox isTrigger;
    public Button clearList;
    public TextField searchField;
    public Label activeObject;

    @FXML
    String ddlForTable;
    String textDDLForTable;
    private String path = null;
    List<TreeItem<String>> searhedItems = new ArrayList<>();

    ContextMenu objectContextMenu = new ContextMenu();
    MenuItem getDDLItem = new MenuItem("Получить DDL");
    MenuItem refresh = new MenuItem("Обновить");

    ContextMenu treeContextMenu = new ContextMenu();
    MenuItem refreshTree = new MenuItem("Обновить");

    ContextMenu grantContextMenu = new ContextMenu();
    MenuItem addToDDL = new MenuItem("Добавить к DDL");
    MenuItem showGrants = new MenuItem("Показать гранты");
    SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
    MenuItem deleteChoosed = new MenuItem("Удалить выбранные");

    {
        getDDLItem.setOnAction(event -> {
            System.out.println("Menu Item Clicked");
            Platform.runLater(this::getDDLForObject);
        });
        refresh.setOnAction(event -> {
            System.out.println("Menu Item Update Clicked");
            refreshTree();
        });
        addToDDL.setOnAction(event -> {
            System.out.println("Menu AddGrants Clicked");
            Platform.runLater(this::addGrantsToDDL);
        });
        showGrants.setOnAction(event -> {
            System.out.println("Menu GetGrants Clicked");
            Platform.runLater(this::showGrantsScript);
        });
        refreshTree.setOnAction(event -> {
            System.out.println("Menu Item Update Clicked");
            refreshTree();
        });
        addToDDL.setOnAction(event -> {
            System.out.println("Menu AddGrants Clicked");
            Platform.runLater(this::addGrantsToDDL);
        });
        showGrants.setOnAction(event -> {
            System.out.println("Menu GetGrants Clicked");
            Platform.runLater(this::showGrantsScript);
        });
        deleteChoosed.setOnAction(event -> {
            KeyEvent keyEvent = new KeyEvent(null, null, KeyEvent.KEY_TYPED, "\u007F", null, KeyCode.DELETE, false, false, false, false);
            deleteRecord(keyEvent);
        });
        objectContextMenu.getItems().addAll(getDDLItem, refresh);
        treeContextMenu.getItems().add(refreshTree);
        grantContextMenu.getItems().addAll(addToDDL, showGrants, separatorMenuItem, deleteChoosed);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        boolean flag = false;
        logger.info("Получение параметров подключения из файла настроек Settings.txt.");
        try (FileInputStream inputStream = new FileInputStream("Settings.txt"); ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            logger.info("Файл настроек получен. Создание подключения.");
            PostgressConnector newConnection = (PostgressConnector) objectInputStream.readObject();
            flag = PostgressConnector.checkParams();
            logger.info("Все параметры для подключения заданы.");
        } catch (ParamsException e) {
            logger.error(e.toString() + System.lineSeparator() + Caster.castStackTraceElementToString(e.getStackTrace()));
            e.printStackTrace();
        } catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + Caster.castStackTraceElementToString(e.getStackTrace()));
            e.printStackTrace();
        }

        if (!flag) {
            Platform.runLater(() -> {
                logger.warn("Не все параметры для подключения заданы или не найден файл с настройками подключения.");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setTitle("Ошибка");
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.initOwner(ddlText.getScene().getWindow());
                alert.setContentText("Необходимо настроить параметры подключения..");
                alert.showAndWait();
                showSettings(new ActionEvent());
            });
        } else {
            refreshTree();
        }

        //добавляем текущий класс в наблюдатели класса PostgressConnector
        PostgressConnector.registerObserver(this);

        clearList.getStyleClass().add("my-button");

        granteesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void refreshTree() {
        logger.info("Старт обновления дерева объектов.");

        Platform.runLater(() -> {
                    TreeItem<String> root = PostgressConnector.getObjectTree();
                    if (root != null) {
                        logger.info("Дерево объектов для БД " + root.getValue() + " получено.");
                        root.setExpanded(true);
                        objectTree.setRoot(root);
                        searhedItems.clear();
                        getAllLeafs(root);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText(null);
                        alert.setTitle("Ошибка");
                        alert.initModality(Modality.APPLICATION_MODAL);
                        alert.initOwner(ddlText.getScene().getWindow());
                        alert.setContentText("Не удалось подключиться к базе данных.\nПроверьте параметры подключения.");
                        alert.showAndWait();
                    }
                }
        );
    }

    public void chooseAction(ContextMenuEvent contextMenuEvent) {
        try {
            if (objectTree.getSelectionModel().getSelectedItem() != null) {
                if (!objectTree.getSelectionModel().getSelectedItem().getValue().equals(PostgressConnector.getDataBase())) {
                    String parentName = objectTree.getSelectionModel().getSelectedItem().getParent().getValue();
                    if (parentName.equals("Таблицы") || parentName.equals("Представления")) {
                        objectContextMenu.show(objectTree, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                    } else {
                        treeContextMenu.show(objectTree, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                    }
                } else {
                    treeContextMenu.show(objectTree, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                }
            } else {
                treeContextMenu.show(objectTree, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            }
        } catch (RuntimeException e) {
            logger.error(e.toString() + System.lineSeparator() + Caster.castStackTraceElementToString(e.getStackTrace()));
            e.printStackTrace();
        }
    }

    public void hideContextMenu(InputEvent mouseEvent) {
        objectContextMenu.hide();
        treeContextMenu.hide();
    }

    public void hideGrantContextMenu(InputEvent mouseEvent) {
        grantContextMenu.hide();
    }

    public void addGrantee(ActionEvent actionEvent) {
        logger.info("Добавление пользователя в список грантов.");
        Grant grantObject = new Grant(granteeName.getText(), isSelect.isSelected(), isInsert.isSelected(), isUpdate.isSelected(), isDelete.isSelected(), isTruncate.isSelected(), isReferences.isSelected(), isTrigger.isSelected());
        if ((!granteeName.getText().equals("")) && (isSelect.isSelected() || isInsert.isSelected() || isUpdate.isSelected() || isDelete.isSelected() || isTruncate.isSelected() || isReferences.isSelected() || isTrigger.isSelected())
                && !granteesList.getItems().contains(grantObject)) {
            granteesList.getItems().add(grantObject);
        }

        clearGrants(new ActionEvent());
    }

    public void getDDLForObject() {
        String schemaName = objectTree.getSelectionModel().getSelectedItem().getParent().getParent().getValue();
        String tableName = objectTree.getSelectionModel().getSelectedItem().getValue();
        String typeObject = objectTree.getSelectionModel().getSelectedItem().getParent().getValue().equals("Таблицы") ? "BASE TABLE" : "VIEW";
        String codeDDL;
        PostgresObject postgresObject = new PostgresObject(schemaName, tableName, typeObject);

        logger.info("Получение DDL для объекта " + schemaName + "." + tableName + " (" + typeObject + ").");

        codeDDL = postgresObject.getDDL();
        ddlText.setText(codeDDL);
        ddlForTable = schemaName + "." + tableName;
        activeObject.setText(ddlForTable);
        textDDLForTable = codeDDL;
    }

    public void clearGranteesList(ActionEvent actionEvent) {
        if (granteesList.getItems().size() > 0) {
            logger.info("Очистка списка грантов.");
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Подтверждение");
            confirm.setHeaderText("Вы уверены, что хотите очистить список грантов?");
            confirm.initOwner(granteesList.getScene().getWindow());
            ButtonType buttonYes = new ButtonType("Да");
            ButtonType buttonNo = new ButtonType("Нет");
            confirm.getButtonTypes().setAll(buttonYes, buttonNo);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() == buttonYes) {
                granteesList.getItems().clear();
            }
        }
    }

    public void closeProgram(ActionEvent actionEvent) {
        logger.info("Выход из приложения.");
        Platform.exit();
    }

    public void selectAllGrants(ActionEvent actionEvent) {
        isSelect.setSelected(true);
        isInsert.setSelected(true);
        isUpdate.setSelected(true);
        isDelete.setSelected(true);
        isTruncate.setSelected(true);
        isReferences.setSelected(true);
        isTrigger.setSelected(true);
    }

    public void clearGrants(ActionEvent actionEvent) {
        granteeName.clear();
        isSelect.setSelected(false);
        isInsert.setSelected(false);
        isUpdate.setSelected(false);
        isDelete.setSelected(false);
        isTruncate.setSelected(false);
        isReferences.setSelected(false);
        isTrigger.setSelected(false);
    }

    public void deleteRecord(KeyEvent keyEvent) {
        if (keyEvent.getCharacter().equals("\u007F")) {
            ObservableList<Grant> grantForDelete = FXCollections.observableArrayList(granteesList.getSelectionModel().getSelectedItems());

            for (Grant i : grantForDelete) {
                granteesList.getItems().remove(i);
            }

            granteesList.getSelectionModel().clearSelection();
        }
    }

    private void addGrantsToDDL() {
        logger.info("Добавление скрипта для выдачи грантов к DDL объекта " + ddlForTable + ".");
        ddlText.setText(textDDLForTable + "\n\n" + getGrantsScript());
    }

    private String getGrantsScript() {
        StringBuilder grantsScript = new StringBuilder();
        String templateForGrants = "DO $$\n" +
                "BEGIN\n" +
                /*"\t" + */"%1$s" +// "\n" +
                "END\n" +
                "$$;";
        for (int i : granteesList.getSelectionModel().getSelectedIndices()) {
            grantsScript.append(granteesList.getItems().get(i).getGrantString(ddlForTable)).append("\n\n");
        }
        grantsScript = new StringBuilder(String.format(templateForGrants, grantsScript.toString()));
        return grantsScript.toString();
    }

    private void showGrantsScript() {
        logger.info("Вывод скрипта для выдачи грантов к объекту" + ddlForTable + ".");
        ddlText.setText(getGrantsScript());
    }

    public void showGrantListMenu(ContextMenuEvent contextMenuEvent) {
        try {
            if (granteesList.getSelectionModel().getSelectedIndices().size() > 0) {
                grantContextMenu.show(granteesList, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            }
        } catch (RuntimeException e) {
            logger.error(e.toString() + System.lineSeparator() + Caster.castStackTraceElementToString(e.getStackTrace()));
        }

    }

    public void showSettings(ActionEvent actionEvent) {
        logger.info("Запуск окна настроек.");
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/Settings.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/styles/Styles.css");

            Stage settingsWindow = new Stage();

            settingsWindow.setTitle("Настройки");
            settingsWindow.setMinWidth(600);
            settingsWindow.setMinHeight(400);
            settingsWindow.setMaxWidth(600);
            settingsWindow.setMaxHeight(400);
            settingsWindow.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icon.ico")));
            settingsWindow.setResizable(false);
            /*
            Таким образом для нового окна можно указать родителя
            settingsWindow.initOwner(granteesList.getScene().getWindow());
             */
            //определяем вызываемое окно, как модальное. Определяем владельца вызываемого окна, т.е. текущую форму.
            //Таким образом окно с настройками всегда поверх основного и в панели задач всего один значок приложения
            settingsWindow.initModality(Modality.APPLICATION_MODAL);
            settingsWindow.initOwner(ddlText.getScene().getWindow());
            settingsWindow.setScene(scene);
            settingsWindow.showAndWait();
        } catch (IOException e) {
            logger.error(e.toString()  + System.lineSeparator() + Caster.castStackTraceElementToString(e.getStackTrace()));
            e.printStackTrace();
        }

    }

    public void saveDDL(ActionEvent actionEvent) {
        if (ddlText.getText().length() > 0) {
            logger.info("Сохранение файла с скриптами для объекта " + ddlForTable + ".");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить как...");
            fileChooser.setInitialFileName(ddlForTable);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL files", "*.sql"));
            if (path != null) {
                fileChooser.setInitialDirectory(new File(path));
            }
            File file = fileChooser.showSaveDialog(searchField.getScene().getWindow());

            if (file != null) {
                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write(ddlText.getText());
                    logger.info("Файл " + file.getName() + " сохранен.");
                    path = file.getParent();
                } catch (IOException e) {
                    logger.error(e.toString() + System.lineSeparator() + Caster.castStackTraceElementToString(e.getStackTrace()));
                    e.printStackTrace();
                }
                System.out.println("File " + file.getName() + " created");
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setTitle("Предупреждение");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initOwner(ddlText.getScene().getWindow());
            alert.setContentText("Нет данных для сохранения.");
            alert.showAndWait();
        }
    }

    private void getAllLeafs(TreeItem<String> item) {
        if (item.isLeaf()) {
            searhedItems.add(item);
        } else {
            List<TreeItem<String>> childrens = item.getChildren();
            for (TreeItem<String> childItem : childrens) {
                getAllLeafs(childItem);
            }
        }
    }

    public void searchObjectReleased(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.BACK_SPACE || keyEvent.getCode().isLetterKey() || (keyEvent.getText().length() > 0 ? Character.isDigit(keyEvent.getText().charAt(0)) : false)
                || keyEvent.getCode() == KeyCode.MINUS || keyEvent.getCode() == KeyCode.PERIOD) {
            System.out.println("Зашли");

            if (searchField.getText().contains(".") && searchField.getText().length() > 1 && searchField.getText().indexOf(".") < searchField.getText().length() - 1) {
                String[] schemaAndName = searchField.getText().split("\\.");
                for (TreeItem<String> item : searhedItems) {
                    if ((item.getParent().getParent().getValue()).toUpperCase().equals(schemaAndName[0].toUpperCase())
                            && (item.getValue()).toUpperCase().contains(schemaAndName[1].toUpperCase())) {
                        objectTree.getSelectionModel().select(item);
                        objectTree.scrollTo(objectTree.getSelectionModel().getSelectedIndex() - 9);
                        return;
                    }
                }
                System.out.println("Ничего не нашли");
            } else if (searchField.getText().length() > 0 && !searchField.getText().contains(".")) {
                for (TreeItem<String> item : searhedItems) {
                    if ((item.getValue()).toUpperCase().contains(searchField.getText().toUpperCase())) {
                        objectTree.getSelectionModel().select(item);
                        objectTree.scrollTo(objectTree.getSelectionModel().getSelectedIndex() - 9);
                        return;
                    }
                }
                System.out.println("Ничего не нашли");
            }
        }
    }

    @Override
    public void update(String message) {
        refreshTree();
        System.out.println("Updated with java interface Observer");
    }
}
