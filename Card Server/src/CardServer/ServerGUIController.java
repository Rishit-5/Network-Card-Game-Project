/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CardServer;


import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
import socketfx.FxSocketServer;
import socketfx.SocketListener;

import javax.swing.*;

/**
 * FXML Controller class
 *
 * @author jtconnor
 */

//ASK WHY IMAGES AREN'T SHOWING UP


public class ServerGUIController implements Initializable {


    @FXML
    private Button sendButton;
    @FXML
    private TextField sendTextField;
    @FXML
    private AnchorPane aPane;
    @FXML
    private Button connectButton, sng1;
    @FXML
    private TextField portTextField;
    @FXML
    private Label lblMessages, lblEvent,lblsc,lblcc;
    @FXML
    private ImageView imgdeck1, imgdeck2, imgS01,h2c1,h2c2,h2c3;
    private int pturn = 1;//determines whose turn it is
    private ArrayList<Card> played = new ArrayList<>();//stores the cards that have been played
    private int timess = 0;//client played an ace
    private int timesc = 0;//server played an ace
    private int timess1 = 0;//client played a jack
    private int timesc1 = 0;//server played a jack
    private int timess2 = 0;//client played a queen
    private int timesc2 = 0;//server played a queen
    private int timess3 = 0;//client played a king
    private int timesc3 = 0;//server played a king



    

//    private String n1, n2;
//    private boolean cClicked,sClicked;

    private final static Logger LOGGER =
            Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private boolean isConnected, turn = true, serverUNO = false, clientUNO = false;
    private int counter = 0;
    private String color;
    private Object Classname;

    public enum ConnectionDisplayState {

        DISCONNECTED, WAITING, CONNECTED, AUTOCONNECTED, AUTOWAITING
    }

