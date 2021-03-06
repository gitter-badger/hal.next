/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.hal.resources;

public interface Constants extends com.google.gwt.i18n.client.Constants {

    //@formatter:off
    String accessType();
    String action();
    String active();
    String activeLower();
    String add();
    String address();
    String adminOnly();
    String and();
    String archived();
    String assignment();
    String assignmentsOfGroup();
    String assignmentsOfUser();
    String attribute();
    String attributes();

    String back();
    String backToNormalMode();
    String browse();
    String browseBy();

    String cancel();
    String category();
    String chooseFile();
    String choosePolicy();
    String chooseSingleton();
    String chooseStrategy();
    String chooseTemplate();
    String clear();
    String clearMessages();
    String clearRunAs();
    String clearRunAsTitle();
    String clone();
    String cloneProfile();
    String close();
    String closed();
    String committed();
    String connectedTo();
    String connection();
    String connections();
    String connectionPool();
    String connectToServer();
    String consoleVersion();
    String content();
    String contentRepository();
    String copied();
    String copy();
    String copyToClipboard();
    String count();
    String counter();
    String custom();

    String data();
    String day();
    String days();
    String defaultValue();
    String deploy();
    String deployContent();
    String deployExistingContent();
    String deploymentAttributes();
    String deploymentError();
    String deploymentInProgress();
    String deploymentPreview();
    String deploymentSuccessful();
    String deployments();
    String description();
    String details();
    String directory();
    String disable();
    String disabled();
    String domainConfigurationChanged();
    String domainConfigurationChangedTooltip();
    String download();
    String duplicateMacro();

    String edit();
    String enable();
    String enabled();
    String enableDeployment();
    String enableRbac();
    String enableStatistics();
    String endpointSelectTitle();
    String endpointSelectDescription();
    String endpointConnect();
    String endpointAddTitle();
    String endpointAddDescription();
    String environment();
    String excludeRole();
    String excludeUserGroup();
    String excludes();
    String expertMode();
    String explode();
    String exploded();
    String expression();
    String expressionResolver();
    String extensionError();
    String extensionNotFound();
    String extensionProcessing();

    String failed();
    String filter();
    String finish();
    String flushAll();
    String flushGracefully();
    String flushIdle();
    String flushInvalid();
    String formErrors();
    String formResetDesc();

    String globalSettings();
    String gotoDeployment();
    String group();
    String groups();

    String help();
    String hiddenColumns();
    String hideSensitive();
    String hitCount();
    String homepageNewToEap();
    String homepageTakeATour();
    String homepageDeploymentsSubHeader();
    String homepageDeploymentsSection();
    String homepageDeploymentsStandaloneStepIntro();
    String homepageDeploymentsStandaloneStep1();
    String homepageDeploymentsStepEnable();
    String homepageDeploymentsDomainStepIntro();
    String homepageDeploymentsDomainStep1();
    String homepageDeploymentsDomainStep2();
    String homepageConfigurationStandaloneSubHeader();
    String homepageConfigurationDomainSubHeader();
    String homepageConfigurationSection();
    String homepageConfigurationStepIntro();
    String homepageConfigurationStandaloneStep1();
    String homepageConfigurationDomainStep1();
    String homepageConfigurationStep2();
    String homepageConfigurationStep3();
    String homepageRuntimeStandaloneSubHeader();
    String homepageRuntimeStandaloneSection();
    String homepageRuntimeStepIntro();
    String homepageRuntimeStandaloneStep1();
    String homepageRuntimeStandaloneStep2();
    String homepageRuntimeDomainSubHeader();
    String homepageRuntimeDomainServerGroupSection();
    String homepageRuntimeDomainServerGroupStepIntro();
    String homepageRuntimeDomainServerGroupStep1();
    String homepageRuntimeDomainServerGroupStep2();
    String homepageRuntimeDomainCreateServerSection();
    String homepageRuntimeDomainCreateServerStepIntro();
    String homepageRuntimeDomainCreateServerStep1();
    String homepageRuntimeDomainCreateServerStep2();
    String homepageRuntimeDomainMonitorServerSection();
    String homepageRuntimeDomainMonitorServerStep1();
    String homepageRuntimeDomainMonitorServerStep2();
    String homepageAccessControlSubHeader();
    String homepageAccessControlSection();
    String homepageAccessControlStepIntro();
    String homepageAccessControlStep1();
    String homepageAccessControlStep2();
    String homepagePatchingSection();
    String homepagePatchingStep1();
    String homepagePatchingDomainStep2();
    String homepagePatchingStepApply();
    String homepageHelpNeedHelp();
    String homepageHelpGeneralResources();
    String homepageHelpEapDocumentationText();
    String homepageHelpEapDocumentationLink();
    String homepageHelpLearnMoreEapText();
    String homepageHelpLearnMoreEapLink();
    String homepageHelpTroubleTicketText();
    String homepageHelpTroubleTicketLink();
    String homepageHelpTrainingText();
    String homepageHelpTrainingLink();
    String homepageHelpTutorialsLink();
    String homepageHelpTutorialsText();
    String homepageHelpEapCommunityLink();
    String homepageHelpEapCommunityText();
    String homepageHelpEapConfigurationsLink();
    String homepageHelpEapConfigurationsText();
    String homepageHelpKnowledgebaseLink();
    String homepageHelpKnowledgebaseText();
    String homepageHelpConsultingLink();
    String homepageHelpConsultingText();
    String homepageHelpWilfdflyDocumentationText();
    String homepageHelpAdminGuideText();
    String homepageHelpWildflyIssuesText();
    String homepageHelpGetHelp();
    String homepageHelpIrcText();
    String homepageHelpUserForumsText();
    String homepageHelpDevelopersMailingListText();
    String homepageHelpWildFlyHomeText();
    String homepageHelpModelReferenceText();
    String homepageHelpLatestNews();
    String hostNameChanged();
    String hostsUsingThisFilter();
    String hostsUsingThisHandler();
    String hostScopedRole();
    String hour();
    String hours();

