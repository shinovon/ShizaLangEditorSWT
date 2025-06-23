import java.io.IOException;

import org.eclipse.ercp.swt.mobile.Command;
import org.eclipse.ercp.swt.mobile.MobileShell;
import org.eclipse.ercp.swt.mobile.QueryDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class LocaleEditor implements Runnable, SelectionListener {
	
	private Display display;
	private MobileShell shell;
	
	private boolean exiting;
	private Composite parent;
	//private Composite buttons;
	private Command loadjarrcmd;
	private Command loadusercmd;
	private Command savefilecmd;
	private Command previewwcmd;
	private Table table;
	private Command exitcmd;
	//private Text name;
	//private Text author;
	private Command settnamecmd;
	private Command setavtorcmd;
	private Command setavtvkcmd;
	private Command filegroupcmd;

	LocaleEditor() {
		new Thread(this).start();
	}

	public void run() {
		display = new Display();
		shell = new MobileShell(display, SWT.NONE, 2);
		shell.setLayout(new GridLayout());
		parent = new Composite(shell, SWT.NONE);
		init();
		while (!exiting) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
	
	private void msg(String s) {
		MessageBox msg = new MessageBox(shell);
		msg.setMessage(s);
		msg.open();
	}

	private void init() {
		/*if(true) {
			loadedinit();
			return;
		}*/
		try {
			ShizaLocale.init();
			FileDialog fd = new FileDialog(shell);
			fd.setText("Load from client jar");
			fd.setFilterExtensions(new String[] { "*.jar", "*.jar0", "*.jarr" });
			String f = fd.open();
			System.out.println(f);
			if(f == null) exit();
			QueryDialog qd = new QueryDialog(shell);
			qd.setPromptText("Choose language file to load from client (without \".txt\")", "english");
			String l = qd.open();
			if(l == null) l = "english";
			ShizaLocale.load(f, l);
			loadedinit();
		} catch (IOException e) {
			e.printStackTrace();
			msg("Error: " + e);
			exit();
		}
	}
	
	private void loadedinit() {
		exitcmd = new Command(shell, Command.EXIT, 1);
		exitcmd.setText("Exit");
		exitcmd.addSelectionListener(this);
		MessageBox qmsg = new MessageBox(shell, SWT.OK | SWT.CANCEL);
		qmsg.setMessage("Load user language file?");
		if(qmsg.open() == SWT.OK) {
			try {
				FileDialog fd = new FileDialog(shell);
				fd.setText("Load user language");
				fd.setFilterExtensions(new String[] { "*.txt" });
				String f = fd.open();
				if(f != null) ShizaLocale.loadKeys(f);
			} catch (IOException e) {
				e.printStackTrace();
				msg("Error: " + e);
			}
		} else {
			
		}
		parent.setLayout(new GridLayout());
        parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		final GridData layoutData = new GridData();
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.verticalAlignment = GridData.FILL;
		final GridData fillHorizontal = new GridData();
		fillHorizontal.horizontalAlignment = GridData.FILL;
		fillHorizontal.grabExcessHorizontalSpace = true;
		fillHorizontal.verticalAlignment = GridData.CENTER;
		final GridData buttonLayout = new GridData();
		buttonLayout.grabExcessHorizontalSpace = true;
		buttonLayout.verticalAlignment = GridData.CENTER;
		
		/*
		buttons = new Composite(parent, SWT.NONE);
		buttons.setLayoutData(fillHorizontal);
		buttons.setLayout(new GridLayout());
		name = new Text(buttons, SWT.BORDER);
		name.setLayoutData(fillHorizontal);
		author = new Text(buttons, SWT.BORDER);
		author.setLayoutData(fillHorizontal);
		
		loadjarcmd = new Button(buttons, SWT.PUSH);
		loadjarcmd.setText("Load jar");
		loadjarcmd.addSelectionListener(this);
		loadusercmd = new Button(buttons, SWT.PUSH);
		loadusercmd.setText("Load user file");
		loadusercmd.addSelectionListener(this);
		loadsavecmd = new Button(buttons, SWT.PUSH);
		loadsavecmd.setText("Save");
		loadsavecmd.addSelectionListener(this);
		loadprevcmd = new Button(buttons, SWT.PUSH);
		loadprevcmd.setText("Preview");
		loadprevcmd.addSelectionListener(this);
		*/
		
		filegroupcmd = new Command(shell, Command.COMMANDGROUP, 9);
		filegroupcmd.setText("File");
		
		previewwcmd = new Command(filegroupcmd, Command.GENERAL, 2);
		previewwcmd.setText("Preview");
		previewwcmd.addSelectionListener(this);
		
		setavtvkcmd = new Command(filegroupcmd, Command.GENERAL, 5);
		setavtvkcmd.setText("Set author vk");
		setavtvkcmd.addSelectionListener(this);
		
		setavtorcmd = new Command(filegroupcmd, Command.GENERAL, 6);
		setavtorcmd.setText("Set author");
		setavtorcmd.addSelectionListener(this);
		
		settnamecmd = new Command(filegroupcmd, Command.GENERAL, 7);
		settnamecmd.setText("Set name");
		settnamecmd.addSelectionListener(this);
		
		loadjarrcmd = new Command(filegroupcmd, Command.GENERAL, 8);
		loadjarrcmd.setText("Load jar");
		loadjarrcmd.addSelectionListener(this);
		
		loadusercmd = new Command(filegroupcmd, Command.GENERAL, 9);
		loadusercmd.setText("Load user file");
		loadusercmd.addSelectionListener(this);
		
		savefilecmd = new Command(shell, Command.OK, 10);
		savefilecmd.setText("Save");
		savefilecmd.addSelectionListener(this);

		table = new Table(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setLayoutData(layoutData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		TableColumn keycolumn = new TableColumn(table, SWT.NONE);
		keycolumn.setText("Key");
		keycolumn.setWidth(160);
		TableColumn valcolumn = new TableColumn(table, SWT.NONE);
		valcolumn.setText("Value");
		valcolumn.setWidth(260);
		System.out.println(table.getFont().toString());
		String s = table.getFont().getFontData()[0].getName();
		System.out.println(s);
		table.setFont(new Font(null, "Nokia Sans S60", 6, SWT.NORMAL));
		shell.open();
	    table();
	}
	
	private void set(TableItem item, String v) {
		String k = item.getText(0);
		System.out.println(k+"=>"+v);
		ShizaLocale.set(k, v);
	}
	
	protected boolean moved = true;

	public void table() {
		new Thread() {
			public void run() {
				System.gc();
				for (int i = 0; i < 340; i++) {
					display.syncExec(new Runnable() {
						public void run() {
							new TableItem(table, SWT.NONE).setText(new String[] { "_", "_" });
						}
					});
				}
				System.gc();
				display.syncExec(new Runnable() {
					public void run() {
						upd();
						final TableEditor editor = new TableEditor(table);
						editor.horizontalAlignment = SWT.LEFT;
						editor.grabHorizontal = true;
						table.addSelectionListener(new SelectionListener() {
							public void widgetSelected(SelectionEvent event) {
								final TableItem item = (TableItem) event.item;
								final Text text = new Text(table, SWT.NONE);
								Listener textListener = new Listener() {
									public void handleEvent(final Event e) {
										switch (e.type) {
										case SWT.FocusOut:
											item.setText(1, text.getText());
											set(item, text.getText());
											text.dispose();
											break;
										case SWT.Traverse:
											switch (e.detail) {
											case SWT.TRAVERSE_RETURN:
												item.setText(1, text.getText());
												set(item, text.getText());
												// FALL THROUGH
											case SWT.TRAVERSE_ESCAPE:
												text.dispose();
												e.doit = false;
											}
											break;
										}
									}
								};
								text.addListener(SWT.FocusOut, textListener);
								text.addListener(SWT.Traverse, textListener);
								editor.setEditor(text, item, 1);
								text.setText(item.getText(1));
								text.addModifyListener(new ModifyListener() {
									public void modifyText(ModifyEvent e) {
										set(item, text.getText());
									}
								});
								text.selectAll();
								text.setFocus();
								return;
							}

							public void widgetDefaultSelected(SelectionEvent e) {
							}

						});
					}
				});
				display.wake();
				resetAsync();
			}
		}.start();
	}
	
	public void reset() {
		System.out.println("reseting");
		int j = 0;
		for(int i = 0; i < ShizaLocale.keys.size(); i++) {
			String k = (String) ShizaLocale.keys.elementAt(i);
			if(k.startsWith("empty")) continue;
			String v = (String) ShizaLocale.values.elementAt(i);
			table.getItem(j).setText(new String[] {k, v});
			j++;
		}
		System.out.println("done");
		display.syncExec(new Runnable() {
			public void run() {
				upd();
			}
		});
		display.wake();
	}
	
	public void resetAsync() {
		System.out.println("reseting asyn");
		new Thread() {
			public void run() {
				int j = 0;
				for(int i = 0; i < ShizaLocale.keys.size(); i++) {
					final String k = (String) ShizaLocale.keys.elementAt(i);
					if(k.startsWith("empty")) continue;
					final String v = (String) ShizaLocale.values.elementAt(i);
					final int x = j;
					display.syncExec(new Runnable() {
						public void run() {
							table.getItem(x).setText(new String[] {k, v});
						}
					});
					j++;
				}
				display.syncExec(new Runnable() {
					public void run() {
						upd();
					}
				});
				display.wake();
				System.out.println("done");
			}
		}.start();
	}

	private void upd() {
		table.redraw();
		parent.layout();
		shell.layout();
	}

	public void exit() {
		exiting = true;
		Display.getDefault().wake();
		LocaleEditorMIDlet.midlet.destroyApp(true);
		LocaleEditorMIDlet.midlet.notifyDestroyed();
	}
	
	public void widgetSelected(SelectionEvent ev) {
		if(ev.widget == exitcmd)
			exit();
		if(ev.widget == previewwcmd) {
			MessageBox msg = new MessageBox(shell);
			msg.setMessage(ShizaLocale.makeString());
			msg.open();
		}
		if(ev.widget == setavtvkcmd) {
			QueryDialog qd = new QueryDialog(shell);
			qd.setText("Set authors vk");
			qd.setPromptText(null, ShizaLocale.vk);
			ShizaLocale.vk = qd.open();
		}
		if(ev.widget == setavtorcmd) {
			QueryDialog qd = new QueryDialog(shell);
			qd.setText("Set authors");
			qd.setPromptText(null, ShizaLocale.authors);
			ShizaLocale.authors = qd.open();
		}
		if(ev.widget == settnamecmd) {
			QueryDialog qd = new QueryDialog(shell);
			qd.setText("Set name");
			qd.setPromptText("Language", ShizaLocale.name);
			ShizaLocale.name = qd.open();
		}
		if(ev.widget == loadjarrcmd) {
			try {
				FileDialog fd = new FileDialog(shell);
				fd.setText("Load from client jar");
				fd.setFilterExtensions(new String[] { "*.jar", "*.jar0", "*.jarr" });
				ShizaLocale.loadKeys(fd.open());
			} catch (IOException e) {
				e.printStackTrace();
				msg("Error: " + e);
			}
		}
		if(ev.widget == loadusercmd) {
			try {
				FileDialog fd = new FileDialog(shell);
				fd.setText("Load user file");
				fd.setFilterExtensions(new String[] { "*.txt" });
				ShizaLocale.loadKeys(fd.open());
			} catch (IOException e) {
				e.printStackTrace();
				msg("Error: " + e);
			}
		}
		if(ev.widget == savefilecmd) {
			try {
				FileDialog fd = new FileDialog(shell);
				fd.setText("Save file");
				fd.setFilterExtensions(new String[] { "*.txt" });
				fd.setFileName(ShizaLocale.name + ".txt");
				ShizaLocale.save(fd.open());
			} catch (IOException e) {
				e.printStackTrace();
				msg("Error: " + e);
			}
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		
	}


}
