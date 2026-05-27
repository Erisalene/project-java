package com.project.gui;

import com.project.model.Book;
import com.project.model.Catalogue;
import com.project.model.Publication;
import com.project.exception.BookNotFoundException;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;

public class BookGUI extends Application {

    private final Catalogue catalogue = new Catalogue();
    private final ObservableList<Publication> observableList = FXCollections.observableArrayList();

    private TextField txtTitle, txtAuthor, txtPublisher, txtYear, txtGenre, txtSearch;
    private ListView<Publication> listView;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Керування каталогом книг");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(8);

        grid.add(new Label("Назва:"), 0, 0);
        txtTitle = new TextField();
        grid.add(txtTitle, 1, 0);

        grid.add(new Label("Автор:"), 0, 1);
        txtAuthor = new TextField();
        grid.add(txtAuthor, 1, 1);

        grid.add(new Label("Видавництво:"), 0, 2);
        txtPublisher = new TextField();
        grid.add(txtPublisher, 1, 2);

        grid.add(new Label("Рік видання:"), 0, 3);
        txtYear = new TextField();
        grid.add(txtYear, 1, 3);

        grid.add(new Label("Жанр:"), 0, 4);
        txtGenre = new TextField();
        grid.add(txtGenre, 1, 4);

        HBox actionButtons = new HBox(10);
        Button btnAdd = new Button("Додати книгу");
        Button btnDelete = new Button("Видалити книгу");
        Button btnUpdate = new Button("Оновити книгу");
        actionButtons.getChildren().addAll(btnAdd, btnDelete, btnUpdate);
        grid.add(actionButtons, 0, 5, 2, 1);

        HBox fileButtons = new HBox(10);
        Button btnSave = new Button("Зберегти у файл");
        Button btnLoad = new Button("Завантажити з файлу");
        fileButtons.getChildren().addAll(btnSave, btnLoad);
        grid.add(fileButtons, 0, 6, 2, 1);

        HBox searchBox = new HBox(10);
        txtSearch = new TextField();
        txtSearch.setPromptText("Введіть назву для пошуку...");
        Button btnSearch = new Button("Пошук");
        Button btnReset = new Button("Скинути");
        searchBox.getChildren().addAll(txtSearch, btnSearch, btnReset);
        grid.add(searchBox, 0, 7, 2, 1);

        listView = new ListView<>(observableList);
        listView.setPrefWidth(450);

        BorderPane root = new BorderPane();
        root.setLeft(grid);
        root.setCenter(listView);
        root.setPadding(new Insets(10));

        btnAdd.setOnAction(e -> handleAddBook());
        btnDelete.setOnAction(e -> handleDeleteBook());
        btnUpdate.setOnAction(e -> handleUpdateBook());
        btnSave.setOnAction(e -> handleSaveToFile());
        btnLoad.setOnAction(e -> handleLoadFromFile());
        btnSearch.setOnAction(e -> handleSearch());
        btnReset.setOnAction(e -> handleResetSearch());

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal instanceof Book) {
                Book book = (Book) newVal;
                txtTitle.setText(book.getTitle());
                txtAuthor.setText(book.getAuthor());
                txtPublisher.setText(book.getPublisher());
                txtYear.setText(String.valueOf(book.getYear()));
                txtGenre.setText(book.getGenre());
            }
        });

        primaryStage.setScene(new Scene(root, 850, 450));
        primaryStage.show();
    }


    private void handleAddBook() {
        try {
            String title = txtTitle.getText().trim();
            String author = txtAuthor.getText().trim();
            String publisher = txtPublisher.getText().trim();
            int year = Integer.parseInt(txtYear.getText().trim());
            String genre = txtGenre.getText().trim();

            if (title.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Назва книги не може бути порожньою.");
                return;
            }

            Book book = new Book(title, year, author, publisher, genre);
            catalogue.addPublication(book);
            refreshList();
            clearInputFields();
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Рік видання має бути числовим значенням.");
        }
    }

    private void handleDeleteBook() {
        String title = txtTitle.getText().trim();
        try {
            catalogue.removePublicationByTitle(title);
            refreshList();
            clearInputFields();
            showAlert(Alert.AlertType.INFORMATION, "Успіх", "Публікацію успішно видалено.");
        } catch (BookNotFoundException ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка видалення", ex.getMessage());
        }
    }

    private void handleUpdateBook() {
        String title = txtTitle.getText().trim();
        Publication pub = catalogue.findPublicationByTitle(title);

        if (pub instanceof Book) {
            try {
                Book book = (Book) pub;
                book.setAuthor(txtAuthor.getText().trim());
                book.setPublisher(txtPublisher.getText().trim());
                book.setYear(Integer.parseInt(txtYear.getText().trim()));
                book.setGenre(txtGenre.getText().trim());

                refreshList();
                showAlert(Alert.AlertType.INFORMATION, "Успіх", "Дані книги оновлено.");
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Рік видання має бути числовим значенням.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Книгу з такою назвою не знайдено для оновлення.");
        }
    }

    private void handleSaveToFile() {
        try {
            catalogue.saveToFile("catalogue.dat");
            showAlert(Alert.AlertType.INFORMATION, "Успіх", "Каталог успішно збережено у файл 'catalogue.dat'.");
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка файлу", "Не вдалося зберегти дані: " + ex.getMessage());
        }
    }

    private void handleLoadFromFile() {
        try {
            catalogue.loadFromFile("catalogue.dat");
            refreshList();
            showAlert(Alert.AlertType.INFORMATION, "Успіх", "Каталог успішно завантажено з файлу.");
        } catch (IOException | ClassNotFoundException ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка файлу", "Не вдалося завантажити дані: " + ex.getMessage());
        }
    }

    private void handleSearch() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshList();
            return;
        }

        observableList.clear();
        for (Publication p : catalogue.getAllPublications()) {
            if (p.getTitle().toLowerCase().contains(query)) {
                observableList.add(p);
            }
        }
    }

    private void handleResetSearch() {
        txtSearch.clear();
        refreshList();
    }

    private void refreshList() {
        observableList.setAll(catalogue.getAllPublications());
    }

    private void clearInputFields() {
        txtTitle.clear();
        txtAuthor.clear();
        txtPublisher.clear();
        txtYear.clear();
        txtGenre.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
