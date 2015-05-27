package test;
import java.net.*;
import java.io.*;

public class test {
	public static void main(String args[]) { 
		try {
			Socket socket = new Socket("icode.renren.com",80);
			InputStream is = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			OutputStream os = socket.getOutputStream();
			PrintWriter pw = new PrintWriter(os);
			
			pw.write("GET http://ebp.renren.com/ebpn/show?ref=http://www.renren.com/ad_100000000061&r=http%3A%2F%2Fwww.renren.com%2FSysHome.do HTTP/1.1\r\n");
			pw.write("Host: ebp.renren.com\r\n");
			pw.write("If-modified-since: Tue, 06 Mar 2012 08:41:41 GMT\r\n");
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
