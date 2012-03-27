/**
 * Copyright 2012 Shutterfly, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.test;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.test.launcher.LaunchFlashPlayerException;

import java.io.File;
import java.util.List;

/**
 * Goal to run integration tests for Flex applications, hosted in a web browser
 * 
 * @author Stephen Kuenzli (skuenzli@shutterfly.com)
 * @since 3.9.2
 * @goal integration-test
 * @requiresDependencyResolution
 * @phase integration-test
 */
public class FlexIntegrationMojo
    extends FlexTestMojo
{

    /**
     * Can be of type <code>&lt;argument&gt;</code>
     *
     * @parameter expression="${browser.command}"
     */
    private String browserCommand = "firefox";

    /**
     * Can be of type <code>&lt;argument&gt;</code>
     *
     * @parameter expression="${browser.command}"
     */
    private String testTargetURL = null;

    /**
     * The test command (e.g. web browser) exits when the test completes.
     *
     * @parameter default-value="false" expression="${testCommandExitsWhenTestCompletes}"
     */
    protected boolean testCommandExitsWhenTestCompletes = false;

    /**
     * When true, allow flexmojos to launch xvfb-run to run test if it detects headless linux env
     *
     * @parameter default-value="true" expression="${allowHeadlessMode}"
     */
    private boolean allowHeadlessMode;

    /**
     * Timeout for the first connection on ping Thread. That means how much time flexmojos will wait for Flashplayer be
     * loaded at first time.
     *
     * @parameter default-value="20000" expression="${firstConnectionTimeout}"
     */
    private int firstConnectionTimeout;

    /**
     * @component role="org.sonatype.flexmojos.test.TestRunner"
     */
    private TestRunner testRunner;

    /**
     * Create a server socket for receiving the test reports from FlexUnit. We read the test reports inside of a Thread.
     */

    @Override
    protected void run()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info("Running " + getClass().getSimpleName());

        if(!isIntegrationTestConfigured()) {
          getLog().info("Skipping integration test; not configured");
          return;
        }

        try
        {
            TestRequest testRequest = new TestRequest();
            testRequest.setTestControlPort( testControlPort );
            testRequest.setTestPort(testPort);
            testRequest.setFileUnderTest(new File(getTestTargetURL()));
            testRequest.setAllowHeadlessMode( allowHeadlessMode );
            testRequest.setTestCommand(getBrowserCommand());
            testRequest.setTestCommandExitsWhenTestCompletes(isTestCommandExitsWhenTestCompletes());
            testRequest.setTestTimeout( testTimeout );
            testRequest.setFirstConnectionTimeout( firstConnectionTimeout );

            List<String> results = testRunner.run( testRequest );
            for ( String result : results )
            {
                writeTestReport( result );
            }
        }
        catch ( TestRunnerException e )
        {
            executionError = e;
        }
        catch ( LaunchFlashPlayerException e )
        {
            throw new MojoExecutionException(
                "Failed to launch browser.  Java probably could not find (" +
                    browserCommand + ")." +
                    "\n\t\tMake sure browser is available on PATH" +
                    "\n\t\tor use -Dbrowser.command=${browser executable}",
                e);
        }
    }

    protected boolean isIntegrationTestConfigured() {
        getLog().debug("browserCommand: " + browserCommand);
        getLog().debug("testTargetURL: " + testTargetURL + " null? " + (testTargetURL == null));

        return StringUtils.trimToNull(browserCommand) != null
            && StringUtils.trimToNull(testTargetURL) != null;
    }

    public boolean isTestCommandExitsWhenTestCompletes() {
      return testCommandExitsWhenTestCompletes;
    }

    public String getBrowserCommand() {
      return browserCommand;
    }

    public void setBrowserCommand(String browserCommand) {
      this.browserCommand = browserCommand;
    }

    public String getTestTargetURL() {
      return testTargetURL;
    }

    public void setTestTargetURL(String testTargetURL) {
      this.testTargetURL = testTargetURL;
    }
}