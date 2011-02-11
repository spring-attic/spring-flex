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

package org.springframework.flex.samples.util;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 
 * @author Christophe Coenraets
 * @author Jeremy Grelle
 */
public class DatabaseInitializer {

    private final JdbcTemplate template;

    public DatabaseInitializer(DataSource ds) {
        this.template = new JdbcTemplate(ds);
        createTableProduct();
        insertProducts();
        createTableContact();
        insertContacts();
        createTableIndustry();
        insertIndustries();
        createTableCompany();
        insertCompanies();
        createTableAccount();
        insertAccount();
    }

    public void createTableContact() {
        String sql = "CREATE TABLE IF NOT EXISTS CONTACT (" + "ID INT AUTO_INCREMENT PRIMARY KEY, " + "FIRST_NAME VARCHAR(50), "
            + "LAST_NAME VARCHAR(50), " + "ADDRESS VARCHAR(50), " + "CITY VARCHAR(50), " + "STATE VARCHAR(20), " + "ZIP VARCHAR(20), "
            + "PHONE VARCHAR(50), " + "EMAIL VARCHAR(50), " + "DOB DATE)";
        this.template.execute(sql);
    }

    public void createTableCompany() {
        String sql = "CREATE TABLE IF NOT EXISTS COMPANY (" + "ID INT AUTO_INCREMENT PRIMARY KEY, " + "NAME VARCHAR(50), " + "ADDRESS VARCHAR(50), "
            + "CITY VARCHAR(50), " + "STATE VARCHAR(20), " + "ZIP VARCHAR(20), " + "PHONE VARCHAR(50), " + "INDUSTRY_ID INT)";
        this.template.execute(sql);
    }

    public void createTableIndustry() {
        String sql = "CREATE TABLE IF NOT EXISTS INDUSTRY (" + "ID INT AUTO_INCREMENT PRIMARY KEY, " + "NAME VARCHAR(50))";
        this.template.execute(sql);
    }

    public void createTableProduct() {
        String sql = "CREATE TABLE IF NOT EXISTS PRODUCT (" + "ID INT AUTO_INCREMENT PRIMARY KEY, " + "NAME VARCHAR(50), " + "CATEGORY VARCHAR(50), "
            + "DESCRIPTION CLOB, " + "IMAGE VARCHAR(255), " + "PRICE DOUBLE, " + "QTY INT)";
        this.template.execute(sql);
    }

    public void createTableAccount() {
        String sql = "CREATE TABLE IF NOT EXISTS ACCOUNT (" + "ID INT AUTO_INCREMENT PRIMARY KEY, " + "NAME VARCHAR(50)," + "TYPE INT,"
            + "INDUSTRY INT," + "OWNER INT," + "PHONE VARCHAR(30)," + "FAX VARCHAR(30)," + "TICKER VARCHAR(10)," + "OWNERSHIP VARCHAR(20),"
            + "NUMBER_EMPLOYEES INT," + "ANNUAL_REVENUE DOUBLE," + "PRIORITY INT," + "RATING INT," + "ADDRESS1 VARCHAR(50),"
            + "ADDRESS2 VARCHAR(50)," + "CITY VARCHAR(50)," + "STATE VARCHAR(50)," + "ZIP VARCHAR(20)," + "URL VARCHAR(50)," + "NOTES CLOB,"
            + "CURRENT_YEAR_RESULTS DOUBLE," + "LAST_YEAR_RESULTS DOUBLE)";
        this.template.execute(sql);
    }

    public void insertContacts() {
        int rowCount = this.template.queryForInt("SELECT COUNT(*) FROM CONTACT");
        if (rowCount > 0) {
            System.out.println("Contacts already exist");
            return;
        }
        System.out.println("Inserting sample data in table CONTACT...");
        String sql = "INSERT INTO CONTACT (FIRST_NAME, LAST_NAME, ADDRESS, CITY, STATE, ZIP, PHONE, EMAIL) VALUES (?,?,?,?,?,?,?,?)";
        this.template.update(sql, new Object[] { "Christophe", "Coenraets", "275 Grove St", "Newton", "MA", "02476", "617-219-2000",
            "ccoenrae@adobe.com" });
        this.template.update(sql, new Object[] { "John", "Smith", "1 Main st", "Boston", "MA", "01744", "617-219-2001", "jsmith@mail.com" });
        this.template.update(sql, new Object[] { "Lisa", "Taylor", "501 Townsend st", "San Francisco", "CA", "", "415-534-7865", "ltaylor@mail.com" });
        this.template.update(sql, new Object[] { "Noah", "Jones", "1200 5th Avenue ", "New York", "NY", "", "212-764-2345", "njones@mail.com" });
        this.template.update(sql, new Object[] { "Bill", "Johnson", "1345 6th street", "Chicago", "IL", "", "", "bjohnson@mail.com" });
        this.template.update(sql, new Object[] { "Chloe", "Rodriguez", "34 Elm street", "Dallas", "TX", "", "415-534-7865", "crodriguez@mail.com" });
        this.template.update(sql, new Object[] { "Jorge", "Espinosa", "23 Putnam Avenue", "Seattle", "WA", "", "", "jespinosa@mail.com" });
        this.template.update(sql, new Object[] { "Amy", "King", "11 Summer st", "Miami", "FL", "", "", "aking@mail.com" });
        this.template.update(sql, new Object[] { "Boris", "Jefferson", "222 Spring st", "Denver", "CO", "", "415-534-7865", "bjefferson@mail.com" });
        this.template.update(sql, new Object[] { "Linda", "Madison", "564 Winter st", "Washington", "DC", "", "", "lmadison@mail.com" });
    }

