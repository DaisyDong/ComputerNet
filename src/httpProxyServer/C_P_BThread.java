package httpProxyServer;
import java.io.*;
import java.net.*;
import java.util.*;
/**
 * �����̣߳�ʵ�ֽ��ͻ��˵�����ͨ������������ķ����ͨ��
 * @author User
 *
 */
public class C_P_BThread extends Thread {
	 public Socket socket = null;
	 //�ͻ��˵�������Ϣ
	 public static List<String> requestMessage = new ArrayList<String>(); 
	 //����������л���ķ���˵���Ϣ
	 public static List<String> proxyMessage = new ArrayList<String>();
	 //����������ӷ���˵ĵ�����Ϣ
	 public static List<String> resposeMessage = new ArrayList<String>();
	 
	 
	 public C_P_BThread(Socket socket) {
		 this.socket = socket;
	 }
	 //�߳�ִ�еĲ�������Ӧ�ͻ��˵������ҽ�����Ӧ����
	 public void run() {
		 InputStream is = null;
		 InputStreamReader isr = null;
		 BufferedReader br = null;
		 try{
			 is = socket.getInputStream();
			 isr = new InputStreamReader(is);
			 br = new BufferedReader(isr);
			 String info = br.readLine();
			 //���ݿͻ��˵�������Ϣ�õ���ص�http request message
			 while(info != null) {
				 requestMessage.add(info);
				 System.out.println(info);
				 info = br.readLine();
			 }
			 //����http request �ĸ�ʽ��������Ӧ�Ķ���
			 
			 InetAddress inetAddress = socket.getInetAddress();
			 System.out.println("�ͻ�������ĵ�ַ�ǣ�"+"w.sugg.sogou.com");
			 //�����µ�socketȥ�������ķ�����������Ϣ
			 Socket proxySocket = new Socket("finance.services.appex.bing.com",80);
			 OutputStream os = proxySocket.getOutputStream();
			 OutputStreamWriter ow = new OutputStreamWriter(os);	//��װΪ��ӡ�� 
			 //���ݿͻ��˵�������������������û�л���õģ����Դ��������Ϊ�ͻ����������������Ϣ
			 ow.write(requestMessage.get(0));
			 ow.write(requestMessage.get(8));
			 B_P_CThread B_P_Cthread = new B_P_CThread(proxySocket,ow);
			 B_P_Cthread.start();
			 
		 }catch(Exception e) {
			 e.printStackTrace();
		 }
	 }
}
