/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fontyspublisherwhiteboard;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import shared.DrawEvent;

/**
 *
 * @author Nico Kuijpers
 */
public class WhiteBoard extends Application {

    // Drawing panel and its size
    private Canvas panel;
    private final int panelWidth = 500;
    private final int panelHeight = 500;
    private WhiteBoardCommunicator communicator;
    
    // Current color to draw
    private Color currentColor;
    
    // Current property to publish
    private String currentProperty;
    
    // Hash map to convert property to color
    private final Map<String, Color> propertyToColor;
    
    // Check boxes to select color and to subscribe to color
    private final Color[] colors = {Color.BLACK,Color.RED,Color.GREEN,Color.BLUE};
    private final String[] properties = {"black","red","green","blue"};
    private CheckBox[] checkBoxPublishArray;
    private CheckBox[] checkBoxSubscribeArray;

    public WhiteBoard() {
        
        // Create communicator to communicate with other white boards
        try {
            this.communicator = new WhiteBoardCommunicator(this);
        } catch (RemoteException ex) {
            Logger.getLogger(WhiteBoard.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Create and initialize hash map to convert property to color
        propertyToColor = new HashMap<>();
        for (int i = 0; i < properties.length; i++) {
            propertyToColor.put(properties[i],colors[i]);
        }
    }
    
    @Override
    public void start(Stage primaryStage) {
       
        // Define grid pane
        GridPane grid;
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        // For debug purposes
        // Make de grid lines visible
        // grid.setGridLinesVisible(true);
        
        // Drawing panel
        panel = new Canvas(panelWidth,panelHeight);
        grid.add(panel, 0, 0, 25, 21);
        clearPanel();
        
        // Define current color to draw
        currentColor = colors[0];
        currentProperty = properties[0];
        
        // Check boxes to select color and to subscribe to color
        grid.add(new Label("Publish: "),0, 22, 4, 1);
        grid.add(new Label("Subscribe: "),0, 23, 4, 1);
        checkBoxPublishArray = new CheckBox[properties.length];
        checkBoxSubscribeArray = new CheckBox[properties.length];
        for (int i = 0; i < properties.length; i++) {
            String text = properties[i];
            final int index = i;
            checkBoxPublishArray[i] = new CheckBox(text);
            checkBoxPublishArray[i].addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        checkBoxPublishMouseClicked(event,index);
                    }
                });
            grid.add(checkBoxPublishArray[i], i+4, 22, 1, 1);
            checkBoxSubscribeArray[i] = new CheckBox(text);
            checkBoxSubscribeArray[i].addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        checkBoxSubscribeMouseClicked(event,index);
                    }
                });
            grid.add(checkBoxSubscribeArray[i], i+4, 23, 1, 1);
        }
        checkBoxPublishArray[0].setSelected(true);
        
        // Button to clear drawing panel
        Button buttonClearPanel = new Button();
        buttonClearPanel.setText("Clear Panel");
        buttonClearPanel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                clearPanelButtonActionPerformed(event);
            }
        });
        grid.add(buttonClearPanel, 0, 24, 1, 1);
        
        // Button to establish connection to remote publisher
        Button buttonConnectToPublisher = new Button();
        buttonConnectToPublisher.setText("Connect to Publisher");
        buttonConnectToPublisher.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                connectToPublisherActionPerformed(event);
            }
        });
        grid.add(buttonConnectToPublisher, 4, 24, 10, 1);
        
        // Add mouse pressed event to drawing panel
        panel.addEventHandler(MouseEvent.MOUSE_PRESSED,
            new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    panelMousePressed(event);
                }
            });
        
        // Add mouse dragged event to drawing panel
        panel.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                panelMouseDragged(event);
            }
        });
        
        // Create the scene and add the grid pane
        Group root = new Group();
        Scene scene = new Scene(root, panelWidth+50, panelHeight+150);
        root.getChildren().add(grid);
        
        // Define title and assign the scene for main window
        primaryStage.setTitle("White Board");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public void clearPanel() {
        GraphicsContext gc = panel.getGraphicsContext2D();
        gc.clearRect(0.0, 0.0, panelWidth, panelHeight);
        gc.setFill(Color.WHITE);
        gc.fillRect(0.0, 0.0, panelWidth, panelHeight);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0.0, 0.0, panelWidth, panelHeight);
    }
    
    // Broadcast draw event to other white boards
    private void broadcastDrawDot (String property, double xPos, double yPos) {
        DrawEvent drawEvent = new DrawEvent(xPos,yPos);
        communicator.broadcast(property,drawEvent);
    }
    
    // Draw dot on white board
    private void drawDot(Color color, double xPos, double yPos) {
        // Graphics
        GraphicsContext gc = panel.getGraphicsContext2D();
        
        // Set color
        gc.setFill(color);
        
        // Draw dot
        gc.fillOval(xPos - 2, yPos - 2, 5, 5);
    }
    
    /**
     * Request to draw dot on white board.
     * @param property  color of draw event
     * @param drawEvent draw event
     */
    public void requestDrawDot(String property, DrawEvent drawEvent) {
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                drawDot(propertyToColor.get(property),drawEvent.getXpos(),drawEvent.getYpos());
            }
        });
    }
    
    private void checkBoxPublishMouseClicked(MouseEvent event, int index) {
        CheckBox cb = checkBoxPublishArray[index];
        if (cb.isSelected()) {
            // Set color to be published
            currentColor = colors[index];
            currentProperty = properties[index];
            for (CheckBox cbi : checkBoxPublishArray) {
                if (cbi != cb) {
                    cbi.setSelected(false);
                }
            }
        } 
    }
    
    private void checkBoxSubscribeMouseClicked(MouseEvent event, int index) {
        CheckBox cb = checkBoxSubscribeArray[index];
        if (cb.isSelected()) {
            // Subscribe to property corresponding to this check box
            communicator.subscribe(properties[index]);
        } else {
            // Unsubscribe to property corresponding to this check box
            communicator.unsubscribe(properties[index]);
        }
    }
    
    private void clearPanelButtonActionPerformed(ActionEvent event) {
        clearPanel();
    } 
    
    private void connectToPublisherActionPerformed(ActionEvent event) {
        // Establish connection with remote publisherForDomain
        communicator.connectToPublisher();
        
        // Register properties to be published
        for (String property : properties) {
            communicator.register(property);
        }
        
        // Subscribe communicator to selected properties
        for (int i = 0; i < checkBoxSubscribeArray.length; i++) {
            CheckBox cb = checkBoxSubscribeArray[i];
            if (cb.isSelected()) {
                // Subscribe to property correspondig to this check box
                communicator.subscribe(properties[i]);
            }
        }
    }                           

    private void panelMouseDragged(MouseEvent event) {
        drawDot(currentColor,event.getX(),event.getY());
        broadcastDrawDot(currentProperty,event.getX(),event.getY());
    }

    private void panelMousePressed(MouseEvent event) {
        drawDot(currentColor,event.getX(),event.getY());
        broadcastDrawDot(currentProperty,event.getX(),event.getY());
    }                                                                        
    
    @Override
    public void stop() {
        try {
            super.stop();
        } catch (Exception ex) {
            Logger.getLogger(WhiteBoard.class.getName()).log(Level.SEVERE, null, ex);
        }
        communicator.stop();
    }
    
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
