package com.aakrititiwari.onlineshopping;
import com.aakrititiwari.onlineshopping.models.CartItem;
import com.aakrititiwari.onlineshopping.models.Product;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, Integer> productIdColumn;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, Double> productPriceColumn;

    @FXML
    private TableColumn<Product, String> productDescriptionColumn;

    @FXML
    private TableView<CartItem> cartTable;

    @FXML
    private TableColumn<CartItem, Integer> cartProductIdColumn;

    @FXML
    private TableColumn<CartItem, String> cartProductNameColumn;

    @FXML
    private TableColumn<CartItem, Double> cartProductPriceColumn;

    @FXML
    private TableColumn<CartItem, Integer> cartProductQuantityColumn;

    @FXML
    private TableColumn<CartItem, Double> cartTotalPriceColumn;

    @FXML
    private VBox rootVBox;

    @FXML
    private Label grandTotalLabel;

    @FXML
    private StackPane stackPane;

    @FXML
    private VBox checkoutFormVBox;

    @FXML
    private TextField nameField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField contactNumberField;

    private ObservableList<Product> products = FXCollections.observableArrayList();
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    private static final String PRODUCT_API_URL = "http://localhost:8080/onlineshopping/api/products";
    private static final String ORDER_API_URL = "http://localhost:8080/onlineshopping/api/orders";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize product table columns
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Initialize cart table columns
        cartProductIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        cartProductNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        cartProductPriceColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        cartProductQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("productQuantity"));
        cartTotalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        // Bind products and cart items to their respective tables
        productTable.setItems(products);
        cartTable.setItems(cartItems);

        // Fetch products from API and populate the product table
        fetchProducts();

        // Update grand total whenever cart items change
        cartItems.addListener((ListChangeListener<CartItem>) change -> updateGrandTotal());

        // Hide checkout form initially
        setCheckoutFormVisibility(false);
    }

    private void fetchProducts() {
        try {
            String response = APIController.getData(PRODUCT_API_URL, "GET");
            JSONArray jsonArray = new JSONArray(response);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Product product = new Product(
                        jsonObject.getInt("id"),
                        jsonObject.getString("name"),
                        jsonObject.getDouble("price"),
                        jsonObject.getString("description")
                );
                products.add(product);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddToCart() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            // Create a new CartItem and add it to the cartItems list
            CartItem cartItem = new CartItem(selectedProduct.getId(), selectedProduct.getName(), selectedProduct.getPrice(), 1);
            cartItems.add(cartItem);
            Alert addedToCart = new Alert(Alert.AlertType.INFORMATION);
            addedToCart.setTitle("Product Added To Cart");
            addedToCart.setHeaderText(null);
            addedToCart.setContentText("Product : " + selectedProduct.getName() + "\nQuantity: 1");
            addedToCart.showAndWait();
        }
    }

    @FXML
    private void removeFromCart() {
        CartItem selectedCartItem = cartTable.getSelectionModel().getSelectedItem();

        if (selectedCartItem != null) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Delete");
            confirmationAlert.setHeaderText("Remove Product From Cart");
            confirmationAlert.setContentText("Are you sure you want to remove the product : " + selectedCartItem.getProductName() + " from cart?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // User confirmed deletion, remove the selected item from the TableView and underlying data list
                cartItems.remove(selectedCartItem);
            }
        } else {
            // Inform the user that no item is selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Product Selected");
            alert.setContentText("Please select a product to remove from cart.");
            alert.showAndWait();
        }
    }

    private void updateGrandTotal() {
        double grandTotal = 0.0;
        for (CartItem cartItem : cartItems) {
            grandTotal += cartItem.getTotalPrice();
        }
        grandTotalLabel.setText(String.format("Grand Total: Rs. %.2f", grandTotal));
    }

    private void setCheckoutFormVisibility(boolean visible) {
        checkoutFormVBox.setVisible(visible);
        checkoutFormVBox.setManaged(visible);
    }

    @FXML
    private void handleProceedToCheckout() {
        setCheckoutFormVisibility(true);
    }

    @FXML
    private void handleSubmitOrder() {
        String name = nameField.getText();
        String address = addressField.getText();
        String contactNumber = contactNumberField.getText();

        if (name.isEmpty() || address.isEmpty() || contactNumber.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill in all fields", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        JSONArray orderItems = new JSONArray();
        for (CartItem cartItem : cartItems) {
            JSONObject item = new JSONObject();
            item.put("productId", cartItem.getProductId());
            item.put("quantity", cartItem.getProductQuantity());
            item.put("price", cartItem.getProductPrice());
            item.put("total_price", cartItem.getTotalPrice());
            orderItems.put(item);
        }

        JSONObject order = new JSONObject();
        order.put("name", name);
        order.put("address", address);
        order.put("contactNumber", contactNumber);
        order.put("items", orderItems);

        String formData = "name=" + name +
                "&address=" + address +
                "&contactNumber=" + contactNumber +
                "&items=" + orderItems.toString();
        try {
            String response = APIController.sendPost(ORDER_API_URL, formData);

            // Parse response and show appropriate alert
            JSONObject responseObject = new JSONObject(response);
            String status = responseObject.getString("status");

            if ("order_placed_successfully".equals(status)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Order placed successfully!", ButtonType.OK);
                alert.showAndWait();

                // Clear cart and reset form
                cartItems.clear();
                nameField.clear();
                addressField.clear();
                contactNumberField.clear();
                setCheckoutFormVisibility(false);
                updateGrandTotal();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to place order: " + status, ButtonType.OK);
                alert.showAndWait();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to place order", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    private void goToProducts() throws IOException {
        App.setRoot("products");
    }

    @FXML
    private void goToOrders() throws IOException{
        App.setRoot("orders");
    }
}
