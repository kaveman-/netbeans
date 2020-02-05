/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

/*
 * ExportBundlePanel.java
 *
 * Created on Feb 16, 2010, 9:47:08 AM
 */

package org.netbeans.modules.mercurial.remote.ui.diff;

import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.remotefs.versioning.api.ExportDiffSupport.AbstractExportDiffPanel;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
public class ExportBundlePanel extends JPanel {

    static final String CMD_SELECT_REVISION = "command.selectRevision"; //NOI18N
    static final String CMD_SELECT_BASE_REVISION = "command.selectBaseRevision"; //NOI18N
    private final VCSFileProxy repository;
    private AbstractExportDiffPanel p;

    /** Creates new form ExportBundlePanel */
    public ExportBundlePanel (VCSFileProxy repository) {
        this.repository = repository;
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblBaseRevision = new javax.swing.JLabel();
        btnBaseRevision = new javax.swing.JButton();
        lblTopRevision = new javax.swing.JLabel();
        btnTopRevision = new javax.swing.JButton();
        insidePanel = new javax.swing.JPanel();

        lblBaseRevision.setLabelFor(baseRevision);
        org.openide.awt.Mnemonics.setLocalizedText(lblBaseRevision, org.openide.util.NbBundle.getMessage(ExportBundlePanel.class, "ExportBundlePanel.lblBaseRevision.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnBaseRevision, org.openide.util.NbBundle.getMessage(ExportBundlePanel.class, "ExportBundlePanel.btnBaseRevision.text")); // NOI18N
        btnBaseRevision.setActionCommand(CMD_SELECT_BASE_REVISION);

        lblTopRevision.setLabelFor(txtTopRevision);
        org.openide.awt.Mnemonics.setLocalizedText(lblTopRevision, org.openide.util.NbBundle.getMessage(ExportBundlePanel.class, "ExportBundlePanel.lblTopRevision.text")); // NOI18N

        txtTopRevision.setEditable(false);
        txtTopRevision.setText(org.openide.util.NbBundle.getMessage(ExportBundlePanel.class, "ExportBundlePanel.txtTopRevision.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnTopRevision, org.openide.util.NbBundle.getMessage(ExportBundlePanel.class, "ExportBundlePanel.btnTopRevision.text")); // NOI18N
        btnTopRevision.setActionCommand(CMD_SELECT_REVISION);

        insidePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        insidePanel.setLayout(new javax.swing.BoxLayout(insidePanel, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblBaseRevision)
                            .addComponent(lblTopRevision))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(baseRevision, 0, 162, Short.MAX_VALUE)
                            .addComponent(txtTopRevision, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnTopRevision)
                            .addComponent(btnBaseRevision)))
                    .addComponent(insidePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTopRevision)
                    .addComponent(txtTopRevision, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTopRevision))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBaseRevision)
                    .addComponent(btnBaseRevision)
                    .addComponent(baseRevision, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(insidePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        lblBaseRevision.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExportBundlePanel.class, "ACSD_ExportBundlePanel_BaseRevision")); // NOI18N
        lblTopRevision.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExportBundlePanel.class, "ACSD_ExportBundlePanel_ToRevision")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JComboBox baseRevision = new javax.swing.JComboBox();
    private javax.swing.JButton btnBaseRevision;
    private javax.swing.JButton btnTopRevision;
    private javax.swing.JPanel insidePanel;
    private javax.swing.JLabel lblBaseRevision;
    private javax.swing.JLabel lblTopRevision;
    final javax.swing.JTextField txtTopRevision = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables

    void setInsidePanel(AbstractExportDiffPanel targetPanel) {
        this.p = targetPanel;
        targetPanel.setBorder(BorderFactory.createEmptyBorder());
        setDefaultOutputFile();
        insidePanel.add(targetPanel);
    }

    String getSelectedBaseRevision() {
        return (String) baseRevision.getSelectedItem();
    }

    String getSelectedRevision() {
        return txtTopRevision.getText().trim();
    }

    void addActionListener (ActionListener al) {
        btnBaseRevision.addActionListener(al);
        btnTopRevision.addActionListener(al);
    }

    void removeActionListener (ActionListener al) {
        btnBaseRevision.removeActionListener(al);
        btnTopRevision.removeActionListener(al);
    }

    private void setDefaultOutputFile() {
        String folderName = HgModuleConfig.getDefault(repository).getPreferences().get("ExportDiff.saveFolder", HgModuleConfig.getDefault(repository).getExportFolder()); // NOI18N
        String fileName = repository.getName(); //NOI18N
        VCSFileProxy file = VCSFileProxySupport.getResource(repository, folderName + "/" + fileName + ".hg"); //NOI18N
        p.setOutputFileText(file.getPath());
    }
}
