package org.springsource.ide.eclipse.commons.quicksearch.util;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Resizes table columns such that columns fit the table width. The table is
 * initially resized when the resize is enabled, and its columns continue being
 * resized automatically as the user resizes the table.
 * 
 */
public class TableResizeHelper {

	private final TableViewer tableViewer;

	public TableResizeHelper(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}

	public void enableResizing() {
		tableViewer.getTable().addControlListener(new ControlListener() {

			public void controlResized(ControlEvent e) {
				resizeTable();
			}

			public void controlMoved(ControlEvent e) {

			}
		});

		// Initial resize of the columns
		resizeTable();

	}

	protected void resizeTable() {
		Composite tableComposite = tableViewer.getTable();//.getParent();
		Rectangle tableCompositeArea = tableComposite.getClientArea();
		int width = tableCompositeArea.width;
//		ScrollBar sb = tableViewer.getTable().getVerticalBar();
//		if (sb!=null && sb.isVisible()) {
//			width = width - sb.getSize().x;
//		}
		resizeTableColumns(width, tableViewer.getTable());
	}
	
	protected void resizeTableColumns(int tableWidth, Table table) {
		TableColumn[] tableColumns = table.getColumns();

		if (tableColumns.length == 0) {
			return;
		}

		int total = 0;

		// resize only if there is empty space at the end of the table
		for (TableColumn column : tableColumns) {
			total += column.getWidth();
		}

		//if (total < tableWidth) {
			// resize the last one
			TableColumn lastColumn = tableColumns[tableColumns.length - 1];
			int newWidth = (tableWidth - total) + lastColumn.getWidth(); //TODO: why 4 ???
			if (newWidth>0) {
				lastColumn.setWidth(newWidth);
			}
		//	lastColumn.setWidth(20);
		//}

	}

}
