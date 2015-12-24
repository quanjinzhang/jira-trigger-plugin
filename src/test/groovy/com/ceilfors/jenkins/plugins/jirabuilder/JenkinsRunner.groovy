package com.ceilfors.jenkins.plugins.jirabuilder

import hudson.model.AbstractBuild
import hudson.model.AbstractProject
import hudson.model.FreeStyleProject
import hudson.model.ParametersAction
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition
import org.jvnet.hudson.test.JenkinsRule

import java.util.concurrent.TimeUnit

import static org.junit.Assert.assertThat
import static org.hamcrest.Matchers.*

/**
 * @author ceilfors
 */
class JenkinsRunner extends JenkinsRule {

    AbstractBuild buildShouldBeScheduled(String jobName) {
        def build = jiraBuilderAction.getLastScheduledBuild(15, TimeUnit.SECONDS)
        assertThat("Last scheduled build should be for the job matched", build.project.name, is(jobName))
        return build
    }

    private JiraWebHook getJiraBuilderAction() {
        instance.getActions().find { it instanceof JiraWebHook } as JiraWebHook
    }

    String getWebHookUrl() {
        return "${getURL().toString()}${jiraBuilderAction.urlName}/"
                .replace("localhost", "10.0.2.2") // vagrant
    }

    FreeStyleProject createJiraTriggeredProject(String name, String... parameters) {
        FreeStyleProject project = createFreeStyleProject(name)
        project.addProperty(new ParametersDefinitionProperty(parameters.collect {new StringParameterDefinition(it, "")}))
        project.addTrigger(new JiraBuilderTrigger())
        return project
    }

    boolean buildTriggeredWithParameter(String jobName, Map<String, String> parameterMap) {
        def parametersAction = instance.getItemByFullName(jobName, AbstractProject).lastSuccessfulBuild.getAction(ParametersAction)
        parameterMap.each { key, value ->
            assertThat(parametersAction.getParameter(key).value as String, is(value))
        }
        return true
    }
}
