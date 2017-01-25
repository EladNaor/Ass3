package bgu.spl171.net.api;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.impl.packet.Packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by Dor on 1/25/2017.
 */
public class MessageEncDec implements MessageEncoderDecoder<Packet> {

    Packet p=null;
    short opCode=0;
    byte[] bytesOfOpCode = new byte[2];
    int i=0;
    ByteBuffer byteBuffer;


    @Override
    public Packet decodeNextByte(byte nextByte) {
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
                    byteBuffer = ByteBuffer.allocate(512);
                if(nextByte!=0) {
                    byteBuffer.put(nextByte);
                    return null;
                }
                else {
                    String userName = byteBufferToChar(byteBuffer);
                    p=new Packet();
                    p.createLOGRQpacket(userName);
                    return p;
                }
            }
        }
        return null;
    }

    private String byteBufferToChar(ByteBuffer byteBuffer){
        byteBuffer.flip();
        byte[] username=new byte[byteBuffer.limit()];
        for(int i=0; i<username.length; i++)
            username[i]=byteBuffer.get(i);
        String ans= null;
        try {
            ans = new String(username,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ans;
    }

    @Override
    public byte[] encode(Packet message) {
        return new byte[0];
    }

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    public static byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
}
