package gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.DayWeather;
import model.Location;
import org.json.JSONException;
import simulation.Simulation;

import java.io.IOException;
import java.util.List;

public class Controller {
    Simulation simulation = new Simulation();
    List<Location> locations;
    private Stage stage;
    protected Location currentLocationDetails;
    Timeline time;
    int day   = 1;
    int month = 1;
    int hour = 0;

    public Controller() throws IOException, JSONException {
        locations = simulation.getLocations();
        time = new Timeline();
        time.setCycleCount(Timeline.INDEFINITE);
        time.getKeyFrames().add(createKeyFrameLangtonAnt(400));
        time.play();
    }

    public void setMonth(int month){
        this.month = month;
    }
    public int getMonth(){
        return this.month;
    }
    public void setDay(int day){
        this.day = day;
    }
    public int getDay(){
        return this.day;
    }

    public Stage getStage(){
        return this.stage;
    }
    public void setStage(Stage stage){
        this.stage = stage;
    }
       //dodajemy w scence buliderze nowego labela nadajemy mu fx:id i tu zmienna musi miec taka sama nazwe jak to id

    @FXML private Label currentDateAndHourLabel;
    @FXML private Label numberOfTouristsLabel;
    @FXML private Label numberOfTouristsText;
    @FXML private Label date;
    @FXML private Button stopBtn;
    @FXML private Button resumeBtn;
    @FXML private Button deselectBtn;
    @FXML private ListView locationsListView  = new ListView();
    @FXML private ListView detailsListView = new ListView();
    public ObservableList details = FXCollections.observableArrayList(
            "Name: ",
            "Type: ",
            "Amount of Tourists: ",
            "Queue: ",
            "Maximal size: ",
            "Covered: ",
            "Google place_id: ",
            "Temperature: ",
            "Solar: ",
            "Precipation: ",
            "Wind: "
    );
    public ObservableList items = FXCollections.observableArrayList();
    public ObservableList defaultLocationsView = defaultLocationsViewInit();

    public ObservableList defaultLocationsViewInit(){
        ObservableList list = FXCollections.observableArrayList();
        for(int i = 0; i < simulation.getLocations().size(); i++){
            list.add(simulation.getDayInfoFromLocation(i));
        }
        return list;
    }
    

    
    public void simulation(int day,int month, int hour){
            currentDateAndHourLabel.setText(day + "." + month + ", "+ hour + ":00 - " + (hour+1) + ":00");
            simulation.simulateForDay(month,day);
            //simulation.checkAmountofTouristsInLocations();
            //setTextToListView();
            //locations.stream().forEach(l-> setTextToLabel(l.getName(),simulation.getDayInfoFromLocationByName(l.getName())));
            simulation.endDayInLocations();
            // sztuczka z buttonem. Jak jest odblokowany, to wyswietla szczegoly lokacji
            if(deselectBtn.isDisabled()){
                setDefaultTextToListView();
            }
            else{
                refreshDetails(day,month,hour);
            }

    }

    public void refreshDetails(int day, int month, int hour){
        if(details!=null){
            DayWeather weather = simulation.
                    getRecommendationSystem().
                    getWeather().
                    getWeatherInDay(month,day);
            details.set(2,"Amount of Tourists: " + currentLocationDetails.getAmountOfTourists());
            details.set(3,"Queue: " + currentLocationDetails.getQueue());
            details.set(7,"Temperature: " + weather.getTemperature());
            details.set(8,"Solar: " + weather.getSolar());
            details.set(9,"Precipation: " + weather.getPrecipation());
            details.set(10,"Wind: " + weather.getWind());
        }
    }

    // Póki co wszystkie sa bez znaczenia
    protected void setEvents(){
        stopBtn.setOnAction(e -> {
            stopBtn.setDisable(true);
            stopBtn.setVisible(false);
            resumeBtn.setDisable(false);
            resumeBtn.setVisible(true);
            time.stop();
        });
        resumeBtn.setOnAction(e -> {
            stopBtn.setDisable(false);
            stopBtn.setVisible(true);
            resumeBtn.setDisable(true);
            resumeBtn.setVisible(false);
            time.play();
        });
        deselectBtn.setOnAction(e -> {
            deselectBtn.setDisable(true);
            setDefaultTextToListView();
        });
        detailsListView.setOnContextMenuRequested(e -> {
            if(deselectBtn.isDisabled()){
                locationClickedForDetailsFromDetailsListView();
            }
        });
        numberOfTouristsLabel.setText(String.valueOf(simulation.getTourists().size()));
    }

    protected void setTextToDetailsList(String locationName){
        currentLocationDetails = simulation.getLocationByName(locationName);
        details.set(0,"Name: " + currentLocationDetails.getName());
        details.set(1,"Type: " + currentLocationDetails.getTypes());
        details.set(2,"Amount of Tourists: " + currentLocationDetails.getAmountOfTourists());
        details.set(3,"Queue: " + currentLocationDetails.getQueue());
        details.set(4,"Maximal size: " + currentLocationDetails.getMaxSize());
        details.set(5,"Covered: " + currentLocationDetails.isCoveredf());
        details.set(6,"Google place_id: " + currentLocationDetails.getPlaceId());
        detailsListView.setItems(details);
    }
    protected void setDefaultTextToListView(){
        for(int i = 0; i < simulation.getLocations().size() ; i++){
            defaultLocationsView.set(i, simulation.getDayInfoFromLocation(i));
        }
        detailsListView.setItems(defaultLocationsView);
    }

    @FXML public void locationClickedForDetails(){
        String locationName = String.valueOf(
                locationsListView.
                        getSelectionModel().
                        getSelectedItem()
        );
        setTextToDetailsList(locationName);
        deselectBtn.setDisable(false);
        detailsListView.refresh();
    }
    @FXML public void locationClickedForDetailsFromDetailsListView(){
        String locationName = String.valueOf(
                locations.get(
                        detailsListView.
                        getSelectionModel().
                        getSelectedIndex()
                ).getName().toString()
        );
        setTextToDetailsList(locationName);
        deselectBtn.setDisable(false);
        detailsListView.refresh();
    }

    protected void setTextToListView(){
        for(int i = 0; i < simulation.getLocations().size() ; i++){
            items.add(simulation.getLocationsName(i));
        }
        locationsListView.setItems(items);
  }
    private KeyFrame createKeyFrameLangtonAnt(int delay){
        return new KeyFrame(Duration.millis(delay), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //day++;
                if(hour < 23){
                    hour++;
                }
                else{
                    hour = 0;
                    day++;
                }

                if((day > 31) && (month == 12))
                {
                    month = 1;
                    day = 1;
                }
                else if((day > 27) && (month == 2))
                {
                    month++;
                    day = 1;
                }
                else if((day > 31) && (
                        (month == 1)||(month == 3)||(month == 5)||(month == 7)||(month == 8)||(month == 10)
                ))
                {
                    month++;
                    day = 1;
                }
                else if((day > 30) && (
                        (month == 4)||(month == 6)||(month == 9)||(month == 11)
                ))
                {
                    month++;
                    day = 1;
                }
                simulation(day,month,hour);
            }
        });
    }
  

}
