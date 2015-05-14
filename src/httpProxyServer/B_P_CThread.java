package httpProxyServer;
import java.io.*;
import java.net.*;
/**
 * ʵ�ִ����������������Ϣ������������Ϣ���أ��Ƚϱ��ػ�����Ϣ�Ƿ�Ϊ���µȹ���
 * @author User
 *
 */
public class B_P_CThread extends Thread {
	Socket socket = null;
	OutputStreamWriter ow= null;
	public B_P_CThread(Socket socket,OutputStreamWriter ow) { 
		try{
			this.socket = socket;
			this.ow = ow;
			ow.flush();
			InputStream is= socket.getInputStream(); 
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String info = br.readLine();
			while(info != null) {
				System.out.println(info);
				info = br.readLine();
			}
		} catch (IOException e) { 
			e.printStackTrace();
		}
	}
	
}
