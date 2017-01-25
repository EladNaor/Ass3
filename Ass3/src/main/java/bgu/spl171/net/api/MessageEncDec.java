package bgu.spl171.net.api;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.impl.packet.Packet;
import java.nio.ByteBuffer;

/**
 * Created by Dor on 1/25/2017.
 */
public class MessageEncDec implements MessageEncoderDecoder {

    Packet p=null;
    short opCode=0;
    byte[] bytesOfOpCode = new byte[2];
    int i=0;
    ByteBuffer byteBuffer;


    @Override
    public Object decodeNextByte(byte nextByte) {
        if(opCode==0){
            bytesOfOpCode[i]=nextByte;
            i++;
            if(i==2)
                opCode=bytesToShort(bytesOfOpCode);
            return null;
        }

        switch (opCode){
            case 7:{
                if(byteBuffer==null)
                    byteBuffer.allocate(512);
                if(nextByte!=0) {
                    byteBuffer.put(nextByte);
                    return null;
                }
                else {
                    char[] userNameChar = byteBufferToChar(byteBuffer);
                    String userNameString="";
                    for(int i=0; i<userNameChar.length; i++)
                        userNameString+=userNameChar[i];
                    p.createLOGRQpacket(userNameString);
                    return p;
                }
            }
        }


        return null;
    }

    private char[] byteBufferToChar(ByteBuffer byteBuffer){
        byteBuffer.flip();
        char[] ans=new char[byteBuffer.limit()];
        for(int i=0; i<ans.length; i++)
            ans[i]=byteBuffer.getChar();
        return ans;
    }

    @Override
    public byte[] encode(Object message) {
        return new byte[0];
    }

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
}
