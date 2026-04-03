package io.kimconsulting.maxbots.api.http;

import io.kimconsulting.maxbots.api.model.ActionResult;
import io.kimconsulting.maxbots.api.model.BotInfo;
import io.kimconsulting.maxbots.api.model.Message;
import io.kimconsulting.maxbots.api.model.Subscription;
import io.kimconsulting.maxbots.api.model.UpdatesPage;
import io.kimconsulting.maxbots.api.request.AnswerCallbackRequest;
import io.kimconsulting.maxbots.api.request.CreateSubscriptionRequest;
import io.kimconsulting.maxbots.api.request.EditMessageRequest;
import io.kimconsulting.maxbots.api.request.GetMessagesRequest;
import io.kimconsulting.maxbots.api.request.GetUpdatesRequest;
import io.kimconsulting.maxbots.api.request.SendMessageRequest;
import java.util.List;

public interface MaxApi {
    BotInfo getMe();

    UpdatesPage getUpdates(GetUpdatesRequest request);

    Message sendMessage(SendMessageRequest request);

    Message editMessage(EditMessageRequest request);

    ActionResult deleteMessage(String messageId);

    Message getMessage(String messageId);

    List<Message> getMessages(GetMessagesRequest request);

    List<Subscription> getSubscriptions();

    ActionResult createSubscription(CreateSubscriptionRequest request);

    ActionResult deleteSubscription(String url);

    ActionResult answerCallback(AnswerCallbackRequest request);

    RawMaxApiClient raw();
}
