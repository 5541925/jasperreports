/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2011 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jasperreports.components.headertoolbar;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import net.sf.jasperreports.components.BaseElementHtmlHandler;
import net.sf.jasperreports.components.headertoolbar.actions.EditColumnHeaderData;
import net.sf.jasperreports.components.headertoolbar.actions.EditColumnValueData;
import net.sf.jasperreports.components.headertoolbar.actions.FilterAction;
import net.sf.jasperreports.components.headertoolbar.actions.SortAction;
import net.sf.jasperreports.components.sort.FieldFilter;
import net.sf.jasperreports.components.sort.FilterTypeDateOperatorsEnum;
import net.sf.jasperreports.components.sort.FilterTypeNumericOperatorsEnum;
import net.sf.jasperreports.components.sort.FilterTypeTextOperatorsEnum;
import net.sf.jasperreports.components.sort.FilterTypesEnum;
import net.sf.jasperreports.components.sort.actions.FilterCommand;
import net.sf.jasperreports.components.sort.actions.FilterData;
import net.sf.jasperreports.components.sort.actions.SortData;
import net.sf.jasperreports.components.table.BaseColumn;
import net.sf.jasperreports.components.table.StandardColumn;
import net.sf.jasperreports.components.table.StandardTable;
import net.sf.jasperreports.components.table.util.TableUtil;
import net.sf.jasperreports.engine.DatasetFilter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.JRIdentifiable;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JRPropertiesUtil.PropertySuffix;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JRSortField;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.ReportContext;
import net.sf.jasperreports.engine.base.JRBasePrintHyperlink;
import net.sf.jasperreports.engine.design.JRDesignComponentElement;
import net.sf.jasperreports.engine.design.JRDesignDataset;
import net.sf.jasperreports.engine.design.JRDesignDatasetRun;
import net.sf.jasperreports.engine.design.JRDesignTextElement;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRHtmlExporterContext;
import net.sf.jasperreports.engine.export.JRXhtmlExporter;
import net.sf.jasperreports.engine.type.JREnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.util.JRColorUtil;
import net.sf.jasperreports.repo.JasperDesignCache;
import net.sf.jasperreports.web.WebReportContext;
import net.sf.jasperreports.web.commands.CommandTarget;
import net.sf.jasperreports.web.servlets.ReportServlet;
import net.sf.jasperreports.web.servlets.ResourceServlet;
import net.sf.jasperreports.web.util.JacksonUtil;
import net.sf.jasperreports.web.util.VelocityUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id:ChartThemesUtilities.java 2595 2009-02-10 17:56:51Z teodord $
 */
public class HeaderToolbarElementHtmlHandler extends BaseElementHtmlHandler
{
	private static final Log log = LogFactory.getLog(HeaderToolbarElementHtmlHandler.class);
	
	private static final String RESOURCE_HEADERTOOLBAR_JS = "net/sf/jasperreports/components/headertoolbar/resources/jasperreports-tableHeaderToolbar.js";
	private static final String RESOURCE_HEADERTOOLBAR_CSS = "net/sf/jasperreports/components/headertoolbar/resources/jasperreports-tableHeaderToolbar.vm.css";

	private static final String CSS_FILTER_DISABLED = 		"filterBtnDisabled";
	private static final String CSS_FILTER_DEFAULT = 		"filterBtnDefault";
	private static final String CSS_FILTER_DEFAULT_HOVER = 	"filterBtnDefaultHover";
	private static final String CSS_FILTER_ENABLED = 		"filterBtnEnabled";
	private static final String CSS_FILTER_ENABLED_HOVER = 	"filterBtnEnabledHover";
	private static final String CSS_FILTER_WRONG = 			"filterBtnWrong";
	private static final String CSS_FILTER_WRONG_HOVER = 	"filterBtnWrongHover";
	
