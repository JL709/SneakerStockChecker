package com.sneaks.sneakerstockchecker;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.app.Activity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 *Created by Jimmy on 2017-07-04.
 */

public class StockSearchService extends IntentService {

    private static final String TAG = "stockSearchService";

    public static final String NOTIFICATION = "service receiver";
    private Intent returnIntent = new Intent(NOTIFICATION);

    public StockSearchService() {
        super("StockSearchService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        doInit();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "service");

        String parm1 = intent.getStringExtra("parm1");
        String parm2 = intent.getStringExtra("parm2");
        String parm3 = intent.getStringExtra("parm3");

        Log.i(TAG, "onHandleIntent() parm1:" + parm1 + " parm2:" + parm2 + " parm3:" + parm3);

        if (parm1.equals("SEARCH_SNEAKER")) {
            productSearch(parm2);
        } else if (parm1.equals("SEARCH_STOCK")) {
            stockSearch(parm2, parm3);
        }
    }

    private void doInit() {
        Log.i(TAG, "==============================  ==============================");
        Log.i(TAG, "doInit()");
    }

    private void doNothing() {
        Log.i(TAG, "doNothing()");
        returnIntent.putExtra("ret1", "NONE");
        returnIntent.putExtra("ret2", "");
        returnIntent.putExtra("ret3", "");
        sendBroadcast(returnIntent);
    }

    private Bitmap downloadImage(String url) {
        Bitmap image = null;
        try {
            InputStream input = new java.net.URL(url).openStream();
            image = BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return image;
    }


    private void productSearch(String proUrl) {
        int result = Activity.RESULT_OK;
        String response = "";
        try {
            URL url = new URL(proUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            boolean redirect = false;
            int status = HTTP_OK;

            status = urlConnection.getResponseCode();
            if (status != HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }
            Log.i("", "Response Code ... " + status);

            if (redirect) {

                // get redirect url from "location" header field
                String newUrl = urlConnection.getHeaderField("Location");

                // get the cookie if need, for login
                String cookies = urlConnection.getHeaderField("Set-Cookie");

                // open the new connection again
                urlConnection = (HttpURLConnection) new URL(newUrl).openConnection();
                urlConnection.setRequestProperty("Cookie", cookies);
                urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
                urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
                urlConnection.setRequestProperty("Host", "www.adidas.ca");

                Log.i("", "Redirect to URL : " + newUrl);
            }

            if (status == HTTP_OK) {
                for (Map.Entry<String, List<String>> header : urlConnection.getHeaderFields().entrySet()) {
                    Log.i("", header.getKey() + "=" + header.getValue());
                }
                Reader reader = null;
                try {
                    if ("gzip".equals(urlConnection.getContentEncoding())) {
                        reader = new InputStreamReader(new GZIPInputStream(urlConnection.getInputStream()));
                    } else {
                        reader = new InputStreamReader(urlConnection.getInputStream());
                    }
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    response = stringBuilder.toString();
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage());
                } finally {
                    urlConnection.disconnect();
                }
            }
        } catch (Exception e) {
            Log.e("ERROR", "ProductSearch doInBackground");
            Log.e("ERROR", e.getMessage(), e);
        }
        Log.i("res", response);
        if (response.contains("We are sorry but no results were found")) {
            returnIntent.putExtra("ret1", "PRODUCT_SEARCH_RESPONSE_NULL");
            returnIntent.putExtra("ret2", "No results found");
            sendBroadcast(returnIntent);
        } else if (!response.equals("")) {

            //ArrayList<SearchListItem> results = new ArrayList<>();
            String resultsStr = "|";

            int productStartIndex;
            int pidStartIndex;
            int nameStartIndex;
            int imageStartIndex;
            int pidEndIndex;
            int nameEndIndex;
            int imageEndIndex;
            String pid;
            String name;
            String imageUrl;

            String productStartStr = "div id=\"product";
            String pidStartStr = "data-target=\"";
            String imageStartStr = "data-original=\"";
            String nameStartStr = "data-productname=\"";

            int startIndex = response.indexOf("<div id=\"product-grid\"") + "<div id=\"product-grid\"".length();

            int productsLoaded = 0;
            while (startIndex > 0 && productsLoaded < 10) {
                productStartIndex = response.indexOf(productStartStr, startIndex);
                pidStartIndex = response.indexOf(pidStartStr, productStartIndex) + pidStartStr.length();
                nameStartIndex = response.indexOf(nameStartStr, productStartIndex) + nameStartStr.length();
                imageStartIndex = response.indexOf(imageStartStr, productStartIndex) + imageStartStr.length();
                pidEndIndex = response.indexOf("\"", pidStartIndex);
                nameEndIndex = response.indexOf("\"", nameStartIndex);
                imageEndIndex = response.indexOf("\"", imageStartIndex);

                pid = response.substring(pidStartIndex, pidEndIndex);
                name = response.substring(nameStartIndex, nameEndIndex);
                imageUrl = response.substring(imageStartIndex, imageEndIndex);
                Log.i("pid", pid);
                Log.i("name", name);
                Log.i("image", imageUrl);
                try {
                    Bitmap image = downloadImage(imageUrl);
                    resultsStr += name + "|" + pid + "|";
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] bytes = stream.toByteArray();
                    returnIntent.putExtra("ret" + (productsLoaded + 3), bytes);
                } catch (Exception ex) {
                    Log.e("ERROR", "ProductSearch downloadingImage");
                    Log.e("ERROR", ex.getMessage(), ex);
                }
                startIndex = response.indexOf(productStartStr, imageEndIndex);
                productsLoaded++;
            }
            //Log.i("Tingaz", resultsStr);
            returnIntent.putExtra("ret1", "PRODUCT_SEARCH_RESULTS");
            returnIntent.putExtra("ret2", resultsStr);
            sendBroadcast(returnIntent);
        } else {
            returnIntent.putExtra("ret1", "PRODUCT_SEARCH_RESPONSE_NULL");
            returnIntent.putExtra("ret2", "Unable to retrieve products");
            sendBroadcast(returnIntent);
        }
    }

    private void stockSearch(String stockUrlStart, String pid) {
        String response = null;
        try {
            returnIntent.putExtra("ret1", "STOCK_TAB");
            sendBroadcast(returnIntent);
            URL url = new URL(stockUrlStart + pid);
            Log.i("INFO", url.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //urlConnection.setReadTimeout(20000);
            //urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            //urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            //urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            //urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
            //urlConnection.setRequestProperty("Host", "www.adidas.ca");
            Reader reader = null;
            //Log.i("headers", urlConnection.getRequestProperties().toString());
            try {
                if ("gzip".equals(urlConnection.getContentEncoding())) {
                    reader = new InputStreamReader(new GZIPInputStream(urlConnection.getInputStream()));
                } else {
                    reader = new InputStreamReader(urlConnection.getInputStream());
                }
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                response = stringBuilder.toString();
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
            } finally {
                urlConnection.disconnect();
            }

            if (response != null) {
                Log.i("res", response);
                returnIntent.putExtra("ret1", "STOCK_SEARCH_RESULTS");
                returnIntent.putExtra("ret2", response);
                sendBroadcast(returnIntent);
                Log.i("ret1", "STOCK_SEARCH_RESULTS");
            } else {
                returnIntent.putExtra("ret1", "STOCK_SEARCH_RESPONSE_NULL");
                returnIntent.putExtra("ret2", "Unable to retrieve stock, or out of stock");
                sendBroadcast(returnIntent);
            }

        } catch (Exception e) {
            Log.e("ERROR", "StockSearch");
            Log.e("ERROR", e.getMessage(), e);
        }
    }
}