package GBN;
import java.net.*;
import java.util.*;
import java.io.*;

public class Client extends Thread{
	public static DatagramSocket socket;
	public static final int MAX_LENGTH = 1025;
	public static final int MAX_SEQ	= 256;
	public static final int TIMEOUT = 3000;
	public static final int WIND_SIZE = 10;
	public static byte[] send;
	public static byte[] receive = new byte[MAX_LENGTH];
	public static byte seq = 0;
	public static InetAddress inetAddress; 
	public static int port;
	public static byte base;
	public static InputStream inputStream;
	public List<byte[]> buffer; 
	public Client() {
		try {
			socket = new DatagramSocket();
			inetAddress = InetAddress.getByName("localhost");
			port = 8888;
			inputStream = new FileInputStream("send.txt");
			buffer = new ArrayList<byte[]>();
			seq = 0;
			base = 0;
		} catch (SocketException e) { 
			e.printStackTrace();
		} catch (UnknownHostException e) { 
			e.printStackTrace();
		} catch (FileNotFoundException e) { 
			e.printStackTrace();
		} 
	}
	public void run() {
		boolean tag = true;
		int lastSeq = 0;
		while(true) {
		try { 
			if(seq < base + WIND_SIZE) {
				int len;
				if(tag){
					send = new byte[MAX_LENGTH];
					len = inputStream.read(send,1,send.length - 1);
				}
				else { 
					send = buffer.get(seq - base);
					System.out.print("��ʱ�ط�");
					len = send.length;
				}
				if(len == -1){
					System.out.println("�ļ�������");
					break; 
				}
				if(len != -1) {
					send[0] =  seq;
					if(seq == lastSeq - 1)
						tag = true;
					if(base == seq) {
						socket.setSoTimeout(TIMEOUT);  //������ʱ�� 
					}
					DatagramPacket packet1 = new DatagramPacket(send,0,len - 1,inetAddress,port);
					socket.send(packet1); 
					 buffer.add(send);
					System.out.println("���͵�"+seq+"�����ݰ�,��ʱseq��base�ֱ�Ϊ��"+base+"seq:"+seq);
					seq += 1;
				}
			}
			//datagram �յ����ͻ�����������ʱ�����Զ�ֹͣ
			DatagramPacket packet2 = new DatagramPacket(receive,0,1);
			socket.receive(packet2);
//			System.out.println("�յ�ACK�ǣ�"+receive[0]);
			if(base == receive[0]) {
				base = (byte) (base + 1);//������ǰ�ƶ� 
				buffer.remove(0); 
				if(base != seq)
					socket.setSoTimeout(TIMEOUT);
			}
//			System.out.println("base�ǣ�"+base+"seq�ǣ�"+seq);
		}catch(SocketTimeoutException e) {
			System.out.println("��ʱ�ط�,ȷ�ϵ�: " + base);
			lastSeq = seq;
			 seq = base;
			 tag = false;
		}catch (IOException e) { 
			e.printStackTrace();
		}
	}
	}  
}
