package rdt;

public class StartRdt {
	public static void main(String args[]) {
		//����������
		Server server = new Server();
		server.start();
		//�����ͻ���
		Client client = new Client();
		client.start();
	}
	
}
