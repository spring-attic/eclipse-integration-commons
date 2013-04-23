/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  IBM Corporation - initial API and implementation
 *  Willian Mitsuda <wmitsuda@gmail.com>
 *     - Fix for bug 196553 - [Dialogs] Support IColorProvider/IFontProvider in FilteredItemsSelectionDialog
 *  Peter Friese <peter.friese@gentleware.com>
 *     - Fix for bug 208602 - [Dialogs] Open Type dialog needs accessible labels
 *  Simon Muschel <smuschel@gmx.de> - bug 258493
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.quicksearch.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.progress.UIJob;
import org.springsource.ide.eclipse.commons.quicksearch.core.LineItem;
import org.springsource.ide.eclipse.commons.quicksearch.core.QuickTextQuery;
import org.springsource.ide.eclipse.commons.quicksearch.core.QuickTextQuery.TextRange;
import org.springsource.ide.eclipse.commons.quicksearch.core.QuickTextSearchRequestor;
import org.springsource.ide.eclipse.commons.quicksearch.core.QuickTextSearcher;
import org.springsource.ide.eclipse.commons.quicksearch.core.priority.DefaultPriorityFunction;
import org.springsource.ide.eclipse.commons.quicksearch.core.priority.PrioriTree;
import org.springsource.ide.eclipse.commons.quicksearch.core.priority.PriorityFunction;
import org.springsource.ide.eclipse.commons.quicksearch.util.DocumentFetcher;

/**
 * Shows a list of items to the user with a text entry field for a string
 * pattern used to filter the list of items.
 * 
 * @since 3.3
 */
@SuppressWarnings({ "rawtypes", "restriction", "unchecked" })
public class QuickSearchDialog extends SelectionStatusDialog {
	
//	private class SelectionChangedListener implements ISelectionChangedListener {
//		public SelectionChangedListener(TableViewer list) {
//			list.addSelectionChangedListener(this);
//		}
//
//		@Override
//		public void selectionChanged(SelectionChangedEvent event) {
//			System.out.println("Selection changed: "+event.getSelection());
//		}
//	}

//	public class ScrollListener implements SelectionListener {
//		
//		ScrollBar scrollbar;
//		
//		public ScrollListener(ScrollBar scrollbar) {
//			this.scrollbar = scrollbar;
//			scrollbar.addSelectionListener(this);
//		}
//
//		@Override
//		public void widgetDefaultSelected(SelectionEvent e) {
//			processEvent(e);
//		}
//
//		private int oldPercent = 0;
//		
//		private void processEvent(SelectionEvent e) {
//			int min = scrollbar.getMinimum();
//			int max = scrollbar.getMaximum();
//			int val = scrollbar.getSelection();
//			int thumb = scrollbar.getThumb();
//			
//			int total = max - min; //Total range of the scrollbar
//			int end = val+thumb; //The bottom of visible region
//			int belowEnd = max - end; //Size of area that is below the current visible area.
//			int percent = (belowEnd*100)/total; // size in percentage of total area that is below visible area. 
//			
//			System.out.println("==== scroll event ===");
//			System.out.println("min: "+min +"  max: "+max);
//			System.out.println("val: "+val +"  thum: "+thumb);
//			System.out.println("percent: "+percent);
//			if (percent <= 10) {
//				walker.requestMoreResults();
//			}
//			if (Math.abs(percent-oldPercent)>50) {
//				System.out.println("Big jump!");
//			}
//			oldPercent = percent;
//		}
//
//		@Override
//		public void widgetSelected(SelectionEvent e) {
//			processEvent(e);
//		}
//	}
	
