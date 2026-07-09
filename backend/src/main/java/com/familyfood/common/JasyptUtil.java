package com.familyfood.common;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

/**
 * Jasypt 加解密工具
 * <p>
 * 用法：
 *   1. 设置环境变量 JASYPT_ENCRYPTOR_PASSWORD（或修改下方 DEFAULT_PASSWORD 用于本地测试）
 *   2. 运行 main 方法，控制台会输出加密后的密文
 *   3. 将密文以 ENC(密文) 格式写入 application.yml
 */
public class JasyptUtil {

    /** 本地测试用默认密码，生产环境务必通过环境变量传入 */
    private static final String DEFAULT_PASSWORD = "family-food-dev-key";

    private static final String ALGORITHM = "PBEWithMD5AndDES";

    /**
     * 加密明文
     */
    public static String encrypt(String plain) {
        return buildEncryptor().encrypt(plain).replace("\n", "").replace("\r", "");
    }

    /**
     * 解密密文
     */
    public static String decrypt(String cipher) {
        return buildEncryptor().decrypt(cipher);
    }

    private static PooledPBEStringEncryptor buildEncryptor() {
        String password = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        if (password == null || password.isEmpty()) {
            password = DEFAULT_PASSWORD;
        }

        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm(ALGORITHM);
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setProviderClassName("com.sun.crypto.provider.SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }
}
