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
 * RevisionsPanel.java
 *
 * Created on Dec 21, 2010, 5:14:40 PM
 */

package org.netbeans.modules.git.remote.ui.repository;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitObjectType;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.SearchCriteria;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.git.remote.cli.progress.RevisionInfoListener;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 */
public class RevisionListPanel extends javax.swing.JPanel implements ActionListener, DocumentListener {

    private ProgressMonitor.DefaultProgressMonitor listHistoryMonitor;
    private final List<GitRevisionInfoDelegate> revisionModel;
    private final DefaultListModel revisionDisplayModel;
    private GitProgressSupport supp;
    private final Object LOCK = new Object();
    private String lastHWRevision;
    private VCSFileProxy lastHWRepository;
    private Revision currRevision;
    private VCSFileProxy currRepository;
    private VCSFileProxy[] currRoots;
    private int currLimit;
    private boolean addition;
    private final int DEFAULT_LIMIT = 10;
    
    /** Creates new form RevisionsPanel */
    public RevisionListPanel() {
        revisionModel = new ArrayList<>();
        lstRevisions.setModel(revisionDisplayModel = new DefaultListModel());
        lstRevisions.setFixedCellHeight(-1);
        lstRevisions.setCellRenderer(new RevisionRenderer());
        initComponents();
        attachListeners();
    }

    @Override
    public void removeNotify () {
        cancelBackgroundTasks();
        super.removeNotify();
    }

    private void attachListeners () {
        btnAll.addActionListener(this);
        btnNext10.addActionListener(this);
        btnRefresh.addActionListener(this);
        txtFilter.getDocument().addDocumentListener(this);
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        updateFilter();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        updateFilter();
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        VCSFileProxy repo;
        VCSFileProxy[] roots;
        Revision revision;
        int limit;
        boolean add = true;
        synchronized (LOCK) {
            repo = currRepository;
            roots = currRoots;
            revision = currRevision;
            limit = currLimit;
        }
        if (btnAll == e.getSource()) {
            limit = -1;
        } else if (btnNext10 == e.getSource()) {
            limit += 10;
        } else if (btnRefresh == e.getSource()) {
            add = false;
            limit = DEFAULT_LIMIT;
        }
        updateHistory(repo, roots, revision, limit, add);
    }

    GitRevisionInfo getSelectedRevision () {
        GitRevisionInfoDelegate delegate = (GitRevisionInfoDelegate) lstRevisions.getSelectedValue();
        return delegate == null ? null : delegate.info;
    }

    private void updateFilter () {
        revisionDisplayModel.clear();
        for (GitRevisionInfoDelegate info : revisionModel) {
            if (applyToFilter(info)) {
                revisionDisplayModel.addElement(info);
            }
        }
    }

    private void addToDisplayModel (GitRevisionInfoDelegate info) {
        if (applyToFilter(info)) {
            revisionDisplayModel.addElement(info);
        }
    }

    private boolean applyToFilter (GitRevisionInfoDelegate info) {
        boolean apply;
        String filter = txtFilter.getText().toLowerCase(Locale.getDefault());
        apply = info.getMessage().toLowerCase(Locale.getDefault()).contains(filter)
                || info.getRevision().toLowerCase(Locale.getDefault()).contains(filter);
        return apply;
    }

    private class RevisionRenderer extends JTextPane implements ListCellRenderer {

        private final Style selectedStyle;
        private final Style normalStyle;
        private final Color selectionBackground;
        private final Color selectionForeground;

        public RevisionRenderer () {
            selectionBackground = new JList().getSelectionBackground();
            selectionForeground = new JList().getSelectionForeground();

            selectedStyle = addStyle("selected", null); // NOI18N
            StyleConstants.setForeground(selectedStyle, selectionForeground); // NOI18N
            StyleConstants.setBackground(selectedStyle, selectionBackground); // NOI18N
            normalStyle = addStyle("normal", null); // NOI18N
            StyleConstants.setForeground(normalStyle, UIManager.getColor("List.foreground")); // NOI18N

            setLayout(new BorderLayout());
            setBorder(null);
        }
        
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            GitRevisionInfoDelegate revision = (GitRevisionInfoDelegate) value;
            StyledDocument sd = getStyledDocument();

            String tooltip = null;
            Style style;
            Color backgroundColor;

            if (isSelected) {
                backgroundColor = selectionBackground;
                style = selectedStyle;
            } else {
                backgroundColor = UIManager.getColor("List.background"); // NOI18N
                style = normalStyle;
            }
            setBackground(backgroundColor);

