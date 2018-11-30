/**
 * Copyright 2012-18 Fraunhofer ISE
 *
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmuc.jdlms.internal.association;

import static org.openmuc.jdlms.AuthenticationMechanism.NONE;
import static org.openmuc.jdlms.internal.security.RandomSequenceGenerator.generateNewChallenge;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.openmuc.jdlms.AuthenticationMechanism;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.AssociateSourceDiagnostic.AcseServiceUser;
import org.openmuc.jdlms.internal.ConformanceSettingConverter;
import org.openmuc.jdlms.internal.ContextId;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemLogicalDevice;
import org.openmuc.jdlms.internal.ObjectIdentifier;
import org.openmuc.jdlms.internal.ServerConnectionData;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.Conformance;
import org.openmuc.jdlms.internal.asn1.cosem.InitiateRequest;
import org.openmuc.jdlms.internal.asn1.iso.acse.AARQApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSEApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.MechanismName;

class InitiateMessageProcessor {

    private final ServerConnectionData connectionData;
    private final CosemLogicalDevice cosemLogicalDevice;
    private final LogicalDevice logicalDevice;
    private ContextId contextId;

    public InitiateMessageProcessor(ServerConnectionData connectionData, CosemLogicalDevice cosemLogicalDevice) {
        this.connectionData = connectionData;
        this.cosemLogicalDevice = cosemLogicalDevice;
        this.logicalDevice = cosemLogicalDevice.getLogicalDevice();

        Map<Integer, SecuritySuite> restrictions = cosemLogicalDevice.getLogicalDevice().getRestrictions();
        this.connectionData.setSecuritySuite(restrictions.get(this.connectionData.getClientId()));
    }

    public ContextId getContextId() {
        return contextId;
    }

    public APdu processInitialMessage(byte[] messageData) throws IOException {
        SecuritySuite securitySuite = this.connectionData.getSecuritySuite();
        InitiateResponseBuilder initialResponseBuilder = new InitiateResponseBuilder(lnConformance());

        if (cosemLogicalDevice == null) {
            throw new AssociatRequestException(AcseServiceUser.NO_REASON_GIVEN);
        }

        Map<Integer, SecuritySuite> restrictions = logicalDevice.getRestrictions();
        APdu aPdu = APdu.decode(messageData, null);

        this.contextId = ObjectIdentifier
                .applicationContextIdFrom(aPdu.getAcseAPdu().getAarq().getApplicationContextName());
        if (restrictions.isEmpty()) {

            this.connectionData.setAuthenticated();
            this.connectionData.setSecuritySuite(SecuritySuite.builder().build());

            return initialResponseBuilder.setContextId(contextId).build();
        }

        if (securitySuite == null) {
            // unknown client ID
            throw new AssociatRequestException(AcseServiceUser.NO_REASON_GIVEN);
        }

        if (securitySuite.getEncryptionMechanism() != EncryptionMechanism.NONE) {
            this.connectionData.setClientSystemTitle(systemTitle());
            aPdu = APdu.decode(messageData, this.connectionData.getClientSystemTitle(), securitySuite, null);
        }

        if (aPdu.getCosemPdu() == null) {
            throw new AssociatRequestException(AcseServiceUser.NO_REASON_GIVEN);
        }

        COSEMpdu cosemPdu = aPdu.getCosemPdu();

        if (cosemPdu.getChoiceIndex() != COSEMpdu.Choices.INITIATEREQUEST) {
            throw new AssociatRequestException(AcseServiceUser.NO_REASON_GIVEN);
        }
        InitiateRequest initiateRequest = cosemPdu.initiateRequest;
        this.connectionData.setClientMaxReceivePduSize(initiateRequest.clientMaxReceivePduSize.getValue() & 0xFFFF);

        ACSEApdu acseAPdu = aPdu.getAcseAPdu();

        if (acseAPdu == null) {
            throw new AssociatRequestException(AcseServiceUser.NO_REASON_GIVEN);
        }
        AARQApdu aarq = acseAPdu.getAarq();

        return tryToAuthenticate(initialResponseBuilder, aarq, securitySuite);
    }

    private static void checkChallangeLength(int challengeLength) throws AssociatRequestException {
        if (challengeLength < 8 || challengeLength > 64) {
            throw new AssociatRequestException(AcseServiceUser.AUTHENTICATION_FAILURE);
        }
    }

    private APdu tryToAuthenticate(InitiateResponseBuilder initialResponseBuilder, AARQApdu aarq,
            SecuritySuite securitySuite) throws IOException {

        MechanismName mechanismName = aarq.getMechanismName();

        AuthenticationMechanism authLevel = AuthenticationMechanism.NONE;
        if (mechanismName != null) {
            authLevel = ObjectIdentifier.mechanismIdFrom(mechanismName);
        }

        if (authLevel != securitySuite.getAuthenticationMechanism()) {
            throw new AssociatRequestException(AcseServiceUser.AUTHENTICATION_FAILURE);
        }

        if (authLevel == NONE && securitySuite.getAuthenticationMechanism() == NONE) {
            this.connectionData.setAuthenticated();
            return initialResponseBuilder.setContextId(contextId).build();
        }

        this.connectionData.setClientToServerChallenge(aarq.getCallingAuthenticationValue().getCharstring().value);

        switch (authLevel) {
        case LOW:
            return processLowAuthentciationRequest(aarq, securitySuite.getPassword());

        case HLS5_GMAC:
            return processHls5GmacAuthentciationRequest(aarq);
        default:
            throw new AssociatRequestException(AcseServiceUser.APPLICATION_CONTEXT_NAME_NOT_SUPPORTED);
        }
    }

    private APdu processHls5GmacAuthentciationRequest(AARQApdu aarq) throws IOException {
        byte[] clientToServerChallenge = this.connectionData.getClientToServerChallenge();

        this.connectionData.setClientSystemTitle(aarq.getCallingAPTitle().getApTitleForm2().value);

        int challengeLength = clientToServerChallenge.length;

        checkChallangeLength(challengeLength);

        byte[] serverToClientChallenge = generateNewChallenge(challengeLength);

        this.connectionData.setServerToClientChallenge(serverToClientChallenge);

        return new InitiateResponseBuilder(lnConformance()).setContextId(contextId)
                .setAuthenticationValue(serverToClientChallenge)
                .setSystemTitle(systemTitle())
                .build();

    }

    private APdu processLowAuthentciationRequest(AARQApdu aarq, byte[] authenticationKey)
            throws AssociatRequestException {
        byte[] clientAuthenticaionValue = aarq.getCallingAuthenticationValue().getCharstring().value;

        if (Arrays.equals(clientAuthenticaionValue, authenticationKey)) {
            this.connectionData.setAuthenticated();
            return new InitiateResponseBuilder(lnConformance()).build();
        }

        throw new AssociatRequestException(AcseServiceUser.AUTHENTICATION_FAILURE);
    }

    private Conformance lnConformance() {
        return ConformanceSettingConverter.conformanceFor(this.logicalDevice.getConformance());
    }

    private byte[] systemTitle() {
        return this.logicalDevice.getSystemTitle();
    }

}
