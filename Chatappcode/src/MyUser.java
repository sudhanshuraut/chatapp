import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.*;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MyUser extends User implements Runnable, Serializable {
    static int port=1111;
    static PrivateKey privateKey;
    List<Block> blockChain=new ArrayList<>();
    static DatagramPacket receivedPacket;
    static DatagramPacket sentPacket;
    static DatagramSocket mySocket;
    static User otherUser;
    boolean isUp=false;
    boolean KeyTransfer;
    String allText="";
    MyUser(String username, String myIP, String otherIP) throws NoSuchAlgorithmException, SocketException{
        super(myIP);
        this.userName=username;
        KeyPairGenerator kpg=KeyPairGenerator.getInstance("RSA");
    	kpg.initialize(2048);
    	KeyPair keyPair=kpg.generateKeyPair();
    	publicKey=keyPair.getPublic();
        privateKey=keyPair.getPrivate();
        otherUser=new User(otherIP);
        mySocket=new DatagramSocket(port);

    }
    void startThread(){
        Thread t1=new Thread(this);
        t1.start();
    }
    void sendMyKeys(String theIP){
        try {
            String str=userName+","+SerializeObject.serializeObject(this.publicKey);
            byte[] toBeSent=str.getBytes();
            InetAddress ia=InetAddress.getByName(theIP);
            sentPacket=new DatagramPacket(toBeSent,toBeSent.length,ia,port);
            mySocket.send(sentPacket);
        } catch (Exception ex) {
            Logger.getLogger(MyUser.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error! The IP is not up!");
            System.exit(0);
        }        
    }
    void listenForOthersKeys(){
        try {
            System.out.println("Listening for others..");
            byte[] receivedData=new byte[2048];
            receivedPacket=new DatagramPacket(receivedData,receivedData.length);
            mySocket.receive(receivedPacket);
            receivedData=receivedPacket.getData();
            String str=new String(receivedData);
            String a[]=str.split(",",2);
            Object o=SerializeObject.deserializeObject(a[1]);
            otherUser.publicKey=(PublicKey)o;
            otherUser.userName=a[0];
            System.out.println("Got key succsessfully from "+otherUser.userName);
            System.out.println(otherUser.publicKey);
        } catch (IOException ex) {
            Logger.getLogger(MyUser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MyUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void run() {
       while(isUp){
           try {
               while(KeyTransfer){
                   listenForOthersKeys();
                   KeyTransfer=false;
               }
               receive();
           } catch (Exception ex) {
               Logger.getLogger(MyUser.class.getName()).log(Level.SEVERE, null, ex);
           }
       }
    }
    void send(String msg) throws IOException, Exception{
        byte[] toBeSent;
        Message m=new Message(this.userName,otherUser.userName);
        String str;
        allText=allText+userName+": "+msg+"\n";
        chatBox.setText(allText);     
        m.cipher=encrypt(otherUser.publicKey,msg);
        m.vCipher=encryptPrivate(this.privateKey,m.vMessage);
        str="Message,"+SerializeObject.serializeObject(m);
        toBeSent=str.getBytes();
        InetAddress ia=InetAddress.getByName(otherUser.IP);
        sentPacket=new DatagramPacket(toBeSent,toBeSent.length,ia,port);
        mySocket.send(sentPacket);
    }
    
    void receive() throws Exception{
        try {
            byte[] receivedData=new byte[2048];
            receivedPacket=new DatagramPacket(receivedData,receivedData.length);
            mySocket.receive(receivedPacket);
            receivedData=receivedPacket.getData();
            String str=new String(receivedData);
            String arr[]=str.split(",",3);
            if(arr[0].equals("Message")){
                Message m=(Message)SerializeObject.deserializeObject(arr[1]);
                String message=decrypt(this.privateKey,m.cipher);
                System.out.println("Message received from "+otherUser.userName+":"+message);
                allText=allText+otherUser.userName+": "+message+"\n";
                chatBox.setText(allText);
            }
            else{
                Block k=(Block)SerializeObject.deserializeObject(arr[1]);
                if(blockChain.size()!=0){
                    System.out.println("currHash:"+k.previousHash);
                    System.out.println("Compared with:"+blockChain.get(blockChain.size()-1).currHash);
                    if(k.previousHash.equals(blockChain.get(blockChain.size()-1).currHash))
                        blockChain.add(k);
                }
                else{
                    blockChain.add(k);
                }
            }
            //String allText=chatBox.getText();              
        } catch (IOException ex) {
            Logger.getLogger(MyUser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MyUser.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    public static byte[] encrypt(PublicKey publicKey, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");  
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);  
        byte[] data=cipher.doFinal(message.getBytes());
        return data;
    }
    public static String decrypt(PrivateKey privateKey, byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");  
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        String message=new String(cipher.doFinal(encrypted));
        return message;
    }
    
    public static byte[] encryptPrivate(PrivateKey privateKey, String message) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");  
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);  
        byte[] data=cipher.doFinal(message.getBytes());
        return data;
    }
    
    public static String decryptPrivate(PublicKey publicKey, byte[] encrypted) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");  
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        String message=new String(cipher.doFinal(encrypted));
        return message;
    }

    void end(){
        isUp=false;
    }
    
}

