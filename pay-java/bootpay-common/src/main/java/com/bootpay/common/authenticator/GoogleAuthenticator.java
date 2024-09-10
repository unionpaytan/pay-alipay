package com.bootpay.common.authenticator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bootpay.common.utils.HttpClient4Utils;
import com.bootpay.core.entity.EthInfo;
import com.bootpay.core.entity.TransInfo;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

/**
 * 
 * 
 * google身份验证器，java服务端实现
 * 
 * @author yangbo
 * 
 * @version 创建时间：2017年8月14日 上午10:10:02
 * 
 * 
 */
public class GoogleAuthenticator {
	private static Logger _log = LoggerFactory.getLogger(GoogleAuthenticator.class);
	// 生成的key长度( Generate secret key length)
	public static final int SECRET_SIZE = 10;

	public static final String SEED = "g8GjEvTbW5oVSV7avL47357438reyhreyuryetredLDVKs2m0QN7vxRs2im5MDaNCWGmcD2rvcZx";
	// Java实现随机数算法
	public static final String RANDOM_NUMBER_ALGORITHM = "SHA1PRNG"; // SHA1PRNG
	// 最多可偏移的时间
	public static int window_size = 5; // default 3 - max 17

	/**
	 * set the windows size. This is an integer value representing the number of 30
	 * second windows we allow The bigger the window, the more tolerant of clock
	 * skew we are.
	 * 
	 * @param s window size - must be >=1 and <=17. Other values are ignored
	 */
	public void setWindowSize(int s) {
		if (s >= 1 && s <= 17)
			window_size = s;
	}

	/**
	 * 生成一个随机秘钥
	 * 
	 * @return secret key
	 */
	public static String generateSecretKey() {
		SecureRandom sr = null;
		try {
			sr = SecureRandom.getInstance(RANDOM_NUMBER_ALGORITHM);
			sr.setSeed(Base64.decodeBase64(SEED));
			byte[] buffer = sr.generateSeed(SECRET_SIZE);
			Base32 codec = new Base32();
			byte[] bEncodedKey = codec.encode(buffer);
			String encodedKey = new String(bEncodedKey);
			return encodedKey;
		} catch (NoSuchAlgorithmException e) {
			// should never occur... configuration error
		}
		return null;
	}

	/**
	 * Return a URL that generates and displays a QR barcode. The user scans this
	 * bar code with the Google Authenticator application on their smartphone to
	 * register the auth code. They can also manually enter the secret if desired
	 * 
	 * @param user   user id (e.g. fflinstone)
	 * @param host   host or system that the code is for (e.g. myapp.com)
	 * @param secret the secret that was previously generated for this user
	 * @return the URL for the QR code to scan
	 */
	public static String getQRBarcodeURL(String user, String host, String secret) {
		String format = "http://www.google.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=otpauth://totp/%s@%s?secret=%s";
		return String.format(format, user, host, secret);
	}

	/**
	 * 生成一个google身份验证器，识别的字符串，只需要把该方法返回值生成二维码扫描就可以了。
	 * 
	 * @param user   账号
	 * @param secret 密钥
	 * @return
	 */
	public static String getQRBarcode(String user, String secret) {
		String format = "otpauth://totp/%s?secret=%s";
		return String.format(format, user, secret);
	}

	/**
	 * Check the code entered by the user to see if it is valid 验证code是否合法
	 */
	public static boolean check_code(String secret, String code, long timeMsec) {
		Base32 codec = new Base32();
		byte[] decodedKey = codec.decode(secret);
		long t = (timeMsec / 1000L) / 30L;
		for (int i = -window_size; i <= window_size; ++i) {
			long hash;
			try {
				hash = verify_code(decodedKey, t + i);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
				// return false;
			}
			String firstIndex = code.substring(0, 1);
			if ("0".equals(firstIndex)) { // 第1数字==0
				code = code.substring(1, 6);
				if ("0".equals(code.substring(0, 1))) {
					code = code.substring(1, 5); // 第1-2都为0
					if ("0".equals(code.substring(0, 1))) {
						code = code.substring(1, 4); // 第1-3都为0
					}
				}
			}
			// System.out.println("google:"+String.valueOf(hash)+",code:"+code.toString()+"");
			if (String.valueOf(hash).equals(code)) {
				return true;
			}
		}
		// The validation code is invalid.
		return false;
	}

