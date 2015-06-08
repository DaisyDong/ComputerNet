package SR;

import java.net.*;
import java.util.*;
import java.io.*;

public class Client extends Thread {
	public static DatagramSocket socket;
	public static final int MAX_LENGTH = 1025;
	public static final int MAX_SEQ = 256;
	public static final int TIMEOUT = 3000;
	public static final int WIND_SIZE = 10;
	public static byte[] send;
	public static byte[] receive = new byte[MAX_LENGTH];
	public static byte seq = 0;
	public static boolean[] ack = new boolean[WIND_SIZE];
	public static InetAddress inetAddress;
	public static int port;
	public static byte base;
	public static InputStream fileRead;
	public byte[][] buffer;

	public Client() {
		try {
			socket = new DatagramSocket();
			inetAddress = InetAddress.getByName("localhost");
			port = 8888;
			fileRead = new FileInputStream("send.txt");
			buffer = new byte[WIND_SIZE][MAX_LENGTH];
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
		int len1 = 0;
		boolean tag = true;
		boolean tag1 = false;
		while (true) {
			try {
				int len = 0;
				if (seq < base + WIND_SIZE && tag) {
					send = new byte[MAX_LENGTH];
					len = fileRead.read(send, 1, send.length - 1);
					System.out.println("���ļ��ж�ȡ��");
					send[0] = (byte) seq;
					tag1 = true;
				} else if(!tag){ // �ǳ�ʱ���������Ҫ�ط������
					int i = 0;
					for (i = 0; i < WIND_SIZE; i++) {
						if (ack[i]) {
							send = buffer[i];
							System.out.println("�ǳ�ʱ,��Ҫ�ط����ݰ���"+send[0]);
							len = send.length;
							ack[i] = false; 
							break;
						} 
					}
					if (i == WIND_SIZE) { 
						System.out.println("û��Ҫ�ش������ݰ���");
						buffer = new byte[WIND_SIZE][MAX_LENGTH];
						ack = new boolean[WIND_SIZE];
						len1 = 0; 
						tag = true;
						base = seq;
						send = new byte[MAX_LENGTH];
						len = fileRead.read(send, 1, send.length - 1);
						System.out.println("���ļ��ж�ȡ��");
						send[0] = (byte) seq;
					}
					tag1 = true;
				}
				if (len == -1) {
					System.out.println("�ļ�������");
					break;
				}
				if (len != -1 && tag1) {
					tag1 = false;
					if (base == seq) {
						socket.setSoTimeout(TIMEOUT); // ������ʱ��
					}
					DatagramPacket packet1 = new DatagramPacket(send, 0,
							len - 1, inetAddress, port);
					System.out.println("���͵�" + send[0] + "�����ݰ�,��ʱseq��base�ֱ�Ϊ��"
							+ base + "seq:" + seq);
					socket.send(packet1);
					if(tag){	//�����ǵ�һ�δ���ʱ��
						buffer[len1] = send;
						ack[len1] = true; // ��ʾҪ�ȴ������ack��
//						System.out.println("Ҫ�ȴ�"+len1+"��ack");
						len1++;
						seq += 1;
					} 
				}
				// datagram �յ����ͻ�����������ʱ�����Զ�ֹͣ
				DatagramPacket packet2 = new DatagramPacket(receive, 0, 1);
				socket.receive(packet2);
//				System.out.println("�յ�ACK�ǣ�" + receive[0]);
				if (base == receive[0]) {
					base = (byte) (base + 1);// ������ǰ�ƶ�
//					for (int i = 0; i < buffer.length - 1; i++) {
//						buffer[i] = buffer[i + 1];
//						ack[i] = ack[i + 1];
//						len1--;
//					}
					len1--;
					if (base != seq)
						socket.setSoTimeout(TIMEOUT);
				} else if (base != receive[0]) {
					int len2 = receive[0] - base; // ��ʾ�Ѿ��յ�ack
					if(len2 > 0)
						ack[len2] = false; 
				} 
			} catch (SocketTimeoutException e) {
				System.out.println("��ʱ�ط�,ȷ�ϵ�: " + base);
				tag = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
