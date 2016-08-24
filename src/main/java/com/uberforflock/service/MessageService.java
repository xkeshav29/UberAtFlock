package com.uberforflock.service;

import co.flock.www.FlockApiClient;
import co.flock.www.model.flockevents.PressButton;
import co.flock.www.model.flockevents.SlashCommand;
import co.flock.www.model.messages.Attachments.*;
import co.flock.www.model.messages.FlockMessage;
import co.flock.www.model.messages.Message;
import co.flock.www.model.messages.SendAs;
import com.uberforflock.dao.UserTokenDao;
import com.uberforflock.model.Availability;
import com.uberforflock.model.Ride;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Created by kumarke on 8/23/16.
 */
@Service
public class MessageService {

    @Autowired
    private UserTokenDao userTokenDao;
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private static SendAs sendAs = new SendAs("Uber App", "http://isource.com/wp-content/uploads/2014/12/UBER-icon.png");

    public void sendRideMessage(Ride ride, PressButton pressButton) throws  Exception {
        String userToken = userTokenDao.getUserToken(pressButton.getUserId());
        int eta = (int) (Math.random() * 4) + 1;
        Message message = new Message(pressButton.getChat(), "Your Uber (" + ride.getVehicle().getMake() + " " + ride.getVehicle().getModel() + " - " + ride.getVehicle().getLicense_plate() +") is arriving at your location in " + eta + " minutes. Enjoy the ride !");

        Attachment attachment = new Attachment();
        attachment.setTitle("Your Uber Today");
        HtmlView htmlView = new HtmlView();
        htmlView.setInline("<table width=\"300\"><tbody><tr><td style=\"text-align:center\"><img src=\"" + ride.getDriver().getPicture_url() + "\" alt=\"\" width=\"50\" height=\"50\" /></td><td>" + ride.getDriver().getName() + " (" + ride.getDriver().getPhone_number() + " )</td></tr></tbody></table>");
        htmlView.setHeight(75);
        View view = new View();
        view.setHtml(htmlView);
        view.setFlockml("<a>" + ride.getDriver().getName() + "</a> (" + ride.getDriver().getPhone_number() + ")");
        attachment.setViews(view);
        Button[] buttons = new Button[1];
        Button button = new Button();
        button.setName("Track your Uber");
        button.setId("btnUpdate");
        Action openSideBar = new Action();
        openSideBar.addOpenWidget("https://8271fb9e.ngrok.io/track.html", "sidebar", "modal");
        button.setAction(openSideBar);
        buttons[0] = button;
        attachment.setButtons(buttons);
        Attachment[] attachments = new Attachment[1];
        attachments[0] = attachment;
        message.setAttachments(attachments);
        message.setSendAs(sendAs);
        FlockApiClient flockApiClient = new FlockApiClient(userToken, false);
        String id = flockApiClient.chatSendMessage(new FlockMessage(message));
    }




    public void sendAvailabilityMessage(String lat, String lon, Availability availability, SlashCommand slashCommand){
        String userToken = userTokenDao.getUserToken(slashCommand.getUserId());
        FlockApiClient flockApiClient = new FlockApiClient(userToken,false);

        Message message = new Message(slashCommand.getChat(),availability.getTimes().size() > 0 ? "Which one you would like to book ?" : "Sorry no uber now");
        message.setSendAs(sendAs);
        message.setAppId("d21753b9-c55b-4514-88a5-5c199c1b7801");
        HashMap<String,String> carImages = new HashMap<>();
        carImages.put("uberX","http://d1a3f4spazzrp4.cloudfront.net/car-types/mono/mono-uberx.png");
        carImages.put("uberGO","http://d1a3f4spazzrp4.cloudfront.net/car-types/mono/mono-ubergo.png");
        carImages.put("uberPOOL","http://d1a3f4spazzrp4.cloudfront.net/car-types/mono/mono-uberpool.png");
        carImages.put("uberXL","http://d1a3f4spazzrp4.cloudfront.net/car-types/mono/mono-uberxl.png");
        if(availability.getTimes().size() > 0) {
            Attachment[] attachments = new Attachment[1];
            Attachment attachment = new Attachment();
            HtmlView htmlView = new HtmlView();
            StringBuilder sb = new StringBuilder();
            String cssBody = "<style type=\"text/css\">.light { color : #737373; } body {margin:0;padding:0;color: #333333;} ul {margin:0;padding: 5px 0 0 20px;} li {line-height:20px} a:link {color: #3B5998;} a:visited {color: #5796DD;} a:hover {color: #3A5998;}</style>";
            String fontStyle = "font-family:Lucida Grande,Arial,sans-serif;font-size:14px;line-height:20px";
            sb.append(cssBody);
            sb.append("<div style=\"" + fontStyle + "\">");
            sb.append("<table width=\"350\">");
            for (Availability.Times times : availability.getTimes()) {
                String img = carImages.getOrDefault(times.getLocalized_display_name(),"http://d1a3f4spazzrp4.cloudfront.net/car-types/mono/mono-ubergo.png");
                sb.append("<tr><td style=\"text-align: center;\"><img src=\"" + img +"\" alt=\"\" width=\"25\" height=\"25\" /></td><td><b>" + times.getLocalized_display_name() + "</b></td> <td>" + (times.getEstimate() / 60) + " minutes</td></tr>");
            }
            sb.append("</table>");
            sb.append("</div>");
            htmlView.setInline(sb.toString());
            htmlView.setHeight(35 * availability.getTimes().size());
            View view = new View();
            view.setHtml(htmlView);

            int totalButtons = availability.getTimes().size() > 3 ? 3 : availability.getTimes().size();

            Button[] buttons = new Button[totalButtons];
            for(int i = 0 ; i < totalButtons ; i++) {
                Button button = new Button();
                button.setIcon("http://d1a3f4spazzrp4.cloudfront.net/car-types/mono/mono-uberpool.png");
                button.setName("Book " + availability.getTimes().get(i).getLocalized_display_name());
                button.setId(availability.getTimes().get(i).getProduct_id());
                Action sendToAppService = new Action();
                sendToAppService.addDispatchEvent();
                button.setAction(sendToAppService);
                buttons[i] = button;
            }

            attachment.setButtons(buttons);

            attachment.setViews(view);
            attachments[0] = attachment;
            message.setAttachments(attachments);

        }

        try {
            String id = flockApiClient.chatSendMessage(new FlockMessage(message));
        }catch (Exception e){
            logger.error("Error sending message", e);
        }
    }
}