	private UIJob refreshJob = new UIJob("Refresh") {
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			refresh();
			return Status.OK_STATUS;
		}
	};
	
	/**
	 * Job that shows a simple busy indicator while a search is active.
	 * The job must be scheduled when a search starts/resumes. It periodically checks the
	 */
	private UIJob progressJob =  new UIJob("Refresh") {
		int animate = 0; // number of dots to display.
		
		protected String dots(int animate) {
			char[] chars = new char[animate];
			for (int i = 0; i < chars.length; i++) {
				chars[i] = '.';
			}
			return new String(chars);
		}
		
		@Override
		public IStatus runInUIThread(IProgressMonitor mon) {
			if (!mon.isCanceled()) {
				if (walker.isDone()) {
					progressLabel.setText("");
				} else {
					progressLabel.setText("Searching"+dots(animate));
					animate = (animate+1)%4;
					this.schedule(333);
				}
			}
			return Status.OK_STATUS;
		}
	};

	public static final ColumnLabelProvider LINE_NUMBER_LABEL_PROVIDER = new ColumnLabelProvider() {
		public String getText(Object _item) {
			if (_item!=null) {
				LineItem item = (LineItem) _item;
				return ""+item.getLineNumber();
			}
			return "?";
		};
	};

	private static final Color YELLOW = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
	private static final Color GREY = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
	private static final Color CYAN = Display.getCurrent().getSystemColor(SWT.COLOR_CYAN);

	private final StyledCellLabelProvider LINE_TEXT_LABEL_PROVIDER = new StyledCellLabelProvider() {
		@Override
		public void update(ViewerCell cell) {
			LineItem item = (LineItem) cell.getElement();
			if (item!=null) {
				QuickTextQuery query = walker.getQuery();
				String text = item.getText();
				cell.setText(text);
				List<TextRange> ranges = query.findAll(text);
				if (ranges!=null && !ranges.isEmpty()) {
					StyleRange[] styleRanges = new StyleRange[ranges.size()];
					int pos = 0;
					for (TextRange range : ranges) {
						styleRanges[pos++] = new StyleRange(range.start, range.len, null, YELLOW);
					}
					cell.setStyleRanges(styleRanges);
				} else {
					cell.setStyleRanges(null);
				}
			} else {
				cell.setText("");
				cell.setStyleRanges(null);
			}
			super.update(cell);
		}
	};

	private static final StyledCellLabelProvider LINE_FILE_LABEL_PROVIDER = new StyledCellLabelProvider() {

		@Override
		public void update(ViewerCell cell) {
			LineItem item = (LineItem) cell.getElement();
			if (item!=null) {
				IPath path = item.getFile().getFullPath();
				String name = path.lastSegment();
				String dir = path.removeLastSegments(1).toString();
				cell.setText(name + " - "+dir);
				StyleRange[] styleRanges = new StyleRange[] {
						new StyleRange(name.length(), dir.length()+3, GREY, null)
				};
				cell.setStyleRanges(styleRanges);
			} else {
				cell.setText("");
				cell.setStyleRanges(null);
			}
			super.update(cell);
		}
		
//		public String getToolTipText(Object element) {
//			LineItem item = (LineItem) element;
//			if (item!=null) {
//				return ""+item.getFile().getFullPath();
//			}
//			return "";
//		};
		
//		public String getText(Object _item) {
//			if (_item!=null) {
//				LineItem item = (LineItem) _item;
//				return item.getFile().getName().toString();
//			}
//			return "?";
//		};
	};	
	
	private static final String DIALOG_SETTINGS = QuickSearchDialog.class.getName()+".DIALOG_SETTINGS";
	
	private static final String DIALOG_BOUNDS_SETTINGS = "DialogBoundsSettings"; //$NON-NLS-1$

	private static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$
	private static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$
	
	private static final String DIALOG_LAST_QUERY = "LAST_QUERY";

	/**
	 * Represents an empty selection in the pattern input field (used only for
	 * initial pattern).
	 */
	public static final int NONE = 0;

	/**
	 * Pattern input field selection where caret is at the beginning (used only
	 * for initial pattern).
	 */
	public static final int CARET_BEGINNING = 1;

	/**
	 * Represents a full selection in the pattern input field (used only for
	 * initial pattern).
	 */
	public static final int FULL_SELECTION = 2;

	private Text pattern;

	private TableViewer list;

	private MenuManager menuManager;

	private MenuManager contextMenuManager;

	private boolean multi;

	private ToolBar toolBar;

	private ToolItem toolItem;

	private Label progressLabel;

	private IStatus status;

	private ContentProvider contentProvider;

	private String initialPatternText;

	private int selectionMode;

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private IHandlerActivation showViewHandler;

	private QuickTextSearcher walker;

	private IWorkbenchWindow window;

	private Text details;

	private DocumentFetcher documents;

	/**
	 * Creates a new instance of the class.
	 * 
	 * @param window.getShell()
	 *            shell to parent the dialog on
	 * @param multi
	 *            indicates whether dialog allows to select more than one
	 *            position in its list of items
	 */
	public QuickSearchDialog(IWorkbenchWindow window) {
		super(window.getShell());
		this.window = window;
		this.multi = false;
		contentProvider = new ContentProvider();
		selectionMode = NONE;
	}

