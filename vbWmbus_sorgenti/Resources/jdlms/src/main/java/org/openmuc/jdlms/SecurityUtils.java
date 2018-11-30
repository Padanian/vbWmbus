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
package org.openmuc.jdlms;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.RFC3394WrapEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.openmuc.jdlms.SecuritySuite.SecurityPolicy;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DlmsEnumeration;

/**
 * A static utility class that provides security functions.
 * 
 * <p>
 * This class is useful if you may want to change the security setup of the remote meter.
 * </p>
 */
public class SecurityUtils {

    public enum KeyId implements DlmsEnumeration {
        GLOBAL_UNICAST_ENCRYPTION_KEY(0),
        GLOBAL_BROADCAST_ENCRYPTION_KEY(1),
        AUTHENTICATION_KEY(2);

        private final int id;

        private KeyId(int id) {
            this.id = id;
        }

        public int keyId() {
            return id;
        }

        @Override
        public long getCode() {
            return keyId();
        }
    }

    /**
     * Returns the method parameter for updating a single key of a DLMS server.
     * 
     * @param masterKey
     *            the master key, also known as KEK
     * @param newKey
     *            the new key to update to the DLMS server
     * @param keyId
     *            the type of key to update
     * @return return {@linkplain MethodParameter} for global key transfer
     */
    public static MethodParameter keyChangeMethodParamFor(byte[] masterKey, byte[] newKey, KeyId keyId) {
        final byte instance = 0; // current instance
        ObisCode obisCode = new ObisCode(0, 0, 43, 0, instance, 255);

        final byte[] wrappedKey = wrapAesRFC3394Key(masterKey, newKey);

        List<DataObject> keyDataList = Arrays.asList(DataObject.newEnumerateData(keyId.id),
                DataObject.newOctetStringData(wrappedKey));
        DataObject keyData = DataObject.newStructureData(keyDataList);
        DataObject methodParameter = DataObject.newArrayData(Arrays.asList(keyData));

        // IC_SecuritySetup#GLOBALE_KEY_TRANSFER
        return new MethodParameter(64, obisCode, 2, methodParameter);
    }

    /**
     * Sets the security policy method parameter
     * 
     * @param securityPolicy
     *            the security policy to set for
     * @return return MethodParameter for security policy
     */
    public static MethodParameter securityActivateMethodParamFor(SecurityPolicy securityPolicy) {
        final byte instance = 0; // current instance
        ObisCode instanceId = new ObisCode(0, 0, 43, 0, instance, 255);

        // IC_SecuritySetup#SECURITY_ACTIVATE
        return new MethodParameter(64, instanceId, 1, DataObject.newEnumerateData(securityPolicy.getId()));
    }

    /**
     * Encrypts a byte array with a master key with the algorithm AES in mode CBC and no padding.
     * 
     * @param masterKey
     *            the master key for encryption the bytesToCypher.
     * @param iv
     *            the initialization vector.
     * @param bytesToCipher
     *            the bytes to cipher.
     * @return the bytesToCipher encrypted
     * 
     * @throws GeneralSecurityException
     *             caused by {@link Cipher#doFinal(byte[])} or {@link Cipher#init(int, Key)}
     */
    public static byte[] cipherWithAes128(byte[] masterKey, byte[] iv, byte[] bytesToCipher)
            throws GeneralSecurityException {
        Key secretkeySpec = new SecretKeySpec(masterKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

        cipher.init(Cipher.ENCRYPT_MODE, secretkeySpec, ivSpec);

        return cipher.doFinal(bytesToCipher);
    }

    /**
     * This function wraps a key with a kek (key encryption key)
     * 
     * @param kek
     *            the key encryption key for wrapping the key
     * @param key
     *            the key to wrap
     * @return returns a with kek wrapped key
     */
    public static byte[] wrapAesRFC3394Key(byte[] kek, byte[] key) {
        RFC3394WrapEngine rfc3394WrapEngine = new RFC3394WrapEngine(new AESEngine());
        rfc3394WrapEngine.init(true, new KeyParameter(kek));

        return rfc3394WrapEngine.wrap(key, 0, key.length);
    }

    /**
     * This function unwraps a wrapped key with the kek (key encryption key)
     * 
     * @param kek
     *            the key encryption key for unwrapping the wrapped key
     * @param wrappedKey
     *            the wrapped key to unwrap
     * @return returns a unwrapped key
     * @throws InvalidCipherTextException
     *             will thrown if something unexpected is in the wrappedKey
     */
    public static byte[] unwrapAesRFC3394Key(byte[] kek, byte[] wrappedKey) throws InvalidCipherTextException {
        RFC3394WrapEngine rfc3394WrapEngine = new RFC3394WrapEngine(new AESEngine());
        rfc3394WrapEngine.init(false, new KeyParameter(kek));

        return rfc3394WrapEngine.unwrap(wrappedKey, 0, wrappedKey.length);
    }

    /**
     * Generates a random AES 128 key
     * 
     * @return returns a random AES 128 key
     */
    public static byte[] generateAES128Key() {
        byte[] key = new byte[16];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(key);

        return key;
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private SecurityUtils() {
    }

}
