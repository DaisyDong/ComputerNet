package test;
import java.net.*;
import java.io.*;

public class test {
	public static void main(String args[]) { 
		try {
			Socket socket = new Socket("www.renren.com",80);
			InputStream is = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			OutputStream os = socket.getOutputStream();
			PrintWriter pw = new PrintWriter(os);
			
			pw.write("GET /a72842/n/core/base-all2.js HTTP/1.1\r\n");
			pw.write("Host: s.xnimg.cn\r\n");
			pw.write("If-modified-since: Wed, 12 Nov 2014 03:36:04 GMT\r\n");
			pw.write("\r\n");
			pw.flush();
			
			String info = new String();
			info = br.readLine();
			while(info != null) {
				System.out.println(info);
				if(info.contains("Last-Modified"))
					break;
				info = br.readLine(); 
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	 
	}
}
