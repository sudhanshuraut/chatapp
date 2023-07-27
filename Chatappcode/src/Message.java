
import java.io.*;
import java.security.*;
import javax.crypto.*;


public class Message implements Serializable {
    String sender;
    String receiver;
    byte[] cipher;
    String vMessage="VERIFY";
    byte[] vCipher;
    Message(String sender,String receiver){
        this.sender=sender;
        this.receiver=receiver;
    }
    
    @Override
    public String toString(){
        String a="Sender: "+sender;
        String b="Receiver: "+receiver;
        String c="Encrypted message: "+cipher.toString();
        String ffinal=a+"\n"+b+"\n"+c+"\n";
        return ffinal;
    }
}


