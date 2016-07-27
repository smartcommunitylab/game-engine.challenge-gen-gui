package eu.fbk.das.challenge.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.fbk.das.challenge.gui.util.ConvertUtil;
import eu.trentorise.game.challenges.util.ChallengeRuleRow;
import eu.trentorise.game.challenges.util.ChallengeRules;

public class ChallengeGeneratorGui {

	private static final Logger logger = LogManager
			.getLogger(ChallengeGeneratorGui.class);

	private static ChallengeGuiController controller;
	private static ChallengeGeneratorGui window;

	private JTextField hostTextField;
	private JTextField userTextField;
	private JPasswordField passwordTextField;

	private JTable challengeTable;

	private static JFrame app;

	private JButton btnCheckConnection;

	private JLabel statusBar;

	private static final String[] challengeColNames = { "Name", "Type",
			"Goal Type", "Target", "Bonus", "Point type", "Difficulty",
			"Baseline variable", "Selection criteria Custom data",
			"Selectin criteria points", "Selection criteria badges" };
	private JTextField gameIdField;

	private JMenuItem mntmUpload;

	private JMenuItem mntmGenerate;

	private JList<String> logList;
	private final Action insertAction = new InsertAction();
	private final Action deleteAction = new DeleteAction();

	public ChallengeGeneratorGui() {
		logger.info("Gui creation");
		app = new JFrame("ChallengeGeneratorGui");
		app.setMinimumSize(new Dimension(1024, 768));
		app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		app.setSize(new Dimension(800, 600));
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout(5, 5));
		JPanel centerPanel = new JPanel();
		GridBagLayout gbl_centerPanel = new GridBagLayout();
		gbl_centerPanel.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0 };
		gbl_centerPanel.columnWeights = new double[] { 1.0 };
		centerPanel.setLayout(gbl_centerPanel);

		JPanel configurationPanel = new JPanel();
		JPanel analytics = new JPanel();
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
				"", "", "", "", "", "", "", "", "", null }, }, new String[] {
				"Name", "Type", "Goal Type", "Target", "Bonus", "Point type",
				"Difficulty", "Baseline variable",
				"Selection criteria Custom data", "Selectin criteria points",
				"Selection criteria badges" }));
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
		challengeTable.getColumnModel().getColumn(10).setPreferredWidth(100);
		challengeTable.getColumnModel().getColumn(10).setMinWidth(100);

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
		jsplitpane.setOpaque(false);
		jsplitpane.setOneTouchExpandable(true);
		jsplitpane.setPreferredSize(new Dimension(669, 500));

		GridBagConstraints gbc_configurationPanel = new GridBagConstraints();
		gbc_configurationPanel.insets = new Insets(5, 5, 5, 0);
		gbc_configurationPanel.anchor = GridBagConstraints.NORTH;
		gbc_configurationPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_configurationPanel.gridx = 0;
		gbc_configurationPanel.gridy = 0;
		gbc_configurationPanel.weightx = 1.0;
		gbc_configurationPanel.weighty = 0.1;

		centerPanel.add(configurationPanel, gbc_configurationPanel);

		JLabel hostLabel = new JLabel("Gamification engine host");
		hostLabel.setMinimumSize(new Dimension(200, 14));
		configurationPanel.add(hostLabel);

		hostTextField = new JTextField();
		hostTextField.setToolTipText("gamification engine host");
		hostTextField.setMinimumSize(new Dimension(200, 20));
		hostLabel.setLabelFor(hostTextField);
		hostTextField.setPreferredSize(new Dimension(200, 20));
		configurationPanel.add(hostTextField);
		hostTextField.setColumns(25);

		JLabel userLabel = new JLabel("Username");
		userLabel.setMinimumSize(new Dimension(200, 14));
		configurationPanel.add(userLabel);

		userTextField = new JTextField();
		userTextField.setMinimumSize(new Dimension(200, 20));
		userLabel.setLabelFor(userTextField);
		configurationPanel.add(userTextField);
		userTextField.setColumns(15);

		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setMinimumSize(new Dimension(200, 14));
		configurationPanel.add(passwordLabel);

		passwordTextField = new JPasswordField();
		configurationPanel.add(passwordTextField);
		passwordTextField.setColumns(15);

		btnCheckConnection = new JButton("check connection");
		btnCheckConnection.setEnabled(false);
		btnCheckConnection.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				controller.checkConnection(hostTextField.getText(),
						userTextField.getText(),
						passwordTextField.getPassword());
			}

		});

		JLabel gameIdLabel = new JLabel("GameID");
		gameIdLabel.setMinimumSize(new Dimension(200, 14));
		configurationPanel.add(gameIdLabel);

		gameIdField = new JTextField();
		gameIdField.setMinimumSize(new Dimension(200, 20));
		gameIdField.setColumns(15);
		configurationPanel.add(gameIdField);
		configurationPanel.add(btnCheckConnection);

		GridBagConstraints gbcsplit = new GridBagConstraints();
		gbcsplit.gridheight = 2;
		gbcsplit.insets = new Insets(0, 5, 5, 0);
		gbcsplit.fill = GridBagConstraints.BOTH;
		gbcsplit.gridx = 0;
		gbcsplit.gridy = 1;
		gbcsplit.weightx = 1.0;
		gbcsplit.weighty = 1.0;

		gbcsplit.fill = GridBagConstraints.BOTH;
		gbcsplit.gridy = 1;
		gbcsplit.weighty = 0.7;

		centerPanel.add(jsplitpane, gbcsplit);

		panel.add(centerPanel, BorderLayout.CENTER);

		logList = new JList<String>();
		logList.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_logList = new GridBagConstraints();
		gbc_logList.insets = new Insets(0, 5, 5, 0);
		gbc_logList.weighty = 1.0;
		gbc_logList.anchor = GridBagConstraints.SOUTH;
		gbc_logList.fill = GridBagConstraints.BOTH;
		gbc_logList.gridx = 0;
		gbc_logList.gridy = 3;
		centerPanel.add(logList, gbc_logList);

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
				JFileChooser chooser = new JFileChooser() {

					private static final long serialVersionUID = 7489308134784417097L;

					@Override
					public void approveSelection() {
						File f = getSelectedFile();
						if (f != null && f.exists()) {
							logger.info("Selected challenges definition file"
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
				chooser.showOpenDialog(null);
			}
		});
		mnNewMenu.add(mntmOpenMenuItem);

		JMenuItem mntmSaveMenuItem = new JMenuItem("Save");
		mntmSaveMenuItem.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				JFileChooser chooser = new JFileChooser() {

					private static final long serialVersionUID = 7489308134784417097L;

					@Override
					public void approveSelection() {
						File f = getSelectedFile();
						if (!f.getAbsolutePath().toLowerCase().endsWith(".csv")) {
							f = new File(f.getAbsolutePath() + ".csv");
						}
						logger.info("Save challenges to " + f.getAbsolutePath());
						super.approveSelection();
						controller.saveChallenges(
								f,
								ConvertUtil
										.convertTable((DefaultTableModel) challengeTable
												.getModel()));
					}

				};
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Comma separated file (.csv)", "csv");
				chooser.setFileFilter(filter);
				chooser.showSaveDialog(null);
			}
		});
		mnNewMenu.add(mntmSaveMenuItem);

		JMenuItem mntmNewMenuItem_3 = new JMenuItem("Exit");
		mntmNewMenuItem_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
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

		JPanel statusBarPanel = new JPanel();
		statusBarPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null,
				null));
		FlowLayout flowLayout = (FlowLayout) statusBarPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel.add(statusBarPanel, BorderLayout.SOUTH);

		statusBar = new JLabel("");
		statusBarPanel.add(statusBar);
		app.setVisible(true);
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
		String[] listData = StringUtils.split(log, "\n");
		logList.setListData(listData);
		refresh();
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
			controller.upload();
		}
	}

}