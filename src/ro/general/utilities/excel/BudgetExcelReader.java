package ro.general.utilities.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.FuncVarPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ro.general.utilities.util.Utils;

/**
 * Generic class that processes a budget excel file (one sheet only) and generates pie charts. 
 * Given the large number of entries in the excel the items to have the pie chart generated from are in the list: listOfCellsToBeCheckedandCharted
 * The formulas inside the targeted cell are evaluated and the sum content is used for the chart. 
 * 
 * The folowing can be provided: 
 * - the name and location of budget file
 * - the name and location of the generated excel with the charts
 * - the number of characters that a sheet can have in its name
 * - the number of the column in the budget excel where to get the values from 
 * - the number of the column in the budget excel where to get the labels from 
 * - the cells to be checked - put in a list
 * 
 *   
 * @author Mihaela Munteanu
 * @since 10.01.2020
 *
 */
public class BudgetExcelReader {
	private static String BUGET_EXCEL_LOCATION = "C:\\Mihaela\\Other projects\\Primarie\\21. Buget\\Buget_decembrie_2020.xlsx";
	private static String BUGET_CHARTS_EXCEL_LOCATION = "C:\\Mihaela\\Other projects\\Primarie\\21. Buget\\Buget_decembrie_2020_charts.xlsx";
	private static int numberOfCharactersPerSheet = 20;
	private static int columnNumberForValues = 5;
	private static int columnNumbersForLabel = 27;
	private static int[] listOfCellsToBeCheckedandCharted = {281, 282, 387,441, 488, 497, 513, 528, 562, 632, 669, 730, 802, 858, 903, 944, 983, 1002, 1057};
	public static List<String> sheetNames = new ArrayList<String>(); 
	
	public static void main(String[] args) throws Exception {
    	Workbook workbookBuget = null;
    	Workbook workbookCharts = null;
    	
        try {
            FileInputStream bugetExcelFile = new FileInputStream(new File(BUGET_EXCEL_LOCATION));
            workbookBuget = new XSSFWorkbook(bugetExcelFile);
            
//            FileInputStream chartsExcelFile = new FileInputStream(new File(BUGET_CHARTS_EXCEL_LOCATION));
            workbookCharts = new XSSFWorkbook();
            
            processSheetBugetLocal(workbookBuget, "11.01", workbookCharts);
            
			// Write output to an excel file
			try (FileOutputStream fileOut = new FileOutputStream(BUGET_CHARTS_EXCEL_LOCATION)) {
				workbookCharts.write(fileOut);
			}
			
            workbookBuget.close();
            workbookCharts.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
			
		}
	}

	private static void processSheetBugetLocal(Workbook workbookBuget, String sheetName, Workbook workbookChart) throws IOException {
		Sheet sheetBuget = workbookBuget.getSheet(sheetName);
//		Iterator<Row> iteratorByRows = sheetBuget.iterator();
//		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		XSSFEvaluationWorkbook formulaParsingWorkbook = XSSFEvaluationWorkbook.create((XSSFWorkbook) workbookBuget);
		FormulaEvaluator evaluatorWorkbookBuget = workbookBuget.getCreationHelper().createFormulaEvaluator(); 
		
		DataFormatter dataFormatter = new DataFormatter();
		//!Utils.isEmpty(dataFormatter.formatCellValue(currentRow.getCell(1)).trim())
		
//		while (iteratorByRows.hasNext()) {
		for (int i = 0; i <= sheetBuget.getLastRowNum(); i++) {
//		    Row currentRow = iteratorByRows.next();
//		    Iterator<Cell> cellIterator = currentRow.cellIterator();
//		    while (cellIterator.hasNext()) {
//		    	Cell currentCell = cellIterator.next();
		    //Do only for column F
		    if (isInCellsToBeCheckedList(i) && 
		    		sheetBuget.getRow(i)!= null && sheetBuget.getRow(i).getCell(columnNumberForValues) != null) {
		    	Row currentRow = sheetBuget.getRow(i);
		    	Cell currentCell = currentRow.getCell(columnNumberForValues);
		    	switch (currentCell.getCellType()) {
		    	    case BOOLEAN:
		    	        System.out.print(currentCell.getBooleanCellValue());
		    	        break;
		    	    case NUMERIC:
		    	        System.out.print(currentCell.getNumericCellValue());
		    	        break;
		    	    case STRING:
		    	        System.out.print(currentCell.getStringCellValue());
		    	        break;
		    	    case BLANK:
		    	        break;
		    	    case ERROR:
		    	        break;
		    	    // CELL_TYPE_FORMULA will never happen
		    	    case FORMULA: 
		    	    	if (currentCell.getCellFormula().contains("SUM")) {
		    	    		System.out.println(currentCell.getCellFormula() + " ");
		    	    		//take this info and write data and generate chart to the new workbook and sheet
		    	    		writeChart(workbookBuget, sheetBuget, workbookChart, formulaParsingWorkbook, dataFormatter, currentRow, currentCell, evaluatorWorkbookBuget);
		    	    	}

		    	        break;
		    	    case _NONE:
		    	    	break;
		    	}
		    }
		}
	}

