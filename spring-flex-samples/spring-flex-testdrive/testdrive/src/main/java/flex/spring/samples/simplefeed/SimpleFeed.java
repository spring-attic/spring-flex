package flex.spring.samples.simplefeed;

import java.util.Random;
import org.springframework.flex.messaging.MessageTemplate;

public class SimpleFeed {

	private static FeedThread thread;
	private MessageTemplate template;

	public SimpleFeed(MessageTemplate template) {
		this.template = template;
	}

	public void start() {
		if (thread == null) {
			thread = new FeedThread(template);
			thread.start();
		}
	}

	public void stop() {
		thread.running = false;
		thread = null;
	}

	public static class FeedThread extends Thread {

		public boolean running = false;

		private MessageTemplate template;
		
		public FeedThread(MessageTemplate template) {
			this.template = template;
		}
		
		public void run() {
			running = true;
			Random random = new Random();
			double initialValue = 35;
			double currentValue = 35;
			double maxChange = initialValue * 0.005;

			while (running) {
				double change = maxChange - random.nextDouble() * maxChange * 2;
				double newValue = currentValue + change;

				if (currentValue < initialValue + initialValue * 0.15
						&& currentValue > initialValue - initialValue * 0.15) {
					currentValue = newValue;
				} else {
					currentValue -= change;
				}

				template.send("simple-feed", new Double(currentValue));

				System.out.println("" + currentValue);

				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
				}

			}
		}
	}
	
}
