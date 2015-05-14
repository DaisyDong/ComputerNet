package httpProxyServer;
import java.net.*;
import java.io.*;

public class Proxy {
	public static void main(String args[]) {
		try {
			ServerSocket proxy = new ServerSocket(8888);
			Socket socket = proxy.accept();
//			Socket proxySocket = new Socket("www.renren.com",80);
//			OutputStream os = socket.getOutputStream();
//			PrintWriter pw = new PrintWriter(os);
			InputStream is = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			String info = br.readLine();
			while(br != null) {
				System.out.println(info); 
				info = br.readLine();
			}
			socket.shutdownInput();
			br.close();
			is.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
