package com.example.appdbconnect;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import javafx.scene.Cursor; // Import Cursor class

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class VenueDetailsController {

    @FXML
    private Label nameLabel;

    @FXML
    private Label locationLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label capacityLabel;

    @FXML
    private Label contactEmailLabel;

    @FXML
    private Label contactPhoneLabel;

    @FXML
    private CheckBox photographyCheckBox;

    @FXML
    private CheckBox videographyCheckBox;

    @FXML
    private CheckBox cateringCheckBox;

    @FXML
    private Label totalLabel;

    @FXML
    private Button reserveButton;

    private double basePrice;
    private double photographyCost;
    private double videographyCost;
    private double cateringCost;

    private final String DB_URL = "jdbc:mysql://localhost:3306/event_management";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "Atharv.";

    private LocalDate selectedDate; // Store the selected date

    public void setVenueDetails(String venueName, LocalDate selectedDate) {
        this.selectedDate = selectedDate; // Set the selected date
        fetchVenueDetails(venueName);
    }

    private void fetchVenueDetails(String venueName) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String sql = "SELECT * FROM venues WHERE venue_name = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, venueName);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String location = resultSet.getString("location");
                double price = resultSet.getDouble("price");
                int capacity = resultSet.getInt("capacity");
                String email = resultSet.getString("email");
                String phone = resultSet.getString("phone_number");
                photographyCost = resultSet.getDouble("photography_cost");
                videographyCost = resultSet.getDouble("videography_cost");
                cateringCost = resultSet.getDouble("catering_cost");

                // Set the venue details
                nameLabel.setText(venueName);
                locationLabel.setText(location);
                priceLabel.setText("₹" + price);
                capacityLabel.setText(String.valueOf(capacity));
                contactEmailLabel.setText(email);
                contactPhoneLabel.setText(phone);
                basePrice = price;
                updateTotal(); // Update total with initial price
            } else {
                // Handle case when venue is not found
                showAlert("Venue Details Not Found", "No details found for the selected venue.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error fetching venue details from the database.");
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void initialize() {
        // Set cursor to hand when hovering over the reserve button
        reserveButton.setCursor(Cursor.HAND);

        // Listeners to update the total price when checkboxes are selected/deselected
        photographyCheckBox.setOnAction(event -> updateTotal());
        videographyCheckBox.setOnAction(event -> updateTotal());
        cateringCheckBox.setOnAction(event -> updateTotal());
    }

    // Updates the total price based on selected options
    private void updateTotal() {
        double total = basePrice;
        if (photographyCheckBox.isSelected()) total += photographyCost;
        if (videographyCheckBox.isSelected()) total += videographyCost;
        if (cateringCheckBox.isSelected()) total += cateringCost;

        totalLabel.setText("Total: ₹" + total);
    }

    @FXML
    private void onReserveButtonClick() {
        // Show invoice first, then close the current window
        showInvoice();

        // Close the current window after a slight delay to ensure the invoice window opens
        Platform.runLater(() -> {
            Stage currentStage = (Stage) reserveButton.getScene().getWindow();
            currentStage.close();
        });
    }

    private void showInvoice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Invoice.fxml"));
            Parent invoiceRoot = loader.load();

            InvoiceController invoiceController = loader.getController();
            invoiceController.setInvoiceDetails(nameLabel.getText(), totalLabel.getText(), generateReservationNumber(), selectedDate); // Pass LocalDate directly

            Stage invoiceStage = new Stage();
            invoiceStage.setScene(new Scene(invoiceRoot));
            invoiceStage.setTitle("Reservation Invoice");
            invoiceStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the invoice page.");
        }
    }

    private String generateReservationNumber() {
        // Generate a unique reservation number
        return "RES-" + System.currentTimeMillis();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}