	private static int verify_code(byte[] key, long t) throws NoSuchAlgorithmException, InvalidKeyException {
		byte[] data = new byte[8];
		long value = t;
		for (int i = 8; i-- > 0; value >>>= 8) {
			data[i] = (byte) value;
		}
		SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1"); // HmacSHA1
		Mac mac = Mac.getInstance("HmacSHA1"); // HmacSHA1
		mac.init(signKey);
		byte[] hash = mac.doFinal(data);
		int offset = hash[20 - 1] & 0xF;
		// We're using a long because Java hasn't got unsigned int.
		long truncatedHash = 0;
		for (int i = 0; i < 4; ++i) {
			truncatedHash <<= 8;
			// We are dealing with signed bytes:
			// we just keep the first byte.
			truncatedHash |= (hash[offset + i] & 0xFF);
		}
		truncatedHash &= 0x7FFFFFFF;
		truncatedHash %= 1000000;
		return (int) truncatedHash;
	}

	public static void main(String[] args) {
	    request_eth();
//		Date currentDate = new Date ();
//		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
//		String date = dateFormat.format(currentDate);
//
//        _log.info("date {},timestamp:{}",date,currentDate.getTime());
//
//        long timestamp = currentDate.getTime();
//        long timestampEth = Long.parseLong("1625756451000");
		//Long.parseLong(ethInfo.getTimeStamp() + "000")


	}