    private FxSocketServer socket;
//precondition: Nothing was clicked beforehand
    //postcondition: the connection to the socket will be enabled for the server
    private void connect() {
        if (socket!=null) {
            socket.shutdown();
        }
        socket = new FxSocketServer(new FxSocketListener(),
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
        //precondition: server has connected to the socket
        //postcondition: handles messages sent from the client
        @Override
        public void onMessage(String line) {
            if (line.substring(0,3).equals("rdy")){
            }else if(line.substring(0,3).equals("nm2")){
                name2 = line.substring(3);
                lblName2.setText(name2);

                start2=true;
                checkDeal();
            }
            else if (line.substring(0,2).equalsIgnoreCase("qr")){//message indicating that client used a special card
                int f = Integer.parseInt(line.substring(2));
                handleClick2(f);
            }
            else if (line.equalsIgnoreCase("flip") && pturn == 2 && chand.size()>0){//makes sure that it's the right turn and the client has more cards left
                flipc();
            }
            else if (chand.size()<=0){//if client ran out of cards
                    lblEvent.setText("YOU WON!");
                    socket.sendMessage("L");
            }
            else if (line.equalsIgnoreCase("click")){// the client slapped the cards
                userClicked(2);
            }
        }


        @Override
        public void onClosedStatus(boolean isClosed) {
           
        }
    }

    @FXML
    private void handleSendMessageButton(ActionEvent event) {
        if (!sendTextField.getText().equals("")) {
            socket.sendMessage(sendTextField.getText());

        }
    }

    @FXML
    private void handleConnectButton(ActionEvent event) {
        connectButton.setDisable(true);

        displayState(ConnectionDisplayState.WAITING);
        connect();
    }

    @FXML
    private void handleStart(){

        name1 = txtName.getText();
        lblName1.setText(name1);
        socket.sendMessage("nm1" + name1);
        start1 = true;
        checkDeal();
    }
    private void checkDeal(){

        if (start1 && start2 ){
            btnDeal.setDisable(false);
        }
        else{

        }
    }
    //precondition: server has connected to the socket, server has more cards, and it's the server's turn
    @FXML
    public void flip() {//server's flip next card

        if (shand.size()>0 && pturn ==1){
            played.add(shand.get(0));//played stores the cards used in the current round
            socket.sendMessage(shand.get(0).getCardPath());
            imgS01.setImage(new Image(shand.remove(0).getCardPath()));
            updateCardCount();//cards that each user has can be seen on the right side of the screen
            if (inWild){//the client uses a wild card and the server's next card is a number, then the client keeps the whole round
                if (played.get(played.size()-1).getCardNumber()>10){
                    shand.addAll(played);
                    lblEvent.setText("P2 turn");
                    socket.sendMessage("P2 turn");
                    pturn =2;
                }
                else{//otherwise, the server gets the whole round
                    chand.addAll(played);
                    lblEvent.setText("P1 turn");
                    socket.sendMessage("P1 turn");
                    pturn = 1;
                }
                played.clear();
                imgS01.setImage(null);
                socket.sendMessage("blank");
                updateCardCount();
                inWild = false;
            }
            else if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("14")) {//if an ace is played then it resets all other cards and causes the client to now have to play 4

                lblEvent.setText("P2 turn");
                socket.sendMessage("P2 turn");
                pturn = 2;
                timesc =1;
                timess = 0;
                timess1 = 0;
                timesc1 = 0;
                timess2 = 0;
                timesc2 = 0;
                timess3 = 0;
                timesc3 = 0;



            }
            else if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("11")){//like ace, but with jack, so client has to put down 1 card

                lblEvent.setText("P2 turn");
                socket.sendMessage("P2 turn");
                pturn = 2;
                timesc1=1;
                timesc = 0;
                timess = 0;
                timess1 = 0;
                timess2 = 0;
                timesc2 = 0;
                timess3 = 0;
                timesc3 = 0;
            }
            else if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("12")){//queen is like jack but two cards
                lblEvent.setText("P2 turn");
                socket.sendMessage("P2 turn");
                pturn = 2;
                timesc1=0;
                timesc = 0;
                timess = 0;
                timess1 = 0;
                timess2 = 0;
                timesc2 = 1;
                timess3 = 0;
                timesc3 = 0;
            }
            else if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("13")){//king is like queen, but with 3 cards
                lblEvent.setText("P2 turn");
                socket.sendMessage("P2 turn");
                pturn = 2;
                timesc1=0;
                timesc = 0;
                timess = 0;
                timess1 = 0;
                timess2 = 0;
                timesc2 = 0;
                timess3 = 0;
                timesc3 = 1;
            }
            else if (timess>0){//if the client put down an ace

                lblEvent.setText("P1 turn");
                socket.sendMessage("P1 turn");
                timess++;
                if (timess>=5){//if the client never stopped the cycle of putting down cards, server gets all of them, and the turn
                    timess = 0;
                    chand.addAll(played);
                    played.clear();
                    imgS01.setImage(null);
                    socket.sendMessage("blank");
                    pturn = 2;
                    updateCardCount();
                }
            }
            else if (timess1>0) {//if client put down a jack

                lblEvent.setText("P1 turn");
                socket.sendMessage("P1 turn");
                timess1++;
                if (timess1 >= 2) {
                    timess1 = 0;
                    chand.addAll(played);
                    played.clear();
                    imgS01.setImage(null);
                        socket.sendMessage("blank");

                    pturn =2;
                    updateCardCount();
                }
            }
            else if (timess2>0){//if client put down a queen
                lblEvent.setText("P1 turn");
                    socket.sendMessage("P1 turn");

                timess2++;
                if (timess2>=3){
                    timess2 = 0;
                    chand.addAll(played);
                    played.clear();
                    imgS01.setImage(null);
                        socket.sendMessage("blank");
                    lblEvent.setText("P2 turn");
                    socket.sendMessage("P2 turn");
                    pturn = 2;
                    updateCardCount();
                }
            }
            else if (timess3>0){//if client put down a king
                lblEvent.setText("P1 turn");
                    socket.sendMessage("P1 turn");

                timess3++;
                if (timess3>=4){
                    timess3 = 0;
                    chand.addAll(played);
                    played.clear();
                    imgS01.setImage(null);
                        socket.sendMessage("blank");
                    lblEvent.setText("P2 turn");
                    socket.sendMessage("P2 turn");
                    pturn = 2;
                    updateCardCount();
                }
            }
            else if (played.size()>0) {//checks if client placed down one of four face cards

                if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("14")) {

                    lblEvent.setText("P2 turn");
                        socket.sendMessage("P2 turn");

                    pturn = 2;
                    timess = 0;
                    timess1 = 0;
                    timesc = 1;
                    timesc1 = 0;
                    timess2 = 0;
                    timesc2 = 0;
                    timess3 = 0;
                    timesc3 = 0;

                }
                if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("11")){

                    lblEvent.setText("P2 turn");
                    socket.sendMessage("P2 turn");
                    pturn = 2;
                    timess = 0;
                    timess1 = 0;
                    timesc = 0;
                    timesc1 = 1;
                    timess2 = 0;
                    timesc2 = 0;
                    timess3 = 0;
                    timesc3 = 0;
                }
                if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("12")){

                    lblEvent.setText("P2 turn");

                        socket.sendMessage("P2 turn");

                    pturn = 2;
                    timess = 0;
                    timess1 = 0;
                    timesc = 0;
                    timesc1 = 0;
                    timess2 = 0;
                    timesc2 = 1;
                    timess3 = 0;
                    timesc3 = 0;
                }
                if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("13")){

                    lblEvent.setText("P2 turn");
                        socket.sendMessage("P2 turn");
                    pturn = 2;
                    timess = 0;
                    timess1 = 0;
                    timesc = 0;
                    timesc1 = 0;
                    timess2 = 0;
                    timesc2 = 0;
                    timess3 = 0;
                    timesc3 = 1;
                }
                else {
                    lblEvent.setText("P2 turn");
                        socket.sendMessage("P2 turn");
                    pturn = 2;
                }
            }
            else {
                lblEvent.setText("P2 turn");
                    socket.sendMessage("P2 turn");
                pturn = 2;
            }
        }
        else if (shand.size()<=0){//if server has no cards left
            lblEvent.setText("BIG SAD ):");
            socket.sendMessage("W");
        }
    }
    //precondition: program started
    //postcondition: rules will be outputted in a pop-up
    @FXML
    public void handleRules(){
        JOptionPane.showMessageDialog(null, "Egyptian Ratscrew works by shuffling a deck of cards randomly and giving each player half the deck. Player one(the server) will " + "\n" +
                "place down their top card without being able to look at their deck. If that card is a face card(Jack,Queen,King, or Ace), then the next player" + "\n" +
                "has to place a certain amount of cards in a row(Jack is one, Queen is two, King is three, and Ace is four). However, if the next player produces a face card themselves, then " + "\n" +
                "the cycle is interrupted and the initial player must now place the correct amount of cards in a row. For example, if player one puts down a ace, " + "\n" +
                "then the second card that player two plays is a jack, player two no longer has to place any more cards, and player one has to put down one.   " + "\n" +
                "The next element of Egyptian Ratscrew is slapping. If a certain condition is met, then whoever slaps the cards first, gets all the cards in the round, regardless of any face cards" + "\n" +
                "(unless the slap is the last card being placed after a face card, like the fourth card of an ace). You aren't forced to slap if neither player sees it. In this version, there are three ways that a player can slap. The first is doubles. " + "\n" +
                "If two cards in a row are the same (Jack and Jack, 7 and 7), then the slap rule applies. Second is sandwiches. If two of the same card are seprated by a card(7, then 8, then 7), then wheoever slaps gets it." + "\n" +
                "Third is Jokers. If a Joker pops up, then whoever slaps the pile first gets the whole pile. This version of Egyptian Ratscrew also has specialty cards" + "\n" +
                " that resemble uno cards. The first is the skip card. If a player is forced to put down cards due to the previous player playing a face card, then they are able to use " + "\n" +
                "the skip turn card to void the face card. The next is the reverse card. If a player is forced down cards due to a face card, it reverses onto the player that played the face card" + "\n" +
                "(If previous player played an ace and the current player plays a reverse card, the previous player now has to put down 4 cards). The last specialty card is the wild card. " + "\n" +
                "If player one plays a wild card, then if player 2 plays a number card, then player one gets the whole pile. However, if player two puts down a face card, then player two gets the whole pile. " + "\n" +
                "The goal of the game is to get all of the cards in the deck and leave the other player with no cards left."




        );
    }
