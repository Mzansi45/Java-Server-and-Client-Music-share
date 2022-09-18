package csc2b.server;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
	
	private static ServerSocket connection = null;
	public static void main(String[] args)
	{
		try {
			connection = new ServerSocket(2021);
			while(true)
			{	
				System.out.println("waiting for connections on port: "+ 2021);
				//start a thread for each connection
				ZEDEMHandler thread = new ZEDEMHandler(connection.accept());
				thread.start();	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
