package rdt;
import java.net.*;
import java.io.*;

public class Client extends Thread{
	public static int MAX_LENGTH = 1024;	//ÿ�ζ�ȡ�ļ��ĵ�����ֽ���
	public static final int TIMEOUT = 3000; //���ó�ʱʱ��	 
	public static byte[] receive = new byte[MAX_LENGTH];
	public static DatagramSocket socket;
	public static InputStream inputFile = null; //�����ȡ��������
	public static byte order;
	public static InetAddress inetAddress;
	public static int port;
	 
	public Client () {
		 try { 
			 socket = new DatagramSocket();
			 socket.setSoTimeout(TIMEOUT);
			inputFile = new FileInputStream("read.txt");
			order = 0;	//�տ�ʼ���͵���0���ݰ�
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
		 //�������ݰ�
		while(true) {  
			int len;
			try { 
				byte[] sendata = new byte[MAX_LENGTH];
				sendata[0] = order; 
				len = inputFile.read(sendata,1,sendata.length-1);
				count++;
				System.out.println(len);
				if(len == -1)	//�ļ��Ѿ��������
					break;
				while(len != -1) { 
					try{
					DatagramPacket packet = new DatagramPacket(sendata,len,inetAddress,port);
					socket.send(packet); 
					System.out.println("���͵�"+count+"�����ݱ�");
					DatagramPacket packet2 = new DatagramPacket(receive,receive.length);
					socket.receive(packet2);
					byte ack = receive[0];
					System.out.println("����ȥ�İ��ǣ�"+order+"��������ACK�ǣ�"+ack);
					if(ack == order) {
						order = (byte) ((order==0)?1:0);
						break;	//ת����һ��ת̬
					} 
					//�����ش� 
					}catch(SocketTimeoutException e) {
						//��ʱ����Ҫ�ش�
						System.out.println("��ʱ���ش�");
					}
				} 
			} catch (IOException e) { 
				e.printStackTrace();
			} 
			}
		}
	} 