	private static final String CSS_SORT_DEFAULT_ASC = 			"sortAscBtnDefault";
	private static final String CSS_SORT_DEFAULT_ASC_HOVER = 	"sortAscBtnDefaultHover";
	private static final String CSS_SORT_ENABLED_ASC = 			"sortAscBtnEnabled";
	private static final String CSS_SORT_ENABLED_ASC_HOVER = 	"sortAscBtnEnabledHover";
	
	private static final String CSS_SORT_DEFAULT_DESC = 		"sortDescBtnDefault";
	private static final String CSS_SORT_DEFAULT_DESC_HOVER = 	"sortDescBtnDefaultHover";
	private static final String CSS_SORT_ENABLED_DESC = 		"sortDescBtnEnabled";
	private static final String CSS_SORT_ENABLED_DESC_HOVER = 	"sortDescBtnEnabledHover";
	
	private static final String SORT_ELEMENT_HTML_TEMPLATE = 	"net/sf/jasperreports/components/headertoolbar/resources/HeaderToolbarElementHtmlTemplate.vm";
	
	private static final String PARAM_GENERATED_TEMPLATE_PREFIX = "net.sf.jasperreports.headertoolbar.";
	
	
	private static class CustomJRExporterParameter extends JRExporterParameter{

		protected CustomJRExporterParameter(String name) {
			super(name);
		}
	}
	
	private static final CustomJRExporterParameter param = new HeaderToolbarElementHtmlHandler.CustomJRExporterParameter("exporter_first_attempt");