    public void insertCompanies() {
        int rowCount = this.template.queryForInt("SELECT COUNT(*) FROM COMPANY");
        if (rowCount > 0) {
            System.out.println("Companies already exist");
            return;
        }
        System.out.println("Inserting sample data in table COMPANY...");
        String sql = "INSERT INTO COMPANY (NAME, ADDRESS, CITY, STATE, ZIP, PHONE, INDUSTRY_ID) VALUES (?,?,?,?,?,?,?)";
        this.template.update(sql, new Object[] { "Adobe", "", "San Jose", "CA", "", "408", 1 });
        this.template.update(sql, new Object[] { "SpringSource", "", "New York", "NY", "", "212", 2 });
        this.template.update(sql, new Object[] { "Allaire", "", "Cambridge", "MA", "", "212", 3 });
        this.template.update(sql, new Object[] { "Acme", "", "Denver", "CO", "", "212", 4 });
        this.template.update(sql, new Object[] { "Macromedia", "", "San Francisco", "CA", "", "212", 1 });
        this.template.update(sql, new Object[] { "Alpha Corp", "", "Chicago", "IL", "", "", 1 });
    }

    public void insertIndustries() {
        int rowCount = this.template.queryForInt("SELECT COUNT(*) FROM INDUSTRY");
        if (rowCount > 0) {
            System.out.println("Industries already exist");
            return;
        }
        System.out.println("Inserting sample data in table INDUSTRY...");
        String sql = "INSERT INTO INDUSTRY (NAME) VALUES (?)";
        this.template.update(sql, new Object[] { "Telecommunication" });
        this.template.update(sql, new Object[] { "Government" });
        this.template.update(sql, new Object[] { "Financial Services" });
        this.template.update(sql, new Object[] { "Life Sciences" });
        this.template.update(sql, new Object[] { "Manufacturing" });
        this.template.update(sql, new Object[] { "Education" });
    }

