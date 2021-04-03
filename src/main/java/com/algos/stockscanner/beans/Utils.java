package com.algos.stockscanner.beans;


import com.vaadin.flow.component.Component;
//import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.imageio.ImageIO;
//import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class Utils {

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



}
