/*
 * Central Repository
 *
 * Copyright 2017-2020 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
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
package org.sleuthkit.autopsy.centralrepository.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.centralrepository.datamodel.CentralRepoAccount.CentralRepoAccountType;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Account;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE;
import org.sleuthkit.datamodel.HashUtility;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;

/**
 * Utility class for working with correlation attributes in the central
 * repository.
 */
public class CorrelationAttributeUtil {

    private static final Logger logger = Logger.getLogger(CorrelationAttributeUtil.class.getName());

    /**
     * Gets a string that is expected to be the same string that is stored in
     * the correlation_types table in the central repository as the display name
     * for the email address correlation attribute type. This string is
     * duplicated in the CorrelationAttributeInstance class.
     *
     * TODO (Jira-6088): We should not have multiple deifnitions of this string.
     *
     * @return The display name of the email address correlation attribute type.
     */
    @Messages({"CorrelationAttributeUtil.emailaddresses.text=Email Addresses"})
    private static String getEmailAddressAttrDisplayName() {
        return Bundle.CorrelationAttributeUtil_emailaddresses_text();
    }

    /**
     * Makes zero to many correlation attribute instances from the attributes of
     * an artifact.
     *
     * IMPORTANT: The correlation attribute instances are NOT added to the
     * central repository by this method.
     *
     * @param artifact An artifact.
     *
     * @return A list, possibly empty, of correlation attribute instances for
     *         the artifact.
     */
    public static List<CorrelationAttributeInstance> makeCorrAttrsFromArtifact(BlackboardArtifact artifact) {
        return makeCorrAttrsFromArtifact(artifact, true );
    }
    
