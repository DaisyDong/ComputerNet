package httpProxyServer;

import java.io.*;
import java.net.*;

public class Client {
	public static void main(String args[]) {
		try{
			Socket socket = new Socket();
			OutputStream os = socket.getOutputStream();
			PrintWriter pw = new PrintWriter(os);
			pw.write("GET http://www.renren.com/ HTTP/1.1");
			pw.write("Accept:text/html,application/xhtml+xml, */*");
			pw.write("Accept-Language: zh-Hans-CN,zh-Hans;q=0.8,en-US;q=0.5,en;q=0.3");
			pw.write("User-Agent: Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.2; WOW64; Trident/6.0)");
			pw.write("Host:www.renren.com");
			pw.write("Connection:close"); 
			pw.flush();	//刷新缓存流，向服务端发送信息
			  
			InputStream is = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String info = br.readLine();
			while(info != null) {
				System.out.println(info);
				info = br.readLine();
			}
			socket.shutdownOutput();
			socket.shutdownInput();
			pw.close();
			os.close();
			br.close();
			is.close();
			socket.close(); 
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
