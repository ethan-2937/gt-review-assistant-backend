package com.jzyz.gtreviewassistant.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class SimpleXlsxWriter {
    private SimpleXlsxWriter() {
    }

    public record Sheet(String name, List<String> headers, List<List<String>> rows) {
    }

    public static byte[] write(List<Sheet> sheets) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
                entry(zip, "[Content_Types].xml", contentTypes(sheets.size()));
                entry(zip, "_rels/.rels", rootRels());
                entry(zip, "xl/workbook.xml", workbook(sheets));
                entry(zip, "xl/_rels/workbook.xml.rels", workbookRels(sheets.size()));
                for (int i = 0; i < sheets.size(); i++) {
                    entry(zip, "xl/worksheets/sheet" + (i + 1) + ".xml", worksheet(sheets.get(i)));
                }
            }
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("failed to write xlsx", ex);
        }
    }

    private static void entry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypes(int sheetCount) {
        StringBuilder xml = new StringBuilder("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>");
        for (int i = 1; i <= sheetCount; i++) {
            xml.append("<Override PartName=\"/xl/worksheets/sheet")
                    .append(i)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        xml.append("</Types>");
        return xml.toString();
    }

    private static String rootRels() {
        return "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private static String workbook(List<Sheet> sheets) {
        StringBuilder xml = new StringBuilder("<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets>");
        for (int i = 0; i < sheets.size(); i++) {
            xml.append("<sheet name=\"")
                    .append(xml(sheets.get(i).name()))
                    .append("\" sheetId=\"")
                    .append(i + 1)
                    .append("\" r:id=\"rId")
                    .append(i + 1)
                    .append("\"/>");
        }
        xml.append("</sheets></workbook>");
        return xml.toString();
    }

    private static String workbookRels(int sheetCount) {
        StringBuilder xml = new StringBuilder("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        for (int i = 1; i <= sheetCount; i++) {
            xml.append("<Relationship Id=\"rId")
                    .append(i)
                    .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet")
                    .append(i)
                    .append(".xml\"/>");
        }
        xml.append("</Relationships>");
        return xml.toString();
    }

    private static String worksheet(Sheet sheet) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(sheet.headers());
        rows.addAll(sheet.rows());
        StringBuilder xml = new StringBuilder("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        for (int r = 0; r < rows.size(); r++) {
            xml.append("<row r=\"").append(r + 1).append("\">");
            List<String> row = rows.get(r);
            for (int c = 0; c < row.size(); c++) {
                String ref = columnName(c + 1) + (r + 1);
                xml.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"><is><t>")
                        .append(xml(cleanCell(row.get(c))))
                        .append("</t></is></c>");
            }
            xml.append("</row>");
        }
        xml.append("</sheetData></worksheet>");
        return xml.toString();
    }

    private static String columnName(int number) {
        StringBuilder name = new StringBuilder();
        int current = number;
        while (current > 0) {
            current--;
            name.insert(0, (char) ('A' + current % 26));
            current /= 26;
        }
        return name.toString();
    }

    private static String cleanCell(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
    }

    private static String xml(String value) {
        return cleanCell(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