	private static boolean isInCellsToBeCheckedList(int currentRowNum) {
//		int currentRowNum = currentRow.getRowNum();
		for (int cellIndex : listOfCellsToBeCheckedandCharted) {
			if (currentRowNum == cellIndex - 1)
				return true;
		}
		return false;
	}

	private static void writeChart(Workbook workbookBuget, Sheet sheetBuget, Workbook workbookChart, XSSFEvaluationWorkbook formulaParsingWorkbook,
			DataFormatter dataFormatter, Row currentRow, Cell currentCell, FormulaEvaluator evaluatorWorkbookBuget) {
		//read the name of the row it can be in the first, second or third row (better all together)
		String numeRand = getNumeRand(dataFormatter, currentRow, evaluatorWorkbookBuget);
		
		String numeSheet = numeRand.substring(0, numeRand.length() > numberOfCharactersPerSheet ? numberOfCharactersPerSheet : numeRand.length());
		short index = 1;
		while (sheetNames.contains(numeSheet)) {
			if (sheetNames.contains(numeSheet)) {
				numeSheet += String.valueOf(index);
			}
			index++;
		}
		sheetNames.add(numeSheet);
		
		XSSFSheet sheetChart = ((XSSFWorkbook) workbookChart).createSheet(numeSheet);
		
		//parse the formula to get the content cells
		Ptg[] sharedFormulaPtg = FormulaParser.parse(currentCell.getCellFormula(), formulaParsingWorkbook, FormulaType.CELL, 0);
		Row firstRowChart = sheetChart.createRow((short) 0);
		Cell firstCellChartLabel = firstRowChart.createCell((short) 0); firstCellChartLabel.setCellValue(numeRand);
		firstCellChartLabel.setCellStyle(constructCellStyleBold(workbookChart));
		Cell secondCellChartValue = firstRowChart.createCell((short) 1); secondCellChartValue.setCellValue(currentCell.getNumericCellValue());
		
		int termsCount = 1;
		for (Ptg ptg : sharedFormulaPtg) {
			if (!(ptg instanceof FuncVarPtg) && !ptg.isBaseToken()) { //true for operand 
				//get value and name from the cell
				String cellReferenceString = ptg.toFormulaString();
				
				if (!cellReferenceString.contains(":")) {
					CellReference cellReference = new CellReference(cellReferenceString);
					Row rowSubtermen = sheetBuget.getRow(cellReference.getRow());
					Cell cellSubtermen = rowSubtermen.getCell(cellReference.getCol());
					String numeSubcamp = getNumeRand(dataFormatter, rowSubtermen, evaluatorWorkbookBuget);
					
					Row rowChart = sheetChart.createRow((short) termsCount);
					Cell cellChartLabel = rowChart.createCell((short) 0); cellChartLabel.setCellValue(numeSubcamp);
					Cell cellChartValue = rowChart.createCell((short) 1); cellChartValue.setCellValue(cellSubtermen.getNumericCellValue());
					termsCount++;
				} else {
					String[] splitRange = cellReferenceString.split(":");
					CellReference cellReferenceStart = new CellReference(splitRange[0]);
					CellReference cellReferenceStop = new CellReference(splitRange[1]);
					for (int row = cellReferenceStart.getRow(); row <= cellReferenceStop.getRow(); row++) {
						Row rowSubtermen = sheetBuget.getRow(row);
						Cell cellSubtermen = rowSubtermen.getCell(cellReferenceStart.getCol());
						String numeSubcamp = getNumeRand(dataFormatter, rowSubtermen, evaluatorWorkbookBuget);
						
						Row rowChart = sheetChart.createRow((short) termsCount);
						Cell cellChartLabel = rowChart.createCell((short) 0); cellChartLabel.setCellValue(numeSubcamp);
						Cell cellChartValue = rowChart.createCell((short) 1); cellChartValue.setCellValue(cellSubtermen.getNumericCellValue());
						termsCount++;	
					}
				}
			}
		}
		
		if(termsCount > 1) {
			drawChart(sheetChart, numeRand, termsCount, currentCell.getNumericCellValue()); //primul rand e pentru titlu
		}
		
	}

