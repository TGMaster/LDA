package com.spring.boot.controller;

import com.spring.boot.model.request.MailRequestModel;
import com.spring.boot.util.APIStatus;
import com.spring.boot.util.ApplicationException;
import com.spring.boot.util.Constant;
import com.spring.boot.util.RestAPIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class MailController extends BasedAPI{

    @Autowired
    public JavaMailSender emailSender;

    @ResponseBody
    @RequestMapping(value = Constant.EMAIL_API, method = RequestMethod.POST)
    public ResponseEntity<RestAPIResponse> sendEmail(
            @RequestBody MailRequestModel mailRequestModel
    ) {
        MimeMessage msg = emailSender.createMimeMessage();

        boolean multipart = true;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        String today = dateFormat.format(date);

        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg, multipart, "utf-8");

            String content = "<table>"
                    + "<tr><td>FROM:</td> <td><strong>" + mailRequestModel.getName() + "</strong></td></tr>"
                    + "<tr><td>DATE:</td> <td><strong>" + today + "</strong></td></tr>"
                    + "<tr><td>EMAIL:</td> <td><strong>" + mailRequestModel.getEmail() + "</strong></td></tr>"
                    + "<tr><td>PHONE:</td> <td><strong>" + mailRequestModel.getPhone() + "</strong></td></tr>"
                    + "<tr><td colspan=2>MESSAGE:</td></tr>"
                    + "<tr><td colspan=2>" + mailRequestModel.getMessage() + "</td></tr>"
                    + "</table>";

            msg.setContent(content, "text/html");

            helper.setTo(Constant.RECV_EMAIL);
            helper.setSubject("[Thông báo] Bạn có 1 lời nhắn từ website Việt Úc Entertainment");

            this.emailSender.send(msg);
        } catch (MessagingException ex) {
            throw new ApplicationException(APIStatus.ERR_SEND_EMAIL, ex);
        }
        return responseUtil.successResponse("OK");
    }
}
