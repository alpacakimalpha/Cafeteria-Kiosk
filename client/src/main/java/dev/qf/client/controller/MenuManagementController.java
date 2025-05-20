package dev.qf.client.ui;

import common.Menu;
import common.service.AdminService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MenuManagementController implements Initializable {

    @FXML private TableView<Menu> menuTable;
    @FXML private TableColumn<Menu, Integer> idColumn;
    @FXML private TableColumn<Menu, String> nameColumn;
    @FXML private TableColumn<Menu, String> categoryColumn;
    @FXML private TableColumn<Menu, Double> priceColumn;
    @FXML private TableColumn<Menu, Boolean> soldOutColumn;

    @FXML private ImageView selectedMenuImage;
    @FXML private Label selectedMenuName;
    @FXML private Label selectedMenuPrice;
    @FXML private Label selectedMenuCategory;
    @FXML private TextArea selectedMenuDescription;
    @FXML private Label selectedMenuStatus;

    @FXML private ComboBox<CategoryItem> categoryFilter;
    @FXML private FlowPane menuItemsPane;

    private final AdminService adminService = new AdminService();
    private ObservableList<Menu> menuList = FXCollections.observableArrayList();

    public static class CategoryItem {
        private final int id;
        private final String name;

        public CategoryItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().menuId()).asObject());
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().name()));
        categoryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        adminService.getCategoryName(cellData.getValue().categoryId())
                )
        );
        priceColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().price()).asObject());

        if (soldOutColumn != null) {
            soldOutColumn.setCellValueFactory(cellData ->
                    new SimpleBooleanProperty(cellData.getValue().soldOut()).asObject());
            soldOutColumn.setCellFactory(column -> new TableCell<Menu, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item ? "품절" : "판매중");
                        setStyle(item ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
                    }
                }
            });
        }

        menuTable.setItems(menuList);

        menuTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showMenuDetails(newValue)
        );

        initCategoryComboBox();

        loadTestData();

        updateMenuItemsView(0);
    }

    private void initCategoryComboBox() {
        ObservableList<CategoryItem> categories = FXCollections.observableArrayList(
                new CategoryItem(0, "전체"),
                new CategoryItem(1, "커피"),
                new CategoryItem(2, "차"),
                new CategoryItem(3, "디저트"),
                new CategoryItem(4, "기타")
        );

        categoryFilter.setItems(categories);
        categoryFilter.getSelectionModel().selectFirst();
    }

    private void loadTestData() {
        adminService.registerMenu("아메리카노", 4500, 1, "깊고 풍부한 맛의 에스프레소에 물을 더한 커피", "/resources/americano.png", false);
        adminService.registerMenu("카페라떼", 5000, 1, "에스프레소에 부드러운 우유를 넣은 커피", "/resources/latte.png", false);
        adminService.registerMenu("녹차", 4000, 2, "향긋한 녹차", "/resources/greentea.png", false);
        adminService.registerMenu("치즈케이크", 6000, 3, "부드럽고 달콤한 치즈케이크", "/resources/cheesecake.png", true);

        refreshMenuList();
    }

    private void refreshMenuList() {
        menuList.clear();
        menuList.addAll(adminService.getAllMenus());
    }

    @FXML
    private void handleCategorySelection() {
        CategoryItem selectedCategory = categoryFilter.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            updateMenuItemsView(selectedCategory.getId());
        }
    }

    private void updateMenuItemsView(int categoryId) {
        menuItemsPane.getChildren().clear();

        List<Menu> menus = adminService.getAllMenus();

        for (Menu menu : menus) {
            if (categoryId != 0 && menu.categoryId() != categoryId) {
                continue;
            }

            VBox menuBox = createMenuItemBox(menu);
            menuItemsPane.getChildren().add(menuBox);
        }
    }

    private VBox createMenuItemBox(Menu menu) {
        VBox menuBox = new VBox(5);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(10));
        menuBox.setPrefWidth(150);
        menuBox.setPrefHeight(200);

        if (menu.soldOut()) {
            menuBox.setStyle("-fx-border-color: #FF6B6B; -fx-border-radius: 5; -fx-background-color: #FFE8E8; -fx-opacity: 0.7;");
        } else {
            menuBox.setStyle("-fx-border-color: #CCCCCC; -fx-border-radius: 5; -fx-background-color: white;");
        }

        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        try {
            String imagePath = menu.imagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                Image image;
                if (imagePath.startsWith("/")) {
                    image = new Image(getClass().getResourceAsStream(imagePath));
                } else {
                    File file = new File(imagePath);
                    if (file.exists()) {
                        image = new Image(file.toURI().toString());
                    } else {
                        image = new Image(getClass().getResourceAsStream("/resources/no_image.png"));
                    }
                }
                imageView.setImage(image);
            } else {
                imageView.setImage(new Image(getClass().getResourceAsStream("/resources/no_image.png")));
            }
        } catch (Exception e) {
            System.out.println("이미지를 불러올 수 없습니다: " + e.getMessage());
            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/resources/no_image.png")));
            } catch (Exception ex) {
            }
        }

        Label nameLabel = new Label(menu.name());
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label priceLabel = new Label(String.format("%,d원", (int)menu.price()));

        Label statusLabel = new Label(menu.soldOut() ? "품절" : "판매중");
        statusLabel.setStyle(menu.soldOut() ?
                "-fx-text-fill: red; -fx-font-weight: bold;" :
                "-fx-text-fill: green; -fx-font-weight: bold;");

        menuBox.setOnMouseClicked(event -> {
            menuTable.getSelectionModel().select(menu);
            showMenuDetails(menu);
        });

        menuBox.getChildren().addAll(imageView, nameLabel, priceLabel, statusLabel);

        return menuBox;
    }

    private void showMenuDetails(Menu menu) {
        if (menu != null) {
            selectedMenuName.setText(menu.name());
            selectedMenuPrice.setText(String.format("%,d원", (int)menu.price()));
            selectedMenuCategory.setText(adminService.getCategoryName(menu.categoryId()));
            selectedMenuDescription.setText(menu.description());

            if (selectedMenuStatus != null) {
                selectedMenuStatus.setText(menu.soldOut() ? "품절" : "판매중");
                selectedMenuStatus.setStyle(menu.soldOut() ?
                        "-fx-text-fill: red; -fx-font-weight: bold;" :
                        "-fx-text-fill: green; -fx-font-weight: bold;");
            }

            try {
                String imagePath = menu.imagePath();
                if (imagePath != null && !imagePath.isEmpty()) {
                    Image image;
                    if (imagePath.startsWith("/")) {
                        image = new Image(getClass().getResourceAsStream(imagePath));
                    } else {
                        File file = new File(imagePath);
                        if (file.exists()) {
                            image = new Image(file.toURI().toString());
                        } else {
                            image = new Image(getClass().getResourceAsStream("/resources/no_image.png"));
                        }
                    }
                    selectedMenuImage.setImage(image);
                } else {
                    selectedMenuImage.setImage(new Image(getClass().getResourceAsStream("/resources/no_image.png")));
                }
            } catch (Exception e) {
                System.out.println("이미지를 불러올 수 없습니다: " + e.getMessage());
                try {
                    selectedMenuImage.setImage(new Image(getClass().getResourceAsStream("/resources/no_image.png")));
                } catch (Exception ex) {
                }
            }
        } else {
            selectedMenuName.setText("");
            selectedMenuPrice.setText("");
            selectedMenuCategory.setText("");
            selectedMenuDescription.setText("");
            if (selectedMenuStatus != null) {
                selectedMenuStatus.setText("");
            }
            try {
                selectedMenuImage.setImage(new Image(getClass().getResourceAsStream("/resources/no_image.png")));
            } catch (Exception e) {
            }
        }
    }

    @FXML
    private void handleAddMenu() {
        MenuDialog dialog = new MenuDialog();
        Optional<Menu> result = dialog.showAndWait();

        result.ifPresent(menu -> {
            adminService.registerMenu(
                    menu.name(),
                    menu.price(),
                    menu.categoryId(),
                    menu.description(),
                    menu.imagePath(),
                    menu.soldOut()
            );
            refreshMenuList();

            CategoryItem selectedCategory = categoryFilter.getSelectionModel().getSelectedItem();
            if (selectedCategory != null) {
                updateMenuItemsView(selectedCategory.getId());
            }
        });
    }

    @FXML
    private void handleDeleteMenu() {
        Menu selectedMenu = menuTable.getSelectionModel().getSelectedItem();
        if (selectedMenu != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("메뉴 삭제");
            confirmDialog.setHeaderText("메뉴 삭제 확인");
            confirmDialog.setContentText("정말로 \"" + selectedMenu.name() + "\" 메뉴를 삭제하시겠습니까?");

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                adminService.deleteMenu(selectedMenu.menuId());
                refreshMenuList();

                CategoryItem selectedCategory = categoryFilter.getSelectionModel().getSelectedItem();
                if (selectedCategory != null) {
                    updateMenuItemsView(selectedCategory.getId());
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("선택 오류");
            alert.setHeaderText(null);
            alert.setContentText("삭제할 메뉴를 선택해주세요.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleEditMenu() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("기능 안내");
        alert.setHeaderText(null);
        alert.setContentText("메뉴 수정 기능은 아직 구현되지 않았습니다.");
        alert.showAndWait();
    }

    @FXML
    private void handleToggleSoldOut() {
        Menu selectedMenu = menuTable.getSelectionModel().getSelectedItem();
        if (selectedMenu != null) {
            adminService.toggleSoldOut(selectedMenu.menuId());
            refreshMenuList();

            CategoryItem selectedCategory = categoryFilter.getSelectionModel().getSelectedItem();
            if (selectedCategory != null) {
                updateMenuItemsView(selectedCategory.getId());
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("선택 오류");
            alert.setHeaderText(null);
            alert.setContentText("상태를 변경할 메뉴를 선택해주세요.");
            alert.showAndWait();
        }
    }
}
