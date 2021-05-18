package com.algos.stockscanner.beans;


import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.HashMap;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExportHelper {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Workbook wb;


    private HashMap<Styles, CellStyle> styles;


    public ExportHelper(Workbook wb) {
        this.wb = wb;
    }

    @PostConstruct
    private void init(){

        styles=new HashMap<>();

        buildDefaultStyles();

    }



    /**
     * Create a String cell
     */
    public Cell createCell(Row row, int idx, String value, CellStyle style){
        Cell cell = createCell(row, idx, style);
        if(value!=null){
            cell.setCellValue(value);
        }
        return cell;
    }
    public Cell createCell(Row row, int idx, String value){
        return createCell(row, idx, value, null);
    }


    /**
     * Create a Number (float) cell
     */
    public Cell createCell(Row row, int idx, float value, CellStyle style){
        Cell cell = createCell(row, idx, style);
        if(style==null){
            cell.setCellStyle(getStyle(Styles.DECIMAL2));
        }
        cell.setCellValue(value);
        return cell;
    }
    public Cell createCell(Row row, int idx, float value){
        return createCell(row, idx, value, null);
    }


    /**
     * Create a Integer cell
     */
    public Cell createCell(Row row, int idx, int value, CellStyle style){
        Cell cell = createCell(row, idx, style);
        if(style==null){
            cell.setCellStyle(getStyle(Styles.INTEGER));
        }
        cell.setCellValue(value);
        return cell;
    }
    public Cell createCell(Row row, int idx, int value){
        return createCell(row, idx, value, null);
    }


    /**
     * Create a Long cell
     */
    public Cell createCell(Row row, int idx, long value, CellStyle style){
        Cell cell = createCell(row, idx, style);
        if(style==null){
            cell.setCellStyle(getStyle(Styles.INTEGER));
        }
        cell.setCellValue(value);
        return cell;
    }
    public Cell createCell(Row row, int idx, long value){
        return createCell(row, idx, value, null);
    }



    /**
     * Create a Date cell
     */
    public Cell createCell(Row row, int idx, LocalDate value, CellStyle style){
        Cell cell = createCell(row, idx, style);
        if(style==null){
            cell.setCellStyle(getStyle(Styles.DATE));
        }
        if(value!=null){
            cell.setCellValue(value);
        }
        return cell;
    }
    public Cell createCell(Row row, int idx, LocalDate value){
        return createCell(row, idx, value, null);
    }


    /**
     * Create a single cell in a row with a Style
     */
    public Cell createCell(Row row,  int idx, CellStyle style){
        Cell cell = createCell(row, idx++);
        if(style!=null){
            cell.setCellStyle(style);
        }
        return cell;
    }

    /**
     * Create a single cell in a row
     */
    public Cell createCell(Row row,  int idx){
        return row.createCell(idx++);
    }


    private void buildDefaultStyles(){
        CellStyle style;
        Font font;

        // default style for headers
        style = wb.createCellStyle();
        font = wb.createFont();
        font.setBold(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(font);
        styles.put(Styles.HEADER, style);

        // default style for integers (int/long)
        style = wb.createCellStyle();
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("#,###;#,###;@"));
        styles.put(Styles.INTEGER, style);

        // default style for decimals (float/double 2d)
        style = wb.createCellStyle();
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("#,##0.00;-#,##0.00;@"));
        styles.put(Styles.DECIMAL2, style);

        // default style for dates
        style = wb.createCellStyle();
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
        styles.put(Styles.DATE, style);


    }


    public CellStyle getStyle(Styles key){
        return styles.get(key);
    }


    public enum Styles{
        HEADER,INTEGER,DECIMAL2,DATE;
    }

}
