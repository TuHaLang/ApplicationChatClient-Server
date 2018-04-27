package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML private TextArea textContent;
    @FXML private TextField textMsg;
    @FXML private Button btnSendFile;
    @FXML private Button btnSendMsg;
    @FXML private Label lblChatWith = new Label();
    @FXML private TextArea textClient;

    static BufferedReader br = null;
    static BufferedWriter bw = null;
    static String contentMsg = "";
    static String contentSend = "";
    static boolean test = false;
    private static String nameClient = "User";
    private static String ipServer = "localhost";
    private static int port = 9999;
    private static String nameGroup = "Your Group";

    public void SendMsg(ActionEvent event){
        if(!textMsg.getText().equals("")) {
            contentSend = nameClient +": " + textMsg.getText() + "\n";
            contentMsg += "\n" + "You: " + textMsg.getText();
            textMsg.setText("");
            test = true;

        }
    }

    public void exit(ActionEvent event){
        System.exit(0);
    }

    public void changeIPServer(ActionEvent event){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("ApplicationChat");
        dialog.setHeaderText("Your old ip server is: " + ipServer + "\nPlease enter a new ip server: ");
        dialog.setContentText("Ip server:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(ip -> {
            if(!ip.trim().equals("")){
                ipServer = ip;
                SendAndReceive();
            }else{
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("WARNING!!!");
                alert.setContentText("Ip server not available ! \nIp server is kept as default");
                alert.show();
            }
        });
    }

    public void changePort(ActionEvent event){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("ApplicationChat");
        dialog.setHeaderText("Your old port is: " + ipServer + "\nPlease enter a new port: ");
        dialog.setContentText("Port:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPort -> {
            if(newPort.trim().matches("\\d+")){
                port = Integer.parseInt(newPort);
                SendAndReceive();
            }else{
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("WARNING!!!");
                alert.setContentText("Port is wrong ! \nPort is kept as default");
                alert.show();
            }
        });
    }

    public void changeNameGroup(ActionEvent event){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("ApplicationChat");
        dialog.setHeaderText("Your old name group is: " + ipServer + "\nPlease enter a new name: ");
        dialog.setContentText("Name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if(name.trim().matches("\\d+")){
                nameGroup = name;
                lblChatWith.setText(nameGroup);
            }else{
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("WARNING!!!");
                alert.setContentText("Name is wrong ! \nName is kept as default");
                alert.show();
            }
        });
    }

    public void SendAndReceive(){
        try {
            //Kết nối với server
            Socket socket = new Socket(ipServer, port);
            System.out.println("Connected server...");

            //Gửi username lên cho server
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bw.write("$$ " + nameClient);
            bw.newLine();
            bw.flush();

            //Nhận tin nhắn
            Runnable run1 = new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String contentLine = br.readLine();

                            //Hiển thị các client online
                            //Tin nhắn được gửi từ server sẽ được đánh dấu bằng cách bắt đầu bằng $$
                            if(contentLine.startsWith("$$")){
                                String[] clientOnline = contentLine.split(";");
                                contentLine="";
                                for(String client : clientOnline){
                                    contentLine+=client + "\n";
                                }
                                textClient.setText(contentLine);
                            }
                            else {
                                //Hiển thị tin nhắn gửi đến thông thường
                                if (!contentLine.trim().equals("")) {
                                    contentMsg += "\n" + contentLine;
                                    textContent.setText(contentMsg);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            Thread thread1 = new Thread(run1);
            thread1.start();



            //gửi tin nhắn
            Runnable run2 = new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                            if (test) {
                                bw.write(contentSend);
                                bw.newLine();
                                bw.flush();

                                textContent.setText(contentMsg);
                                contentSend = "";
                                test = false;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("ERROR!!!");
                            alert.setContentText("If you can't send messager please check Ip server and port !");
                            alert.show();
                        }
                    }
                }
            };
            Thread thread2 = new Thread(run2);
            thread2.start();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR!!!");
            alert.setContentText("Please make sure you have started SERVER!!! \nIf you started SERVER please check IP server and port!!!");
            alert.show();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //Nhập tên cho client
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("ApplicationChat");
        dialog.setHeaderText("Enter your name:");
        dialog.setContentText("Name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if(!name.trim().equals("")){
                nameClient = name;
            }
        });

        lblChatWith.setText(nameGroup);
        textClient.setText("");

        SendAndReceive();

        textContent.setEditable(false);

        //bắt sự kiện người dùng nhấn phím enter để gửi tin nhắn
        textMsg.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ENTER){
                    if(!textMsg.getText().equals("")) {
                        contentSend = nameClient + ": " + textMsg.getText() + "\n";
                        contentMsg += "\n" + "You: " + textMsg.getText();
                        textMsg.setText("");
                        test = true;
                    }
                }
            }
        });
    }
}