            try {
                // clear document
                StringBuilder sb = new StringBuilder();
                String revStr = revision.getRevision();
                sb.append(revStr.length() > 7 ? revStr.substring(0, 7) : revStr).append(" - "); //NOI18N
                sb.append(revision.getMessage());
                tooltip = sb.toString();
                sd.remove(0, sd.getLength());
                String text = sb.toString();
                sd.insertString(0, text, style);
                String filter = txtFilter.getText().toLowerCase(Locale.getDefault());
                if (!isSelected && !filter.isEmpty()) {
                    text = text.toLowerCase(Locale.getDefault());
                    int pos = -filter.length();
                    while ((pos = text.indexOf(filter, pos + filter.length())) > -1) {
                        sd.setCharacterAttributes(pos, filter.length(), selectedStyle, false);
                    }
                }
            } catch (BadLocationException e) {
                //
            }
            setToolTipText(tooltip);

            return this;
        }
        
    }
    
    private void enableButtons (final boolean enabled) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                btnAll.setEnabled(enabled);
                btnNext10.setEnabled(enabled);
                btnRefresh.setEnabled(enabled);
            }
        });
    }
    
    private void cancelBackgroundTasks () {
        synchronized (LOCK) {
            if (supp != null) {
                supp.cancel();
            }
            if (listHistoryMonitor != null) {
                listHistoryMonitor.cancel();
            }
        }
    }
    
    void updateHistory (VCSFileProxy repository, VCSFileProxy[] roots, Revision revision) {
        updateHistory(repository, roots, revision, DEFAULT_LIMIT, null);
    }
    
    private void updateHistory (VCSFileProxy repository, VCSFileProxy[] roots, Revision revision, int limit, Boolean addition) {
        synchronized (LOCK) {
            if (addition == null && (repository == lastHWRepository || lastHWRepository != null && lastHWRepository.equals(repository))
                    && (revision == null && lastHWRevision == null || lastHWRevision != null && revision != null && lastHWRevision.equals(revision.getCommitId()))) {
                // no change made (selected repository i the same and selected revision is the same)
                return;
            }
            this.addition = Boolean.TRUE.equals(addition);
            lastHWRepository = repository;
            lastHWRevision = revision == null ? null : revision.getCommitId();
            currRepository = repository;
            currRevision = revision;
            currRoots = roots;
            currLimit = limit;
            cancelBackgroundTasks();
            supp = new ListHistoryProgressSupport();
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(RevisionListPanel.class, "LBL_RevisionList.LoadingRevisions")); //NOI18N
        }
    }
    
    private class ListHistoryProgressSupport extends GitProgressSupport.NoOutputLogging implements RevisionInfoListener, ListSelectionListener {
        
        private final List<GitRevisionInfo> revisions = new LinkedList<>();
        private final Set<String> displayedRevisions = new HashSet<>(10);
        private boolean reselected;
        private GitRevisionInfoDelegate selectedRevision;
        private int limit;
        
        @Override
        public void perform () {
            Revision rev;
            VCSFileProxy repository;
            VCSFileProxy[] roots;
            final boolean add;
            synchronized (LOCK) {
                rev = currRevision;
                repository = currRepository;
                roots = currRoots;
                limit = currLimit;
                add = addition;
            }
            synchronized (revisions) {
                revisions.clear();
            }
            if (repository != null) {
                listHistoryMonitor = new ProgressMonitor.DefaultProgressMonitor();
                if (isCanceled()) {
                    return;
                }
                boolean finished = false;
                try {
                    GitClient client = getClient();
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            selectedRevision = (GitRevisionInfoDelegate) lstRevisions.getSelectedValue();
                            displayedRevisions.clear();
                            if (add) {
                                for (GitRevisionInfoDelegate info : revisionModel) {
                                    displayedRevisions.add(info.getRevision());
                                }
                            } else {
                                revisionModel.clear();
                                updateFilter();
                            }
                            reselected = false;
                            enableButtons(false);
                        }
                    });
                    lstRevisions.addListSelectionListener(this);
                    client.addNotificationListener(this);
                    SearchCriteria criteria = new SearchCriteria();
                    criteria.setFiles(roots);
                    if (limit > 0) {
                        criteria.setLimit(limit + 1); // get one extra revision to be able to enable/disable buttons
                    }
                    if (rev != null) {
                        criteria.setRevisionTo(rev.getCommitId());
                    }
                    final GitRevisionInfo[] revs = client.log(criteria, false, listHistoryMonitor);
                    if (!isCanceled()) {
                        finished = true;
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                btnRefresh.setEnabled(true);
                                if (limit > 0 && revs.length > limit) {
                                    btnAll.setEnabled(true);
                                    btnNext10.setEnabled(true);
                                }
                            }
                        });
                    }
                } catch (GitException.MissingObjectException ex) {
                    if (ex.getObjectType() != GitObjectType.HEAD) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    }
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                } finally {
                    final boolean isFinished = finished;
                    lstRevisions.removeListSelectionListener(this);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            if (!isFinished) {
                                enableButtons(true);
                            }
                            // do not keep the reference forever
                            selectedRevision = null;
                        }
                    });
                }
            }
        }

        @Override
        public void notifyRevisionInfo (GitRevisionInfo revisionInfo) {
            synchronized (revisions) {
                revisions.add(revisionInfo);
            }
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run () {
                    synchronized (revisions) {
                        while (!revisions.isEmpty()) {
                            GitRevisionInfoDelegate info = new GitRevisionInfoDelegate(revisions.remove(0));// override toString, so one can Ctrl+C the revision string
                            if ((limit < 0 || limit > revisionModel.size()) && !displayedRevisions.contains(info.getRevision())) {
                                revisionModel.add(info);
                                addToDisplayModel(info);
                                if (!reselected && selectedRevision != null && info.getRevision().equals(selectedRevision.getRevision())) {
                                    // has not yet been reselected or manually selected by user
                                    lstRevisions.setSelectedValue(info, false);
                                    reselected = true;
                                    selectedRevision = null;
                                }
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void valueChanged (ListSelectionEvent e) {
            if (!e.getValueIsAdjusting() && e.getLastIndex() > -1 && e.getFirstIndex() > -1) {
                reselected = true;
                selectedRevision = null;
            }
        }
    }
    
    private static class GitRevisionInfoDelegate {
        private final GitRevisionInfo info;

        public GitRevisionInfoDelegate (GitRevisionInfo info) {
            this.info = info;
        }

        public String getRevision () {
            return info.getRevision();
        }

        public String getMessage () {
            return info.getShortMessage();
        }

        @Override
        public String toString () {
            return getRevision();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jLabel1 = new javax.swing.JLabel();
        btnNext10 = new org.netbeans.modules.versioning.history.LinkButton();
        jLabel2 = new javax.swing.JLabel();
        btnAll = new org.netbeans.modules.versioning.history.LinkButton();
        jLabel3 = new javax.swing.JLabel();
        btnRefresh = new org.netbeans.modules.versioning.history.LinkButton();
        jLabel4 = new javax.swing.JLabel();
        txtFilter = new javax.swing.JTextField();

        lstRevisions.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(lstRevisions);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RevisionListPanel.class, "RevisionListPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnNext10, org.openide.util.NbBundle.getMessage(RevisionListPanel.class, "RevisionListPanel.btnNext10.text")); // NOI18N
        btnNext10.setToolTipText(org.openide.util.NbBundle.getMessage(RevisionListPanel.class, "RevisionListPanel.btnNext10.TTtext")); // NOI18N
        btnNext10.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RevisionListPanel.class, "RevisionListPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnAll, org.openide.util.NbBundle.getMessage(RevisionListPanel.class, "RevisionListPanel.btnAll.text")); // NOI18N
        btnAll.setToolTipText(org.openide.util.NbBundle.getMessage(RevisionListPanel.class, "RevisionListPanel.btnAll.TTtext")); // NOI18N
        btnAll.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(RevisionListPanel.class, "RevisionListPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnRefresh, org.openide.util.NbBundle.getMessage(RevisionListPanel.class, "RevisionListPanel.btnRefresh.text")); // NOI18N
        btnRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(RevisionListPanel.class, "RevisionListPanel.btnRefresh.toolTipText")); // NOI18N

        jLabel4.setLabelFor(txtFilter);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(RevisionListPanel.class, "RevisionListPanel.jLabel4.text")); // NOI18N
        jLabel4.setToolTipText(org.openide.util.NbBundle.getMessage(RevisionListPanel.class, "RevisionListPanel.jLabel4.TTtext")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNext10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(jLabel2)
                        .addGap(5, 5, 5)
                        .addComponent(btnAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(jLabel3)
                        .addGap(5, 5, 5)
                        .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFilter)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(btnNext10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(txtFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.versioning.history.LinkButton btnAll;
    private org.netbeans.modules.versioning.history.LinkButton btnNext10;
    private org.netbeans.modules.versioning.history.LinkButton btnRefresh;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    final javax.swing.JList lstRevisions = new javax.swing.JList();
    private javax.swing.JTextField txtFilter;
    // End of variables declaration//GEN-END:variables

}
