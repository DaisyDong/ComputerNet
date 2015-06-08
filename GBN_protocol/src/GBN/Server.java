package GBN;
import java.net.*;
import java.io.*;

public class Server extends Thread{
	public static DatagramSocket socket;
	public static final int MAX_LENGTH = 1025;//最大的数据量
	public static final int WINDOW_SIZE = 10;//最大的序列号
	public static byte[] receive = new byte[MAX_LENGTH];
	public static byte[] send = new byte[MAX_LENGTH];
	public static byte next = 0;	//下一个期待的序列号
	public static OutputStream writeFile;
	public static InetAddress inetAddress;
	public static int port;
	
	public Server() { 
		try {
			socket = new DatagramSocket(8888);
			next = 0;
			writeFile = new FileOutputStream("receive.txt");
		} catch (SocketException | FileNotFoundException e) { 
			e.printStackTrace();
		}
	}
	
	public void run() {
		int count = 0;
		while(true) {
		try {
			DatagramPacket packet1 = new DatagramPacket(receive,receive.length); 
			socket.receive(packet1);
			System.out.println("收到第"+receive[0]+"个数据包,此时期待的是："+next);
			byte order = receive[0]; //窗口的最大尺寸是10 
			if(order == 3)
				count++;
			if(order == next && count != 1) { 
				writeFile.write(receive,1,packet1.getLength() - 1);
				System.out.println("写到文件的包是："+receive[0]);
					send[0] =  next;   
					next = (byte) (order + 1);  
			}
			else { 
				send[0] =  (byte) (next - 1);
			}
			System.out.println("回传的ack是："+send[0]);
			inetAddress = packet1.getAddress();
			port = packet1.getPort();
			DatagramPacket packet2 = new DatagramPacket(send,send.length,inetAddress,port);
			socket.send(packet2); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	}
}
