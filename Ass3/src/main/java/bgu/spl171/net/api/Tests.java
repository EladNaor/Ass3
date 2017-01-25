package bgu.spl171.net.api;
import bgu.spl171.net.impl.packet.Packet;
import bgu.spl171.net.api.MessageEncDec;
/**
 * Created by Dor on 1/25/2017.
 */
public class Tests {

    public static void main(String[] args){
            MessageEncDec p = new MessageEncDec();
            Packet tmp = null;
            String nickName = "Spl_Took_My_Life";
            byte[] name = (nickName + '\0').getBytes();
            byte[] Opcode = MessageEncDec.shortToBytes((short)7);
            byte[] bytes = new byte [2 + name.length];
            bytes[0] = Opcode[0] ; bytes [1] = Opcode[1];
            for (int i = 2 ; i < bytes.length ; i++){
                bytes[i] = name[i-2];
            }

            for (int i = 0 ; i < bytes.length ; i++){
                tmp = p.decodeNextByte(bytes[i]);
            }

            System.out.println(tmp.getOpCode()==7);
    }
}