    String includeRole();
    String includeUserGroup();
    String includes();
    String includesAll();
    String includesAllDescription();
    String includesAllHeader();
    String input();
    String invalidExpression();
    String invalidJson();
    String invalidMetadata();

    String jcaConfiguration();
    String jgroupsRelayAlias();
    String jdbcDriver();
    String jndiTree();

    String kill();

    String lastModified();
    String loading();
    String loadingPleaseWait();
    String logFile();
    String logFilePreviewError();
    String logFiles();
    String logout();

    String macroEditor();
    String mainAttributes();
    String managed();
    String managementVersion();
    String maxUsed();
    String membership();
    String membershipOfRole();
    String message();
    String minute();
    String minutes();
    String missCount();
    String modelBrowser();
    String monitor();

    String needsReload();
    String needsRestart();
    String networkError();
    String next();
    String no();
    String noAttributes();
    String noBootErrors();
    String noConfiguredMailServers();
    String noContentSelected();
    String noDetails();
    String noHaPolicy();
    String noItems();
    String noMacros();
    String noPrincipalsExcluded();
    String noPrincipalsIncluded();
    String noReferenceServer();
    String noResource();
    String noRolesExcluded();
    String noRolesIncluded();
    String noRolesIncludedOrExcluded();
    String noRootLogger();
    String noRootLoggerDescription();
    String noRunningServers();
    String noStore();
    String noTransport();
    String noWrite();
    String notANumber();
    String notEnabled();
    String nothingSelected();

    String ok();
    String opened();
    String openInExternalWindow();
    String openInModelBrowser();
    String operationFailed();
    String operationMode();
    String operationSuccessful();
    String operation();
    String operations();
    String or();
    String orDragItHere();
    String output();

    String pattern();
    String pending();
    String pin();
    String ping();
    String platform();
    String play();
    String policy();
    String pool();
    String preparedStatementCache();
    String preview();
    String processors();
    String productName();
    String productVersion();
    String providedBy();

    String recovery();
    String references();
    String refresh();
    String releaseName();
    String releaseVersion();
    String reload();
    String reloadRequired();
    String reloadStandaloneTooltip();
    String remove();
    String rename();
    String replace();
    String replaceContent();
    String required();
    String requiredField();
    String reset();
    String resolve();
    String resolveExpression();
    String resolvedValue();
    String restart();
    String restartAllServices();
    String restartJvm();
    String restartNoServices();
    String restartRequired();
    String restartResourceServices();
    String restartStandaloneTooltip();
    String restricted();
    String resume();
    String review();
    String role();
    String roles();
    String runAs();
    String runAsRoleTitle();
    String running();

    String sameOrigin();
    String save();
    String search();
    String second();
    String seconds();
    String security();
    String serverGroupScopedRole();
    String serverName();
    String sessions();
    String settings();
    String showAll();
    String showSensitive();
    String size();
    String sizing();
    String specifyNames();
    String start();
    String starting();
    String startMacro();
    String statements();
    String statisticsDisabled();
    String statisticsDisabledHeader();
    String status();
    String stop();
    String stopMacro();
    String stopped();
    String storage();
    String supportsExpressions();
    String suspend();
    String suspended();
    String switchBehaviour();
    String switchProvider();
    String switchStore();
    String switchToExpressionMode();
    String switchToNormalMode();

    String tags();
    String tailMode();

    String test();
    String testConnection();
    String testConnectionError();
    String testConnectionSuccess();
    String timeout();
    String timeouts();
    String toggleDropdown();
    String toggleNavigation();
    String tools();
    String tracking();
    String type();

    String undeploy();
    String undeployContent();
    String undeployed();
    String unknownError();
    String unknownState();
    String unmanaged();
    String unpin();
    String unsupportedFileType();
    String unsupportedFileTypeDescription();
    String uploadContent();
    String uploadDeployment();
    String uploadError();
    String uploadInProgress();
    String uploadNewDeployment();
    String uploadSuccessful();
    String used();
    String user();
    String users();

    String validation();
    String view();
    String viewInEditor();

    String xaProperties();

    String yes();
    //@formatter:on
}
