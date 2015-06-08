package GBN;

public class startSingle {
	public static void main(String args[]) {
		Server server = new Server();
		server.start();
		Client client = new Client();
		client.start(); 
	}
}
