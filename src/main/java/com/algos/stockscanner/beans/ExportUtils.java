package com.algos.stockscanner.beans;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ExportUtils {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Create a String cell
     */
    public Cell createCell(Workbook wb, Row row, int idx, String value, CellStyle style){
        Cell cell = createCell(row, idx, style);
        if(value!=null){
            cell.setCellValue(value);
        }
        return cell;
    }

    /**
     * Create a Number (float) cell
     */
    public Cell createCell(Workbook wb, Row row, int idx, float value, CellStyle style){
        Cell cell = createCell(row, idx, style);
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("#,##0.00;-#,##0.00;@"));
        cell.setCellValue(value);
        return cell;
    }

    /**
     * Create a Integer cell
     */
    public Cell createCell(Workbook wb, Row row, int idx, int value, CellStyle style){
        Cell cell = createCell(row, idx, style);
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("#,###;#,###;@"));
        cell.setCellValue(value);
        return cell;
    }

    /**
     * Create a Long cell
     */
    public Cell createCell(Workbook wb, Row row, int idx, long value, CellStyle style){
        Cell cell = createCell(row, idx, style);
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("#,###;#,###;@"));
        cell.setCellValue(value);
        return cell;
    }


    /**
     * Create a Date cell
     */
    public Cell createCell(Workbook wb, Row row, int idx, LocalDate value, CellStyle style){
        Cell cell = createCell(row, idx, style);
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
        if(value!=null){
            cell.setCellValue(value);
        }
        return cell;
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



}
