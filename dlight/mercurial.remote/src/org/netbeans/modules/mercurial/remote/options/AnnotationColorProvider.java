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

package org.netbeans.modules.mercurial.remote.options;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.Collection;
import javax.swing.UIManager;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.MercurialAnnotator;
import org.netbeans.modules.mercurial.remote.MercurialVCS;
import org.netbeans.modules.versioning.util.OptionsPanelColorProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * 
 */
@ServiceProviders({@ServiceProvider(service=OptionsPanelColorProvider.class), @ServiceProvider(service=AnnotationColorProvider.class)})
//@OptionsPanelController.Keywords(keywords={"mercurial", "#KW_Colors"}, location=OptionsDisplayer.FONTSANDCOLORS, tabTitle="#CTL_AnnotationColorsPanel.title")
@Messages("CTL_AnnotationColorsPanel.title=Versioning")
public class AnnotationColorProvider extends OptionsPanelColorProvider {

    private static String name;
    private static AnnotationColorProvider INSTANCE;
    private static final Color ADDED_COLOR;
    private static final Color MODIFIED_COLOR;
    private static final Color DELETED_COLOR;
    private static final Color CONFLICTED_COLOR;
    private static final Color IGNORED_COLOR;
    private static final Color TEXT_ANNOTATION_COLOR;
    static {
        Color c = UIManager.getColor("nb.versioning.added.color"); //NOI18N
        ADDED_COLOR = c == null ? new Color(0, 0x80, 0) : c;
        c = UIManager.getColor("nb.versioning.modified.color"); //NOI18N
        MODIFIED_COLOR = c == null ? new Color(0, 0, 0xff) : c;
        c = UIManager.getColor("nb.versioning.deleted.color"); //NOI18N
        DELETED_COLOR = c == null ? new Color(0x99, 0x99, 0x99) : c;
        c = UIManager.getColor("nb.versioning.conflicted.color"); //NOI18N
        CONFLICTED_COLOR = c == null ? new Color(0xff, 0, 0) : c;
        c = UIManager.getColor("nb.versioning.ignored.color"); //NOI18N
        IGNORED_COLOR = c == null ? new Color(0x99, 0x99, 0x99) : c;
        c = UIManager.getColor("nb.versioning.textannotation.color"); //NOI18N
        TEXT_ANNOTATION_COLOR = c == null ? new Color(0x99, 0x99, 0x99) : c;
    }

