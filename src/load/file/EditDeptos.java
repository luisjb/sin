/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import com.ucod.FlatLookAndFeel.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.*;

/**
 *
 * @author diego
 */
public class EditDeptos extends javax.swing.JDialog {

    public static final DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

    DefaultTableModel dtm;
    JTable tbl;
    JScrollPane scp;

    /**
     * Creates new form ChangePrice
     */
    public EditDeptos(java.awt.Dialog parent, ModalityType modal) { //(java.awt.Frame parent, boolean modal) {

        super(parent, modal);
        initComponents();

        tbl = new JTable() {
            @Override
            public boolean isCellEditable(int rowIndex, int vColIndex) {
                if (vColIndex == 1) {
                    return true;
                } else {
                    return false;
                }
            }

        };

        tbl.setDefaultEditor(Double.class, new DoubleCellEditor());
        tbl.setFillsViewportHeight(true);
        tbl.setSurrendersFocusOnKeystroke(true); // Habilitar la selección automática al editar una celda

        scp = new JScrollPane();

        setFilas();

        scp.add(tbl);
        pnlTbl.add(scp);
        this.setSize(600, 700);

        scp.setViewportView(tbl);

        scp.getVerticalScrollBar().setPreferredSize(new Dimension(30, 0));

        //setEventoMouseClicked(tbl);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

    }

    private static class DoubleCellEditor extends DefaultCellEditor {

        private JTextField textField;

        public DoubleCellEditor() {
            super(new JTextField());
            textField = (JTextField) getComponent();
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    SwingUtilities.invokeLater(() -> textField.selectAll());
                }
            });
        }

        @Override
        public boolean stopCellEditing() {
            try {
                if (textField.getText().trim().isEmpty()) {
                    textField.setText("0");
                }
            } catch (NumberFormatException e) {
                return false;
            } catch (Exception e) {
                return false;
            }
            return super.stopCellEditing();
        }
    }

//Encabezados de la tabla
    private String[] getColumnas() {
        String columna[] = new String[]{"ID", "Nombre"};
        return columna;
    }

    private void setFilas() {

        Object datos[] = new Object[2]; //Numero de columnas de la tabla

        try {
            dtm = new DefaultTableModel(null, getColumnas());

            for (SystelDBConnector.SystelDepto lstDepto : ChooseDevices.lstDeptos) {
                datos[0] = lstDepto.getId();
                datos[1] = lstDepto.getName();

                dtm.addRow(datos);
            }
            tbl.setModel(dtm);

            tbl.setFont(new java.awt.Font("Segoe UI Light", 1, 18));
            tbl.setRowHeight(30);

            // Aplicar el renderizador de celdas centradas a la cabecera de la tabla
            JTableHeader th;
            th = tbl.getTableHeader();
            th.setFont(new java.awt.Font("Segoe UI Light", 1, 20));
            th.setBackground(com.ucod.crud.Common.getNewButtonBgColor());
            th.setForeground(Color.BLACK);
            DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) th.getDefaultRenderer();
            headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            th.setDefaultRenderer(headerRenderer);

            tbl.setSelectionForeground(Color.BLACK);
            tbl.setSelectionBackground(com.ucod.crud.Common.getNewButtonBgColor());//azul

            tbl.setBackground(new Color(230, 230, 230));//gris
            tbl.setForeground(Color.BLACK);
            tbl.setDoubleBuffered(true);
            tbl.setRowSelectionAllowed(true);
            tbl.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            tbl.getTableHeader().setReorderingAllowed(false);

//PLU
            tbl.getColumnModel().getColumn(0).setMaxWidth(100);
            tbl.getColumnModel().getColumn(0).setMinWidth(100);
            tbl.getColumnModel().getColumn(0).setPreferredWidth(100);

//NOMBRE
            tbl.getColumnModel().getColumn(1).setMaxWidth(320);
            tbl.getColumnModel().getColumn(1).setMinWidth(320);
            tbl.getColumnModel().getColumn(1).setPreferredWidth(320);
//PRECIO
            tbl.getColumnModel().getColumn(0).setCellRenderer(new MyRenderer());
            tbl.getColumnModel().getColumn(1).setCellRenderer(new MyRenderer());
            tbl.getColumnModel().getColumn(1).setCellEditor(new DoubleCellEditor());

        } catch (Exception e) {
            Logger.getLogger(EditDeptos.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        pnlTbl = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        btnCancel = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("load/file/Bundle"); // NOI18N
        setTitle(bundle.getString("EditDeptos.title_1")); // NOI18N
        setResizable(false);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        pnlTbl.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel1.add(pnlTbl, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jPanel3, gridBagConstraints);

        btnCancel.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        btnCancel.setText(bundle.getString("ChooseDevices.btnCancel.text")); // NOI18N
        btnCancel.setMaximumSize(new java.awt.Dimension(120, 48));
        btnCancel.setMinimumSize(new java.awt.Dimension(120, 48));
        btnCancel.setPreferredSize(new java.awt.Dimension(120, 48));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel2.add(btnCancel, gridBagConstraints);

        btnSave.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        btnSave.setText(bundle.getString("ShowConfigs.btnSave.text")); // NOI18N
        btnSave.setMaximumSize(new java.awt.Dimension(120, 48));
        btnSave.setMinimumSize(new java.awt.Dimension(120, 48));
        btnSave.setPreferredSize(new java.awt.Dimension(120, 48));
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel2.add(btnSave, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(jPanel2, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        int rowCount = tbl.getRowCount();
        List<SystelDBConnector.SystelDepto> deptos = new ArrayList<>();

        for (int row = 0; row < rowCount; row++) {
            int id = (int) tbl.getValueAt(row, 0);
            String name = (String) tbl.getValueAt(row, 1);
            deptos.add(new SystelDBConnector.SystelDepto(id, name));
        }

        MainApp.chooseDev.lstDeptos = FileLoader.parseInputMGVDepto(deptos);
        
        this.dispose();
    }//GEN-LAST:event_btnSaveActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel pnlTbl;
    // End of variables declaration//GEN-END:variables

    class MyRenderer implements TableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            Component c = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            c.setBackground(UpperEssentialLookAndFeel.getWindowBackground());

            if (row >= 0 && column >= 2) {
                c.setBackground(Color.WHITE);
            }

            return c;

        }
    }

}
