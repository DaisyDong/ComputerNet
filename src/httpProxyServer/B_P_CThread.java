package httpProxyServer;
import java.io.*;
import java.net.*;
/**
 * 实现代理向服务器请求消息，服务器将消息传回，比较本地缓存消息是否为最新等功能
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
