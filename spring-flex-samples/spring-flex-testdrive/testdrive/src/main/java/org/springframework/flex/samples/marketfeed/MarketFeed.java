/*
 * Copyright 2002-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.samples.marketfeed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.core.io.Resource;
import org.springframework.flex.messaging.AsyncMessageCreator;
import org.springframework.flex.messaging.MessageTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import flex.messaging.messages.AsyncMessage;

/**
 * 
 * @author Christophe Coenraets
 * @author Jeremy Grelle
 */
public class MarketFeed {

    private static FeedThread thread;

    private final MessageTemplate template;

    private final List<Stock> stockList;

    public MarketFeed(MessageTemplate template, Resource filePath) throws IOException {
        this.template = template;
        this.stockList = getStocks(filePath.getFile());
    }

    public void start() {
        if (thread == null) {
            thread = new FeedThread(this.template, this.stockList);
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
            for (int i = 0; i < length; i++) {
                stockNode = stockNodes.item(i);
                stock = new Stock();
                stock.setSymbol(getStringValue(stockNode, "symbol"));
                stock.setName(getStringValue(stockNode, "company"));
                stock.setLast(getDoubleValue(stockNode, "last"));
                stock.setHigh(stock.getLast());
                stock.setLow(stock.getLast());
                stock.setOpen(stock.getLast());
                stock.setChange(0);
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
        return Double.parseDouble(getStringValue(node, name));
    }

    public static class FeedThread extends Thread {

        public boolean running = false;

        private final MessageTemplate template;

        private final List<Stock> stockList;

        private final Random random = new Random();

        public FeedThread(MessageTemplate template, List<Stock> stockList) {
            this.template = template;
            this.stockList = stockList;
        }

        @Override
        public void run() {
            this.running = true;

            int size = this.stockList.size();
            int index = 0;

            Stock stock;

            while (this.running) {

                stock = this.stockList.get(index);
                simulateChange(stock);

                index++;
                if (index >= size) {
                    index = 0;
                }

                sendStockUpdate(stock);

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                }

            }
        }

        private void sendStockUpdate(final Stock stock) {
            template.send(new AsyncMessageCreator() {

                public AsyncMessage createMessage() {
                    AsyncMessage msg = template.createMessageForDestination("market-feed");
                    msg.setHeader("DSSubtopic", stock.getSymbol());
                    msg.setBody(stock);
                    return msg;
                }
            });
        }

        private void simulateChange(Stock stock) {

            double maxChange = stock.open * 0.005;
            double change = maxChange - this.random.nextDouble() * maxChange * 2;
            stock.change = change;
            double last = stock.last + change;

            if (last < stock.open + stock.open * 0.15 && last > stock.open - stock.open * 0.15) {
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