	private static void request_eth(){
		//http://api.etherscan.io/api?module=account&action=tokentx&contractaddress=0xdac17f958d2ee523a2206206994597c13d831ec7&address=0x962Ea20d34d6625681cb4E4Fce358225a74B25d6&page=1&offset=100&sort=desc&apikey=RQ9CUMRFUDJ4KMCPZA9ED2AU4AYB9FVGIZ
		Map<String, Object> reqMap = new HashMap<>();
		reqMap.put("module", "account");
		reqMap.put("action", "tokentx");
		reqMap.put("contractaddress","0xdac17f958d2ee523a2206206994597c13d831ec7");//usdt eth合约地址
		reqMap.put("address", "0x962Ea20d34d6625681cb4E4Fce358225a74B25d6");//要查询的钱包地址
		reqMap.put("page", "1");//页数
		reqMap.put("offset", "200");//1页200条数据
		reqMap.put("sort", "desc"); //排序
		reqMap.put("apikey", "RQ9CUMRFUDJ4KMCPZA9ED2AU4AYB9FVGIZ"); //apikey

		//String ethUrl = "http://api.etherscan.io/api?module=account&action=tokentx&contractaddress=0xdac17f958d2ee523a2206206994597c13d831ec7&address=0x962Ea20d34d6625681cb4E4Fce358225a74B25d6&page=1&offset=100&sort=desc&apikey=RQ9CUMRFUDJ4KMCPZA9ED2AU4AYB9FVGIZ";
	    String ethUrl = "http://api.etherscan.io/api";
		_log.info("ethUrl =====>>>>>{}", ethUrl);
		String serverResult = HttpClient4Utils.sendGet(ethUrl, reqMap);
		_log.info("ETH网络节点数据(已确认数据)返回=====>>>>>{}", serverResult);
		if (serverResult!=null){
			 JSONObject json = JSONObject.parseObject(serverResult);
			if (json != null){
				//JSONObject status = JSONObject.parseObject(json.get("status").toString());
				_log.info("status:{}",json.get("status").toString());
				if ("1".equals(json.get("status").toString())) {
					 _log.info("JSON OBJECT:{}", json.get("result").toString());
					BigDecimal unit_sun = new BigDecimal(1000000);
					//封装EthInfo类
					List<EthInfo> list = JSONObject.parseArray(json.get("result").toString(), EthInfo.class);

					for (EthInfo ethInfo : list) {
						//美东时间差8小时 60s*60m*8hours
						//20分钟 60 * 20 =120秒
						long delta = new Date().getTime() - Long.parseLong(ethInfo.getTimeStamp() + "000") + 60*60*8; //millseconds
						_log.info("delta:{}",delta / 1000);

						_log.info("Time:{},transId:{},value:{},from:{},to:{},timeStamp:{},tokenSymbol:{}",
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.parseLong(ethInfo.getTimeStamp() + "000") + 60*60*8),
								ethInfo.getHash(),
								ethInfo.getValue(),ethInfo.getFrom(),ethInfo.getTo(),
								Long.parseLong(ethInfo.getTimeStamp() + "000") + 60*60*8,ethInfo.getTokenSymbol());
						/**
						 *
						 * 判断时间 timeStamp 1200秒内 60*20
						 * 判断收款人钱包地址 to
						 * 判断金额 value
						 * hashid 0x6057a7844116b33fbed17948e7f34cf297bb5dfef9e993593e49ef11f68dbc8d
						 *
						 * */
						if (delta/1000 < (60*20)){
							_log.info("在20分钟内,转账成功");
						}
					}
				}
			}
		}


	}

	private  static void get_googleKey(){
		//String googleKey = generateSecretKey();
		//System.out.println("google key =" + googleKey);
		// String qrcode = GoogleAuthenticator.getQRBarcode("MAYI",googleKey);
		// System.out.println("qrcode:" + qrcode + ",key:" + googleKey);
		//System.out.println(check_code("SK3JAYS5L2NI366D", "023976", System.currentTimeMillis()));
		// System.out.println(getQRBarcodeURL("382505329@qq.com","382505329@qq.com","SK3JAYS5L2NI366D"));
		// System.out.println(getQRBarcodeURL("mayi","mayi",googleKey));
	}

	private  static void get_calendar(){
		//int minsAgo = 4; //4分钟
//		Calendar calendar = Calendar.getInstance();
//		calendar.add(Calendar.MINUTE, -minsAgo);
//		String howmanyMinsPreviewTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
//		_log.warn("超时任务==>>4分钟前的时间:{}", howmanyMinsPreviewTime);
	}

	private static void request_tron(){
		int seconds = 60 * 6 * 10000 ; //6分钟
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, -seconds);
		String payerAddr = "TTk2iCthT9ga38qAVPtKv1LtrjuN8KeMiX";
		Map<String, Object> reqMap = new HashMap<>();
		reqMap.put("only_confirmed", true);
		reqMap.put("limit", 100);
		reqMap.put("min_timestamp",calendar.getTimeInMillis());

		String tronUrl = "https://api.trongrid.io/v1/accounts/"+payerAddr+"/transactions/trc20";
		_log.info("tronUrl =====>>>>>{}", tronUrl);
		String serverResult = HttpClient4Utils.sendGet(tronUrl, reqMap);
		_log.info("TRON网络节点数据(已确认数据)返回=====>>>>>{}", serverResult);
		JSONObject json = JSONObject.parseObject(serverResult);

		if (json != null){
			JSONObject meta = JSONObject.parseObject(json.get("meta").toString());
			_log.info("JSON OBJECT:{}", meta.get("page_size").toString());
			if (!"0".equals(meta.get("page_size").toString())) {
				//有数据才做判断
				_log.info("有数据才做判断");
				BigDecimal unit_sun = new BigDecimal(1000000);
				//封装TRANS类
				List<TransInfo> list = JSONObject.parseArray(json.get("data").toString(), TransInfo.class);
				for (TransInfo transInfo : list) {
					_log.info("Type:{},transaction id:{},amt:{},from:{},to:{},date:{}",
							transInfo.getType(),transInfo.getTransaction_id(),
							transInfo.getValue(),transInfo.getFrom(),transInfo.getTo(),transInfo.getBlock_timestamp());

					//只要轩账的才记录 Transfer | Approval
					if ("Transfer".equals(transInfo.getType())){
						if (String.valueOf(new BigDecimal(1.00).multiply(unit_sun)).equals(transInfo.getValue())) {
							_log.info("付款成功:{}",transInfo.getType());
						}
					}


				}

			}
		}
	}
}