/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CardClient;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import socketfx.Constants;
import socketfx.FxSocketClient;
import socketfx.SocketListener;

/**
 * FXML Controller class
 *
 * @author jtconnor
 */
public class ClientGUIController implements Initializable {


    @FXML
    private Button sendButton;
    @FXML
    private TextField sendTextField;
    @FXML
    private Button connectButton, btnFlip;
    @FXML
    private TextField portTextField;
    @FXML
    private AnchorPane aPane;
    @FXML
    private TextField hostTextField;
    @FXML
    private Label lblName3, lblName4, lblMessages, lblEvent,lblsc,lblcc;
    @FXML
    private ImageView imgdeck1,imgdeck2,imgS01,h2c1,h2c2,h2c3;
    @FXML
    private GridPane gPaneServer, gPaneClient;

    
    private final static Logger LOGGER =
            Logger.getLogger(MethodHandles.lookup().lookupClass().getName());



    private boolean isConnected, turn, serverUNO = false, clientUNO = false;

    public enum ConnectionDisplayState {

        DISCONNECTED, WAITING, CONNECTED, AUTOCONNECTED, AUTOWAITING
    }

    private FxSocketClient socket;
    private void connect() {
        socket = new FxSocketClient(new FxSocketListener(),
                hostTextField.getText(),
                Integer.valueOf(portTextField.getText()),
                Constants.instance().DEBUG_NONE);
        socket.connect();
    }

    private void displayState(ConnectionDisplayState state) {
//        switch (state) {
//            case DISCONNECTED:
//                connectButton.setDisable(false);
//                sendButton.setDisable(true);
//                sendTextField.setDisable(true);
//                break;
//            case WAITING:
//            case AUTOWAITING:
//                connectButton.setDisable(true);
//                sendButton.setDisable(true);
//                sendTextField.setDisable(true);
//                break;
//            case CONNECTED:
//                connectButton.setDisable(true);
//                sendButton.setDisable(false);
//                sendTextField.setDisable(false);
//                break;
//            case AUTOCONNECTED:
//                connectButton.setDisable(true);
//                sendButton.setDisable(false);
//                sendTextField.setDisable(false);
//                break;
//        }
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isConnected = false;
        displayState(ConnectionDisplayState.DISCONNECTED);


        

        Runtime.getRuntime().addShutdownHook(new ShutDownThread());

        /*
         * Uncomment to have autoConnect enabled at startup
         */
//        autoConnectCheckBox.setSelected(true);
//        displayState(ConnectionDisplayState.WAITING);
//        connect();
    }

    class ShutDownThread extends Thread {

        @Override
        public void run() {
            if (socket != null) {
                if (socket.debugFlagIsSet(Constants.instance().DEBUG_STATUS)) {
                    LOGGER.info("ShutdownHook: Shutting down Server Socket");    
                }
                socket.shutdown();
            }
        }
    }

    class FxSocketListener implements SocketListener {
        private ArrayList<String> cards= new ArrayList<>();
        @Override
        public void onMessage(String line) {
            if (line.equalsIgnoreCase("enable")){
                btnFlip.setDisable(false);
            }
            else if (cards.size()<52) {//deals with dealing
                cards.add(line);
                if (cards.size()==1){
                    imgdeck1.setImage(new Image("resources/BACK-7.jpg"));
                    imgdeck2.setImage(new Image("resources/BACK-7.jpg"));
                    h2c1.setImage(new Image("resources/skip.jpg"));
                    h2c2.setImage(new Image("resources/reverse.png"));
                    h2c3.setImage(new Image("resources/wild.jpg"));
                }
            }
            else if (line.equalsIgnoreCase("L")){
                lblEvent.setText("BIG SAD ):");
            }
            else if (line.equalsIgnoreCase("W")){
                lblEvent.setText("YOU WON!!");
            }
            else if (line.substring(0,1).equalsIgnoreCase("q")){
                lblsc.setText("Server's cards: " + line.substring(1));
            }
            else if (line.substring(0,1).equalsIgnoreCase("x")){
                lblcc.setText("Client's cards: " + line.substring(1));
            }
            else if (line.equalsIgnoreCase("blank")){
                imgS01.setImage(null);
            }
            else if (line.substring(0,6).equalsIgnoreCase("flipc")){
                imgS01.setImage(new Image(line.substring(6)));
            }
            else if (line.equalsIgnoreCase("P1 turn") || line.equalsIgnoreCase("P2 turn")){
                lblEvent.setText(line);
            }
            else{//deals with the flip card from the server
                imgS01.setImage(new Image(line));
            }
        }

        @Override
        public void onClosedStatus(boolean isClosed) {
           
        }
    }
    @FXML
    public void handleClick(MouseEvent event){
        int imgClicked;
        imgClicked = GridPane.getColumnIndex((ImageView) event.getSource());
        socket.sendMessage("qr" + imgClicked);
        if (imgClicked==0){
            h2c1.setImage(null);
        }
        else if (imgClicked==1){
            h2c2.setImage(null);
        }
        else if (imgClicked==2){
            h2c3.setImage(null);
        }
    }
    @FXML
    public void click(){
        socket.sendMessage("click");
    }
    


    @FXML
    private void handleSendMessageButton(ActionEvent event) {
        if (!sendTextField.getText().equals("")) {
            String x = sendTextField.getText();
            socket.sendMessage(x);


        }

    }
    @FXML
    public void flip(){
        socket.sendMessage("flip");
    }

    @FXML
    private void handleConnectButton(ActionEvent event) {
        connectButton.setDisable(true);

        displayState(ConnectionDisplayState.WAITING);
        connect();
    }
    @FXML
    private void handleStart(){

        name2 = txtName.getText();
        lblName2.setText(name2);
        socket.sendMessage("nm2" + name2);

    }

    @FXML
    public TextField txtName;
    @FXML
    public Label lblName1, lblName2;
    private String name1;
    private String name2;
}
