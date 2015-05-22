package rdt;
import java.net.*;
import java.io.*;

public class Server extends Thread{
	public static final int MAX_LENGTH = 1024;
	public static DatagramSocket socket;
	public static int last;	//上一次收到的包的编号
	public static byte[] receive = new byte[MAX_LENGTH];
	public static byte[] send = new byte[MAX_LENGTH];
	public static OutputStream writeFile;
	public static InetAddress inetAddress;
	public static int port;
	
	public Server() {
		try {
			socket = new DatagramSocket(8888);
			last = 1;	//因为第一个需要的是0号packet
			writeFile = new FileOutputStream("receive.txt"); 
			receive[0] = 1;
		} catch (SocketException e) { 
			e.printStackTrace();
		} catch (FileNotFoundException e) { 
			e.printStackTrace();
		}  
	}
	public void run() {
		int timeOut = 0;	//模拟最多超时的次数，之后恢复
		while(true) {
		try{
		DatagramPacket packet1 = new DatagramPacket(receive,receive.length);
		socket.receive(packet1);
		byte order = receive[0]; 
		byte need = (byte)((last==0)?1:0);
		System.out.println("收到的数据包是："+order+" 需要的是："+need);
		//收到的是需要的数据包，则写入文件，回传ack
		if(need == order) { 
			writeFile.write(receive, 1, packet1.getLength()-1);
			send[0] = need;
			last = order;
			System.out.println("回传的ack是："+need);  
			inetAddress = packet1.getAddress();
			port = packet1.getPort();
			System.out.println("主机名："+inetAddress.getHostName()+" port:"+port);
			DatagramPacket packet2 = new DatagramPacket(send,send.length,inetAddress,port);
			if(timeOut++ > 1){
				socket.send(packet2); 
			}
		}
		else{
			send[0] = order; 
			System.out.println("传回来的包不是想要的，丢弃。。");
			System.out.println("回传的ack是："+need);
			inetAddress = packet1.getAddress();
			port = packet1.getPort();
			System.out.println("主机名："+inetAddress.getHostName()+" port:"+port);
			DatagramPacket packet2 = new DatagramPacket(send,send.length,inetAddress,port);
			socket.send(packet2); 
		}
		} catch (IOException e) { 
			e.printStackTrace();
		}
	}
	}
}
