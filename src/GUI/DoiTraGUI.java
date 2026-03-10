package GUI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import BUS.DoiTraBUS;
import DTO.DoiTraDTO;
import DTO.SharedData;
import UTIL.DBConnection;

public class DoiTraGUI extends JPanel {

    private static final Color PRIMARY            = new Color(21, 101, 192);
    private static final Color PRIMARY_DARK       = new Color(10, 60, 130);
    private static final Color CONTENT_BG         = new Color(236, 242, 250);
    private static final Color ACCENT_YELLOW      = new Color(255, 215, 40);
    private static final Color ACCENT_YELLOW_DARK = new Color(240, 180, 0);
    private static final Color GRAY_BTN           = new Color(134, 142, 150);
    private static final Color GRAY_DARK          = new Color(108, 117, 125);
    private static final Color RED_BTN            = new Color(198, 40, 40);
    private static final Color RED_DARK           = new Color(160, 20, 20);
    private static final Color SUCCESS            = new Color(46, 125, 50);
    private static final Color SUCCESS_DARK       = new Color(30, 90, 35);
    private static final Color BORDER_COLOR       = new Color(180, 210, 240);
    private static final Color TEXT_PRIMARY       = new Color(10, 60, 130);
    private static final Color ROW_SELECTED       = new Color(187, 222, 251);
    private static final Color ROW_ALT            = new Color(245, 250, 255);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TABLE  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);

	private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final DoiTraBUS bus = new DoiTraBUS();
	private final List<DoiTraDTO> rows = new ArrayList<>();

	private DefaultTableModel model;
	private JTable table;
	private JTextField txtKeywordHoaDon;
	private JComboBox<String> cbStatus;
	private JComboBox<String> cbLoai;

	public DoiTraGUI() {
		setLayout(new BorderLayout(0, 0));
		setBackground(CONTENT_BG);
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		add(buildHeader(), BorderLayout.NORTH);
		add(buildCenterCard(), BorderLayout.CENTER);

		loadData();
	}

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 58));
        header.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JComponent icon = new JComponent() {
            { setPreferredSize(new Dimension(30, 58)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cy = getHeight() / 2;
                g2.drawArc(5, cy - 12, 10, 10, 40, 260);
                g2.drawLine(16, cy + 2, 24, cy + 2);
                g2.drawLine(24, cy + 2, 21, cy - 1);
                g2.drawLine(24, cy + 2, 21, cy + 5);
                g2.dispose();
            }
        };

        JLabel title = new JLabel("  QUẢN LÝ ĐỔI TRẢ");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        JPanel left = new JPanel(new GridBagLayout());
        left.setOpaque(false);
        GridBagConstraints lgc = new GridBagConstraints();
        lgc.anchor = GridBagConstraints.CENTER;
        lgc.gridx = 0; left.add(icon, lgc);
        lgc.gridx = 1; left.add(title, lgc);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        right.setOpaque(false);
        JButton btnAdd = buildHeaderButton("+ Tạo phiếu đổi trả", ACCENT_YELLOW, ACCENT_YELLOW_DARK, PRIMARY_DARK, "Tạo mới phiếu đổi trả");
        btnAdd.addActionListener(e -> openCreateDialog());
        right.add(btnAdd);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCenterCard() {
        JPanel centerCard = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        centerCard.setOpaque(false);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        centerCard.add(buildTopBar(), BorderLayout.NORTH);
        centerCard.add(buildTablePanel(), BorderLayout.CENTER);
        return centerCard;
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JPanel searchBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        searchBar.setOpaque(false);
        searchBar.setPreferredSize(new Dimension(240, 36));
        JLabel icon = new JLabel("🔎");
        icon.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
        searchBar.add(icon, BorderLayout.WEST);

        txtKeywordHoaDon = new JTextField();
        txtKeywordHoaDon.setFont(FONT_NORMAL);
        txtKeywordHoaDon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        txtKeywordHoaDon.setOpaque(false);
        txtKeywordHoaDon.setToolTipText("Tìm theo mã hóa đơn");
        searchBar.add(txtKeywordHoaDon, BorderLayout.CENTER);

        cbStatus = new JComboBox<>(new String[] {
                "Tất cả trạng thái", "Chờ duyệt", "Đang xử lý", "Từ chối", "Hoàn thành"
        });
        styleComboBox(cbStatus);
        cbStatus.setPreferredSize(new Dimension(150, 36));

        cbLoai = new JComboBox<>(new String[] {
			"Tất cả loại", "Đổi sản phẩm", "Trả hàng"
        });
        styleComboBox(cbLoai);
        cbLoai.setPreferredSize(new Dimension(140, 36));

        JButton btnFilter = buildActionButton("Lọc", PRIMARY, PRIMARY_DARK, Color.WHITE);
        btnFilter.addActionListener(e -> applyFilter());

        left.add(new JLabel("Mã hóa đơn:"));
        left.add(searchBar);
        left.add(cbStatus);
        left.add(cbLoai);
        left.add(btnFilter);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton btnRefresh = buildActionButton("Làm mới", GRAY_BTN, GRAY_DARK, Color.WHITE);
        btnRefresh.addActionListener(e -> loadData());
        right.add(btnRefresh);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

	private JPanel buildTablePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		model = new DefaultTableModel(new String[] {
				"Mã đổi trả", "Mã hóa đơn", "Sản phẩm", "Serial", "Loại đổi trả", "Ngày yêu cầu", "Trạng thái", "Nhân viên", "Thao tác"
		}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 8;
			}
		};

		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(34);
		table.setFont(FONT_TABLE);
		table.setGridColor(new Color(230, 238, 246));
		table.setShowVerticalLines(false);
		table.setSelectionBackground(ROW_SELECTED);
		table.setSelectionForeground(TEXT_PRIMARY);

		JTableHeader header = table.getTableHeader();
		header.setFont(FONT_HEADER);
		header.setReorderingAllowed(false);
		header.setDefaultRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
														   boolean hasFocus, int row, int column) {
				JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
				lbl.setOpaque(true);
				lbl.setBackground(PRIMARY);
				lbl.setForeground(Color.WHITE);
				lbl.setFont(FONT_HEADER);
				lbl.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
				return lbl;
			}
		});

		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
														   boolean hasFocus, int row, int column) {
				JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
				lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
				if (!isSelected) {
					lbl.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
					lbl.setForeground(TEXT_PRIMARY);
				}
				if (column == 8) {
					lbl.setHorizontalAlignment(CENTER);
					lbl.setText("Xem chi tiết");
					lbl.setForeground(PRIMARY_DARK);
					lbl.setFont(FONT_HEADER);
				}
				return lbl;
			}
		});

		table.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				int col = table.columnAtPoint(e.getPoint());
				if (row < 0) {
					return;
				}
				if (e.getClickCount() == 2 || col == 8) {
					int ma = Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
					showDetailDialog(ma);
				}
			}
		});

		JScrollPane sp = new JScrollPane(table);
		sp.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		sp.getViewport().setBackground(Color.WHITE);
		panel.add(sp, BorderLayout.CENTER);
		return panel;
	}

    private JButton buildHeaderButton(String text, Color bg, Color bgHover, Color fg, String tooltip) {
        JButton btn = buildPaintedButton(text, FONT_HEADER, bg, bgHover, fg, 10);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setToolTipText(tooltip);
        return btn;
    }

    private JButton buildActionButton(String text, Color bg, Color bgHover, Color fg) {
        JButton btn = buildPaintedButton(text, FONT_NORMAL, bg, bgHover, fg, 10);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return btn;
    }

    private JButton buildPaintedButton(String text, Font font, Color bg, Color bgHover, Color fg, int arc) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isRollover() ? bgHover : bg;
                if (!isEnabled()) {
                    fill = new Color(170, 170, 170);
                }
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(font);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadData() {
        try {
            rows.clear();
            rows.addAll(bus.layTatCa());
            applyFilter();
        } catch (Exception ex) {
            showError("Không tải được dữ liệu đổi/trả", ex);
        }
    }

	private void applyFilter() {
		String kwHoaDon = txtKeywordHoaDon.getText() == null ? "" : txtKeywordHoaDon.getText().trim().toLowerCase();
		String dbStatus = mapStatusUiToDb((String) cbStatus.getSelectedItem());
		String dbLoai = mapLoaiUiToDbFilter((String) cbLoai.getSelectedItem());

		model.setRowCount(0);
		for (DoiTraDTO d : rows) {
			if (dbStatus != null && !dbStatus.equals(d.getTrangThai())) {
				continue;
			}
			if (dbLoai != null && !dbLoai.equals(d.getLoaiDoiTra())) {
				continue;
			}
			String maHoaDonText = String.valueOf(d.getMaHoaDon()).toLowerCase();
			if (!kwHoaDon.isBlank() && !maHoaDonText.contains(kwHoaDon)) {
				continue;
			}

			model.addRow(new Object[] {
					d.getMaDoiTra(),
					d.getMaHoaDon(),
					safe(d.getTenSP()),
					safe(d.getSerialCode()),
					mapLoaiDbToUi(d.getLoaiDoiTra()),
					d.getNgayYeuCau() != null ? d.getNgayYeuCau().format(DF) : "-",
					mapStatusDbToUi(d.getTrangThai()),
					"NV" + d.getMaNV(),
					"Xem chi tiết"
			});
		}
	}

    private void showDetailDialog(int maDoiTra) {
        DoiTraDTO dto;
        try {
            dto = bus.layTheoMa(maDoiTra);
        } catch (Exception ex) {
            showError("Không tải được chi tiết phiếu", ex);
            return;
        }
        if (dto == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy phiếu đổi trả.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết phiếu đổi trả", JDialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(8, 8));

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(CONTENT_BG);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel infoCard = new JPanel();
        infoCard.setBackground(Color.WHITE);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));

		JLabel infoTitle = new JLabel("THÔNG TIN CHUNG");
		infoTitle.setFont(FONT_HEADER.deriveFont(Font.BOLD, 16f));
		infoTitle.setForeground(PRIMARY_DARK);
		infoCard.add(infoTitle);
		infoCard.add(Box.createVerticalStrut(8));

		JPanel infoGrid = new JPanel(new GridBagLayout());
		infoGrid.setOpaque(false);
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(2, 0, 2, 10);
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = 0.5;

		int r = 0;
		addInfoCell(infoGrid, gc, r, 0, "Mã đổi trả", String.valueOf(dto.getMaDoiTra()));
		addInfoCell(infoGrid, gc, r++, 1, "Mã hóa đơn", String.valueOf(dto.getMaHoaDon()));
		addInfoCell(infoGrid, gc, r, 0, "Sản phẩm", safe(dto.getTenSP()));
		addInfoCell(infoGrid, gc, r++, 1, "Serial", safe(dto.getSerialCode()));
		addInfoCell(infoGrid, gc, r, 0, "Số lượng trả", String.valueOf(dto.getSoLuongTra()));
		addInfoCell(infoGrid, gc, r++, 1, "Loại đổi trả", mapLoaiDbToUi(dto.getLoaiDoiTra()));
		addInfoCell(infoGrid, gc, r, 0, "Nhân viên xử lý", "NV" + dto.getMaNV());
		addInfoCell(infoGrid, gc, r++, 1, "Trạng thái", mapStatusDbToUi(dto.getTrangThai()));
		addInfoCell(infoGrid, gc, r, 0, "Ngày yêu cầu", dto.getNgayYeuCau() != null ? dto.getNgayYeuCau().format(DF) : "-");
		addInfoCell(infoGrid, gc, r++, 1, "Ngày xử lý", dto.getNgayXuLy() != null ? dto.getNgayXuLy().format(DF) : "-");
		infoCard.add(infoGrid);
		infoCard.add(Box.createVerticalStrut(8));

		infoCard.add(createReadOnlyArea("Lý do", safe(dto.getLyDo()), 90));
		infoCard.add(Box.createVerticalStrut(8));
		infoCard.add(createReadOnlyArea("Ghi chú", safe(dto.getGhiChu()), 90));

        JPanel processCard = new JPanel();
        processCard.setBackground(Color.WHITE);
        processCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        processCard.setLayout(new BoxLayout(processCard, BoxLayout.Y_AXIS));

        if (DoiTraDTO.LOAI_DOI_SAN_PHAM.equals(dto.getLoaiDoiTra())) {
            buildExchangeProcessPanel(processCard, dto, dlg);
        } else if (DoiTraDTO.LOAI_TRA_HANG.equals(dto.getLoaiDoiTra())) {
            buildRefundProcessPanel(processCard, dto, dlg);
		} else {
			JLabel lbl = new JLabel("Phiếu không thuộc nhóm Đổi sản phẩm / Trả hàng.");
            lbl.setFont(FONT_NORMAL);
            lbl.setForeground(TEXT_PRIMARY);
            processCard.add(lbl);
        }

        root.add(new JScrollPane(infoCard), BorderLayout.CENTER);
        root.add(processCard, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setSize(760, 680);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void openCreateDialog() {
		Window owner = SwingUtilities.getWindowAncestor(this);
		JDialog dialog = new JDialog(owner, "Tạo phiếu đổi/trả", JDialog.ModalityType.APPLICATION_MODAL);
		dialog.setLayout(new BorderLayout(0, 0));

		JPanel root = new JPanel(new BorderLayout(10, 10));
		root.setBackground(CONTENT_BG);
		root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

		JPanel dialogHeader = new JPanel(new BorderLayout()) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setPaint(new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				g2.dispose();
			}
		};
		dialogHeader.setOpaque(false);
		dialogHeader.setPreferredSize(new Dimension(0, 56));
		dialogHeader.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
		JLabel title = new JLabel("TẠO PHIẾU ĐỔI TRẢ");
		title.setFont(FONT_HEADER.deriveFont(Font.BOLD, 18f));
		title.setForeground(Color.WHITE);
		dialogHeader.add(title, BorderLayout.WEST);

		JPanel card = new JPanel();
		card.setOpaque(true);
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR),
				BorderFactory.createEmptyBorder(16, 16, 16, 16)));
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

		JComboBox<InvoiceOption> cbHoaDon = new JComboBox<>();
		styleComboBox(cbHoaDon);
		JComboBox<InvoiceItemOption> cbItem = new JComboBox<>();
		styleComboBox(cbItem);

		JTextField tfMaHoaDon = createStyledField();
		tfMaHoaDon.setEditable(false);
		JTextField tfMaSP = createStyledField();
		tfMaSP.setEditable(false);
		JTextField tfMaSerial = createStyledField();
		tfMaSerial.setEditable(false);
		JTextField tfSoLuong = createStyledField();
		tfSoLuong.setText("1");
		JComboBox<String> cbLoaiDialog = new JComboBox<>(new String[] {"Đổi sản phẩm", "Trả hàng"});
		styleComboBox(cbLoaiDialog);
		JComboBox<ProductOption> cbSPMoi = new JComboBox<>();
		styleComboBox(cbSPMoi);
		JComboBox<SerialOption> cbSerialMoi = new JComboBox<>();
		styleComboBox(cbSerialMoi);
		JTextField tfTienChenh = createStyledField();
		tfTienChenh.setText("0");
		tfTienChenh.setEditable(false);
		JTextArea taLyDo = createStyledArea();
		JTextArea taGhiChu = createStyledArea();
		JTextField tfNhanVien = createStyledField();
		tfNhanVien.setEditable(false);
		JTextField tfNgayYeuCau = createStyledField();
		tfNgayYeuCau.setEditable(false);

		int currentMaNV = SharedData.currentMaNV > 0 ? SharedData.currentMaNV : 1;
		tfNhanVien.setText(String.valueOf(currentMaNV));
		tfNgayYeuCau.setText(LocalDate.now().format(DF));

		for (InvoiceOption op : loadInvoiceOptions()) {
			cbHoaDon.addItem(op);
		}
		for (ProductOption p : loadProductOptions()) {
			cbSPMoi.addItem(p);
		}

		Runnable refreshByInvoice = () -> {
			InvoiceOption inv = (InvoiceOption) cbHoaDon.getSelectedItem();
			cbItem.removeAllItems();
			if (inv == null) {
				tfMaHoaDon.setText("");
				tfMaSP.setText("");
				tfMaSerial.setText("");
				return;
			}
			tfMaHoaDon.setText(String.valueOf(inv.maHoaDon));
			for (InvoiceItemOption item : loadInvoiceItems(inv.maHoaDon)) {
				cbItem.addItem(item);
			}
		};
		cbHoaDon.addActionListener(e -> refreshByInvoice.run());
		cbItem.addActionListener(e -> {
			InvoiceItemOption item = (InvoiceItemOption) cbItem.getSelectedItem();
			if (item == null) {
				tfMaSP.setText("");
				tfMaSerial.setText("");
				return;
			}
			tfMaSP.setText(String.valueOf(item.maSP));
			tfMaSerial.setText(String.valueOf(item.maSerial));
		});

		cbSPMoi.addActionListener(e -> {
			cbSerialMoi.removeAllItems();
			ProductOption p = (ProductOption) cbSPMoi.getSelectedItem();
			if (p != null) {
				for (SerialOption s : loadSerialOptions(p.maSP)) {
					cbSerialMoi.addItem(s);
				}
			}
			InvoiceItemOption oldItem = (InvoiceItemOption) cbItem.getSelectedItem();
			if (oldItem != null && p != null) {
				BigDecimal diff = p.gia.subtract(oldItem.giaBan);
				tfTienChenh.setText(diff.toPlainString());
			}
		});
		cbLoaiDialog.addActionListener(e -> {
			boolean exchange = "Đổi sản phẩm".equals(cbLoaiDialog.getSelectedItem());
			cbSPMoi.setEnabled(exchange);
			cbSerialMoi.setEnabled(exchange);
		});
		refreshByInvoice.run();
		cbLoaiDialog.setSelectedIndex(0);

		card.add(createTwoColField("Hóa đơn *", cbHoaDon, "Sản phẩm theo hóa đơn *", cbItem));
		card.add(Box.createVerticalStrut(10));
		card.add(createTwoColField("Mã hóa đơn", tfMaHoaDon, "Mã sản phẩm", tfMaSP));
		card.add(Box.createVerticalStrut(10));
		card.add(createTwoColField("Mã serial cũ", tfMaSerial, "Số lượng trả *", tfSoLuong));
		card.add(Box.createVerticalStrut(10));
		card.add(createTwoColField("Loại đổi trả *", cbLoaiDialog, "Nhân viên xử lý", tfNhanVien));
		card.add(Box.createVerticalStrut(10));
		card.add(createTwoColField("Ngày yêu cầu", tfNgayYeuCau, "Tiền chênh lệch", tfTienChenh));
		card.add(Box.createVerticalStrut(10));
		card.add(createTwoColField("Sản phẩm mới", cbSPMoi, "Serial mới", cbSerialMoi));
		card.add(Box.createVerticalStrut(10));
		card.add(createSingleField("Lý do *", new JScrollPane(taLyDo), 140));
		card.add(Box.createVerticalStrut(10));
		card.add(createSingleField("Ghi chú", new JScrollPane(taGhiChu), 84));

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		actions.setOpaque(false);
		JButton btnCancel = buildActionButton("Hủy", GRAY_BTN, GRAY_DARK, Color.WHITE);
		JButton btnSave = buildActionButton("Lưu phiếu", PRIMARY, PRIMARY_DARK, Color.WHITE);
		actions.add(btnCancel);
		actions.add(btnSave);

		btnCancel.addActionListener(e -> dialog.dispose());
		btnSave.addActionListener(e -> {
			try {
				DoiTraDTO dto = new DoiTraDTO();
				dto.setMaHoaDon(Integer.parseInt(tfMaHoaDon.getText().trim()));
				dto.setMaSP(Integer.parseInt(tfMaSP.getText().trim()));
				dto.setMaSerial(Integer.parseInt(tfMaSerial.getText().trim()));
				dto.setSoLuongTra(Integer.parseInt(tfSoLuong.getText().trim()));
				dto.setLoaiDoiTra(mapLoaiUiToDb((String) cbLoaiDialog.getSelectedItem()));

				if (DoiTraDTO.LOAI_DOI_SAN_PHAM.equals(dto.getLoaiDoiTra())) {
					ProductOption spMoi = (ProductOption) cbSPMoi.getSelectedItem();
					SerialOption serialMoi = (SerialOption) cbSerialMoi.getSelectedItem();
					if (spMoi == null || serialMoi == null) {
						throw new IllegalArgumentException("Vui lòng chọn sản phẩm mới và serial mới cho yêu cầu đổi sản phẩm.");
					}
					dto.setMaSPMoi(spMoi.maSP);
					dto.setMaSerialMoi(serialMoi.maSerial);
					dto.setTienChenhLech(spMoi.gia.subtract(loadGiaSanPham(dto.getMaSP())));
				} else {
					dto.setMaSPMoi(null);
					dto.setMaSerialMoi(null);
					dto.setTienChenhLech(BigDecimal.ZERO);
				}
				dto.setLyDo(taLyDo.getText().trim());
				dto.setGhiChu(taGhiChu.getText().trim());
				dto.setNgayYeuCau(LocalDate.now());
				dto.setTrangThai(DoiTraDTO.TRANG_THAI_DANG_XU_LY);
				dto.setMaNV(currentMaNV);

				int newId = bus.themPhieuDoiTra(dto);
				if (newId <= 0) {
					throw new IllegalStateException("Không nhận được mã phiếu mới từ hệ thống.");
				}

				dialog.dispose();
				loadData();
				JOptionPane.showMessageDialog(this, "Đã tạo phiếu đổi/trả #" + newId, "Thành công", JOptionPane.INFORMATION_MESSAGE);
			} catch (RuntimeException ex) {
				showError("Tạo phiếu thất bại", ex);
			}
		});

		root.add(dialogHeader, BorderLayout.NORTH);
		root.add(card, BorderLayout.CENTER);
		root.add(actions, BorderLayout.SOUTH);

		dialog.setContentPane(root);
		dialog.setSize(980, 740);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

    private JTextField createStyledField() {
		JTextField tf = new JTextField();
		tf.setFont(FONT_NORMAL);
		tf.setForeground(TEXT_PRIMARY);
		tf.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR),
				BorderFactory.createEmptyBorder(8, 10, 8, 10)));
		return tf;
	}

    private JTextArea createStyledArea() {
		JTextArea ta = new JTextArea();
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setFont(FONT_NORMAL);
		ta.setForeground(TEXT_PRIMARY);
		ta.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
		return ta;
	}

    private <T> void styleComboBox(JComboBox<T> cb) {
		cb.setFont(FONT_NORMAL);
		cb.setBackground(Color.WHITE);
		cb.setForeground(TEXT_PRIMARY);
		cb.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
		cb.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				lbl.setFont(FONT_NORMAL);
				if (!isSelected) {
					lbl.setBackground(Color.WHITE);
					lbl.setForeground(TEXT_PRIMARY);
				}
				return lbl;
			}
		});
	}

    private JPanel createTwoColField(String labelLeft, Component compLeft, String labelRight, Component compRight) {
		JPanel row = new JPanel(new GridBagLayout());
		row.setOpaque(false);
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(0, 0, 0, 10);
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = 0.5;
		gc.gridx = 0;
		row.add(createSingleField(labelLeft, compLeft, 36), gc);
		gc.gridx = 1;
		gc.insets = new Insets(0, 10, 0, 0);
		row.add(createSingleField(labelRight, compRight, 36), gc);
		return row;
	}

    private JPanel createSingleField(String label, Component comp, int height) {
		JPanel p = new JPanel(new BorderLayout(0, 4));
		p.setOpaque(false);
		JLabel lbl = new JLabel(label);
		lbl.setFont(FONT_HEADER);
		lbl.setForeground(TEXT_PRIMARY);
		p.add(lbl, BorderLayout.NORTH);

		if (comp instanceof JScrollPane sp) {
			sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
			sp.setPreferredSize(new Dimension(0, height));
			p.add(sp, BorderLayout.CENTER);
			return p;
		}
		if (comp instanceof JTextField tf) {
			tf.setPreferredSize(new Dimension(0, height));
		}
		if (comp instanceof JComboBox<?> cb) {
			cb.setPreferredSize(new Dimension(0, height));
		}
		if (comp instanceof JComponent jc) {
			jc.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
		}
		p.add(comp, BorderLayout.CENTER);
		return p;
	}

	private void addInfoCell(JPanel parent, GridBagConstraints gc, int row, int col, String label, String value) {
		JPanel cell = new JPanel(new BorderLayout(0, 2));
		cell.setOpaque(false);

		JLabel lbl = new JLabel(label);
		lbl.setFont(FONT_HEADER);
		lbl.setForeground(new Color(70, 90, 120));
		JLabel val = new JLabel(value);
		val.setFont(FONT_NORMAL);
		val.setForeground(TEXT_PRIMARY);

		cell.add(lbl, BorderLayout.NORTH);
		cell.add(val, BorderLayout.CENTER);

		gc.gridx = col;
		gc.gridy = row;
		parent.add(cell, gc);
	}

	private JPanel createReadOnlyArea(String label, String value, int height) {
		JTextArea ta = createStyledArea();
		ta.setEditable(false);
		ta.setText(value);
		JScrollPane sp = new JScrollPane(ta);
		sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
		return createSingleField(label, sp, height);
	}

    private String mapStatusUiToDb(String ui) {
        if (ui == null || "Tất cả trạng thái".equals(ui)) {
            return null;
        }
        if ("Chờ duyệt".equals(ui)) {
            return DoiTraDTO.TRANG_THAI_CHO_DUYET;
        }
        if ("Đang xử lý".equals(ui)) {
            return DoiTraDTO.TRANG_THAI_DANG_XU_LY;
        }
        if ("Từ chối".equals(ui)) {
            return DoiTraDTO.TRANG_THAI_TU_CHOI;
        }
        if ("Hoàn thành".equals(ui)) {
            return DoiTraDTO.TRANG_THAI_HOAN_THANH;
        }
        return ui;
    }

	private String mapStatusDbToUi(String db) {
		if (DoiTraDTO.TRANG_THAI_CHO_DUYET.equals(db)) {
			return "Chờ duyệt";
		}
		if (DoiTraDTO.TRANG_THAI_DANG_XU_LY.equals(db)) {
			return "Đang xử lý";
		}
		if (DoiTraDTO.TRANG_THAI_TU_CHOI.equals(db)) {
			return "Từ chối";
		}
		if (DoiTraDTO.TRANG_THAI_HOAN_THANH.equals(db)) {
			return "Hoàn thành";
		}
		return safe(db);
	}

	private String mapLoaiUiToDbFilter(String ui) {
		if (ui == null || "Tất cả loại".equals(ui)) {
			return null;
		}
		return mapLoaiUiToDb(ui);
	}

    private String mapLoaiUiToDb(String ui) {
        if ("Đổi sản phẩm".equals(ui)) {
            return DoiTraDTO.LOAI_DOI_SAN_PHAM;
        }
        if ("Trả hàng".equals(ui)) {
            return DoiTraDTO.LOAI_TRA_HANG;
        }
        if ("Bảo hành".equals(ui)) {
            return DoiTraDTO.LOAI_BAO_HANH;
        }
        return DoiTraDTO.LOAI_TRA_HANG;
    }

    private String mapLoaiDbToUi(String db) {
        if (DoiTraDTO.LOAI_DOI_SAN_PHAM.equals(db)) {
            return "Đổi sản phẩm";
        }
        if (DoiTraDTO.LOAI_TRA_HANG.equals(db)) {
            return "Trả hàng";
        }
        if (DoiTraDTO.LOAI_BAO_HANH.equals(db)) {
            return "Bảo hành";
        }
        return safe(db);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void showError(String title, Exception ex) {
        JOptionPane.showMessageDialog(this, title + ": " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

	private void buildExchangeProcessPanel(JPanel panel, DoiTraDTO dto, JDialog dlg) {
		panel.add(new JLabel("XỬ LÝ ĐỔI SẢN PHẨM"));
		panel.add(Box.createVerticalStrut(8));

		BigDecimal giaCu = loadGiaSanPham(dto.getMaSP());
		JLabel lblOld = new JLabel("Giá sản phẩm cũ: " + giaCu.toPlainString());
		lblOld.setFont(FONT_NORMAL);
		panel.add(lblOld);

		JComboBox<ProductOption> cbSPMoi = new JComboBox<>();
		styleComboBox(cbSPMoi);
		for (ProductOption p : loadProductOptions()) {
			cbSPMoi.addItem(p);
		}

		JComboBox<SerialOption> cbSerialMoi = new JComboBox<>();
		styleComboBox(cbSerialMoi);

		JLabel lblGiaMoi = new JLabel("Giá sản phẩm mới: 0");
		lblGiaMoi.setFont(FONT_NORMAL);
		JLabel lblLech = new JLabel("Tiền chênh lệch: 0");
		lblLech.setFont(FONT_HEADER);
		lblLech.setForeground(PRIMARY_DARK);

		cbSPMoi.addActionListener(e -> {
			cbSerialMoi.removeAllItems();
			ProductOption p = (ProductOption) cbSPMoi.getSelectedItem();
			if (p != null) {
				for (SerialOption s : loadSerialOptions(p.maSP)) {
					cbSerialMoi.addItem(s);
				}
				BigDecimal lech = p.gia.subtract(giaCu);
				lblGiaMoi.setText("Giá sản phẩm mới: " + p.gia.toPlainString());
				lblLech.setText("Tiền chênh lệch: " + lech.toPlainString());
			}
		});
		if (cbSPMoi.getItemCount() > 0) {
			cbSPMoi.setSelectedIndex(0);
		}

		panel.add(createSingleField("Chọn sản phẩm mới", cbSPMoi, 36));
		panel.add(Box.createVerticalStrut(6));
		panel.add(createSingleField("Chọn serial mới", cbSerialMoi, 36));
		panel.add(Box.createVerticalStrut(6));
		panel.add(lblGiaMoi);
		panel.add(Box.createVerticalStrut(4));
		panel.add(lblLech);
		panel.add(Box.createVerticalStrut(10));

		JButton btnConfirm = buildActionButton("Xác nhận đổi sản phẩm", SUCCESS, SUCCESS_DARK, Color.WHITE);
		btnConfirm.addActionListener(e -> {
			ProductOption spMoi = (ProductOption) cbSPMoi.getSelectedItem();
			SerialOption serialMoi = (SerialOption) cbSerialMoi.getSelectedItem();
			if (spMoi == null || serialMoi == null) {
				JOptionPane.showMessageDialog(dlg, "Vui lòng chọn đủ sản phẩm/serial mới.", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
				return;
			}
			BigDecimal lech = spMoi.gia.subtract(giaCu);
			try {
				bus.xuLyDoiSanPham(dto.getMaDoiTra(), spMoi.maSP, serialMoi.maSerial, lech, "Đã xử lý đổi sản phẩm");
				loadData();
				dlg.dispose();
			} catch (Exception ex) {
				showError("Xử lý đổi sản phẩm thất bại", ex);
			}
		});
		panel.add(btnConfirm);
	}

	private void buildRefundProcessPanel(JPanel panel, DoiTraDTO dto, JDialog dlg) {
		panel.add(new JLabel("XỬ LÝ TRẢ HÀNG - HOÀN TIỀN"));
		panel.add(Box.createVerticalStrut(8));

		BigDecimal gia = loadGiaSanPham(dto.getMaSP());
		BigDecimal refund = gia.multiply(BigDecimal.valueOf(dto.getSoLuongTra()));

		JLabel l1 = new JLabel("Sản phẩm: " + safe(dto.getTenSP()));
		JLabel l2 = new JLabel("Serial: " + safe(dto.getSerialCode()));
		JLabel l3 = new JLabel("Số lượng: " + dto.getSoLuongTra());
		JLabel l4 = new JLabel("Giá bán: " + gia.toPlainString());
		JLabel l5 = new JLabel("Số tiền hoàn: " + refund.toPlainString());
		l5.setFont(FONT_HEADER);
		l5.setForeground(PRIMARY_DARK);
		panel.add(l1); panel.add(l2); panel.add(l3); panel.add(l4); panel.add(l5);
		panel.add(Box.createVerticalStrut(10));

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		actions.setOpaque(false);
		JButton btnRefund = buildActionButton("Xác nhận hoàn tiền", SUCCESS, SUCCESS_DARK, Color.WHITE);
		JButton btnReject = buildActionButton("Từ chối yêu cầu", RED_BTN, RED_DARK, Color.WHITE);

		btnRefund.addActionListener(e -> {
			try {
				bus.xacNhanHoanTien(dto.getMaDoiTra(), "Đã hoàn tiền cho khách");
				loadData();
				dlg.dispose();
			} catch (Exception ex) {
				showError("Không thể xác nhận hoàn tiền", ex);
			}
		});
		btnReject.addActionListener(e -> {
			String reason = JOptionPane.showInputDialog(dlg, "Lý do từ chối:", "Từ chối", JOptionPane.PLAIN_MESSAGE);
			if (reason == null || reason.isBlank()) {
				return;
			}
			try {
				bus.tuChoiPhieu(dto.getMaDoiTra(), reason.trim());
				loadData();
				dlg.dispose();
			} catch (Exception ex) {
				showError("Không thể từ chối yêu cầu", ex);
			}
		});

		actions.add(btnRefund);
		actions.add(btnReject);
		panel.add(actions);
	}

	private List<InvoiceOption> loadInvoiceOptions() {
		List<InvoiceOption> list = new ArrayList<>();
		String sql = "SELECT DISTINCT hd.MaHoaDon FROM HOADON hd "
				+ "JOIN CHITIETHOADON ct ON ct.MaHoaDon = hd.MaHoaDon "
				+ "WHERE hd.TrangThai <> N'Huy' ORDER BY hd.MaHoaDon DESC";
		try (Connection cn = DBConnection.getConnection();
			 PreparedStatement ps = cn != null ? cn.prepareStatement(sql) : null;
			 ResultSet rs = ps != null ? ps.executeQuery() : null) {
			if (rs == null) {
				return list;
			}
			while (rs.next()) {
				list.add(new InvoiceOption(rs.getInt("MaHoaDon")));
			}
		} catch (Exception ignored) {
		}
		return list;
	}

	private List<InvoiceItemOption> loadInvoiceItems(int maHoaDon) {
		List<InvoiceItemOption> list = new ArrayList<>();
		String sql = "SELECT ct.MaSP, sp.TenSP, ct.MaSerial, sr.SerialCode, ct.DonGia "
				+ "FROM CHITIETHOADON ct "
				+ "JOIN SANPHAM sp ON sp.MaSP = ct.MaSP "
				+ "JOIN SERIAL sr ON sr.MaSerial = ct.MaSerial "
				+ "WHERE ct.MaHoaDon = ?";
		try (Connection cn = DBConnection.getConnection();
			 PreparedStatement ps = cn != null ? cn.prepareStatement(sql) : null) {
			if (ps == null) {
				return list;
			}
			ps.setInt(1, maHoaDon);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(new InvoiceItemOption(
							rs.getInt("MaSP"),
							rs.getString("TenSP"),
							rs.getInt("MaSerial"),
							rs.getString("SerialCode"),
							rs.getBigDecimal("DonGia")
					));
				}
			}
		} catch (Exception ignored) {
		}
		return list;
	}

	private List<ProductOption> loadProductOptions() {
		List<ProductOption> list = new ArrayList<>();
		String sql = "SELECT MaSP, TenSP, Gia FROM SANPHAM WHERE TrangThai = N'DangBan' ORDER BY TenSP";
		try (Connection cn = DBConnection.getConnection();
			 PreparedStatement ps = cn != null ? cn.prepareStatement(sql) : null;
			 ResultSet rs = ps != null ? ps.executeQuery() : null) {
			if (rs == null) {
				return list;
			}
			while (rs.next()) {
				list.add(new ProductOption(rs.getInt("MaSP"), rs.getString("TenSP"), rs.getBigDecimal("Gia")));
			}
		} catch (Exception ignored) {
		}
		return list;
	}

	private List<SerialOption> loadSerialOptions(int maSP) {
		List<SerialOption> list = new ArrayList<>();
		String sql = "SELECT MaSerial, SerialCode FROM SERIAL WHERE MaSP = ? AND TrangThai = N'TrongKho' ORDER BY MaSerial";
		try (Connection cn = DBConnection.getConnection();
			 PreparedStatement ps = cn != null ? cn.prepareStatement(sql) : null) {
			if (ps == null) {
				return list;
			}
			ps.setInt(1, maSP);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(new SerialOption(rs.getInt("MaSerial"), rs.getString("SerialCode")));
				}
			}
		} catch (Exception ignored) {
		}
		return list;
	}

	private BigDecimal loadGiaSanPham(int maSP) {
		String sql = "SELECT Gia FROM SANPHAM WHERE MaSP = ?";
		try (Connection cn = DBConnection.getConnection();
			 PreparedStatement ps = cn != null ? cn.prepareStatement(sql) : null) {
			if (ps == null) {
				return BigDecimal.ZERO;
			}
			ps.setInt(1, maSP);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getBigDecimal("Gia");
				}
			}
		} catch (Exception ignored) {
		}
		return BigDecimal.ZERO;
	}

	private static class InvoiceOption {
		final int maHoaDon;
		InvoiceOption(int maHoaDon) { this.maHoaDon = maHoaDon; }
		@Override public String toString() { return "HD" + maHoaDon; }
	}

	private static class InvoiceItemOption {
		final int maSP;
		final String tenSP;
		final int maSerial;
		final String serialCode;
		final BigDecimal giaBan;
		InvoiceItemOption(int maSP, String tenSP, int maSerial, String serialCode, BigDecimal giaBan) {
			this.maSP = maSP;
			this.tenSP = tenSP;
			this.maSerial = maSerial;
			this.serialCode = serialCode;
			this.giaBan = giaBan == null ? BigDecimal.ZERO : giaBan;
		}
		@Override public String toString() { return tenSP + " (" + serialCode + ")"; }
	}

	private static class ProductOption {
		final int maSP;
		final String tenSP;
		final BigDecimal gia;
		ProductOption(int maSP, String tenSP, BigDecimal gia) {
			this.maSP = maSP;
			this.tenSP = tenSP;
			this.gia = gia == null ? BigDecimal.ZERO : gia;
		}
		@Override public String toString() { return tenSP; }
	}

	private static class SerialOption {
		final int maSerial;
		final String serialCode;
		SerialOption(int maSerial, String serialCode) {
			this.maSerial = maSerial;
			this.serialCode = serialCode;
		}
		@Override public String toString() { return serialCode; }
	}
}