	private static CellStyle constructCellStyleBold(Workbook workbookChart) {
		Font titluSheetFont = workbookChart.createFont();
		titluSheetFont.setFontName("Calibri");
		titluSheetFont.setFontHeightInPoints((short)12);
		titluSheetFont.setBold(true);
//		CreationHelper createHelper = workbookChart.getCreationHelper();
        CellStyle cellStyle = workbookChart.createCellStyle();
//        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.MM.yyyy"));
//        cellStyle.setAlignment(HorizontalAlignment.LEFT);
//        cellStyle.setFillBackgroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
//        cellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
//        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFont(titluSheetFont);
        
        return cellStyle;
	}

	private static void drawChart(XSSFSheet sheetChart, String nume, int numberOfValues, double totalValue) {
		//don't try to draw a chart
		if (totalValue == 0) return;
		
		XSSFDrawing drawing = sheetChart.createDrawingPatriarch();
		XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 3, 0, 16, 41);

		XSSFChart chart = drawing.createChart(anchor);
		chart.setTitleText("Pie Chart " + nume);
		chart.setTitleOverlay(false);
//		chart.set

		XDDFChartLegend legend = chart.getOrAddLegend();
		legend.setPosition(LegendPosition.TOP_RIGHT);

		XDDFDataSource<String> labels = XDDFDataSourcesFactory.fromStringCellRange(sheetChart,
				new CellRangeAddress(1, numberOfValues - 1, 0, 0)); //este zero based

		XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheetChart,
				new CellRangeAddress(1, numberOfValues - 1, 1, 1)); //este zero based

		XDDFChartData data = chart.createData(ChartTypes.PIE, null, null);// chart.createData(ChartTypes.PIE,
																			// null, null);
	    
		data.setVaryColors(true);
		data.addSeries(labels, values); //data.getSeries(numberOfValues).
		
		
		// Add data labels
	    if (!chart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(0).isSetDLbls()) {
	        chart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(0).addNewDLbls();
	    }
	    chart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(0).getDLbls().addNewShowVal().setVal(true);
	    chart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(0).getDLbls().addNewShowSerName().setVal(false);
	    chart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(0).getDLbls().addNewShowCatName().setVal(false);
	    chart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(0).getDLbls().addNewShowPercent().setVal(true);
	    chart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(0).getDLbls().addNewShowLegendKey().setVal(false);
	    
	    //chart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(0).getDLbls().addNewNumFmt().setFormatCode("0,00");
	    chart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(0).getDLbls().addNewNumFmt();
	    chart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(0).getDLbls().getNumFmt().setSourceLinked(false);
	    chart.getCTChart().getPlotArea().getPieChartArray(0).getSerArray(0).getDLbls().getNumFmt().setFormatCode("#,##0.00");
	    
	    //plot data
		chart.plot(data);
		
	}

	private static String getNumeRand(DataFormatter dataFormatter, Row currentRow, FormulaEvaluator evaluatorWorkbookBuget) {
		
		String numeRand = "";
		for (int i = 0;i <= 2; i++) {
			int columnNumber = columnNumbersForLabel - 1 + i;
			String continutCell =
					!Utils.isEmpty(dataFormatter.formatCellValue(currentRow.getCell(columnNumber)).trim()) ? currentRow.getCell(columnNumber).getStringCellValue() : "";
//			String continutCell = "" 
//					!Utils.isEmpty(dataFormatter.formatCellValue(currentRow.getCell(columnNumber)).trim()) ? dataFormatter.formatCellValue(currentRow.getCell(columnNumber)) : "";
//			if (currentRow.getCell(columnNumber) != null) {
//				Cell cell = currentRow.getCell(columnNumber); 
//				CellType cellType = evaluatorWorkbookBuget.evaluateFormulaCell(currentRow.getCell(columnNumber));
//			    switch (cellType) {
//			        case BOOLEAN:
//			            System.out.println(cell.getBooleanCellValue());
//			            break;
//			        case NUMERIC:
//			            System.out.println(cell.getNumericCellValue());
//			            break;o
//			        case STRING:
//			            System.out.println(cell.getStringCellValue());
//			            break;
//				default:
//					break;
//			    }
//			}
//			
			numeRand += continutCell;
		}
		
		if (Utils.isEmpty(numeRand)) {
			for (int i = 0;i <= 2; i++) {
				int columnNumber = 0 + i;
				String continutCell =
						!Utils.isEmpty(dataFormatter.formatCellValue(currentRow.getCell(columnNumber)).trim()) ? currentRow.getCell(columnNumber).getStringCellValue() : "";
				numeRand += continutCell;
			}	
		}
		return numeRand;
	}
    
}