//precondition: the server previously flipped, pturn is two, and the client has more than 0 cards
    //postcondition: the client will be able to flip a card and it will be evaluated to meet certain criteria(face card, client has to play more cards, etc.)
    public void flipc(){//client's flip next card
        played.add(chand.get(0));
            socket.sendMessage(chand.get(0).getCardPath());
        imgS01.setImage(new Image(chand.remove(0).getCardPath()));
        updateCardCount();
        if (inWild){//same as with server
            if (played.get(played.size()-1).getCardNumber()>10){
                chand.addAll(played);
                pturn =1;
                lblEvent.setText("P1 turn");
                socket.sendMessage("P1 turn");
            }
            else{
                shand.addAll(played);
                pturn = 2;
                lblEvent.setText("P2 turn");
                socket.sendMessage("P2 turn");
            }
            played.clear();
            imgS01.setImage(null);
            socket.sendMessage("blank");
            updateCardCount();
            inWild = false;
        }
        else if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("14")) {//if the client played an ace

            lblEvent.setText("P1 turn");
                socket.sendMessage("P1 turn");
            pturn = 1;
            timess = 1;
            timess1 = 0;
            timesc = 0;
            timesc1 = 0;
            timess2 = 0;
            timesc2 = 0;
            timess3 = 0;
            timesc3 = 0;

        }
        else if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("11")){//client played a jack

            lblEvent.setText("P1 turn");
                socket.sendMessage("P1 turn");
            pturn = 1;
            timess1 = 1;
            timess = 0;
            timesc = 0;
            timesc1 = 0;
            timess2 = 0;
            timesc2 = 0;
            timess3 = 0;
            timesc3 = 0;
        }
        else if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("12")){//client played a queen

            lblEvent.setText("P1 turn");
                socket.sendMessage("P1 turn");
            pturn = 1;
            timess1 = 0;
            timess = 0;
            timesc = 0;
            timesc1 = 0;
            timess2 = 1;
            timesc2 = 0;
            timess3 = 0;
            timesc3 = 0;
        }
        else if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("13")){//client played a king

            lblEvent.setText("P1 turn");
                socket.sendMessage("P1 turn");
            pturn = 1;
            timess1 = 0;
            timess = 0;
            timesc = 0;
            timesc1 = 0;
            timess2 = 0;
            timesc2 = 0;
            timess3 = 1;
            timesc3 = 0;
        }

        else if (timesc>0){//server played an ace, so client has to place down 4 cards

            lblEvent.setText("P2 turn");
                socket.sendMessage("P2 turn");
            timesc++;
            if (timesc>=5){
                timesc = 0;
                shand.addAll(played);
                played.clear();
                imgS01.setImage(null);
                    socket.sendMessage("blank");
                pturn = 1;
                lblEvent.setText("P1 turn");
                socket.sendMessage("P1 turn");
                updateCardCount();
            }
        }
        else if (timesc1>0){//server put down a jack, so client has to play one card and then reset
            lblEvent.setText("P2 turn");
                socket.sendMessage("P2 turn");
            timesc1++;
            if (timesc1>=2){
                timesc1 = 0;
                shand.addAll(played);
                played.clear();
                imgS01.setImage(null);
                    socket.sendMessage("blank");
                pturn = 1;
                lblEvent.setText("P1 turn");
                socket.sendMessage("P1 turn");
                updateCardCount();
            }
        }
        else if (timesc2>0){//server put down a queen, so client has to put down  2 cards and reset
            lblEvent.setText("P2 turn");
                socket.sendMessage("P2 turn");
            timesc2++;
            if (timesc2>=3){
                timesc2 = 0;
                shand.addAll(played);
                played.clear();
                imgS01.setImage(null);
                    socket.sendMessage("blank");
                pturn = 1;
                lblEvent.setText("P1 turn");
                socket.sendMessage("P1 turn");
                updateCardCount();
            }
        }
        else if (timesc3>0){//server put down a king, so client has to put down 3 cards and reset
            lblEvent.setText("P2 turn");
            socket.sendMessage("P2 turn");//tells the client to update turn
            timesc3++;
            if (timesc3>=4){//king has to put down 3 cards(goes to 4 because timesc3 is already increased to initiate this)
                timesc3= 0;
                shand.addAll(played);
                played.clear();
                imgS01.setImage(null);
                    socket.sendMessage("blank");
                pturn = 1;
                lblEvent.setText("P1 turn");
                socket.sendMessage("P1 turn");
                updateCardCount();
            }
        }
        else if (played.size()>0) {
            if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("14")) {
                lblEvent.setText("P1 turn");
                    socket.sendMessage("P1 turn");
                pturn = 1;
                timess = 1;
                timess1 = 0;
                timesc = 0;
                timesc1 = 0;
                timess2 = 0;
                timesc2 = 0;
                timess3 = 0;
                timesc3 = 0;
            }
            if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("11")){

                lblEvent.setText("P1 turn");
                    socket.sendMessage("P1 turn");
                pturn = 1;
                timess1 = 1;
                timess = 0;
                timesc = 0;
                timesc1 = 0;
                timess2 = 0;
                timesc2 = 0;
                timess3 = 0;
                timesc3 = 0;
            }
            if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("12")){

                lblEvent.setText("P1 turn");
                    socket.sendMessage("P1 turn");
                pturn = 1;
                timess1 = 0;
                timess = 0;
                timesc = 0;
                timesc1 = 0;
                timess2 = 1;
                timesc2 = 0;
                timess3 = 0;
                timesc3 = 0;
            }
            if (played.get(played.size() - 1).getCardName().substring(1).equalsIgnoreCase("13")){

                lblEvent.setText("P1 turn");
                    socket.sendMessage("P1 turn");
                pturn = 1;
                timess1 = 0;
                timess = 0;
                timesc = 0;
                timesc1 = 0;
                timess2 = 0;
                timesc2 = 0;
                timess3 = 1;
                timesc3 = 0;
            }

            else {
                lblEvent.setText("P1 turn");
                    socket.sendMessage("P1 turn");
                pturn = 1;

            }
        }
        else {
            lblEvent.setText("P1 turn");
                socket.sendMessage("P1 turn");
            pturn = 1;

        }

    }

    //precondition: server clicked on the image view
    //postcondition: will send to another method that evaluates if it's a valid click
    @FXML
    public void click(){
        userClicked(1);
    }
    //precondition: a move has been made that changes the card count
    //postcondition: the labels with the card count will be updated
    public void updateCardCount(){
        lblcc.setText("Client's cards: " + chand.size());
        lblsc.setText("Server's cards: " + shand.size());
            socket.sendMessage("x" + chand.size());
            socket.sendMessage("q" + shand.size());
    }
    //precondition: either the server or the client clicked on the image view
    //postcondition: evaluates whether the user correctly clicked, and if so, it resets
    public void userClicked(int user){
        if (played.size()>1){
            if (played.get(played.size()-1).getCardNumber() == played.get(played.size()-2).getCardNumber()){//last two cards are the same
                if (user==1){
                    reset(1);
                }
                else if (user==2){
                    reset(2);
                }
            }
        }
        if (played.size()>2){
            if (played.get(played.size()-1).getCardNumber()== played.get(played.size()-3).getCardNumber()){//there was a sandwich with a card separating two that are the same
                if (user==1){
                    reset(1);
                }
                else if (user==2){
                    reset(2);
                }
            }
        }
        if (played.size()>0) {//sees if a joker has been played
            if (played.get(played.size() - 1).getCardName().equals("J1")) {
                if (user==1){
                    reset(1);
                }
                else if (user==2){
                    reset(2);
                }
            }
        }
    }
    //precondition: someone has won a pile
    //postcondition: that player gets all the cards, image views are reset, arrays are cleared, and pturn is decided based on who won
    public void reset(int user){
        if (user==1){
            shand.addAll(played);
            played.clear();
            imgS01.setImage(null);
                socket.sendMessage("blank");
            pturn = 1;
            lblEvent.setText("P1 turn");
            socket.sendMessage("P1 turn");
        }
        else if (user==2){
            chand.addAll(played);
            played.clear();
            imgS01.setImage(null);
                socket.sendMessage("blank");
            pturn = 2;
            lblEvent.setText("P2 turn");
            socket.sendMessage("P2 turn");
        }
        timess1 = 0;
        timess = 0;
        timesc = 0;
        timesc1 = 0;
        timess2 = 0;
        timesc2 = 0;
        timess3 = 0;
        timesc3 = 0;
        updateCardCount();
    }
    //precondition: server and client are connected
    //postcondition: server and client get their own cards and jokers, as well as specialty cards to use
    @FXML
    public void handleDeal(){
        btnFlip.setDisable(false);
        socket.sendMessage("enable");
        for (int i = 2; i < 15; i++) {
            deck.add(new Card("C" + Integer.toString(i)));
            deck.add(new Card("D" + Integer.toString(i)));
            deck.add(new Card("H" + Integer.toString(i)));
            deck.add(new Card("S" + Integer.toString(i)));

        }
        for (int i = 0; i < 26; i++) {
            shand.add(deck.remove((int)(Math.random()*deck.size())));

        }
            for (Card c : shand) {
                socket.sendMessage(c.getCardPath());
            }
        for (int i = 0; i < 26; i++) {
            chand.add(deck.remove((int)(Math.random()*deck.size())));
        }
            for (Card c : chand) {
                socket.sendMessage(c.getCardPath());
            }
        for (int i = 0; i < 3; i++) {//deals the jokers
            if (((int) (Math.random() * 2 + 1)) == 2) {
                chand.add((int) (Math.random() * chand.size() - 1), new Card("J" + 1));


            } else {
                shand.add((int) (Math.random() * shand.size() - 1), new Card("J" + 1));
            }
        }
        //sets up all the specialty uno cards for the players
        h2c1.setImage(new Image("resources/skip.jpg"));
        h2c2.setImage(new Image("resources/reverse.png"));
        h2c3.setImage(new Image("resources/wild.jpg"));
        Arrays.fill(shand2, 1);
        Arrays.fill(chand2,1);
        imgdeck1.setImage(new Image("resources/BACK-7.jpg"));
        imgdeck2.setImage(new Image("resources/BACK-7.jpg"));
        btnDeal.setDisable(true);
        lblsc.setText("Server's cards: " + shand.size() );
        lblcc.setText("Client's cards: " + chand.size() );
    }
    //precondition: the client clicked on a specialty card
    //postcondition: skip card will stop face cards, reverse cards will reverse the face cards to original player, and wild cards will end the pile based on the next card
    @FXML
    public void handleClick2(int index){
        if (index==0 && chand2[0]>0){
            imgS01.setImage(new Image("resources/skip.jpg"));
            socket.sendMessage("resources/skip.jpg");
            lblEvent.setText("P1 turn");
            socket.sendMessage("P1 turn");
            pturn =1;
            chand2[0]--;//prevents the client from using multiple of the same specialty card
            timess1 = 0;
            timess = 0;
            timesc = 0;
            timesc1 = 0;
            timess2 = 0;
            timesc2 = 0;
            timess3 = 0;
            timesc3 = 0;
        }
        else if (index==1 && chand2[1]>0){
            imgS01.setImage(new Image("resources/reverse.png"));//sets up images for both the server and client
            socket.sendMessage("resources/reverse.png");
            lblEvent.setText("P1 turn");
            socket.sendMessage("P1 turn");
            pturn =1;
            chand2[1]--;
            if (timesc>0){
                timesc = 0;
                timess = 1;
            }
            if (timesc1>0){
                timesc1 = 0;
                timess1 = 1;
            }
            if (timesc2>0){
                timesc2 = 0;
                timess2 = 1;
            }
            if (timesc3>0){
                timesc3 = 0;
                timess3 = 1;
            }
        }
        if (index==2 && chand2[2]>0){
            imgS01.setImage(new Image("resources/wild.jpg"));
            socket.sendMessage("resources/wild.jpg");
            lblEvent.setText("P1 turn");
            socket.sendMessage("P1 turn");
            pturn =1;
            chand2[2]--;
            timess1 = 0;//resets variables cause it's WILD TIME
            timess = 0;
            timesc = 0;
            timesc1 = 0;
            timess2 = 0;
            timesc2 = 0;
            timess3 = 0;
            timesc3 = 0;
            inWild = true;
        }
    }
    //precondition: the server clicked on a specialty card
    //postcondition: skip card will stop face cards, reverse cards will reverse the face cards to original player, and wild cards will end the pile based on the next card
    public void handleClick(MouseEvent event){
        int imgClicked;
        imgClicked =GridPane.getColumnIndex((ImageView) event.getSource());
        if (imgClicked==0 && shand2[0]>0){
            imgS01.setImage(new Image("resources/skip.jpg"));
            socket.sendMessage("resources/skip.jpg");
            h2c1.setImage(null);
            lblEvent.setText("P2 turn");
            socket.sendMessage("P2 turn");
            pturn =2;
            shand2[0]--;
            timess1 = 0;
            timess = 0;
            timesc = 0;
            timesc1 = 0;
            timess2 = 0;
            timesc2 = 0;
            timess3 = 0;
            timesc3 = 0;
        }
        if (imgClicked==1 && shand2[1]>0){
            imgS01.setImage(new Image("resources/reverse.png"));
            socket.sendMessage("resources/reverse.png");
            lblEvent.setText("P2 turn");
            socket.sendMessage("P2 turn");
            pturn =2;
            shand2[1]--;
            h2c2.setImage(null);
            if (timess>0){
                timess = 0;
                timesc = 1;
            }
            if (timess1>0){
                timess1 = 0;
                timesc1 = 1;
            }
            if (timess2>0){
                timess2 = 0;
                timesc2 = 1;
            }
            if (timess3>0){
                timess3 = 0;
                timesc3 = 1;
            }
        }
        if (imgClicked==2 && shand2[2]>0){
            imgS01.setImage(new Image("resources/wild.jpg"));
            socket.sendMessage("resources/wild.jpg");
            lblEvent.setText("P2 turn");
            socket.sendMessage("P2 turn");
            pturn =2;
            shand2[2]--;
            h2c3.setImage(null);
            timess1 = 0;
            timess = 0;
            timesc = 0;
            timesc1 = 0;
            timess2 = 0;
            timesc2 = 0;
            timess3 = 0;
            timesc3 = 0;
            inWild = true;
        }
    }
    private ArrayList<Card> deck = new ArrayList<>();
    private ArrayList<Card> shand = new ArrayList<>();
    private ArrayList<Card> chand = new ArrayList<>();
    private int[] shand2 = new int[3];//server's specialty cards
    private int[] chand2 = new int[3];//client's specialty cards
    private boolean start1 = false;
    private boolean start2 = false;
    private boolean inWild;//determines whether it's WILD TIME
    @FXML
    public Button btnDeal, btnFlip;
    @FXML
    public TextField txtName;
    private String name1;
    private String name2;
    @FXML
    public Label lblName1,lblName2;

    
   
    
    
    
}