    /**
     * Makes zero to many correlation attribute instances from the attributes of
     * an artifact.
     *
     * IMPORTANT: The correlation attribute instances are NOT added to the
     * central repository by this method.
     *
     * TODO (Jira-6088): The methods in this low-level, utility class should
     * throw exceptions instead of logging them. The reason for this is that the
     * clients of the utility class, not the utility class itself, should be in
     * charge of error handling policy, per the Autopsy Coding Standard. Note
     * that clients of several of these methods currently cannot determine
     * whether receiving a null return value is an error or not, plus null
     * checking is easy to forget, while catching exceptions is enforced.
     *
     * @param artifact              An artifact.
     * @param resolveSourceArtifact A flag to indicate whether to resolve the
     *                              source artifact, if the given artifact is 
     *                              of type TSK_INTERESTING_ARTIFACT_HIT.
     *
     * @return A list, possibly empty, of correlation attribute instances for
     *          the artifact.
     */
    public static List<CorrelationAttributeInstance> makeCorrAttrsFromArtifact(BlackboardArtifact artifact, boolean resolveSourceArtifact) {
        List<CorrelationAttributeInstance> correlationAttrs = new ArrayList<>();
        
        // If the artifact is of type TSK_INTERESTING_ARTIFACT_HIT, and the caller 
        // has not indicated to resolve the source artifact, then return an empty list.
        if ((artifact.getArtifactTypeID() == ARTIFACT_TYPE.TSK_INTERESTING_ARTIFACT_HIT.getTypeID()) && (resolveSourceArtifact == false) ) {
            return correlationAttrs;
        }
        try {
            BlackboardArtifact sourceArtifact = getCorrAttrSourceArtifact(artifact);
            if (sourceArtifact != null) {
                int artifactTypeID = sourceArtifact.getArtifactTypeID();
                if (artifactTypeID == ARTIFACT_TYPE.TSK_WEB_BOOKMARK.getTypeID()
                        || artifactTypeID == ARTIFACT_TYPE.TSK_WEB_COOKIE.getTypeID()
                        || artifactTypeID == ARTIFACT_TYPE.TSK_WEB_DOWNLOAD.getTypeID()
                        || artifactTypeID == ARTIFACT_TYPE.TSK_WEB_HISTORY.getTypeID()) {
                    makeCorrAttrFromArtifactAttr(correlationAttrs, sourceArtifact, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DOMAIN, CorrelationAttributeInstance.DOMAIN_TYPE_ID);

                } else if (artifactTypeID == ARTIFACT_TYPE.TSK_DEVICE_ATTACHED.getTypeID()) {
                    makeCorrAttrFromArtifactAttr(correlationAttrs, sourceArtifact, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DEVICE_ID, CorrelationAttributeInstance.USBID_TYPE_ID);
                    makeCorrAttrFromArtifactAttr(correlationAttrs, sourceArtifact, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_MAC_ADDRESS, CorrelationAttributeInstance.MAC_TYPE_ID);

                } else if (artifactTypeID == ARTIFACT_TYPE.TSK_WIFI_NETWORK.getTypeID()) {
                    makeCorrAttrFromArtifactAttr(correlationAttrs, sourceArtifact, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_SSID, CorrelationAttributeInstance.SSID_TYPE_ID);

                } else if (artifactTypeID == ARTIFACT_TYPE.TSK_WIFI_NETWORK_ADAPTER.getTypeID()
                        || artifactTypeID == ARTIFACT_TYPE.TSK_BLUETOOTH_PAIRING.getTypeID()
                        || artifactTypeID == ARTIFACT_TYPE.TSK_BLUETOOTH_ADAPTER.getTypeID()) {
                    makeCorrAttrFromArtifactAttr(correlationAttrs, sourceArtifact, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_MAC_ADDRESS, CorrelationAttributeInstance.MAC_TYPE_ID);

                } else if (artifactTypeID == ARTIFACT_TYPE.TSK_DEVICE_INFO.getTypeID()) {
                    makeCorrAttrFromArtifactAttr(correlationAttrs, sourceArtifact, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_IMEI, CorrelationAttributeInstance.IMEI_TYPE_ID);
                    makeCorrAttrFromArtifactAttr(correlationAttrs, sourceArtifact, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_IMSI, CorrelationAttributeInstance.IMSI_TYPE_ID);
                    makeCorrAttrFromArtifactAttr(correlationAttrs, sourceArtifact, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ICCID, CorrelationAttributeInstance.ICCID_TYPE_ID);

                } else if (artifactTypeID == ARTIFACT_TYPE.TSK_SIM_ATTACHED.getTypeID()) {
                    makeCorrAttrFromArtifactAttr(correlationAttrs, sourceArtifact, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_IMSI, CorrelationAttributeInstance.IMSI_TYPE_ID);
                    makeCorrAttrFromArtifactAttr(correlationAttrs, sourceArtifact, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ICCID, CorrelationAttributeInstance.ICCID_TYPE_ID);

                } else if (artifactTypeID == ARTIFACT_TYPE.TSK_WEB_FORM_ADDRESS.getTypeID()) {
                    makeCorrAttrFromArtifactAttr(correlationAttrs, sourceArtifact, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER, CorrelationAttributeInstance.PHONE_TYPE_ID);
                    makeCorrAttrFromArtifactAttr(correlationAttrs, sourceArtifact, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_EMAIL, CorrelationAttributeInstance.EMAIL_TYPE_ID);

                } else if (artifactTypeID == ARTIFACT_TYPE.TSK_ACCOUNT.getTypeID()) {
                    makeCorrAttrFromAcctArtifact(correlationAttrs, sourceArtifact);
                }
            }
        } catch (CentralRepoException ex) {
            logger.log(Level.SEVERE, String.format("Error querying central repository (%s)", artifact), ex); // NON-NLS
            return correlationAttrs;
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, String.format("Error getting querying case database (%s)", artifact), ex); // NON-NLS
            return correlationAttrs;
        } catch (NoCurrentCaseException ex) {
            logger.log(Level.SEVERE, "Error getting current case", ex); // NON-NLS
            return correlationAttrs;
        }
        return correlationAttrs;
    }

    /**
     * Gets the associated artifact of a "meta-artifact" such as an interesting
     * artifact hit artifact.
     *
     * @param artifact An artifact.
     *
     * @return The associated artifact if the input artifact is a
     *         "meta-artifact", otherwise the input artifact.
     *
     * @throws NoCurrentCaseException If there is no open case.
     * @throws TskCoreException       If there is an error querying thew case
     *                                database.
     */
    private static BlackboardArtifact getCorrAttrSourceArtifact(BlackboardArtifact artifact) throws NoCurrentCaseException, TskCoreException {
        BlackboardArtifact sourceArtifact = null;
        if (BlackboardArtifact.ARTIFACT_TYPE.TSK_INTERESTING_ARTIFACT_HIT.getTypeID() == artifact.getArtifactTypeID()) {
            BlackboardAttribute assocArtifactAttr = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ASSOCIATED_ARTIFACT));
            if (assocArtifactAttr != null) {
                sourceArtifact = Case.getCurrentCaseThrows().getSleuthkitCase().getBlackboardArtifact(assocArtifactAttr.getValueLong());
            }
        } else {
            sourceArtifact = artifact;
        }
        return sourceArtifact;
    }

    /**
     * Makes a correlation attribute instance for an account artifact.
     * 
     * Also creates an account in the CR DB if it doesn't exist.
     *
     * IMPORTANT: The correlation attribute instance is NOT added to the central
     * repository by this method.
     *
     * TODO (Jira-6088): The methods in this low-level, utility class should
     * throw exceptions instead of logging them. The reason for this is that the
     * clients of the utility class, not the utility class itself, should be in
     * charge of error handling policy, per the Autopsy Coding Standard. Note
     * that clients of several of these methods currently cannot determine
     * whether receiving a null return value is an error or not, plus null
     * checking is easy to forget, while catching exceptions is enforced.
     *
     * @param corrAttrInstances A list of correlation attribute instances.
     * @param acctArtifact      An account artifact.
     *
     * @return The correlation attribute instance.
     */
    private static void makeCorrAttrFromAcctArtifact(List<CorrelationAttributeInstance> corrAttrInstances, BlackboardArtifact acctArtifact) throws TskCoreException, CentralRepoException {
       
        // Get the account type from the artifact
        BlackboardAttribute accountTypeAttribute = acctArtifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ACCOUNT_TYPE));
        String accountTypeStr = accountTypeAttribute.getValueString();
        
        // @@TODO Vik-6136: CR currently does not know of custom account types.  
        // Ensure there is a predefined account type for this account.
        Account.Type predefinedAccountType = Account.Type.PREDEFINED_ACCOUNT_TYPES.stream().filter(type -> type.getTypeName().equalsIgnoreCase(accountTypeStr)).findAny().orElse(null);
             
        // do not create any correlation attribute instance for a Device account
        if (Account.Type.DEVICE.getTypeName().equalsIgnoreCase(accountTypeStr) == false && predefinedAccountType != null) {

            // Get the corresponding CentralRepoAccountType from the database.
            CentralRepoAccountType crAccountType = CentralRepository.getInstance().getAccountTypeByName(accountTypeStr);

            int corrTypeId = crAccountType.getCorrelationTypeId();
            CorrelationAttributeInstance.Type corrType = CentralRepository.getInstance().getCorrelationTypeById(corrTypeId);

            // Get the account identifier
            BlackboardAttribute accountIdAttribute = acctArtifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ID));
            String accountIdStr = accountIdAttribute.getValueString();

            // add/get the account and get its accountId.
            CentralRepoAccount crAccount = CentralRepository.getInstance().getOrCreateAccount(crAccountType, accountIdStr);

            CorrelationAttributeInstance corrAttr = makeCorrAttr(acctArtifact, corrType, accountIdStr);
            if (corrAttr != null) {
                // set the account_id in correlation attribute
                corrAttr.setAccountId(crAccount.getAccountId());
                corrAttrInstances.add(corrAttr);
            }
        }
    }

    /**
     * Makes a correlation attribute instance from a specified attribute of an
     * artifact. The correlation attribute instance is added to an input list.
     *
     * @param corrAttrInstances A list of correlation attribute instances.
     * @param artifact          An artifact.
     * @param artAttrType       The type of the atrribute of the artifact that
     *                          is to be made into a correlatin attribute
     *                          instance.
     * @param typeId            The type ID for the desired correlation
     *                          attribute instance.
     *
     * @throws CentralRepoException If there is an error querying the central
     *                              repository.
     * @throws TskCoreException     If there is an error querying the case
     *                              database.
     */
    private static void makeCorrAttrFromArtifactAttr(List<CorrelationAttributeInstance> corrAttrInstances, BlackboardArtifact artifact, ATTRIBUTE_TYPE artAttrType, int typeId) throws CentralRepoException, TskCoreException {
        BlackboardAttribute attribute = artifact.getAttribute(new BlackboardAttribute.Type(artAttrType));
        if (attribute != null) {
            String value = attribute.getValueString();
            if ((null != value) && (value.isEmpty() == false)) {
                CorrelationAttributeInstance inst = makeCorrAttr(artifact, CentralRepository.getInstance().getCorrelationTypeById(typeId), value);
                if (inst != null) {
                    corrAttrInstances.add(inst);
                }
            }
        }
    }

    /**
     * Makes a correlation attribute instance of a given type from an artifact.
     *
     * @param artifact        The artifact.
     * @param correlationType the correlation attribute type.
     * @param value           The correlation attribute value.
     *
     * TODO (Jira-6088): The methods in this low-level, utility class should
     * throw exceptions instead of logging them. The reason for this is that the
     * clients of the utility class, not the utility class itself, should be in
     * charge of error handling policy, per the Autopsy Coding Standard. Note
     * that clients of several of these methods currently cannot determine
     * whether receiving a null return value is an error or not, plus null
     * checking is easy to forget, while catching exceptions is enforced.
     *
     * @return The correlation attribute instance or null, if an error occurred.
     */
    private static CorrelationAttributeInstance makeCorrAttr(BlackboardArtifact artifact, CorrelationAttributeInstance.Type correlationType, String value) {
        try {
            Case currentCase = Case.getCurrentCaseThrows();
            AbstractFile bbSourceFile = currentCase.getSleuthkitCase().getAbstractFileById(artifact.getObjectID());
            if (null == bbSourceFile) {
                logger.log(Level.SEVERE, "Error creating artifact instance. Abstract File was null."); // NON-NLS
                return null;
            }

            CorrelationCase correlationCase = CentralRepository.getInstance().getCase(Case.getCurrentCaseThrows());
            return new CorrelationAttributeInstance(
                    correlationType,
                    value,
                    correlationCase,
                    CorrelationDataSource.fromTSKDataSource(correlationCase, bbSourceFile.getDataSource()),
                    bbSourceFile.getParentPath() + bbSourceFile.getName(),
                    "",
                    TskData.FileKnown.UNKNOWN,
                    bbSourceFile.getId());

        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, String.format("Error getting querying case database (%s)", artifact), ex); // NON-NLS
            return null;
        } catch (CentralRepoException ex) {
            logger.log(Level.SEVERE, String.format("Error querying central repository (%s)", artifact), ex); // NON-NLS
            return null;
        } catch (CorrelationAttributeNormalizationException ex) {
            logger.log(Level.WARNING, String.format("Error creating correlation attribute instance (%s)", artifact), ex); // NON-NLS
            return null;
        } catch (NoCurrentCaseException ex) {
            logger.log(Level.SEVERE, "Error getting current case", ex); // NON-NLS
            return null;
        }
    }

    /**
     * Gets the correlation attribute instance for a file.
     *
     * @param file The file.
     *
     * TODO (Jira-6088): The methods in this low-level, utility class should
     * throw exceptions instead of logging them. The reason for this is that the
     * clients of the utility class, not the utility class itself, should be in
     * charge of error handling policy, per the Autopsy Coding Standard. Note
     * that clients of several of these methods currently cannot determine
     * whether receiving a null return value is an error or not, plus null
     * checking is easy to forget, while catching exceptions is enforced.
     *
     * @return The correlation attribute instance or null, if no such
     *         correlation attribute instance was found or an error occurred.
     */
    public static CorrelationAttributeInstance getCorrAttrForFile(AbstractFile file) {

        if (!isSupportedAbstractFileType(file)) {
            return null;
        }

        CorrelationAttributeInstance.Type type;
        CorrelationCase correlationCase;
        CorrelationDataSource correlationDataSource;

        try {
            type = CentralRepository.getInstance().getCorrelationTypeById(CorrelationAttributeInstance.FILES_TYPE_ID);
            correlationCase = CentralRepository.getInstance().getCase(Case.getCurrentCaseThrows());
            if (null == correlationCase) {
                //if the correlationCase is not in the Central repo then attributes generated in relation to it will not be
                return null;
            }
            correlationDataSource = CorrelationDataSource.fromTSKDataSource(correlationCase, file.getDataSource());
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, String.format("Error getting querying case database (%s)", file), ex); // NON-NLS
            return null;
        } catch (CentralRepoException ex) {
            logger.log(Level.SEVERE, String.format("Error querying central repository (%s)", file), ex); // NON-NLS
            return null;
        } catch (NoCurrentCaseException ex) {
            logger.log(Level.SEVERE, "Error getting current case", ex); // NON-NLS
            return null;
        }

        CorrelationAttributeInstance correlationAttributeInstance;
        try {
            correlationAttributeInstance = CentralRepository.getInstance().getCorrelationAttributeInstance(type, correlationCase, correlationDataSource, file.getId());
        } catch (CentralRepoException ex) {
            logger.log(Level.SEVERE, String.format("Error querying central repository (%s)", file), ex); // NON-NLS
            return null;
        } catch (CorrelationAttributeNormalizationException ex) {
            logger.log(Level.WARNING, String.format("Error creating correlation attribute instance (%s)", file), ex); // NON-NLS
            return null;
        }

        /*
         * If no correlation attribute instance was found when querying by file
         * object ID, try searching by file path instead. This is necessary
         * because file object IDs were not stored in the central repository in
         * early versions of its schema.
         */
        if (correlationAttributeInstance == null && file.getMd5Hash() != null) {
            String filePath = (file.getParentPath() + file.getName()).toLowerCase();
            try {
                correlationAttributeInstance = CentralRepository.getInstance().getCorrelationAttributeInstance(type, correlationCase, correlationDataSource, file.getMd5Hash(), filePath);
            } catch (CentralRepoException ex) {
                logger.log(Level.SEVERE, String.format("Error querying central repository (%s)", file), ex); // NON-NLS
                return null;
            } catch (CorrelationAttributeNormalizationException ex) {
                logger.log(Level.WARNING, String.format("Error creating correlation attribute instance (%s)", file), ex); // NON-NLS
                return null;
            }
        }

        return correlationAttributeInstance;
    }

    /**
     * Makes a correlation attribute instance for a file.
     *
     * IMPORTANT: The correlation attribute instance is NOT added to the central
     * repository by this method.
     *
     * TODO (Jira-6088): The methods in this low-level, utility class should
     * throw exceptions instead of logging them. The reason for this is that the
     * clients of the utility class, not the utility class itself, should be in
     * charge of error handling policy, per the Autopsy Coding Standard. Note
     * that clients of several of these methods currently cannot determine
     * whether receiving a null return value is an error or not, plus null
     * checking is easy to forget, while catching exceptions is enforced.
     *
     * @param file The file.
     *
     * @return The correlation attribute instance or null, if an error occurred.
     */
    public static CorrelationAttributeInstance makeCorrAttrFromFile(AbstractFile file) {

        if (!isSupportedAbstractFileType(file)) {
            return null;
        }

        // We need a hash to make the correlation artifact instance.
        String md5 = file.getMd5Hash();
        if (md5 == null || md5.isEmpty() || HashUtility.isNoDataMd5(md5)) {
            return null;
        }

        try {
            CorrelationAttributeInstance.Type filesType = CentralRepository.getInstance().getCorrelationTypeById(CorrelationAttributeInstance.FILES_TYPE_ID);

            CorrelationCase correlationCase = CentralRepository.getInstance().getCase(Case.getCurrentCaseThrows());
            return new CorrelationAttributeInstance(
                    filesType,
                    file.getMd5Hash(),
                    correlationCase,
                    CorrelationDataSource.fromTSKDataSource(correlationCase, file.getDataSource()),
                    file.getParentPath() + file.getName(),
                    "",
                    TskData.FileKnown.UNKNOWN,
                    file.getId());

        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, String.format("Error querying case database (%s)", file), ex); // NON-NLS            
            return null;
        } catch (CentralRepoException ex) {
            logger.log(Level.SEVERE, String.format("Error querying central repository (%s)", file), ex); // NON-NLS            
            return null;
        } catch (CorrelationAttributeNormalizationException ex) {
            logger.log(Level.WARNING, String.format("Error creating correlation attribute instance (%s)", file), ex); // NON-NLS
            return null;
        } catch (NoCurrentCaseException ex) {
            logger.log(Level.SEVERE, "Error getting current case", ex); // NON-NLS
            return null;
        }
    }

    /**
     * Checks whether or not a file is of a type that can be added to the
     * central repository as a correlation attribute instance.
     *
     * @param file A file.
     *
     * @return True or false.
     */
    public static boolean isSupportedAbstractFileType(AbstractFile file) {
        if (file == null) {
            return false;
        }
        switch (file.getType()) {
            case UNALLOC_BLOCKS:
            case UNUSED_BLOCKS:
            case SLACK:
            case VIRTUAL_DIR:
            case LOCAL_DIR:
                return false;
            case CARVED:
            case DERIVED:
            case LOCAL:
            case LAYOUT_FILE:
                return true;
            case FS:
                return file.isMetaFlagSet(TskData.TSK_FS_META_FLAG_ENUM.ALLOC);
            default:
                logger.log(Level.WARNING, "Unexpected file type {0}", file.getType().getName());
                return false;
        }
    }

    /**
     * Prevent instantiation of this utility class.
     */
    private CorrelationAttributeUtil() {
    }

}
