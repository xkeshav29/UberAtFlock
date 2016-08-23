package com.uberforflock.service;

import co.flock.www.FlockApiClient;
import co.flock.www.model.flockevents.SlashCommand;
import co.flock.www.model.messages.Attachments.*;
import co.flock.www.model.messages.FlockMessage;
import co.flock.www.model.messages.Message;
import com.uberforflock.dao.UserTokenDao;
import com.uberforflock.model.Availability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by kumarke on 8/23/16.
 */
@Service
public class MessageService {
    @Autowired
    private UserTokenDao userTokenDao;
    public void sendAvailabilityMessage(Availability availability, SlashCommand slashCommand) throws  Exception{
        String userToken = userTokenDao.getUserToken(slashCommand.getUserId());
        FlockApiClient flockApiClient = new FlockApiClient(userToken,false);
        Message message = new Message(slashCommand.getChat(),availability.getTimes().size() > 0 ? "We have uber for you" : "Sorry no uber now");
        message.setAppId("d21753b9-c55b-4514-88a5-5c199c1b7801");
        if(availability.getTimes().size() > 0) {
            Attachment[] attachments = new Attachment[1];
            Attachment attachment = new Attachment();
            HtmlView htmlView = new HtmlView();
            StringBuilder sb = new StringBuilder();
            for (Availability.Times times : availability.getTimes()) {
                sb.append(times.getLocalized_display_name() + " in  estimated " + times.getEstimate() + " seconds <br/>");
            }
            htmlView.setInline(sb.toString());
            htmlView.setHeight(17 * availability.getTimes().size());
            View view = new View();
            view.setHtml(htmlView);

            Button[] buttons = new Button[1];
            Button button = new Button();
            button.setName("Book "  + availability.getTimes().get(0).getLocalized_display_name());
            button.setId(availability.getTimes().get(0).getProduct_id());
            Action sendToAppService = new Action();
            sendToAppService.addDispatchEvent();
            button.setAction(sendToAppService);

            buttons[0] = button;

            attachment.setButtons(buttons);

            attachment.setViews(view);
            attachments[0] = attachment;
            message.setAttachments(attachments);

        }
        
        String id = flockApiClient.chatSendMessage(new FlockMessage(message));
    }
}
