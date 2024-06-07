package application;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FXController {
	
	@FXML
	private Button start_btn;
	
	@FXML
	private ImageView currentFrame;
	
	private ScheduledExecutorService timer;
	private VideoCapture capture = new VideoCapture();
	private boolean cameraActive = false;

	private static int cameraId = 0;
	@FXML
	protected void StartCamera(ActionEvent event) {
		if (!this.cameraActive)
		{
			// start the video capture
			this.capture.open(cameraId);
			
			// is the video stream available?
			if (this.capture.isOpened())
			{
				this.cameraActive = true;
				
				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {
					
					@Override
					public void run()
					{
						// effectively grab and process a single frame
						Mat frame = grabFrame();
						// convert and show the frame
						Image imageToShow = Utils.mat2Image(frame);
						updateImageView(currentFrame, imageToShow);
					}
				};
				
				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				
				// update the button content
				this.start_btn.setText("Stop Camera");
			}
			else
			{
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		}
		else
		{
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.start_btn.setText("Start Camera");
			
			// stop the timer
			this.stopAcquisition();
		}
	}
	
		
		
		
		
		private Mat grabFrame()
		{
			// init everything
			Mat frame = new Mat();
			
			// check if the capture is open
			if (this.capture.isOpened())
			{
				try
				{
					// read the current frame
					this.capture.read(frame);
					
					// if the frame is not empty, process it
					if (!frame.empty())
					{
						Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
					}
					
				}
				catch (Exception e)
				{
					// log the error
					System.err.println("Exception during the image elaboration: " + e);
				}
			}
			
			return frame;
		}
		
		
		
		
		
		
		private void stopAcquisition()
		{
			if (this.timer!=null && !this.timer.isShutdown())
			{
				try
				{
					// stop the timer
					this.timer.shutdown();
					this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException e)
				{
					// log any exception
					System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
				}
			}
			
			if (this.capture.isOpened())
			{
				// release the camera
				this.capture.release();
			}
		}
	
		
		
		
		
		
		private void updateImageView(ImageView view, Image image)
		{
			Utils.onFXThread(view.imageProperty(), image);
		}

   

	
	
}
