package bgu.spl171.net.api;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.impl.packet.Packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by Dor on 1/25/2017.
 */
public class MessageEncDec implements MessageEncoderDecoder<Packet> {

    private Packet p=null;
    private short opCode=0;
    private byte[] bytesOfOpCode = new byte[2];
    private int i=0;
    private ByteBuffer byteBuffer;

    public void init(){
        p=null;
        bytesOfOpCode = new byte[2];
        i=0;
        byteBuffer = ByteBuffer.allocate(512);
    }
    @Override
    public Packet decodeNextByte(byte nextByte) {
        if(opCode==0){
            bytesOfOpCode[i]=nextByte;
            i++;
            if(i==2) {
                opCode = bytesToShort(bytesOfOpCode);
                init();
            }
            return null;
        }
        switch (opCode){
            case 1: {
                if (nextByte != 0) {
                    byteBuffer.put(nextByte);
                    return null;
                }
                else{
                    String fileName = byteBufferToChar(byteBuffer);
                    p=new Packet();
                    p.createRRQpacket(fileName);
                    opCode=0;
                    return p;
                }
            }

            case 2: {
                if (nextByte != 0) {
                    byteBuffer.put(nextByte);
                    return null;
                }
                else{
                    String fileName = byteBufferToChar(byteBuffer);
                    p=new Packet();
                    p.createWRQpacket(fileName);
                    opCode=0;
                    return p;
                }
            }

            case 6:{

            }

            case 7:{
                if(nextByte!=0) {
                    byteBuffer.put(nextByte);
                    return null;
                }
                else {
                    String userName = byteBufferToChar(byteBuffer);
                    p=new Packet();
                    p.createLOGRQpacket(userName);
                    opCode=0;
                    return p;
                }
            }

            case 8:{
                if(nextByte!=0) {
                    byteBuffer.put(nextByte);
                    return null;
                }
                else{
                    String fileName = byteBufferToChar(byteBuffer);
                    p=new Packet();
                    p.createDELRQpacket(fileName);
                    opCode=0;
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
