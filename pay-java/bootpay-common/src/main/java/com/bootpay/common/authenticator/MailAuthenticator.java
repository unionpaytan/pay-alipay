package com.bootpay.common.authenticator;

import java.io.*;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.excetion.TranException;

/**
 * 邮箱发送验证码
 * 
 * @author Administrator
 *
 */
@Component
public class MailAuthenticator {

	private Logger _log = LoggerFactory.getLogger(MailAuthenticator.class);

	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String username;

	public void send(String toMail, String subject, String code, String merchantName, String type) throws TranException {
		//_log.info("生产环境关闭 EMAIL -- subject:{},merchantName:{},type:{},校验码 code:{}",subject,merchantName,type,code);
		String htmlContent = getHtml(subject, merchantName, type, code);
		// new 一个简单邮件消息对象
		MimeMessage message = mailSender.createMimeMessage();
		// 发送
		try {
			//给自己抄送一份 不然163会屏蔽你的IP
		  message.addRecipients(MimeMessage.RecipientType.CC, InternetAddress.parse(username));

		  MimeMessageHelper helper=new MimeMessageHelper(message,true);
		   // 和配置文件中的的username相同，相当于发送方
	        helper.setFrom(username);
			// 收件人邮箱
	        helper.setTo(toMail);
			// 标题
	        helper.setSubject(subject);
			// 正文
	        helper.setText(htmlContent,true);
			mailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
			_log.info("sending email error:{}", e.getMessage());
			throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "邮件服务器异常");
		}
	}

	public static String getHtml(String title, String userName, String type, String code) {
		String emailTemplet = System.getProperty("emailTemplet");
		emailTemplet = emailTemplet.replace("$(title)", title);
		emailTemplet = emailTemplet.replace("$(userName)", userName);
		emailTemplet = emailTemplet.replace("$(type)", type); //操作类型
		emailTemplet = emailTemplet.replace("$(captcha)", code);
		return emailTemplet;
	}

	//FIXME:导入邮件模板出错
	public static void initEmailTemplet() {
		//String url = MailAuthenticator.class.getResource("/emailTemplet.html").getFile();
		String filename = "emailTemplet.html";
		//String url = MailAuthenticator.class.getResource(filename).getFile();
		InputStream stream = MailAuthenticator.class.getClassLoader().getResourceAsStream(filename);
		if (null == stream){
			System.out.println("---------------- email template  not  exist----------------");
			return;
		}
		try {
			String encoding = "UTF-8";
			InputStreamReader reader = new InputStreamReader(stream, encoding);// 考虑到编码格式
			BufferedReader bufferedReader = new BufferedReader(reader);
			String lineTxt = null;
			StringBuilder sb = new StringBuilder();
			while ((lineTxt = bufferedReader.readLine()) != null) {
				sb.append(lineTxt);
			}
			System.setProperty("emailTemplet", sb.toString());
			reader.close();
			System.out.println("---------------- email template loaded successfully ----------------");

		} catch (Exception e) {
			System.out.println("get errors while reading email template ");
			e.printStackTrace();
		}
	}
}