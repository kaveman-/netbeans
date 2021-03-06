/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

/*
 * GeneralInfoPanel.java
 *
 * Created on July 25, 2005, 10:37 AM
 */
package org.netbeans.modules.mobility.end2end.multiview;

import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import javax.swing.JComponent;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.end2end.client.config.ClientConfiguration;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.openide.util.NbBundle;

/**
 *
 */
public class ClientGeneralInfoPanel extends SectionInnerPanel {
    
    final private transient E2EDataObject dataObject;
    
    /** Creates new form GeneralInfoPanel */
    public ClientGeneralInfoPanel( SectionView sectionView, E2EDataObject dataObject ) {
        super( sectionView );
        
        this.dataObject = dataObject;
        
        initComponents();
        
        initValues();
    }
    
    private void initValues() {
        final Configuration config = dataObject.getConfiguration();
        
        final ClientConfiguration cc = config.getClientConfiguration();
        final String projectName = cc.getProjectName();
        if( "".equals( projectName )) {
            //System.err.println("ERR_ProjectNameIsNotValid");
            getSectionView().getErrorPanel().setError(
                    new Error( Error.TYPE_FATAL, Error.ERROR_MESSAGE,
                    NbBundle.getMessage( ClientGeneralInfoPanel.class, "ERR_ProjectNameIsNotValid" ), textProject ));
        }
        textProject.setText( projectName );
        textPackage.setText( cc.getClassDescriptor().getPackageName() );
        final String clientName = cc.getClassDescriptor().getLeafClassName();
        if( "".equals( clientName )) {
            //System.err.println("ERR_ClientNameIsNotValid");
            getSectionView().getErrorPanel().setError(
                    new Error( Error.TYPE_FATAL, Error.ERROR_MESSAGE,
                    NbBundle.getMessage( ClientGeneralInfoPanel.class, "ERR_ClientNameIsNotValid" ), textClientName ));
        }
        textClientName.setText( clientName );
    }
    
    public JComponent getErrorComponent( @SuppressWarnings("unused")
	final String errorId ) {
        return null;
    }
    
    public void linkButtonPressed( @SuppressWarnings("unused")
	final Object ddBean, @SuppressWarnings("unused")
	final String ddProperty ) {
    }
    
    public void setValue( @SuppressWarnings("unused")
	final JComponent source, @SuppressWarnings("unused")
	final Object value ) {
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        textPackage = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        textClientName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        textProject = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(textPackage);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ClientGeneralInfoPanel.class, "LABEL_Package")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        add(jLabel1, gridBagConstraints);

        textPackage.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 10);
        add(textPackage, gridBagConstraints);

        jLabel2.setLabelFor(textClientName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ClientGeneralInfoPanel.class, "LABEL_Client_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        add(jLabel2, gridBagConstraints);

        textClientName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 10);
        add(textClientName, gridBagConstraints);

        jLabel3.setLabelFor(textProject);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ClientGeneralInfoPanel.class, "LABEL_Project")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 0);
        add(jLabel3, gridBagConstraints);

        textProject.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 10);
        add(textProject, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField textClientName;
    private javax.swing.JTextField textPackage;
    private javax.swing.JTextField textProject;
    // End of variables declaration//GEN-END:variables
    
}
