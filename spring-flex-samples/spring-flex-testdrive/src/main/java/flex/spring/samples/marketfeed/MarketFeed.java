package flex.spring.samples.marketfeed;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import flex.messaging.MessageBroker;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.util.UUIDUtils;

public class MarketFeed {

	private static FeedThread thread;

	private List<Stock> stockList;
	
	public MarketFeed(Resource filePath) throws IOException {
		stockList = getStocks(filePath.getFile());
	}

	public void start() {
		if (thread == null) {
			thread = new FeedThread(stockList);
			thread.start();
		}
	}

	public void stop() {
		thread.running = false;
		thread = null;
	}
	
	private List<Stock> getStocks(File file) {
		
		List<Stock> list = new ArrayList<Stock>();
		
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            Document doc = factory.newDocumentBuilder().parse(file);
            NodeList stockNodes = doc.getElementsByTagName("stock");
            int length = stockNodes.getLength();
            Stock stock;
            Node stockNode;
            for (int i=0; i<length; i++) {
            	stockNode = stockNodes.item(i);
            	stock = new Stock();
            	stock.setSymbol( getStringValue(stockNode, "symbol") );
            	stock.setName( getStringValue(stockNode, "company") );
            	stock.setLast( getDoubleValue(stockNode, "last") );
            	stock.setHigh( stock.getLast() );
            	stock.setLow( stock.getLast() );
            	stock.setOpen( stock.getLast() );
            	stock.setChange( 0 );
            	list.add(stock);
            	System.out.println(stock.getSymbol());
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }

        return list;
	}
	
	private String getStringValue(Node node, String name) {
		return ((Element) node).getElementsByTagName(name).item(0).getFirstChild().getNodeValue();		
	}

	private double getDoubleValue(Node node, String name) {
		return Double.parseDouble( getStringValue(node, name) );		
	}

	public static class FeedThread extends Thread {

		public boolean running = false;

		private List<Stock> stockList;
		
		private Random random = new Random();

		public FeedThread(List<Stock> stockList) {
			this.stockList = stockList;
		}
		
		public void run() {
			running = true;

			MessageBroker msgBroker = MessageBroker.getMessageBroker("_messageBroker");
			String clientID = UUIDUtils.createUUID();

			int size = stockList.size();
			int index = 0;

			Stock stock;

			while (running) {

				stock = (Stock) stockList.get(index);
				simulateChange(stock);

				index++;
				if (index >= size) {
					index = 0;
				}

				AsyncMessage msg = new AsyncMessage();
				msg.setDestination("market-feed");
				msg.setHeader("DSSubtopic", stock.getSymbol());
				msg.setClientId(clientID);
				msg.setMessageId(UUIDUtils.createUUID());
				msg.setTimestamp(System.currentTimeMillis());
				msg.setBody(stock);
				msgBroker.routeMessageToService(msg, null);
				
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
				}

			}
		}

		private void simulateChange(Stock stock) {

			double maxChange = stock.open * 0.005;
			double change = maxChange - random.nextDouble() * maxChange * 2;
			stock.change = change;
			double last = stock.last + change;

			if (last < stock.open + stock.open * 0.15
					&& last > stock.open - stock.open * 0.15) {
				stock.last = last;
			} else {
				stock.last = stock.last - change;
			}

			if (stock.last > stock.high) {
				stock.high = stock.last;
			} else if (stock.last < stock.low) {
				stock.low = stock.last;
			}
			stock.date = new Date();

		}

	}

}
