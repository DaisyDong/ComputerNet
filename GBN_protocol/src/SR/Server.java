package SR;
import java.net.*;
import java.io.*;

public class Server extends Thread{
	public static DatagramSocket socket;
	public static final int MAX_LENGTH = 1025;//最大的数据量
	public static final int WIND_SIZE = 10;//最大的序列号
	public static byte[] receive;
	public static byte[] send = new byte[MAX_LENGTH];
	public static byte next = 0;	//下一个期待的序列号
	public static byte[][] buffer = new byte[MAX_LENGTH][MAX_LENGTH];
	public static boolean[] ack = new boolean[MAX_LENGTH];
	public static OutputStream writeFile;
	public static InetAddress inetAddress;
	public static int port;
	
	public Server() { 
		try {
			socket = new DatagramSocket(8888);
			next = 0;
			writeFile = new FileOutputStream("receive_SR.txt");
		} catch (SocketException | FileNotFoundException e) { 
			e.printStackTrace();
		}
	}
	
	public void run() { 
		int count = 0;
		int num = 0; 
		int len[] = new int[MAX_LENGTH];
		while(true) {
		try {
			receive = new byte[MAX_LENGTH];
			DatagramPacket packet1 = new DatagramPacket(receive,receive.length); 
			socket.receive(packet1); 
			System.out.println("收到第"+receive[0]+"个数据包,此时期待的是："+next);
			byte order = receive[0]; //窗口的最大尺寸是10
			count++;	//模拟丢失
			if(next != order && count != 1 && count != 15) {
				System.out.println("期待的不是所需要的写入缓存，此时num为："+num+"  order:"+order);
				buffer[order] = receive;
				len[order]  = packet1.getLength(); 
				num++;
			}
			else if(next == order && count != 1 && count != 15){
				if(num > 0 ){	//所有都收到即缓冲区中有内容就将缓冲的写入文件
					buffer[order] = receive;
					len[order] = packet1.getLength();
					for(int i = 0;i <= num;i++){
						next++;
						System.out.println("将缓冲中的东西写入文件，数据包："+buffer[i][0]+"期待的next是："+next);
						writeFile.write(buffer[i],0,len[i] - 1);
					}
					num = 0;
				}
				else {
					next++;
					writeFile.write(receive,0,packet1.getLength() - 1);
					System.out.println("将缓冲中的东西写入文件2，数据包："+receive[0]+"期待的next是："+next);
				}
			}
			if(count != 1 && count != 15)
				send[0] = receive[0];
			else send[0] = (byte) (next - 1);
			if(order == 3)
				count++; 
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
