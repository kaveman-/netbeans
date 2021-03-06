/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * CreateTagPanel.java
 *
 * Created on Jun 28, 2011, 4:35:14 PM
 */
package org.netbeans.modules.mercurial.remote.ui.tag;

/**
 *
 * 
 */
public class CreateTagPanel extends javax.swing.JPanel {

    /** Creates new form CreateTagPanel */
    public CreateTagPanel () {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTagName = new javax.swing.JLabel();
        lblRevision = new javax.swing.JLabel();
        lblMessage = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();

        lblTagName.setLabelFor(txtTagName);
        org.openide.awt.Mnemonics.setLocalizedText(lblTagName, org.openide.util.NbBundle.getMessage(CreateTagPanel.class, "CreateTagPanel.lblTagName.text")); // NOI18N
        lblTagName.setToolTipText(org.openide.util.NbBundle.getMessage(CreateTagPanel.class, "CreateTagPanel.lblTagName.TTtext")); // NOI18N

        txtRevision.setEditable(false);
        txtRevision.setText(org.openide.util.NbBundle.getMessage(CreateTagPanel.class, "CreateTagPanel.txtRevision.text")); // NOI18N

        lblRevision.setLabelFor(txtRevision);
        org.openide.awt.Mnemonics.setLocalizedText(lblRevision, org.openide.util.NbBundle.getMessage(CreateTagPanel.class, "CreateTagPanel.lblRevision.text")); // NOI18N
        lblRevision.setToolTipText(org.openide.util.NbBundle.getMessage(CreateTagPanel.class, "CreateTagPanel.lblRevision.toolTipText")); // NOI18N

        lblMessage.setLabelFor(txtTagName);
        org.openide.awt.Mnemonics.setLocalizedText(lblMessage, org.openide.util.NbBundle.getMessage(CreateTagPanel.class, "CreateTagPanel.lblMessage.text")); // NOI18N
        lblMessage.setToolTipText(org.openide.util.NbBundle.getMessage(CreateTagPanel.class, "CreateTagPanel.lblMessage.toolTipText")); // NOI18N

        txtMessage.setColumns(1);
        txtMessage.setLineWrap(true);
        txtMessage.setRows(1);
        jScrollPane1.setViewportView(txtMessage);

        org.openide.awt.Mnemonics.setLocalizedText(btnSelectRevision, org.openide.util.NbBundle.getMessage(CreateTagPanel.class, "CreateTagPanel.btnSelectRevision.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbLocal, org.openide.util.NbBundle.getMessage(CreateTagPanel.class, "CreateTagPanel.cbLocal.text")); // NOI18N
        cbLocal.setToolTipText(org.openide.util.NbBundle.getMessage(CreateTagPanel.class, "CreateTagPanel.cbLocal.TTtext")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTagName)
                            .addComponent(lblRevision))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(txtRevision, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSelectRevision))
                            .addComponent(txtTagName, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)))
                    .addComponent(lblMessage)
                    .addComponent(cbLocal))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTagName)
                    .addComponent(txtTagName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRevision)
                    .addComponent(txtRevision, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSelectRevision))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbLocal)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JButton btnSelectRevision = new javax.swing.JButton();
    final javax.swing.JCheckBox cbLocal = new javax.swing.JCheckBox();
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblRevision;
    private javax.swing.JLabel lblTagName;
    final javax.swing.JTextArea txtMessage = new javax.swing.JTextArea();
    final javax.swing.JTextField txtRevision = new javax.swing.JTextField();
    final javax.swing.JTextField txtTagName = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables
}