//	/**
//	 * Returns the label decorator for selected items in the list.
//	 * 
//	 * @return the label decorator for selected items in the list
//	 */
//	private ILabelDecorator getListSelectionLabelDecorator() {
//		return getItemsListLabelProvider().getSelectionDecorator();
//	}
//
//	/**
//	 * Sets the label decorator for selected items in the list.
//	 * 
//	 * @param listSelectionLabelDecorator
//	 *            the label decorator for selected items in the list
//	 */
//	public void setListSelectionLabelDecorator(
//			ILabelDecorator listSelectionLabelDecorator) {
//		getItemsListLabelProvider().setSelectionDecorator(
//				listSelectionLabelDecorator);
//	}

//	/**
//	 * Returns the item list label provider.
//	 * 
//	 * @return the item list label provider
//	 */
//	private ItemsListLabelProvider getItemsListLabelProvider() {
//		if (itemsListLabelProvider == null) {
//			itemsListLabelProvider = new ItemsListLabelProvider(
//					new LabelProvider(), null);
//		}
//		return itemsListLabelProvider;
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {
		super.create();
		pattern.setFocus();
	}

	/**
	 * Restores dialog using persisted settings. 
	 */
	protected void restoreDialog(IDialogSettings settings) {
		if (initialPatternText==null) {
			String lastSearch = settings.get(DIALOG_LAST_QUERY);
			if (lastSearch==null) {
				lastSearch = "";
			}
			pattern.setText(lastSearch);
			pattern.setSelection(0, lastSearch.length());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		this.progressJob.cancel();
		this.progressJob = null;
//		this.refreshProgressMessageJob.cancel();
		if (showViewHandler != null) {
			IHandlerService service = (IHandlerService) PlatformUI
					.getWorkbench().getService(IHandlerService.class);
			service.deactivateHandler(showViewHandler);
			showViewHandler.getHandler().dispose();
			showViewHandler = null;
		}
		if (menuManager != null)
			menuManager.dispose();
		if (contextMenuManager != null)
			contextMenuManager.dispose();
		storeDialog(getDialogSettings());
		return super.close();
	}

	/**
	 * Stores dialog settings.
	 * 
	 * @param settings
	 *            settings used to store dialog
	 */
	protected void storeDialog(IDialogSettings settings) {
		String currentSearch = pattern.getText();
		settings.put(DIALOG_LAST_QUERY, currentSearch);
	}

	/**
	 * Create a new header which is labelled by headerLabel.
	 * 
	 * @param parent
	 * @return Label the label of the header
	 */
	private Label createHeader(Composite parent) {
		Composite header = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		header.setLayout(layout);

		Label headerLabel = new Label(header, SWT.NONE);
		headerLabel.setText((getMessage() != null && getMessage().trim()
				.length() > 0) ? getMessage()
				: WorkbenchMessages.FilteredItemsSelectionDialog_patternLabel);
		headerLabel.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
					e.detail = SWT.TRAVERSE_NONE;
					pattern.setFocus();
				}
			}
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		headerLabel.setLayoutData(gd);

		createViewMenu(header);
		header.setLayoutData(gd);
		return headerLabel;
	}

	/**
	 * Create the labels for the list and the progress. Return the list label.
	 * 
	 * @param parent
	 * @return Label
	 */
	private Label createLabels(Composite parent) {
		Composite labels = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		labels.setLayout(layout);

		Label listLabel = new Label(labels, SWT.NONE);
		listLabel
				.setText(WorkbenchMessages.FilteredItemsSelectionDialog_listLabel);

		listLabel.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
					e.detail = SWT.TRAVERSE_NONE;
					list.getTable().setFocus();
				}
			}
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		listLabel.setLayoutData(gd);

		progressLabel = new Label(labels, SWT.RIGHT);
		progressLabel.setLayoutData(gd);

		labels.setLayoutData(gd);
		return listLabel;
	}

	private void createViewMenu(Composite parent) {
		toolBar = new ToolBar(parent, SWT.FLAT);
		toolItem = new ToolItem(toolBar, SWT.PUSH, 0);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.END;
		toolBar.setLayoutData(data);

		toolBar.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				showViewMenu();
			}
		});

		toolItem.setImage(WorkbenchImages
				.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU));
		toolItem
				.setToolTipText(WorkbenchMessages.FilteredItemsSelectionDialog_menu);
		toolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showViewMenu();
			}
		});

		menuManager = new MenuManager();

		fillViewMenu(menuManager);

		IHandlerService service = (IHandlerService) PlatformUI.getWorkbench()
				.getService(IHandlerService.class);
		IHandler handler = new AbstractHandler() {
			public Object execute(ExecutionEvent event) {
				showViewMenu();
				return null;
			}
		};
		showViewHandler = service.activateHandler(
				IWorkbenchCommandConstants.WINDOW_SHOW_VIEW_MENU, handler,
				new ActiveShellExpression(getShell()));
	}

	/**
	 * Fills the menu of the dialog.
	 * 
	 * @param menuManager
	 *            the menu manager
	 */
	protected void fillViewMenu(IMenuManager menuManager) {
//		toggleStatusLineAction = new ToggleStatusLineAction();
//		menuManager.add(toggleStatusLineAction);
	}

	private void showViewMenu() {
		Menu menu = menuManager.createContextMenu(getShell());
		Rectangle bounds = toolItem.getBounds();
		Point topLeft = new Point(bounds.x, bounds.y + bounds.height);
		topLeft = toolBar.toDisplay(topLeft);
		menu.setLocation(topLeft.x, topLeft.y);
		menu.setVisible(true);
	}

    /**
     * Hook that allows to add actions to the context menu.
	 * <p>
	 * Subclasses may extend in order to add other actions.</p>
     * 
     * @param menuManager the context menu manager
     * @since 3.5
     */
	protected void fillContextMenu(IMenuManager menuManager) {
//		List selectedElements= ((StructuredSelection)list.getSelection()).toList();
//
//		Object item= null;
//
//		for (Iterator it= selectedElements.iterator(); it.hasNext();) {
//			item= it.next();
//			if (item instanceof ItemsListSeparator || !isHistoryElement(item)) {
//				return;
//			}
//		}

//		if (selectedElements.size() > 0) {
//			removeHistoryItemAction.setText(WorkbenchMessages.FilteredItemsSelectionDialog_removeItemsFromHistoryAction);
//
//			menuManager.add(removeHistoryActionContributionItem);
//
//		}
	}

	private void createPopupMenu() {
//		removeHistoryItemAction = new RemoveHistoryItemAction();
//		removeHistoryActionContributionItem = new ActionContributionItem(
//				removeHistoryItemAction);

		contextMenuManager = new MenuManager();
		contextMenuManager.setRemoveAllWhenShown(true);
		contextMenuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});

		final Table table = list.getTable();
		Menu menu= contextMenuManager.createContextMenu(table);
		table.setMenu(menu);
	}

