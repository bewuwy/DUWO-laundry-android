package dev.bewu.duwolaundry;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiPossScraper {

    private String phpSessionID;
    private String siteID = "INCORRECT_SITE_ID";
    private final String userEmail;
    private final String userPass;
    private final String multipossURL;

    private String userBalance = null;
    private String userLocation = null;

    public MultiPossScraper(String email, String password, String multipossURL) {
        this.phpSessionID = "";
        this.userEmail = email;
        this.userPass = password;
        this.multipossURL = multipossURL;
    }

    /*
        Steps to scrape:

        initMultiposs():
        1. Get PHP Session ID - GET https://duwo.multiposs.nl/login/index.php
        2. Login (only with email) - POST https://duwo.multiposs.nl/login/submit.php
        3. Init multiposs - GET https://duwo.multiposs.nl/StartSite.php?ID=RANDOM&UserID=EMAIL
        getAvailability():
        4. Fetch availability - GET https://duwo.multiposs.nl/MachineAvailability.php
     */

    public String getUserBalance() {
        return userBalance;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void initScraper() {
        getPHPSession();
        loginMultiposs();
        initMultiposs();

    }

    public String getQRCode() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://duwo.multiposs.nl/GenUserQrcode.php?GenNew=TRUE")
                .get()
                .addHeader("Cookie", "PHPSESSID=" + this.phpSessionID)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            String htmlBody = response.body().string();
            Document doc = Jsoup.parse(htmlBody);

            List<Element> scripts = doc.select("script");

            if (scripts.size() < 4) {
                Log.d("MultipossScraper", "QR: did not get the QR code: " + htmlBody);
                return null;
            }

            Element QRscript = scripts.get(3);

            Pattern pattern = Pattern.compile("(<ID=\\d+>)");
            Matcher matcher = pattern.matcher(QRscript.html());

            if (matcher.find()) {
                return matcher.group(1);
            }

            return null;

        } catch (IOException e) {
            couldNotConnect("Could not connect while fetching the QR code");
            return null;
        }
    }

    /**
     * (4) Final step in fetching availability
     * @return HashMap of Machine->Number of available
     */
    public HashMap<String, Integer> getAvailability() {
        HashMap<String, Integer> availability = new HashMap<>();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(multipossURL + "/MachineAvailability.php")
                .get()
                .addHeader("Cookie", "PHPSESSID=" + this.phpSessionID)
                .build();

        try (Response response = client.newCall(request).execute()) {

            assert response.body() != null;
            Document doc = Jsoup.parse(response.body().string());

            Element availabilityTable = doc.selectFirst("table.ColorTable > tbody");
            if (availabilityTable == null) {
                return availability;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
            System.out.print(sdf.format(new Date()) + " ");

            for (int i=1; i<availabilityTable.childrenSize(); i++) {
                Element el = availabilityTable.child(i);

                String machineType = el.child(1).text();
                String location = el.child(0).text();
                if (i == 1) {
                    System.out.println(location);
                    this.userLocation = location;
                }
                machineType = machineType.replace("Mach.", "Machine");
                String status = el.child(2).text();

                int available = 0;
                if (!status.startsWith("Not Available")) {
                    available = Integer.parseInt(status.split(" :")[1]);
                }

                System.out.println(machineType + ": " + available);
                availability.put(machineType, available);
            }

        } catch (IOException e) {
            couldNotConnect("getAvailability: IOException");
            return availability;
        }

        return availability;
    }

    /**
     * (3) Third step in scraping multiposs
     */
    private void initMultiposs() {

        // the multiposs id does not matter, as the building is determined by your account
        String reqUrl = String.format("%s/StartSite.php?ID=%s&UserID=%s",
                this.multipossURL, this.siteID, this.userEmail);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(reqUrl)
                .get()
                .addHeader("Cookie", "PHPSESSID=" + phpSessionID)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 302 && response.code() != 200) {
                couldNotConnect("initMultiposs: wrong response code " + response.code());
                return;
            }

            assert response.body() != null;
            Document doc = Jsoup.parse(response.body().string());

            Element credits = doc.getElementById("LblUserCredits");
            if (credits != null) {
                String balanceString = credits.text();
                System.out.println("User balance: " + balanceString);
                this.userBalance = balanceString;
            }

            System.out.println("Initialised multiposs page");
        } catch (IOException e) {
            couldNotConnect("initMultiposs: " + e);
        }
    }

    /**
     * (2) Second step in scraping multiposs
     */
    private void loginMultiposs() {

        // the password does not have to be correct
        String loginBody = "UserInput=" + this.userEmail + "&PwdInput=" + this.userPass;

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, loginBody);
        Request request = new Request.Builder()
                .url(this.multipossURL + "/login/submit.php")
                .post(body)
                .addHeader("Cookie", "PHPSESSID=" + phpSessionID)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (response.code() != 200) {
                couldNotConnect("loginMultiposs: wrong response code " + response.code());
                return;
            }

            assert response.body() != null;
            String responseBody = response.body().string();

            if (responseBody.contains("username and/or password<br>incorrect")) {
                Log.d("MultipossScraper", "Logged in with incorrect password");
                return;
            }

            System.out.println("Logged in successfully with correct password");

            Pattern pattern = Pattern.compile("\\.\\./StartSite\\.php\\?ID=(.+)&");
            Matcher matcher = pattern.matcher(responseBody);

            if (matcher.find()) {
                this.siteID = matcher.group(1);
                Log.d("MultipossScraper", "loginMultiposs: Set site id");
            } else {
                Log.d("MultipossScraper", "loginMultiposs: Could not find site id: " + responseBody);
            }

        } catch (IOException e) {
            couldNotConnect("loginMultiposs: " + e);
        }
    }

    /**
     * (1) First step in scraping multiposs
     */
    private void getPHPSession() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(this.multipossURL + "/login/index.php")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String sessionCookieString = response.headers().get("set-cookie");

            if (sessionCookieString == null) {
                couldNotConnect("Did not get PHP session ID in cookies");
                return;
            }

            this.phpSessionID = sessionCookieString.split("=")[1].split(";")[0];
        } catch (IOException e) {
            couldNotConnect("Couldn't fetch PHP session ID: " + e);
        }

    }

    /**
     * Signal to user that there was an error while fetching
     * @param message error message
     */
    private void couldNotConnect(String message) {
        Log.d("MultipossScraper", message);
    }

}
