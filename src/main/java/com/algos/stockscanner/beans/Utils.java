package com.algos.stockscanner.beans;


import com.algos.stockscanner.Application;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.services.UpdateIndexDataCallable;
import com.google.common.io.Resources;
import com.vaadin.flow.component.Component;
//import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.server.*;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
//import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);



    @Autowired
    private HttpClient httpClient;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Find a child component by id
     */
    public Optional<Component> findChildById(Component parent, String id){
        Stream<Component> children = parent.getChildren();
        Stream<Component> filtered = children.filter(b -> {
            if(b.getId().isPresent()){
                return b.getId().get().equalsIgnoreCase(id);
            }
            return false;
        });
        return filtered.findFirst();
    }

    /**
     * Find the customizable area of the header inside the main view
     */
    public Optional<HorizontalLayout> findCustomArea(Component mainView){
        Optional<Component> header = findChildById(mainView,"header");
        if(header.isPresent()){
            Optional<Component> custom = findChildById(header.get(),"custom");
            if(custom.isPresent() && custom.get() instanceof HorizontalLayout){
                HorizontalLayout hl = (HorizontalLayout)custom.get();
                Optional<HorizontalLayout> opt = Optional.of(hl);
                return opt;
            }
        }
        return null;
    }

    /**
     * Convert byte[] to Vaadin Image
     */
    public Image byteArrayToImage(byte[] imageData)
    {
        StreamResource streamResource = new StreamResource("isr", new InputStreamFactory() {
            @Override
            public InputStream createInputStream() {
                return new ByteArrayInputStream(imageData);
            }
        });
        return new Image(streamResource, "img");
    }

    /**
     * Convert an image to a byte array
     * Works only for images retrieved from resources!
     */
    public byte[] imageToByteArray(Image img){
        String src=img.getSrc();
        byte[] bytes = resourceToByteArray(src);
        return bytes;
    }

    /**
     * Retrieve a resource file from the resources directory as a byte array.
     * @param path of the file relative to the resource directory, no leading slash
     */
    public byte[] resourceToByteArray(String path){
        byte[] bytes=null;
        String classpath = "classpath:META-INF/resources/"+path;
        try {
            Resource resource = resourceLoader.getResource(classpath);
            InputStream inputStream = resource.getInputStream();
            bytes = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            log.error("can't retrieve file from resources", e);
        }
        return bytes;
    }


    public byte[] scaleImage(byte[] fileData, int width, int height) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(fileData);
        try {
            BufferedImage img = ImageIO.read(in);
            if(height == 0) {
                height = (width * img.getHeight())/ img.getWidth();
            }
            if(width == 0) {
                width = (height * img.getWidth())/ img.getHeight();
            }
            java.awt.Image scaledImage = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
            BufferedImage imageBuff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            imageBuff.getGraphics().drawImage(scaledImage, 0, 0, new java.awt.Color(0,0,0), null);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            ImageIO.write(imageBuff, "jpg", buffer);

            return buffer.toByteArray();
        } catch (IOException e) {
            throw new IOException("IOException in scale");
        }
    }


    public double toPrimitive(Double wrapper){
        if(wrapper!=null){
            return wrapper.doubleValue();
        }else{
            return 0d;
        }
    }

    public float toPrimitive(Float wrapper){
        if(wrapper!=null){
            return wrapper.floatValue();
        }else{
            return 0f;
        }
    }

    public long toPrimitive(Long wrapper){
        if(wrapper!=null){
            return wrapper.longValue();
        }else{
            return 0l;
        }
    }

    public int toPrimitive(Integer wrapper){
        if(wrapper!=null){
            return wrapper.intValue();
        }else{
            return 0;
        }
    }

    public boolean toPrimitive(Boolean wrapper){
        if(wrapper!=null){
            return wrapper.booleanValue();
        }else{
            return false;
        }
    }

    public float toPrimitiveFloat(BigDecimal wrapper){
        if(wrapper!=null){
            return wrapper.floatValue();
        }else{
            return 0;
        }
    }



    public byte[] getBytesFromUrl(String url) throws IOException {
        byte[] bytes=null;
        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if(response.isSuccessful()){
                bytes=response.body().bytes();
            }
        }
        return bytes;
    }

    public byte[] getIconFromUrl(String url) throws IOException {
        byte[] imageData = getBytesFromUrl(url);
        imageData = scaleImage(imageData, Application.STORED_ICON_WIDTH, Application.STORED_ICON_HEIGHT);
        return imageData;
    }

