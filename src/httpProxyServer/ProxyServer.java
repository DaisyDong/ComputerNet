package httpProxyServer;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * http代理服务器，监听客户端的请求，向服务器端请求信息，实现客户端和服务端之间的代理服务
 * @author User
 *
 */

public class ProxyServer {
//	public static List<String> requestMessage = new ArrayList<String>();
//	public static List<String> resposeMessage = new ArrayList<String>();
	
	public static void main(String args[]) {
		try{
			//创建serverSocket监听来自服务器的请求
			ServerSocket serverSocket = new ServerSocket(8080);
			Socket socket = null;
			System.out.println("*****服务代理启动，等待客户端的请求*****"); 
			while(true) {
				socket = serverSocket.accept();
				C_P_BThread C_P_Bthread = new C_P_BThread(socket);	//创建客户端通过代理请求服务器信息的线程
				//启动线程
				C_P_Bthread.start();
			} 
		}catch(Exception e) {
			e.printStackTrace();
		} 
	}
	
}
