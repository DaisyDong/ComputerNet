package test;

import java.io.*;
import java.net.*;
import java.util.*;

public class MyHttpProxy extends Thread { 
	public static int CONNECT_RETRIES=5;	//尝试与目标主机连接次数
	public static int CONNECT_PAUSE=5;	//每次建立连接的间隔时间
	public static int TIMEOUT=50;	//每次尝试连接的最大时间
	public static int BUFSIZ=1024;	//缓冲区最大字节数
	public static boolean logging = false;	//是否记录日志
	public static OutputStream log_S=null;	//日志输出流
	public static OutputStream log_C=null;	//日志输出流
	public static OutputStream log_D=null;	//响应报文日志
	public static int count = -1;
	public static List<String> cacheInfo = new ArrayList<String>(); 
	// 与客户端相连的Socket
	protected Socket csocket;	
    public MyHttpProxy(Socket cs) { 
	csocket=cs;
	start(); 
    }
    public void writeLog(int c, int browser) throws IOException {
    	if(browser==1)  
    		log_C.write((char)c);
    	else if(browser==2)
    		log_S.write((char)c);
    	else
    		log_D.write((char)c);
    }

    public void writeLog(byte[] bytes,int offset, int len, int browser) throws IOException {
   	for (int i=0;i<len;i++) 
   		writeLog((int)bytes[offset+i],browser);
    }
    public void run(){
    	String buffer = "";		//读取请求头
    	String URL="";			//读取请求URL
    	String host="";			//读取目标主机host
    	int port=80;			//默认端口80
    	Socket ssocket = null;
         //cis为客户端输入流，sis为目标主机输入流
    	InputStream cis = null,sis=null;
    	BufferedReader cbr = null,sbr=null;	//转化为字符流读取便于比较
         //cos为客户端输出流，sos为目标主机输出流
    	OutputStream cos = null,sos=null;	
    	PrintWriter cpw = null,spw = null;//转化为字符流
       	try{
    		csocket.setSoTimeout(TIMEOUT);
    		cis=csocket.getInputStream();	//代理服务器作为服务器接受客户端的请求
    		cbr = new BufferedReader(new InputStreamReader(cis));
    		cos=csocket.getOutputStream();	//代理服务器作为服务器向客户端发出响应
    		cpw = new PrintWriter(cos);
    		buffer = cbr.readLine();	//获取首部行 
    	//抽取URL(<a href="http://www.baidu.com/">http://www.baidu.com/</a>)  	
   		//while(buffer != null) {
    	System.out.println("buffer:"+buffer);
   		URL=getRequestURL(buffer);		
	
		int n;
    	//抽取host
  		n=URL.indexOf("//");
 		if (n!=-1) 	
                		host=URL.substring(n+2);	// www.baidu.com/
  		n=host.indexOf('/');
   		if (n!=-1) 	
                  		host=host.substring(0,n);// www.baidu.com
    	    
    	// 分析可能存在的端口号
  		n=host.indexOf(':');
   		if (n!=-1) { 
   			port=Integer.parseInt(host.substring(n+1));
   			host=host.substring(0,n);
  		}
   		int retry=CONNECT_RETRIES;
   		while (retry--!=0) {
   			try {
   					System.out.println("端口号："+port+"主机："+host);
   					System.out.println("第一行是 "+retry+":"+buffer);
    				ssocket=new Socket(host,port);	//尝试建立与目标主机的连接
    				break;
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
                 		// 等待
   			Thread.sleep(CONNECT_PAUSE);
   		}
   		if(ssocket!=null){
   			ssocket.setSoTimeout(TIMEOUT);
   			sis=ssocket.getInputStream();	//代理服务器作为客户端接受响应
   			sbr=new BufferedReader(new InputStreamReader(sis));
   			sos=ssocket.getOutputStream();	//代理服务器作为客户端发出请求
   			spw=new PrintWriter(sos);
   			 
   			String modifTime = findCache(buffer);//在缓存中寻找是否之前已经缓存过这个url的信息
   			System.out.println("上一次修改的时间为："+modifTime);//
//			writeLog(buffer.getBytes(),0,buffer.length(),1);
//			writeLog(buffer.getBytes(),0,buffer.length(),3);
//			writeLog("\r\n".getBytes(),0,2,3);
			//之前没有缓存
   			if(modifTime == null) {
   				while(!buffer.equals("")) {
   					buffer += "\r\n";
   					spw.write(buffer);
   					writeLog(buffer.getBytes(),0,buffer.length(),1);
   					System.out.print(buffer);
   					buffer = cbr.readLine(); 
   				}
   				spw.write("\r\n");
				writeLog("\r\n".getBytes(),0,2,1);
   				spw.flush();
   				//读取服务器的响应信息
   				String info = null;
   				while(true) {
   				try{
   				info = sbr.readLine();
   				if(info != null) break;
   				}catch(SocketTimeoutException e){
   					e.printStackTrace();
   				}
   				}
   				while(!info.equals("")) {
   					info += "\r\n";
   					writeLog(info.getBytes(),0,info.length(),3);
   					writeLog(info.getBytes(),0,info.length(),2);
   					cpw.write(info);
   					info = sbr.readLine();
   				}
   				cpw.write("\r\n");
				writeLog("\r\n".getBytes(),0,2,3);
				writeLog("\r\n".getBytes(),0,2,2);
   				cpw.flush(); 
   			}
   			else {
   				buffer += "\r\n";
   				spw.write(buffer);
   				System.out.println("向服务器发送确认修改时间请求");
   				String str1 = "Host: "+host+"\r\n";
   				spw.write(str1);
   				String str = "If-modified-since: "+modifTime+"\r\n\r\n";
   				spw.write(str);
   				
   				String info = sbr.readLine(); 
   				if(info.contains("Not Modified")){
   					int j = 0;
   					System.out.println("使用缓存中的数据");
   					while(j < cacheInfo.size()) {
   						info = cacheInfo.get(j++);
   						info += "\r\n";
   						System.out.print(info);
   						cpw.write(info);
   					}
   					cpw.write("\r\n");
   					cpw.flush();
   				}
   				else {
   					System.out.println("有更新，使用新的数据");
   					while(!info.equals("")){
   					info += "\r\n";
   					cpw.write(info);
   					info = sbr.readLine();
   					}
   					cpw.write("\r\n");
   					cpw.flush();
   				}
   			}  
    }
   		//读取mark     
   			//if(cbr == null || buffer.equals("")) 
   				//System.out.println("bbb");
   			//b/uffer = cbr.readLine();
   		//}
       	}catch(Exception e) {
       		e.printStackTrace();
       	} 
    }
    public String getRequestURL(String buffer){ 
    	String[] tokens=buffer.split(" ");
    	String URL="";
    	if(tokens[0].equals("GET"))
    	for(int index=0;index<tokens.length;index++){
    		if(tokens[index].startsWith("http://")){
    			URL=tokens[index];
    			break;
    		}
    	}
    	return URL;    	
    }
    public void pipe(InputStream cis,InputStream sis,OutputStream sos,OutputStream cos){
    	try {
    	    int length;
    	    byte bytes[]=new byte[BUFSIZ];
    	    while (true) {
    	    	try {
    	    		if ((length=cis.read(bytes))>0) {	//读取客户端的请求转给服务器 
    	    			sos.write(bytes,0,length);
    	    			if (logging) writeLog(bytes,0,length,1); 	    			
    	    		}
    	    		else if (length<0)
    	    			break;
    	    	}
    	    	catch(SocketTimeoutException e){}
    	    	catch (InterruptedIOException e) { 
    	    		System.out.println("\nRequest Exception:");
    	    		e.printStackTrace();
    	    	}
    	    	try {
    	    		if ((length=sis.read(bytes))>0) {//接受服务器的响应回传给请求的客户端
    	    			cos.write(bytes,0,length);	//因为是按字节读取，所以将回车和换行符也传递过去了
    	    			if (logging){
    	    				writeLog(bytes,0,length,1);
    	    				writeLog(bytes,0,length,3);
    	    			}
    	    		} 
    	    	}
    	    	catch(SocketTimeoutException e){}
    	    	catch (InterruptedIOException e) {
    	    		System.out.println("\nResponse Exception:");
    		    	e.printStackTrace();
    	    	}
    	    }
    	} catch (Exception e0) {
    	    System.out.println("Pipe异常: " + e0);
    	}
    }
    public static  void startProxy(int port,Class clobj) { 
        try { 
            ServerSocket ssock=new ServerSocket(port); 
            while (true) { 
            Class [] sarg = new Class[1]; 
            Object [] arg= new Object[1]; 
            sarg[0]=Socket.class; 
            try { 
            java.lang.reflect.Constructor cons = clobj.getDeclaredConstructor(sarg); 
            arg[0]=ssock.accept(); 
            cons.newInstance(arg); // 创建HttpProxy或其派生类的实例 
            } catch (Exception e) { 
            Socket esock = (Socket)arg[0]; 
            try { esock.close(); } catch (Exception ec) {} 
            } 
            } 
        } catch (IOException e) { 
        System.out.println("\nStartProxy Exception:"); 
        e.printStackTrace(); 
        } 
        } 
        // 测试用的简单main方法 
        static public void main(String args[]) throws FileNotFoundException { 
        System.out.println("在端口8888启动代理服务器\n"); 
        OutputStream file_S=new FileOutputStream(new File("log_s.txt")); 
        OutputStream file_C=new FileOutputStream(new File("log_c.txt"));
        OutputStream file_D=new FileOutputStream(new File("log_d.txt"));
        MyHttpProxy.log_S=file_S; 
        MyHttpProxy.log_C=file_C;
        MyHttpProxy.log_D=file_D;	//直接存储相关URl对应的响应报文
        MyHttpProxy.logging=true; 
        MyHttpProxy.startProxy(8888,MyHttpProxy.class);
        }

        public String findCache(String head) {
        	String resul = null;
        	int count = 0;
        	try {
				InputStream file_S = new FileInputStream("log_s.txt"); 
				InputStream file_C = new FileInputStream("log_c.txt");
				String info = "";
				while(true){
	    			int c=file_C.read();
	    			if(c==-1) break;		//-1为结尾标志
	    			if(c=='\r'){
	    				file_C.read();
	    				break;//读入每一行数据
	    			}
	    			if(c=='\n')
	    				break;
	    			info=info+(char)c; 
	    		}
//				System.out.print("第一次得到："+info);
//				System.out.print("要找的是："+head);
				int m = 0;
				while(!info.equals("") || (m = file_C.read()) != -1) {
//					System.out.println("在寻找："+info);
//					System.out.println("要找的是："+head);
				if(info.equals(head)) {
					System.out.println("找到相同的了："+info);
					System.out.println("现在的count是："+count);
					String info1 = "";
					while(true){
		    			int c=file_S.read();
		    			if(c==-1) break;		//-1为结尾标志
		    			if(c=='\r'){
		    				file_S.read();
		    				break;//读入每一行数据
		    			}
		    			if(c == '\n') break;
		    			info1=info1+(char)c; 
		    		}
					while(count > 0) { 
						if(info1.contains("HTTP/1.1")) --count;
						while(true){
			    			int c=file_S.read();
			    			if(c==-1) break;		//-1为结尾标志
			    			if(c=='\r'){
			    				file_S.read();
			    				break;//读入每一行数据
			    			}
			    			if(c=='\n') break;
			    			info1=info1+(char)c; 
			    		}
					}
					while(count == 0 && !info1.equals("")) {
						cacheInfo.add(info1); 
						System.out.println("cache中有："+info1);
						if(info1.contains("Last-Modified:"))
							resul = info1.substring(16);
						info1 = "";
						while(true){
				   			int c=file_S.read();
				   			if(c==-1) break;		//-1为结尾标志
				   			if(c=='\r'){
				   				file_S.read();//把\n读取
				   				break;//读入每一行数据
				   			}
				   			if(c=='\n') break;
				    		info1=info1+(char)c; 
				    	}
						if(info1.contains("HTTP/1.1")) break;
					} 
					return resul;
				}
				else if(info.contains("HTTP/1.1")) ++count;
				if(info == "") info=info + (char)m;
				else info = "";
				while(true){
	    			int c=file_C.read();
	    			if(c==-1) break;		//-1为结尾标志
	    			if(c=='\r'){
	    				file_C.read();
	    				break;//读入每一行数据
	    			}
	    			if(c=='\n') break;
	    			info=info+(char)c; 
	    		}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
        	
        	return resul;
        }
     

}