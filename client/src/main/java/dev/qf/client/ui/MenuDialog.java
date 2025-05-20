package dev.qf.client.ui;

import common.Menu;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;

public class MenuDialog extends Dialog<Menu> {
    private final TextField nameField = new TextField();
    private final TextField priceField = new TextField();
    private final ComboBox<String> categoryCombo = new ComboBox<>();
    private final TextArea descriptionArea = new TextArea();
    private final TextField imagePathField = new TextField();
    private final ImageView imagePreview = new ImageView();
    private final Button browseButton = new Button("파일 선택...");
    private final CheckBox soldOutCheckBox = new CheckBox("품절");

    public MenuDialog() {
        setTitle("메뉴 추가");
        setHeaderText("새 메뉴 정보를 입력하세요");

        ButtonType addButtonType = new ButtonType("추가", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        imagePreview.setFitHeight(150);
        imagePreview.setFitWidth(150);
        imagePreview.setPreserveRatio(true);

        try {
            imagePreview.setImage(new Image("/resources/no_image.png"));
        } catch (Exception e) {
            System.out.println("기본 이미지를 찾을 수 없습니다: " + e.getMessage());
        }

        VBox imageBox = new VBox(10);
        imageBox.setAlignment(Pos.CENTER);
        imageBox.getChildren().add(imagePreview);

        HBox imagePathBox = new HBox(5);
        imagePathBox.getChildren().addAll(imagePathField, browseButton);
        imagePathField.setPrefWidth(200);

        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("이미지 파일 선택");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("이미지 파일", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                    new FileChooser.ExtensionFilter("모든 파일", "*.*")
            );

            File selectedFile = fileChooser.showOpenDialog(getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                imagePathField.setText(selectedFile.getAbsolutePath());
                try {
                    imagePreview.setImage(new Image(selectedFile.toURI().toString()));
                } catch (Exception ex) {
                    System.out.println("이미지를 불러올 수 없습니다: " + ex.getMessage());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("이미지 오류");
                    alert.setContentText("이미지를 불러올 수 없습니다.");
                    alert.showAndWait();
                }
            }
        });

        categoryCombo.setItems(FXCollections.observableArrayList("커피", "차", "디저트", "기타"));
        categoryCombo.getSelectionModel().selectFirst();

        descriptionArea.setPrefRowCount(3);

        grid.add(imageBox, 0, 0, 1, 4);
        grid.add(new Label("메뉴명:"), 1, 0);
        grid.add(nameField, 2, 0);
        grid.add(new Label("가격:"), 1, 1);
        grid.add(priceField, 2, 1);
        grid.add(new Label("카테고리:"), 1, 2);
        grid.add(categoryCombo, 2, 2);
        grid.add(new Label("설명:"), 1, 3);
        grid.add(descriptionArea, 2, 3);
        grid.add(new Label("이미지:"), 1, 4);
        grid.add(imagePathBox, 2, 4);
        grid.add(new Label("상태:"), 1, 5);
        grid.add(soldOutCheckBox, 2, 5);

        getDialogPane().setContent(grid);

        setResultConverter(new Callback<ButtonType, Menu>() {
            @Override
            public Menu call(ButtonType buttonType) {
                if (buttonType == addButtonType) {
                    try {
                        String name = nameField.getText().trim();
                        if (name.isEmpty()) {
                            showErrorAlert("메뉴명을 입력해주세요.");
                            return null;
                        }

                        String priceText = priceField.getText().trim();
                        if (priceText.isEmpty()) {
                            showErrorAlert("가격을 입력해주세요.");
                            return null;
                        }
                        double price = Double.parseDouble(priceText);

                        int categoryId = categoryCombo.getSelectionModel().getSelectedIndex() + 1;
                        String description = descriptionArea.getText().trim();
                        String imagePath = imagePathField.getText().trim();
                        boolean soldOut = soldOutCheckBox.isSelected();

                        return new Menu(0, name, price, categoryId, description, imagePath, soldOut);
                    } catch (NumberFormatException e) {
                        showErrorAlert("가격은 숫자로 입력해야 합니다.");
                        return null;
                    }
                }
                return null;
            }
        });

        nameField.requestFocus();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("입력 오류");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
