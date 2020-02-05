/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote.client.cli.commands;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.cli.SvnCommand;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;

/**
 *
 * 
 */
public class CatCommand extends SvnCommand {

    private enum CatType {
        url,
        file,
    }
    
    private final CatType type;
    
    private final SVNUrl url;
    private final VCSFileProxy file;    
    private final SVNRevision rev;
    private final SVNRevision pegRevision;
    
    private byte[] bytes;
    
    public CatCommand(FileSystem fileSystem, SVNUrl url, SVNRevision rev, SVNRevision pegRevision) {
        super(fileSystem);
        this.url = url;                
        this.rev = rev;        
        this.file = null;
        this.pegRevision = pegRevision;
        type = CatType.url;

    }
    
    public CatCommand(FileSystem fileSystem, VCSFileProxy file, SVNRevision rev) {
        super(fileSystem);
        this.file = file;
        this.rev = rev;        
        this.url = null;
        this.pegRevision = null;
        type = CatType.file;
    }

    public InputStream getOutput() {
        return new ByteArrayInputStream(bytes == null ? new byte[] {} : bytes);
    }
    
    @Override
    protected boolean hasBinaryOutput() {
        return true;
    }

    @Override
    protected boolean notifyOutput() {
        return false;
    }    
    
    @Override
    public void output(byte[] bytes) {
        this.bytes = bytes;
    }
    
    @Override
    protected ISVNNotifyListener.Command getCommand() {
        return ISVNNotifyListener.Command.CAT;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("cat"); //NOI18N
        switch(type) {
            case url: 
                if (pegRevision == null) {
                    arguments.add(url);
                } else {
                    arguments.add(url, pegRevision);
                }
                break;
            case file:     
                arguments.add(file);
                setCommandWorkingDirectory(file);
                break;
            default :    
                throw new IllegalStateException("Illegal cattype: " + type); //NOI18N
        }                
        arguments.add(rev);
    }    
}
