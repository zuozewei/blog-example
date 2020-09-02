package org.jenkinsci.plugins.changelogenvironment;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.FakeChangeLogSCM;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.Exception;

public class ChangelogEnvironmentContributorTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testValidation() throws Exception {
        ChangelogEnvironmentContributor.ChangelogEnvironmentContributorDescriptor d =
                new ChangelogEnvironmentContributor.ChangelogEnvironmentContributorDescriptor();

        assertSame(d.doCheckDateFormat("").kind, FormValidation.Kind.OK);
        assertSame(d.doCheckDateFormat("lolno").kind, FormValidation.Kind.ERROR);
        assertSame(d.doCheckDateFormat("yyyy-MM-dd 'at' HH:mm:ss z").kind, FormValidation.Kind.OK);
    }

    @Test
    public void testBuildWithoutEnvironment() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();
        p.getBuildWrappersList().add(new ChangelogEnvironmentContributor());
        p.getBuildersList().add(new EnvironmentCheckBuilder(null));
        j.buildAndAssertSuccess(p);
    }

    @Test
    public void testBuildWithEnvironment() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();

        FakeChangeLogSCM scm = new FakeChangeLogSCM();
        p.setScm(scm);
        j.buildAndAssertSuccess(p);

        scm.addChange().withAuthor("danielbeck").withMsg("initial commit");

        ChangelogEnvironmentContributor c = new ChangelogEnvironmentContributor();
        c.setEntryFormat("%s | %3$s"); // format: author | message
        p.getBuildWrappersList().add(c);
        p.getBuildersList().add(new EnvironmentCheckBuilder("danielbeck | initial commit"));

        j.buildAndAssertSuccess(p);
    }

    public static class EnvironmentCheckBuilder extends Builder implements SimpleBuildStep {

        private final String expected;

        public EnvironmentCheckBuilder(String expected) {
            this.expected = expected;
        }

        @Override
        public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
            if (expected == null) {
                assertNull("Not expecting SCM_CHANGELOG in environment", run.getEnvironment(listener).get("SCM_CHANGELOG"));
            } else {
                assertEquals("Expecting SCM_CHANGELOG to contain string", expected, run.getEnvironment(listener).get("SCM_CHANGELOG"));
            }
        }
    }
}