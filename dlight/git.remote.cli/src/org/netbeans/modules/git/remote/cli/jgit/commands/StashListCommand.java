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
package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.git.remote.cli.progress.RevisionInfoListener;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class StashListCommand extends GitCommand {

    private final List<GitRevisionInfo> revisions;
    private final RevisionInfoListener listener;
    private final ProgressMonitor monitor;
    
    public StashListCommand (JGitRepository repository, GitClassFactory accessor,
            ProgressMonitor monitor, RevisionInfoListener listener) {
        super(repository, accessor, monitor);
        revisions = new ArrayList<>();
        this.monitor = monitor;
        this.listener = listener;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "stash"); //NOI18N
        addArgument(0, "list"); //NOI18N
        addArgument(0, "--raw"); //NOI18N
        addArgument(0, "--pretty=raw"); //NOI18N
    }

    public GitRevisionInfo[] getRevisions () {
        return revisions.toArray(new GitRevisionInfo[revisions.size()]);
    }

    private void addRevision (GitRevisionInfo info) {
        revisions.add(info);
        listener.notifyRevisionInfo(info);
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final LinkedHashMap<String, GitRevisionInfo.GitRevCommit> statuses = new LinkedHashMap<String, GitRevisionInfo.GitRevCommit>();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    LogCommand.parseLog(output, statuses);
                }
            }.runCLI();
            for(Map.Entry<String, GitRevisionInfo.GitRevCommit> entry : statuses.entrySet()) {
                addRevision(getClassFactory().createRevisionInfo(entry.getValue(), getRepository()));
            }
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if(canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }        
    }
    
}