	public String getHtmlFragment(JRHtmlExporterContext context, JRGenericPrintElement element)
	{
		boolean templateAlreadyLoaded = false;
		boolean exporterFirstAttempt = true;

		String htmlFragment = null;
		ReportContext reportContext = context.getExporter().getReportContext();
		if (reportContext != null)//FIXMEJIVE
		{
			
			String tableUUID = (String) element.getParameterValue(HeaderToolbarElement.PARAMETER_TABLE_UUID);
			String sortColumnName = (String) element.getParameterValue(HeaderToolbarElement.PARAMETER_COLUMN_NAME);
			Map<String, Object> contextMap = new HashMap<String, Object>();
			
			if (reportContext.getParameterValue(PARAM_GENERATED_TEMPLATE_PREFIX + tableUUID) != null) {
				templateAlreadyLoaded = true;
			} else {
				reportContext.setParameterValue(PARAM_GENERATED_TEMPLATE_PREFIX + tableUUID, true);
				setAllColumnNames(element, context.getJasperReportsContext(), contextMap);
			}
			
			if (context.getExportParameters().containsKey(param) && (Boolean)context.getExportParameters().get(param)) {
				exporterFirstAttempt = false;
			} else {
				context.getExportParameters().put(param, Boolean.TRUE);
			}
			
			contextMap.put("exporterFirstAttempt", exporterFirstAttempt);
			
			String sortColumnLabel = (String) element.getParameterValue(HeaderToolbarElement.PARAMETER_COLUMN_LABEL);
			String sortColumnType = (String) element.getParameterValue(HeaderToolbarElement.PARAMETER_COLUMN_TYPE);
			
			String popupId = (String) element.getParameterValue("popupId");
			Integer columnIndex = (Integer) element.getParameterValue("columnIndex");
			
			contextMap.put("columnLabel", sortColumnLabel);
			
			FilterTypesEnum filterType = FilterTypesEnum.getByName(element.getPropertiesMap().getProperty(HeaderToolbarElement.PROPERTY_FILTER_TYPE));
			if (filterType == null)//FIXMEJIVE
			{
				return null;
			}
			
			String filterPattern = element.getPropertiesMap().getProperty(HeaderToolbarElement.PROPERTY_FILTER_PATTERN);
			if (filterPattern == null) {
				filterPattern = "";
			}

			Locale locale = (Locale) reportContext.getParameterValue(JRParameter.REPORT_LOCALE);
			
			if (log.isDebugEnabled()) {
				log.debug("report locale: " + locale);
			}
			
			if (locale == null) {
				locale = Locale.getDefault();
			}
			
			Map<String, String> translatedOperators = null;
			Map<String, String> valuesFormatPatternMap = new LinkedHashMap<String, String>();
			String formatPatternLabel = "";
			switch (filterType) {
				case NUMERIC:
					translatedOperators = getTranslatedOperators(FilterTypeNumericOperatorsEnum.class.getName(), FilterTypeNumericOperatorsEnum.values(), locale);
					valuesFormatPatternMap.put("###,###.###", "123,456.789");
					formatPatternLabel = "Number pattern:";
					break;
				case DATE:
					translatedOperators = getTranslatedOperators(FilterTypeDateOperatorsEnum.class.getName(), FilterTypeDateOperatorsEnum.values(), locale);
					valuesFormatPatternMap.put("dd.MM.yy", "30.01.12");
					valuesFormatPatternMap.put("yyyy-MM-dd", "2012-01-30");
					formatPatternLabel = "Date pattern:";
					break;
				case TEXT:
					translatedOperators = getTranslatedOperators(FilterTypeTextOperatorsEnum.class.getName(), FilterTypeTextOperatorsEnum.values(), locale);
					formatPatternLabel = "!?Text pattern!?:";
					break;
			}
			
			contextMap.put("valuesFormatPatternMap", valuesFormatPatternMap);
			contextMap.put("formatPatternLabel", formatPatternLabel);
			contextMap.put("templateAlreadyLoaded", templateAlreadyLoaded);

			String webResourcesBasePath = JRPropertiesUtil.getInstance(context.getJasperReportsContext()).getProperty("net.sf.jasperreports.web.resources.base.path");
			if (webResourcesBasePath == null)
			{
				webResourcesBasePath = ResourceServlet.DEFAULT_PATH + "?" + ResourceServlet.RESOURCE_URI + "=";
			}

			contextMap.put("jasperreports_tableHeaderToolbar_js", webResourcesBasePath + HeaderToolbarElementHtmlHandler.RESOURCE_HEADERTOOLBAR_JS);
			contextMap.put("jasperreports_tableHeaderToolbar_css", getDynamicResourceLink(webResourcesBasePath, HeaderToolbarElementHtmlHandler.RESOURCE_HEADERTOOLBAR_CSS));
			contextMap.put("elementX", ((JRXhtmlExporter)context.getExporter()).toSizeUnit(element.getX()));
			contextMap.put("elementY", ((JRXhtmlExporter)context.getExporter()).toSizeUnit(element.getY()));
			contextMap.put("elementWidth", element.getWidth());
			contextMap.put("elementHeight", element.getHeight());
			
			contextMap.put("filterColumnNameLabel", sortColumnLabel != null ? sortColumnLabel : "");
			
			
			
			contextMap.put("actionBaseUrl", getActionBaseUrl(context));
			contextMap.put("actionBaseData", getActionBaseJsonData(context));

			// begin:temp
			if (popupId != null) {
				contextMap.put("popupId", popupId);
			}
			if (columnIndex != null) {
				contextMap.put("columnIndex", columnIndex);
				setColumnHeaderData(sortColumnLabel, columnIndex, tableUUID, contextMap, context.getJasperReportsContext(), reportContext);
				setColumnValueData(sortColumnLabel, columnIndex, tableUUID, contextMap, context.getJasperReportsContext(), reportContext);
			}
			SortData sortAscData = new SortData(tableUUID, sortColumnName, sortColumnType, HeaderToolbarElement.SORT_ORDER_ASC);
			SortData sortDescData = new SortData(tableUUID, sortColumnName, sortColumnType, HeaderToolbarElement.SORT_ORDER_DESC);
	
			String sortAscActive = CSS_SORT_DEFAULT_ASC;
			String sortAscHover = CSS_SORT_DEFAULT_ASC_HOVER;
			String sortDescActive = CSS_SORT_DEFAULT_DESC;
			String sortDescHover = CSS_SORT_DEFAULT_DESC_HOVER;
			String filterActive = CSS_FILTER_DISABLED;
			String filterHover = "";
			
			if (filterType != null) {
				filterActive = CSS_FILTER_DEFAULT;
				filterHover = CSS_FILTER_DEFAULT_HOVER;
			}
			// end:temp
			
			
			if (element.getModeValue() == ModeEnum.OPAQUE)
			{
				contextMap.put("backgroundColor", JRColorUtil.getColorHexa(element.getBackcolor()));
			}

			String sortField = getCurrentSortField(context.getJasperReportsContext(), reportContext, tableUUID, sortColumnName, sortColumnType);
			if (sortField != null) 
			{
				String[] sortActionData = HeaderToolbarElementUtils.extractColumnInfo(sortField);
				
				boolean isAscending = HeaderToolbarElement.SORT_ORDER_ASC.equals(sortActionData[2]);
				if (isAscending) {
					sortAscData.setSortOrder(HeaderToolbarElement.SORT_ORDER_NONE);
					sortAscActive = CSS_SORT_ENABLED_ASC;
					sortAscHover = CSS_SORT_ENABLED_ASC_HOVER;
				} else {
					sortDescData.setSortOrder(HeaderToolbarElement.SORT_ORDER_NONE);
					sortDescActive = CSS_SORT_ENABLED_DESC;
					sortDescHover = CSS_SORT_ENABLED_DESC_HOVER;
				}
			}
			
			// existing filters
			String filterValueStart = "";
			String filterValueEnd = "";
			String filterTypeOperatorValue = "";
			List<DatasetFilter> fieldFilters = getExistingFiltersForField(context.getJasperReportsContext(), reportContext, tableUUID, sortColumnName);

			if (fieldFilters.size() > 0) {
				FieldFilter ff = (FieldFilter)fieldFilters.get(0);
				if (ff.getFilterValueStart() != null) {
					filterValueStart = ff.getFilterValueStart();
				}
				if (ff.getFilterValueEnd() != null) {
					filterValueEnd = ff.getFilterValueEnd();
				}
				filterTypeOperatorValue = ff.getFilterTypeOperator();
				if (ff.getIsValid() != null && !ff.getIsValid()) {
					filterActive = CSS_FILTER_WRONG;
					filterHover = CSS_FILTER_WRONG_HOVER;
				} else {
					filterActive = CSS_FILTER_ENABLED;
					filterHover = CSS_FILTER_ENABLED_HOVER;
				}
			}
			
			// params for clear filter
			contextMap.put("filterToRemoveParamvalue", sortColumnName);
			
			// begin: the params that will generate the JSON post object for filtering
			FilterData filterData = new FilterData();
			filterData.setUuid(tableUUID);
			filterData.setFieldName(sortColumnName);
			filterData.setFilterType(filterType.getName());
			filterData.setFilterPattern(filterPattern);
			filterData.setFieldValueStart(filterValueStart);
			filterData.setFieldValueEnd(filterValueEnd);
//			fd.setFilterTypeOperator(filterTypeOperatorValue);
			
			contextMap.put("filterData", JacksonUtil.getInstance(context.getJasperReportsContext()).getJsonString(filterData));
			contextMap.put("filterTypeValuesMap", translatedOperators);
			contextMap.put("filterTypeOperatorValue", filterTypeOperatorValue);
			contextMap.put("filterTableUuid", tableUUID);
			// end
			
			// begin:temp
			contextMap.put("sortAscData", JacksonUtil.getInstance(context.getJasperReportsContext()).getJsonString(sortAscData));
			contextMap.put("sortDescData", JacksonUtil.getInstance(context.getJasperReportsContext()).getJsonString(sortDescData));
			
			contextMap.put("sortAscActive", sortAscActive);
			contextMap.put("sortAscHover", sortAscHover);
			contextMap.put("sortDescActive", sortDescActive);
			contextMap.put("sortDescHover", sortDescHover);
			contextMap.put("filterActive", filterActive);
			contextMap.put("filterHover", filterHover);
			// end: temp
			
			htmlFragment = VelocityUtil.processTemplate(HeaderToolbarElementHtmlHandler.SORT_ELEMENT_HTML_TEMPLATE, contextMap);
		}
		
		return htmlFragment;
	}
	