    public void insertProducts() {
        int rowCount = this.template.queryForInt("SELECT COUNT(*) FROM PRODUCT");
        if (rowCount > 0) {
            System.out.println("Products already exist");
            return;
        }
        System.out.println("Inserting sample data in table PRODUCT...");
        String sql = "INSERT INTO PRODUCT (NAME, CATEGORY, IMAGE, PRICE, DESCRIPTION, QTY) VALUES (?,?,?,?,?,?)";
        this.template.update(
            sql,
            new Object[] {
                "Nokia 6010",
                "6000",
                "Nokia_6010.gif",
                99.0E0,
                "Easy to use without sacrificing style, the Nokia 6010 phone offers functional voice communication supported by text messaging, multimedia messaging, mobile internet, games and more.",
                21 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia 3100 Blue",
                "9000",
                "Nokia_3100_blue.gif",
                109.0E0,
                "Light up the night with a glow-in-the-dark cover - when it is charged with light you can easily find your phone in the dark. When you get a call, the Nokia 3100 phone flashes in tune with your ringing tone. And when you snap on a Nokia Xpress-on gaming cover, you will get luminescent light effects in time to the gaming action.",
                99 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia 3100 Pink",
                "3000",
                "Nokia_3100_pink.gif",
                139.0E0,
                "Light up the night with a glow-in-the-dark cover - when it is charged with light you can easily find your phone in the dark. When you get a call, the Nokia 3100 phone flashes in tune with your ringing tone. And when you snap on a Nokia Xpress-on gaming cover, you will get luminescent light effects in time to the gaming action.",
                30 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia 3120",
                "3000",
                "Nokia_3120.gif",
                159.99E0,
                "Designed for both business and pleasure, the elegant Nokia 3120 phone offers a pleasing mix of features. Enclosed within its chic, compact body, you will discover the benefits of tri-band compatibility, a color screen, MMS, XHTML browsing, cheerful screensavers, and much more.",
                10 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia 3220",
                "3000",
                "Nokia_3220.gif",
                199.0E0,
                "The Nokia 3220 phone is a fresh new cut on some familiar ideas - animate your MMS messages with cute characters, see the music with lights that flash in time with your ringing tone, download wallpapers and screensavers with matching color schemes for the interface.",
                20 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia 3650",
                "3000",
                "Nokia_3650.gif",
                200.0E0,
                "Messaging is more personal, versatile and fun with the Nokia 3650 camera phone.  Capture experiences as soon as you see them and send the photos you take to you friends and family.",
                11 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia 6820",
                "6000",
                "Nokia_6820.gif",
                299.99E0,
                "Messaging just got a whole lot smarter. The Nokia 6820 messaging device puts the tools you need for rich communication - full messaging keyboard, digital camera, mobile email, MMS, SMS, and Instant Messaging - right at your fingertips, in a small, sleek device.",
                8 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia 6670",
                "6000",
                "Nokia_6670.gif",
                319.99E0,
                "Classic business tools meet your creative streak in the Nokia 6670 imaging smartphone. It has a Netfront Web browser with PDF support, document viewer applications for email attachments, a direct printing application, and a megapixel still camera that also shoots up to 10 minutes of video.",
                2 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia 6620",
                "6000",
                "Nokia_6620.gif",
                329.99E0,
                "Shoot a basket. Shoot a movie. Video phones from Nokia... the perfect way to save and share life\u2019s playful moments. Feel connected.",
                10 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia 3230 Silver",
                "3000",
                "Nokia_3230_black.gif",
                500.0E0,
                "Get creative with the Nokia 3230 smartphone. Create your own ringing tones, print your mobile images, play multiplayer games over a wireless Bluetooth connection, and browse HTML and xHTML Web pages. ",
                10 });
        this.template.update(sql,
            new Object[] { "Nokia 6680", "6000", "Nokia_6680.gif", 222.0E0, "The Nokia 6680 is an imaging smartphone that", 36 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia 6630",
                "6000",
                "Nokia_6630.gif",
                379.0E0,
                "The Nokia 6630 imaging smartphone is a 1.3 megapixel digital imaging device (1.3 megapixel camera sensor, effective resolution 1.23 megapixels for image capture, image size 1280 x 960 pixels});.",
                8 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia 7610 Black",
                "7000",
                "Nokia_7610_black.gif",
                450.0E0,
                "The Nokia 7610 imaging phone with its sleek, compact design stands out in any crowd. Cut a cleaner profile with a megapixel camera and 4x digital zoom. Quality prints are all the proof you need of your cutting edge savvy.",
                20 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia 7610 White",
                "7000",
                "Nokia_7610_white.gif",
                399.99E0,
                "The Nokia 7610 imaging phone with its sleek, compact design stands out in any crowd. Cut a cleaner profile with a megapixel camera and 4x digital zoom. Quality prints are all the proof you need of your cutting edge savvy.",
                7 });
        this.template.update(sql, new Object[] { "Nokia 6680", "6000", "Nokia_6680.gif", 219.0E0, "The Nokia 6680 is an imaging smartphone.", 15 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia 9300",
                "9000",
                "Nokia_9300_close.gif",
                599.0E0,
                "The Nokia 9300 combines popular voice communication features with important productivity applications in one well-appointed device. Now the tools you need to stay in touch and on top of schedules, email, news, and messages are conveniently at your fingertips.",
                26 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia 9500",
                "9000",
                "Nokia_9500_close.gif",
                799.99E0,
                "Fast data connectivity with Wireless LAN. Browse the Internet in full color, on a wide, easy-to-view screen. Work with office documents not just email with attachments and memos, but presentations and databases too.",
                54 });
        this.template.update(
            sql,
            new Object[] {
                "Nokia N90",
                "9000",
                "Nokia_N90.gif",
                499.0E0,
                "Twist and shoot. It is a pro-photo taker. A personal video-maker. Complete with Carl Zeiss Optics for crisp, bright images you can view, edit, print and share. Meet the Nokia N90.",
                12 });
    }

    public void insertAccount() {
        int rowCount = this.template.queryForInt("SELECT COUNT(*) FROM ACCOUNT");
        if (rowCount > 0) {
            System.out.println("Accounts already exist");
            return;
        }
        System.out.println("Inserting sample data in table ACCOUNT...");
        String sql = "INSERT INTO ACCOUNT (NAME, ADDRESS1, CITY, STATE, ZIP, PHONE) VALUES (?,?,?,?,?,?)";
        this.template.update(sql, new Object[] { "Adobe", "", "San Jose", "CA", "", "408" });
        this.template.update(sql, new Object[] { "SpringSource", "", "New York", "NY", "", "212" });
        this.template.update(sql, new Object[] { "Allaire", "", "Cambridge", "MA", "", "212" });
        this.template.update(sql, new Object[] { "Acme", "", "Denver", "CO", "", "212" });
        this.template.update(sql, new Object[] { "Macromedia", "", "San Francisco", "CA", "", "212" });
        this.template.update(sql, new Object[] { "Alpha Corp", "", "Chicago", "IL", "", "" });
    }
}