//    /**
//     * @return the data of the default index icon
//     */
//    public byte[] getDefaultIndexIcon(){
//        Resource res=context.getResource(Application.GENERIC_INDEX_ICON);
//        byte[] imageData=null;
//        try {
//            imageData = Files.readAllBytes(Paths.get(res.getURI()));
//            imageData = scaleImage(imageData, Application.STORED_ICON_WIDTH, Application.STORED_ICON_HEIGHT);
//        } catch (IOException e) {
//            log.error("can't load default index icon "+Application.GENERIC_INDEX_ICON, e);
//        }
//        return imageData;
//    }

    /**
     * @return the default index icon
     */
    public Image getDefaultIndexIcon(){
        return new Image(Application.GENERIC_INDEX_ICON,"index");
    }

    /**
     * @return the default index icon as a byte array
     */
    public byte[] getDefaultIndexIconBytes(){
        return resourceToByteArray(Application.GENERIC_INDEX_ICON);
    }



    public ComboBox<MarketIndex> buildIndexCombo() {

        // create a DataProvider with filtering callbacks
        MarketIndex exampleItem = new MarketIndex();
        ExampleMatcher matcher = ExampleMatcher.matchingAny().withMatcher("symbol", ExampleMatcher.GenericPropertyMatchers.startsWith().ignoreCase());
        Example<MarketIndex> example = Example.of(exampleItem, matcher);
        DataProvider<MarketIndex, String> dataProvider = DataProvider.fromFilteringCallbacks(fetchCallback -> {
            AtomicReference<String> filter=new AtomicReference<>();
            fetchCallback.getFilter().ifPresent( x -> filter.set(x));
            exampleItem.setSymbol(filter.get());
            return marketIndexService.fetch(fetchCallback.getOffset(), fetchCallback.getLimit(), example, null).stream();
        }, countCallback -> {
            AtomicReference<String> filter=new AtomicReference<>();
            countCallback.getFilter().ifPresent( x -> filter.set(x));
            exampleItem.setSymbol(filter.get());
            return marketIndexService.count(example);
        });

        // create a renderer for the items in the combo list
        Renderer<MarketIndex> listItemRenderer = new ComponentRenderer<>(item -> {
            Div divSymbol = new Div();
            divSymbol.setText(item.getSymbol());
            divSymbol.getStyle().set("font-weight", "bold");
            Div divName = new Div();
            divName.setText(item.getName());
            divName.setMaxHeight("0.6em");
            divName.getStyle().set("font-size", "60%");
            FlexLayout texts = new FlexLayout();
            texts.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
            texts.add(divSymbol, divName);
            texts.getStyle().set("margin-left", "0.5em");

            Image image = byteArrayToImage(item.getImage());
            image.getStyle().set("border-radius","10%");

            image.setWidth("2em");
            image.setHeight("2em");

            FlexLayout wrapper = new FlexLayout();
            wrapper.setFlexDirection(FlexLayout.FlexDirection.ROW);
            wrapper.add(image, texts);

            return wrapper;
        });

        ComboBox<MarketIndex> indexCombo = new ComboBox<>();
        indexCombo.setLabel("Index");
        indexCombo.setWidth("14em");
        indexCombo.setDataProvider(dataProvider);
        indexCombo.setRenderer(listItemRenderer);
        indexCombo.setItemLabelGenerator(MarketIndex::getSymbol);

        return indexCombo;
    }


    public Sort buildSort(List<QuerySortOrder> orders){

        List<Sort.Order> sortOrders = new ArrayList<>();

        for(QuerySortOrder order : orders){

            SortDirection sortDirection = order.getDirection();
            String sortProperty = order.getSorted();

            Sort.Direction sDirection=null;
            switch (sortDirection){
                case ASCENDING:
                    sDirection=Sort.Direction.ASC;
                    break;
                case DESCENDING:
                    sDirection=Sort.Direction.DESC;
                    break;
            }

            sortOrders.add(new Sort.Order(sDirection, sortProperty));

        }

        return Sort.by(sortOrders);
    }


    /**
     * Convert a number to a string in thousands/million/billion/trillion format
     */
    public static String numberWithSuffix(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f %c",
                count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp-1));
    }



}
