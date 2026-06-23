package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.example.model.*;
import org.example.client.ClientService;
import org.example.client.SessionManager;
import org.example.client.NetworkClient;
import org.example.util.ExportUtil;
import org.example.util.ImportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private StackPane contentPane;

    @FXML
    private Label userLabel;

    @FXML
    private Label pageTitle;

    @FXML
    private Label sidebarUsername;

    @FXML
    private Label sidebarRole;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnProducts;

    @FXML
    private Button btnCustomers;

    @FXML
    private Button btnOrders;

    @FXML
    private Button btnCategories;

    @FXML
    private Button btnUsers;

    @FXML
    private Button btnReports;

    @FXML
    private Button btnSell;

    private ClientService clientService = new ClientService();
    private SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        User user = sessionManager.getCurrentUser();
        if (user != null) {
            userLabel.setText(user.getUsername() + " (" + user.getRole() + ")");
            sidebarUsername.setText(user.getUsername());
            sidebarRole.setText("Vai trò: " + user.getRole());
            
            // Restrict operations based on role
            if (!"ADMIN".equals(user.getRole())) {
                btnDashboard.setVisible(false);
                btnDashboard.setManaged(false);
                btnCategories.setVisible(false);
                btnCategories.setManaged(false);
                btnUsers.setVisible(false);
                btnUsers.setManaged(false);
                btnReports.setVisible(false);
                btnReports.setManaged(false);
                
                btnSell.setVisible(true);
                btnSell.setManaged(true);
                btnCustomers.setVisible(true);
                btnCustomers.setManaged(true);
                btnProducts.setVisible(true);
                btnProducts.setManaged(true);
                btnOrders.setVisible(true);
                btnOrders.setManaged(true);
                
                // Show sales screen on startup
                showOrderForm();
            } else {
                btnDashboard.setVisible(true);
                btnDashboard.setManaged(true);
                btnCategories.setVisible(true);
                btnCategories.setManaged(true);
                btnUsers.setVisible(true);
                btnUsers.setManaged(true);
                btnReports.setVisible(true);
                btnReports.setManaged(true);
                
                btnSell.setVisible(false);
                btnSell.setManaged(false);
                
                // Show dashboard on startup
                showDashboard();
            }
        }
    }

    /**
     * Set active styling on sidebar buttons and update top title bar
     */
    private void setActiveNavigation(Button activeBtn, String title) {
        pageTitle.setText(title);
        
        // Remove active class from all
        btnDashboard.getStyleClass().remove("active");
        if (btnSell != null) {
            btnSell.getStyleClass().remove("active");
        }
        btnProducts.getStyleClass().remove("active");
        btnCustomers.getStyleClass().remove("active");
        btnOrders.getStyleClass().remove("active");
        btnCategories.getStyleClass().remove("active");
        btnUsers.getStyleClass().remove("active");
        btnReports.getStyleClass().remove("active");
        
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("active");
        }
    }

    // ========== DASHBOARD METHODS ==========

    @FXML
    public void showDashboard() {
        setActiveNavigation(btnDashboard, "Dashboard - Tổng Quan");
        
        try {
            Map<String, Object> stats = clientService.getDashboardStats();
            if (stats == null) {
                stats = new HashMap<>();
                stats.put("totalProducts", 0);
                stats.put("totalCustomers", 0);
                stats.put("totalOrders", 0);
                stats.put("totalRevenue", 0.0);
            }
            
            VBox mainLayout = new VBox(25);
            mainLayout.setStyle("-fx-background-color: transparent;");
            
            // Metric Cards (HBox)
            HBox cardsBox = new HBox(20);
            cardsBox.setAlignment(javafx.geometry.Pos.CENTER);
            
            int totalProducts = ((Number) stats.getOrDefault("totalProducts", 0)).intValue();
            int totalCustomers = ((Number) stats.getOrDefault("totalCustomers", 0)).intValue();
            int totalOrders = ((Number) stats.getOrDefault("totalOrders", 0)).intValue();
            double totalRevenue = ((Number) stats.getOrDefault("totalRevenue", 0.0)).doubleValue();
            
            // Standard User dashboard doesn't display full overall revenues, only simple counts
            boolean isAdmin = sessionManager.isAdmin();
            String revString = isAdmin ? String.format("%,.0f đ", totalRevenue) : "Mật Khẩu / Hồ Sơ";
            String revTitle = isAdmin ? "Doanh Thu Shop" : "Quyền: Nhân Viên";
            
            cardsBox.getChildren().addAll(
                createStatCard("Tổng Sản Phẩm", String.valueOf(totalProducts), "📦", "icon-wrapper-1"),
                createStatCard("Tổng Khách Hàng", String.valueOf(totalCustomers), "👥", "icon-wrapper-2"),
                createStatCard("Tổng Hóa Đơn", String.valueOf(totalOrders), "📋", "icon-wrapper-3"),
                createStatCard(revTitle, revString, "💰", "icon-wrapper-4")
            );
            
            // Charts Row (Only display charts for Admin to protect revenue details)
            if (isAdmin) {
                HBox chartsBox = new HBox(20);
                chartsBox.setPrefHeight(380);
                chartsBox.setAlignment(javafx.geometry.Pos.CENTER);
                
                // 1. Pie Chart - Products by Category
                PieChart pieChart = new PieChart();
                pieChart.setTitle("Tỷ lệ món theo Danh mục");
                pieChart.setLegendSide(javafx.geometry.Side.BOTTOM);
                
                boolean hasPieData = false;
                List<Product> products = clientService.getAllProducts();
                if (products != null && !products.isEmpty()) {
                    Map<String, Long> categoryCounts = products.stream()
                        .filter(p -> p.getCategoryName() != null)
                        .collect(Collectors.groupingBy(Product::getCategoryName, Collectors.counting()));
                    
                    if (!categoryCounts.isEmpty()) {
                        hasPieData = true;
                        categoryCounts.forEach((catName, count) -> {
                            pieChart.getData().add(new PieChart.Data(catName + " (" + count + ")", count));
                        });
                    }
                }
                
                VBox pieContainer;
                if (hasPieData) {
                    pieContainer = new VBox(10, pieChart);
                } else {
                    Label noPieLabel = new Label("Chưa có dữ liệu thống kê món ăn");
                    noPieLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                    pieContainer = new VBox(10, noPieLabel);
                    pieContainer.setAlignment(javafx.geometry.Pos.CENTER);
                }
                pieContainer.getStyleClass().add("chart-container-card");
                pieContainer.setPrefWidth(600);
                pieContainer.setMaxWidth(600);
                
                chartsBox.getChildren().addAll(pieContainer);
                mainLayout.getChildren().addAll(cardsBox, chartsBox);
            } else {
                // For non-admin, display a friendly greeting panel instead of charts
                VBox welcomeBox = new VBox(15);
                welcomeBox.getStyleClass().add("form-container");
                welcomeBox.setStyle("-fx-padding: 30; -fx-alignment: center;");
                Label lblWelcome = new Label("Chào mừng nhân viên, " + sessionManager.getCurrentUser().getUsername() + "!");
                lblWelcome.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #0E98A0;");
                Label lblInfo = new Label("Sử dụng bảng điều khiển bên trái để bắt đầu bán hàng tại quầy, tra cứu sản phẩm hoặc xem danh sách hóa đơn.");
                lblInfo.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 14;");
                welcomeBox.getChildren().addAll(lblWelcome, lblInfo);
                mainLayout.getChildren().addAll(cardsBox, welcomeBox);
            }
            
            contentPane.getChildren().clear();
            contentPane.getChildren().add(new ScrollPane(mainLayout) {
                {
                    setFitToWidth(true);
                    setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
                }
            });
            
        } catch (Exception e) {
            logger.error("Error loading dashboard", e);
            showError("Lỗi", "Không thể tải dữ liệu Dashboard: " + e.getMessage());
        }
    }
    
    private VBox createStatCard(String title, String value, String icon, String iconClass) {
        VBox card = new VBox(10);
        card.getStyleClass().add("stat-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        
        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-card-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().addAll("stat-card-icon", iconClass);
        
        header.getChildren().addAll(titleLabel, spacer, iconLabel);
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-card-value");
        
        card.getChildren().addAll(header, valueLabel);
        return card;
    }

    // ========== PRODUCT METHODS ==========

    @FXML
    public void showProductList() {
        setActiveNavigation(btnProducts, "Quản Lý Sản Phẩm");
        try {
            List<Product> products = clientService.getAllProducts();
            List<Category> categories = clientService.getAllCategories();
            if (products != null) {
                showProductTable(products, categories);
            } else {
                showError("Lỗi", "Không thể tải danh sách sản phẩm");
            }
        } catch (Exception e) {
            logger.error("Error showing product list", e);
            showError("Lỗi", "Lỗi: " + e.getMessage());
        }
    }

    private void showProductTable(List<Product> products, List<Category> categories) {
        VBox vbox = new VBox(20);
        vbox.setStyle("-fx-background-color: transparent;");

        // Action Panel
        HBox topPanel = new HBox(15);
        topPanel.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Tìm theo tên hoặc mô tả...");
        searchField.setPrefWidth(280);

        ComboBox<Category> categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("Chọn danh mục");
        ObservableList<Category> catFilterList = FXCollections.observableArrayList();
        Category allCat = new Category(-1, "Tất cả danh mục", "");
        catFilterList.add(allCat);
        if (categories != null) {
            catFilterList.addAll(categories);
        }
        categoryFilter.setItems(catFilterList);
        categoryFilter.setValue(allCat);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕ Thêm sản phẩm");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> showProductForm(null));

        Button editBtn = new Button("📝 Sửa");
        editBtn.getStyleClass().addAll("btn", "btn-secondary");

        Button deleteBtn = new Button("🗑️ Xoá");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");

        // Hide Admin Actions for Normal Users
        boolean isAdmin = sessionManager.isAdmin();
        if (!isAdmin) {
            addBtn.setVisible(false);
            addBtn.setManaged(false);
            editBtn.setVisible(false);
            editBtn.setManaged(false);
            deleteBtn.setVisible(false);
            deleteBtn.setManaged(false);
        }

        topPanel.getChildren().addAll(searchField, categoryFilter, spacer, addBtn, editBtn, deleteBtn);

        // Table View
        TableView<Product> table = new TableView<>();
        
        // Thumbnail Image Column
        TableColumn<Product, String> imgCol = new TableColumn<>("Hình ảnh");
        imgCol.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
        imgCol.setPrefWidth(80);
        imgCol.setCellFactory(col -> new TableCell<Product, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    if (item == null || item.trim().isEmpty()) {
                        setGraphic(new Label("☕"));
                    } else {
                        try {
                            imageView.setImage(new javafx.scene.image.Image(item, true));
                            setGraphic(imageView);
                        } catch (Exception ex) {
                            setGraphic(new Label("☕"));
                        }
                    }
                }
            }
        });

        TableColumn<Product, Void> idCol = new TableColumn<>("STT");
        idCol.setPrefWidth(50);
        idCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        TableColumn<Product, String> nameCol = new TableColumn<>("Tên sản phẩm");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<Product, Double> priceCol = new TableColumn<>("Giá bán (đ)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(120);
        priceCol.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.0f đ", item));
            }
        });

        TableColumn<Product, String> categoryCol = new TableColumn<>("Danh mục");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryCol.setPrefWidth(150);

        TableColumn<Product, String> descCol = new TableColumn<>("Mô tả");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(200);

        table.getColumns().addAll(imgCol, idCol, nameCol, priceCol, categoryCol, descCol);

        ObservableList<Product> tableData = FXCollections.observableArrayList(products);
        FilteredList<Product> filteredData = new FilteredList<>(tableData, p -> true);

        // Filter Logic
        Runnable runFilter = () -> {
            String text = searchField.getText().trim().toLowerCase();
            Category selectedCat = categoryFilter.getValue();
            
            filteredData.setPredicate(p -> {
                if (selectedCat != null && selectedCat.getId() != -1) {
                    if (p.getCategoryId() != selectedCat.getId()) {
                        return false;
                    }
                }
                if (text.isEmpty()) {
                    return true;
                }
                if (p.getName().toLowerCase().contains(text)) return true;
                if (p.getDescription() != null && p.getDescription().toLowerCase().contains(text)) return true;
                return false;
            });
        };

        searchField.textProperty().addListener((obs, oldVal, newVal) -> runFilter.run());
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> runFilter.run());

        table.setItems(filteredData);
        VBox.setVgrow(table, Priority.ALWAYS);

        editBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showProductForm(selected);
            } else {
                showWarning("Cảnh báo", "Vui lòng chọn sản phẩm cần sửa!");
            }
        });

        deleteBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (confirmAction("Xác nhận xoá", "Bạn có chắc chắn muốn xoá sản phẩm '" + selected.getName() + "'?")) {
                    if (clientService.deleteProduct(selected.getId())) {
                        showSuccess("Thành công", "Đã xoá sản phẩm thành công!");
                        showProductList();
                    } else {
                        showError("Lỗi", "Không thể xoá sản phẩm này.");
                    }
                }
            } else {
                showWarning("Cảnh báo", "Vui lòng chọn sản phẩm cần xoá!");
            }
        });

        vbox.getChildren().addAll(topPanel, table);
        contentPane.getChildren().clear();
        contentPane.getChildren().add(vbox);
    }

    @FXML
    public void showProductForm() {
        showProductForm(null);
    }

    private void showProductForm(Product product) {
        if (!sessionManager.isAdmin()) {
            showWarning("Cảnh báo", "Chỉ quản trị viên mới có quyền thêm/sửa sản phẩm!");
            return;
        }

        VBox vbox = new VBox(15);
        vbox.getStyleClass().add("form-container");
        vbox.setMaxWidth(600);

        Label title = new Label(product == null ? "Thêm Sản Phẩm Mới" : "Chỉnh Sửa Sản Phẩm");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #0E98A0;");

        TextField nameField = new TextField();
        nameField.setPromptText("Tên sản phẩm");
        if (product != null) nameField.setText(product.getName());

        TextField priceField = new TextField();
        priceField.setPromptText("Đơn giá (đ)");
        if (product != null) priceField.setText(String.valueOf(product.getPrice()));


        TextField imagePathField = new TextField();
        imagePathField.setPromptText("Đường dẫn hình ảnh (URL)");
        if (product != null) imagePathField.setText(product.getImagePath());

        ImageView imgPreview = new ImageView();
        imgPreview.setFitWidth(150);
        imgPreview.setFitHeight(150);
        imgPreview.setPreserveRatio(true);
        imgPreview.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-background-radius: 6; -fx-border-radius: 6;");

        if (product != null && product.getImagePath() != null && !product.getImagePath().trim().isEmpty()) {
            try {
                imgPreview.setImage(new javafx.scene.image.Image(product.getImagePath(), true));
            } catch (Exception ex) {
                // Ignore
            }
        }

        imagePathField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                try {
                    imgPreview.setImage(new javafx.scene.image.Image(newVal.trim(), true));
                } catch (Exception ex) {
                    imgPreview.setImage(null);
                }
            } else {
                imgPreview.setImage(null);
            }
        });

        TextArea descField = new TextArea();
        descField.setPromptText("Mô tả sản phẩm...");
        descField.setPrefRowCount(3);
        if (product != null) descField.setText(product.getDescription());

        List<Category> categories = clientService.getAllCategories();
        ComboBox<Category> categoryBox = new ComboBox<>();
        if (categories != null) {
            categoryBox.setItems(FXCollections.observableArrayList(categories));
            if (product != null) {
                for (Category cat : categories) {
                    if (cat.getId() == product.getCategoryId()) {
                        categoryBox.setValue(cat);
                        break;
                    }
                }
            }
        }

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Hủy bỏ");
        cancelBtn.getStyleClass().addAll("btn", "btn-secondary");
        cancelBtn.setOnAction(e -> showProductList());

        Button saveBtn = new Button("Lưu lại");
        saveBtn.getStyleClass().addAll("btn", "btn-primary");
        saveBtn.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int stock = 0;
                Category selectedCat = categoryBox.getValue();

                if (name.isEmpty() || selectedCat == null) {
                    showWarning("Nhập thiếu", "Vui lòng nhập tên và chọn danh mục sản phẩm!");
                    return;
                }

                Product p = new Product(
                    product != null ? product.getId() : 0,
                    name,
                    price,
                    selectedCat.getId(),
                    descField.getText().trim(),
                    stock,
                    imagePathField.getText().trim()
                );

                boolean success = product != null ? clientService.updateProduct(p) : clientService.createProduct(p);
                if (success) {
                    showSuccess("Thành công", product != null ? "Cập nhật sản phẩm thành công!" : "Thêm mới sản phẩm thành công!");
                    showProductList();
                } else {
                    showError("Lỗi", "Không thể lưu sản phẩm. Vui lòng kiểm tra lại dữ liệu.");
                }
            } catch (NumberFormatException ex) {
                showWarning("Lỗi nhập liệu", "Giá bán phải là số hợp lệ!");
            }
        });

        btnBox.getChildren().addAll(cancelBtn, saveBtn);

        vbox.getChildren().addAll(
            title,
            new Label("Tên sản phẩm:"), nameField,
            new Label("Giá bán (đ):"), priceField,
            new Label("Danh mục:"), categoryBox,
            new Label("Đường dẫn hình ảnh (URL):"), imagePathField,
            new Label("Xem trước hình ảnh:"), imgPreview,
            new Label("Mô tả:"), descField,
            btnBox
        );

        contentPane.getChildren().clear();
        contentPane.getChildren().add(new ScrollPane(vbox) {
            {
                setFitToWidth(true);
                setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
            }
        });
    }

    // ========== CUSTOMER METHODS ==========

    @FXML
    public void showCustomerList() {
        setActiveNavigation(btnCustomers, "Quản Lý Khách Hàng");
        try {
            List<Customer> customers = clientService.getAllCustomers();
            if (customers != null) {
                showCustomerTable(customers);
            } else {
                showError("Lỗi", "Không thể tải danh sách khách hàng");
            }
        } catch (Exception e) {
            logger.error("Error showing customer list", e);
            showError("Lỗi", "Lỗi: " + e.getMessage());
        }
    }

    private void showCustomerTable(List<Customer> customers) {
        VBox vbox = new VBox(20);
        vbox.setStyle("-fx-background-color: transparent;");

        // Action Panel
        HBox topPanel = new HBox(15);
        topPanel.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Tìm theo tên, số điện thoại...");
        searchField.setPrefWidth(300);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕ Thêm khách hàng");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> showCustomerForm(null));

        Button editBtn = new Button("📝 Sửa");
        editBtn.getStyleClass().addAll("btn", "btn-secondary");

        Button deleteBtn = new Button("🗑️ Xoá");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");

        // Hide delete action for non-admin users
        boolean isAdmin = sessionManager.isAdmin();
        if (!isAdmin) {
            deleteBtn.setVisible(false);
            deleteBtn.setManaged(false);
        }

        topPanel.getChildren().addAll(searchField, spacer, addBtn, editBtn, deleteBtn);

        // Table View
        TableView<Customer> table = new TableView<>();
        TableColumn<Customer, Void> idCol = new TableColumn<>("STT");
        idCol.setPrefWidth(50);
        idCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        TableColumn<Customer, String> nameCol = new TableColumn<>("Tên khách hàng");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<Customer, String> phoneCol = new TableColumn<>("Số điện thoại");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, nameCol, phoneCol);

        ObservableList<Customer> tableData = FXCollections.observableArrayList(customers);
        FilteredList<Customer> filteredData = new FilteredList<>(tableData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(c -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase();
                if (c.getName().toLowerCase().contains(filter)) return true;
                if (c.getPhone() != null && c.getPhone().contains(filter)) return true;
                return false;
            });
        });

        table.setItems(filteredData);
        VBox.setVgrow(table, Priority.ALWAYS);

        editBtn.setOnAction(e -> {
            Customer selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showCustomerForm(selected);
            } else {
                showWarning("Cảnh báo", "Vui lòng chọn khách hàng cần sửa!");
            }
        });

        deleteBtn.setOnAction(e -> {
            Customer selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (confirmAction("Xác nhận xoá", "Bạn có chắc chắn muốn xoá khách hàng '" + selected.getName() + "'?")) {
                    if (clientService.deleteCustomer(selected.getId())) {
                        showSuccess("Thành công", "Đã xoá khách hàng thành công!");
                        showCustomerList();
                    } else {
                        showError("Lỗi", "Không thể xoá khách hàng này.");
                    }
                }
            } else {
                showWarning("Cảnh báo", "Vui lòng chọn khách hàng cần xoá!");
            }
        });

        vbox.getChildren().addAll(topPanel, table);
        contentPane.getChildren().clear();
        contentPane.getChildren().add(vbox);
    }

    private void showCustomerForm(Customer customer) {
        VBox vbox = new VBox(15);
        vbox.getStyleClass().add("form-container");
        vbox.setMaxWidth(600);

        Label title = new Label(customer == null ? "Thêm Khách Hàng Mới" : "Chỉnh Sửa Khách Hàng");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #0E98A0;");

        TextField nameField = new TextField();
        nameField.setPromptText("Tên khách hàng");
        if (customer != null) nameField.setText(customer.getName());

        TextField phoneField = new TextField();
        phoneField.setPromptText("Số điện thoại");
        if (customer != null) phoneField.setText(customer.getPhone());

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Hủy bỏ");
        cancelBtn.getStyleClass().addAll("btn", "btn-secondary");
        cancelBtn.setOnAction(e -> showCustomerList());

        Button saveBtn = new Button("Lưu lại");
        saveBtn.getStyleClass().addAll("btn", "btn-primary");
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                showWarning("Nhập thiếu", "Vui lòng nhập tên và số điện thoại khách hàng!");
                return;
            }

            Customer c = new Customer(
                customer != null ? customer.getId() : 0,
                name,
                phone
            );

            boolean success = customer != null ? clientService.updateCustomer(c) : clientService.createCustomer(c);
            if (success) {
                showSuccess("Thành công", customer != null ? "Cập nhật thông tin khách hàng thành công!" : "Thêm mới khách hàng thành công!");
                showCustomerList();
            } else {
                showError("Lỗi", "Không thể lưu thông tin khách hàng.");
            }
        });

        btnBox.getChildren().addAll(cancelBtn, saveBtn);

        vbox.getChildren().addAll(
            title,
            new Label("Tên khách hàng:"), nameField,
            new Label("Số điện thoại:"), phoneField,
            btnBox
        );

        contentPane.getChildren().clear();
        contentPane.getChildren().add(vbox);
    }

    // ========== ORDER METHODS ==========

    @FXML
    public void showOrderList() {
        setActiveNavigation(btnOrders, "Quản Lý Hóa Đơn");
        try {
            List<Order> orders = clientService.getAllOrders();
            if (orders != null) {
                showOrderTable(orders);
            } else {
                showError("Lỗi", "Không thể tải danh sách hóa đơn");
            }
        } catch (Exception e) {
            logger.error("Error showing order list", e);
            showError("Lỗi", "Lỗi: " + e.getMessage());
        }
    }

    private void showOrderTable(List<Order> orders) {
        VBox vbox = new VBox(20);
        vbox.setStyle("-fx-background-color: transparent;");

        // Action Panel
        HBox topPanel = new HBox(15);
        topPanel.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Tìm theo tên khách hàng...");
        searchField.setPrefWidth(300);

        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList(
            "Tất cả trạng thái", "Đã thanh toán", "Đã hủy"
        ));
        statusFilter.setValue("Tất cả trạng thái");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("🛒 Lập hóa đơn mới");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> showOrderForm(null));

        Button viewDetailBtn = new Button("📄 Xem & In Hóa Đơn");
        viewDetailBtn.getStyleClass().addAll("btn", "btn-info");

        Button updateStatusBtn = new Button("🔄 Trạng thái");
        updateStatusBtn.getStyleClass().addAll("btn", "btn-secondary");

        topPanel.getChildren().addAll(searchField, statusFilter, spacer, addBtn, viewDetailBtn, updateStatusBtn);

        // Table
        TableView<Order> table = new TableView<>();
        TableColumn<Order, Void> idCol = new TableColumn<>("STT");
        idCol.setPrefWidth(50);
        idCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        TableColumn<Order, String> customerCol = new TableColumn<>("Khách hàng");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerCol.setPrefWidth(220);

        TableColumn<Order, Double> amountCol = new TableColumn<>("Tổng tiền");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        amountCol.setPrefWidth(150);
        amountCol.setCellFactory(col -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.0f đ", item));
            }
        });

        TableColumn<Order, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(160);
        statusCol.setCellFactory(column -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label();
                    badge.getStyleClass().add("badge");
                    
                    if ("COMPLETED".equalsIgnoreCase(item) || "HOÀN THÀNH".equalsIgnoreCase(item) || "ĐÃ THANH TOÁN".equalsIgnoreCase(item) || "PENDING".equalsIgnoreCase(item) || "PROCESSING".equalsIgnoreCase(item)) {
                        badge.setText("Đã thanh toán");
                        badge.getStyleClass().add("badge-completed");
                    } else if ("CANCELLED".equalsIgnoreCase(item) || "ĐÃ HỦY".equalsIgnoreCase(item)) {
                        badge.setText("Đã hủy");
                        badge.getStyleClass().add("badge-cancelled");
                    } else {
                        badge.setText(item);
                    }
                    setGraphic(badge);
                }
            }
        });

        TableColumn<Order, Long> dateCol = new TableColumn<>("Ngày lập");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        dateCol.setPrefWidth(180);
        dateCol.setCellFactory(col -> new TableCell<Order, Long>() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : sdf.format(new Date(item)));
            }
        });

        table.getColumns().addAll(idCol, customerCol, amountCol, statusCol, dateCol);

        ObservableList<Order> tableData = FXCollections.observableArrayList(orders);
        FilteredList<Order> filteredData = new FilteredList<>(tableData, p -> true);

        // Multi Filter Listener
        Runnable runFilter = () -> {
            String text = searchField.getText().trim().toLowerCase();
            String status = statusFilter.getValue();
            
            filteredData.setPredicate(order -> {
                if (!"Tất cả trạng thái".equals(status)) {
                    if ("Đã thanh toán".equals(status)) {
                        String s = order.getStatus();
                        if (!("COMPLETED".equalsIgnoreCase(s) || "PENDING".equalsIgnoreCase(s) || "PROCESSING".equalsIgnoreCase(s))) {
                            return false;
                        }
                    } else if ("Đã hủy".equals(status)) {
                        if (!"CANCELLED".equalsIgnoreCase(order.getStatus())) {
                            return false;
                        }
                    }
                }
                if (text.isEmpty()) {
                    return true;
                }
                if (order.getCustomerName() != null && order.getCustomerName().toLowerCase().contains(text)) {
                    return true;
                }
                return false;
            });
        };

        searchField.textProperty().addListener((obs, oldVal, newVal) -> runFilter.run());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> runFilter.run());

        table.setItems(filteredData);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Add double-click to view receipt details
        table.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Order rowData = row.getItem();
                    showOrderDetailDialog(rowData);
                }
            });
            return row;
        });

        viewDetailBtn.setOnAction(e -> {
            Order selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showOrderDetailDialog(selected);
            } else {
                showWarning("Cảnh báo", "Vui lòng chọn hóa đơn cần xem chi tiết & in!");
            }
        });

        updateStatusBtn.setOnAction(e -> {
            Order selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showUpdateStatusDialog(selected);
            } else {
                showWarning("Cảnh báo", "Vui lòng chọn hóa đơn cần cập nhật trạng thái!");
            }
        });

        vbox.getChildren().addAll(topPanel, table);
        contentPane.getChildren().clear();
        contentPane.getChildren().add(vbox);
    }

    private void showOrderDetailDialog(Order order) {
        if (order == null) return;
        
        List<OrderDetail> details = clientService.getOrderDetails(order.getId());
        if (details == null) {
            showError("Lỗi", "Không thể tải chi tiết hóa đơn!");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết Hóa đơn #" + order.getId());
        
        ButtonType printButtonType = new ButtonType("In Hóa Đơn (Xuất File)", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(printButtonType, ButtonType.CLOSE);

        VBox box = new VBox(15);
        box.setStyle("-fx-padding: 25; -fx-min-width: 480; -fx-background-color: #FFFFFF;");
        
        Label storeHeader = new Label("🏪 KT DRINK & DESSERT SHOP");
        storeHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0E98A0; -fx-alignment: center;");
        storeHeader.setMaxWidth(Double.MAX_VALUE);
        
        Label receiptTitle = new Label("HÓA ĐƠN THANH TOÁN");
        receiptTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1F2937; -fx-alignment: center;");
        receiptTitle.setMaxWidth(Double.MAX_VALUE);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String formattedDate = sdf.format(new Date(order.getOrderDate()));

        javafx.scene.layout.GridPane infoGrid = new javafx.scene.layout.GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(8);
        infoGrid.addRow(0, new Label("Mã hóa đơn:"), new Label(String.valueOf(order.getId())));
        infoGrid.addRow(1, new Label("Khách hàng:"), new Label(order.getCustomerName() != null ? order.getCustomerName() : "Khách vãng lai"));
        infoGrid.addRow(2, new Label("Thời gian:"), new Label(formattedDate));
        infoGrid.addRow(3, new Label("Trạng thái:"), new Label(order.getStatus()));

        Separator sep1 = new Separator();

        VBox itemsBox = new VBox(8);
        itemsBox.setStyle("-fx-padding: 5 0;");
        
        HBox headerRow = new HBox();
        Label lblNameH = new Label("Sản phẩm");
        lblNameH.setStyle("-fx-font-weight: bold;");
        Label lblQtyH = new Label("SL");
        lblQtyH.setStyle("-fx-font-weight: bold;");
        Label lblPriceH = new Label("Đ.Giá");
        lblPriceH.setStyle("-fx-font-weight: bold;");
        Label lblTotalH = new Label("T.Tiền");
        lblTotalH.setStyle("-fx-font-weight: bold;");
        
        lblNameH.setPrefWidth(200);
        lblQtyH.setPrefWidth(40);
        lblPriceH.setPrefWidth(100);
        lblTotalH.setPrefWidth(100);
        
        headerRow.getChildren().addAll(lblNameH, lblQtyH, lblPriceH, lblTotalH);
        itemsBox.getChildren().add(headerRow);
        
        for (OrderDetail d : details) {
            HBox row = new HBox();
            Label lblName = new Label(d.getProductName() != null ? d.getProductName() : ("SP #" + d.getProductId()));
            Label lblQty = new Label(String.valueOf(d.getQuantity()));
            Label lblPrice = new Label(String.format("%,.0fđ", d.getUnitPrice()));
            Label lblTotal = new Label(String.format("%,.0fđ", d.getTotalPrice()));
            
            lblName.setPrefWidth(200);
            lblQty.setPrefWidth(40);
            lblPrice.setPrefWidth(100);
            lblTotal.setPrefWidth(100);
            
            row.getChildren().addAll(lblName, lblQty, lblPrice, lblTotal);
            itemsBox.getChildren().add(row);
        }

        Separator sep2 = new Separator();

        HBox totalRow = new HBox();
        Label lblTotalTitle = new Label("TỔNG TIỀN THANH TOÁN:");
        lblTotalTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label lblTotalVal = new Label(String.format("%,.0f đ", order.getTotalAmount()));
        lblTotalVal.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #DC2626;");
        totalRow.getChildren().addAll(lblTotalTitle, spacer, lblTotalVal);

        box.getChildren().addAll(storeHeader, receiptTitle, new Separator(), infoGrid, sep1, itemsBox, sep2, totalRow);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == printButtonType) {
                javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                fileChooser.setTitle("In hóa đơn - Lưu tệp tin");
                fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));
                fileChooser.setInitialFileName("hoadon_" + order.getId() + ".txt");
                
                File file = fileChooser.showSaveDialog(dialog.getOwner());
                if (file != null) {
                    try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file))) {
                        writer.println("==========================================");
                        writer.println("        🏪 KT DRINK & DESSERT SHOP        ");
                        writer.println("            HÓA ĐƠN THANH TOÁN            ");
                        writer.println("==========================================");
                        writer.println(" Mã hóa đơn: " + order.getId());
                        writer.println(" Khách hàng: " + (order.getCustomerName() != null ? order.getCustomerName() : "Khách vãng lai"));
                        writer.println(" Thời gian:  " + formattedDate);
                        writer.println(" Trạng thái: " + order.getStatus());
                        writer.println("------------------------------------------");
                        writer.printf(" %-18s %3s %9s %10s%n", "Sản phẩm", "SL", "Đ.Giá", "T.Tiền");
                        writer.println("------------------------------------------");
                        for (OrderDetail d : details) {
                            String nameStr = d.getProductName() != null ? d.getProductName() : "SP #" + d.getProductId();
                            if (nameStr.length() > 18) {
                                nameStr = nameStr.substring(0, 15) + "...";
                            }
                            writer.printf(" %-18s %3d %,9.0fđ %,10.0fđ%n", 
                                nameStr, d.getQuantity(), d.getUnitPrice(), d.getTotalPrice());
                        }
                        writer.println("------------------------------------------");
                        writer.printf(" TỔNG THANH TOÁN: %,24.0f đ%n", order.getTotalAmount());
                        writer.println("==========================================");
                        writer.println("      CẢM ƠN QUÝ KHÁCH & HẸN GẶP LẠI!     ");
                        writer.flush();
                        
                        javafx.application.Platform.runLater(() -> {
                            showSuccess("Thành công", "Đã in/xuất hóa đơn ra file:\n" + file.getAbsolutePath());
                        });
                    } catch (java.io.IOException ex) {
                        logger.error("Error printing invoice to file", ex);
                        javafx.application.Platform.runLater(() -> {
                            showError("Lỗi", "Không thể in hóa đơn: " + ex.getMessage());
                        });
                    }
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showUpdateStatusDialog(Order order) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật trạng thái");
        dialog.setHeaderText("Thay đổi trạng thái hóa đơn #" + order.getId());
        
        ButtonType saveButtonType = new ButtonType("Cập nhật", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        VBox box = new VBox(10);
        box.setStyle("-fx-padding: 20;");
        Label lbl = new Label("Chọn trạng thái mới:");
        ComboBox<String> selectStatus = new ComboBox<>(FXCollections.observableArrayList(
            "Đã thanh toán", "Đã hủy"
        ));
        
        String currentVal = "Đã thanh toán";
        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            currentVal = "Đã hủy";
        }
        selectStatus.setValue(currentVal);
        
        box.getChildren().addAll(lbl, selectStatus);
        dialog.getDialogPane().setContent(box);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String val = selectStatus.getValue();
                if ("Đã thanh toán".equals(val)) return "COMPLETED";
                if ("Đã hủy".equals(val)) return "CANCELLED";
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(status -> {
            order.setStatus(status);
            if (clientService.updateOrder(order)) {
                showSuccess("Thành công", "Đã cập nhật trạng thái hóa đơn!");
                showOrderList();
            } else {
                showError("Lỗi", "Không thể cập nhật trạng thái hóa đơn.");
            }
        });
    }

    @FXML
    public void showOrderForm() {
        showOrderForm(null);
    }

    private void showOrderForm(Order order) {
        setActiveNavigation(sessionManager.isAdmin() ? null : btnSell, "Bán Hàng Tại Quầy");

        VBox mainBox = new VBox(20);
        mainBox.setStyle("-fx-background-color: transparent;");

        Label formTitle = new Label("LẬP HÓA ĐƠN MỚI (BÁN HÀNG TẠI QUẦY)");
        formTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #0E98A0;");

        HBox splitPane = new HBox(20);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // Left Side: Select customer & Product Picker
        VBox leftPane = new VBox(15);
        leftPane.getStyleClass().add("form-container");
        leftPane.setPrefWidth(450);

        List<Customer> customers = clientService.getAllCustomers();
        ComboBox<Customer> customerBox = new ComboBox<>();
        customerBox.setPrefWidth(220);
        if (customers != null) {
            customerBox.setItems(FXCollections.observableArrayList(customers));
            customerBox.setPromptText("Chọn khách hàng");
            customerBox.setCellFactory(lv -> new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName() + " - " + item.getPhone());
                }
            });
            customerBox.setButtonCell(new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName() + " - " + item.getPhone());
                }
            });
            
            // Auto select Nguyễn Văn A (default / walkthrough customer) if exists
            for (Customer c : customers) {
                if ("Nguyễn Văn A".equals(c.getName())) {
                    customerBox.setValue(c);
                    break;
                }
            }
        }

        HBox customerRow = new HBox(10);
        customerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Button quickAddCustBtn = new Button("➕ Thêm nhanh");
        quickAddCustBtn.getStyleClass().addAll("btn", "btn-secondary");
        quickAddCustBtn.setOnAction(e -> showQuickAddCustomerDialog(customerBox));
        customerRow.getChildren().addAll(customerBox, quickAddCustBtn);
        
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("Đã thanh toán", "Đã hủy"));
        statusBox.setValue("Đã thanh toán");

        VBox addProdBox = new VBox(10);
        addProdBox.setStyle("-fx-background-color: #F8FAFB; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 8;");
        
        Label pickerTitle = new Label("Thêm món vào hóa đơn");
        pickerTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0E98A0; -fx-font-size: 13;");

        List<Product> products = clientService.getAllProducts();
        ComboBox<Product> productBox = new ComboBox<>();
        productBox.setPrefWidth(350);
        if (products != null) {
            productBox.setItems(FXCollections.observableArrayList(products));
            productBox.setPromptText("Chọn món uống / tráng miệng");
            productBox.setCellFactory(lv -> new ListCell<Product>() {
                private final ImageView thumbnail = new ImageView();
                private final Label nameLabel = new Label();
                private final Label priceLabel = new Label();
                private final HBox cellLayout = new HBox(10);
                {
                    thumbnail.setFitWidth(30);
                    thumbnail.setFitHeight(30);
                    thumbnail.setPreserveRatio(true);
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1F2937;");
                    priceLabel.setStyle("-fx-text-fill: #0E98A0;");
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    cellLayout.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    cellLayout.getChildren().addAll(thumbnail, nameLabel, spacer, priceLabel);
                }
                @Override
                protected void updateItem(Product item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        nameLabel.setText(item.getName());
                        priceLabel.setText(String.format("%,.0fđ", item.getPrice()));
                        if (item.getImagePath() != null && !item.getImagePath().trim().isEmpty()) {
                            try {
                                thumbnail.setImage(new javafx.scene.image.Image(item.getImagePath(), true));
                            } catch (Exception ex) {
                                thumbnail.setImage(null);
                            }
                        } else {
                            thumbnail.setImage(null);
                        }
                        setGraphic(cellLayout);
                    }
                }
            });
            productBox.setButtonCell(new ListCell<Product>() {
                @Override
                protected void updateItem(Product item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName() + " (" + String.format("%,.0f đ", item.getPrice()) + ")");
                }
            });
        }

        // Live image preview
        ImageView prodPreview = new ImageView();
        prodPreview.setFitWidth(100);
        prodPreview.setFitHeight(100);
        prodPreview.setPreserveRatio(true);
        prodPreview.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-background-radius: 6; -fx-border-radius: 6;");

        productBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getImagePath() != null && !newVal.getImagePath().trim().isEmpty()) {
                try {
                    prodPreview.setImage(new javafx.scene.image.Image(newVal.getImagePath(), true));
                } catch (Exception ex) {
                    prodPreview.setImage(null);
                }
            } else {
                prodPreview.setImage(null);
            }
        });

        TextField qtyField = new TextField("1");
        qtyField.setPromptText("Số lượng");

        Button addToCartBtn = new Button("🛒 Thêm vào giỏ");
        addToCartBtn.getStyleClass().addAll("btn", "btn-primary");
        addToCartBtn.setMaxWidth(Double.MAX_VALUE);

        addProdBox.getChildren().addAll(pickerTitle, new Label("Sản phẩm:"), productBox, new Label("Ảnh minh họa:"), prodPreview, new Label("Số lượng:"), qtyField, addToCartBtn);
        leftPane.getChildren().addAll(new Label("Khách hàng:"), customerRow, new Label("Trạng thái:"), statusBox, addProdBox);

        // Right Side: Cart Table
        VBox rightPane = new VBox(15);
        rightPane.getStyleClass().add("form-container");
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        Label cartTitle = new Label("Món ăn / Đồ uống trong hóa đơn");
        cartTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827;");

        TableView<OrderDetail> cartTable = new TableView<>();
        TableColumn<OrderDetail, String> nameCol = new TableColumn<>("Sản phẩm");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        nameCol.setPrefWidth(220);

        TableColumn<OrderDetail, Double> priceCol = new TableColumn<>("Đơn giá");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        priceCol.setPrefWidth(110);
        priceCol.setCellFactory(col -> new TableCell<OrderDetail, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.0f đ", item));
            }
        });

        TableColumn<OrderDetail, Integer> qtyCol = new TableColumn<>("S.Lượng");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(80);

        TableColumn<OrderDetail, Double> subtotalCol = new TableColumn<>("Thành tiền");
        subtotalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        subtotalCol.setPrefWidth(120);
        subtotalCol.setCellFactory(col -> new TableCell<OrderDetail, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.0f đ", item));
            }
        });

        TableColumn<OrderDetail, Void> actionCol = new TableColumn<>("Xóa");
        actionCol.setPrefWidth(80);
        actionCol.setCellFactory(col -> new TableCell<OrderDetail, Void>() {
            private final Button deleteBtn = new Button("❌");
            {
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2;");
                deleteBtn.setOnAction(e -> {
                    OrderDetail detail = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(detail);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        cartTable.getColumns().addAll(nameCol, priceCol, qtyCol, subtotalCol, actionCol);
        
        ObservableList<OrderDetail> cartItems = FXCollections.observableArrayList();
        cartTable.setItems(cartItems);
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        Label totalLabel = new Label("TỔNG CỘNG: 0 đ");
        totalLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #DC2626;");

        cartItems.addListener((javafx.collections.ListChangeListener<OrderDetail>) c -> {
            double total = cartItems.stream().mapToDouble(OrderDetail::getTotalPrice).sum();
            totalLabel.setText(String.format("TỔNG CỘNG: %,.0f đ", total));
        });

        addToCartBtn.setOnAction(e -> {
            Product selectedProd = productBox.getValue();
            String qtyText = qtyField.getText().trim();
            if (selectedProd == null) {
                showWarning("Lỗi chọn món", "Vui lòng chọn một món đồ uống / tráng miệng!");
                return;
            }
            int qty;
            try {
                qty = Integer.parseInt(qtyText);
                if (qty <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showWarning("Lỗi số lượng", "Số lượng sản phẩm nhập vào phải là số nguyên dương!");
                return;
            }
            
            Optional<OrderDetail> existing = cartItems.stream()
                .filter(item -> item.getProductId() == selectedProd.getId())
                .findFirst();
                
            if (existing.isPresent()) {
                OrderDetail detail = existing.get();
                int newQty = detail.getQuantity() + qty;
                detail.setQuantity(newQty);
                cartTable.refresh();
                cartItems.set(cartItems.indexOf(detail), detail);
            } else {
                OrderDetail detail = new OrderDetail(0, 0, selectedProd.getId(), qty, selectedProd.getPrice());
                detail.setProductName(selectedProd.getName());
                cartItems.add(detail);
            }
        });

        HBox footerBox = new HBox(15);
        footerBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Quay lại");
        cancelBtn.getStyleClass().addAll("btn", "btn-secondary");
        cancelBtn.setOnAction(e -> showOrderList());

        Button saveOrderBtn = new Button("💳 Thanh toán & In hóa đơn");
        saveOrderBtn.getStyleClass().addAll("btn", "btn-primary");
        saveOrderBtn.setOnAction(e -> {
            Customer selectedCust = customerBox.getValue();
            if (selectedCust == null) {
                showWarning("Lỗi thanh toán", "Vui lòng chọn khách hàng lập hóa đơn!");
                return;
            }
            if (cartItems.isEmpty()) {
                showWarning("Giỏ trống", "Chưa có món nào trong giỏ hàng!");
                return;
            }

            double total = cartItems.stream().mapToDouble(OrderDetail::getTotalPrice).sum();
            String mappedStatus = "Đã thanh toán".equals(statusBox.getValue()) ? "COMPLETED" : "CANCELLED";
            Order newOrder = new Order(0, selectedCust.getId(), total, mappedStatus);
            newOrder.setOrderDate(System.currentTimeMillis());
            newOrder.setUpdatedAt(System.currentTimeMillis());

            try {
                int orderId = clientService.createOrder(newOrder);
                if (orderId > 0) {
                    newOrder.setId(orderId);
                    newOrder.setCustomerName(selectedCust.getName());
                    
                    boolean allSaved = true;
                    for (OrderDetail item : cartItems) {
                        item.setOrderId(orderId);
                        boolean ok = clientService.createOrderDetail(item);
                        if (!ok) allSaved = false;
                    }
                    if (allSaved) {
                        showSuccess("Thành công", "Tạo hóa đơn thành công! Hệ thống sẽ hiển thị bản in hóa đơn.");
                        
                        // Show Receipt details print dialog
                        showOrderDetailDialog(newOrder);
                        
                        // Clear cart
                        cartItems.clear();
                        qtyField.setText("1");
                        productBox.setValue(null);
                        prodPreview.setImage(null);
                        
                        showOrderList();
                    } else {
                        showWarning("Lưu ý", "Hóa đơn được lưu nhưng có lỗi khi lưu các chi tiết món ăn.");
                        showOrderList();
                    }
                } else {
                    showError("Lỗi", "Không thể lưu hóa đơn.");
                }
            } catch (Exception ex) {
                showError("Lỗi hệ thống", "Lỗi: " + ex.getMessage());
            }
        });

        footerBox.getChildren().addAll(cancelBtn, saveOrderBtn);
        rightPane.getChildren().addAll(cartTitle, cartTable, totalLabel, footerBox);

        splitPane.getChildren().addAll(leftPane, rightPane);
        mainBox.getChildren().addAll(formTitle, splitPane);

        contentPane.getChildren().clear();
        contentPane.getChildren().add(mainBox);
    }

    private void showQuickAddCustomerDialog(ComboBox<Customer> customerBox) {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Thêm nhanh khách hàng");
        dialog.setHeaderText("Nhập thông tin khách hàng mới");

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox box = new VBox(10);
        box.setStyle("-fx-padding: 20; -fx-min-width: 350;");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Tên khách hàng (*)");
        
        TextField phoneField = new TextField();
        phoneField.setPromptText("Số điện thoại (*)");

        box.getChildren().addAll(
            new Label("Tên khách hàng:"), nameField,
            new Label("Số điện thoại:"), phoneField
        );
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();
                if (name.isEmpty() || phone.isEmpty()) {
                    showWarning("Nhập thiếu", "Tên và số điện thoại không được để trống!");
                    return null;
                }
                return new Customer(0, name, phone);
            }
            return null;
        });

        Optional<Customer> result = dialog.showAndWait();
        result.ifPresent(customer -> {
            boolean success = clientService.createCustomer(customer);
            if (success) {
                showSuccess("Thành công", "Đã thêm nhanh khách hàng mới!");
                List<Customer> updatedCustomers = clientService.getAllCustomers();
                if (updatedCustomers != null) {
                    customerBox.setItems(FXCollections.observableArrayList(updatedCustomers));
                    Optional<Customer> matching = updatedCustomers.stream()
                        .filter(c -> c.getPhone().equals(customer.getPhone()))
                        .findFirst();
                    matching.ifPresent(customerBox::setValue);
                }
            } else {
                showError("Lỗi", "Không thể lưu thông tin khách hàng mới.");
            }
        });
    }

    // ========== CATEGORY METHODS ==========

    @FXML
    public void showCategoryList() {
        setActiveNavigation(btnCategories, "Quản Lý Danh Mục");
        try {
            List<Category> categories = clientService.getAllCategories();
            if (categories != null) {
                showCategoryTable(categories);
            } else {
                showError("Lỗi", "Không thể tải danh mục");
            }
        } catch (Exception e) {
            logger.error("Error showing categories", e);
            showError("Lỗi", "Lỗi: " + e.getMessage());
        }
    }

    private void showCategoryTable(List<Category> categories) {
        VBox vbox = new VBox(20);
        vbox.setStyle("-fx-background-color: transparent;");

        // Action Panel
        HBox topPanel = new HBox(15);
        topPanel.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Tìm kiếm danh mục...");
        searchField.setPrefWidth(300);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕ Thêm danh mục");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> showCategoryForm(null));

        Button editBtn = new Button("📝 Sửa");
        editBtn.getStyleClass().addAll("btn", "btn-secondary");
        
        Button deleteBtn = new Button("🗑️ Xoá");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");

        topPanel.getChildren().addAll(searchField, spacer, addBtn, editBtn, deleteBtn);

        // Table
        TableView<Category> table = new TableView<>();
        TableColumn<Category, Void> idCol = new TableColumn<>("STT");
        idCol.setPrefWidth(50);
        idCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        TableColumn<Category, String> nameCol = new TableColumn<>("Tên danh mục");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);

        TableColumn<Category, String> descCol = new TableColumn<>("Mô tả");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(450);

        table.getColumns().addAll(idCol, nameCol, descCol);

        ObservableList<Category> tableData = FXCollections.observableArrayList(categories);
        FilteredList<Category> filteredData = new FilteredList<>(tableData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(cat -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase();
                if (cat.getName().toLowerCase().contains(filter)) return true;
                if (cat.getDescription() != null && cat.getDescription().toLowerCase().contains(filter)) return true;
                return false;
            });
        });

        table.setItems(filteredData);
        VBox.setVgrow(table, Priority.ALWAYS);

        editBtn.setOnAction(e -> {
            Category selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showCategoryForm(selected);
            } else {
                showWarning("Cảnh báo", "Vui lòng chọn danh mục cần sửa!");
            }
        });

        deleteBtn.setOnAction(e -> {
            Category selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (confirmAction("Xác nhận xoá", "Bạn có chắc chắn muốn xoá danh mục '" + selected.getName() + "'?")) {
                    if (clientService.deleteCategory(selected.getId())) {
                        showSuccess("Thành công", "Đã xoá danh mục");
                        showCategoryList();
                    } else {
                        showError("Lỗi", "Không thể xoá danh mục này. Có thể còn các sản phẩm đang thuộc danh mục này.");
                    }
                }
            } else {
                showWarning("Cảnh báo", "Vui lòng chọn danh mục cần xoá!");
            }
        });

        vbox.getChildren().addAll(topPanel, table);
        contentPane.getChildren().clear();
        contentPane.getChildren().add(vbox);
    }

    private void showCategoryForm(Category category) {
        if (!sessionManager.isAdmin()) {
            showWarning("Cảnh báo", "Chỉ quản trị viên mới có quyền thêm/sửa danh mục!");
            return;
        }

        VBox vbox = new VBox(20);
        vbox.getStyleClass().add("form-container");
        vbox.setMaxWidth(600);

        Label title = new Label(category == null ? "Thêm Danh Mục Mới" : "Chỉnh Sửa Danh Mục");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #0E98A0;");

        TextField nameField = new TextField();
        nameField.setPromptText("Tên danh mục");
        if (category != null) nameField.setText(category.getName());

        TextArea descField = new TextArea();
        descField.setPromptText("Mô tả chi tiết danh mục...");
        descField.setPrefRowCount(4);
        if (category != null) descField.setText(category.getDescription());

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Hủy");
        cancelBtn.getStyleClass().addAll("btn", "btn-secondary");
        cancelBtn.setOnAction(e -> showCategoryList());

        Button saveBtn = new Button("Lưu");
        saveBtn.getStyleClass().addAll("btn", "btn-primary");
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showWarning("Lỗi nhập liệu", "Tên danh mục không được để trống!");
                return;
            }
            Category cat = new Category(
                category == null ? 0 : category.getId(),
                name,
                descField.getText().trim()
            );
            boolean success = category == null ? clientService.createCategory(cat) : clientService.updateCategory(cat);
            if (success) {
                showSuccess("Thành công", category == null ? "Đã thêm danh mục mới!" : "Đã cập nhật danh mục!");
                showCategoryList();
            } else {
                showError("Lỗi", "Không thể lưu danh mục. Tên danh mục có thể đã trùng lặp.");
            }
        });

        btnBox.getChildren().addAll(cancelBtn, saveBtn);

        vbox.getChildren().addAll(
            title,
            new Label("Tên danh mục:"), nameField,
            new Label("Mô tả:"), descField,
            btnBox
        );

        contentPane.getChildren().clear();
        contentPane.getChildren().add(vbox);
    }

    // ========== USER MANAGEMENT METHODS (ADMIN ONLY) ==========

    @FXML
    public void showUserList() {
        setActiveNavigation(btnUsers, "Quản Lý Tài Khoản Người Dùng");
        try {
            List<User> users = clientService.getAllUsers();
            if (users != null) {
                showUserTable(users);
            } else {
                showError("Lỗi", "Không thể tải danh sách tài khoản");
            }
        } catch (Exception e) {
            logger.error("Error showing user list", e);
            showError("Lỗi", "Lỗi: " + e.getMessage());
        }
    }

    private void showUserTable(List<User> users) {
        VBox vbox = new VBox(20);
        vbox.setStyle("-fx-background-color: transparent;");

        // Action Panel
        HBox topPanel = new HBox(15);
        topPanel.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Tìm tài khoản, email...");
        searchField.setPrefWidth(300);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕ Thêm tài khoản");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> showUserForm(null));

        Button editBtn = new Button("📝 Sửa");
        editBtn.getStyleClass().addAll("btn", "btn-secondary");

        Button lockBtn = new Button("🔒 Khóa/Mở");
        lockBtn.getStyleClass().addAll("btn", "btn-danger");

        topPanel.getChildren().addAll(searchField, spacer, addBtn, editBtn, lockBtn);

        // Table
        TableView<User> table = new TableView<>();
        TableColumn<User, Void> idCol = new TableColumn<>("STT");
        idCol.setPrefWidth(50);
        idCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        TableColumn<User, String> nameCol = new TableColumn<>("Tên tài khoản");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameCol.setPrefWidth(150);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(220);

        TableColumn<User, String> roleCol = new TableColumn<>("Vai trò");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(120);

        TableColumn<User, Boolean> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        statusCol.setPrefWidth(150);
        statusCol.setCellFactory(col -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label();
                    badge.getStyleClass().add("badge");
                    if (item) {
                        badge.setText("Hoạt động");
                        badge.getStyleClass().add("badge-completed");
                    } else {
                        badge.setText("Đã khóa");
                        badge.getStyleClass().add("badge-cancelled");
                    }
                    setGraphic(badge);
                }
            }
        });

        TableColumn<User, Long> dateCol = new TableColumn<>("Ngày tạo");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setPrefWidth(180);
        dateCol.setCellFactory(col -> new TableCell<User, Long>() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : sdf.format(new Date(item)));
            }
        });

        table.getColumns().addAll(idCol, nameCol, emailCol, roleCol, statusCol, dateCol);

        ObservableList<User> tableData = FXCollections.observableArrayList(users);
        FilteredList<User> filteredData = new FilteredList<>(tableData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(u -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase();
                if (u.getUsername().toLowerCase().contains(filter)) return true;
                if (u.getEmail() != null && u.getEmail().toLowerCase().contains(filter)) return true;
                if (u.getRole().toLowerCase().contains(filter)) return true;
                return false;
            });
        });

        table.setItems(filteredData);
        VBox.setVgrow(table, Priority.ALWAYS);

        editBtn.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showUserForm(selected);
            } else {
                showWarning("Cảnh báo", "Vui lòng chọn tài khoản cần sửa!");
            }
        });

        lockBtn.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String action = selected.isActive() ? "khoá" : "mở khoá";
                if (confirmAction("Xác nhận thay đổi", "Bạn muốn " + action + " tài khoản '" + selected.getUsername() + "'?")) {
                    selected.setActive(!selected.isActive());
                    if (clientService.updateUser(selected)) {
                        showSuccess("Thành công", "Đã cập nhật trạng thái tài khoản!");
                        showUserList();
                    } else {
                        showError("Lỗi", "Không thể cập nhật trạng thái tài khoản.");
                    }
                }
            } else {
                showWarning("Cảnh báo", "Vui lòng chọn tài khoản cần khóa/mở!");
            }
        });

        vbox.getChildren().addAll(topPanel, table);
        contentPane.getChildren().clear();
        contentPane.getChildren().add(vbox);
    }

    private void showUserForm(User user) {
        VBox vbox = new VBox(15);
        vbox.getStyleClass().add("form-container");
        vbox.setMaxWidth(600);

        Label title = new Label(user == null ? "Thêm Tài Khoản Mới" : "Cập Nhật Tài Khoản");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #0E98A0;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Tên đăng nhập");
        if (user != null) {
            usernameField.setText(user.getUsername());
            usernameField.setDisable(true);
        }

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");
        Label pwdLabel = new Label("Mật khẩu:");
        if (user != null) {
            pwdLabel.setText("Mật khẩu mới (để trống nếu không đổi):");
        }

        TextField emailField = new TextField();
        emailField.setPromptText("Địa chỉ email");
        if (user != null) emailField.setText(user.getEmail());

        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("USER", "ADMIN"));
        roleBox.setValue(user == null ? "USER" : user.getRole());

        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("Hoạt động", "Khóa"));
        statusBox.setValue(user == null || user.isActive() ? "Hoạt động" : "Khóa");

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Quay lại");
        cancelBtn.getStyleClass().addAll("btn", "btn-secondary");
        cancelBtn.setOnAction(e -> showUserList());

        Button saveBtn = new Button("Lưu lại");
        saveBtn.getStyleClass().addAll("btn", "btn-primary");
        saveBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String pwd = passwordField.getText();
            String email = emailField.getText().trim();
            
            if (user == null && (username.isEmpty() || pwd.isEmpty())) {
                showWarning("Lỗi nhập liệu", "Tên đăng nhập và mật khẩu không được để trống!");
                return;
            }

            User u = new User();
            if (user != null) {
                u.setId(user.getId());
                u.setUsername(user.getUsername());
                u.setPassword(pwd.isEmpty() ? null : pwd);
            } else {
                u.setUsername(username);
                u.setPassword(pwd);
            }
            u.setEmail(email);
            u.setRole(roleBox.getValue());
            u.setActive("Hoạt động".equals(statusBox.getValue()));

            boolean success = (user == null) ? clientService.createUser(u) : clientService.updateUser(u);

            if (success) {
                showSuccess("Thành công", user == null ? "Đã tạo tài khoản thành công!" : "Đã cập nhật tài khoản!");
                showUserList();
            } else {
                showError("Lỗi", "Không thể lưu tài khoản. Tên đăng nhập có thể đã bị trùng.");
            }
        });

        btnBox.getChildren().addAll(cancelBtn, saveBtn);

        vbox.getChildren().addAll(
            title,
            new Label("Tên tài khoản:"), usernameField,
            pwdLabel, passwordField,
            new Label("Email:"), emailField,
            new Label("Vai trò:"), roleBox,
            new Label("Trạng thái:"), statusBox,
            btnBox
        );

        contentPane.getChildren().clear();
        contentPane.getChildren().add(vbox);
    }

    // ========== REPORTS & IMPORT/EXPORT SCREEN ==========

    @FXML
    public void showReportsScreen() {
        setActiveNavigation(btnReports, "Báo Cáo & Dữ Liệu");
        
        VBox vbox = new VBox(25);
        vbox.setStyle("-fx-background-color: transparent;");

        HBox cardsBox = new HBox(20);
        cardsBox.setAlignment(javafx.geometry.Pos.CENTER);

        // Card 1: Export Card
        VBox exportCard = new VBox(15);
        exportCard.getStyleClass().add("form-container");
        HBox.setHgrow(exportCard, Priority.ALWAYS);
        
        Label exportTitle = new Label("📤 XUẤT DỮ LIỆU (EXPORT)");
        exportTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #0E98A0;");
        Label exportDesc = new Label("Sao lưu dữ liệu từ hệ thống ra file.");
        exportDesc.setWrapText(true);
        exportDesc.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13;");

        Button expProdCsv = new Button("Xuất Sản Phẩm sang CSV");
        expProdCsv.getStyleClass().addAll("btn", "btn-primary");
        expProdCsv.setMaxWidth(Double.MAX_VALUE);
        expProdCsv.setOnAction(e -> handleExportData("PRODUCTS_CSV"));

        Button expCustCsv = new Button("Xuất Khách Hàng sang CSV");
        expCustCsv.getStyleClass().addAll("btn", "btn-primary");
        expCustCsv.setMaxWidth(Double.MAX_VALUE);
        expCustCsv.setOnAction(e -> handleExportData("CUSTOMERS_CSV"));

        Button expOrderCsv = new Button("Xuất Đơn Hàng sang CSV");
        expOrderCsv.getStyleClass().addAll("btn", "btn-primary");
        expOrderCsv.setMaxWidth(Double.MAX_VALUE);
        expOrderCsv.setOnAction(e -> handleExportData("ORDERS_CSV"));

        exportCard.getChildren().addAll(exportTitle, exportDesc, expProdCsv, expCustCsv, expOrderCsv);

        // Card 2: Import Card
        VBox importCard = new VBox(15);
        importCard.getStyleClass().add("form-container");
        HBox.setHgrow(importCard, Priority.ALWAYS);

        Label importTitle = new Label("📥 NHẬP DỮ LIỆU (IMPORT)");
        importTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #10B981;");
        Label importDesc = new Label("Nhập nhanh danh sách sản phẩm hoặc khách hàng.");
        importDesc.setWrapText(true);
        importDesc.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13;");

        Button impProdCsv = new Button("Nhập Sản Phẩm từ CSV");
        impProdCsv.getStyleClass().addAll("btn", "btn-success");
        impProdCsv.setMaxWidth(Double.MAX_VALUE);
        impProdCsv.setOnAction(e -> handleImportData("PRODUCTS_CSV"));

        Button impCustCsv = new Button("Nhập Khách Hàng từ CSV");
        impCustCsv.getStyleClass().addAll("btn", "btn-success");
        impCustCsv.setMaxWidth(Double.MAX_VALUE);
        impCustCsv.setOnAction(e -> handleImportData("CUSTOMERS_CSV"));

        importCard.getChildren().addAll(importTitle, importDesc, impProdCsv, impCustCsv);

        cardsBox.getChildren().addAll(exportCard, importCard);
        vbox.getChildren().addAll(cardsBox);

        contentPane.getChildren().clear();
        contentPane.getChildren().add(vbox);
    }

    private void handleExportData(String type) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Lưu tập tin dữ liệu");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        String defaultName = type.toLowerCase().replace("_csv", "") + "_export.csv";
        fileChooser.setInitialFileName(defaultName);
        
        File file = fileChooser.showSaveDialog(contentPane.getScene().getWindow());
        if (file != null) {
            boolean success = false;
            try {
                if ("PRODUCTS_CSV".equals(type)) {
                    List<Product> list = clientService.getAllProducts();
                    success = ExportUtil.exportProductsToCSV(list, file.getAbsolutePath());
                } else if ("CUSTOMERS_CSV".equals(type)) {
                    List<Customer> list = clientService.getAllCustomers();
                    success = ExportUtil.exportCustomersToCSV(list, file.getAbsolutePath());
                } else if ("ORDERS_CSV".equals(type)) {
                    List<Order> list = clientService.getAllOrders();
                    success = ExportUtil.exportOrdersToCSV(list, file.getAbsolutePath());
                }
                
                if (success) {
                    showSuccess("Thành công", "Xuất dữ liệu thành công ra tập tin:\n" + file.getName());
                } else {
                    showError("Lỗi", "Không thể xuất dữ liệu.");
                }
            } catch (Exception ex) {
                showError("Lỗi", "Đã xảy ra lỗi: " + ex.getMessage());
            }
        }
    }

    private void handleImportData(String type) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Chọn tập tin dữ liệu CSV");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        
        File file = fileChooser.showOpenDialog(contentPane.getScene().getWindow());
        if (file != null) {
            try {
                int importedCount = 0;
                if ("PRODUCTS_CSV".equals(type)) {
                    List<Product> list = ImportUtil.importProductsFromCSV(file.getAbsolutePath());
                    if (list != null && !list.isEmpty()) {
                        for (Product p : list) {
                            if (p.getCategoryId() <= 0) {
                                p.setCategoryId(1);
                            }
                            if (clientService.createProduct(p)) {
                                importedCount++;
                            }
                        }
                    }
                } else if ("CUSTOMERS_CSV".equals(type)) {
                    List<Customer> list = ImportUtil.importCustomersFromCSV(file.getAbsolutePath());
                    if (list != null && !list.isEmpty()) {
                        for (Customer c : list) {
                            if (clientService.createCustomer(c)) {
                                importedCount++;
                            }
                        }
                    }
                }
                
                if (importedCount > 0) {
                    showSuccess("Thành công", "Đã nhập thành công " + importedCount + " bản ghi vào database.");
                    showReportsScreen();
                } else {
                    showWarning("Kết quả", "Không nhập được bản ghi nào. Vui lòng kiểm tra định dạng file.");
                }
            } catch (Exception ex) {
                showError("Lỗi", "Đã xảy ra lỗi khi đọc file: " + ex.getMessage());
            }
        }
    }

    // ========== PROFILE & PERSONAL INFO METHODS ==========

    // ========== OTHER METHODS ==========

    @FXML
    public void handleExport() {
        showReportsScreen();
    }

    @FXML
    public void handleImport() {
        showReportsScreen();
    }

    @FXML
    public void handleLogout() {
        if (confirmAction("Xác nhận", "Bạn có muốn đăng xuất khỏi hệ thống?")) {
            try {
                // Terminate session
                sessionManager.logout();
                NetworkClient.getInstance().disconnect();
                
                // Switch back to Login Screen
                Stage stage = (Stage) userLabel.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                javafx.scene.Parent root = loader.load();
                javafx.scene.Scene scene = new javafx.scene.Scene(root, 400, 300);
                stage.setTitle("Quản lý cửa hàng - Đăng nhập");
                stage.setScene(scene);
                
                // Reconnect socket for next login
                NetworkClient.getInstance().connect();
                
                stage.show();
                logger.info("User logged out, returned to login screen");
            } catch (Exception ex) {
                logger.error("Error returning to login screen", ex);
                System.exit(0);
            }
        }
    }


    // ========== HELPER METHODS ==========

    private boolean confirmAction(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showSuccess(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    private void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    private void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");
        
        alert.showAndWait();
    }
}



