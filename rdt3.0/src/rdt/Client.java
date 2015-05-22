package rdt;
import java.net.*;
import java.io.*;

public class Client extends Thread{
	public static int MAX_LENGTH = 1024;	//每次读取文件的的最大字节数
	public static final int TIMEOUT = 3000; //设置超时时间	 
	public static byte[] receive = new byte[MAX_LENGTH];
	public static DatagramSocket socket;
	public static InputStream inputFile = null; //从这读取传送数据
	public static byte order;
	public static InetAddress inetAddress;
	public static int port;
	 
	public Client () {
		 try { 
			 socket = new DatagramSocket();
			 socket.setSoTimeout(TIMEOUT);
			inputFile = new FileInputStream("read.txt");
			order = 0;	//刚开始发送的是0数据包
			inetAddress =  InetAddress.getByName("localhost");
			port = 8888;
		} catch (SocketException | FileNotFoundException e) { 
			e.printStackTrace();
		} catch (UnknownHostException e) { 
			e.printStackTrace();
		}	 
	}
	public void run() {
		int count = 0; 
		 //创建数据包
		while(true) {  
			int len;
			try { 
				byte[] sendata = new byte[MAX_LENGTH];
				sendata[0] = order; 
				len = inputFile.read(sendata,1,sendata.length-1);
				count++;
				System.out.println(len);
				if(len == -1)	//文件已经传送完毕
					break;
				while(len != -1) { 
					try{
					DatagramPacket packet = new DatagramPacket(sendata,len,inetAddress,port);
					socket.send(packet); 
					System.out.println("发送第"+count+"个数据报");
					DatagramPacket packet2 = new DatagramPacket(receive,receive.length);
					socket.receive(packet2);
					byte ack = receive[0];
					System.out.println("传出去的包是："+order+"传回来的ACK是："+ack);
					if(ack == order) {
						order = (byte) ((order==0)?1:0);
						break;	//转到下一个转态
					} 
					//否则重传 
					}catch(SocketTimeoutException e) {
						//超时，需要重传
						System.out.println("超时，重传");
					}
				} 
			} catch (IOException e) { 
				e.printStackTrace();
			} 
			}
		}
	} 
