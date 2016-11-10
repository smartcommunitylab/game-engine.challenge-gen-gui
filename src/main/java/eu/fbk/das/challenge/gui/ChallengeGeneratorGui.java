package eu.fbk.das.challenge.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.Rotation;

import eu.fbk.das.challenge.gui.util.ConvertUtil;
import eu.trentorise.game.challenges.util.ChallengeRuleRow;
import eu.trentorise.game.challenges.util.ChallengeRules;

public class ChallengeGeneratorGui {

	private static final String CSV = ".csv";

	private static final Logger logger = LogManager
			.getLogger(ChallengeGeneratorGui.class);

	private static final String[] challengeColNames = { "Name", "Model Name",
			"Goal Type", "Target", "Bonus", "Point type", "Difficulty",
			"Baseline variable", "Selection criteria points",
			"Selection criteria badges" };

	protected static final String LAST_USED_FOLDER = "LAST_USED_FOLDER";

	private static JFrame app;
	private static ChallengeGuiController controller;
	private static ChallengeGeneratorGui window;
	private JTextField hostTextField;
	private JTextField userTextField;
	private JPasswordField passwordTextField;
	private JTable challengeTable;
	private JButton btnCheckConnection;
	private JLabel statusBar;
	private JTextField gameIdField;
	private JMenuItem mntmUpload;
	private JMenuItem mntmGenerate;
	private JList<String> logList;
	private final Action insertAction = new InsertAction();
	private final Action deleteAction = new DeleteAction();
	private JScrollPane scrollPane;
	private final Action saveAction = new SaveLogAction();
	private final Action aboutAction = new AboutAction();
	private AboutDialog about = new AboutDialog();
	private ChartPanel chartPanel;
	private JFreeChart chart;
	private JPanel analytics;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private JTextField startField;
	private JTextField endField;

	private JCheckBox useRsCheckBox;

	public ChallengeGeneratorGui() {
		logger.info("Gui creation");
		app = new JFrame("ChallengeGeneratorGui");
		app.setMinimumSize(new Dimension(1024, 768));
		app.setSize(new Dimension(800, 600));
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		app.setLocationRelativeTo(null);
		try {
			app.setIconImage(ImageIO.read(getClass().getResource(
					"/images/1469713255_Bulb_On-40.png")));
		} catch (IOException e1) {
			logger.error(e1);
		}

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout(5, 5));
		JPanel centerPanel = new JPanel();
		GridBagLayout gbl_centerPanel = new GridBagLayout();
		gbl_centerPanel.rowWeights = new double[] { 0.0, 0.0, 0.0 };
		gbl_centerPanel.columnWeights = new double[] { 1.0 };
		centerPanel.setLayout(gbl_centerPanel);

