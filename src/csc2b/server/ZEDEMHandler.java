package csc2b.server;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ZEDEMHandler extends Thread{

	private DataInputStream din = null;
	private DataOutputStream dos = null;
	
	public ZEDEMHandler(Socket connection) {
		
		try {
			// create input and output streams
			din = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
			dos = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	@Override
	public void run() {
		try {		
			String Request = din.readUTF();
			
			switch(Request)
			{
			case "BONJOUR":
			{
				//get password and username
				String username = din.readUTF();
				String password = din.readUTF();
				
				boolean loginAttempt = matchUser(username,password);//return false if user does not have an account
				dos.writeBoolean(loginAttempt);
				dos.flush();
				
				// send login failure if user does not exist
				if(!loginAttempt)
				{
					dos.writeUTF("NEE Incorrect Password/Username");
					dos.flush();
				}
				break;
			}
			case "PLAYLIST":
			{
				ArrayList<String> list = getFileList();
				String tobesent = "";
				for(String item : list)
				{
					tobesent += item+"\n";
				}
				
				dos.writeUTF(tobesent);
				dos.flush();
				
				break;
			}
			case "ZEDEMGET":
			{
				String ID = din.readUTF();
				String filename = idToFileName(ID);
				
				//check if file is found
				if(filename.equals(""))
				{
					dos.writeUTF("NEE File Not Found");
					dos.flush();
				}
				else
				{
					dos.writeUTF("JA Transmitting file");
					dos.writeUTF(filename);
					dos.flush();
				}
				
				
				File file = new File("data/server",filename);
				FileInputStream fis = new FileInputStream(file);
				
				long filesize = file.length();
				dos.writeLong(filesize);
				dos.flush();
				
				while(fis.available()>0)
				{
					dos.write(fis.readAllBytes());
				}
				
				dos.flush();
				fis.close();
				break;
			}
			}		
		}catch(FileNotFoundException e)
		{
			System.err.println("File Requested does not exist");
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private boolean matchUser(String userN, String passW)
	{
		boolean found = false;
		
		//Code to search users.txt file for match with userN and passW.
		File userFile = new File("data/server","users.txt");
		try
		{
		    Scanner scan = new Scanner(userFile);
		    while(scan.hasNextLine()&&!found)
		    {
				String line = scan.nextLine();
				String lineSec[] = line.split(" ");
		    		
				//***OMITTED - Enter code here to compare user*** 
				if(passW.equals(lineSec[1]) && userN.equals(lineSec[0])) 
				{
					found = true;
				}
		    }
		    
		    scan.close();
		}
		catch(IOException ex)
		{
		    ex.printStackTrace();
		}
		
		return found;
	}
	
	private ArrayList<String> getFileList()
	{
		ArrayList<String> result = new ArrayList<String>();
		
		//Code to add list text file contents to the arrayList.
		
		File lstFile = new File("data/server","List.txt");
		try
		{
			Scanner scan = new Scanner(lstFile);

			//***OMITTED - Read each line of the file and add to the arraylist***
			while(scan.hasNext())
			{
				String line = scan.nextLine();
				result.add(line);
			}
			
			scan.close();
		}	    
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		
		return result;
	}
	
	private String idToFileName(String strID)
	{
		String result ="";
		
		//Code to find the file name that matches strID
		File lstFile = new File("data/server","List.txt");
    	try
    	{
    		Scanner scan = new Scanner(lstFile);
    		//***OMITTED - Read filename from file and search for filename based on ID***
    		while(scan.hasNext())
    		{
    			String[] tokens = scan.nextLine().split(" ");
    			if(tokens[0].equals(strID))
				{
					result = tokens[1];
					break;
				}
    		}
    		
    		scan.close();
    	}
    	catch(IOException ex)
    	{
    		ex.printStackTrace();
    	}
		return result;
	}
}
