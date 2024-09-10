const crypto = require('crypto');

const CHAR_ENCODING = 'utf-8';
const AES_ALGORITHM = 'aes-128-ecb'; // ECB模式，Java默认的AES模式是AES/ECB/PKCS5Padding
const AES_KEY = 'LZKBZCA2wSTeEDCY'; // 16位加密TOKEN的盐 (须和java的 AesUtil的 token一致)

// 加密
function encrypt(data, key) {
    if (key.length < 16) {
        throw new Error('Invalid AES key length (must be 16 bytes)');
    } else if (key.length > 16) {
        key = key.substring(0, 16);
    }

    const cipher = crypto.createCipheriv(AES_ALGORITHM, key, null);
    let encrypted = cipher.update(data, CHAR_ENCODING, 'hex');
    encrypted += cipher.final('hex');
    return encrypted.toUpperCase();
}

// Base64加密
function encryptBase64(data, key) {
    if (key.length < 16) {
        throw new Error('Invalid AES key length (must be 16 bytes)');
    } else if (key.length > 16) {
        key = key.substring(0, 16);
    }

    const cipher = crypto.createCipheriv(AES_ALGORITHM, key, null);
    let encrypted = cipher.update(data, CHAR_ENCODING, 'base64');
    encrypted += cipher.final('base64');
    return encrypted;
}

// 解密
function decrypt(data, key) {
    if (key.length < 16) {
        throw new Error('Invalid AES key length (must be 16 bytes)');
    } else if (key.length > 16) {
        key = key.substring(0, 16);
    }

    const decipher = crypto.createDecipheriv(AES_ALGORITHM, key, null);
    let decrypted = decipher.update(data, 'hex', CHAR_ENCODING);
    decrypted += decipher.final(CHAR_ENCODING);
    return decrypted;
}

// 将二进制转换成16进制
function parseByte2HexStr(buf) {
    return buf.toString('hex').toUpperCase();
}

// 将16进制转换为二进制
function parseHexStr2Byte(hexStr) {
    return Buffer.from(hexStr, 'hex');
}

// 导出 encrypt 和 decrypt 方法
module.exports = {
    encrypt,
    decrypt,
};

// 测试 test
// function test() {
//     const startTime = Date.now();
//     const data = 'abcdefg'; // 需要加密的数据
//     const encryptedData = encrypt(data, AES_KEY);
//     console.log('Encrypted Data (Hex):', encryptedData);

//     const decryptedData = decrypt(encryptedData, AES_KEY);
//     console.log('Decrypted Data:', decryptedData);

//     console.log('共用时:', Date.now() - startTime, 'ms');
// }

// test();
