package com.uberforflock.service;

import co.flock.www.FlockApiClient;
import co.flock.www.model.flockevents.SlashCommand;
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
        String id = flockApiClient.chatSendMessage(new FlockMessage(message));
    }
}
