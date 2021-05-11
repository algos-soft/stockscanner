package com.algos.stockscanner.views.admin;

import com.algos.stockscanner.Application;
import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.services.AdminService;
import com.algos.stockscanner.services.MarketService;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.claspina.confirmdialog.ConfirmDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(value = SCOPE_PROTOTYPE)
@CssImport("./views/admin/admin-view.css")
public class ToolsPage extends VerticalLayout {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MarketService marketService;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private AdminService adminService;

    private @Autowired
    Utils utils;

    @Autowired
    private ContextStore contextStore;

    @Value("${alphavantage.api.key}")
    private String alphavantageApiKey;

    private OkHttpClient okHttpClient;
    private JsonAdapter<FDResponse> fdJsonAdapter;


    private HorizontalLayout statusLayout;

    private static final String OUTPUT_FILENAME="etoro_valid_symbols.txt";

    @PostConstruct
    private void init() {

        okHttpClient = new OkHttpClient();
        Moshi moshi = new Moshi.Builder().build();
        fdJsonAdapter = moshi.adapter(FDResponse.class);

        statusLayout = new HorizontalLayout();
        statusLayout.setSpacing(false);
        statusLayout.setPadding(false);
        statusLayout.addClassName("admin-view-statuslayout");

        // button compare etoro alphavantage
        Button bComp = new Button("Start thread on server");

        bComp.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                try {
                    doCompare();
                    ConfirmDialog.createInfo().withMessage("Background thread started on server. Check server logs.").open();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        String label = "<strong>Symbol validation</strong><br>" +
                "Iterate all symbols contained in the file <strong>"+ Application.ETORO_INSTRUMENTS+"</strong><br>" +
                "For each symbol, check if it is available on Alpha Vantage API.<br>" +
                "Produces the file <strong>"+OUTPUT_FILENAME+"</strong> containing the available symbols.<br>" +
                "Does the work in a background thread on the server.";
        Span span = new Span();
        span.setId("adminview-compare_symbols_label");
        span.getElement().setProperty("innerHTML", label);

        VerticalLayout bCompLayout = new VerticalLayout();
        bCompLayout.add(span, bComp);

        VerticalLayout content = new VerticalLayout();
        content.setHeight("100%");
        content.add(bCompLayout);

        setHeight("100%");
        add(content, statusLayout);


    }


    private void doCompare() {
        List<String> eToroInstruments = loadEtoroInstruments();

        Runnable runnable = new CompareRunnable(eToroInstruments);
        new Thread(runnable).start();

        log.info("Compare thread started");

    }


    private List<String> loadEtoroInstruments() {

        List<String> eToroInstruments = new ArrayList<>();

        String filename = Application.ETORO_INSTRUMENTS;
        File etoroInstrumentsFile = new File(filename);
        if (!etoroInstrumentsFile.exists()) {
            log.warn("File " + filename + " not found. Can't load list of eToro instruments.");
            return null;
        }

        try {
            List<String> lines = Files.readAllLines(etoroInstrumentsFile.toPath());
            for (String line : lines) {
                eToroInstruments.add(line);
            }
        } catch (IOException e) {
            log.error("could not read eToro instruments file " + etoroInstrumentsFile.toString(), e);
        }

        return eToroInstruments;
    }


    /**
     * Fetch fundamental data from the network
     *
     * @return 0=valid, 1=not found, 2=limit reached, 3=internal error
     */
    private int checkSymbol(String symbol) {
        int code = 1;

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.alphavantage.co/query").newBuilder();
        urlBuilder.addQueryParameter("apikey", alphavantageApiKey);
        urlBuilder.addQueryParameter("function", "OVERVIEW");
        urlBuilder.addQueryParameter("symbol", symbol);

        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String string = response.body().string();
                if (string.contains("Thank you")) {   // limit reached
                    log.error("Alphavantage limit reached: " + string);
                    code = 2;
                } else {
                    FDResponse fdresp = fdJsonAdapter.fromJson(string);
                    if (fdresp.Symbol != null) {
                        code = 0;
                    }
                }
            }
        } catch (IOException e) {
            log.error("error in alphavantage request", e);
            code = 3;
        }

        return code;
    }

    static class FDResponse {
        String Symbol;
        String Name;
        String AssetType;
    }


    class CompareRunnable implements Runnable {

        List<String> eToroInstruments;

        public CompareRunnable(List<String> eToroInstruments) {
            this.eToroInstruments = eToroInstruments;
        }

        @Override
        public void run() {
            List<String> validInstruments = new ArrayList<>();
            int i = 0;
            int v = 0;
            for (String symbol : eToroInstruments) {
                i++;
                log.info("checking symbol: " + symbol + " [" + i + "/" + eToroInstruments.size() + "]");

                int code = checkSymbol(symbol);
                boolean terminate=false;
                switch (code) {
                    case 0: // valid
                        validInstruments.add(symbol);
                        v++;
                        log.info("valid symbol found: " + symbol + " [" + v + "]");
                        break;
                    case 1: // not found
                        break;
                    case 2: // limit reached
                        log.info("execution terminated because of limit reached");
                        terminate=true;
                        break;
                    case 3: // internal error
                        log.info("execution terminated for internal error");
                        terminate=true;
                        break;

                }

                if(terminate){
                   break;
                }

                try {
                    Thread.sleep(12000);    // max 5 req per minute
                } catch (InterruptedException e) {
                }
            }

            try {
                FileWriter writer = new FileWriter(OUTPUT_FILENAME);
                for (String str : validInstruments) {
                    writer.write(str + System.lineSeparator());
                }
                writer.close();
                log.info("output written to file "+OUTPUT_FILENAME);
            } catch (Exception e) {
                log.error("could not write list to file", e);
            }

        }
    }

}