	private String getActionBaseUrl(JRHtmlExporterContext context) {
		JRBasePrintHyperlink hyperlink = new JRBasePrintHyperlink();
		hyperlink.setLinkType("ReportExecution");
		return context.getHyperlinkURL(hyperlink);
	}

	private String getActionBaseJsonData(JRHtmlExporterContext context) {
		ReportContext reportContext = context.getExporter().getReportContext();
		Map<String, Object> actionParams = new HashMap<String, Object>();
		actionParams.put(WebReportContext.REQUEST_PARAMETER_REPORT_CONTEXT_ID, reportContext.getId());
		actionParams.put(ReportServlet.REQUEST_PARAMETER_RUN_REPORT, true);
		
//		return JacksonUtil.getInstance(context.getJasperReportsContext()).getEscapedJsonString(actionParams);
		return JacksonUtil.getInstance(context.getJasperReportsContext()).getJsonString(actionParams);
	}

	private String getCurrentSortField(
		JasperReportsContext jasperReportsContext,
		ReportContext reportContext, 
		String uuid, 
		String sortColumnName, 
		String sortColumnType
		) 
	{
		JasperDesignCache cache = JasperDesignCache.getInstance(jasperReportsContext, reportContext);
		SortAction action = new SortAction();
		action.init(jasperReportsContext, reportContext);
		CommandTarget target = action.getCommandTarget(UUID.fromString(uuid));
		if (target != null)
		{
			JRIdentifiable identifiable = target.getIdentifiable();
			JRDesignComponentElement componentElement = identifiable instanceof JRDesignComponentElement ? (JRDesignComponentElement)identifiable : null;
			StandardTable table = componentElement == null ? null : (StandardTable)componentElement.getComponent();
			
			JRDesignDatasetRun datasetRun = (JRDesignDatasetRun)table.getDatasetRun();
			
			String datasetName = datasetRun.getDatasetName();
			
			JasperDesign jasperDesign = cache.getJasperDesign(target.getUri());//FIXMEJIVE getJasperReport not design
			JRDesignDataset dataset = (JRDesignDataset)jasperDesign.getDatasetMap().get(datasetName);
			
			List<JRSortField> existingFields =  dataset.getSortFieldsList();
			String sortField = null;
	
			if (existingFields != null && existingFields.size() > 0) {
				for (JRSortField field: existingFields) {
					if (field.getName().equals(sortColumnName) && field.getType().getName().equals(sortColumnType)) {
						sortField = sortColumnName + HeaderToolbarElement.SORT_COLUMN_TOKEN_SEPARATOR + sortColumnType + HeaderToolbarElement.SORT_COLUMN_TOKEN_SEPARATOR;
						switch (field.getOrderValue()) {
							case ASCENDING:
								sortField += HeaderToolbarElement.SORT_ORDER_ASC;
								break;
							case DESCENDING:
								sortField += HeaderToolbarElement.SORT_ORDER_DESC;
								break;
						}
						break;
					}
				}
			}
		
			return sortField;
		}
		
		return null;
	}
	
