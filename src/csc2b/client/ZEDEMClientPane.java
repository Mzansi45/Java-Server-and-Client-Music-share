package csc2b.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JOptionPane;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;



public class ZEDEMClientPane  extends HBox//You may change the JavaFX pane layout
{
	private DataInputStream din = null;
	private DataOutputStream dos = null;
	private Socket socket = null;
	private String username = "";
	private String password = "";
	private boolean loggedIn = false;
	
	/**
	 * 
	 * @return state of the music page. true if user is logged in
	 */
	public boolean getState()
	{
		return loggedIn;
	}
	
	/*
	 * this function is responsible for logging the user in
	 */
	public void login()
	{
		//login page JavaFX setup
		Stage loginPage = new Stage();	
		
		VBox page = new VBox();
		page.setSpacing(10);
		Insets v = new Insets(50);
		page.setPadding(v);	
		
		Label lblUsername = new Label("Username");
		TextField txtUsername = new TextField();
	
		Label lblPassword = new Label("Password");
		PasswordField txtPassword = new PasswordField();
		
		Button login = new Button("Login");
		
		Label error = new Label(); // for when user enters wrong username or password
		error.setVisible(false);
		error.textFillProperty().set(Color.RED);
		login.setOnAction(event->{
			
			password = txtPassword.getText();
			username = txtUsername.getText();
			
			if(password.equals("")||username.equals(""))
			{
				error.setVisible(true);
			}
			else
			{
				InitializeStreams();
				try {
					// send a request to server
					dos.writeUTF("BONJOUR");
					dos.flush();
				
					// send username and password for validation
					dos.writeUTF(username);
					dos.flush();
					dos.writeUTF(password);
					dos.flush();
					
					// server returns true if the client password and username is exists
					loggedIn = din.readBoolean();
					if(loggedIn)
					{
						// close login page and open main page
						loginPage.close();
					}
					else
					{
						//get error message from server
						String errorMessage = din.readUTF();
						error.setText(errorMessage);
						// if the password and username does not exist set error to visible
						error.setVisible(true);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {
					closeStreams();
				}
			}
		});
		
		// add all nodes to the 
		page.getChildren().addAll(lblUsername,txtUsername,lblPassword,txtPassword,login,error);
		Scene scene = new Scene(page,400,300,Color.GRAY);
		loginPage.setScene(scene);
		loginPage.setResizable(false);
		loginPage.show();
	}
	
	public ZEDEMClientPane(){	
		
		// Login page for user
		login();
		
		//Main client page setup
		Button playlist = new Button("PLAYLIST");
		Button getAudio = new Button("ZEDEMGET");
		Button bye = new Button("ZEDEMBYE");
		
		playlist.setMinSize(170, 40);
		getAudio.setMinSize(170, 40);
		bye.setMinSize(170, 40);
		
		VBox buttons = new VBox();
		buttons.setPadding(new Insets(30));
		buttons.setSpacing(10);
		buttons.getChildren().addAll(playlist,getAudio,bye);
		this.getChildren().add(buttons);
		
		VBox Content = new VBox();
		Content.setPadding(new Insets(30));
		
		TextArea content = new TextArea();
		content.setPrefHeight(400);  //sets height of the TextArea to 400 pixels 
		content.setPrefWidth(300);   //sets width of the TextArea to 300 pixels
		
		Content.getChildren().add(content);
		this.getChildren().add(Content);
		
		//getting play-list from the server
		playlist.setOnAction(even->{
			if(loggedIn)
			{
				InitializeStreams();
				
				try {
					dos.writeUTF("PLAYLIST");
					dos.flush();
					
					String list = din.readUTF(); // list of songs in the server
					
					String[] brokenList = list.split("\n"); // break list into array tokens
					content.setText("");
					for(String item : brokenList)
					{
						content.appendText(item +"\n"); //display content to Client
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally
				{
					closeStreams();
				}
			}
			else
			{
				content.setText("NEE You are LoggedOut");
			}	
		});
		
		//getting songs from server
		
		getAudio.setOnAction(event->{
			if(loggedIn)
			{
				InitializeStreams();
				try {
					dos.writeUTF("ZEDEMGET");
					dos.flush();
					
					String ID = JOptionPane.showInputDialog("Enter ID of song you wish to get");
					dos.writeUTF(ID);
					dos.flush();
					String response = din.readUTF();
					if(response.startsWith("JA"))
					{
						String filename = din.readUTF();						
						content.setText(response);
						
						File file = new File("data/client",filename);
						FileOutputStream fos = new FileOutputStream(file);
					
						long fileSize = din.readLong();
						byte[] buffer = new byte[1024];
						int n=0;
						int totalbytes = 0;
						while(totalbytes!=fileSize)
						{
							n= din.read(buffer,0, buffer.length);
							fos.write(buffer,0,n);
							fos.flush();
							totalbytes+=n;
						}
						
						fos.close();
						content.setText(response +".................................\n"+filename + "  Received");
					}else
					{
						content.setText(response);
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
					closeStreams();
				}	
			}
			else
			{
				content.setText("NEE You Are LoggedOut");
			}		
		});
		
		bye.setOnAction(event->{
			if(loggedIn)
			{
				loggedIn = false;
				login();
			}
			else
			{
				content.setText("NEE Please Login");
			}
		});
	}
	
	/**
	 * this function initializes all byte streams
	 */
	public void InitializeStreams()
	{		
		try {
			socket = new Socket("localhost",2021);
			din = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	/**
	 * this function closes all open byte streams
	 */
	public void closeStreams()
	{
		try {
			if(socket!=null)
			{
				socket.close();
			}
			if(din !=null)
			{
				din.close();
			}
			if(dos!=null)
			{
				dos.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
