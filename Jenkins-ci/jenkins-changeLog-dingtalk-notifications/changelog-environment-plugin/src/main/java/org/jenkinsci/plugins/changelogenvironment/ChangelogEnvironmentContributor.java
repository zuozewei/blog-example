package org.jenkinsci.plugins.changelogenvironment;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildWrapper;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.List;

public class ChangelogEnvironmentContributor extends SimpleBuildWrapper {

    private String entryFormat;

    private String lineFormat;

    private String dateFormat;

    @DataBoundConstructor
    public ChangelogEnvironmentContributor() {
        // need empty constructor so Stapler creates instances
    }

    @DataBoundSetter
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @DataBoundSetter
    public void setEntryFormat(String entryFormat) {
        this.entryFormat = entryFormat;
    }

    @DataBoundSetter
    public void setLineFormat(String lineFormat) {
        this.lineFormat = lineFormat;
    }

    public String getEntryFormat() {
        return this.entryFormat;
    }

    public String getLineFormat() {
        return this.lineFormat;
    }

    public String getDateFormat() {
        return this.dateFormat;
    }

    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();

        DateFormat df;
        try {
            df = new SimpleDateFormat(Util.fixNull(dateFormat));
        } catch (IllegalArgumentException ex) {
            listener.error("Failed to insert changelog into the environment: Illegal date format");
            return;
        }

        try {
            if (build instanceof AbstractBuild<?, ?>) {
                AbstractBuild<?, ?> abstractBuild = (AbstractBuild<?, ?>) build;
                ChangeLogSet cs = abstractBuild.getChangeSet();
                processChangeLogSet(sb, cs, df);
            }

            try {
                // FIXME TODO I have no idea whether this works, untested
                if (build instanceof WorkflowRun) {
                    WorkflowRun wfr = (WorkflowRun) build;
                    List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeLogSets = wfr.getChangeSets();
                    for (ChangeLogSet<? extends ChangeLogSet.Entry> changeLogSet : changeLogSets) {
                        processChangeLogSet(sb, changeLogSet, df);
                    }
                }
            } catch (NoClassDefFoundError ncder) {
                // ignore
            }

        } catch (IllegalFormatException ex) {
            listener.error("Failed to insert changelog into the environment: " + ex.getMessage());
            return;
        }

        String value = sb.toString();
        if (!"".equals(value)) {
            context.env("SCM_CHANGELOG", value);
        }
    }

    private void processChangeLogSet(StringBuilder sb, ChangeLogSet cs, DateFormat df) {
        for (Object o : cs) {
            ChangeLogSet.Entry e = (ChangeLogSet.Entry) o;
            sb.append(String.format(Util.fixNull(this.entryFormat), e.getAuthor(), e.getCommitId(), e.getMsg(), df.format(new Date(e.getTimestamp()))));

            try {
                for (ChangeLogSet.AffectedFile file : e.getAffectedFiles()) {
                    sb.append(String.format(Util.fixNull(this.lineFormat), file.getEditType().getName(), file.getPath()));
                }
            } catch (UnsupportedOperationException ex) {
                // early versions of SCM did not support getAffectedFiles, only had getAffectedPaths
                for (String file : e.getAffectedPaths()) {
                    sb.append(String.format(Util.fixNull(this.lineFormat), "", file));
                }
            }
        }
    }

    @Extension
    public static class ChangelogEnvironmentContributorDescriptor extends BuildWrapperDescriptor {

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            // only really makes sense for jobs with SCM, but cannot really not show this option otherwise
            // users would have to leave the config form between setting up SCM and this.
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.DisplayName();
        }

        public FormValidation doCheckLineFormat(@QueryParameter String lineFormat) {
            try {
                String result = String.format(lineFormat, "add", "README.md");
                return FormValidation.ok(Messages.LineFormat_Sample(result));
            } catch (IllegalFormatException ex) {
                return FormValidation.error(Messages.LineFormat_Error());
            }
        }

        public FormValidation doCheckEntryFormat(@QueryParameter String entryFormat) {
            try {
                String result = String.format(entryFormat, "danielbeck", "879e6fa97d79fd", "Initial commit", 1448305200000L);
                return FormValidation.ok(Messages.EntryFormat_Sample(result));
            } catch (IllegalFormatException ex) {
                return FormValidation.error(Messages.EntryFormat_Error());
            }
        }

        public FormValidation doCheckDateFormat(@QueryParameter String dateFormat) {
            try {
                String result = new SimpleDateFormat(dateFormat).format(new Date(1448305200000L));
                return FormValidation.ok(Messages.DateFormat_Sample(result));
            } catch (IllegalArgumentException ex) {
                return FormValidation.error(Messages.DateFormat_Error());
            }
        }
    }
}
