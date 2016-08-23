package com.uberforflock.model;

import co.flock.www.FlockEventsHandler;
import co.flock.www.model.flockevents.*;
import com.uberforflock.dao.UserTokenDao;
import com.uberforflock.service.MessageService;
import com.uberforflock.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by devesh.k on 23/08/16.
 */
@Service
public class UberFlockEventHandler implements FlockEventsHandler {

    @Autowired
    private UserTokenDao userTokenDao;

    @Autowired
    private RideService rideService;

    @Autowired
    private MessageService messageService;

    @Override
    public void onAppInstall(AppInstall appInstall) {
            userTokenDao.addUserToken(appInstall.getUserId(),appInstall.getUserToken());
    }

    @Override
    public void onAppUnInstall(AppUnInstall appUnInstall) {

    }

    @Override
    public void onChatMessageReceived(ChatReceiveMessage chatReceiveMessage) {

    }

    @Override
    public void onUnfurlUrl(UnfurlUrl unfurlUrl) {

    }

    @Override
    public void onFlockMLAction(FlockMLAction flockMLAction) {

    }

    @Override
    public void onOpenAttachmentWidget(OpenAttachmentWidget openAttachmentWidget) {

    }

    @Override
    public void onPressButton(PressButton pressButton){
        String productId = "";
        String start_latitude = "12.98";
        String start_longitude = "77.64";
        Ride ride = rideService.bookRide(productId, start_latitude, start_longitude);
        try {
            messageService.sendRideMessage(ride, pressButton);
        }catch (Exception e){
            
        }
    }

    @Override
    public void onSlashCommand(SlashCommand slashCommand) {

    }

    @Override
    public void onWidgetAction(WidgetAction widgetAction) {

    }

    @Override
    public void onGroupUpdated(GroupUpdated group) {

    }
}