//	/**
//	 * Creates an extra content area, which will be located above the details.
//	 * 
//	 * @param parent
//	 *            parent to create the dialog widgets in
//	 * @return an extra content area
//	 */
//	protected abstract Control createExtendedContentArea(Composite parent);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Composite content = new Composite(dialogArea, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		content.setLayoutData(gd);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		content.setLayout(layout);

		final Label headerLabel = createHeader(content);

		pattern = new Text(content, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		pattern.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				e.result = LegacyActionTools.removeMnemonics(headerLabel
						.getText());
			}
		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		pattern.setLayoutData(gd);

		final Label listLabel = createLabels(content);

		list = new TableViewer(content, (multi ? SWT.MULTI : SWT.SINGLE)
				| SWT.BORDER | SWT.V_SCROLL | SWT.VIRTUAL);
//		ColumnViewerToolTipSupport.enableFor(list, ToolTip.NO_RECREATE); 
		
		list.getTable().setHeaderVisible(true);
		list.getTable().setLinesVisible(true); 
		list.getTable().getAccessible().addAccessibleListener(
				new AccessibleAdapter() {
					public void getName(AccessibleEvent e) {
						if (e.childID == ACC.CHILDID_SELF) {
							e.result = LegacyActionTools
									.removeMnemonics(listLabel.getText());
						}
					}
				});
		list.setContentProvider(contentProvider);
