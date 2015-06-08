package SR;

public class StartSR {
	public static void main(String args[]) {
		Server server = new Server();
		server.start();
		Client client = new Client();
		client.start();
	}
}
