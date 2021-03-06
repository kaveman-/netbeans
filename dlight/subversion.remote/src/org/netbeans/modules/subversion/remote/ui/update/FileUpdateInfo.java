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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote.ui.update;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 *
 * 
 */
public class FileUpdateInfo {

    /**
     * A  Added
     * D  Deleted
     * U  Updated
     * C  Conflict
     * G  Merged
     */
    private static final String KNOWN_ACTIONS = "ADUCG "; //NOI18N
        
    public static final int ACTION_TYPE_FILE                 = 1;
    public static final int ACTION_TYPE_PROPERTY             = 2;
    
    public static final int ACTION_ADDED                     = 4;
    public static final int ACTION_DELETED                   = 8;
    public static final int ACTION_UPDATED                   = 16;
    public static final int ACTION_CONFLICTED                = 32;
    public static final int ACTION_MERGED                    = 64;
    public static final int ACTION_CONFLICTED_RESOLVED       = 128;
        
    public static final int ACTION_LOCK_BROKEN               = 256;
        
    private final VCSFileProxy file;    
    private final int action;
    
    private static final Pattern pattern = Pattern.compile("^([ADUCG ])([ADUCG ])([B ])( *)(.+)$"); //NOI18N

    FileUpdateInfo(VCSFileProxy file, int action) {
        this.file   = file;
        this.action = action;
    }

    public VCSFileProxy getFile() {
        return file;
    }
    
    public int getAction() {
        return action;
    }
    
    public static FileUpdateInfo[] createFromLogMsg(VCSFileProxy root, String log) {
        Matcher m = pattern.matcher(log);
        if(!m.matches()) {
            return null;
        }
                        
        String fileActionValue       = m.group(1);
        String propertyActionValue   = m.group(2);
        String broken                = m.group(3);
        String filePath              = m.group(5);
        if( KNOWN_ACTIONS.indexOf(fileActionValue)     < 0 || 
            KNOWN_ACTIONS.indexOf(propertyActionValue) < 0 ) {
            return null;
        }

        FileUpdateInfo[] fui = new FileUpdateInfo[2];
        int fileAction = parseAction(fileActionValue.charAt(0)) | (broken.equals("B") ? ACTION_LOCK_BROKEN : 0); //NOI18N
        int propertyAction = parseAction(propertyActionValue.charAt(0));
        final VCSFileProxy aFile = VCSFileProxySupport.getResource(root, filePath).normalizeFile();
        fui[0] = fileAction != 0 ? new FileUpdateInfo(aFile, fileAction | ACTION_TYPE_FILE) : null;
        fui[1] = propertyAction != 0 ? new FileUpdateInfo(aFile, propertyAction | ACTION_TYPE_PROPERTY) : null;
        return fui;
    }
    
    private static int parseAction(char actionChar) {
        switch(actionChar) {
            case 'A': return ACTION_ADDED;          
            case 'D': return ACTION_DELETED;
            case 'U': return ACTION_UPDATED;
            case 'C': return ACTION_CONFLICTED;
            case 'G': return ACTION_MERGED;
        }                
        return 0;
    }
}
