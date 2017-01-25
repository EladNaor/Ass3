package bgu.spl171.net.api;
import bgu.spl171.net.impl.packet.Packet;

import java.util.Date;
import java.util.Random;

/**
 * Created by Dor on 1/25/2017.
 */
public class Tests {

    private static MessageEncoderDecoder<Packet> p = new MessageEncDec();

    public static void main (String []args){
        // log request test
       // ACKTest ();
        LOGRQtest();
//        DATAtest ();
//        ERRORtest ();
//        DELRQtest();
        WRQTest ();
//        RRQTest ();
//        DISCTest ();
//        DIRQtest ();
//        RRQTest ();
//        ACKTest ();
//        DELRQtest();
//        DIRQtest ();
//        WRQTest ();
//        ERRORtest ();


    }

    private static void ERRORtest() {
        String expected = "Not defined";
        byte[] asa = expected.getBytes();
        Packet tmp = null;
        byte[] en = new byte[asa.length+5];
        en[0]=0; en[1]=5; en[2]=0; en[3]=4; en[en.length-1]=0;
        for (int i = 4 ; i < en.length-1 ; i++){
            en[i] = asa[i-4];
        }
        for (int i = 0 ; i < en.length ; i++){
            tmp = p.decodeNextByte(en[i]);
        }
        System.out.println(tmp.getOpCode()==5 && tmp.getString().equals(expected));
    }

    private static void DATAtest() {
        Random rnd = new Random(new Date().getTime());
        Packet tmp = null;
        byte[] Opcode = MessageEncDec.shortToBytes((short) 3);
        byte[] size = MessageEncDec.shortToBytes((short)400);
        byte[] block = MessageEncDec.shortToBytes((short) 7);
        byte[] bytes = new byte[6 + 400];
        bytes[0] = Opcode[0] ; bytes[1] = Opcode[1] ; bytes[2] =  size[0] ; bytes[3] = size[1] ;bytes[4] = block[0] ;bytes[5] = block[1];
        for (int i = 6 ; i < bytes.length ; i++){
            bytes[i] = (byte) (rnd.nextInt(255) + 1);
        }

        for (int i = 0 ; i < bytes.length ; i++){
            tmp = p.decodeNextByte(bytes[i]);
        }

        System.out.println(tmp .getOpCode() == 3 );
    }

    private static void DIRQtest() {
        Packet tmp = null;
        byte[] Opcode = MessageEncDec.shortToBytes((short) 6);
        for (int i = 0 ; i < 2 ; i++){
            tmp = p.decodeNextByte(Opcode[i]);
        }
        System.out.println(tmp.getOpCode() == 6);
    }

    private static void ACKTest() {
//        Packet tmp = null;
//        byte[] Opcode = MessageEncDec.shortToBytes((short) 4);
//        byte[] block = MessageEncDec.shortToBytes((short) 0);
//        byte[] bytes = new byte[] {Opcode[0] , Opcode[1] , block[0] , block[1]};
//        for (int i = 0 ; i < 4 ; i++){
//            tmp = p.decodeNextByte(bytes[i]);
//        }
//
        Packet pp = new Packet();
        Packet pp2 = new Packet();
        pp.createACKpacket((short) 0);
        byte[] aaa = p.encode(pp);
        for (int i = 0; i < aaa.length; i++) {
            pp2 = p.decodeNextByte(aaa[i]);
        }

        System.out.println(pp.getOpCode()== pp2.getOpCode() && pp.getBlockNumber()==pp2.getBlockNumber());
//        System.out.println(tmp.getOpCode()== 4 && tmp.getBlockNumber()==0);
    }

    private static void DISCTest() {
        Packet tmp = null;
        byte[] Opcode = MessageEncDec.shortToBytes((short) 10);
        for (int i = 0 ; i < 2 ; i++){
            tmp = p.decodeNextByte(Opcode[i]);
        }
        System.out.println(tmp .getOpCode() == 10);
    }

    private static void RRQTest() {
        Packet tmp = null;
        String fileName = "I-have-a-programmer-hands";
        byte[] name = (fileName + '\0').getBytes();
        byte[] Opcode = MessageEncDec.shortToBytes((short) 1);
        byte[] bytes = new byte[2 + name.length];
        bytes[0] = Opcode[0] ; bytes[1] = Opcode[1];
        for (int i = 2 ; i < bytes.length ; i++){
            bytes[i] = name[i-2];
        }

        for (int i = 0 ; i < bytes.length ; i++){
            tmp = p.decodeNextByte(bytes[i]);
        }

        System.out.println(tmp .getOpCode() == 1 && tmp.getString().equals(fileName));
    }

    private static void WRQTest() {
//        Packet tmp = null;
//        String fileName = "chocolate_or_strongberry";
//        byte[] name = (fileName + '\0').getBytes();
//        byte[] Opcode = MessageEncDec.shortToBytes((short) 2);
//        byte[] bytes = new byte[2 + name.length];
//        bytes[0] = Opcode[0] ; bytes[1] = Opcode[1];
//        for (int i = 2 ; i < bytes.length ; i++){
//            bytes[i] = name[i-2];
//        }
//
//        for (int i = 0 ; i < bytes.length ; i++){
//            tmp = p.decodeNextByte(bytes[i]);
//        }
//
//        System.out.println(tmp .getOpCode() ==  2 && tmp.getString().equals(fileName));

        Packet pp = new Packet();
        Packet pp2 = new Packet();
        pp.createWRQpacket("WritePacket");
        byte[] aaa = p.encode(pp);
        for (int i = 0; i < aaa.length; i++) {
            pp2 = p.decodeNextByte(aaa[i]);
        }
        System.out.println(pp.getOpCode()== pp2.getOpCode() && pp.getString().equals(pp2.getString()));
    }

    private static void DELRQtest() {
        Packet tmp = null;
        String fileName = "Neto-Avoda";
        byte[] name = (fileName + '\0').getBytes();
        byte[] Opcode = MessageEncDec.shortToBytes((short) 8);
        byte[] bytes = new byte[2 + name.length];
        bytes[0] = Opcode[0] ; bytes[1] = Opcode[1];
        for (int i = 2 ; i < bytes.length ; i++){
            bytes[i] = name[i-2];
        }

        for (int i = 0 ; i < bytes.length ; i++){
            tmp = p.decodeNextByte(bytes[i]);
        }

        System.out.println(tmp.getOpCode() ==8 && tmp.getString().equals(fileName));
    }


    private static void LOGRQtest() {
//        MessageEncDec p = new MessageEncDec();
//        Packet tmp = null;
//        String nickName = "Spl_Took_My_Life";
//        byte[] name = (nickName + '\0').getBytes();
//        byte[] Opcode = MessageEncDec.shortToBytes((short)7);
//        byte[] bytes = new byte [2 + name.length];
//        bytes[0] = Opcode[0] ; bytes [1] = Opcode[1];
//        for (int i = 2 ; i < bytes.length ; i++){
//            bytes[i] = name[i-2];
//        }
//
//        for (int i = 0 ; i < bytes.length ; i++){
//            tmp = p.decodeNextByte(bytes[i]);
//        }
//        System.out.println(tmp.getOpCode()==7 && tmp.getString().equals(nickName));\

        Packet pp = new Packet();
        Packet pp2 = new Packet();
        pp.createLOGRQpacket("login nickname");
        byte[] aaa = p.encode(pp);
        for (int i = 0; i < aaa.length; i++) {
            pp2 = p.decodeNextByte(aaa[i]);
        }

        System.out.println(pp.getOpCode()== pp2.getOpCode() && pp.getString().equals(pp2.getString()));


    }
}
