package httpProxyServer;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * http����������������ͻ��˵��������������������Ϣ��ʵ�ֿͻ��˺ͷ����֮��Ĵ������
 * @author User
 *
 */

public class ProxyServer {
//	public static List<String> requestMessage = new ArrayList<String>();
//	public static List<String> resposeMessage = new ArrayList<String>();
	
	public static void main(String args[]) {
		try{
			//����serverSocket�������Է�����������
			ServerSocket serverSocket = new ServerSocket(8080);
			Socket socket = null;
			System.out.println("*****��������������ȴ��ͻ��˵�����*****"); 
			while(true) {
				socket = serverSocket.accept();
				C_P_BThread C_P_Bthread = new C_P_BThread(socket);	//�����ͻ���ͨ�����������������Ϣ���߳�
				//�����߳�
				C_P_Bthread.start();
			} 
		}catch(Exception e) {
			e.printStackTrace();
		} 
	}
	
}
