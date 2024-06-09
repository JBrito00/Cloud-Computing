package org.example.clientapp.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Utility class for fetching IP addresses.
 */
public class IpFetcher {
    private final String cfURL;

    public IpFetcher() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/lookup.properties")) {
            if (input == null) {
                throw new IOException("Unable to find lookup.properties");
            }
            properties.load(input);
        }

        this.cfURL = properties.getProperty("lookuplink");
    }

    /**
     * Fetches IP addresses from a remote service.
     *
     * @return A list of IP addresses.
     */
    public List<String> fetchIPAddresses() {
        List<String> listips = new ArrayList<>();
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet reqGet = new HttpGet(cfURL);
            try (CloseableHttpResponse respGet = httpclient.execute(reqGet)) {
                HttpEntity entity = respGet.getEntity();
                String jstr = EntityUtils.toString(entity);
                System.out.println("json string=" + jstr);

                Type listType = new TypeToken<ArrayList<String>>() {}.getType();
                listips = new Gson().fromJson(jstr, listType);
            }
        } catch (Exception ex) {
            System.out.println("Error fetching IP addresses: " + ex.getMessage());
        }
        return listips;
    }

    /**
     * Displays the list of IP addresses and allows the user to choose one.
     *
     * @param ips  The list of IP addresses.
     * @param scan Scanner object for user input.
     * @return The chosen IP address.
     */
    public static String chooseIPAddress(List<String> ips, Scanner scan) {
        System.out.println("Available gRPC server IPs:");
        for (int i = 0; i < ips.size(); i++) {
            System.out.println(i + ": " + ips.get(i));
        }
        System.out.print("Choose an IP by entering its number: ");
        int ipChoice = scan.nextInt();
        return ips.get(ipChoice);
    }
}
