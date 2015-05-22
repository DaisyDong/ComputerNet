package rdt;

public class StartRdt {
	public static void main(String args[]) {
		//启动服务器
		Server server = new Server();
		server.start();
		//启动客户端
		Client client = new Client();
		client.start();
	}
	
}
