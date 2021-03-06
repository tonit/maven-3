package org.apache.maven.model.validation;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.InputStream;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.SimpleProblemCollector;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultModelValidatorTest
    extends PlexusTestCase
{

    private DefaultModelValidator validator;

    private Model read( String pom )
        throws Exception
    {
        String resource = "/poms/validation/" + pom;
        InputStream is = getClass().getResourceAsStream( resource );
        assertNotNull( "missing resource: " + resource, is );
        return new MavenXpp3Reader().read( is );
    }

    private SimpleProblemCollector validate( String pom )
        throws Exception
    {
        return validateEffective( pom, ModelBuildingRequest.VALIDATION_LEVEL_STRICT );
    }

    private SimpleProblemCollector validateRaw( String pom )
        throws Exception
    {
        return validateRaw( pom, ModelBuildingRequest.VALIDATION_LEVEL_STRICT );
    }

    private SimpleProblemCollector validateEffective( String pom, int level )
        throws Exception
    {
        ModelBuildingRequest request = new DefaultModelBuildingRequest().setValidationLevel( level );

        SimpleProblemCollector problems = new SimpleProblemCollector();

        validator.validateEffectiveModel( read( pom ), request, problems );

        return problems;
    }

    private SimpleProblemCollector validateRaw( String pom, int level )
        throws Exception
    {
        ModelBuildingRequest request = new DefaultModelBuildingRequest().setValidationLevel( level );

        SimpleProblemCollector problems = new SimpleProblemCollector();

        validator.validateRawModel( read( pom ), request, problems );

        return problems;
    }

    private void assertContains( String msg, String substring )
    {
        assertTrue( "\"" + substring + "\" was not found in: " + msg, msg.contains( substring ) );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        validator = (DefaultModelValidator) lookup( ModelValidator.class );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        this.validator = null;

        super.tearDown();
    }

    private void assertViolations( SimpleProblemCollector result, int fatals, int errors, int warnings )
    {
        assertEquals( fatals, result.getFatals().size() );
        assertEquals( errors, result.getErrors().size() );
        assertEquals( warnings, result.getWarnings().size() );
    }

    public void testMissingModelVersion()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-modelVersion-pom.xml" );

        assertViolations( result, 0, 1, 0 );

        assertEquals( "'modelVersion' is missing.", result.getErrors().get( 0 ) );
    }

    public void testBadModelVersion()
        throws Exception
    {
        SimpleProblemCollector result =
            validateRaw( "bad-modelVersion.xml", ModelBuildingRequest.VALIDATION_LEVEL_STRICT );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).indexOf( "modelVersion" ) > -1 );
    }

    public void testMissingArtifactId()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-artifactId-pom.xml" );

        assertViolations( result, 0, 1, 0 );

        assertEquals( "'artifactId' is missing.", result.getErrors().get( 0 ) );
    }

    public void testMissingGroupId()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-groupId-pom.xml" );

        assertViolations( result, 0, 1, 0 );

        assertEquals( "'groupId' is missing.", result.getErrors().get( 0 ) );
    }

    public void testInvalidIds()
        throws Exception
    {
        SimpleProblemCollector result = validate( "invalid-ids-pom.xml" );

        assertViolations( result, 0, 2, 0 );

        assertEquals( "'groupId' with value 'o/a/m' does not match a valid id pattern.", result.getErrors().get( 0 ) );

        assertEquals( "'artifactId' with value 'm$-do$' does not match a valid id pattern.", result.getErrors().get( 1 ) );
    }

    public void testMissingType()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-type-pom.xml" );

        assertViolations( result, 0, 1, 0 );

        assertEquals( "'packaging' is missing.", result.getErrors().get( 0 ) );
    }

    public void testMissingVersion()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-version-pom.xml" );

        assertViolations( result, 0, 1, 0 );

        assertEquals( "'version' is missing.", result.getErrors().get( 0 ) );
    }

    public void testInvalidAggregatorPackaging()
        throws Exception
    {
        SimpleProblemCollector result = validate( "invalid-aggregator-packaging-pom.xml" );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).indexOf( "Aggregator projects require 'pom' as packaging." ) > -1 );
    }

    public void testMissingDependencyArtifactId()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-dependency-artifactId-pom.xml" );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).indexOf(
                                                         "'dependencies.dependency.artifactId' for groupId:null:jar is missing" ) > -1 );
    }

    public void testMissingDependencyGroupId()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-dependency-groupId-pom.xml" );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).indexOf(
                                                         "'dependencies.dependency.groupId' for null:artifactId:jar is missing" ) > -1 );
    }

    public void testMissingDependencyVersion()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-dependency-version-pom.xml" );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).indexOf(
                                                         "'dependencies.dependency.version' for groupId:artifactId:jar is missing" ) > -1 );
    }

    public void testMissingDependencyManagementArtifactId()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-dependency-mgmt-artifactId-pom.xml" );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).indexOf(
                                                         "'dependencyManagement.dependencies.dependency.artifactId' for groupId:null:jar is missing" ) > -1 );
    }

    public void testMissingDependencyManagementGroupId()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-dependency-mgmt-groupId-pom.xml" );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).indexOf(
                                                         "'dependencyManagement.dependencies.dependency.groupId' for null:artifactId:jar is missing" ) > -1 );
    }

    public void testMissingAll()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-1-pom.xml" );

        assertViolations( result, 0, 4, 0 );

        List<String> messages = result.getErrors();

        assertTrue( messages.contains( "\'modelVersion\' is missing." ) );
        assertTrue( messages.contains( "\'groupId\' is missing." ) );
        assertTrue( messages.contains( "\'artifactId\' is missing." ) );
        assertTrue( messages.contains( "\'version\' is missing." ) );
        // type is inherited from the super pom
    }

    public void testMissingPluginArtifactId()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-plugin-artifactId-pom.xml" );

        assertViolations( result, 0, 1, 0 );

        assertEquals( "'build.plugins.plugin.artifactId' is missing.", result.getErrors().get( 0 ) );
    }

    public void testEmptyPluginVersion()
        throws Exception
    {
        SimpleProblemCollector result = validate( "empty-plugin-version.xml" );

        assertViolations( result, 0, 1, 0 );

        assertEquals( "'build.plugins.plugin.version' for org.apache.maven.plugins:maven-it-plugin"
            + " must be a valid version but is ''.", result.getErrors().get( 0 ) );
    }

    public void testMissingRepositoryId()
        throws Exception
    {
        SimpleProblemCollector result =
            validateRaw( "missing-repository-id-pom.xml", ModelBuildingRequest.VALIDATION_LEVEL_STRICT );

        assertViolations( result, 0, 4, 0 );

        assertEquals( "'repositories.repository.id' is missing.", result.getErrors().get( 0 ) );

        assertEquals( "'repositories.repository[null].url' is missing.", result.getErrors().get( 1 ) );

        assertEquals( "'pluginRepositories.pluginRepository.id' is missing.", result.getErrors().get( 2 ) );

        assertEquals( "'pluginRepositories.pluginRepository[null].url' is missing.", result.getErrors().get( 3 ) );
    }

    public void testMissingResourceDirectory()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-resource-directory-pom.xml" );

        assertViolations( result, 0, 2, 0 );

        assertEquals( "'build.resources.resource.directory' is missing.", result.getErrors().get( 0 ) );

        assertEquals( "'build.testResources.testResource.directory' is missing.", result.getErrors().get( 1 ) );
    }

    public void testBadPluginDependencyScope()
        throws Exception
    {
        SimpleProblemCollector result = validate( "bad-plugin-dependency-scope.xml" );

        assertViolations( result, 0, 3, 0 );

        assertTrue( result.getErrors().get( 0 ).contains( "test:d" ) );

        assertTrue( result.getErrors().get( 1 ).contains( "test:e" ) );

        assertTrue( result.getErrors().get( 2 ).contains( "test:f" ) );
    }

    public void testBadDependencyScope()
        throws Exception
    {
        SimpleProblemCollector result = validate( "bad-dependency-scope.xml" );

        assertViolations( result, 0, 0, 2 );

        assertTrue( result.getWarnings().get( 0 ).contains( "test:f" ) );

        assertTrue( result.getWarnings().get( 1 ).contains( "test:g" ) );
    }

    public void testBadDependencyVersion()
        throws Exception
    {
        SimpleProblemCollector result = validate( "bad-dependency-version.xml" );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).contains( "test:b" ) );
    }

    public void testDuplicateModule()
        throws Exception
    {
        SimpleProblemCollector result = validate( "duplicate-module.xml" );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).contains( "child" ) );
    }

    public void testDuplicateProfileId()
        throws Exception
    {
        SimpleProblemCollector result = validateRaw( "duplicate-profile-id.xml" );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).contains( "non-unique-id" ) );
    }

    public void testBadPluginVersion()
        throws Exception
    {
        SimpleProblemCollector result = validate( "bad-plugin-version.xml" );

        assertViolations( result, 0, 3, 0 );

        assertTrue( result.getErrors().get( 0 ).contains( "test:mip" ) );
        assertTrue( result.getErrors().get( 1 ).contains( "test:rmv" ) );
        assertTrue( result.getErrors().get( 2 ).contains( "test:lmv" ) );
    }

    public void testDistributionManagementStatus()
        throws Exception
    {
        SimpleProblemCollector result = validate( "distribution-management-status.xml" );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).contains( "distributionManagement.status" ) );
    }

    public void testIncompleteParent()
        throws Exception
    {
        SimpleProblemCollector result = validateRaw( "incomplete-parent.xml" );

        assertViolations( result, 3, 0, 0 );

        assertTrue( result.getFatals().get( 0 ).contains( "parent.groupId" ) );
        assertTrue( result.getFatals().get( 1 ).contains( "parent.artifactId" ) );
        assertTrue( result.getFatals().get( 2 ).contains( "parent.version" ) );
    }

    public void testHardCodedSystemPath()
        throws Exception
    {
        SimpleProblemCollector result = validateRaw( "hard-coded-system-path.xml" );

        assertViolations( result, 0, 0, 1 );

        assertTrue( result.getWarnings().get( 0 ).contains( "test:a:jar" ) );
    }

    public void testEmptyModule()
        throws Exception
    {
        SimpleProblemCollector result = validate( "empty-module.xml" );

        assertViolations( result, 0, 0, 1 );

        assertTrue( result.getWarnings().get( 0 ).contains( "'modules.module[0]' has been specified without a path" ) );
    }

    public void testDuplicatePlugin()
        throws Exception
    {
        SimpleProblemCollector result = validateRaw( "duplicate-plugin.xml" );

        assertViolations( result, 0, 0, 4 );

        assertTrue( result.getWarnings().get( 0 ).contains( "duplicate declaration of plugin test:duplicate" ) );
        assertTrue( result.getWarnings().get( 1 ).contains( "duplicate declaration of plugin test:managed-duplicate" ) );
        assertTrue( result.getWarnings().get( 2 ).contains( "duplicate declaration of plugin profile:duplicate" ) );
        assertTrue( result.getWarnings().get( 3 ).contains( "duplicate declaration of plugin profile:managed-duplicate" ) );
    }

    public void testDuplicatePluginExecution()
        throws Exception
    {
        SimpleProblemCollector result = validateRaw( "duplicate-plugin-execution.xml" );

        assertViolations( result, 0, 4, 0 );

        assertContains( result.getErrors().get( 0 ), "duplicate execution with id a" );
        assertContains( result.getErrors().get( 1 ), "duplicate execution with id default" );
        assertContains( result.getErrors().get( 2 ), "duplicate execution with id c" );
        assertContains( result.getErrors().get( 3 ), "duplicate execution with id b" );
    }

    public void testReservedRepositoryId()
        throws Exception
    {
        SimpleProblemCollector result = validate( "reserved-repository-id.xml" );

        assertViolations( result, 0, 0, 4 );

        assertContains( result.getWarnings().get( 0 ), "'repositories.repository.id'" + " must not be 'local'" );
        assertContains( result.getWarnings().get( 1 ), "'pluginRepositories.pluginRepository.id' must not be 'local'" );
        assertContains( result.getWarnings().get( 2 ), "'distributionManagement.repository.id' must not be 'local'" );
        assertContains( result.getWarnings().get( 3 ),
                        "'distributionManagement.snapshotRepository.id' must not be 'local'" );
    }

    public void testMissingPluginDependencyGroupId()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-plugin-dependency-groupId.xml" );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).contains( ":a:" ) );
    }

    public void testMissingPluginDependencyArtifactId()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-plugin-dependency-artifactId.xml" );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).contains( "test:" ) );
    }

    public void testMissingPluginDependencyVersion()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-plugin-dependency-version.xml" );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).contains( "test:a" ) );
    }

    public void testBadPluginDependencyVersion()
        throws Exception
    {
        SimpleProblemCollector result = validate( "bad-plugin-dependency-version.xml" );

        assertViolations( result, 0, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).contains( "test:b" ) );
    }

    public void testBadVersion()
        throws Exception
    {
        SimpleProblemCollector result = validate( "bad-version.xml" );

        assertViolations( result, 0, 0, 1 );

        assertContains( result.getWarnings().get( 0 ), "'version' must not contain any of these characters" );
    }

    public void testBadRepositoryId()
        throws Exception
    {
        SimpleProblemCollector result = validate( "bad-repository-id.xml" );

        assertViolations( result, 0, 0, 4 );

        assertContains( result.getWarnings().get( 0 ),
                        "'repositories.repository.id' must not contain any of these characters" );
        assertContains( result.getWarnings().get( 1 ),
                        "'pluginRepositories.pluginRepository.id' must not contain any of these characters" );
        assertContains( result.getWarnings().get( 2 ),
                        "'distributionManagement.repository.id' must not contain any of these characters" );
        assertContains( result.getWarnings().get( 3 ),
                        "'distributionManagement.snapshotRepository.id' must not contain any of these characters" );
    }

    public void testBadDependencyExclusionId()
        throws Exception
    {
        SimpleProblemCollector result = validate( "bad-dependency-exclusion-id.xml" );

        assertViolations( result, 0, 0, 2 );

        assertContains( result.getWarnings().get( 0 ),
                        "'dependencies.dependency.exclusions.exclusion.groupId' for gid:aid:jar" );
        assertContains( result.getWarnings().get( 1 ),
                        "'dependencies.dependency.exclusions.exclusion.artifactId' for gid:aid:jar" );
    }

    public void testMissingDependencyExclusionId()
        throws Exception
    {
        SimpleProblemCollector result = validate( "missing-dependency-exclusion-id.xml" );

        assertViolations( result, 0, 0, 2 );

        assertContains( result.getWarnings().get( 0 ),
                        "'dependencies.dependency.exclusions.exclusion.groupId' for gid:aid:jar is missing" );
        assertContains( result.getWarnings().get( 1 ),
                        "'dependencies.dependency.exclusions.exclusion.artifactId' for gid:aid:jar is missing" );
    }

}
