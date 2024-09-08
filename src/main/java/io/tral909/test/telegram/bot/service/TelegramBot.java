package io.tral909.test.telegram.bot.service;

import io.tral909.test.telegram.bot.properties.BotProperties;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.stream.Collectors;

@Service
public class TelegramBot extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final CbrService cbrService;

    @Autowired
    public TelegramBot(BotProperties botProperties, CbrService cbrService) {
        super(botProperties.getToken());
        this.botProperties = botProperties;
        this.cbrService = cbrService;
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (update.hasMessage() && message.hasText()) {
            String messageText = message.getText();
            Long chatId = message.getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, message.getChat().getFirstName());
                    break;

                case "/val":
                    // due to 400 from tg - too long message (for long valutes list from cbr)
                    // return first 3 valutes
                    sendMessage(chatId, cbrService.getExchangeRates().getValute()
                            .stream().limit(3)
                            .map(v -> v.getCharCode() + "\n" + v.getName() + "\n" + v.getValue())
                            .collect(Collectors.joining("\n\n")));
                    break;

                default:
                    sendMessage(chatId, "Sorry, command was not recognized");
            }
        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you!";
        sendMessage(chatId, answer);
    }

    @SneakyThrows
    private void sendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textToSend);
        execute(sendMessage);
    }
}
