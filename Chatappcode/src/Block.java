import java.io.IOException;
import java.io.Serializable;
import java.lang.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author ssmis
 */
public class Block implements Serializable, Runnable {
    String previousHash;
    String currHash;
    Message message;
    long timeStamp;
    private int nonce;
        
    Block(String previousHash, Message m){
        try {
            this.previousHash=previousHash;
            message=m;
            timeStamp=new Date().getTime();
            currHash=generateCurrHash();
        } catch (IOException ex) {
            Logger.getLogger(Block.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    static Block createBlock(String ph,Message msg){
        return new Block(ph,msg);
    }
    
    
    private String generateCurrHash() throws IOException {
        String calculatedHash;
        calculatedHash=StringUtil.applySha256( previousHash +Long.toString(timeStamp) +SerializeObject.serializeObject(message)+nonce);
        return calculatedHash;
    }
    
    //guessing hash
    public void mineBlock() throws IOException {
        int difficulty; //number of initial zeroes
        difficulty = Integer.parseInt(TheChatApp.diff.getSelectedItem().toString());
	String target = new String(new char[difficulty]).replace('\0', '0'); // "000" for difficulty = 3
	while(!currHash.substring( 0, difficulty).equals(target)) {
		nonce ++;
		currHash = generateCurrHash();
	}
        this.currHash=currHash;
        
	System.out.println("Block Mined!!! : " + currHash);
    }

    @Override
    public void run(){
        try {
            mineBlock();
            if(Miner.verifyTransaction(this)){
                Miner.broadcastBlock(this);
            }
            TheChatApp.busy=false;
            TheChatApp.mineStatus.setVisible(false);
        } catch (IOException ex) {
            Logger.getLogger(Block.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Block.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public String toString(){
        String prev="Previous Hash: "+previousHash;
        String curr="Current Hash: "+currHash;
        String msg=message.toString();
        return prev+"\n\n"+msg+"\n"+curr;
    }
    
}
