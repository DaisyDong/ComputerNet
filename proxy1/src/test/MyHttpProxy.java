package test;

import java.io.*;
import java.net.*;
import java.util.*;

public class MyHttpProxy extends Thread { 
	public static int CONNECT_RETRIES=5;	//������Ŀ���������Ӵ���
	public static int CONNECT_PAUSE=5;	//ÿ�ν������ӵļ��ʱ��
	public static int TIMEOUT=50;	//ÿ�γ������ӵ����ʱ��
	public static int BUFSIZ=1024;	//����������ֽ���
	public static boolean logging = false;	//�Ƿ��¼��־
	public static OutputStream log_S=null;	//��־�����
	public static OutputStream log_C=null;	//��־�����
	public static OutputStream log_D=null;	//��Ӧ������־
	public static int count = -1;
	public static List<String> cacheInfo = new ArrayList<String>(); 
	// ��ͻ���������Socket
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
    	String buffer = "";		//��ȡ����ͷ
    	String URL="";			//��ȡ����URL
    	String host="";			//��ȡĿ������host
    	int port=80;			//Ĭ�϶˿�80
    	Socket ssocket = null;
         //cisΪ�ͻ�����������sisΪĿ������������
    	InputStream cis = null,sis=null;
    	BufferedReader cbr = null,sbr=null;	//ת��Ϊ�ַ�����ȡ���ڱȽ�
         //cosΪ�ͻ����������sosΪĿ�����������
    	OutputStream cos = null,sos=null;	
    	PrintWriter cpw = null,spw = null;//ת��Ϊ�ַ���
       	try{
    		csocket.setSoTimeout(TIMEOUT);
    		cis=csocket.getInputStream();	//�����������Ϊ���������ܿͻ��˵�����
    		cbr = new BufferedReader(new InputStreamReader(cis));
    		cos=csocket.getOutputStream();	//�����������Ϊ��������ͻ��˷�����Ӧ
    		cpw = new PrintWriter(cos);
    		buffer = cbr.readLine();	//��ȡ�ײ��� 
    	//��ȡURL(<a href="http://www.baidu.com/">http://www.baidu.com/</a>)  	
   		//while(buffer != null) {
    	System.out.println("buffer:"+buffer);
   		URL=getRequestURL(buffer);		
	
		int n;
    	//��ȡhost
  		n=URL.indexOf("//");
 		if (n!=-1) 	
                		host=URL.substring(n+2);	// www.baidu.com/
  		n=host.indexOf('/');
   		if (n!=-1) 	
                  		host=host.substring(0,n);// www.baidu.com
    	    
    	// �������ܴ��ڵĶ˿ں�
  		n=host.indexOf(':');
   		if (n!=-1) { 
   			port=Integer.parseInt(host.substring(n+1));
   			host=host.substring(0,n);
  		}
   		int retry=CONNECT_RETRIES;
   		while (retry--!=0) {
   			try {
   					System.out.println("�˿ںţ�"+port+"������"+host);
   					System.out.println("��һ���� "+retry+":"+buffer);
    				ssocket=new Socket(host,port);	//���Խ�����Ŀ������������
    				break;
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
                 		// �ȴ�
   			Thread.sleep(CONNECT_PAUSE);
   		}
   		if(ssocket!=null){
   			ssocket.setSoTimeout(TIMEOUT);
   			sis=ssocket.getInputStream();	//�����������Ϊ�ͻ��˽�����Ӧ
   			sbr=new BufferedReader(new InputStreamReader(sis));
   			sos=ssocket.getOutputStream();	//�����������Ϊ�ͻ��˷�������
   			spw=new PrintWriter(sos);
   			 
   			String modifTime = findCache(buffer);//�ڻ�����Ѱ���Ƿ�֮ǰ�Ѿ���������url����Ϣ
   			System.out.println("��һ���޸ĵ�ʱ��Ϊ��"+modifTime);//
//			writeLog(buffer.getBytes(),0,buffer.length(),1);
//			writeLog(buffer.getBytes(),0,buffer.length(),3);
//			writeLog("\r\n".getBytes(),0,2,3);
			//֮ǰû�л���
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
   				//��ȡ����������Ӧ��Ϣ
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
   				System.out.println("�����������ȷ���޸�ʱ������");
   				String str1 = "Host: "+host+"\r\n";
   				spw.write(str1);
   				String str = "If-modified-since: "+modifTime+"\r\n\r\n";
   				spw.write(str);
   				
   				String info = sbr.readLine(); 
   				if(info.contains("Not Modified")){
   					int j = 0;
   					System.out.println("ʹ�û����е�����");
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
   					System.out.println("�и��£�ʹ���µ�����");
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
   		//��ȡmark     
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
    	    		if ((length=cis.read(bytes))>0) {	//��ȡ�ͻ��˵�����ת�������� 
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
    	    		if ((length=sis.read(bytes))>0) {//���ܷ���������Ӧ�ش�������Ŀͻ���
    	    			cos.write(bytes,0,length);	//��Ϊ�ǰ��ֽڶ�ȡ�����Խ��س��ͻ��з�Ҳ���ݹ�ȥ��
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
    	    System.out.println("Pipe�쳣: " + e0);
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
            cons.newInstance(arg); // ����HttpProxy�����������ʵ�� 
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
        // �����õļ�main���� 
        static public void main(String args[]) throws FileNotFoundException { 
        System.out.println("�ڶ˿�8888�������������\n"); 
        OutputStream file_S=new FileOutputStream(new File("log_s.txt")); 
        OutputStream file_C=new FileOutputStream(new File("log_c.txt"));
        OutputStream file_D=new FileOutputStream(new File("log_d.txt"));
        MyHttpProxy.log_S=file_S; 
        MyHttpProxy.log_C=file_C;
        MyHttpProxy.log_D=file_D;	//ֱ�Ӵ洢���URl��Ӧ����Ӧ����
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
	    			if(c==-1) break;		//-1Ϊ��β��־
	    			if(c=='\r'){
	    				file_C.read();
	    				break;//����ÿһ������
	    			}
	    			if(c=='\n')
	    				break;
	    			info=info+(char)c; 
	    		}
//				System.out.print("��һ�εõ���"+info);
//				System.out.print("Ҫ�ҵ��ǣ�"+head);
				int m = 0;
				while(!info.equals("") || (m = file_C.read()) != -1) {
//					System.out.println("��Ѱ�ң�"+info);
//					System.out.println("Ҫ�ҵ��ǣ�"+head);
				if(info.equals(head)) {
					System.out.println("�ҵ���ͬ���ˣ�"+info);
					System.out.println("���ڵ�count�ǣ�"+count);
					String info1 = "";
					while(true){
		    			int c=file_S.read();
		    			if(c==-1) break;		//-1Ϊ��β��־
		    			if(c=='\r'){
		    				file_S.read();
		    				break;//����ÿһ������
		    			}
		    			if(c == '\n') break;
		    			info1=info1+(char)c; 
		    		}
					while(count > 0) { 
						if(info1.contains("HTTP/1.1")) --count;
						while(true){
			    			int c=file_S.read();
			    			if(c==-1) break;		//-1Ϊ��β��־
			    			if(c=='\r'){
			    				file_S.read();
			    				break;//����ÿһ������
			    			}
			    			if(c=='\n') break;
			    			info1=info1+(char)c; 
			    		}
					}
					while(count == 0 && !info1.equals("")) {
						cacheInfo.add(info1); 
						System.out.println("cache���У�"+info1);
						if(info1.contains("Last-Modified:"))
							resul = info1.substring(16);
						info1 = "";
						while(true){
				   			int c=file_S.read();
				   			if(c==-1) break;		//-1Ϊ��β��־
				   			if(c=='\r'){
				   				file_S.read();//��\n��ȡ
				   				break;//����ÿһ������
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
	    			if(c==-1) break;		//-1Ϊ��β��־
	    			if(c=='\r'){
	    				file_C.read();
	    				break;//����ÿһ������
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