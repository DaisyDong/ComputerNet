package SR;
import java.net.*;
import java.io.*;

public class Server extends Thread{
	public static DatagramSocket socket;
	public static final int MAX_LENGTH = 1025;//����������
	public static final int WIND_SIZE = 10;//�������к�
	public static byte[] receive;
	public static byte[] send = new byte[MAX_LENGTH];
	public static byte next = 0;	//��һ���ڴ������к�
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
			System.out.println("�յ���"+receive[0]+"�����ݰ�,��ʱ�ڴ����ǣ�"+next);
			byte order = receive[0]; //���ڵ����ߴ���10
			count++;	//ģ�ⶪʧ
			if(next != order && count != 1 && count != 15) {
				System.out.println("�ڴ��Ĳ�������Ҫ��д�뻺�棬��ʱnumΪ��"+num+"  order:"+order);
				buffer[order] = receive;
				len[order]  = packet1.getLength(); 
				num++;
			}
			else if(next == order && count != 1 && count != 15){
				if(num > 0 ){	//���ж��յ����������������ݾͽ������д���ļ�
					buffer[order] = receive;
					len[order] = packet1.getLength();
					for(int i = 0;i <= num;i++){
						next++;
						System.out.println("�������еĶ���д���ļ������ݰ���"+buffer[i][0]+"�ڴ���next�ǣ�"+next);
						writeFile.write(buffer[i],0,len[i] - 1);
					}
					num = 0;
				}
				else {
					next++;
					writeFile.write(receive,0,packet1.getLength() - 1);
					System.out.println("�������еĶ���д���ļ�2�����ݰ���"+receive[0]+"�ڴ���next�ǣ�"+next);
				}
			}
			if(count != 1 && count != 15)
				send[0] = receive[0];
			else send[0] = (byte) (next - 1);
			if(order == 3)
				count++; 
			System.out.println("�ش���ack�ǣ�"+send[0]);
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
