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
package org.netbeans.modules.git.remote.client;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitBlameResult;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitCherryPickResult;
import org.netbeans.modules.git.remote.cli.GitClient.CherryPickOperation;
import org.netbeans.modules.git.remote.cli.GitClient.DiffMode;
import org.netbeans.modules.git.remote.cli.GitClient.RebaseOperationType;
import org.netbeans.modules.git.remote.cli.GitClient.ResetType;
import org.netbeans.modules.git.remote.cli.GitClientCallback;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitMergeResult;
import org.netbeans.modules.git.remote.cli.GitPullResult;
import org.netbeans.modules.git.remote.cli.GitPushResult;
import org.netbeans.modules.git.remote.cli.GitRebaseResult;
import org.netbeans.modules.git.remote.cli.GitRefUpdateResult;
import org.netbeans.modules.git.remote.cli.GitRemoteConfig;
import org.netbeans.modules.git.remote.cli.GitRepository;
import org.netbeans.modules.git.remote.cli.GitRepository.FastForwardOption;
import org.netbeans.modules.git.remote.cli.GitRepositoryState;
import org.netbeans.modules.git.remote.cli.GitRevertResult;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo.GitFileInfo;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.GitSubmoduleStatus;
import org.netbeans.modules.git.remote.cli.GitTag;
import org.netbeans.modules.git.remote.cli.GitTransportUpdate;
import org.netbeans.modules.git.remote.cli.GitUser;
import org.netbeans.modules.git.remote.cli.SearchCriteria;
import org.netbeans.modules.git.remote.cli.progress.NotificationListener;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NetworkSettings;
import org.openide.util.RequestProcessor;

/**
 *
 */
public final class GitClient {
    
    private final org.netbeans.modules.git.remote.cli.GitClient delegate;
    private final GitProgressSupport progressSupport;
    private final boolean handleAuthenticationIssues;
    private static final int CLEANUP_TIME = 15000;
    private static final List<org.netbeans.modules.git.remote.cli.GitClient> unusedClients = new LinkedList<>();
    private static final RequestProcessor.Task cleanTask = Git.getInstance().getRequestProcessor().create(new CleanTask());
    
