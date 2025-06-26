import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ExcelAggregator {
    public static void main(String[] args) throws Exception {
        String folderPath = "your_folder_path_here"; // TODO: 修改为你的文件夹路径
        String outputFile = "aggregated_result.xlsx";

        // 用于去重和存储结果
        Set<String> uniqueRows = new LinkedHashSet<>();
        List<String[]> resultRows = new ArrayList<>();

        // 遍历文件夹下所有Excel文件
        Files.walk(Paths.get(folderPath))
                .filter(p -> p.toString().endsWith(".xlsx") || p.toString().endsWith(".xls"))
                .forEach(path -> {
                    try (InputStream is = Files.newInputStream(path);
                         Workbook workbook = WorkbookFactory.create(is)) {
                        for (int i = 0; i < 3 && i < workbook.getNumberOfSheets(); i++) {
                            Sheet sheet = workbook.getSheetAt(i);
                            String sheetName = sheet.getSheetName();
                            for (Row row : sheet) {
                                if (row.getRowNum() == 0) continue; // 跳过表头
                                Cell cell1 = row.getCell(0);
                                Cell cell2 = row.getCell(1);
                                String col1 = cell1 != null ? cell1.toString().trim() : "";
                                String col2 = cell2 != null ? cell2.toString().trim() : "";
                                String key = path.getFileName() + "|" + col1 + "|" + col2 + "|" + sheetName;
                                if (!col1.isEmpty() || !col2.isEmpty()) {
                                    if (uniqueRows.add(key)) {
                                        resultRows.add(new String[]{
                                                path.getFileName().toString(), col1, col2, sheetName
                                        });
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing file: " + path + " - " + e.getMessage());
                    }
                });

        // 写入新的Excel文件
        try (Workbook outWb = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            Sheet outSheet = outWb.createSheet("Result");
            Row header = outSheet.createRow(0);
            header.createCell(0).setCellValue("文件名");
            header.createCell(1).setCellValue("column 1");
            header.createCell(2).setCellValue("column 2");
            header.createCell(3).setCellValue("sheet");

            int rowIdx = 1;
            for (String[] rowData : resultRows) {
                Row row = outSheet.createRow(rowIdx++);
                for (int i = 0; i < rowData.length; i++) {
                    row.createCell(i).setCellValue(rowData[i]);
                }
            }
            outWb.write(fos);
        }
        System.out.println("汇总完成，结果文件：" + outputFile);
    }
}