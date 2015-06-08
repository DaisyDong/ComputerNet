package GBN;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server2 extends Thread{
	public static DatagramSocket socket;
	public static final int MAX_LENGTH = 1025;//����������
	public static final int WIND_SIZE = 10;//�������к�
	public static final int TIMEOUT = 3000;
	public static byte[] receive = new byte[MAX_LENGTH];
	public static byte[] send = new byte[MAX_LENGTH];
	public static byte next = 0;	//��һ���ڴ������к�
	public static byte base = 0;
	public static byte seq = 0;
	public static OutputStream writeFile;
	public static InputStream readFile;
	public static InetAddress inetAddress;
	public static int port;
	public List<byte[]> buffer = new ArrayList<byte[]>(); 
	
	public Server2() { 
		try {
			socket = new DatagramSocket(8888);   
			writeFile = new FileOutputStream("Server_Receive.txt"); 
			readFile = new FileInputStream("Server_Send.txt");
		} catch (SocketException | FileNotFoundException e) { 
			e.printStackTrace();
		}
	}
	
	public void run() {
		int count = 0;
		boolean tag = true;
		byte lastSeq = -1;
		next = 0;
		byte base = 0;
		byte seq = 0;
		while(true) {
		try { 
			DatagramPacket packet1 = new DatagramPacket(receive,receive.length); 
			socket.receive(packet1);
			System.out.println("�������յ���"+receive[0]+"�����ݰ�,��ʱ�ڴ����ǣ�"+next);
			byte order = receive[0]; //���ڵ����ߴ���10
			byte ack = receive[1];	//�Է����Լ��������ݵ�ack
			send = new byte[MAX_LENGTH];
			if(order == 3)
				count++;
			if(order == next && count != 1) { 
				writeFile.write(receive,2,packet1.getLength() - 1);
				System.out.println("������д���ļ��İ��ǣ�"+receive[0]);
					send[1] =  next; 
					next = (byte) (order + 1);  
			}
			else { 
				send[1] =  (byte) (next - 1);
			}
			if(ack == lastSeq) {
				base = (byte) (base + 1);//������ǰ�ƶ�
				if(buffer.size() > 0)
					buffer.remove(0); 
				if(base != seq)
					socket.setSoTimeout(TIMEOUT); 
			}
			if(seq < (base + WIND_SIZE)){
				int len = 0;
				if(tag) {
					System.out.println("send�ĳ��ȣ�"+send.length);
					len = readFile.read(send,2,send.length - 2);
				}
				else{
					send = buffer.get(seq - base);
					len = send.length;
				}
				if(len == -1) {
					System.out.println("��������ݴ������");
					len = 0;
				}
				send[0] = seq;
				if(seq == (byte) (lastSeq - 1))
					tag = true;
				buffer.add(send);
				lastSeq = seq;
				if(base == seq)
					socket.setSoTimeout(TIMEOUT);
				inetAddress = packet1.getAddress();
				port = packet1.getPort();
				DatagramPacket packet2 = new DatagramPacket(send,0,len+1,inetAddress,port);
				socket.send(packet2);
				System.out.println("�������ش���ack�ǣ�"+send[1]+"���͵�"+send[0]+"�����ݰ�");
				seq++;
			}  
		} catch(SocketTimeoutException e){
			System.out.println("��������ʱ�ط�");
			tag = false;
			seq = base;
		} catch (IOException e) {
			e.printStackTrace();
		} 
		 
	}
	}
}