    /**
     * Set of commands that do not need to run under repository lock
     */
    private static final HashSet<String> PARALLELIZABLE_COMMANDS = new HashSet<>(Arrays.asList("addNotificationListener", //NOI18N
            "blame", //NOI18N
            "catFile",  //NOI18N
            "catIndexEntry",  //NOI18N
            "exportCommit", //NOI18N
            "exportDiff", //NOI18N
            "getBranches",  //NOI18N
            "getCommonAncestor", //NOI18N
            "getConflicts", //NOI18N
            "getPreviousRevision", //NOI18N
            "getStatus",  //NOI18N
            "getTags", //NOI18N
            "getRemote", //NOI18N
            "getRemotes", //NOI18N
            "getRepositoryState",  //NOI18N
            "getSubmoduleStatus",  //NOI18N
            "getUser",  //NOI18N
            "listModifiedIndexEntries", //NOI18N
            "listRemoteBranches", //NOI18N
            "listRemoteTags", //NOI18N
            "log", //NOI18N
            "removeNotificationListener", //NOI18N
            "removeRemote", //NOI18N - i guess there's no need to mke this an exclusive command
            "setCallback", //NOI18N
            "setRemote", //NOI18N - i guess there's no need to mke this an exclusive command
            "stashList")); //NOI18N
    /**
     * Commands triggering last cached timestamp of the index file. This means that after every command that somehow modifies the index, we need to refresh the timestamp
     * otherwise a FS event will come to Interceptor and trigger the full scan.
     */
    private static final HashSet<String> WORKING_TREE_READ_ONLY_COMMANDS = new HashSet<>(Arrays.asList("addNotificationListener",  //NOI18N
            "blame", //NOI18N
            "catFile",  //NOI18N
            "catIndexEntry",  //NOI18N
            "createBranch", //NOI18N - does not update index or files in WT
            "createTag", //NOI18N - does not update index or files in WT
            "deleteBranch", //NOI18N - does not update index or files in WT
            "deleteTag", //NOI18N - does not update index or files in WT
            "fetch", //NOI18N - updates only metadata
            "exportCommit", //NOI18N
            "exportDiff", //NOI18N
            "getBranches",  //NOI18N
            "getCommonAncestor", //NOI18N
            "getConflicts", //NOI18N
            "getPreviousRevision", //NOI18N
            "getStatus",  //NOI18N
            "getRemote", //NOI18N
            "getRemotes", //NOI18N
            "getRepositoryState",  //NOI18N
            "getSubmoduleStatus",  //NOI18N
            "getTags", //NOI18N
            "getUser",  //NOI18N
            "ignore",  //NOI18N
            "listModifiedIndexEntries", //NOI18N
            "listRemoteBranches", //NOI18N
            "listRemoteTags", //NOI18N
            "log", //NOI18N
            "unignore", //NOI18N
            "updateReference", //NOI18N
            "push", //NOI18N - does not manipulate with index
            "removeNotificationListener", //NOI18N
            "removeRemote", //NOI18N - does not update index or files in WT
            "setCallback", //NOI18N
            "setRemote", //NOI18N - does not update index or files in WT
            "stashDrop", //NOI18N
            "stashDropAll", //NOI18N
            "stashList")); //NOI18N
    /**
     * Commands that will trigger repository information refresh, i.e. those that change HEAD, current branch, etc.
     */
    private static final HashSet<String> NEED_REPOSITORY_REFRESH_COMMANDS = new HashSet<>(Arrays.asList("add",//NOI18N // may change state, e.g. MERGING->MERGED
            "checkout", //NOI18N
            "checkoutRevision", //NOI18N // current head changes
            "cherryPick", //NOI18N
            "commit", //NOI18N
            "createBranch", //NOI18N // should refresh set of known branches
            "createTag", //NOI18N - should refresh set of available tags
            "deleteBranch", //NOI18N - should refresh set of available branches
            "deleteTag", //NOI18N - should refresh set of available tags
            "fetch", //NOI18N - changes available remote heads or tags
            "merge", //NOI18N // creates a new head
            "pull", //NOI18N // creates a new head
            "push", //NOI18N // usually updates remote branches
            "rebase", //NOI18N // creates new head and branches
            "remove", //NOI18N // may change state, e.g. MERGING->MERGED
            "reset", //NOI18N
            "removeRemote", //NOI18N - updates remotes
            "revert", //NOI18N - creates a new head
            "setRemote", //NOI18N - updates remotes
            "setUpstreamBranch", //NOI18N - updates remotes
            "updateReference", //NOI18N - updates branches
            "updateSubmodules" //NOI18N - current head changes
    ));
    /**
     * Commands accessing a remote repository. For these NbAuthenticator must be switched off
     */
    private static final HashSet<String> NETWORK_COMMANDS = new HashSet<>(Arrays.asList(
            "fetch", //NOI18N
            "listRemoteBranches", //NOI18N
            "listRemoteTags", //NOI18N
            "pull", //NOI18N
            "push", //NOI18N
            "updateSubmodules" //NOI18N
            ));
    private static final Logger LOG = Logger.getLogger(GitClient.class.getName());
    private final VCSFileProxy repositoryRoot;
    private boolean released;

    public GitClient (VCSFileProxy repository, GitProgressSupport progressSupport, boolean handleAuthenticationIssues) throws GitException {
        this.repositoryRoot = repository;
        delegate = GitRepository.getInstance(repository).createClient();
        this.progressSupport = progressSupport;
        this.handleAuthenticationIssues = handleAuthenticationIssues;
    }
    
