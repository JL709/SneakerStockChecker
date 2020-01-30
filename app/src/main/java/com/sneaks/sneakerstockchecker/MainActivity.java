package com.sneaks.sneakerstockchecker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    TabHost mainTabHost;
    Spinner regionSpinner;
    int selectedRegion;
    String searchUrlStart;
    String stockUrlStart;
    Button searchNameButton;
    Button searchIdButton;
    EditText searchText;
    ProgressBar searchProgressBar;
    ProgressBar stockProgressBar;

    private BroadcastReceiver receiver =  null;

    public void nameSearch(Intent intent){
        if (selectedRegion == 0) {
            searchUrlStart = "http://www.adidas.ca/en/search?q=";
            stockUrlStart = "http://www.adidas.ca/on/demandware.store/Sites-adidas-CA-Site/en_CA/Product-GetVariants?pid=";
        } else if (selectedRegion == 1) {
            searchUrlStart = "http://www.adidas.com/us/search?q=";
            stockUrlStart = "http://www.adidas.com/on/demandware.store/Sites-adidas-US-Site/en_US/Product-GetVariants?pid=";
        } else if (selectedRegion == 2) {
            searchUrlStart = "http://www.adidas.co.uk/search?q=";
            stockUrlStart = "http://www.adidas.nl/on/demandware.store/Sites-adidas-GB-Site/nl_NL/Product-GetVariants?pid=";
        } else if (selectedRegion == 3) {
            searchUrlStart = "http://www.adidas.co.uk/search?q=";
            stockUrlStart = "http://www.adidas.nl/on/demandware.store/Sites-adidas-DE-Site/de_DE/Product-GetVariants?pid=";
        } else if (selectedRegion == 4) {
            searchUrlStart = "http://www.adidas.com.au/search?q=";
            stockUrlStart = "http://www.adidas.com.au/on/demandware.store/Sites-adidas-AU-Site/en_AU/Product-GetVariants?pid=";
        }
        if (searchText.getText().toString().equals("")) {
            // Builds and displays alert
            AlertDialog invalidSearchAlert = new AlertDialog.Builder(MainActivity.this).create();
            invalidSearchAlert.setMessage("No product name or ID");
            invalidSearchAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            invalidSearchAlert.show();
        } else {
            intent.putExtra("parm1", "SEARCH_SNEAKER");
            intent.putExtra("parm2", searchUrlStart + searchText.getText().toString().replace(" ", "%20"));
            startService(intent);
            searchProgressBar.setVisibility(View.VISIBLE);
            mainTabHost.setCurrentTab(1);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new android.content.IntentFilter(StockSearchService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        // AdView
        MobileAds.initialize(this, "");
        AdView adView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();//.addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        adView.loadAd(adRequest);

        // Intent
        receiver = new MyReceiver(this);
        final Intent intent = new Intent(this, StockSearchService.class);

        // Init + make invis ProgressBars
        searchProgressBar = (ProgressBar) findViewById(R.id.searchProgressBar);
        searchProgressBar.setVisibility(View.INVISIBLE);
        stockProgressBar = (ProgressBar) findViewById(R.id.stockProgressBar);
        stockProgressBar.setVisibility(View.INVISIBLE);

        // Sets up main tabs
        mainTabHost = (TabHost) findViewById(R.id.mainTabHost);
        mainTabHost.setup();
        TabHost.TabSpec tabSpec1 = mainTabHost.newTabSpec("search");
        tabSpec1.setContent(R.id.tabSearch);
        tabSpec1.setIndicator("Search");
        mainTabHost.addTab(tabSpec1);
        TabHost.TabSpec tabSpec2 = mainTabHost.newTabSpec("results");
        tabSpec2.setContent(R.id.tabSearchResults);
        tabSpec2.setIndicator("Results");
        mainTabHost.addTab(tabSpec2);
        TabHost.TabSpec tabSpec3 = mainTabHost.newTabSpec("stock");
        tabSpec3.setContent(R.id.tabStock);
        tabSpec3.setIndicator("Stock");
        mainTabHost.addTab(tabSpec3);

        // Sets up spinner
        regionSpinner = (Spinner) findViewById(R.id.regionSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.regions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(adapter);
        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int spinnerPosition, long id) {
                selectedRegion = spinnerPosition;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        searchNameButton = (Button) findViewById(R.id.buttonNameSearch);
        searchIdButton = (Button) findViewById(R.id.buttonIdSearch);
        searchText = (EditText) findViewById(R.id.productSearchEditText);

        searchNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameSearch(intent);
            }
        });

        searchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER){
                    nameSearch(intent);
                    return true;
                }
                return false;
            }
        });

        searchIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedRegion == 0) {
                    stockUrlStart = "http://www.adidas.ca/on/demandware.store/Sites-adidas-CA-Site/en_CA/Product-GetVariants?pid=";
                } else if (selectedRegion == 1) {
                    stockUrlStart = "http://www.adidas.com/on/demandware.store/Sites-adidas-US-Site/en_US/Product-GetVariants?pid=";
                } else if (selectedRegion == 2) {
                    stockUrlStart = "http://www.adidas.nl/on/demandware.store/Sites-adidas-GB-Site/nl_NL/Product-GetVariants?pid=";
                } else if (selectedRegion == 3) {
                    stockUrlStart = "http://www.adidas.nl/on/demandware.store/Sites-adidas-DE-Site/de_DE/Product-GetVariants?pid=";
                } else if (selectedRegion == 4) {
                    stockUrlStart = "http://www.adidas.com.au/on/demandware.store/Sites-adidas-AU-Site/en_AU/Product-GetVariants?pid=";
                }
                if (searchText.getText().toString().equals("")) {
                    // Builds and displays alert
                    AlertDialog invalidSearchAlert = new AlertDialog.Builder(MainActivity.this).create();
                    invalidSearchAlert.setMessage("No product name or ID");
                    invalidSearchAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    invalidSearchAlert.show();
                } else {
                    stockProgressBar.setVisibility(View.VISIBLE);
                    intent.putExtra("parm1", "SEARCH_STOCK");
                    intent.putExtra("parm2", stockUrlStart);
                    intent.putExtra("parm3", searchText.getText().toString().toUpperCase());
                    startService(intent);
                    mainTabHost.setCurrentTab(2);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public class MyReceiver extends BroadcastReceiver{
        private String _stocks = "";
        private Activity _activity;

        public MyReceiver(Activity activity) {
            _activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String return1 = bundle.getString("ret1");
                if (return1 != null && return1.equals("PRODUCT_SEARCH_RESULTS")){
                    Log.i("return", return1);

                    String response = bundle.getString("ret2");
                    ArrayList<SearchListItem> results = new ArrayList<>();
                    int productStartIndex;
                    int nameStartIndex;
                    int pidStartIndex;
                    //int imageStartIndex;
                    int nameEndIndex;
                    int pidEndIndex;
                    //int imageEndIndex;
                    String name;
                    String pid;
                    //String imageBitMapStr;

                    String strStart = "|";

                    int startIndex = 0;
                    int productsLoaded = 0;

                    try {
                        while (productsLoaded < 10) {
                            productStartIndex = response.indexOf(strStart, startIndex);

                            nameStartIndex = response.indexOf(strStart, productStartIndex) + 1;
                            nameEndIndex = response.indexOf("|", nameStartIndex);

                            pidStartIndex = response.indexOf(strStart, nameEndIndex) + 1;
                            pidEndIndex = response.indexOf("|", pidStartIndex);

                            //imageStartIndex = response.indexOf(strStart, pidEndIndex) + 1;
                            //imageEndIndex = response.indexOf("|", imageStartIndex);

                            name = response.substring(nameStartIndex, nameEndIndex);
                            pid = response.substring(pidStartIndex, pidEndIndex);
                            //imageBitMapStr = response.substring(imageStartIndex, imageEndIndex);

                            Log.i("name", name);
                            Log.i("pid", pid);
                            //Log.i("image", imageBitMapStr);
                            try {
                                //Bitmap image = StringToBitMap(imageBitMapStr);
                                SearchListItem searchListItem = new SearchListItem();
                                searchListItem.setName(name);
                                searchListItem.setPid(pid);
                                byte[] bytes = bundle.getByteArray("ret" + (productsLoaded + 3));
                                //Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                searchListItem.setImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                //searchListItem.setImage(image);
                                results.add(searchListItem);
                            } catch (Exception ex) {
                                Log.e("ERROR", "Search Results Loop");
                                Log.e("ERROR", ex.getMessage(), ex);
                            }
                            startIndex = response.indexOf(strStart, pidEndIndex);//imageEndIndex);
                            productsLoaded++;
                        }
                    }catch (Error e) {
                        Log.e("ERROR", "IndexOf Error");
                        Log.e("ERROR", e.getMessage(), e);
                    }
                    ListView searchResultsList = (ListView) findViewById(R.id.searchResultsListView);
                    searchResultsList.setAdapter(new SearchListAdapter(getApplicationContext(), results, stockUrlStart));
                    searchProgressBar.setVisibility(View.INVISIBLE);

                } else if (return1 != null && return1.equals("STOCK_SEARCH_RESULTS")){
                    try {
                        Log.i("return", return1);

                        StockListItem stockListItem = new StockListItem();
                        ArrayList<StockListItem> results = new ArrayList<>();
                        ListView stockList = (ListView) findViewById(R.id.stockListView);

                        String response = bundle.getString("ret2");
                        JSONObject json = new JSONObject(response);
                        JSONArray json2 = json.optJSONObject("variations").optJSONArray("variants");

                        // Gets total stock number
                        int stockTotal = 0;
                        for (int x = 0; x < json2.length(); x++) {
                            stockTotal += json2.optJSONObject(x).optInt("ATS");
                        }
                        stockListItem.setSize("ALL SIZES");
                        stockListItem.setNumber("STOCK: " + stockTotal);
                        results.add(stockListItem);

                        // Gets stock numbers for each size
                        String size;
                        int stockNumber;
                        for (int x = 0; x < json2.length(); x++) {
                            stockListItem = new StockListItem();
                            size = json2.optJSONObject(x).optJSONObject("attributes").optString("size");
                            stockNumber = json2.optJSONObject(x).optInt("ATS");
                            stockListItem.setSize("SIZE: " + size);
                            stockListItem.setNumber("STOCK: " + stockNumber);
                            Log.i(size + "" + x, stockNumber + "");
                            results.add(stockListItem);
                        }
                        stockList.setAdapter(new StockListAdapter(getApplicationContext(), results));
                        stockProgressBar.setVisibility(View.INVISIBLE);
                        Log.i("stockting", "done");
                    } catch (Exception e){
                        Log.e("ERROR", e.getMessage(), e);
                    }
                }  else if (return1 != null && (return1.equals("STOCK_SEARCH_RESPONSE_NULL") || return1.equals("PRODUCT_SEARCH_RESPONSE_NULL"))) {
                    try {
                        Log.i("return", return1);
                        String response = bundle.getString("ret2");
                        AlertDialog invalidSearchAlert = new AlertDialog.Builder(MainActivity.this).create();
                        invalidSearchAlert.setMessage(response);
                        invalidSearchAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        invalidSearchAlert.show();
                    } catch (Exception e) {
                        Log.e("ERROR", e.getMessage(), e);
                    }
                }else if (return1 != null && return1.equals("STOCK_TAB")) {
                    mainTabHost.setCurrentTab(2);
                }

            }
        }
                /*

                Log.i("onReceive", "ret1:" + return1 + " ret2:" + return2 + " ret3:" + return3);

                try {
                    if ("SEARCH_SNEAKER".equals(return1)) {
                        int resultCode = RESULT_CANCELED;

                        try {
                            resultCode = Integer.parseInt(return2);
                        }
                        catch(Exception e) {}
                        refreshListView(return3);
                        //Toast.makeText(MainActivity.this, return3, Toast.LENGTH_SHORT).show();
                    }
                    else {
                    }
                }
                catch (Exception e) {
                    Log.i("onReceive", e.toString());
                }
                */

        public void refreshListView(String param) {
            //here you can parse param, then refresh the reuslt listview

            //try {
            //    SimpleAdapter adapter = new SimpleAdapter(_activity, list,
            //            R.layout.my_list,
            //            new String[] { "colText1", "colText2", "colText3", "colText4", "colText5", "colText6" },
            //            new int[] { R.id.colText1, R.id.colText2, R.id.colText3, R.id.colText4, R.id.colText5, R.id.colText6 });
            //    searchResultsList.setAdapter(adapter);
            //} catch (Exception e) {
            //}

        }
    }
}