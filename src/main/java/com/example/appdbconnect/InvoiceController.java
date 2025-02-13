package com.example.appdbconnect;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InvoiceController {

    @FXML
    private Label reservationNumberLabel;

    @FXML
    private Label venueDetailsLabel;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private Label advancePaymentLabel;

    @FXML
    private Label dueDateLabel;

    @FXML
    private Label dateTimeLabel;

    @FXML
    private Label reservationDateLabel; // New label for reservation date

    // Set the invoice details
    public void setInvoiceDetails(String venueName, String totalAmount, String reservationNumber, LocalDate reservationDate) {
        reservationNumberLabel.setText("Reservation Number: " + reservationNumber);
        venueDetailsLabel.setText("Venue: " + venueName);
        totalAmountLabel.setText("Total Amount: " + totalAmount);
        reservationDateLabel.setText("Reservation Date: " + reservationDate); // Display the reservation date

        // Extract numeric part from totalAmount (remove "Total: " and currency symbol)
        String numericTotal = totalAmount.replace("Total: ₹", "").replace(",", "").trim();
        double total = Double.parseDouble(numericTotal);

        // Calculate advance payment (20% of total)
        double advancePayment = total * 0.20;
        advancePaymentLabel.setText("Advance Payment: ₹" + String.format("%.2f", advancePayment));

        // Calculate and display the due date (one week before the reserved date)
        LocalDate dueDate = reservationDate.minusWeeks(1);
        dueDateLabel.setText("Due Date: " + dueDate);

        // Display the current date and time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTimeLabel.setText("Date & Time: " + dtf.format(now));
    }

    @FXML
    private void onCloseButtonClick() {
        // Close the invoice window
        Stage stage = (Stage) reservationNumberLabel.getScene().getWindow();
        stage.close();
    }
}