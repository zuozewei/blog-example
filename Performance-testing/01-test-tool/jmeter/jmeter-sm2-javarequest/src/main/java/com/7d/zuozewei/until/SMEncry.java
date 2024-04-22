package com.7d.zuozewei.until;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.BCUtil;
import com.sm.sdk.encry.smencry.SMUtil;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

//import static com.sm2.sdk.encry.smencry.SMUtil.hexToByte;

@Slf4j
public class SMEncry {

    static X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
    static ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());

    /**
     * 生成sm2秘钥方法
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static AsymmetricCipherKeyPair sm2GenerateKeyPair() throws NoSuchAlgorithmException {
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());
        ECKeyPairGenerator keyPairGenerator = new ECKeyPairGenerator();
        keyPairGenerator.init(new ECKeyGenerationParameters(domainParameters, SecureRandom.getInstance("SHA1PRNG")));
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 获取公钥
     *
     * @param sm2GenerateKeyPair
     * @return
     */
    public static String getSm2HexPublicKey(AsymmetricCipherKeyPair sm2GenerateKeyPair) {
        ECPoint ecPoint = ((ECPublicKeyParameters) sm2GenerateKeyPair.getPublic()).getQ();
        return Hex.toHexString(ecPoint.getEncoded(false));
    }

    /**
     * 获取私钥
     *
     * @param sm2GenerateKeyPair
     * @return
     */
    public static String getSm2HexPrivateKey(AsymmetricCipherKeyPair sm2GenerateKeyPair) {
        //私钥，16进制格式，自己保存，格式如a2081b5b81fbea0b6b973a3ab6dbbbc65b1164488bf22d8ae2ff0b8260f64853
        BigInteger privatekey = ((ECPrivateKeyParameters) sm2GenerateKeyPair.getPrivate()).getD();
        return privatekey.toString(16);
    }

    /**
     * 加密
     *
     * @param content
     * @param pubKey
     * @return
     */
    public static String sm2Encrypt(String content, String pubKey) {
        try {
            if (StrUtil.isNotEmpty(content)) {
                int SM2_KEY_LEN = 32;
                //sm4加密后
                String publicStr = "";
                if (StrUtil.isNotEmpty(pubKey)) {
                    publicStr = pubKey;
                }
                byte[] publicKeyByte = SMUtil.hexToByte(publicStr.startsWith("04") ? publicStr.substring(2) : publicStr);
                if (publicKeyByte.length != SM2_KEY_LEN * 2) {
                    throw new RuntimeException("err key. ");
                }
                BigInteger X = new BigInteger(1, Arrays.copyOfRange(publicKeyByte, 0, SM2_KEY_LEN));
                BigInteger Y = new BigInteger(1, Arrays.copyOfRange(publicKeyByte, SM2_KEY_LEN, SM2_KEY_LEN * 2));
                ECPublicKeyParameters ecPublicKeyParameters = BCUtil.toParams(X, Y, domainParameters);
                CipherParameters pubKeyParameters = new ParametersWithRandom(ecPublicKeyParameters);
                SM2Engine engine = new SM2Engine();
                engine.init(true, pubKeyParameters);
                String hexString = Hex.toHexString(engine.processBlock(content.getBytes(), 0, content.getBytes().length));
                if (StrUtil.isNotEmpty(hexString)) {
                    if (hexString.startsWith("04")) {
                        hexString = hexString.substring(2);
                    }
                    return hexString;
                }
            }
        } catch (InvalidCipherTextException e) {
            log.error("加密失败", e);
        }
        return "";
    }

    /**
     * 解密
     *
     * @param content
     * @param priKey
     * @return
     */
    public static String sm2Decrypt(String content, String priKey) {
        if (StrUtil.isNotEmpty(content)) {
            //sm4加密后
            String privateStr = "";
            if (StrUtil.isNotEmpty(priKey)) {
                privateStr = priKey;
            }
            BigInteger privateKeyD = new BigInteger(privateStr, 16);
            byte[] cipherDataByte = Hex.decode(content);
            byte[] bytes1 = new byte[cipherDataByte.length + 1];
            bytes1[0] = 04;
            System.arraycopy(cipherDataByte, 0, bytes1, 1, cipherDataByte.length);
            //刚才的私钥Hex，先还原私钥
            ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKeyD, domainParameters);
            //用私钥解密
            SM2Engine sm2Engine = new SM2Engine();
            sm2Engine.init(false, privateKeyParameters);
            //processBlock得到Base64格式，记得解码
            byte[] processBlock;
            try {
                processBlock = sm2Engine.processBlock(bytes1, 0, bytes1.length);
            } catch (InvalidCipherTextException e) {
                log.error("解密失败", e);
                return "";
            }
            return new String(processBlock);
        }
        return "";
    }


}