		JPanel configurationPanel = new JPanel();
		configurationPanel.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "Configuration",
				TitledBorder.LEADING, TitledBorder.TOP, null,
				new Color(0, 0, 0)));
		analytics = new JPanel();
		analytics.setMinimumSize(new Dimension(250, 0));
		// Object[][] data = {
		// { "w1_challengeX", "Percent", "carDistance", "2", "400",
		// "green leaves", "", "", "green leaves week 6 < 20", "",
		// "" },
		// { "w1_challengeY", "Percent", "carDistance", "2", "400",
		// "green leaves", "", "", "green leaves week 6 < 20", "",
		// "" } };
		challengeTable = new JTable(null, challengeColNames);
		challengeTable.setDragEnabled(true);
		challengeTable.setDropMode(DropMode.INSERT_ROWS);
		challengeTable.setTransferHandler(new TableRowTransferHandler(
				challengeTable));
		challengeTable.setRowHeight(20);
		challengeTable.setModel(new DefaultTableModel(new Object[][] { { "",
				"", "", "", "", "", "", "", "", "", null }, },
				challengeColNames));
		challengeTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		challengeTable.getColumnModel().getColumn(0).setMinWidth(50);
		challengeTable.getColumnModel().getColumn(1).setMinWidth(75);
		challengeTable.getColumnModel().getColumn(2).setMinWidth(50);
		challengeTable.getColumnModel().getColumn(3).setMinWidth(20);
		challengeTable.getColumnModel().getColumn(4).setMinWidth(20);
		challengeTable.getColumnModel().getColumn(5).setMinWidth(50);
		challengeTable.getColumnModel().getColumn(6).setMinWidth(5);
		challengeTable.getColumnModel().getColumn(7).setMinWidth(20);
		challengeTable.getColumnModel().getColumn(8).setPreferredWidth(100);
		challengeTable.getColumnModel().getColumn(8).setMinWidth(100);
		challengeTable.getColumnModel().getColumn(9).setPreferredWidth(100);
		challengeTable.getColumnModel().getColumn(9).setMinWidth(100);

		challengeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		challengeTable.setFillsViewportHeight(true);

		JScrollPane scrollpane = new JScrollPane(challengeTable);

		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(challengeTable, popupMenu);

		JMenuItem mntmInsert = new JMenuItem("Insert");
		mntmInsert.setAction(insertAction);
		popupMenu.add(mntmInsert);

		JMenuItem mntmDelete = new JMenuItem("Delete");
		mntmDelete.setAction(deleteAction);
		popupMenu.add(mntmDelete);
		scrollpane.setPreferredSize(new Dimension(652, 402));
		JSplitPane jsplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				scrollpane, analytics);
		analytics.setLayout(new BorderLayout(0, 50));
		jsplitpane.setOpaque(false);
		jsplitpane.setOneTouchExpandable(true);
		jsplitpane.setPreferredSize(new Dimension(669, 500));

		GridBagConstraints gbc_configurationPanel = new GridBagConstraints();
		gbc_configurationPanel.insets = new Insets(5, 5, 5, 0);
		gbc_configurationPanel.anchor = GridBagConstraints.NORTH;
		gbc_configurationPanel.fill = GridBagConstraints.BOTH;
		gbc_configurationPanel.gridx = 0;
		gbc_configurationPanel.gridy = 0;
		gbc_configurationPanel.weightx = 1.0;
		gbc_configurationPanel.weighty = 0.05;

		centerPanel.add(configurationPanel, gbc_configurationPanel);
		configurationPanel.setLayout(new GridLayout(0, 5, 10, 5));

		JLabel hostLabel = new JLabel("Gamification engine host");
		hostLabel.setMinimumSize(new Dimension(200, 14));
		configurationPanel.add(hostLabel);

		hostTextField = new JTextField();
		hostTextField.setMargin(new Insets(2, 5, 2, 2));
		hostTextField.setToolTipText("gamification engine host");
		hostTextField.setMinimumSize(new Dimension(200, 20));
		hostLabel.setLabelFor(hostTextField);
		hostTextField.setPreferredSize(new Dimension(200, 20));
		configurationPanel.add(hostTextField);
		hostTextField.setColumns(25);
		hostTextField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkHostGameIdField();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				checkHostGameIdField();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});

		JLabel gameIdLabel = new JLabel("GameID");
		gameIdLabel.setMinimumSize(new Dimension(200, 14));
		configurationPanel.add(gameIdLabel);

		gameIdField = new JTextField();
		gameIdField.setMargin(new Insets(2, 5, 2, 2));
		gameIdField.setMinimumSize(new Dimension(200, 20));
		gameIdField.setColumns(15);
		gameIdField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkHostGameIdField();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				checkHostGameIdField();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
		configurationPanel.add(gameIdField);

		JLabel lblNewLabel = new JLabel("");
		configurationPanel.add(lblNewLabel);

		JLabel userLabel = new JLabel("Username");
		userLabel.setMinimumSize(new Dimension(200, 14));
		configurationPanel.add(userLabel);
		userLabel.setLabelFor(userTextField);

		userTextField = new JTextField();
		userTextField.setMargin(new Insets(2, 5, 2, 2));
		userTextField.setMinimumSize(new Dimension(200, 20));
		configurationPanel.add(userTextField);
		userTextField.setColumns(15);

		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setMinimumSize(new Dimension(200, 14));
		configurationPanel.add(passwordLabel);

		passwordTextField = new JPasswordField();
		passwordTextField.setMargin(new Insets(2, 5, 2, 2));
		configurationPanel.add(passwordTextField);
		passwordTextField.setColumns(15);

		btnCheckConnection = new JButton("check connection");
		btnCheckConnection.setEnabled(false);
		btnCheckConnection.addActionListener(new CheckConnectionAction());
		configurationPanel.add(btnCheckConnection);

		Label startLabel = new Label(
				"Challenge date start (dd/MM/YYYY HH:mm:ss)");
		configurationPanel.add(startLabel);

		startField = new JTextField();
		startField.setMargin(new Insets(2, 5, 2, 2));
		startField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				JTextField s = (JTextField) e.getSource();
				if (invalidDate(s)) {
					s.setBackground(Color.red);
				} else {
					s.setBackground(Color.white);
				}
			}
		});
		startField.setColumns(15);
		configurationPanel.add(startField);

		Label endLabel = new Label("Challenge date end (dd/MM/YYYY HH:mm:ss)");
		configurationPanel.add(endLabel);

		endField = new JTextField();
		endField.setMargin(new Insets(2, 5, 2, 2));
		endField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				JTextField s = (JTextField) e.getSource();
				if (invalidDate(s)) {
					s.setBackground(Color.red);
				} else {
					s.setBackground(Color.white);
				}
			}
		});
		endField.setColumns(15);
		configurationPanel.add(endField);

		useRsCheckBox = new JCheckBox("Use recommendation system");
		useRsCheckBox.setEnabled(false);
		useRsCheckBox.setSelected(true);
		configurationPanel.add(useRsCheckBox);

		GridBagConstraints gbcsplit = new GridBagConstraints();
		gbcsplit.insets = new Insets(0, 5, 5, 0);
		gbcsplit.fill = GridBagConstraints.BOTH;
		gbcsplit.gridx = 0;
		gbcsplit.gridy = 1;
		gbcsplit.weightx = 1.0;
		gbcsplit.weighty = 0.6;

		gbcsplit.fill = GridBagConstraints.BOTH;
		gbcsplit.gridy = 1;
		gbcsplit.weighty = 0.7;

		centerPanel.add(jsplitpane, gbcsplit);

		panel.add(centerPanel, BorderLayout.CENTER);

		DefaultListModel<String> model = new DefaultListModel<String>();

		scrollPane = new JScrollPane();

		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.weighty = 0.1;
		gbc_scrollPane.anchor = GridBagConstraints.SOUTH;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 2;
		centerPanel.add(scrollPane, gbc_scrollPane);
		logList = new JList<String>(model);
		scrollPane.setViewportView(logList);
		logList.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));

		JPopupMenu popupMenu_1 = new JPopupMenu();
		addPopup(logList, popupMenu_1);

		JMenuItem mntmSaveLog = new JMenuItem("save log");
		mntmSaveLog.setAction(saveAction);
		popupMenu_1.add(mntmSaveLog);

		app.getContentPane().add(panel);

		JMenuBar menuBar = new JMenuBar();
		panel.add(menuBar, BorderLayout.NORTH);

		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);

		JMenuItem mntmNewMenuItem = new JMenuItem("New");
		mntmNewMenuItem.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				controller.newSession();
			}
		});
		mnNewMenu.add(mntmNewMenuItem);

		JMenuItem mntmOpenMenuItem = new JMenuItem("Open");
		mntmOpenMenuItem.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Preferences prefs = Preferences.userRoot().node(
						getClass().getName());
				JFileChooser chooser = new JFileChooser((prefs.get(
						LAST_USED_FOLDER, new File(".").getAbsolutePath()))) {

					private static final long serialVersionUID = 7489308134784417097L;

					@Override
					public void approveSelection() {
						File f = getSelectedFile();
						if (f != null && f.exists()) {
							logger.info("Selected challenges definition file "
									+ f.getAbsolutePath());
							super.approveSelection();
							controller.openChallenges(f);
						} else {
							logger.error("Selected challenges definition file is not found");
						}
					}

				};
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Comma separated file (.csv)", "csv");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					prefs.put(LAST_USED_FOLDER, chooser.getSelectedFile()
							.getParent());
				}
			}
		});
		mnNewMenu.add(mntmOpenMenuItem);

		JMenuItem mntmSaveMenuItem = new JMenuItem("Save");
		mntmSaveMenuItem.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				Preferences prefs = Preferences.userRoot().node(
						getClass().getName());
				JFileChooser chooser = new JFileChooser((prefs.get(
						LAST_USED_FOLDER, new File(".").getAbsolutePath()))) {

					private static final long serialVersionUID = 7489308134784417097L;

					@Override
					public void approveSelection() {
						File f = getSelectedFile();
						if (!f.getAbsolutePath().toLowerCase().endsWith(CSV)) {
							f = new File(f.getAbsolutePath() + CSV);
						}
						logger.info("Save challenges to " + f.getAbsolutePath());
						super.approveSelection();
						try {
							ChallengeRules converted = ConvertUtil
									.convertTable((DefaultTableModel) challengeTable
											.getModel());
							controller.saveChallenges(f, converted);
						} catch (NumberFormatException nfe) {
							logger.error(
									"Error on conversion between table and data: "
											+ nfe.getMessage(), nfe);
							addLog("Error on conversion between table and data: "
									+ nfe.getMessage());
						}

					}

				};
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Comma separated file (.csv)", "csv");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showSaveDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					prefs.put(LAST_USED_FOLDER, chooser.getSelectedFile()
							.getParent());
				}

			}
		});
		mnNewMenu.add(mntmSaveMenuItem);

		JMenuItem mntmNewMenuItem_3 = new JMenuItem("Exit");
		mntmNewMenuItem_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				logger.info("Exit");
				System.exit(0);
			}
		});
		mnNewMenu.add(mntmNewMenuItem_3);

		JMenu mnChallenges = new JMenu("Challenges");
		menuBar.add(mnChallenges);

		mntmGenerate = new JMenuItem("Generate");
		mntmGenerate.setEnabled(false);
		mntmGenerate.setAction(new GenerateAction());
		mnChallenges.add(mntmGenerate);

		mntmUpload = new JMenuItem("Upload");
		mntmUpload.setEnabled(false);
		mntmUpload.setAction(new UploadAction());
		mnChallenges.add(mntmUpload);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.setAction(aboutAction);
		mnHelp.add(mntmAbout);

		JPanel statusBarPanel = new JPanel();
		statusBarPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null,
				null));
		FlowLayout flowLayout = (FlowLayout) statusBarPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel.add(statusBarPanel, BorderLayout.SOUTH);

		statusBar = new JLabel("");
		statusBarPanel.add(statusBar);
		app.setVisible(true);

		about = new AboutDialog();
		about.setVisible(false);
	}

	private boolean invalidDate(JTextField source) {
		try {
			if (sdf.parse(source.getText()) != null) {
				return false;
			}
		} catch (IllegalArgumentException | ParseException e) {

		}
		return true;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					logger.warn("Unable to set System look and Feel", e);
				}
				controller = new ChallengeGuiController();
				window = new ChallengeGeneratorGui();
				controller.setWindow(window);
				controller.newSession();
			}
		});
	}

	public void setChallenges(ChallengeRules challenges) {
		if (challenges != null) {
			DefaultTableModel model = new DefaultTableModel(null,
					challengeColNames);
			for (ChallengeRuleRow crr : challenges.getChallenges()) {
				model.addRow(ConvertUtil.convertChallenge(crr));
			}
			challengeTable.setModel(model);
		}
		refresh();
	}

	public void refresh() {
		app.getContentPane().validate();
		app.getContentPane().repaint();
	}

	public void setStatusBar(String text, boolean error) {
		if (error) {
			statusBar.setForeground(Color.red);
		} else {
			statusBar.setForeground(Color.black);
		}
		statusBar.setText(text);
		refresh();
	}

	public void setHost(String host) {
		hostTextField.setText(host);
	}

	public void setUser(String user) {
		userTextField.setText(user);
	}

	public void setPassword(String psw) {
		passwordTextField.setText(psw);
	}

	public String getHost() {
		return hostTextField.getText();
	}

	public String getUser() {
		return userTextField.getText();
	}

	public String getPassword() {
		return String.valueOf(passwordTextField.getPassword());
	}

	public void enableCheckConnection(boolean b) {
		btnCheckConnection.setEnabled(b);
	}

	public void setGameId(String gameId) {
		gameIdField.setText(gameId);
	}

	public void enableGenerate(boolean b) {
		useRsCheckBox.setEnabled(b);
		mntmGenerate.setEnabled(b);
	}

	private class GenerateAction extends AbstractAction {
		private static final long serialVersionUID = 7562917295725963136L;

		public GenerateAction() {
			putValue(NAME, "Generate");
			putValue(SHORT_DESCRIPTION, "Challenge generation");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			controller.generate();
		}
	}

	public String getGameId() {
		return gameIdField.getText();
	}

	public void addLog(String log) {
		if (log == null) {
			return;
		}
		DefaultListModel<String> model = (DefaultListModel<String>) logList
				.getModel();
		String[] listData = StringUtils.split(log, "\n");
		if (listData != null) {
			for (String e : listData) {
				model.addElement(e);
			}
		}

		// scroll to the bottom
		scrollPane.validate();
		JScrollBar vertical = scrollPane.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	private class InsertAction extends AbstractAction {
		private static final long serialVersionUID = 7597057801468846067L;

		public InsertAction() {
			putValue(NAME, "Insert");
			putValue(SHORT_DESCRIPTION, "Insert new challenge");
		}

		public void actionPerformed(ActionEvent e) {
			if (challengeTable.getSelectedRow() != -1) {
				controller.addChallenge(challengeTable.getSelectedRow());
			}
		}
	}

	private class DeleteAction extends AbstractAction {
		private static final long serialVersionUID = 4637589614176994853L;

		public DeleteAction() {
			putValue(NAME, "Delete");
			putValue(SHORT_DESCRIPTION, "Delete selected challenge");
		}

		public void actionPerformed(ActionEvent e) {
			if (challengeTable.getSelectedRow() != -1) {
				controller.removeChallenge(challengeTable.getSelectedRow());
			}
		}
	}

	public ChallengeRules getChallenges() {
		return ConvertUtil.convertTable((DefaultTableModel) challengeTable
				.getModel());
	}

	public void enableUpload(boolean b) {
		mntmUpload.setEnabled(b);
	}

	private class UploadAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public UploadAction() {
			putValue(NAME, "Upload");
			putValue(SHORT_DESCRIPTION, "Challenge generation upload");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			String msg = String.format(
					"Do you wan to upload generated challenges on %s ? ",
					getHost());
			int result = JOptionPane.showConfirmDialog(
					(Component) e.getSource(), msg, "Upload challenges",
					JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				controller.upload();
			}

		}
	}

	private class SaveLogAction extends AbstractAction {
		private static final long serialVersionUID = 8184822790331262798L;

		public SaveLogAction() {
			putValue(NAME, "Save log");
			putValue(SHORT_DESCRIPTION, "save log");
		}

		public void actionPerformed(ActionEvent e) {
			Preferences prefs = Preferences.userRoot().node(
					getClass().getName());
			JFileChooser chooser = new JFileChooser((prefs.get(
					LAST_USED_FOLDER, new File(".").getAbsolutePath()))) {

				private static final long serialVersionUID = -7993854134304731166L;

				@Override
				public void approveSelection() {
					File f = getSelectedFile();
					if (!f.getAbsolutePath().toLowerCase().endsWith(".log")) {
						f = new File(f.getAbsolutePath() + ".log");
					}
					logger.info("Save log to " + f.getAbsolutePath());
					super.approveSelection();
					try {
						StringBuffer sb = new StringBuffer();
						for (int i = 0; i < logList.getModel().getSize(); i++) {
							Object item = logList.getModel().getElementAt(i);
							sb.append(item + "\n");
						}
						IOUtils.write(sb.toString(), new FileOutputStream(f));
						setStatusBar("Log saved " + f.getAbsolutePath(), false);
					} catch (IOException e) {
						setStatusBar(
								"Error in log save " + f.getAbsolutePath(),
								false);
						logger.error(e);
					}
				}

			};
			int returnVal = chooser.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				prefs.put(LAST_USED_FOLDER, chooser.getSelectedFile()
						.getParent());
			}
		}
	}

	private class AboutAction extends AbstractAction {
		private static final long serialVersionUID = 6922499789788835040L;

		public AboutAction() {
			putValue(NAME, "About");
			putValue(SHORT_DESCRIPTION, "About");
		}

		public void actionPerformed(ActionEvent e) {
			about.setVisible(true);
		}
	}

	public void updateChart(DefaultPieDataset pieDataSet, List<Integer> values,
			int totalPlayers) {
		// clean panel
		analytics.removeAll();

		// info panel
		JPanel infoPanel = new JPanel();
		analytics.add(infoPanel, BorderLayout.NORTH);

		// add info about total players
		JLabel totalLabel = new JLabel("Number of players : " + totalPlayers);
		totalLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
		infoPanel.add(totalLabel, BorderLayout.NORTH);

		// add info about challenges for player
		JLabel challengeNumberLabel = new JLabel("Challenges per player : "
				+ values.toString());
		challengeNumberLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		challengeNumberLabel.setHorizontalAlignment(SwingConstants.CENTER);
		infoPanel.add(challengeNumberLabel, BorderLayout.CENTER);

		// add info about total players

		// create chart
		chart = ChartFactory.createPieChart("Challenges for users", pieDataSet,
				true, true, false);

		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(0.8f);
		plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
				"{0} {1} {2}"));

		chartPanel = new ChartPanel(chart, true);
		chartPanel.setVisible(true);

		analytics.add(chartPanel, BorderLayout.CENTER);
		refresh();
	}

	public void resetAnalytics() {
		analytics.removeAll();
	}

	private void checkHostGameIdField() {
		if (hostTextField.getText().length() == 0
				|| gameIdField.getText().length() == 0) {
			btnCheckConnection.setEnabled(false);
		} else if (hostTextField.getText().length() != 0
				&& gameIdField.getText().length() != 0) {
			btnCheckConnection.setEnabled(true);
		}
	}

	private class CheckConnectionAction extends AbstractAction {

		private static final long serialVersionUID = 1732760821158054790L;

		public CheckConnectionAction() {
			putValue(NAME, "CheckConnection");
			putValue(SHORT_DESCRIPTION, "CheckConnection");
		}

		public void actionPerformed(ActionEvent e) {
			controller.checkConnection(hostTextField.getText(),
					userTextField.getText(), passwordTextField.getPassword(),
					gameIdField.getText());
		}
	}

	public void setStartDate(Date date) {
		startField.setText(sdf.format(date));
	}

	public void setEndDate(Date date) {
		endField.setText(sdf.format(date));
	}

	public String getStartDate() {
		return startField.getText();
	}

	public String getEndDate() {
		return endField.getText();
	}

	public Boolean getUseRs() {
		return useRsCheckBox.isSelected();
	}

}