    public void add (final VCSFileProxy[] roots, final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.add(roots, monitor);
                return null;
            }
        }, "add", roots); //NOI18N
    }

    public void addNotificationListener (NotificationListener listener) {
        delegate.addNotificationListener(listener);
    }

    public GitBlameResult blame (final VCSFileProxy file, final String revision, final ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        return new CommandInvoker().runMethod(new Callable<GitBlameResult>() {

            @Override
            public GitBlameResult call () throws Exception {
                return delegate.blame(file, revision, monitor);
            }
        }, "blame"); //NOI18N
    }

    public boolean catFile (final VCSFileProxy file, final String revision, final java.io.OutputStream out, final ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        return new CommandInvoker().runMethod(new Callable<Boolean>() {

            @Override
            public Boolean call () throws Exception {
                return delegate.catFile(file, revision, out, monitor);
            }
        }, "catFile"); //NOI18N
    }

    public boolean catIndexEntry (final VCSFileProxy file, final int stage, final java.io.OutputStream out, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<Boolean>() {

            @Override
            public Boolean call () throws Exception {
                return delegate.catIndexEntry(file, stage, out, monitor);
            }
        }, "catIndexEntry"); //NOI18N
    }

    public void checkout (final VCSFileProxy[] roots, final String revision, final boolean recursively, final ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.checkout(roots, revision, recursively, monitor);
                return null;
            }
        }, "checkout", roots); //NOI18N
    }

    public void checkoutRevision (final String revision, final boolean failOnConflict, final ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.checkoutRevision(revision, failOnConflict, monitor);
                return null;
            }
        }, "checkoutRevision", new VCSFileProxy[] { repositoryRoot }); //NOI18N
    }

    public GitCherryPickResult cherryPick (final CherryPickOperation op, final String[] revisions, final ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        return new CommandInvoker().runMethod(new Callable<GitCherryPickResult>() {

            @Override
            public GitCherryPickResult call () throws Exception {
                return delegate.cherryPick(op, revisions, monitor);
            }
        }, "cherryPick", new VCSFileProxy[] { repositoryRoot }); //NOI18N
    }

    public void clean(final VCSFileProxy[] roots, final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.clean(roots, monitor);
                return null;
            }
        }, "clean", roots); //NOI18N
    }
    
    public GitRevisionInfo commit (final VCSFileProxy[] roots, final String commitMessage, final GitUser author, final GitUser commiter, final ProgressMonitor monitor) throws GitException {
        return commit(roots, commitMessage, author, commiter, false, monitor);
    }
    
    public GitRevisionInfo commit (final VCSFileProxy[] roots, final String commitMessage, final GitUser author, final GitUser commiter, final boolean amend, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<GitRevisionInfo>() {

            @Override
            public GitRevisionInfo call () throws Exception {
                return delegate.commit(roots, commitMessage, author, commiter, amend, monitor);
            }
        }, "commit", roots); //NOI18N
    }

    public void copyAfter (final VCSFileProxy source, final VCSFileProxy target, final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.copyAfter(source, target, monitor);
                return null;
            }
        }, "copyAfter", new VCSFileProxy[] { target }); //NOI18N
    }

    public GitBranch createBranch (final String branchName, final String revision, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<GitBranch>() {

            @Override
            public GitBranch call () throws Exception {
                return delegate.createBranch(branchName, revision, monitor);
            }
        }, "createBranch"); //NOI18N
    }

    public GitTag createTag (final String tagName, final String taggedObject, final String message, final boolean signed, final boolean forceUpdate, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<GitTag>() {

            @Override
            public GitTag call () throws Exception {
                return delegate.createTag(tagName, taggedObject, message, signed, forceUpdate, monitor);
            }
        }, "createTag"); //NOI18N
    }

    public void deleteBranch (final String branchName, final boolean forceDeleteUnmerged, final ProgressMonitor monitor) throws GitException.NotMergedException, GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.deleteBranch(branchName, forceDeleteUnmerged, monitor);
                return null;
            }
        }, "deleteBranch"); //NOI18N
    }

    public void deleteTag (final String tagName, final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.deleteTag(tagName, monitor);
                return null;
            }
        }, "deleteTag"); //NOI18N
    }

    public void exportCommit (final String commit, final OutputStream out, final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.exportCommit(commit, out, monitor);
                return null;
            }
        }, "exportCommit"); //NOI18N
    }
    
    public void exportDiff (final VCSFileProxy[] roots, final DiffMode mode, final OutputStream out, final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.exportDiff(roots, mode, out, monitor);
                return null;
            }
        }, "exportDiff", roots); //NOI18N
    }
    
    public void exportDiff (final VCSFileProxy[] roots, final String base, final String other, final OutputStream out, final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.exportDiff(roots, base, other, out, monitor);
                return null;
            }
        }, "exportDiff", roots); //NOI18N
    }
    
    public Map<String, GitTransportUpdate> fetch (final String remote, final ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        return new CommandInvoker().runMethod(new Callable<Map<String, GitTransportUpdate>>() {

            @Override
            public Map<String, GitTransportUpdate> call () throws Exception {
                return delegate.fetch(remote, monitor);
            }
        }, "fetch"); //NOI18N
    }
    
    public Map<String, GitTransportUpdate> fetch (final String remote, final List<String> fetchRefSpecifications, final ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        return new CommandInvoker().runMethod(new Callable<Map<String, GitTransportUpdate>>() {

            @Override
            public Map<String, GitTransportUpdate> call () throws Exception {
                return delegate.fetch(remote, fetchRefSpecifications, monitor);
            }
        }, "fetch"); //NOI18N
    }
    
    public Map<String, GitBranch> getBranches (final boolean all, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<Map<String, GitBranch>>() {

            @Override
            public Map<String, GitBranch> call () throws Exception {
                return delegate.getBranches(all, monitor);
            }
        }, "getBranches"); //NOI18N
    }

    public Map<VCSFileProxy, GitSubmoduleStatus> getSubmoduleStatus (final VCSFileProxy[] roots, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<Map<VCSFileProxy, GitSubmoduleStatus>>() {

            @Override
            public Map<VCSFileProxy, GitSubmoduleStatus> call () throws Exception {
                return delegate.getSubmoduleStatus(roots, monitor);
            }
        }, "getSubmoduleStatus"); //NOI18N
    }

    public Map<String, GitTag> getTags (final ProgressMonitor monitor, final boolean allTags) throws GitException {
        return new CommandInvoker().runMethod(new Callable<Map<String, GitTag>>() {

            @Override
            public Map<String, GitTag> call () throws Exception {
                return delegate.getTags(monitor, allTags);
            }
        }, "getTags"); //NOI18N
    }

    public GitRevisionInfo getCommonAncestor (final String[] revisions, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<GitRevisionInfo>() {

            @Override
            public GitRevisionInfo call () throws Exception {
                return delegate.getCommonAncestor(revisions, monitor);
            }
        }, "getCommonAncestor"); //NOI18N
    }

    public GitRevisionInfo getPreviousRevision (final VCSFileProxy file, final String revision, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<GitRevisionInfo>() {

            @Override
            public GitRevisionInfo call () throws Exception {
                return delegate.getPreviousRevision(file, revision, monitor);
            }
        }, "getPreviousRevision"); //NOI18N
    }

    public Map<VCSFileProxy, GitStatus> getConflicts (final VCSFileProxy[] roots, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<Map<VCSFileProxy, GitStatus>>() {

            @Override
            public Map<VCSFileProxy, GitStatus> call () throws Exception {
                return delegate.getConflicts(roots, monitor);
            }
        }, "getConflicts", roots); //NOI18N
    }

    public Map<VCSFileProxy, GitStatus> getStatus (final VCSFileProxy[] roots, final ProgressMonitor monitor) throws GitException {
        return getStatus(roots, GitUtils.HEAD, monitor);
    }

    public Map<VCSFileProxy, GitStatus> getStatus (final VCSFileProxy[] roots, final String revision, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<Map<VCSFileProxy, GitStatus>>() {

            @Override
            public Map<VCSFileProxy, GitStatus> call () throws Exception {
                return delegate.getStatus(roots, revision, monitor);
            }
        }, "getStatus", roots); //NOI18N
    }

    public Map<VCSFileProxy, GitFileInfo> getStatus (final VCSFileProxy[] roots, final String revisionLeft, final String revisionRight, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<Map<VCSFileProxy, GitFileInfo>>() {

            @Override
            public Map<VCSFileProxy, GitFileInfo> call () throws Exception {
                return delegate.getStatus(roots, revisionLeft, revisionRight, monitor);
            }
        }, "getStatus", roots); //NOI18N
    }

    public GitRemoteConfig getRemote (final String remoteName, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<GitRemoteConfig>() {

            @Override
            public GitRemoteConfig call () throws Exception {
                return delegate.getRemote(remoteName, monitor);
            }
        }, "getRemote"); //NOI18N
    }

    public Map<String, GitRemoteConfig> getRemotes (final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<Map<String, GitRemoteConfig>>() {

            @Override
            public Map<String, GitRemoteConfig> call () throws Exception {
                return delegate.getRemotes(monitor);
            }
        }, "getRemotes"); //NOI18N
    }

    public VCSFileProxy getRepositoryRoot () {
        return repositoryRoot;
    }
    
    public GitRepositoryState getRepositoryState (final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<GitRepositoryState>() {

            @Override
            public GitRepositoryState call () throws Exception {
                return delegate.getRepositoryState(monitor);
            }
        }, "getRepositoryState"); //NOI18N
    }
    
    public GitUser getUser() throws GitException {        
        return new CommandInvoker().runMethod(new Callable<GitUser>() {

            @Override
            public GitUser call () throws Exception {
                return delegate.getUser();
            }
        }, "getUser"); //NOI18N
    }

    public VCSFileProxy[] ignore (final VCSFileProxy[] files, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<VCSFileProxy[]>() {

            @Override
            public VCSFileProxy[] call () throws Exception {
                return delegate.ignore(files, monitor);
            }
        }, "ignore", files); //NOI18N
    }

    public void init (final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.init(monitor);
                return null;
            }
        }, "init"); //NOI18N
    }

    public Map<VCSFileProxy, GitSubmoduleStatus> initializeSubmodules (final VCSFileProxy[] roots, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<Map<VCSFileProxy, GitSubmoduleStatus>>() {

            @Override
            public Map<VCSFileProxy, GitSubmoduleStatus> call () throws Exception {
                return delegate.initializeSubmodules(roots, monitor);
            }
        }, "initializeSubmodules"); //NOI18N
    }

    public VCSFileProxy[] listModifiedIndexEntries (final VCSFileProxy[] roots, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<VCSFileProxy[]>() {

            @Override
            public VCSFileProxy[] call () throws Exception {
                return delegate.listModifiedIndexEntries(roots, monitor);
            }
        }, "listModifiedIndexEntries", roots); //NOI18N
    }
    
    public Map<String, GitBranch> listRemoteBranches (final String remoteRepositoryUrl, final ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        return new CommandInvoker().runMethod(new Callable<Map<String, GitBranch>>() {

            @Override
            public Map<String, GitBranch> call () throws Exception {
                return delegate.listRemoteBranches(remoteRepositoryUrl, monitor);
            }
        }, "listRemoteBranches"); //NOI18N
    }
    
    public Map<String, String> listRemoteTags (final String remoteRepositoryUrl, final ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        return new CommandInvoker().runMethod(new Callable<Map<String, String>>() {

            @Override
            public Map<String, String> call () throws Exception {
                return delegate.listRemoteTags(remoteRepositoryUrl, monitor);
            }
        }, "listRemoteTags"); //NOI18N
    }

    public GitRevisionInfo log (final String revision, final ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        return new CommandInvoker().runMethod(new Callable<GitRevisionInfo>() {

            @Override
            public GitRevisionInfo call () throws Exception {
                return delegate.log(revision, monitor);
            }
        }, "log"); //NOI18N
    }

    public GitRevisionInfo[] log (final SearchCriteria searchCriteria, final boolean fetchAffectedBranches,
            final ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        return new CommandInvoker().runMethod(new Callable<GitRevisionInfo[]>() {

            @Override
            public GitRevisionInfo[] call () throws Exception {
                return delegate.log(searchCriteria, fetchAffectedBranches, monitor);
            }
        }, "log"); //NOI18N
    }
    
    public GitMergeResult merge (final String revision, final FastForwardOption ffOption, final ProgressMonitor monitor) throws GitException.CheckoutConflictException, GitException {
        return new CommandInvoker().runMethod(new Callable<GitMergeResult>() {

            @Override
            public GitMergeResult call () throws Exception {
                return delegate.merge(revision, ffOption, monitor);
            }
        }, "merge"); //NOI18N
    }
    
    public GitPullResult pull (final String remote, final List<String> fetchRefSpecifications, final String branchToMerge, final ProgressMonitor monitor) throws GitException.AuthorizationException, 
            GitException.CheckoutConflictException, GitException.MissingObjectException, GitException {
        return new CommandInvoker().runMethod(new Callable<GitPullResult>() {

            @Override
            public GitPullResult call () throws Exception {
                return delegate.pull(remote, fetchRefSpecifications, branchToMerge, monitor);
            }
        }, "pull"); //NOI18N
    }
    
    public GitPushResult push (final String remote, final List<String> pushRefSpecifications, final List<String> fetchRefSpecifications, final ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        return new CommandInvoker().runMethod(new Callable<GitPushResult>() {

            @Override
            public GitPushResult call () throws Exception {
                return delegate.push(remote, pushRefSpecifications, fetchRefSpecifications, monitor);
            }
        }, "push"); //NOI18N
    }
    
    public GitRebaseResult rebase (final RebaseOperationType operation, final String upstream, final ProgressMonitor monitor) throws GitException.AuthorizationException, GitException {
        return new CommandInvoker().runMethod(new Callable<GitRebaseResult>() {

            @Override
            public GitRebaseResult call () throws Exception {
                return delegate.rebase(operation, upstream, monitor);
            }
        }, "rebase"); //NOI18N
    }
    
    /**
     * Schedule cleanup of git repository used by this client
     */
    public void release () {
        synchronized (unusedClients) {
            if (released) {
                return;
            }
            unusedClients.add(delegate);
            released = true;
        }
        cleanTask.schedule(CLEANUP_TIME);
    }

    public void remove (final VCSFileProxy[] roots, final boolean cached, final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.remove(roots, cached, monitor);
                return null;
            }
        }, "remove", roots); //NOI18N
    }

    public void removeNotificationListener (NotificationListener listener) {
        delegate.removeNotificationListener(listener);
    }
    
    public void removeRemote (final String remote, final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.removeRemote(remote, monitor);
                return null;
            }
        }, "removeRemote"); //NOI18N
    }

    public void rename (final VCSFileProxy source, final VCSFileProxy target, final boolean after, final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.rename(source, target, after, monitor);
                return null;
            }
        }, "rename", new VCSFileProxy[] { source, target }); //NOI18N
    }
    
    public void reset (final VCSFileProxy[] roots, final String revision, final boolean recursively, final ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.reset(roots, revision, recursively, monitor);
                return null;
            }
        }, "reset", roots); //NOI18N
    }

    public void reset (final String revision, final ResetType resetType, final ProgressMonitor monitor) throws GitException.MissingObjectException, GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.reset(revision, resetType, monitor);
                return null;
            }
        }, "reset"); //NOI18N
    }

    public GitRevertResult revert (final String revision, final String commitMessage, final boolean commit, final ProgressMonitor monitor)
            throws GitException.MissingObjectException, GitException.CheckoutConflictException, GitException {
        return new CommandInvoker().runMethod(new Callable<GitRevertResult>() {

            @Override
            public GitRevertResult call () throws Exception {
                return delegate.revert(revision, commitMessage, commit, monitor);
            }
        }, "revert"); //NOI18N
    }

    public void setCallback (GitClientCallback callback) {
        delegate.setCallback(callback);
    }
    
    public void setRemote (final GitRemoteConfig remoteConfig, final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.setRemote(remoteConfig, monitor);
                return null;
            }
        }, "setRemote"); //NOI18N
    }
    
    public void stashApply (final int stashIndex, final boolean drop, final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.stashApply(stashIndex, drop, monitor);
                return null;
            }
        }, "stashApply"); //NOI18N
    }
    
    public void stashDrop (final int stashIndex, final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.stashDrop(stashIndex, monitor);
                return null;
            }
        }, "stashDrop"); //NOI18N
    }
    
    public void stashDropAll (final ProgressMonitor monitor) throws GitException {
        new CommandInvoker().runMethod(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                delegate.stashDropAll(monitor);
                return null;
            }
        }, "stashDropAll"); //NOI18N
    }
    
    public GitRevisionInfo[] stashList (final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<GitRevisionInfo[]>() {

            @Override
            public GitRevisionInfo[] call () throws Exception {
                return delegate.stashList(monitor);
            }
        }, "stashList"); //NOI18N
    }
    
    public GitRevisionInfo stashSave (final String message, final boolean includeUntracked, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<GitRevisionInfo>() {

            @Override
            public GitRevisionInfo call () throws Exception {
                return delegate.stashSave(message, includeUntracked, monitor);
            }
        }, "stashSave"); //NOI18N
    }

    public VCSFileProxy[] unignore (final VCSFileProxy[] files, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<VCSFileProxy[]>() {

            @Override
            public VCSFileProxy[] call () throws Exception {
                return delegate.unignore(files, monitor);
            }
        }, "unignore"); //NOI18N
    }

    public GitRefUpdateResult updateReference (final String referenceName, final String newId,
            final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<GitRefUpdateResult>() {

            @Override
            public GitRefUpdateResult call () throws Exception {
                return delegate.updateReference(referenceName, newId, monitor);
            }
        }, "updateReference"); //NOI18N
    }

    public Map<VCSFileProxy, GitSubmoduleStatus> updateSubmodules (final VCSFileProxy[] roots, final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<Map<VCSFileProxy, GitSubmoduleStatus>>() {

            @Override
            public Map<VCSFileProxy, GitSubmoduleStatus> call () throws Exception {
                return delegate.updateSubmodules(roots, monitor);
            }
        }, "updateSubmodules"); //NOI18N
    }

    public GitBranch updateTracking (final String localBranchName, final String remoteBranchName,
            final ProgressMonitor monitor) throws GitException {
        return new CommandInvoker().runMethod(new Callable<GitBranch>() {

            @Override
            public GitBranch call () throws Exception {
                return delegate.setUpstreamBranch(localBranchName, remoteBranchName, monitor);
            }
        }, "setUpstreamBranch"); //NOI18N
    }

    private static class CleanTask implements Runnable {

        @Override
        public void run () {
            Set<org.netbeans.modules.git.remote.cli.GitClient> toRelease;
            synchronized (unusedClients) {
                toRelease = new HashSet<>(unusedClients);
                unusedClients.clear();
            }
            for (org.netbeans.modules.git.remote.cli.GitClient unusuedClient : toRelease) {
                unusuedClient.release();
            }
        }
        
    }
    
    private final class CommandInvoker {
        
        private <T> T runMethod (Callable<T> callable, String methodName) throws GitException {
            if (released) {
                throw new IllegalStateException("Client already released.");
            }
            return runMethod(callable, methodName, new VCSFileProxy[0]);
        }

        private <T> T runMethod (Callable<T> callable, String methodName, VCSFileProxy[] roots) throws GitException {
            try {
                if (isExclusiveRepositoryAccess(methodName)) {
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.log(Level.FINER, "Running an exclusive command: {0} on {1}", new Object[] { methodName, repositoryRoot }); //NOI18N
                    }
                    if (progressSupport != null) {
                        progressSupport.setRepositoryStateBlocked(repositoryRoot, true);
                    }
                    synchronized (repositoryRoot) {
                        if (Thread.interrupted()) {
                            throw new InterruptedException();
                        }
                        if (progressSupport != null) {
                            LOG.log(Level.FINEST, "Repository unblocked: {0}", repositoryRoot); //NOI18N
                            progressSupport.setRepositoryStateBlocked(repositoryRoot, false);
                        }
                        return runMethodIntern(callable, methodName, roots);
                    }
                } else {
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.log(Level.FINER, "Running a parallelizable command: {0} on {1}", new Object[] { methodName, repositoryRoot.getPath() }); //NOI18N
                    }
                    return runMethodIntern(callable, methodName, roots);
                }
            } catch (InterruptedException ex) {
                throw new GitCanceledException(ex);
            } catch (GitException ex) {
                throw ex;
            } catch (Throwable ex) {
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                } else if (ex.getCause() != null) {
                    throw new GitException(ex.getCause());
                } else {
                    throw new GitException(ex);
                }
            }
        }
        
        private <T> T runMethodIntern (final Callable<T> toRun, final String methodName, final VCSFileProxy[] roots) throws Throwable {
            Utils.logVCSClientEvent("GIT", "JAVALIB"); //NOI18N
            boolean refreshIndexTimestamp = modifiesWorkingTree(methodName);
            Callable<T> callable = new Callable<T>() {
                @Override
                public T call() throws Exception {
                    Callable<T> callable = new Callable<T>() {
                        @Override
                        public T call() throws Exception {
                            boolean repositoryInfoRefreshNeeded = NEED_REPOSITORY_REFRESH_COMMANDS.contains(methodName);
                            long t = 0;
                            if (LOG.isLoggable(Level.FINE)) {
                                t = System.currentTimeMillis();
                                LOG.log(Level.FINE, "Starting a git command: [{0}] on repository [{1}]", new Object[] { methodName, repositoryRoot.getPath() }); //NOI18N
                            }
                            try {
                                if (withoutAuthenticator(methodName)) {
                                    return NetworkSettings.suppressAuthenticationDialog(toRun);
                                } else {
                                    return toRun.call();
                                }
                            } catch (Exception ex) {
                                if ((progressSupport == null || !progressSupport.isCanceled()) && new GitClientExceptionHandler(GitClient.this, handleAuthenticationIssues).handleException(ex)) {
                                    return this.call();
                                } else {
                                    throw ex;
                                }
                            } finally {
                                if (LOG.isLoggable(Level.FINE)) {
                                    LOG.log(Level.FINE, "Git command finished: [{0}] on repository [{1}], lasted {2} ms", new Object[] { methodName, repositoryRoot.getPath(), System.currentTimeMillis() - t}); //NOI18N
                                }
                                if (repositoryInfoRefreshNeeded) {
                                    if (LOG.isLoggable(Level.FINER)) {
                                        LOG.log(Level.FINER, "Refreshing repository info after: {0} on {1}", new Object[] { methodName, repositoryRoot.getPath() }); //NOI18N
                                    }
                                    RepositoryInfo.getInstance(repositoryRoot).refresh();
                                }
                            }
                        }
                    };
                    return callable.call();
//                    if (!Boolean.TRUE.equals(indexingBridgeDisabled.get()) && runsWithBlockedIndexing(methodName)) {
//                        LOG.log(Level.FINER, "Running command in indexing bridge: {0} on {1}", new Object[] { methodName, repositoryRoot.getAbsolutePath() }); //NOI18N
//                        return IndexingBridge.getInstance().runWithoutIndexing(callable, roots.length > 0 ? roots : new File[] { repositoryRoot });
//                    } else {
//                    }
                }
            };
            try {
                if (refreshIndexTimestamp) {
                    return Git.getInstance().runWithoutExternalEvents(repositoryRoot, methodName, callable);
                } else {
                    return callable.call();
                }
            } catch (InvocationTargetException ex) {
                if (ex.getCause() != null) {
                    throw ex.getCause();
                } else {
                    throw ex;
                }
            }
        }
        
    }

    private static boolean isExclusiveRepositoryAccess (String commandName) {
        return !PARALLELIZABLE_COMMANDS.contains(commandName);
    }

    private static boolean modifiesWorkingTree (String commandName) {
        return !WORKING_TREE_READ_ONLY_COMMANDS.contains(commandName);
    }

    private static boolean withoutAuthenticator (String commandName) {
        return NETWORK_COMMANDS.contains(commandName);
    }
}