//		new ScrollListener(list.getTable().getVerticalBar());
//		new SelectionChangedListener(list);
		
		TableViewerColumn col = new TableViewerColumn(list, SWT.RIGHT);
		col.setLabelProvider(LINE_NUMBER_LABEL_PROVIDER);
		col.getColumn().setText("Line");
		col.getColumn().setWidth(40);
		col = new TableViewerColumn(list, SWT.LEFT);
		col.getColumn().setText("Text");
		col.setLabelProvider(LINE_TEXT_LABEL_PROVIDER);
		col.getColumn().setWidth(400);
		col = new TableViewerColumn(list, SWT.LEFT);
		col.getColumn().setText("Path");
		col.setLabelProvider(LINE_FILE_LABEL_PROVIDER);
		col.getColumn().setWidth(150);
		
		
		//list.setLabelProvider(getItemsListLabelProvider());
		list.setInput(new Object[0]);
		list.setItemCount(contentProvider.getNumberOfElements());
		gd = new GridData(GridData.FILL_BOTH);
		applyDialogFont(list.getTable());
		gd.heightHint= list.getTable().getItemHeight() * 15;
		list.getTable().setLayoutData(gd);

		createPopupMenu();

		pattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				applyFilter();
			}
		});

		pattern.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					if (list.getTable().getItemCount() > 0) {
						list.getTable().setFocus();
					}
				}
			}
		});

		list.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event
						.getSelection();
				handleSelected(selection);
			}
		});

		list.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick();
			}
		});

		list.getTable().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {

				if (e.keyCode == SWT.ARROW_UP && (e.stateMask & SWT.SHIFT) != 0
						&& (e.stateMask & SWT.CTRL) != 0) {
					StructuredSelection selection = (StructuredSelection) list
							.getSelection();

					if (selection.size() == 1) {
						Object element = selection.getFirstElement();
						if (element.equals(list.getElementAt(0))) {
							pattern.setFocus();
						}
						list.getTable().notifyListeners(SWT.Selection,
								new Event());

					}
				}

				if (e.keyCode == SWT.ARROW_DOWN
						&& (e.stateMask & SWT.SHIFT) != 0
						&& (e.stateMask & SWT.CTRL) != 0) {

					list.getTable().notifyListeners(SWT.Selection, new Event());
				}

			}
		});
		
		createDetailsArea(content);

		applyDialogFont(content);

		restoreDialog(getDialogSettings());

		if (initialPatternText != null) {
			pattern.setText(initialPatternText);
		}

		switch (selectionMode) {
		case CARET_BEGINNING:
			pattern.setSelection(0, 0);
			break;
		case FULL_SELECTION:
			pattern.setSelection(0, initialPatternText.length());
			break;
		}

		// apply filter even if pattern is empty (display history)
		applyFilter();

		return dialogArea;
	}

	private void createDetailsArea(Composite parent) {
		details = new Text(parent, SWT.MULTI+SWT.READ_ONLY+SWT.BORDER);
		details.setText("Line 1\nLine 2\nLine 3\nLine 4\nLine5");
		details.setFont(JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(details);
		
		list.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				refreshDetails();
			}
		});
	}
	
	private void refreshDetails() {
		if (details!=null && list!=null && !details.isDisposed() && !list.getTable().isDisposed()) {
			if (documents==null) {
				documents = new DocumentFetcher();
			}
			IStructuredSelection sel = (IStructuredSelection) list.getSelection();
			if (sel!=null && !sel.isEmpty()) {
				//Not empty selection
				LineItem item = (LineItem) sel.getFirstElement();
				IDocument document = documents.getDocument(item.getFile());
				try {
					int line = item.getLineNumber();
					int start = document.getLineOffset(Math.max(line-3, 0));
					int end = document.getLength();
					try {
						end = document.getLineOffset(line+2);
					} catch (BadLocationException e) {
						//Presumably line number is past the end of document.
						//ignore.
					}
					details.setText(document.get(start, end-start));
					return;
				} catch (BadLocationException e) {
				}
			}
			details.setText("");
		}
	}

	/**
	 * This method is a hook for subclasses to override default dialog behavior.
	 * The <code>handleDoubleClick()</code> method handles double clicks on
	 * the list of filtered elements.
	 * <p>
	 * Current implementation makes double-clicking on the list do the same as
	 * pressing <code>OK</code> button on the dialog.
	 */
	protected void handleDoubleClick() {
		okPressed();
	}

	/**
	 * Handle selection in the items list by updating labels of selected and
	 * unselected items and refresh the details field using the selection.
	 * 
	 * @param selection
	 *            the new selection
	 */
	protected void handleSelected(StructuredSelection selection) {
		IStatus status = new Status(IStatus.OK, PlatformUI.PLUGIN_ID,
				IStatus.OK, EMPTY_STRING, null);

		updateStatus(status);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Dialog#getDialogBoundsSettings()
	 */
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_BOUNDS_SETTINGS);
		if (section == null) {
			section = settings.addNewSection(DIALOG_BOUNDS_SETTINGS);
			section.put(DIALOG_HEIGHT, 500);
			section.put(DIALOG_WIDTH, 600);
		}
		return section;
	}

	/**
	 * Returns the dialog settings. Returned object can't be null.
	 * 
	 * @return return dialog settings for this dialog
	 */
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = IDEWorkbenchPlugin.getDefault()
				.getDialogSettings().getSection(DIALOG_SETTINGS);

		if (settings == null) {
			settings = IDEWorkbenchPlugin.getDefault().getDialogSettings()
					.addNewSection(DIALOG_SETTINGS);
		}

		return settings;
	}

	/**
	 * Refreshes the dialog - has to be called in UI thread.
	 */
	public void refresh() {
		if (list != null && !list.getTable().isDisposed()) {
			ScrollBar sb = list.getTable().getVerticalBar();
			int oldScroll = sb.getSelection();
			list.setItemCount(contentProvider.getNumberOfElements());
			list.refresh(true, false);
			int newScroll = sb.getSelection();
			if (oldScroll!=newScroll) {
				System.out.println("Scroll moved in refresh: "+oldScroll+ " => " + newScroll);
			}
			//sb.setSelection((int) Math.floor(oldScroll*sb.getMaximum()));
		}
//			
// The code below attempts to preserve selection, but it also messes up the
// scroll position (reset to 0) in the common case where selection is first element
// and more elements are getting added as I scroll down to bottom of list.
//			
//			
//			list.getTable().deselectAll();
//
//			list.setItemCount(contentProvider.getNumberOfElements());
//			list.refresh(/*updateLabels*/true, /*reveal*/false);
//
//			if (list.getTable().getItemCount() > 0) {
//				// preserve previous selection
//				if (lastRefreshSelection != null) {
//					if (lastRefreshSelection.size() > 0) {
//						list.setSelection(new StructuredSelection(lastRefreshSelection), false);
//					}
//				} else {
//					list.setSelection(StructuredSelection.EMPTY, false);
//				}
//			} else {
//				list.setSelection(StructuredSelection.EMPTY);
//			}
//		}
	}

	/**
	 * Schedule refresh job.
	 */
	public void scheduleRefresh() {
		refreshJob.schedule();
//		list.re
//		refreshCacheJob.cancelAll();
//		refreshCacheJob.schedule();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	protected void computeResult() {
		List objectsToReturn = ((StructuredSelection) list.getSelection())
				.toList();
		if (objectsToReturn.isEmpty()) {
			//Pretend that the first element is selected.
			Object first = list.getElementAt(0);
			if (first!=null) {
				objectsToReturn = Arrays.asList(first);
			}
		}
		setResult(objectsToReturn);
	}

	/*
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#updateStatus(org.eclipse.core.runtime.IStatus)
	 */
	protected void updateStatus(IStatus status) {
		this.status = status;
		super.updateStatus(status);
	}

	/*
	 * @see Dialog#okPressed()
	 */
	protected void okPressed() {
		
		if (status != null
				&& (status.isOK() || status.getCode() == IStatus.INFO)) {
			super.okPressed();
		}
	}

	/**
	 * Sets the initial pattern used by the filter. This text is copied into the
	 * selection input on the dialog. A full selection is used in the pattern
	 * input field.
	 * 
	 * @param text
	 *            initial pattern for the filter
	 * @see QuickSearchDialog#FULL_SELECTION
	 */
	public void setInitialPattern(String text) {
		setInitialPattern(text, FULL_SELECTION);
	}

	/**
	 * Sets the initial pattern used by the filter. This text is copied into the
	 * selection input on the dialog. The <code>selectionMode</code> is used
	 * to choose selection type for the input field.
	 * 
	 * @param text
	 *            initial pattern for the filter
	 * @param selectionMode
	 *            one of: {@link QuickSearchDialog#NONE},
	 *            {@link QuickSearchDialog#CARET_BEGINNING},
	 *            {@link QuickSearchDialog#FULL_SELECTION}
	 */
	public void setInitialPattern(String text, int selectionMode) {
		this.initialPatternText = text;
		this.selectionMode = selectionMode;
	}

	/**
	 * Gets initial pattern.
	 * 
	 * @return initial pattern, or <code>null</code> if initial pattern is not
	 *         set
	 */
	protected String getInitialPattern() {
		return this.initialPatternText;
	}

	/**
	 * Returns the current selection.
	 * 
	 * @return the current selection
	 */
	protected StructuredSelection getSelectedItems() {

		StructuredSelection selection = (StructuredSelection) list
				.getSelection();

		List selectedItems = selection.toList();
		
		return new StructuredSelection(selectedItems);
	}

	/**
	 * Validates the item. When items on the items list are selected or
	 * deselected, it validates each item in the selection and the dialog status
	 * depends on all validations.
	 * 
	 * @param item
	 *            an item to be checked
	 * @return status of the dialog to be set
	 */
	protected IStatus validateItem(Object item) {
		return new Status(IStatus.OK, QuickSearchActivator.PLUGIN_ID, "fine");
	}

	/**
	 * Creates an instance of a filter.
	 * 
	 * @return a filter for items on the items list. Can be <code>null</code>,
	 *         no filtering will be applied then, causing no item to be shown in
	 *         the list.
	 */
	protected QuickTextQuery createFilter() {
		return new QuickTextQuery(pattern.getText());
	}

	/**
	 * Applies the filter created by <code>createFilter()</code> method to the
	 * items list. When new filter is different than previous one it will cause
	 * refiltering.
	 */
	protected void applyFilter() {
		QuickTextQuery newFilter = createFilter();
		if (newFilter.isTrivial()) {
			return;
		}
		if (this.walker==null) {
			//Create the QuickTextSearcher with the inital query.
			this.walker = new QuickTextSearcher(newFilter, createPriorityFun(), new QuickTextSearchRequestor() {
				@Override
				public void add(LineItem match) {
					contentProvider.add(match);
					contentProvider.refresh();
				}
				@Override
				public void clear() {
					contentProvider.reset();
					contentProvider.refresh();
				}
				@Override
				public void revoke(LineItem match) {
					contentProvider.remove(match);
					contentProvider.refresh();
				}
				@Override
				public void update(LineItem match) {
					contentProvider.refresh();
				}
			});
			refresh();
//			this.list.setInput(input)
		} else {
			//The QuickTextSearcher is already active update the query
			this.walker.setQuery(newFilter);
		}
		if (progressJob!=null) {
			progressJob.schedule();
		}
	}
	
	/**
	 * Gets the IFile that is currently open in the active editor.
	 * @return IFile or null if there is no current editor or the editor isn't associated to a file.
	 */
	private IFile getActiveFile() {
		IWorkbenchPage page = window.getActivePage();
		if (page!=null) {
			IEditorPart editor = page.getActiveEditor();
			if (editor!=null) {
				IEditorInput input = editor.getEditorInput();
				if (input!=null) {
					return (IFile) input.getAdapter(IFile.class);
				}
			}
		}
		return null;
	}

	/**
	 * We remember the last result of getOpenFiles in here. This is so that we can return this
	 * if we are having trouble to compute the open files. Sometimes we may not be able to
	 * access the active workbench page etc. In this case it is probably better to return
	 * a stale list of files than nothing at all.
	 */
	private static Collection<IFile> lastOpenFiles = Arrays.asList(); //Empty list to start with.
	
	private Collection<IFile> getOpenFiles() {
		System.out.println(">>> open files");
		try {
			IWorkbenchPage page = window.getActivePage();
			if (page!=null) {
				Collection<IFile> files = new ArrayList<IFile>();
				IEditorReference[] editors = page.getEditorReferences();
				if (editors!=null) {
					for (IEditorReference editor : editors) {
						try {
							IEditorInput input = editor.getEditorInput();
							if (input!=null) {
								IFile file = (IFile) input.getAdapter(IFile.class);
								System.out.println(file.getFullPath());
								files.add(file);
							}
						} catch (PartInitException e) {
							QuickSearchActivator.log(e);
						}
					}
					lastOpenFiles = files;
					return files;
				}
			}
			return lastOpenFiles;
		} finally {
			System.out.println(">>> open files");
		}
	}

	
	/**
	 * Create a walker priority function based on the current 'context' (i.e. for now this means the open editors).
	 */
	private PriorityFunction createPriorityFun() {
		PrioriTree priorities = new PrioriTree();
		try {
			IFile currentFile = getActiveFile();
			if (currentFile!=null) {
				priorities.setPriority(currentFile.getFullPath(), PriorityFunction.PRIORITY_VISIT_FIRST);
			}
			Collection<IFile> openFiles = getOpenFiles();
			for (IFile file : openFiles) {
				priorities.setPriority(file.getFullPath(), PriorityFunction.PRIORITY_INTERESTING);
			}
			
			return priorities;
		} catch (Throwable e) {
			QuickSearchActivator.log(e);
		}
		return new DefaultPriorityFunction();
	}

	/**
	 * Returns name for then given object.
	 * 
	 * @param item
	 *            an object from the content provider. Subclasses should pay
	 *            attention to the passed argument. They should either only pass
	 *            objects of a known type (one used in content provider) or make
	 *            sure that passed parameter is the expected one (by type
	 *            checking like <code>instanceof</code> inside the method).
	 * @return name of the given item
	 */
	public String getElementName(Object item) {
		return ""+item;
//		return (String)item; // Assuming the items are strings for now
	}

	/**
	 * Collects filtered elements. Contains one synchronized, sorted set for
	 * collecting filtered elements. 
	 * Implementation of <code>ItemsFilter</code> is used to filter elements.
	 * The key function of filter used in to filtering is
	 * <code>matchElement(Object item)</code>.
	 * <p>
	 * The <code>ContentProvider</code> class also provides item filtering
	 * methods. The filtering has been moved from the standard TableView
	 * <code>getFilteredItems()</code> method to content provider, because
	 * <code>ILazyContentProvider</code> and virtual tables are used. This
	 * class is responsible for adding a separator below history items and
	 * marking each items as duplicate if its name repeats more than once on the
	 * filtered list.
	 */
	private class ContentProvider implements IStructuredContentProvider, ILazyContentProvider {

		private List items;

		/**
		 * Creates new instance of <code>ContentProvider</code>.
		 */
		public ContentProvider() {
			this.items = Collections.synchronizedList(new ArrayList(2048));
//			this.duplicates = Collections.synchronizedSet(new HashSet(256));
//			this.lastSortedItems = Collections.synchronizedList(new ArrayList(
//					2048));
		}

		public void remove(LineItem match) {
			this.items.remove(match);
		}

		/**
		 * Removes all content items and resets progress message.
		 */
		public void reset() {
			this.items.clear();
		}

		/**
		 * Adds filtered item.
		 * 
		 * @param match
		 */
		public void add(LineItem match) {
			this.items.add(match);
		}

		/**
		 * Refresh dialog.
		 */
		public void refresh() {
			scheduleRefresh();
		}

//		/**
//		 * Removes items from history and refreshes the view.
//		 * 
//		 * @param item
//		 *            to remove
//		 * 
//		 * @return removed item
//		 */
//		public Object removeHistoryElement(Object item) {
//			if (this.selectionHistory != null)
//				this.selectionHistory.remove(item);
//			if (filter == null || filter.getPattern().length() == 0) {
//				items.remove(item);
//				duplicates.remove(item);
//				this.lastSortedItems.remove(item);
//			}
//
//			synchronized (lastSortedItems) {
//				Collections.sort(lastSortedItems, getHistoryComparator());
//			}
//			return item;
//		}

//		/**
//		 * Adds item to history and refresh view.
//		 * 
//		 * @param item
//		 *            to add
//		 */
//		public void addHistoryElement(Object item) {
//			if (this.selectionHistory != null)
//				this.selectionHistory.accessed(item);
//			if (filter == null || !filter.matchItem(item)) {
//				this.items.remove(item);
//				this.duplicates.remove(item);
//				this.lastSortedItems.remove(item);
//			}
//			synchronized (lastSortedItems) {
//				Collections.sort(lastSortedItems, getHistoryComparator());
//			}
//			this.refresh();
//		}

//		/**
//		 * Sets/unsets given item as duplicate.
//		 * 
//		 * @param item
//		 *            item to change
//		 * 
//		 * @param isDuplicate
//		 *            duplicate flag
//		 */
//		public void setDuplicateElement(Object item, boolean isDuplicate) {
//			if (this.items.contains(item)) {
//				if (isDuplicate)
//					this.duplicates.add(item);
//				else
//					this.duplicates.remove(item);
//			}
//		}

//		/**
//		 * Indicates whether given item is a duplicate.
//		 * 
//		 * @param item
//		 *            item to check
//		 * @return <code>true</code> if item is duplicate
//		 */
//		public boolean isDuplicateElement(Object item) {
//			return duplicates.contains(item);
//		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return items.toArray();
		}

		public int getNumberOfElements() {
			return items.size();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILazyContentProvider#updateElement(int)
		 */
		public void updateElement(int index) {

			QuickSearchDialog.this.list.replace((items
					.size() > index) ? items.get(index) : null,
					index);

		}

	}

	/**
	 * Get the control where the search pattern is entered. Any filtering should
	 * be done using an {@link ItemsFilter}. This control should only be
	 * accessed for listeners that wish to handle events that do not affect
	 * filtering such as custom traversal.
	 * 
	 * @return Control or <code>null</code> if the pattern control has not
	 *         been created.
	 */
	public Control getPatternControl() {
		return pattern;
	}

	public QuickTextQuery getQuery() {
		return walker.getQuery();
	}

}