    public final AnnotationFormat UP_TO_DATE_FILE = createAnnotationFormat("uptodate", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_UpToDate"), null, false); //NOI18N
    public final AnnotationFormat NEW_LOCALLY_FILE = createAnnotationFormat("newLocally", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_NewLocally"), ADDED_COLOR, false); //NOI18N
    public final AnnotationFormat NEW_LOCALLY_FILE_TOOLTIP = createAnnotationFormat("newLocallyTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_NewLocally"), ADDED_COLOR, true); //NOI18N
    public final AnnotationFormat ADDED_LOCALLY_FILE = createAnnotationFormat("addedLocally", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_AddedLocally"), ADDED_COLOR, false); //NOI18N
    public final AnnotationFormat ADDED_LOCALLY_FILE_TOOLTIP = createAnnotationFormat("addedLocallyTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_AddedLocally"), ADDED_COLOR, true); //NOI18N
    public final AnnotationFormat COPIED_LOCALLY_FILE = createAnnotationFormat("copiedLocally", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_AddedLocallyCopied"), ADDED_COLOR, false); //NOI18N
    public final AnnotationFormat COPIED_LOCALLY_FILE_TOOLTIP = createAnnotationFormat("copiedLocallyTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_AddedLocallyCopied"), ADDED_COLOR, true); //NOI18N
    public final AnnotationFormat MOVED_LOCALY_FILE = createAnnotationFormat("movedLocally", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_AddedLocallyMoved"), ADDED_COLOR, false); //NOI18N
    public final AnnotationFormat MOVED_LOCALY_FILE_TOOLTIP = createAnnotationFormat("movedLocallyTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_AddedLocallyMoved"), ADDED_COLOR, true); //NOI18N
    public final AnnotationFormat MODIFIED_LOCALLY_FILE = createAnnotationFormat("modifiedLocally", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_ModifiedLocally"), MODIFIED_COLOR, false); //NOI18N
    public final AnnotationFormat MODIFIED_LOCALLY_FILE_TOOLTIP = createAnnotationFormat("modifiedLocallyTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_ModifiedLocally"), MODIFIED_COLOR, true); //NOI18N
    public final AnnotationFormat REMOVED_LOCALLY_FILE = createAnnotationFormat("removedLocally", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_RemovedLocally"), DELETED_COLOR, false); //NOI18N
    public final AnnotationFormat REMOVED_LOCALLY_FILE_TOOLTIP = createAnnotationFormat("removedLocallyTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_RemovedLocally"), DELETED_COLOR, true); //NOI18N
    public final AnnotationFormat DELETED_LOCALLY_FILE = createAnnotationFormat("deletedLocally", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_DeletedLocally"), DELETED_COLOR, false); //NOI18N
    public final AnnotationFormat DELETED_LOCALLY_FILE_TOOLTIP = createAnnotationFormat("deletedLocallyTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_DeletedLocally"), DELETED_COLOR, true); //NOI18N
    public final AnnotationFormat EXCLUDED_FILE = createAnnotationFormat("excluded", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_Excluded"), IGNORED_COLOR, false); //NOI18N
    public final AnnotationFormat EXCLUDED_FILE_TOOLTIP = createAnnotationFormat("excludedTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_Excluded"), IGNORED_COLOR, true); //NOI18N
    public final AnnotationFormat CONFLICT_FILE = createAnnotationFormat("conflict", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_Conflict"), CONFLICTED_COLOR, false); //NOI18N
    public final AnnotationFormat CONFLICT_FILE_TOOLTIP = createAnnotationFormat("conflictTT", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_Conflict"), CONFLICTED_COLOR, true); //NOI18N
    public final AnnotationFormat TEXT_ANNOTATION = createAnnotationFormat("textAnnotation", NbBundle.getMessage(MercurialAnnotator.class, "CTL_FileInfoStatus_TextAnnotation"), TEXT_ANNOTATION_COLOR, true); //NOI18N

    public AnnotationColorProvider () {
        initColors();
    }
    
    public static synchronized AnnotationColorProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = Lookup.getDefault().lookup(AnnotationColorProvider.class);
            if (INSTANCE == null) {
                INSTANCE = new AnnotationColorProvider();
            }
        }
        return INSTANCE;
    }

    @Override
    public String getName() {
        if (name == null) {
            name = MercurialVCS.getDisplayName();
        }
        return name;
    }

    @Override
    protected Color getSavedColor (String key, Color defaultColor) {
        return HgModuleConfig.getDefault(null).getColor(key, defaultColor);
    }

    @Override
    protected MessageFormat createFormat (Color color, boolean isTooltip) {
        StringBuilder annotationFormatString = new StringBuilder("{0}"); //NOI18N
        if (color != null) {
            annotationFormatString = new StringBuilder("<font color=\"#") //NOI18N
                    .append(to2Hex(color.getRed())).append(to2Hex(color.getGreen())).append(to2Hex(color.getBlue())).append("\"").append(">{0}</font>"); //NOI18N
        }
        if (!isTooltip) {
            annotationFormatString.append("{1}"); //NOI18N
        }
        return new MessageFormat(annotationFormatString.toString());
    }

    @Override
    protected void saveColors (Collection<AnnotationFormat> colors) {
        for (AnnotationFormat af : colors) {
            if (af != null) {
                HgModuleConfig.getDefault(null).setColor(getColorKey(af.getKey()), af.getActualColor());
            }
        }
        Mercurial.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                Mercurial.getInstance().refreshAllAnnotations();
            }
        });
    }

    private void initColors() {
        putColor(ADDED_LOCALLY_FILE);
        putColor(CONFLICT_FILE);
        putColor(COPIED_LOCALLY_FILE);
        putColor(MOVED_LOCALY_FILE);
        putColor(DELETED_LOCALLY_FILE);
        putColor(EXCLUDED_FILE);
        putColor(MODIFIED_LOCALLY_FILE);
        putColor(NEW_LOCALLY_FILE);
        putColor(REMOVED_LOCALLY_FILE);
        putColor(TEXT_ANNOTATION);
    }
}