	public boolean toExport(JRGenericPrintElement element) {
		return true;
	}
	
	private Map<String, String> getTranslatedOperators(String bundleName, JREnum[] operators, Locale locale) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		ResourceBundle rb = ResourceBundle.getBundle(bundleName, locale);
		
		for (JREnum operator: operators) {
			result.put(((Enum<?>)operator).name(), rb.getString(((Enum<?>)operator).name()));
		}
		
		return result;
	}
	
	private List<DatasetFilter> getExistingFiltersForField(
		JasperReportsContext jasperReportsContext, 
		ReportContext reportContext, 
		String uuid, 
		String filterFieldName
		) 
	{
		JasperDesignCache cache = JasperDesignCache.getInstance(jasperReportsContext, reportContext);
		FilterAction action = new FilterAction();
		action.init(jasperReportsContext, reportContext);
		CommandTarget target = action.getCommandTarget(UUID.fromString(uuid));
		List<DatasetFilter> result = new ArrayList<DatasetFilter>();
		if (target != null)
		{
			JRIdentifiable identifiable = target.getIdentifiable();
			JRDesignComponentElement componentElement = identifiable instanceof JRDesignComponentElement ? (JRDesignComponentElement)identifiable : null;
			StandardTable table = componentElement == null ? null : (StandardTable)componentElement.getComponent();
			
			JRDesignDatasetRun datasetRun = (JRDesignDatasetRun)table.getDatasetRun();
			
			String datasetName = datasetRun.getDatasetName();
			
			JasperDesign jasperDesign = cache.getJasperDesign(target.getUri());//FIXMEJIVE getJasperReport not design
			JRDesignDataset dataset = (JRDesignDataset)jasperDesign.getDatasetMap().get(datasetName);
			
			// get existing filter as JSON string
			String serializedFilters = "[]";
			JRPropertiesMap propertiesMap = dataset.getPropertiesMap();
			if (propertiesMap.getProperty(FilterCommand.DATASET_FILTER_PROPERTY) != null) {
				serializedFilters = propertiesMap.getProperty(FilterCommand.DATASET_FILTER_PROPERTY);
			}
			
			ObjectMapper mapper = new ObjectMapper();
			List<DatasetFilter> existingFilters = null;
			try {
				existingFilters = mapper.readValue(serializedFilters, new TypeReference<List<FieldFilter>>(){});
			} catch (Exception e) {
				throw new JRRuntimeException(e);
			}
			
			if (existingFilters.size() > 0) {
				for (DatasetFilter filter: existingFilters) {
					if (((FieldFilter)filter).getField().equals(filterFieldName)) {
						result.add(filter);
						break;
					}
				}
			}
		}
		
		return result;		
	}
	
	private void setColumnHeaderData(String sortColumnLabel, Integer columnIndex, String tableUuid, Map<String, Object> contextMap, JasperReportsContext jasperReportsContext, ReportContext reportContext) {
		FilterAction action = new FilterAction();
		action.init(jasperReportsContext, reportContext);
		CommandTarget target = action.getCommandTarget(UUID.fromString(tableUuid));
		EditColumnHeaderData colHeaderData = new EditColumnHeaderData();
		
		if (target != null){
			JRIdentifiable identifiable = target.getIdentifiable();
			JRDesignComponentElement componentElement = identifiable instanceof JRDesignComponentElement ? (JRDesignComponentElement)identifiable : null;
			StandardTable table = componentElement == null ? null : (StandardTable)componentElement.getComponent();
			
			List<BaseColumn> tableColumns = TableUtil.getAllColumns(table);
			
			if (columnIndex != null) {
				StandardColumn column = (StandardColumn) tableColumns.get(columnIndex);
				
				JRDesignTextElement textElement = TableUtil.getColumnHeaderTextElement(column);
				
				if (textElement != null) {
					colHeaderData.setHeadingName(sortColumnLabel);
					colHeaderData.setColumnIndex(columnIndex);
					colHeaderData.setTableUuid(tableUuid);
					colHeaderData.setFontName(textElement.getFontName());
					colHeaderData.setFontSize(textElement.getFontSize());
					colHeaderData.setFontBold(textElement.isBold());
					colHeaderData.setFontItalic(textElement.isItalic());
					colHeaderData.setFontUnderline(textElement.isUnderline());
					colHeaderData.setFontColor(JRColorUtil.getColorHexa(textElement.getForecolor()));
					colHeaderData.setFontHAlign(textElement.getHorizontalAlignmentValue().getName());
				}
			}
		}
		contextMap.put("colHeaderData", JacksonUtil.getInstance(jasperReportsContext).getJsonString(colHeaderData));
	}

	private void setColumnValueData(String sortColumnLabel, Integer columnIndex, String tableUuid, Map<String, Object> contextMap, JasperReportsContext jasperReportsContext, ReportContext reportContext) {
		FilterAction action = new FilterAction();
		action.init(jasperReportsContext, reportContext);
		CommandTarget target = action.getCommandTarget(UUID.fromString(tableUuid));
		EditColumnValueData colValueData = new EditColumnValueData();
		
		if (target != null){
			JRIdentifiable identifiable = target.getIdentifiable();
			JRDesignComponentElement componentElement = identifiable instanceof JRDesignComponentElement ? (JRDesignComponentElement)identifiable : null;
			StandardTable table = componentElement == null ? null : (StandardTable)componentElement.getComponent();
			
			List<BaseColumn> tableColumns = TableUtil.getAllColumns(table);
			
			if (columnIndex != null) {
				StandardColumn column = (StandardColumn) tableColumns.get(columnIndex);
				
				JRDesignTextField textElement = (JRDesignTextField)TableUtil.getColumnDetailTextElement(column);
				
				if (textElement != null) {
					colValueData.setHeadingName(sortColumnLabel);
					colValueData.setColumnIndex(columnIndex);
					colValueData.setTableUuid(tableUuid);
					colValueData.setFontName(textElement.getFontName());
					colValueData.setFontSize(textElement.getFontSize());
					colValueData.setFontBold(textElement.isBold());
					colValueData.setFontItalic(textElement.isItalic());
					colValueData.setFontUnderline(textElement.isUnderline());
					colValueData.setFontColor(JRColorUtil.getColorHexa(textElement.getForecolor()));
					colValueData.setFontHAlign(textElement.getHorizontalAlignmentValue().getName());
					
					contextMap.put("formatPatternValue", textElement.getPattern());
				}
			}
		}
		contextMap.put("colValueData", JacksonUtil.getInstance(jasperReportsContext).getJsonString(colValueData));
	}

	public static class ColumnInfo {
		private String index;
		private String label;
		private boolean enabled;
		
		private ColumnInfo(String index, String label, boolean enabled) {
			this.index = index;
			this.label = label;
			this.enabled = enabled;
		}
		
		public String getIndex() {
			return index;
		}
		
		public String getLabel() {
			return label;
		}
		
		public boolean getEnabled() {
			return enabled;
		}
	}

	private void setAllColumnNames(JRGenericPrintElement element, JasperReportsContext jasperReportsContext, Map<String, Object> contextMap) {
		
		List<PropertySuffix> props =  JRPropertiesUtil.getInstance(jasperReportsContext).getAllProperties(element, HeaderToolbarElement.PARAM_COLUMN_LABEL_PREFIX);
		Map<String, ColumnInfo> columnNames = new HashMap<String, ColumnInfo>();

		for (PropertySuffix prop: props) {
			String columnName = prop.getValue();
			if (columnName == null || columnName.length() == 0) {
				columnName = "Column_" + prop.getSuffix();
			}
			columnNames.put(prop.getSuffix(), new ColumnInfo(prop.getSuffix(), columnName, false));
		}
		
		contextMap.put("allColumnNames", JacksonUtil.getInstance(jasperReportsContext).getJsonString(columnNames));
	}
	
	private String getDynamicResourceLink(String webResourceBasePath, String resourcePath) {
		String encodedPath;
		try {
			encodedPath = URLEncoder.encode(webResourceBasePath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new JRRuntimeException(e);
		}
		
		return webResourceBasePath + resourcePath + "&" + ResourceServlet.RESOURCE_IS_DYNAMIC + "=true&" + ResourceServlet.SERVLET_PATH + "=" + encodedPath;
	}

}
