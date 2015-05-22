package rdt;
import java.net.*;
import java.io.*;

public class Server extends Thread{
	public static final int MAX_LENGTH = 1024;
	public static DatagramSocket socket;
	public static int last;	//��һ���յ��İ��ı��
	public static byte[] receive = new byte[MAX_LENGTH];
	public static byte[] send = new byte[MAX_LENGTH];
	public static OutputStream writeFile;
	public static InetAddress inetAddress;
	public static int port;
	
	public Server() {
		try {
			socket = new DatagramSocket(8888);
			last = 1;	//��Ϊ��һ����Ҫ����0��packet
			writeFile = new FileOutputStream("receive.txt"); 
			receive[0] = 1;
		} catch (SocketException e) { 
			e.printStackTrace();
		} catch (FileNotFoundException e) { 
			e.printStackTrace();
		}  
	}
	public void run() {
		int timeOut = 0;	//ģ����೬ʱ�Ĵ�����֮��ָ�
		while(true) {
		try{
		DatagramPacket packet1 = new DatagramPacket(receive,receive.length);
		socket.receive(packet1);
		byte order = receive[0]; 
		byte need = (byte)((last==0)?1:0);
		System.out.println("�յ������ݰ��ǣ�"+order+" ��Ҫ���ǣ�"+need);
		//�յ�������Ҫ�����ݰ�����д���ļ����ش�ack
		if(need == order) { 
			writeFile.write(receive, 1, packet1.getLength()-1);
			send[0] = need;
			last = order;
			System.out.println("�ش���ack�ǣ�"+need);  
			inetAddress = packet1.getAddress();
			port = packet1.getPort();
			System.out.println("��������"+inetAddress.getHostName()+" port:"+port);
			DatagramPacket packet2 = new DatagramPacket(send,send.length,inetAddress,port);
			if(timeOut++ > 1){
				socket.send(packet2); 
			}
		}
		else{
			send[0] = order; 
			System.out.println("�������İ�������Ҫ�ģ���������");
			System.out.println("�ش���ack�ǣ�"+need);
			inetAddress = packet1.getAddress();
			port = packet1.getPort();
			System.out.println("��������"+inetAddress.getHostName()+" port:"+port);
			DatagramPacket packet2 = new DatagramPacket(send,send.length,inetAddress,port);
			socket.send(packet2); 
		}
		} catch (IOException e) { 
			e.printStackTrace();
		}
	}
	}
}
