package com.example.appdbconnect;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HelloController {

    @FXML
    private ComboBox<String> locationComboBox;

    @FXML
    private ComboBox<String> eventTypeComboBox;

    @FXML
    private ComboBox<String> peopleComboBox;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Spinner<String> timeSpinner;

    @FXML
    private ListView<String> venuesListView;

    private final String DB_URL = "jdbc:mysql://localhost:3306/event_management";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "Atharv.";

    private ObservableList<String> venues = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        ObservableList<String> locationList = FXCollections.observableArrayList("Bangalore", "Chennai", "Kolkata", "Mumbai", "New Delhi");
        locationComboBox.setItems(locationList);

        ObservableList<String> eventTypeList = FXCollections.observableArrayList("Wedding", "Party", "Conference", "Concert");
        eventTypeComboBox.setItems(eventTypeList);

        ObservableList<String> peopleList = FXCollections.observableArrayList("<100", "100-200", "200-500", "500+");
        peopleComboBox.setItems(peopleList);

        ObservableList<String> sortList = FXCollections.observableArrayList("Price (Low to High)", "Price (High to Low)", "Alphabetically (A-Z)", "Alphabetically (Z-A)");
        sortComboBox.setItems(sortList);

        datePicker.setValue(LocalDate.now());

        List<String> timeList = generateTimeOptions();
        SpinnerValueFactory<String> timeValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(FXCollections.observableArrayList(timeList));
        timeSpinner.setValueFactory(timeValueFactory);
        timeValueFactory.setValue("10:00");

        venuesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedVenue = venuesListView.getSelectionModel().getSelectedItem();
                if (selectedVenue != null) {
                    onVenueClick(selectedVenue.split(" \\(")[0]); // Extract venue name
                }
            }
        });
    }

    private List<String> generateTimeOptions() {
        List<String> times = new ArrayList<>();
        LocalTime time = LocalTime.of(9, 0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        while (!time.isAfter(LocalTime.of(22, 0))) {
            times.add(time.format(formatter));
            time = time.plusMinutes(15);
        }

        return times;
    }

    @FXML
    public void onSearchButtonClick() {
        String location = locationComboBox.getValue();
        String eventType = eventTypeComboBox.getValue();
        String people = peopleComboBox.getValue();
        LocalDate eventDate = datePicker.getValue();
        String eventTime = timeSpinner.getValue();

        // Check if all fields are filled
        if (location == null || eventType == null || people == null || eventDate == null || eventTime == null) {
            showAlert("Error", "Please fill all fields.");
            return;
        }

        // Check if the selected date is at least one week from today
        LocalDate currentDatePlusOneWeek = LocalDate.now().plusWeeks(1);
        if (eventDate.isBefore(currentDatePlusOneWeek)) {
            showAlert("Error", "You must book at least one week in advance. Please select a date after " + currentDatePlusOneWeek + ".");
            return;
        }

        fetchAvailableVenues(location, eventType, people, eventDate, eventTime);
    }

    private void fetchAvailableVenues(String location, String eventType, String people, LocalDate eventDate, String eventTime) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String sql = "SELECT venue_name, capacity, price, email, phone_number FROM venues WHERE location = ? AND event_type = ? AND capacity >= ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, location);
            preparedStatement.setString(2, eventType);
            preparedStatement.setInt(3, getCapacity(people));

            resultSet = preparedStatement.executeQuery();

            venues.clear();
            while (resultSet.next()) {
                String venueName = resultSet.getString("venue_name");
                int capacity = resultSet.getInt("capacity");
                double price = resultSet.getDouble("price");
                String email = resultSet.getString("email");
                String phoneNumber = resultSet.getString("phone_number");
                venues.add(venueName + " (Capacity: " + capacity + ", Price: ₹" + price +  ")");
            }

            venuesListView.setItems(venues);
            if (venues.isEmpty()) {
                showAlert("No Venues Found", "No venues available for the selected criteria.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to fetch venues from the database.");
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
    public void onSortComboBoxChanged() {
        String selectedSortOption = sortComboBox.getValue();

        if (selectedSortOption == null) return;

        switch (selectedSortOption) {
            case "Price (Low to High)":
                venues.sort(Comparator.comparing(venue -> {
                    String[] parts = venue.split(", Price: ₹");
                    return Double.parseDouble(parts[1].replace(")", "").split(",")[0]);
                }));
                break;

            case "Price (High to Low)":
                venues.sort(Comparator.comparing(venue -> {
                    String[] parts = venue.split(", Price: ₹");
                    return -Double.parseDouble(parts[1].replace(")", "").split(",")[0]);
                }));
                break;

            case "Alphabetically (A-Z)":
                venues.sort(Comparator.naturalOrder());
                break;

            case "Alphabetically (Z-A)":
                venues.sort(Comparator.reverseOrder());
                break;
        }

        venuesListView.setItems(venues);
    }

    private int getCapacity(String people) {
        switch (people) {
            case "<100":
                return 1;
            case "100-200":
                return 100;
            case "200-500":
                return 200;
            case "500+":
                return 500;
            default:
                return 0;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void onVenueClick(String selectedVenue) {
        try {
            // Close the current stage (window)
            Stage currentStage = (Stage) venuesListView.getScene().getWindow();
            currentStage.close();

            // Load the venue details FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("VenueDetails.fxml"));
            Parent venueDetailsRoot = loader.load();

            // Get the controller and set the venue details
            VenueDetailsController venueDetailsController = loader.getController();
            venueDetailsController.setVenueDetails(selectedVenue, datePicker.getValue()); // Pass the selected date

            // Create a new stage for the venue details
            Stage stage = new Stage();
            stage.setScene(new Scene(venueDetailsRoot));
            stage.setTitle("Venue Details");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the venue details page.");
        }
    }
}