package httpProxyServer;
import java.io.*;
import java.net.*;
import java.util.*;
/**
 * 处理线程，实现将客户端的请求通过代理与请求的服务端通信
 * @author User
 *
 */
public class C_P_BThread extends Thread {
	 public Socket socket = null;
	 //客户端的请求信息
	 public static List<String> requestMessage = new ArrayList<String>(); 
	 //代理服务器中缓存的服务端的信息
	 public static List<String> proxyMessage = new ArrayList<String>();
	 //代理服务器从服务端的到的信息
	 public static List<String> resposeMessage = new ArrayList<String>();
	 
	 
	 public C_P_BThread(Socket socket) {
		 this.socket = socket;
	 }
	 //线程执行的操作，响应客户端的请求并且进行相应操作
	 public void run() {
		 InputStream is = null;
		 InputStreamReader isr = null;
		 BufferedReader br = null;
		 try{
			 is = socket.getInputStream();
			 isr = new InputStreamReader(is);
			 br = new BufferedReader(isr);
			 String info = br.readLine();
			 //根据客户端的请求信息得到相关的http request message
			 while(info != null) {
				 requestMessage.add(info);
				 System.out.println(info);
				 info = br.readLine();
			 }
			 //根据http request 的格式，解析相应的动作
			 
			 InetAddress inetAddress = socket.getInetAddress();
			 System.out.println("客户端请求的地址是："+"w.sugg.sogou.com");
			 //创建新的socket去向真正的服务器请求信息
			 Socket proxySocket = new Socket("finance.services.appex.bing.com",80);
			 OutputStream os = proxySocket.getOutputStream();
			 OutputStreamWriter ow = new OutputStreamWriter(os);	//包装为打印流 
			 //根据客户端的请求，如果代理服务器中没有缓存好的，就以代理服务器为客户端向服务器请求信息
			 ow.write(requestMessage.get(0));
			 ow.write(requestMessage.get(8));
			 B_P_CThread B_P_Cthread = new B_P_CThread(proxySocket,ow);
			 B_P_Cthread.start();
			 
		 }catch(Exception e) {
			 e.printStackTrace();
		 }
	 }
}
