package csc2b.client;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application {

	public static void main(String[] args) {
		launch();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
	
		ZEDEMClientPane root = new ZEDEMClientPane();
		
		Scene scene = new Scene(root,600,500);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);

		//set timer to show main stage if user is logged in
		AnimationTimer timer = new AnimationTimer() {	
			@Override
			public void handle(long now) {
				// TODO Auto-generated method stub
				if(root.getState())
				{
					primaryStage.show();
					this.stop();
				}
				else
				{
					primaryStage.close();
				}
			}
		};	
		timer.start();
	}

}
